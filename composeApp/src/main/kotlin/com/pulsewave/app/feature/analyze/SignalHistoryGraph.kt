package com.pulsewave.app.feature.analyze

import androidx.compose.foundation.focusable
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.setProgress
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitHorizontalTouchSlopOrCancellation
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.drag
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
import androidx.compose.ui.input.pointer.PointerType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.stateDescription
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

/** Tap or scrub to inspect frozen readings; live charts leave vertical gestures to page scrolling. */
@Composable
fun SignalHistoryGraph(
    history: Map<String, List<Int>>,
    networks: List<WifiNetwork>,
    selectedNetwork: WifiNetwork? = null,
    connectedBssid: String? = null,
    modifier: Modifier = Modifier,
    allowVerticalScrubbing: Boolean = false,
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
    fun pause() {
        if (snapshot == null) { snapshot = history; snapshotNetworks = shown }
    }
    fun resume() {
        cursor = null
        snapshot = null
        snapshotNetworks = null
        inspectedBssid = null
        onNetworkSelected?.invoke(null)
    }
    /** [sticky] keeps the inspected line until the finger is clearly closer to another, so crowded lines don't flicker. */
    fun inspect(at: Offset, sticky: Boolean) {
        if (samples < 2) return
        val plot = timelinePlot(canvasSize.width, canvasSize.height, density)
        val fraction = ((at.x - plot.left) / plot.width.coerceAtLeast(1f)).coerceIn(0f, 1f)
        val atIndex = (fraction * (samples - 1)).roundToInt()
        pause()
        fun distance(network: WifiNetwork) = historyValueAt(readings[network.bssid].orEmpty(), atIndex, samples)
            ?.let { abs(plot.bottom - plot.height * dbmToFraction(it.toFloat()) - at.y) }
        val pick = shown.mapNotNull { network -> distance(network)?.let { network to it } }.minByOrNull { it.second }
        val current = shown.firstOrNull { it.bssid == inspectedBssid }?.let(::distance)
        val switchMargin = with(density) { 14.dp.toPx() }
        if (pick != null && pick.first.bssid != inspectedBssid && (!sticky || current == null || pick.second + switchMargin < current)) {
            if (inspectedBssid != null) haptics.light()
            inspectedBssid = pick.first.bssid
            if (pick.first.bssid != selected?.bssid) onNetworkSelected?.invoke(pick.first)
        }
        if (atIndex != index) haptics.light()
        cursor = fraction
    }
    val inspectLatest by rememberUpdatedState(::inspect)
    fun inspectIndex(requested: Int): Boolean {
        if (samples < 2) return false
        pause()
        if (inspectedBssid == null) inspectedBssid = selected?.bssid
            ?: shown.firstOrNull { it.bssid.equals(connectedBssid, true) }?.bssid ?: shown.firstOrNull()?.bssid
        cursor = requested.coerceIn(0, samples - 1) / (samples - 1f)
        return true
    }

    fun inspectNetwork(direction: Int): Boolean {
        if (samples < 2) return false
        val atIndex = index ?: samples - 1
        val current = inspectedBssid ?: selected?.bssid ?: connectedBssid
        val next = historyNeighborBssid(readings, shown.map { it.bssid }, atIndex, samples, current, direction) ?: return false
        pause()
        inspectedBssid = next
        cursor = atIndex / (samples - 1f)
        if (next != selected?.bssid) shown.firstOrNull { it.bssid == next }?.let { onNetworkSelected?.invoke(it) }
        return true
    }
    val inspectingLatest by rememberUpdatedState(cursor != null)

    Column(modifier) {
        if (samples < 2) {
            Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Collecting readings… The lines appear after a couple of scans.", style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
            }
        } else Canvas(
            Modifier.weight(1f).fillMaxWidth()
                .onSizeChanged { canvasSize = Size(it.width.toFloat(), it.height.toFloat()) }
                .semantics {
                    contentDescription = "Signal history for ${shown.size} networks. Tap a line to inspect. Arrow keys move through time and networks."
                    stateDescription = if (index == null) "Live" else "${inspected?.ssid?.ifBlank { "Hidden network" } ?: "Network"}, ${value?.let { display.reading(it) } ?: "no reading"}, paused, ${samples - 1 - index} readings ago"
                    customActions = listOf(
                        CustomAccessibilityAction("Stronger network") { inspectNetwork(-1) },
                        CustomAccessibilityAction("Weaker network") { inspectNetwork(1) },
                        CustomAccessibilityAction("Resume live readings") { resume(); true }
                    )
                    progressBarRangeInfo = ProgressBarRangeInfo((index ?: samples - 1).toFloat(), 0f..(samples - 1).toFloat(), (samples - 2).coerceAtLeast(0))
                    setProgress { inspectIndex(it.roundToInt()) }
                }
                .onKeyEvent {
                    if (it.type != KeyEventType.KeyDown) false else when (it.key) {
                        Key.DirectionLeft -> inspectIndex((index ?: samples - 1) - 1)
                        Key.DirectionRight -> inspectIndex((index ?: samples - 1) + 1)
                        Key.DirectionUp -> inspectNetwork(-1)
                        Key.DirectionDown -> inspectNetwork(1)
                        Key.Escape -> if (cursor != null) { resume(); true } else false
                        else -> false
                    }
                }
                .focusable()
                .pointerInput(Unit) { detectTapGestures { inspectLatest(it, false) } }
                .pointerInput(allowVerticalScrubbing) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        // A vertical swipe scrolls the live page. After tapping to inspect, or in
                        // the full-screen explorer, the plot owns both axes for precise scrubbing.
                        val wasInspecting = inspectingLatest
                        val start = if (allowVerticalScrubbing || wasInspecting || down.type != PointerType.Touch) {
                            awaitTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                        } else {
                            awaitHorizontalTouchSlopOrCancellation(down.id) { change, _ -> change.consume() }
                        }
                        if (start != null) {
                            // A fresh scrub grabs the nearest line; while sliding, the finger takes
                            // over whichever line it clearly moves onto.
                            inspectLatest(down.position, wasInspecting)
                            inspectLatest(start.position, true)
                            drag(start.id) { change -> inspectLatest(change.position, true); change.consume() }
                        }
                    }
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
        Row(Modifier.fillMaxWidth().padding(top = 8.dp).heightIn(min = 64.dp).padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(inspected?.ssid?.ifBlank { "Hidden network" } ?: "Explore signal history", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    when {
                        index == null -> "Tap a line, then drag to compare"
                        value == null -> "No reading at this point"
                        else -> "${display.reading(value)} · ${SignalQuality.of(value).label} · " + if (index == samples - 1) "latest reading" else "${samples - 1 - index} reading${if (samples - 1 - index == 1) "" else "s"} ago"
                    },
                    style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis
                )
            }
            Box(Modifier.width(104.dp).heightIn(min = 48.dp), contentAlignment = Alignment.CenterEnd) {
                if (cursor != null) TextButton(onClick = ::resume) { Text("Resume live") }
            }
        }
    }
}

private fun timelinePlot(width: Float, height: Float, density: Density): Rect = with(density) {
    Rect(QualityGutter.toPx(), 12.dp.toPx(), (width - 12.dp.toPx()).coerceAtLeast(QualityGutter.toPx() + 1f), (height - 26.dp.toPx()).coerceAtLeast(13.dp.toPx()))
}
