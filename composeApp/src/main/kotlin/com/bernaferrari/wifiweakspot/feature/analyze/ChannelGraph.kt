package com.bernaferrari.wifiweakspot.feature.analyze

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings
import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.unit.Constraints
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import com.bernaferrari.wifiweakspot.core.designsystem.graphSeriesColors
import com.bernaferrari.wifiweakspot.core.designsystem.signalColors
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import com.bernaferrari.wifiweakspot.core.wifi.frequencyToChannel
import com.bernaferrari.wifiweakspot.core.wifi.groupByRadio
import androidx.compose.ui.graphics.Brush
import com.bernaferrari.wifiweakspot.core.wifi.occupiedFrequencySegments
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
    onNetworkSelected: ((WifiNetwork?) -> Unit)? = null,
    /**
     * Explore mode (the full-screen chart): drag or tap anywhere and a cursor snaps to the nearest
     * channel; [onExplore] gets that channel and every network using it, strongest first.
     */
    onExplore: ((channel: Int, networks: List<WifiNetwork>) -> Unit)? = null,
    initialChannel: Int? = null
) {
    val textMeasurer = rememberTextMeasurer()
    val explore = onExplore != null
    val channels = remember(selectedBand) { exploreChannels(selectedBand) }
    var cursorChannel by androidx.compose.runtime.saveable.rememberSaveable(selectedBand) {
        androidx.compose.runtime.mutableStateOf(initialChannel?.takeIf { c -> channels.any { it.first == c } } ?: channels[channels.size / 2].first)
    }
    val latestNetworks by androidx.compose.runtime.rememberUpdatedState(networks)
    if (explore) LaunchedEffect(cursorChannel, networks) {
        val center = channels.first { it.first == cursorChannel }.second
        // Everything a 20 MHz router on this channel would share airtime with, not just networks
        // centred on it: channel 4 overlaps both 2 and 6.
        onExplore?.invoke(cursorChannel, latestNetworks.filter { n ->
            occupiedFrequencySegments(n).any { center - 10 < it.upperMhz && center + 10 > it.lowerMhz }
        }.sortedByDescending { it.rssi })
    }
    val palette = rememberNetworkPalette(networks, connectedBssid)
    // One hill per radio: a router's guest and hidden names share its airtime, so they share its hill.
    val groups = remember(networks, connectedBssid) { groupByRadio(networks, connectedBssid) }
    val radios = remember(groups) { groups.map { it.lead } }
    val showCrowd = selectedBand == FrequencyBand.BAND_2_4 && radios.size >= 2
    val latestRadios by androidx.compose.runtime.rememberUpdatedState(radios)
    val latestShowCrowd by androidx.compose.runtime.rememberUpdatedState(showCrowd)
    val selectLatest by androidx.compose.runtime.rememberUpdatedState(onNetworkSelected)
    // The crowd strip flips to exact counts (and back) when tapped.
    var crowdCounts by androidx.compose.runtime.saveable.rememberSaveable { androidx.compose.runtime.mutableStateOf(false) }
    val extraNames = remember(groups) { groups.associate { it.lead.bssid to it.extraNames } }
    val focused = selectedNetwork?.let { pick -> groups.firstOrNull { group -> group.members.any { it.bssid == pick.bssid } }?.lead }
    val latestFocus by androidx.compose.runtime.rememberUpdatedState(focused)
    val haptics = com.bernaferrari.wifiweakspot.core.designsystem.rememberWifiWeakspotHapticFeedback()
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

    val sharedCatalog = strings

    val s = strings.analyze
    val density = androidx.compose.ui.platform.LocalDensity.current
    // Translated axis words and larger accessibility text need their actual measured width.
    val qualityGutter = with(density) {
        maxOf(QualityGutter, listOf(s.axisStrong, s.axisOkay, s.axisWeak, s.busyCaption)
            .maxOf { textMeasurer.measure(it, labelStyle, maxLines = 1).size.width }.toDp() + 8.dp)
    }
    val busiest = remember(networks) {
        networks.groupBy { frequencyToChannel(it.frequency) }.maxByOrNull { it.value.size }
    }
    Canvas(
        modifier
            .semantics {
                contentDescription = s.channelGraphDescription(networks.size, busiest?.key, busiest?.value?.size ?: 0)
            }
            .then(if (explore) Modifier.pointerInput(selectedBand, qualityGutter) {
                fun moveTo(x: Float) {
                    val plot = plotRect(size.width.toFloat(), size.height.toFloat(), this, selectedBand, latestShowCrowd, qualityGutter)
                    val nearest = channels.minBy { kotlin.math.abs(plot.left + mapFreqToNormX(it.second.toFloat(), selectedBand) * plot.width - x) }.first
                    if (nearest != cursorChannel) { cursorChannel = nearest; haptics.light() }
                }
                awaitEachGesture {
                    val down = awaitFirstDown()
                    moveTo(down.position.x)
                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { if (it.pressed) { moveTo(it.position.x); it.consume() } }
                    } while (event.changes.any { it.pressed })
                }
            } else Modifier)
            .pointerInput(selectedBand, explore, qualityGutter) {
                if (explore) return@pointerInput
                // Tapping the same spot again steps through overlapping hills, strongest first.
                detectTapGestures { tap ->
                    val plot = plotRect(size.width.toFloat(), size.height.toFloat(), this, selectedBand, latestShowCrowd, qualityGutter)
                    if (latestShowCrowd && tap.y > plot.bottom + 24.dp.toPx()) {
                        haptics.light()
                        crowdCounts = !crowdCounts
                        return@detectTapGestures
                    }
                    val here = networksAt(tap, latestRadios, selectedBand, plot)
                    val current = here.indexOfFirst { it.bssid == latestFocus?.bssid }
                    selectLatest?.invoke(if (here.isEmpty()) null else here[(current + 1) % here.size])
                }
            }
            .pointerInput(selectedBand, explore, qualityGutter) {
                if (explore) return@pointerInput
                // Horizontal scrubbing is immediate; vertical gestures remain available to the list.
                var last: String? = null
                fun scrub(at: Offset) {
                    val plot = plotRect(size.width.toFloat(), size.height.toFloat(), this, selectedBand, latestShowCrowd, qualityGutter)
                    val pick = networksAt(Offset(at.x, plot.bottom), latestRadios, selectedBand, plot, anyHeight = true).firstOrNull() ?: return
                    if (pick.bssid != last) {
                        last = pick.bssid
                        haptics.light()
                        if (pick.bssid != latestFocus?.bssid) selectLatest?.invoke(pick)
                    }
                }
                detectHorizontalDragGestures(
                    onDragStart = { last = null; scrub(it) },
                    onHorizontalDrag = { change, _ -> change.consume(); scrub(change.position) }
                )
            }
    ) {
        val plot = plotRect(size.width, size.height, this, selectedBand, latestShowCrowd, qualityGutter)
        drawZones(plot, zones)
        drawQualityLabels(s, plot, textMeasurer, labelStyle.copy(color = scheme.onSurfaceVariant.copy(alpha = 0.7f)))
        val yourNetwork = connectedBssid?.let { bssid -> radios.firstOrNull { it.bssid.equals(bssid, ignoreCase = true) } }
        drawChannelAxis(
            plot, selectedBand, textMeasurer, labelStyle, scheme.onSurfaceVariant, scheme.onSurface, scheme.outlineVariant,
            used = radios.map { frequencyToChannel(it.frequency) }.toSet(),
            yours = yourNetwork?.let { frequencyToChannel(it.frequency) },
            yourColor = yourNetwork?.let { palette.color(it.bssid) } ?: scheme.onSurface
        )
        if (showCrowd) {
            drawCrowdRow(s.busyCaption, plot, radios, textMeasurer, labelStyle, crowdColors, crowdCounts)
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

        if (explore) {
            // The explore cursor: a line through the chosen channel, with its number on top.
            val center = channels.first { it.first == cursorChannel }.second
            val x = plot.left + mapFreqToNormX(center.toFloat(), selectedBand) * plot.width
            drawLine(scheme.onSurface.copy(alpha = 0.7f), Offset(x, plot.top), Offset(x, plot.bottom), strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
            val tag = textMeasurer.measure(s.channelShort(cursorChannel), peakStyle.copy(color = scheme.inverseOnSurface))
            val w = tag.size.width + 16.dp.toPx()
            val h = tag.size.height + 6.dp.toPx()
            // Centred on its line; it may spill past the plot into the margins, just not off screen.
            val left = (x - w / 2).coerceIn(0f, size.width - w)
            drawRoundRect(scheme.inverseSurface, Offset(left, plot.top - h / 2), Size(w, h), CornerRadius(h / 2))
            drawText(tag, topLeft = Offset(left + 8.dp.toPx(), plot.top - h / 2 + 3.dp.toPx()))
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
            val name = s.chartNetworkLabel(network.ssid.ifEmpty { sharedCatalog.common.hiddenNetworkShort }.let { if (it.length > 16) it.take(15) + "…" else it }, isYou, extra)
            val layout = textMeasurer.measure(name, peakStyle.copy(color = palette.color(network.bssid)), maxLines = 1, overflow = TextOverflow.Ellipsis,
                constraints = Constraints(maxWidth = (plot.width - 16.dp.toPx()).toInt().coerceAtLeast(1)))
            val segment = occupiedFrequencySegments(network).first()
            val x = plot.left + mapFreqToNormX(segment.centerMhz.toFloat(), selectedBand) * plot.width
            val peakY = plot.bottom - plot.height * dbmToFraction(network.rssi.toFloat()) * grow.value
            var rect = chartLabelRect(
                Offset(x, peakY),
                Size(layout.size.width.toFloat(), layout.size.height.toFloat()),
                Rect(plot.left + 4.dp.toPx(), 4.dp.toPx(), plot.right - 4.dp.toPx(), plot.bottom),
                4.dp.toPx()
            )
            // Nudge up once if the spot is taken; otherwise the list below still names it.
            if (placed.any { it.overlaps(rect) }) {
                rect = listOf(rect.translate(0f, -rect.height - 2.dp.toPx()), rect.translate(0f, rect.height + 6.dp.toPx()))
                    .firstOrNull { candidate -> candidate.top >= 4.dp.toPx() && candidate.bottom <= plot.bottom && placed.none { it.overlaps(candidate) } }
                    ?: return@forEach
            }
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

private fun plotRect(width: Float, height: Float, density: Density, band: FrequencyBand, crowd: Boolean = band == FrequencyBand.BAND_2_4, qualityGutter: androidx.compose.ui.unit.Dp = QualityGutter): Rect = with(density) {
    // Room below the channel numbers for the 2.4 GHz crowd strip, only when it's drawn.
    val below = if (crowd) 52.dp else 28.dp
    // The gutter holds the signal words, so they never collide with hills or names.
    Rect(left = qualityGutter.toPx(), top = 12.dp.toPx(), right = width - 12.dp.toPx(), bottom = height - below.toPx())
}

internal data class CrowdColors(
    val calm: com.bernaferrari.wifiweakspot.core.designsystem.SignalColors,
    val busy: com.bernaferrari.wifiweakspot.core.designsystem.SignalColors,
    val packed: com.bernaferrari.wifiweakspot.core.designsystem.SignalColors,
    val quietText: Color
)

/** Networks weaker than this rarely slow yours down. */
internal const val CrowdFloorDbm = -80

/**
 * How crowded each part of 2.4 GHz is, as one soft strip under the channels: green where few
 * routers overlap, red where many do. Counting stays in the help; the strip just shows where.
 */
private fun DrawScope.drawCrowdRow(busyCaption: String, plot: Rect, networks: List<WifiNetwork>, measurer: TextMeasurer, style: TextStyle, colors: CrowdColors, counts: Boolean) {
    val y = plot.bottom + 34.dp.toPx()
    val caption = measurer.measure(busyCaption, style.copy(color = colors.quietText))
    val perChannel = (1..13).map { channel ->
        val center = 2407 + channel * 5
        val count = networks.count { network -> network.rssi >= CrowdFloorDbm && occupiedFrequencySegments(network).any { it.overlaps(center) } }
        val tone = when { count <= 2 -> colors.calm; count <= 5 -> colors.busy; else -> colors.packed }
        Triple(mapFreqToNormX(center.toFloat(), FrequencyBand.BAND_2_4).coerceIn(0f, 1f), count, tone)
    }
    if (!counts) {
        val height = 8.dp.toPx()
        drawText(caption, topLeft = Offset(plot.left - caption.size.width - 8.dp.toPx(), y + (height - caption.size.height) / 2f))
        drawRoundRect(
            androidx.compose.ui.graphics.Brush.horizontalGradient(
                colorStops = perChannel.map { (x, count, tone) -> x to tone.accent.copy(alpha = if (count == 0) 0.25f else 0.85f) }.toTypedArray(),
                startX = plot.left, endX = plot.right
            ),
            topLeft = Offset(plot.left, y),
            size = Size(plot.width, height),
            cornerRadius = CornerRadius(height / 2)
        )
        return
    }
    // Exact counts: small tonal dots, quiet where nobody is.
    val dot = 18.dp.toPx()
    drawText(caption, topLeft = Offset(plot.left - caption.size.width - 8.dp.toPx(), y + (dot - caption.size.height) / 2f - 4.dp.toPx()))
    perChannel.forEach { (fx, count, tone) ->
        val cx = plot.left + fx * plot.width
        val top = y - 4.dp.toPx()
        if (count == 0) {
            drawCircle(colors.quietText.copy(alpha = 0.25f), radius = 2.dp.toPx(), center = Offset(cx, top + dot / 2))
            return@forEach
        }
        drawRoundRect(tone.container, topLeft = Offset(cx - dot / 2, top), size = Size(dot, dot), cornerRadius = CornerRadius(dot / 2))
        val layout = measurer.measure("$count", style.copy(color = tone.onContainer, fontWeight = FontWeight.Bold))
        drawText(layout, topLeft = Offset(cx - layout.size.width / 2f, top + (dot - layout.size.height) / 2f))
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
internal val QualityGutter = 48.dp

/** Words instead of dBm numbers: what the level means matters more than the exact reading. */
internal fun DrawScope.drawQualityLabels(s: AnalyzeStrings, plot: Rect, measurer: TextMeasurer, style: TextStyle) {
    listOf(s.axisStrong to -40, s.axisOkay to -71, s.axisWeak to -88).forEach { (label, dbm) ->
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
    grid: Color,
    used: Set<Int> = emptySet(),
    yours: Int? = null,
    yourColor: Color = strong
) {
    drawLine(grid, Offset(plot.left, plot.bottom), Offset(plot.right, plot.bottom), strokeWidth = 1.dp.toPx())
    channelMarkers(band).forEach { (channel, freq) ->
        val x = plot.left + mapFreqToNormX(freq.toFloat(), band) * plot.width
        // Bold means "a network sits here", and your own channel takes your network's colour,
        // so the axis reads the same as the hills above it.
        val color = when (channel) {
            yours -> yourColor
            in used -> strong
            else -> quiet
        }
        val layout = measurer.measure(
            "$channel",
            style.copy(color = color, fontWeight = if (channel == yours || channel in used) FontWeight.Bold else FontWeight.Normal)
        )
        drawText(layout, topLeft = Offset(x - layout.size.width / 2f, plot.bottom + 6.dp.toPx()))
    }
}

/** Every channel the explore cursor can stop on, with its centre frequency. */
private fun exploreChannels(band: FrequencyBand): List<Pair<Int, Int>> = when (band) {
    FrequencyBand.BAND_2_4 -> (1..13).map { it to 2407 + it * 5 }
    FrequencyBand.BAND_5 -> (listOf(36, 40, 44, 48, 52, 56, 60, 64) + (100..144 step 4) + (149..165 step 4)).map { it to 5000 + it * 5 }
    FrequencyBand.BAND_6 -> (1..233 step 4).map { it to 5950 + it * 5 }
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
    // The edges sit one tick (5 MHz) past channels 1 and 13, so the spacing reads evenly; the outer
    // skirts of channel 1 and 13 hills are pressed against the edge rather than leaving empty room.
    FrequencyBand.BAND_2_4 -> {
        val visual = if (freq > 2472f) freq - 7f else freq
        (visual - 2407f) / (2477f - 2407f)
    }
    FrequencyBand.BAND_5 -> when {
        freq <= 5350f -> (freq - 5170f) / (5350f - 5170f) * 0.38f
        freq >= 5470f -> 0.42f + (freq - 5470f) / (5835f - 5470f) * 0.58f
        else -> 0.38f + (freq - 5350f) / (5470f - 5350f) * 0.04f
    }
    FrequencyBand.BAND_6 -> (freq - 5945f) / (7125f - 5945f)
}
