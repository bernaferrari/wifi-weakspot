package com.pulsewave.app.feature.analyze

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.graphSeriesColors
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.wifi.SignalQuality
import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.wifi.frequencyToChannel
import com.pulsewave.app.core.wifi.occupiedFrequencySegments
import kotlin.math.abs
import kotlin.math.pow

/** Plot range shared by both charts, matching the signal-quality scale. */
internal const val ChartTopDbm = -25
internal const val ChartBottomDbm = -95

internal fun dbmToFraction(rssi: Float): Float =
    ((rssi - ChartBottomDbm) / (ChartTopDbm - ChartBottomDbm)).coerceIn(0f, 1f)

/**
 * The same network gets the same color in the chart, its timeline, and the list. Colors are
 * handed out in a stable order within what's on screen, so neighbors never share one.
 */
@Composable
fun rememberNetworkColors(networks: List<WifiNetwork>, connectedBssid: String?): (String) -> Color {
    val palette = graphSeriesColors()
    val primary = MaterialTheme.colorScheme.primary
    val ids = networks.map { it.bssid.lowercase() }.sorted()
    return remember(palette, primary, connectedBssid, ids) {
        val others = ids.filterNot { connectedBssid != null && it.equals(connectedBssid, ignoreCase = true) }
        val assigned = others.withIndex().associate { (i, id) -> id to palette[i % palette.size] }
        val lookup: (String) -> Color = { bssid ->
            if (connectedBssid != null && bssid.equals(connectedBssid, ignoreCase = true)) primary
            else assigned[bssid.lowercase()] ?: palette[(bssid.lowercase().hashCode() and Int.MAX_VALUE) % palette.size]
        }
        lookup
    }
}

/**
 * Each network is a hill exactly as wide as the spectrum it occupies and as tall as its
 * signal. Names sit on the peaks; overlapping hills mean networks sharing airtime.
 */
@Composable
fun ChannelGraph(
    networks: List<WifiNetwork>,
    selectedBand: FrequencyBand = FrequencyBand.BAND_2_4,
    selectedNetwork: WifiNetwork? = null,
    connectedBssid: String? = null,
    modifier: Modifier = Modifier,
    onNetworkSelected: ((WifiNetwork?) -> Unit)? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val colorFor = rememberNetworkColors(networks, connectedBssid)
    val scheme = MaterialTheme.colorScheme
    val zones = SignalQuality.entries.associateWith { signalColors(it).container }
    val labelStyle = MaterialTheme.typography.labelSmall
    val peakStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)

    // Hills rise once whenever the band or the set of networks changes; no idle looping.
    val grow = remember { Animatable(0f) }
    val growSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(selectedBand, networks.size) {
        grow.snapTo(0f)
        grow.animateTo(1f, growSpec)
    }

    val busiest = remember(networks) {
        networks.groupBy { frequencyToChannel(it.frequency) }.maxByOrNull { it.value.size }
    }
    Canvas(
        modifier
            .semantics {
                contentDescription = buildString {
                    append("Channel graph, ${networks.size} networks.")
                    busiest?.let { append(" Busiest channel ${it.key} with ${it.value.size}.") }
                }
            }
            .pointerInput(networks, selectedBand) {
                detectTapGestures { tap ->
                    val plot = plotRect(size.width.toFloat(), size.height.toFloat(), this)
                    onNetworkSelected?.invoke(networkAt(tap, networks, selectedBand, plot))
                }
            }
    ) {
        val plot = plotRect(size.width, size.height, this)
        drawZones(plot, zones)
        drawDbmAxis(plot, textMeasurer, labelStyle.copy(color = scheme.onSurfaceVariant))
        drawChannelAxis(plot, selectedBand, textMeasurer, labelStyle, scheme.onSurfaceVariant, scheme.onSurface, scheme.outlineVariant)

        val dimOthers = selectedNetwork != null
        // Weakest first so strong networks sit on top.
        networks.sortedBy { it.rssi }.forEach { network ->
            val isYou = connectedBssid != null && network.bssid.equals(connectedBssid, true)
            val emphasized = network.bssid == selectedNetwork?.bssid || isYou
            val alpha = if (dimOthers && network.bssid != selectedNetwork?.bssid) 0.25f else 1f
            drawHills(network, colorFor(network.bssid).copy(alpha = alpha), plot, selectedBand, grow.value, emphasized)
        }

        // Labels: selected and connected first, then strongest, skipping any that would collide.
        val placed = mutableListOf<Rect>()
        val labelOrder = networks.sortedWith(
            compareByDescending<WifiNetwork> { it.bssid == selectedNetwork?.bssid }
                .thenByDescending { connectedBssid != null && it.bssid.equals(connectedBssid, true) }
                .thenByDescending { it.rssi }
        )
        labelOrder.forEach { network ->
            val isYou = connectedBssid != null && network.bssid.equals(connectedBssid, true)
            if (dimOthers && network.bssid != selectedNetwork?.bssid && !isYou) return@forEach
            val name = network.ssid.ifEmpty { "Hidden" }.let { if (it.length > 16) it.take(15) + "…" else it } +
                if (isYou) " (you)" else ""
            val layout = textMeasurer.measure(name, peakStyle.copy(color = colorFor(network.bssid)), maxLines = 1, overflow = TextOverflow.Clip)
            val segment = occupiedFrequencySegments(network).first()
            val x = plot.left + mapFreqToNormX(segment.centerMhz.toFloat(), selectedBand) * plot.width
            val peakY = plot.bottom - plot.height * dbmToFraction(network.rssi.toFloat()) * grow.value
            var rect = Rect(
                Offset(
                    (x - layout.size.width / 2f).coerceIn(plot.left, (plot.right - layout.size.width).coerceAtLeast(plot.left)),
                    peakY - layout.size.height - 4.dp.toPx()
                ),
                Size(layout.size.width.toFloat(), layout.size.height.toFloat())
            )
            // Nudge up once if the spot is taken; otherwise the list below still names it.
            if (placed.any { it.overlaps(rect) }) rect = rect.translate(0f, -rect.height - 2.dp.toPx())
            if (placed.any { it.overlaps(rect) } || rect.top < 0f) return@forEach
            placed += rect
            drawRoundRect(
                scheme.surfaceContainerLow.copy(alpha = 0.85f),
                topLeft = rect.topLeft - Offset(4.dp.toPx(), 1.dp.toPx()),
                size = Size(rect.width + 8.dp.toPx(), rect.height + 2.dp.toPx()),
                cornerRadius = CornerRadius(6.dp.toPx())
            )
            drawText(layout, topLeft = rect.topLeft)
        }
    }
}

private fun plotRect(width: Float, height: Float, density: Density): Rect = with(density) {
    Rect(left = 40.dp.toPx(), top = 28.dp.toPx(), right = width - 12.dp.toPx(), bottom = height - 28.dp.toPx())
}

/** Faint bands for each signal level, so "weak" is visible without reading numbers. */
internal fun DrawScope.drawZones(plot: Rect, zones: Map<SignalQuality, Color>) {
    var upper = ChartTopDbm.toFloat()
    SignalQuality.entries.forEach { quality ->
        val lower = if (quality == SignalQuality.DeadZone) ChartBottomDbm.toFloat() else quality.minRssi.toFloat()
        val top = plot.bottom - plot.height * dbmToFraction(upper)
        val bottom = plot.bottom - plot.height * dbmToFraction(lower)
        if (bottom > top) drawRect(zones.getValue(quality).copy(alpha = 0.35f), Offset(plot.left, top), Size(plot.width, bottom - top))
        upper = lower
    }
}

internal fun DrawScope.drawDbmAxis(plot: Rect, measurer: TextMeasurer, style: TextStyle) {
    listOf(-30, -50, -70, -90).forEach { dbm ->
        val y = plot.bottom - plot.height * dbmToFraction(dbm.toFloat())
        val layout = measurer.measure("$dbm", style)
        drawText(layout, topLeft = Offset(plot.left - layout.size.width - 8.dp.toPx(), y - layout.size.height / 2f))
    }
    val unit = measurer.measure("dBm", style)
    drawText(unit, topLeft = Offset(plot.left - unit.size.width - 8.dp.toPx(), plot.top - unit.size.height - 6.dp.toPx()))
}

private fun DrawScope.drawChannelAxis(
    plot: Rect,
    band: FrequencyBand,
    measurer: TextMeasurer,
    style: TextStyle,
    quiet: Color,
    strong: Color,
    grid: Color
) {
    drawLine(grid, Offset(plot.left, plot.bottom), Offset(plot.right, plot.bottom), strokeWidth = 1.dp.toPx())
    channelMarkers(band).forEach { (channel, freq) ->
        val x = plot.left + mapFreqToNormX(freq.toFloat(), band) * plot.width
        // 1, 6 and 11 are the only 2.4 GHz channels that don't overlap each other.
        val key = band == FrequencyBand.BAND_2_4 && channel in setOf(1, 6, 11)
        val layout = measurer.measure(
            "$channel",
            style.copy(color = if (key) strong else quiet, fontWeight = if (key) FontWeight.Bold else FontWeight.Normal)
        )
        drawText(layout, topLeft = Offset(x - layout.size.width / 2f, plot.bottom + 6.dp.toPx()))
    }
}

private fun channelMarkers(band: FrequencyBand): List<Pair<Int, Int>> = when (band) {
    FrequencyBand.BAND_2_4 -> (1..13).map { it to 2407 + it * 5 }
    FrequencyBand.BAND_5 -> listOf(36, 52, 64, 100, 116, 132, 149, 165).map { it to 5000 + it * 5 }
    FrequencyBand.BAND_6 -> listOf(1, 33, 65, 97, 129, 161, 193, 225).map { it to 5950 + it * 5 }
}

/** A flat-topped dome spanning exactly the occupied segment(s), like a channel's spectral mask. */
private fun DrawScope.drawHills(
    network: WifiNetwork,
    color: Color,
    plot: Rect,
    band: FrequencyBand,
    grow: Float,
    emphasized: Boolean
) {
    val peak = plot.height * dbmToFraction(network.rssi.toFloat()) * grow
    occupiedFrequencySegments(network).forEach { segment ->
        val left = plot.left + mapFreqToNormX(segment.lowerMhz.toFloat(), band).coerceIn(0f, 1f) * plot.width
        val right = plot.left + mapFreqToNormX(segment.upperMhz.toFloat(), band).coerceIn(0f, 1f) * plot.width
        if (right - left < 1f) return@forEach
        val outline = Path().apply {
            moveTo(left, plot.bottom)
            val steps = 32
            for (i in 0..steps) {
                val t = i / steps.toFloat()
                val shape = 1f - abs(2 * t - 1).toDouble().pow(3.0).toFloat()
                lineTo(left + (right - left) * t, plot.bottom - peak * shape)
            }
            lineTo(right, plot.bottom)
        }
        drawPath(outline, color.copy(alpha = color.alpha * if (emphasized) 0.32f else 0.16f))
        drawPath(outline, color, style = Stroke(width = (if (emphasized) 3.5f else 2f).dp.toPx(), cap = StrokeCap.Round))
    }
}

/** The tallest hill under the finger wins; tapping empty space clears the selection. */
private fun networkAt(tap: Offset, networks: List<WifiNetwork>, band: FrequencyBand, plot: Rect): WifiNetwork? {
    if (!plot.inflate(24f).contains(tap)) return null
    return networks.filter { network ->
        occupiedFrequencySegments(network).any { segment ->
            val left = plot.left + mapFreqToNormX(segment.lowerMhz.toFloat(), band) * plot.width
            val right = plot.left + mapFreqToNormX(segment.upperMhz.toFloat(), band) * plot.width
            tap.x in left..right
        }
    }.filter { tap.y >= plot.bottom - plot.height * dbmToFraction(it.rssi.toFloat()) - 48f }
        .maxByOrNull { it.rssi }
}

// 2.4 GHz uses uniform spacing so channel 14 doesn't leave a gap.
// 5 GHz compresses the 5350-5470 MHz gap where no channels exist.
internal fun mapFreqToNormX(freq: Float, band: FrequencyBand): Float = when (band) {
    FrequencyBand.BAND_2_4 -> {
        // Channel 14 (Japan only) is folded next to 13 instead of widening the axis.
        val visual = if (freq > 2472f) freq - 7f else freq
        (visual - 2400f) / (2484f - 2400f)
    }
    FrequencyBand.BAND_5 -> when {
        freq <= 5350f -> (freq - 5150f) / (5350f - 5150f) * 0.4f
        freq >= 5470f -> 0.45f + (freq - 5470f) / (5850f - 5470f) * 0.55f
        else -> 0.4f + (freq - 5350f) / (5470f - 5350f) * 0.05f
    }
    FrequencyBand.BAND_6 -> (freq - 5925f) / (7125f - 5925f)
}
