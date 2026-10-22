@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.linktest

import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material3.FilledTonalButton
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.horizontalScroll
import com.pulsewave.app.core.designsystem.bleed
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

private fun formatMs(ms: Float): String = if (!ms.isFinite()) "—" else if (ms >= 10) "${ms.fixed(0)}" else "${ms.fixed(1)}"

@Composable
fun LinkTestScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenInternetTest: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    viewModel: LinkTestViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
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
                title = { Text("Router check") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") } },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // Keep the stage at the screen's vertical midpoint before scrolling. Deriving its
            // space from the viewport also accommodates system insets and the expanded app bar.
            val expandedTopPadding = padding.calculateTopPadding() - with(LocalDensity.current) {
                scrollBehavior.state.heightOffset.toDp()
            }
            val stageHeight = (maxHeight - expandedTopPadding * 2 - 16.dp)
                .coerceAtLeast(300.dp)
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(Modifier.fillMaxWidth().height(stageHeight), contentAlignment = Alignment.Center) {
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
                                progress = s.progress,
                                value = s.latestMs,
                                unit = "ms",
                                label = "Checking response time",
                                format = ::formatMs
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
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(s.result.verdict.headline, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
                            // One supporting line: how it compares with before, or what it means on a first run.
                            Text(
                                if (history.size >= 2) com.pulsewave.app.feature.history.trendInsight(history) else s.result.verdict.explanation,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                        ResultTiles(s.result, Modifier.padding(top = 8.dp))
                        if (s.result.savedId != null) RoomTagger(savedRooms, taggedRoom, onTag = { haptics.click(); viewModel.tagRoom(it) })
                        Button(
                            onClick = { haptics.click(); viewModel.start() },
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight)
                        ) { Text("Test again", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)) }
                    }
                    is LinkTestState.Pinging -> {
                        OutlinedButton(onClick = { haptics.light(); viewModel.cancel() }, shapes = ButtonDefaults.shapes()) { Text("Cancel") }
                    }
                    is LinkTestState.Failed -> {
                        Text(s.message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                    LinkTestState.Idle -> {
                        IdleInfo(status)
                        if (history.isNotEmpty()) LastTestCard(history, onClick = onOpenHistory)
                    }
                }
                }
                }

                message?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                TextButton(
                    onClick = onOpenInternetTest,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(Icons.Rounded.Public, null, Modifier.size(18.dp))
                    Text("  Test internet speed instead")
                }
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
    // Critically damped, slow springs chase each new target without overshoot and keep their
    // velocity when the next reply lands, so bursts of replies read as one continuous motion.
    val chase = androidx.compose.animation.core.spring<Float>(dampingRatio = 1f, stiffness = androidx.compose.animation.core.Spring.StiffnessVeryLow)
    val shown by animateFloatAsState(value ?: 0f, chase, label = "linkValue")
    val stroke = with(LocalDensity.current) { Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round) }
    val animated by animateFloatAsState(progress, chase, label = "linkProgress")
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
                Text(" $unit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Normal, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 14.dp))
            }
            Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun ResultStage(result: LinkTestResult) {
    val colors = signalColors(result.verdict.quality())
    // The final number counts up once, so the result lands rather than appears.
    val target = result.latency.medianMs.takeIf { it.isFinite() } ?: 0f
    val count = remember(result) { androidx.compose.animation.core.Animatable(0f) }
    val countSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(result) { count.animateTo(target, countSpec) }
    SignalMorphBlob(quality = result.verdict.quality(), color = colors.container, size = 248.dp, spinning = false) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if (result.latency.samples > 0) formatMs(count.value) else "—",
                style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Black, color = colors.onContainer)
            Text("ms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Normal, color = colors.onContainer.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun ResultTiles(result: LinkTestResult, modifier: Modifier = Modifier) {
    // Three plain numbers in one card; the details live in History.
    Surface(shape = PulseWaveShapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(vertical = 20.dp)) {
            Stat("Replies", "${result.latency.samples}", Modifier.weight(1f))
            Stat("Variation", "±${formatMs(result.latency.jitterMs)} ms", Modifier.weight(1f))
            Stat("Lost", "${result.latency.lossPercent.fixed(0)}%", Modifier.weight(1f))
        }
    }
}

@Composable
private fun Stat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"), fontWeight = FontWeight.Bold, maxLines = 1)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun IdleInfo(status: NetworkStatus) {
    Text(
        if (status.isConnected) "Keep your device still for a few seconds." else "Connect to your Wi-Fi to start.",
        style = MaterialTheme.typography.titleMedium,
        textAlign = TextAlign.Center
    )
    if (status.isConnected) {
        Text(
            buildString {
                append(status.ssid)
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
private fun LastTestCard(points: List<com.pulsewave.app.feature.history.RouterTestPoint>, onClick: () -> Unit) {
    val last = points.last()
    // Tapping opens History, where every earlier test lives.
    Surface(
        onClick = onClick,
        shape = PulseWaveShapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Last time", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(
                    com.pulsewave.app.feature.history.momentLabel(last.timestamp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Column {
                Text(
                    last.responseMs?.let { "${formatMs(it)} ms" } ?: "No reply",
                    style = MaterialTheme.typography.displaySmall.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold
                )
                // The verdict reads as a sentence under the number, tinted by how it went.
                Text(
                    last.verdict.headline.removeSuffix(" here"),
                    style = MaterialTheme.typography.titleMedium,
                    color = signalColors(last.verdict.quality()).accent
                )
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
    // The same "Where are you?" tiles as Rooms, behind one button, so the result screen stays calm.
    var picking by rememberSaveable { mutableStateOf(false) }
    FilledTonalButton(onClick = { picking = true }, shapes = ButtonDefaults.shapes()) {
        Icon(if (tagged == null) Icons.Outlined.AddLocationAlt else Icons.Rounded.Check, null, Modifier.size(ButtonDefaults.IconSize))
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(if (tagged == null) "Which room is this?" else "Saved to $tagged")
    }
    if (picking) com.pulsewave.app.feature.walk.RoomPickerSheet(
        existing = saved,
        onSave = { room -> picking = false; onTag(room) },
        onDismiss = { picking = false }
    )
}

/** Idle (or failed), running, or done: the only changes worth animating. */
private fun phaseOf(state: LinkTestState): Int = when (state) {
    LinkTestState.Idle, is LinkTestState.Failed -> 0
    is LinkTestState.Pinging -> 1
    is LinkTestState.Done -> 2
}
