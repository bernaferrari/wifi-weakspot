package com.pulsewave.app.feature.analyze

import androidx.compose.runtime.getValue
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import com.pulsewave.app.core.wifi.groupByRadio
import androidx.compose.ui.graphics.Brush
import com.pulsewave.app.core.wifi.occupiedFrequencySegments
import kotlin.math.abs
import kotlin.math.pow

/** Plot range shared by both charts, matching the signal-quality scale. */
internal const val ChartTopDbm = -25
internal const val ChartBottomDbm = -95

internal fun dbmToFraction(rssi: Float): Float =
    ((rssi - ChartBottomDbm) / (ChartTopDbm - ChartBottomDbm)).coerceIn(0f, 1f)

/** How many networks get their own color; the rest become quiet context. */
internal const val FeaturedNetworks = 9

/** Names drawn on peaks when nothing is focused, besides your own network. */
private const val UnfocusedLabels = 4

/**
 * The same network gets the same color in the chart, its timeline, and the list.
 * Only the strongest few (plus yours) are colored, so 100 networks still read clearly;
 * colors are handed out in a stable order so they don't shuffle between scans.
 */
class NetworkPalette internal constructor(
    private val assigned: Map<String, Color>,
    private val connectedBssid: String?,
    private val primary: Color,
    private val quiet: Color,
    /** Guest and hidden names point at the radio that sends them, so they share its color. */
    private val leadOf: Map<String, String> = emptyMap()
) {
    private fun key(bssid: String) = leadOf[bssid.lowercase()] ?: bssid.lowercase()
    fun isYou(bssid: String) = connectedBssid != null && bssid.equals(connectedBssid, ignoreCase = true)
    private fun isYourRadio(bssid: String) = connectedBssid != null && key(bssid) == key(connectedBssid)
    fun isFeatured(bssid: String) = isYourRadio(bssid) || key(bssid) in assigned
    fun color(bssid: String): Color = when {
        isYourRadio(bssid) -> primary
        else -> assigned[key(bssid)] ?: quiet
    }
}

@Composable
fun rememberNetworkPalette(networks: List<WifiNetwork>, connectedBssid: String?): NetworkPalette {
    val palette = graphSeriesColors()
    val primary = MaterialTheme.colorScheme.primary
    val quiet = MaterialTheme.colorScheme.outline
    val groups = remember(networks, connectedBssid) { groupByRadio(networks, connectedBssid) }
    val leadOf = remember(groups) {
        groups.flatMap { group -> group.members.map { it.bssid.lowercase() to group.lead.bssid.lowercase() } }.toMap()
    }
    val featured = groups
        .map { it.lead }
        .filterNot { connectedBssid != null && it.bssid.equals(connectedBssid, ignoreCase = true) }
        .sortedByDescending { it.rssi }
        .take(FeaturedNetworks)
        .map { it.bssid.lowercase() }
        .sorted()
    return remember(palette, primary, quiet, connectedBssid, featured, leadOf) {
        NetworkPalette(featured.withIndex().associate { (i, id) -> id to palette[i % palette.size] }, connectedBssid, primary, quiet, leadOf)
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
    val palette = rememberNetworkPalette(networks, connectedBssid)
    // One hill per radio: a router's guest and hidden names share its airtime, so they share its hill.
    val groups = remember(networks, connectedBssid) { groupByRadio(networks, connectedBssid) }
    val radios = remember(groups) { groups.map { it.lead } }
    val extraNames = remember(groups) { groups.associate { it.lead.bssid to it.extraNames } }
    val focused = selectedNetwork?.let { pick -> groups.firstOrNull { group -> group.members.any { it.bssid == pick.bssid } }?.lead }
    val latestFocus by androidx.compose.runtime.rememberUpdatedState(focused)
    val haptics = com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback()
    val scheme = MaterialTheme.colorScheme
    val zones = SignalQuality.entries.associateWith { signalColors(it).container }
    val labelStyle = MaterialTheme.typography.labelSmall
    val crowdColors = CrowdColors(
        calm = signalColors(SignalQuality.Excellent),
        busy = signalColors(SignalQuality.Fair),
        packed = signalColors(SignalQuality.DeadZone),
        quietText = MaterialTheme.colorScheme.onSurfaceVariant
    )
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
            .pointerInput(radios, selectedBand) {
                // Tapping the same spot again steps through overlapping hills, strongest first.
                detectTapGestures { tap ->
                    val plot = plotRect(size.width.toFloat(), size.height.toFloat(), this, selectedBand)
                    val here = networksAt(tap, radios, selectedBand, plot)
                    val current = here.indexOfFirst { it.bssid == latestFocus?.bssid }
                    onNetworkSelected?.invoke(if (here.isEmpty()) null else here[(current + 1) % here.size])
                }
            }
            .pointerInput(radios, selectedBand) {
                // Press and hold, then slide: the focus follows your finger across the channels.
                var last: String? = null
                fun scrub(at: Offset) {
                    val plot = plotRect(size.width.toFloat(), size.height.toFloat(), this, selectedBand)
                    val pick = networksAt(Offset(at.x, plot.bottom), radios, selectedBand, plot, anyHeight = true).firstOrNull() ?: return
                    if (pick.bssid != last) {
                        last = pick.bssid
                        haptics.light()
                        onNetworkSelected?.invoke(pick)
                    }
                }
                detectDragGesturesAfterLongPress(onDragStart = { last = null; scrub(it) }) { change, _ -> scrub(change.position) }
            }
    ) {
        val plot = plotRect(size.width, size.height, this, selectedBand)
        drawZones(plot, zones)
        drawQualityLabels(plot, textMeasurer, labelStyle.copy(color = scheme.onSurfaceVariant.copy(alpha = 0.7f)))
        drawChannelAxis(plot, selectedBand, textMeasurer, labelStyle, scheme.onSurfaceVariant, scheme.onSurface, scheme.outlineVariant)
        if (selectedBand == FrequencyBand.BAND_2_4 && radios.size > 3) {
            drawCrowdRow(plot, radios, textMeasurer, labelStyle, crowdColors)
        }

        val dimOthers = focused != null
        // Weakest first so strong networks sit on top.
        // Background networks first, as thin quiet outlines; featured ones on top, weakest first.
        radios.sortedWith(compareBy<WifiNetwork> { palette.isFeatured(it.bssid) || it.bssid == focused?.bssid }.thenBy { it.rssi })
            .forEach { network ->
                val featured = palette.isFeatured(network.bssid) || network.bssid == focused?.bssid
                val emphasized = network.bssid == focused?.bssid || palette.isYou(network.bssid)
                val alpha = when {
                    dimOthers && network.bssid != focused?.bssid -> 0.2f
                    !featured -> 0.45f
                    else -> 1f
                }
                drawHills(network, palette.color(network.bssid).copy(alpha = alpha), plot, selectedBand, grow.value, emphasized, quiet = !featured)
            }

        // Labels: selected and connected first, then strongest, skipping any that would collide.
        // Unfocused, only a few strongest are named; the list's color dots identify the rest.
        val placed = mutableListOf<Rect>()
        var unfocusedLabels = 0
        val labelOrder = radios.sortedWith(
            compareByDescending<WifiNetwork> { it.bssid == focused?.bssid }
                .thenByDescending { connectedBssid != null && it.bssid.equals(connectedBssid, true) }
                .thenByDescending { it.rssi }
        )
        labelOrder.forEach { network ->
            val isYou = palette.isYou(network.bssid)
            if (dimOthers && network.bssid != focused?.bssid && !isYou) return@forEach
            if (!palette.isFeatured(network.bssid) && network.bssid != focused?.bssid) return@forEach
            if (!dimOthers && !isYou && unfocusedLabels >= UnfocusedLabels) return@forEach
            val extra = extraNames[network.bssid] ?: 0
            val name = network.ssid.ifEmpty { "Hidden" }.let { if (it.length > 16) it.take(15) + "…" else it } +
                (if (isYou) " (you)" else "") + (if (extra > 0) " +$extra" else "")
            val layout = textMeasurer.measure(name, peakStyle.copy(color = palette.color(network.bssid)), maxLines = 1, overflow = TextOverflow.Clip)
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
            if (!dimOthers && !isYou) unfocusedLabels++
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

private fun plotRect(width: Float, height: Float, density: Density, band: FrequencyBand): Rect = with(density) {
    // Room below the channel numbers for the 2.4 GHz crowd row; other bands only need the numbers.
    val below = if (band == FrequencyBand.BAND_2_4) 52.dp else 28.dp
    // The gutter holds the signal words, so they never collide with hills or names.
    Rect(left = QualityGutter.toPx(), top = 12.dp.toPx(), right = width - 12.dp.toPx(), bottom = height - below.toPx())
}

internal data class CrowdColors(
    val calm: com.pulsewave.app.core.designsystem.SignalColors,
    val busy: com.pulsewave.app.core.designsystem.SignalColors,
    val packed: com.pulsewave.app.core.designsystem.SignalColors,
    val quietText: Color
)

/** Networks weaker than this rarely slow yours down. */
internal const val CrowdFloorDbm = -80

/** How many networks overlap each 2.4 GHz channel: the number that actually decides congestion. */
private fun DrawScope.drawCrowdRow(plot: Rect, networks: List<WifiNetwork>, measurer: TextMeasurer, style: TextStyle, colors: CrowdColors) {
    val y = plot.bottom + 30.dp.toPx()
    val pill = Size(20.dp.toPx(), 16.dp.toPx())
    val caption = measurer.measure("Busy", style.copy(color = colors.quietText))
    drawText(caption, topLeft = Offset(plot.left - caption.size.width - 8.dp.toPx(), y + (pill.height - caption.size.height) / 2f))
    (1..13).forEach { channel ->
        val center = 2407 + channel * 5
        // Only networks loud enough to compete for airtime; distant ones barely matter.
        val count = networks.count { network ->
            network.rssi >= CrowdFloorDbm && occupiedFrequencySegments(network).any { it.overlaps(center) }
        }
        if (count == 0) return@forEach
        val tone = when { count <= 3 -> colors.calm; count <= 8 -> colors.busy; else -> colors.packed }
        val x = plot.left + mapFreqToNormX(center.toFloat(), FrequencyBand.BAND_2_4) * plot.width
        drawRoundRect(tone.container, topLeft = Offset(x - pill.width / 2, y), size = pill, cornerRadius = CornerRadius(pill.height / 2))
        val layout = measurer.measure("$count", style.copy(color = tone.onContainer, fontWeight = FontWeight.Bold))
        drawText(layout, topLeft = Offset(x - layout.size.width / 2f, y + (pill.height - layout.size.height) / 2f))
    }
}

/**
 * A soft wash from "strong" at the top to "weak" at the bottom. Readings drift by several dB
 * between scans, so the levels blend into each other instead of meeting at hard lines.
 */
internal fun DrawScope.drawZones(plot: Rect, zones: Map<SignalQuality, Color>) {
    val stops = SignalQuality.entries.map { quality ->
        val upper = SignalQuality.entries.getOrNull(quality.ordinal - 1)?.minRssi ?: ChartTopDbm
        val lower = if (quality == SignalQuality.DeadZone) ChartBottomDbm else quality.minRssi
        val middle = (upper + lower) / 2f
        (1f - dbmToFraction(middle)) to zones.getValue(quality).copy(alpha = 0.12f)
    }.sortedBy { it.first }
    drawRoundRect(
        Brush.verticalGradient(colorStops = stops.toTypedArray(), startY = plot.top, endY = plot.bottom),
        topLeft = plot.topLeft,
        size = plot.size,
        cornerRadius = CornerRadius(12.dp.toPx())
    )
}

/** Room left of the plot for the signal words. */
internal val QualityGutter = 52.dp

/** Words instead of dBm numbers: what the level means matters more than the exact reading. */
internal fun DrawScope.drawQualityLabels(plot: Rect, measurer: TextMeasurer, style: TextStyle) {
    listOf("Strong" to -40, "Okay" to -71, "Weak" to -88).forEach { (label, dbm) ->
        val layout = measurer.measure(label, style)
        val y = plot.bottom - plot.height * dbmToFraction(dbm.toFloat()) - layout.size.height / 2f
        drawText(layout, topLeft = Offset(plot.left - layout.size.width - 8.dp.toPx(), y))
    }
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
    emphasized: Boolean,
    quiet: Boolean = false
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
        val fill = when { emphasized -> 0.6f; quiet -> 0.08f; else -> 0.32f }
        val stroke = when { emphasized -> 3f; quiet -> 1f; else -> 2f }
        // Fades toward the floor, so stacked hills stay readable and the peak carries the color.
        drawPath(
            outline,
            Brush.verticalGradient(
                listOf(color.copy(alpha = color.alpha * fill), color.copy(alpha = 0f)),
                startY = plot.bottom - peak,
                endY = plot.bottom
            )
        )
        drawPath(outline, color, style = Stroke(width = stroke.dp.toPx(), cap = StrokeCap.Round))
    }
}

/** Hills under the finger, strongest first; [anyHeight] ignores how high the finger is. */
private fun networksAt(tap: Offset, networks: List<WifiNetwork>, band: FrequencyBand, plot: Rect, anyHeight: Boolean = false): List<WifiNetwork> {
    if (!plot.inflate(24f).contains(tap)) return emptyList()
    return networks.filter { network ->
        occupiedFrequencySegments(network).any { segment ->
            val left = plot.left + mapFreqToNormX(segment.lowerMhz.toFloat(), band) * plot.width
            val right = plot.left + mapFreqToNormX(segment.upperMhz.toFloat(), band) * plot.width
            tap.x in left..right
        }
    }.filter { anyHeight || tap.y >= plot.bottom - plot.height * dbmToFraction(it.rssi.toFloat()) - 48f }
        .sortedByDescending { it.rssi }
}

// Each band starts where its first channel's signal starts and ends where its last one ends,
// so a network on channel 1 (2402-2422 MHz) touches the axis instead of floating after a gap.
// 2.4 GHz folds channel 14 (Japan only) next to 13; 5 GHz compresses the 5350-5470 MHz gap.
internal fun mapFreqToNormX(freq: Float, band: FrequencyBand): Float = when (band) {
    FrequencyBand.BAND_2_4 -> {
        val visual = if (freq > 2472f) freq - 7f else freq
        (visual - 2402f) / (2482f - 2402f)
    }
    FrequencyBand.BAND_5 -> when {
        freq <= 5350f -> (freq - 5170f) / (5350f - 5170f) * 0.38f
        freq >= 5470f -> 0.42f + (freq - 5470f) / (5835f - 5470f) * 0.58f
        else -> 0.38f + (freq - 5350f) / (5470f - 5350f) * 0.04f
    }
    FrequencyBand.BAND_6 -> (freq - 5945f) / (7125f - 5945f)
}
