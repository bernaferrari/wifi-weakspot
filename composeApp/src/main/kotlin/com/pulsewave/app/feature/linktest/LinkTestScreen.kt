@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.linktest

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

private fun formatMbps(mbps: Double): String = if (mbps >= 100) "%.0f".format(mbps) else "%.1f".format(mbps)
private fun formatMs(ms: Float): String = if (!ms.isFinite()) "—" else if (ms >= 10) "%.0f".format(ms) else "%.1f".format(ms)

@Composable
fun LinkTestScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onOpenInternetTest: () -> Unit = {},
    viewModel: LinkTestViewModel = koinViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
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
                    targetState = state::class,
                    transitionSpec = { (fadeIn(stageFade) + scaleIn(stageScale, initialScale = 0.9f)) togetherWith fadeOut(stageFade) },
                    label = "linkStage"
                ) { _ ->
                    when (val s = state) {
                        LinkTestState.Idle, is LinkTestState.Failed -> StartButton(
                            enabled = status.isConnected,
                            onClick = { haptics.heavy(); viewModel.start() }
                        )
                        is LinkTestState.Pinging -> ProgressStage(
                            progress = s.progress * 0.5f,
                            value = s.latestMs?.let(::formatMs) ?: "…",
                            unit = "ms",
                            label = "Checking response time"
                        )
                        is LinkTestState.Uploading -> ProgressStage(
                            progress = 0.5f + s.progress * 0.5f,
                            value = if (s.mbps > 0) formatMbps(s.mbps) else "…",
                            unit = "Mbps",
                            label = "Sending data to your router"
                        )
                        is LinkTestState.Done -> ResultStage(s.result)
                    }
                }
            }

            when (val s = state) {
                is LinkTestState.Done -> {
                    Text(s.result.verdict.headline, style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                    Text(
                        s.result.verdict.explanation,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
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
                LinkTestState.Idle -> IdleInfo(status)
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
private fun ProgressStage(progress: Float, value: String, unit: String, label: String) {
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
                    value,
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
    SignalMorphBlob(quality = result.verdict.quality(), color = colors.container, size = 248.dp, spinning = false) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (result.uploadMbps != null) {
                Text(
                    formatMbps(result.uploadMbps),
                    style = MaterialTheme.typography.displayLarge.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Black,
                    color = colors.onContainer
                )
                Text("Mbps to router", style = MaterialTheme.typography.titleMedium, color = colors.onContainer)
            } else {
                Text(
                    formatMs(result.latency.medianMs),
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
                if (result.latency.lossPercent > 0) "%.0f%% replies lost".format(result.latency.lossPercent) else "No replies lost",
                Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatTile(
                "Link rate",
                if (result.rxLinkMbps > 0 && result.txLinkMbps > 0) "${result.rxLinkMbps} / ${result.txLinkMbps}" else "—",
                "Mbps down / up, as negotiated",
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
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
