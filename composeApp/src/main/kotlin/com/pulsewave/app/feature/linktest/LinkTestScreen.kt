@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.linktest

import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.horizontalScroll
import com.pulsewave.app.core.util.fixed

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.component.SignalMorphBlob
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.router.LinkVerdict
import com.pulsewave.app.core.wifi.SignalQuality
import com.pulsewave.app.core.wifi.bandLabel
import org.koin.compose.viewmodel.koinViewModel

private fun LinkVerdict.quality() = when (this) {
    LinkVerdict.Great -> SignalQuality.Excellent
    LinkVerdict.Good -> SignalQuality.Good
    LinkVerdict.Slow -> SignalQuality.Weak
    LinkVerdict.Unreliable -> SignalQuality.DeadZone
}

private fun formatMbps(mbps: Double): String = if (mbps >= 100) "${mbps.fixed(0)}" else "${mbps.fixed(1)}"
private fun formatMs(ms: Float): String = if (!ms.isFinite()) "—" else if (ms >= 10) "${ms.fixed(0)}" else "${ms.fixed(1)}"

@Composable
fun LinkTestScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenInternetTest: () -> Unit = {},
    viewModel: LinkTestViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()
    val savedRooms by viewModel.savedRooms.collectAsStateWithLifecycle()
    val taggedRoom by viewModel.taggedRoom.collectAsStateWithLifecycle()
    val haptics = rememberPulseWaveHapticFeedback()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(state is LinkTestState.Done) {
        (state as? LinkTestState.Done)?.let {
            if (it.result.verdict.quality().isProblem) haptics.error() else haptics.success()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Wi-Fi speed") },
                subtitle = { Text("Between this phone and your router") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(Modifier.fillMaxWidth().heightIn(min = 300.dp), contentAlignment = Alignment.Center) {
                val stageFade = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
                val stageScale = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
                AnimatedContent(
                    // Each phase renders its own snapshot, so the leaving stage never flashes the
                    // next state's content; ping → upload stays one continuous ring.
                    targetState = state,
                    contentKey = ::phaseOf,
                    transitionSpec = { (fadeIn(stageFade) + scaleIn(stageScale, initialScale = 0.9f)) togetherWith fadeOut(stageFade) },
                    label = "linkStage"
                ) { stage ->
                    when (val s = stage) {
                        LinkTestState.Idle, is LinkTestState.Failed -> StartButton(
                            enabled = status.isConnected,
                            onClick = { haptics.heavy(); viewModel.start() }
                        )
                        is LinkTestState.Pinging -> ProgressStage(
                            progress = s.progress * 0.5f,
                            value = s.latestMs,
                            unit = "ms",
                            label = "Checking response time",
                            format = ::formatMs
                        )
                        is LinkTestState.Uploading -> ProgressStage(
                            progress = 0.5f + s.progress * 0.5f,
                            value = s.mbps.toFloat().takeIf { it > 0f },
                            unit = "Mbps",
                            label = "Sending data to your router",
                            format = { formatMbps(it.toDouble()) }
                        )
                        is LinkTestState.Done -> ResultStage(s.result)
                    }
                }
            }

            // The text under the stage eases in with it, instead of popping.
            val below = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
            val belowSlide = MaterialTheme.motionScheme.defaultSpatialSpec<androidx.compose.ui.unit.IntOffset>()
            AnimatedContent(
                targetState = state,
                contentKey = ::phaseOf,
                transitionSpec = {
                    (fadeIn(below) + androidx.compose.animation.slideInVertically(belowSlide) { it / 12 }) togetherWith fadeOut(below) using
                        androidx.compose.animation.SizeTransform(clip = false)
                },
                label = "linkDetails"
            ) { stage ->
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (val s = stage) {
                is LinkTestState.Done -> {
                    Text(s.result.verdict.headline, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    Text(
                        s.result.verdict.explanation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    // Compares with earlier tests once this one is saved; silent on a first run.
                    if (history.size >= 2) {
                        Text(
                            com.pulsewave.app.feature.history.trendInsight(history),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                    }
                    RoomTagger(savedRooms, taggedRoom, onTag = { haptics.click(); viewModel.tagRoom(it) })
                    ResultTiles(s.result)
                    Button(
                        onClick = { haptics.click(); viewModel.start() },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight)
                    ) { Text("Test again", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)) }
                }
                is LinkTestState.Pinging, is LinkTestState.Uploading -> {
                    OutlinedButton(onClick = { haptics.light(); viewModel.cancel() }, shapes = ButtonDefaults.shapes()) { Text("Cancel") }
                }
                is LinkTestState.Failed -> {
                    Text(s.message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                }
                LinkTestState.Idle -> {
                    IdleInfo(status)
                    if (history.isNotEmpty()) LastTestCard(history)
                }
            }
            }
            }

            Text(
                "Measured inside your home network. Nothing is sent to the internet.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            TextButton(onClick = onOpenInternetTest) {
                Icon(Icons.Rounded.Public, null, Modifier.size(18.dp))
                Text("  Test internet speed instead")
            }
        }
    }
}

@Composable
private fun StartButton(enabled: Boolean, onClick: () -> Unit) {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.92f else 1f, MaterialTheme.motionScheme.fastSpatialSpec(), label = "startPress")
    val scheme = MaterialTheme.colorScheme
    Surface(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interaction,
        shape = androidx.compose.foundation.shape.CircleShape,
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.size(248.dp).graphicsLayer { scaleX = scale; scaleY = scale }
    ) {
        SignalMorphBlob(
            quality = if (pressed) SignalQuality.Fair else SignalQuality.Excellent,
            color = if (enabled) scheme.primary else scheme.surfaceContainerHighest,
            size = 248.dp
        ) {
            Text(
                "GO",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = if (enabled) scheme.onPrimary else scheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ProgressStage(progress: Float, value: Float?, unit: String, label: String, format: (Float) -> String) {
    // Readings glide to each new value instead of jumping digit by digit.
    val shown by animateFloatAsState(value ?: 0f, MaterialTheme.motionScheme.slowEffectsSpec(), label = "linkValue")
    val stroke = with(LocalDensity.current) { Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round) }
    val animated by animateFloatAsState(progress, MaterialTheme.motionScheme.defaultEffectsSpec(), label = "linkProgress")
    Box(contentAlignment = Alignment.Center, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
        CircularWavyProgressIndicator(
            progress = { animated },
            modifier = Modifier.size(272.dp),
            stroke = stroke,
            trackStroke = stroke
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (value == null) "…" else format(shown),
                    style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Black
                )
                Text(" $unit", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))
            }
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ResultStage(result: LinkTestResult) {
    val colors = signalColors(result.verdict.quality())
    // The final number counts up once, so the result lands rather than appears.
    val target = (result.uploadMbps ?: result.latency.medianMs.toDouble()).toFloat()
    val count = androidx.compose.runtime.remember(result) { androidx.compose.animation.core.Animatable(0f) }
    val countSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(result) { count.animateTo(target, countSpec) }
    SignalMorphBlob(quality = result.verdict.quality(), color = colors.container, size = 248.dp, spinning = false) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (result.uploadMbps != null) {
                Text(
                    formatMbps(count.value.toDouble()),
                    style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Black,
                    color = colors.onContainer
                )
                Text("Mbps to router", style = MaterialTheme.typography.titleMedium, color = colors.onContainer)
            } else {
                Text(
                    formatMs(count.value),
                    style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Black,
                    color = colors.onContainer
                )
                Text("ms response", style = MaterialTheme.typography.titleMedium, color = colors.onContainer)
            }
        }
    }
}

@Composable
private fun ResultTiles(result: LinkTestResult) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile("Response", "${formatMs(result.latency.medianMs)} ms", "Median of ${result.latency.samples} replies", Modifier.weight(1f))
            StatTile(
                "Steadiness",
                "±${formatMs(result.latency.jitterMs)} ms",
                if (result.latency.lossPercent > 0) "${result.latency.lossPercent.fixed(0)}% replies lost" else "No replies lost",
                Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                "Link rate",
                if (result.rxLinkMbps > 0 && result.txLinkMbps > 0) "${result.rxLinkMbps} / ${result.txLinkMbps}" else "—",
                "Mbps down / up",
                Modifier.weight(1f)
            )
            StatTile("Signal", "${result.rssi} dBm", bandLabel(result.frequency), Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, detail: String, modifier: Modifier = Modifier) {
    Card(
        shape = PulseWaveShapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum"), fontWeight = FontWeight.Bold)
            // One line each, so tiles in a row always match in height.
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun IdleInfo(status: NetworkStatus) {
    Text(
        if (status.isConnected) "Tap GO and hold still for about 10 seconds." else "Connect to your Wi-Fi to start.",
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center
    )
    if (status.isConnected) {
        Text(
            buildString {
                append(status.ssid)
                if (status.gateway.isNotBlank()) append(" · router ${status.gateway}")
                append(" · ${bandLabel(status.frequency)}")
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/** What a returning person wants first: how it went last time, and whether that's normal. */
@Composable
private fun LastTestCard(points: List<com.pulsewave.app.feature.history.RouterTestPoint>) {
    val last = points.last()
    Surface(
        shape = PulseWaveShapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Last time", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        last.uploadMbps?.let { "${formatMbps(it.toDouble())} Mbps" } ?: "No reply",
                        style = MaterialTheme.typography.headlineSmall.copy(fontFeatureSettings = "tnum")
                    )
                    Text(
                        listOfNotNull(
                            com.pulsewave.app.feature.history.momentLabel(last.timestamp),
                            last.responseMs?.let { "${formatMs(it)} ms" },
                            last.rssi?.let { "$it\u00A0dBm" }
                        ).joinToString(" · "),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                com.pulsewave.app.feature.history.VerdictBadge(last.verdict)
            }
            if (points.size >= 2) {
                com.pulsewave.app.feature.history.RouterTrendBars(
                    points.takeLast(com.pulsewave.app.feature.history.MaxTrendBars),
                    Modifier.fillMaxWidth().height(64.dp)
                )
            }
        }
    }
}

/** "Where are you?" so results can be compared room by room. Saved rooms come first. */
@Composable
private fun RoomTagger(saved: List<String>, tagged: String?, onTag: (String) -> Unit) {
    val options = (saved + com.pulsewave.app.core.rooms.RoomSuggestions).distinctBy { it.lowercase() }.take(10)
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (tagged == null) "Where are you?" else "Saved to $tagged. Test other rooms to compare.",
            style = MaterialTheme.typography.titleSmall,
            color = if (tagged == null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { room ->
                val selected = tagged.equals(room, ignoreCase = true)
                FilterChip(
                    selected = selected,
                    onClick = { onTag(room) },
                    label = { Text(room) },
                    leadingIcon = if (selected) {
                        { Icon(Icons.Rounded.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                    } else null
                )
            }
        }
    }
}

/** Idle (or failed), running, or done: the only changes worth animating. */
private fun phaseOf(state: LinkTestState): Int = when (state) {
    LinkTestState.Idle, is LinkTestState.Failed -> 0
    is LinkTestState.Pinging, is LinkTestState.Uploading -> 1
    is LinkTestState.Done -> 2
}
