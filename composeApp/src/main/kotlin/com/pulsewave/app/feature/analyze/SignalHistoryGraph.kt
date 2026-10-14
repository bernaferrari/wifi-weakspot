package com.pulsewave.app.feature.analyze

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.wifi.SignalQuality
import com.pulsewave.app.core.wifi.WifiNetwork
import kotlin.math.abs

private const val MaxLines = 8

/**
 * How steady each network has been across recent scans: a flat line is a stable link,
 * a jagged one drops in and out. Shows the strongest few plus whatever is selected.
 */
@Composable
fun SignalHistoryGraph(
    history: Map<String, List<Int>>,
    networks: List<WifiNetwork>,
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
    val nameStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)

    val shown = networks
        .filter { (history[it.bssid]?.size ?: 0) > 0 }
        .sortedWith(
            compareByDescending<WifiNetwork> { it.bssid == selectedNetwork?.bssid }
                .thenByDescending { connectedBssid != null && it.bssid.equals(connectedBssid, true) }
                .thenByDescending { it.rssi }
        )
        .take(MaxLines)
    val samples = shown.maxOfOrNull { history[it.bssid]?.size ?: 0 } ?: 0

    if (samples < 2) {
        Box(modifier, contentAlignment = Alignment.Center) {
            Text(
                "Collecting readings… The lines appear after a couple of scans.",
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant
            )
        }
        return
    }

    Canvas(
        modifier
            .semantics {
                contentDescription = "Signal over the last $samples scans for ${shown.size} networks."
            }
            .pointerInput(shown, history) {
                detectTapGestures { tap ->
                    val plot = timelinePlot(size.width.toFloat(), size.height.toFloat(), this)
                    val nearest = shown.minByOrNull { network ->
                        val values = history[network.bssid].orEmpty()
                        if (values.isEmpty()) Float.MAX_VALUE else {
                            val index = (((tap.x - plot.left) / plot.width) * (samples - 1)).toInt().coerceIn(0, samples - 1)
                            val value = values.getOrNull(index - (samples - values.size)) ?: values.first()
                            abs(plot.bottom - plot.height * dbmToFraction(value.toFloat()) - tap.y)
                        }
                    }
                    onNetworkSelected?.invoke(nearest)
                }
            }
    ) {
        val plot = timelinePlot(size.width, size.height, this)
        drawZones(plot, zones)
        drawDbmAxis(plot, textMeasurer, labelStyle.copy(color = scheme.onSurfaceVariant))
        listOf("older" to plot.left, "now" to plot.right).forEach { (label, x) ->
            val layout = textMeasurer.measure(label, labelStyle.copy(color = scheme.onSurfaceVariant))
            val left = if (label == "now") x - layout.size.width else x
            drawText(layout, topLeft = Offset(left, plot.bottom + 6.dp.toPx()))
        }

        val step = plot.width / (samples - 1)
        val dim = selectedNetwork != null
        val labels = mutableListOf<Rect>()
        // Draw back to front so the most important line sits on top.
        shown.reversed().forEach { network ->
            val values = history[network.bssid].orEmpty()
            val offset = samples - values.size
            val isYou = connectedBssid != null && network.bssid.equals(connectedBssid, true)
            val emphasized = network.bssid == selectedNetwork?.bssid || isYou
            val color = colorFor(network.bssid).copy(alpha = if (dim && network.bssid != selectedNetwork?.bssid) 0.25f else 1f)
            fun point(i: Int) = Offset(
                plot.left + (i + offset) * step,
                plot.bottom - plot.height * dbmToFraction(values[i].toFloat())
            )
            val path = Path().apply {
                val first = point(0)
                moveTo(first.x, first.y)
                for (i in 1 until values.size) {
                    val previous = point(i - 1)
                    val current = point(i)
                    val midX = (previous.x + current.x) / 2
                    cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
                }
            }
            drawPath(path, color, style = Stroke((if (emphasized) 3.5f else 2f).dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
            val end = point(values.lastIndex)
            drawCircle(color, radius = (if (emphasized) 5f else 3.5f).dp.toPx(), center = end)
        }
        // Names at the newest end, front to back, skipping collisions.
        shown.forEach { network ->
            val values = history[network.bssid].orEmpty()
            val isYou = connectedBssid != null && network.bssid.equals(connectedBssid, true)
            if (dim && network.bssid != selectedNetwork?.bssid && !isYou) return@forEach
            val name = network.ssid.ifEmpty { "Hidden" }.let { if (it.length > 16) it.take(15) + "…" else it } +
                if (isYou) " (you)" else ""
            val layout = textMeasurer.measure(name, nameStyle.copy(color = colorFor(network.bssid)), maxLines = 1, overflow = TextOverflow.Clip)
            val y = plot.bottom - plot.height * dbmToFraction(values.last().toFloat())
            val above = Rect(
                Offset(plot.right - layout.size.width - 10.dp.toPx(), y - layout.size.height - 6.dp.toPx()),
                Size(layout.size.width.toFloat(), layout.size.height.toFloat())
            )
            // Prefer above the line end; fall back to below when another name is there.
            val rect = listOf(above, above.translate(0f, layout.size.height + 12.dp.toPx()))
                .firstOrNull { candidate -> labels.none { it.overlaps(candidate) } && candidate.top >= 0f && candidate.bottom <= size.height }
                ?: return@forEach
            labels += rect
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

private fun timelinePlot(width: Float, height: Float, density: Density): Rect = with(density) {
    Rect(left = 40.dp.toPx(), top = 24.dp.toPx(), right = width - 12.dp.toPx(), bottom = height - 26.dp.toPx())
}
