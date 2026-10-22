package com.pulsewave.app.feature.analyze

import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.wifi.SignalQuality
import com.pulsewave.app.core.wifi.WifiNetwork
import kotlin.math.abs
import kotlin.math.roundToInt

private const val MaxLines = 8

/** Recent observations on a fixed signal scale. Horizontal drags inspect; vertical drags scroll. */
@Composable
fun SignalHistoryGraph(
    history: Map<String, List<Int>>,
    networks: List<WifiNetwork>,
    selectedNetwork: WifiNetwork? = null,
    connectedBssid: String? = null,
    modifier: Modifier = Modifier,
    onNetworkSelected: ((WifiNetwork?) -> Unit)? = null
) {
    val display = com.pulsewave.app.core.designsystem.LocalSignalDisplay.current
    val measurer = rememberTextMeasurer()
    val scheme = MaterialTheme.colorScheme
    val zones = SignalQuality.entries.associateWith { signalColors(it).container }
    val labelStyle = MaterialTheme.typography.labelSmall
    val nameStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
    var snapshotNetworks by remember { mutableStateOf<List<WifiNetwork>?>(null) }
    val displayNetworks = snapshotNetworks ?: networks
    val palette = rememberNetworkPalette(displayNetworks, connectedBssid)
    val leads = remember(displayNetworks, connectedBssid) { com.pulsewave.app.core.wifi.groupByRadio(displayNetworks, connectedBssid) }
    val selected = selectedNetwork?.let { pick -> leads.firstOrNull { group -> group.members.any { it.bssid == pick.bssid } }?.lead }
    // Freeze the observations while inspecting: a new scan must not move the value under a finger.
    var snapshot by remember { mutableStateOf<Map<String, List<Int>>?>(null) }
    var cursor by remember { mutableStateOf<Float?>(null) }
    var inspectedBssid by remember { mutableStateOf<String?>(null) }
    val readings = snapshot ?: history
    val shown = leads.map { it.lead }
        .filter { !readings[it.bssid].isNullOrEmpty() }
        .sortedWith(compareByDescending<WifiNetwork> { it.bssid == inspectedBssid || it.bssid == selected?.bssid }
            .thenByDescending { it.bssid.equals(connectedBssid, true) }.thenByDescending { it.rssi })
        .take(MaxLines)
    val samples = shown.maxOfOrNull { readings[it.bssid]?.size ?: 0 } ?: 0
    val density = androidx.compose.ui.platform.LocalDensity.current
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    val haptics = com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback()
    val index = cursor?.let { (it * (samples - 1)).roundToInt().coerceIn(0, (samples - 1).coerceAtLeast(0)) }
    val inspected = shown.firstOrNull { it.bssid == inspectedBssid }
    val value = if (index != null && inspected != null) historyValueAt(readings[inspected.bssid].orEmpty(), index, samples) else null
    fun inspect(at: Offset, chooseLine: Boolean) {
        if (samples < 2) return
        val plot = timelinePlot(canvasSize.width, canvasSize.height, density)
        val fraction = ((at.x - plot.left) / plot.width.coerceAtLeast(1f)).coerceIn(0f, 1f)
        val atIndex = (fraction * (samples - 1)).roundToInt()
        if (snapshot == null) { snapshot = history; snapshotNetworks = networks }
        if (chooseLine || inspectedBssid == null) {
            val pick = shown.filter { historyValueAt(readings[it.bssid].orEmpty(), atIndex, samples) != null }
                .minByOrNull { network ->
                    val rssi = historyValueAt(readings[network.bssid].orEmpty(), atIndex, samples)!!
                    abs(plot.bottom - plot.height * dbmToFraction(rssi.toFloat()) - at.y)
                }
            inspectedBssid = pick?.bssid
            if (pick != null && pick.bssid != selected?.bssid) onNetworkSelected?.invoke(pick)
        }
        if (atIndex != index) haptics.light()
        cursor = fraction
    }
    val inspectLatest by rememberUpdatedState(::inspect)
    fun inspectIndex(requested: Int): Boolean {
        if (samples < 2) return false
        if (snapshot == null) { snapshot = history; snapshotNetworks = networks }
        if (inspectedBssid == null) inspectedBssid = selected?.bssid
            ?: shown.firstOrNull { it.bssid.equals(connectedBssid, true) }?.bssid ?: shown.firstOrNull()?.bssid
        cursor = requested.coerceIn(0, samples - 1) / (samples - 1f)
        return true
    }

    Column(modifier) {
        if (samples < 2) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Collecting readings… The lines appear after a couple of scans.", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
        } else Canvas(
            Modifier.weight(1f).fillMaxWidth()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                .semantics {
                    contentDescription = "Signal history for ${shown.size} networks. Tap a line, drag horizontally, or use arrow keys to inspect readings."
                    progressBarRangeInfo = ProgressBarRangeInfo((index ?: samples - 1).toFloat(), 0f..(samples - 1).toFloat(), (samples - 2).coerceAtLeast(0))
                    setProgress { inspectIndex(it.roundToInt()) }
                }
                .onKeyEvent {
                    if (it.type != KeyEventType.KeyDown) false else when (it.key) {
                        Key.DirectionLeft -> inspectIndex((index ?: samples - 1) - 1)
                        Key.DirectionRight -> inspectIndex((index ?: samples - 1) + 1)
                        else -> false
                    }
                }
                .focusable()
                .pointerInput(Unit) { detectTapGestures { inspectLatest(it, true) } }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { inspectLatest(it, true) },
                        onHorizontalDrag = { change, _ -> change.consume(); inspectLatest(change.position, false) }
                    )
                }
        ) {
            val plot = timelinePlot(size.width, size.height, this)
            drawZones(plot, zones)
            drawQualityLabels(plot, measurer, labelStyle.copy(color = scheme.onSurfaceVariant))
            listOf("older" to plot.left, "latest" to plot.right).forEach { (label, x) ->
                val layout = measurer.measure(label, labelStyle.copy(color = scheme.onSurfaceVariant))
                drawText(layout, topLeft = Offset(if (label == "latest") x - layout.size.width else x, plot.bottom + 6.dp.toPx()))
            }
            val step = plot.width / (samples - 1)
            val focus = inspectedBssid.takeIf { cursor != null } ?: selected?.bssid
            val labels = mutableListOf<Rect>()
            shown.reversed().forEach { network ->
                val values = readings[network.bssid].orEmpty()
                val offset = samples - values.size
                val emphasized = network.bssid == focus || network.bssid.equals(connectedBssid, true)
                val color = palette.color(network.bssid).copy(alpha = if (focus != null && network.bssid != focus) 0.25f else 1f)
                fun point(i: Int) = Offset(plot.left + (i + offset) * step, plot.bottom - plot.height * dbmToFraction(values[i].toFloat()))
                val path = Path().apply {
                    moveTo(point(0).x, point(0).y)
                    for (i in 1 until values.size) {
                        val previous = point(i - 1); val current = point(i); val midX = (previous.x + current.x) / 2
                        cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
                    }
                }
                drawPath(path, color, style = Stroke((if (emphasized) 3f else 2f).dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
                drawCircle(color, (if (emphasized) 5f else 3.5f).dp.toPx(), point(values.lastIndex))
                if (index != null) historyValueAt(values, index, samples)?.let { rssi ->
                    drawCircle(color, 5.dp.toPx(), Offset(plot.left + index * step, plot.bottom - plot.height * dbmToFraction(rssi.toFloat())))
                }
            }
            if (index != null) {
                val x = plot.left + index * step
                drawLine(scheme.onSurface.copy(alpha = 0.65f), Offset(x, plot.top), Offset(x, plot.bottom), strokeWidth = 1.dp.toPx())
            }
            shown.forEach { network ->
                if (focus != null && network.bssid != focus) return@forEach
                val values = readings[network.bssid].orEmpty()
                val name = network.ssid.ifEmpty { "Hidden network" } + if (network.bssid.equals(connectedBssid, true)) " (you)" else ""
                val layout = measurer.measure(name, nameStyle.copy(color = palette.color(network.bssid)), maxLines = 1,
                    overflow = TextOverflow.Ellipsis, constraints = Constraints(maxWidth = (plot.width - 16.dp.toPx()).toInt().coerceAtLeast(1)))
                val end = Offset(plot.right - layout.size.width / 2f - 8.dp.toPx(), plot.bottom - plot.height * dbmToFraction(values.last().toFloat()))
                val bounds = Rect(plot.left + 4.dp.toPx(), 4.dp.toPx(), plot.right - 4.dp.toPx(), plot.bottom)
                val above = chartLabelRect(end, Size(layout.size.width.toFloat(), layout.size.height.toFloat()), bounds, 6.dp.toPx())
                val below = above.translate(0f, layout.size.height + 12.dp.toPx())
                val rect = listOf(above, below).firstOrNull { it.bottom <= plot.bottom && labels.none { other -> other.overlaps(it) } } ?: return@forEach
                labels += rect
                drawRoundRect(scheme.surfaceContainerLow.copy(alpha = 0.92f), rect.topLeft - Offset(4.dp.toPx(), 1.dp.toPx()),
                    Size(rect.width + 8.dp.toPx(), rect.height + 2.dp.toPx()), CornerRadius(6.dp.toPx()))
                drawText(layout, topLeft = rect.topLeft)
            }
        }
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(inspected?.ssid?.ifBlank { "Hidden network" } ?: "Explore signal history", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    when {
                        index == null -> "Tap a line, then slide to compare readings"
                        value == null -> "No reading at this point"
                        else -> "${SignalQuality.of(value).label} · ${display.reading(value)} · Paused · " + if (index == samples - 1) "latest reading" else "${samples - 1 - index} reading${if (samples - 1 - index == 1) "" else "s"} back"
                    },
                    style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant
                )
            }
            if (cursor != null) TextButton(onClick = { cursor = null; snapshot = null; snapshotNetworks = null; inspectedBssid = null; onNetworkSelected?.invoke(null) }) { Text("Live") }
        }
    }
}

private fun timelinePlot(width: Float, height: Float, density: Density): Rect = with(density) {
    Rect(QualityGutter.toPx(), 12.dp.toPx(), (width - 12.dp.toPx()).coerceAtLeast(QualityGutter.toPx() + 1f), (height - 26.dp.toPx()).coerceAtLeast(13.dp.toPx()))
}
