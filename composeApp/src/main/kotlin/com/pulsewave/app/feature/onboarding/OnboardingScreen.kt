@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiFind
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.designsystem.LocalReduceMotion
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.component.ShapeIcon
import com.pulsewave.app.core.designsystem.component.SignalMorphBlob
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.permissions.PermissionGate
import com.pulsewave.app.core.wifi.SignalQuality
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

private enum class Step { Welcome, Permission }

/**
 * First run: what the app does and the one permission it needs. If access is already
 * granted, it's a single welcome page. Faster scans are offered later, when they matter.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val permissionGate: PermissionGate = koinInject()
    val permissions by permissionGate.state.collectAsStateWithLifecycle()
    val haptics = rememberPulseWaveHapticFeedback()

    // Fixed at launch so granting permission doesn't shift the pages underneath the person.
    val steps = remember {
        buildList {
            add(Step.Welcome)
            if (!permissions.canScanWifi) add(Step.Permission)
        }
    }
    var index by rememberSaveable { mutableIntStateOf(0) }
    val step = steps[index.coerceIn(steps.indices)]
    fun next() {
        haptics.click()
        if (index >= steps.lastIndex) onFinish() else index++
    }
    val requestPermission = com.pulsewave.app.core.permissions.rememberScanPermissionRequest { next() }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp, vertical = 16.dp)) {
            Row(Modifier.fillMaxWidth().heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
                StepDots(count = steps.size, current = index, modifier = Modifier.weight(1f))
                if (step != Step.Welcome) TextButton(onClick = { onFinish() }) { Text("Skip") }
            }
            val fade = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
            AnimatedContent(
                targetState = step,
                transitionSpec = {
                    val forward = targetState.ordinal > initialState.ordinal
                    (slideInHorizontally { if (forward) it / 4 else -it / 4 } + fadeIn(fade)) togetherWith
                        (slideOutHorizontally { if (forward) -it / 4 else it / 4 } + fadeOut(fade))
                },
                modifier = Modifier.weight(1f),
                label = "onboardingStep"
            ) { current ->
                Column(
                    Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    when (current) {
                        Step.Welcome -> WelcomePage()
                        Step.Permission -> MessagePage(
                            icon = Icons.Rounded.WifiFind,
                            shape = MaterialShapes.Cookie9Sided,
                            title = "See the Wi-Fi around you",
                            body = "To list nearby networks, Android asks for location access. PulseWave never saves where you are, and nothing leaves your phone.",
                            footnote = "You can use the signal meter and Wi-Fi speed test without it."
                        )
                    }
                }
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                when (step) {
                    Step.Welcome -> PrimaryButton("Get started") { next() }
                    Step.Permission -> {
                        PrimaryButton("Allow access", onClick = requestPermission)
                        TextButton(onClick = { next() }) { Text("Not now") }
                    }
                }
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight)
    ) { Text(text, style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)) }
}

@Composable
private fun StepDots(count: Int, current: Int, modifier: Modifier = Modifier) {
    if (count <= 1) {
        Spacer(modifier)
        return
    }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(count) { i ->
            val width by animateDpAsState(if (i == current) 28.dp else 8.dp, MaterialTheme.motionScheme.fastSpatialSpec(), label = "dot")
            Box(
                Modifier.size(width = width, height = 8.dp).background(
                    if (i <= current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                    PulseWaveShapes.full
                )
            )
        }
    }
}

/** The shape cycles from dead zone to excellent: the same language the whole app uses. */
@Composable
private fun WelcomePage() {
    val reduceMotion = LocalReduceMotion.current
    var quality by remember { mutableStateOf(if (reduceMotion) SignalQuality.Excellent else SignalQuality.DeadZone) }
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        val cycle = SignalQuality.entries.reversed()
        var i = 0
        while (true) {
            quality = cycle[i % cycle.size]
            delay(if (quality == SignalQuality.Excellent) 2_400 else 1_100)
            i++
        }
    }
    val colors = signalColors(quality)
    SignalMorphBlob(quality = quality, color = colors.container, size = 200.dp) {
        Icon(Icons.Rounded.Wifi, null, Modifier.size(80.dp), tint = colors.onContainer)
    }
    Spacer(Modifier.height(28.dp))
    Text(
        "Find bad Wi-Fi\nat home",
        style = MaterialTheme.typography.displaySmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.semantics { heading() }
    )
    Spacer(Modifier.height(12.dp))
    Text(
        "PulseWave shows how strong your Wi-Fi is, where it drops, and what to do about it.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(28.dp))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        val scheme = MaterialTheme.colorScheme
        FeatureRow(Icons.AutoMirrored.Rounded.DirectionsWalk, MaterialShapes.SoftBurst, scheme.tertiaryContainer, scheme.onTertiaryContainer,
            "Walk your home", "Watch the signal drop as you move.")
        FeatureRow(Icons.Rounded.Speed, MaterialShapes.Sunny, scheme.primaryContainer, scheme.onPrimaryContainer,
            "Test your Wi-Fi", "Phone-to-router speed, no internet needed.")
        FeatureRow(Icons.Rounded.Radar, MaterialShapes.Cookie7Sided, scheme.secondaryContainer, scheme.onSecondaryContainer,
            "See every network", "Pick a clearer channel for your router.")
        FeatureRow(Icons.Rounded.Shield, MaterialShapes.Puffy, scheme.surfaceContainerHighest, scheme.onSurface,
            "Private", "No account, no tracking, nothing uploaded.")
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    shape: RoundedPolygon,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    title: String,
    body: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ShapeIcon(icon, shape, container, content, size = 48.dp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun MessagePage(icon: ImageVector, shape: RoundedPolygon, title: String, body: String, footnote: String) {
    val scheme = MaterialTheme.colorScheme
    ShapeIcon(icon, shape, scheme.primaryContainer, scheme.onPrimaryContainer, size = 168.dp, iconSize = 72.dp)
    Spacer(Modifier.height(32.dp))
    Text(title, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
    Spacer(Modifier.height(12.dp))
    Text(body, style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant, textAlign = TextAlign.Center)
    Spacer(Modifier.height(16.dp))
    Text(footnote, style = MaterialTheme.typography.bodyMedium, color = scheme.primary, textAlign = TextAlign.Center)
}
