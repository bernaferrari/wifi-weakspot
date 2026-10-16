@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.setup

import androidx.compose.animation.togetherWith
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.component.ShapeIcon
import com.pulsewave.app.core.designsystem.component.SignalMorphBlob
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.wifi.ScanSetupStatus
import com.pulsewave.app.core.wifi.ScanThrottleManager
import com.pulsewave.app.core.wifi.SignalQuality
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import com.pulsewave.app.core.root.RootAccess
import com.pulsewave.app.core.root.RootState
import androidx.compose.material3.LoadingIndicator

private const val AdbCommand = "adb shell settings put global wifi_scan_throttle_enabled 0"

/** Re-reads Developer options each time the person comes back from system Settings. */
@Composable
fun rememberScanSetupStatus(throttleManager: ScanThrottleManager = koinInject()): ScanSetupStatus {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, throttleManager) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) throttleManager.refresh()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val status by throttleManager.setupStatus.collectAsStateWithLifecycle()
    return status
}

@Composable
fun ScanSetupScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val status = rememberScanSetupStatus()
    val platform: com.pulsewave.app.core.platform.PlatformActions = org.koin.compose.koinInject()
    val haptics = rememberPulseWaveHapticFeedback()
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showAdb by remember { mutableStateOf(false) }
    val root: RootAccess = koinInject()
    val rootState by root.state.collectAsStateWithLifecycle()
    var rootWorking by remember { mutableStateOf(false) }
    var showRoot by remember { mutableStateOf(false) }

    var wasUnlimited by remember { mutableStateOf(status.isUnlimited) }
    val startedLimited = remember { !status.isUnlimited }
    LaunchedEffect(status.isUnlimited) {
        if (status.isUnlimited && !wasUnlimited) haptics.success()
        wasUnlimited = status.isUnlimited
    }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Faster scans") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back") }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { StatusHero(status) }

            // Steps show while there's something to do, and stay (ticked) if you finished them just now.
            if (startedLimited && status.throttlingApplies && status.canDisableThrottling && !status.rootScanning) {
                item {
                    SetupStep(
                        number = 1,
                        shape = MaterialShapes.Cookie4Sided,
                        title = "Turn on Developer options",
                        body = "In Settings, open **About phone** and tap **Build number** 7 times. You may need to enter your screen lock.",
                        done = status.developerOptionsEnabled || !status.throttlingEnabled,
                        enabled = true,
                        actionLabel = "Open About phone",
                        onAction = { platform.openDeviceInfo() }
                    )
                }
                item {
                    SetupStep(
                        number = 2,
                        shape = MaterialShapes.Cookie6Sided,
                        title = "Turn off Wi-Fi scan throttling",
                        body = "In **Developer options**, scroll down to **Networking** and switch off **Wi-Fi scan throttling**.",
                        done = !status.throttlingEnabled,
                        enabled = status.developerOptionsEnabled,
                        actionLabel = "Open Developer options",
                        onAction = { platform.openDeveloperOptions() }
                    )
                }
                item {
                    SetupStep(
                        number = 3,
                        shape = MaterialShapes.Cookie9Sided,
                        title = "Come back here",
                        body = "PulseWave checks automatically when you return. You can switch throttling back on at any time.",
                        done = !status.throttlingEnabled,
                        enabled = true,
                        actionLabel = null,
                        onAction = {}
                    )
                }
                item {
                    Column {
                        TextButton(onClick = { showAdb = !showAdb }) {
                            Icon(Icons.Rounded.Terminal, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(if (showAdb) "Hide computer method" else "Prefer a computer? Use ADB")
                        }
                        AnimatedVisibility(showAdb) {
                            Card(
                                shape = PulseWaveShapes.medium,
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text(
                                        AdbCommand,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace)
                                    )
                                    CopyButton(onCopy = { platform.copyToClipboard("ADB command", AdbCommand) })
                                }
                            }
                        }
                    }
                }
            }

            if (status.rootAvailable || status.rootScanning) {
                item {
                    Column {
                        if (!status.rootScanning) {
                            TextButton(onClick = { showRoot = !showRoot }) {
                                Icon(Icons.Rounded.Terminal, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(if (showRoot) "Hide root option" else "Rooted phone? Use root instead")
                            }
                        }
                        AnimatedVisibility(showRoot || status.rootScanning) {
                            RootCard(
                                status = status,
                                rootState = rootState,
                                working = rootWorking,
                                onUseRoot = {
                                    rootWorking = true
                                    scope.launch {
                                        if (root.enable()) haptics.success() else haptics.error()
                                        rootWorking = false
                                    }
                                },
                                onStopRoot = { root.disable() }
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "Android limits scans to save battery. PulseWave never changes this for you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StatusHero(status: ScanSetupStatus) {
    val good = status.isUnlimited
    val colors = signalColors(if (good) SignalQuality.Excellent else SignalQuality.Fair)
    val (title, body) = when {
        status.rootScanning -> "Unlimited scans with root" to "PulseWave asks the system to scan, which Android never limits."
        good && !status.throttlingApplies -> "No scan limit on this phone" to "This Android version lets PulseWave scan as often as it needs."
        good -> "Unlimited scans are on" to "Nearby networks and channel graphs refresh every few seconds."
        !status.canDisableThrottling -> "Scans are limited" to "Android 9 allows 4 scans every 2 minutes and can't turn this off. PulseWave reuses recent results in between."
        status.developerOptionsEnabled -> "Scans are limited" to "Android allows only 4 scans every 2 minutes, so lists lag as you move. One switch removes the limit."
        else -> "Scans are limited" to "Android allows only 4 scans every 2 minutes, so lists lag as you move. Two quick steps remove the limit."
    }
    Card(
        shape = PulseWaveShapes.extraExtraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            SignalMorphBlob(
                quality = if (good) SignalQuality.Excellent else SignalQuality.Weak,
                color = colors.container,
                size = 140.dp
            ) {
                Icon(
                    if (good) Icons.Rounded.Bolt else Icons.Rounded.HourglassTop,
                    null,
                    tint = colors.onContainer,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(Modifier.height(20.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(highlighted(body), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SetupStep(
    number: Int,
    shape: RoundedPolygon,
    title: String,
    body: String,
    done: Boolean,
    enabled: Boolean,
    actionLabel: String?,
    onAction: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = PulseWaveShapes.large,
        colors = CardDefaults.cardColors(containerColor = if (done) scheme.surfaceContainer else scheme.surfaceContainerHigh)
    ) {
        Row(Modifier.fillMaxWidth().padding(20.dp)) {
            Box(contentAlignment = Alignment.Center) {
                ShapeIcon(
                    Icons.Rounded.Check, shape,
                    containerColor = if (done) scheme.primary else scheme.secondaryContainer,
                    contentColor = if (done) scheme.onPrimary else Color.Transparent,
                    size = 44.dp
                )
                if (!done) Text("$number", style = MaterialTheme.typography.titleMedium, color = scheme.onSecondaryContainer)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f, fill = false))
                    if (done) {
                        Spacer(Modifier.width(8.dp))
                        Surface(shape = PulseWaveShapes.full, color = scheme.primaryContainer, contentColor = scheme.onPrimaryContainer) {
                            Text("Done", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
                        }
                    }
                }
                Text(highlighted(body), style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                if (actionLabel != null && !done) {
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onAction, enabled = enabled, shapes = ButtonDefaults.shapes()) { Text(actionLabel) }
                }
            }
        }
    }
}

@Composable
private fun RootCard(
    status: ScanSetupStatus,
    rootState: RootState,
    working: Boolean,
    onUseRoot: () -> Unit,
    onStopRoot: () -> Unit
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = PulseWaveShapes.large,
        colors = CardDefaults.cardColors(containerColor = scheme.tertiaryContainer, contentColor = scheme.onTertiaryContainer)
    ) {
        Row(Modifier.fillMaxWidth().padding(20.dp)) {
            ShapeIcon(Icons.Rounded.Terminal, MaterialShapes.Clover8Leaf, scheme.tertiary, scheme.onTertiary, size = 44.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (status.rootScanning) "Using root" else "Your phone is rooted", style = MaterialTheme.typography.titleMedium)
                Text(
                    when {
                        status.rootScanning -> "Scans go through the system shell, so there's nothing to change in Developer options."
                        rootState == RootState.Denied -> "Root access was denied. Allow PulseWave in your root manager and try again, or use Developer options below."
                        status.isUnlimited -> "Scans are already unlimited. Root isn't needed."
                        else -> "Skip Developer options and let PulseWave scan through root. Your root manager will ask once."
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(8.dp))
                when {
                    status.rootScanning -> TextButton(onClick = onStopRoot) { Text("Stop using root") }
                    status.isUnlimited -> Unit
                    working -> Row(verticalAlignment = Alignment.CenterVertically) {
                        LoadingIndicator(Modifier.size(32.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Waiting for your root manager…", style = MaterialTheme.typography.labelLarge)
                    }
                    else -> Button(onClick = onUseRoot, shapes = ButtonDefaults.shapes()) {
                        Text(if (rootState == RootState.Denied) "Try root again" else "Use root")
                    }
                }
            }
        }
    }
}

/** Names of on-screen settings, written **like this**, are bolded in the accent color so they're easy to spot. */
@Composable
private fun highlighted(text: String): androidx.compose.ui.text.AnnotatedString {
    val accent = MaterialTheme.colorScheme.primary
    return androidx.compose.ui.text.buildAnnotatedString {
        text.split("**").forEachIndexed { index, part ->
            if (index % 2 == 1) withStyle(androidx.compose.ui.text.SpanStyle(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, color = accent)) { append(part) }
            else append(part)
        }
    }
}

/**
 * Confirms in place: the icon morphs to a check, the label to "Copied", and the button fills
 * with the accent color, then settles back. No snackbar to read.
 */
@Composable
private fun CopyButton(onCopy: () -> Unit) {
    val haptics = rememberPulseWaveHapticFeedback()
    var copied by remember { mutableStateOf(false) }
    LaunchedEffect(copied) {
        if (copied) { kotlinx.coroutines.delay(2_000); copied = false }
    }
    val scheme = MaterialTheme.colorScheme
    val container by androidx.compose.animation.animateColorAsState(
        if (copied) scheme.primary else scheme.secondaryContainer, MaterialTheme.motionScheme.defaultEffectsSpec(), label = "copyContainer"
    )
    val content by androidx.compose.animation.animateColorAsState(
        if (copied) scheme.onPrimary else scheme.onSecondaryContainer, MaterialTheme.motionScheme.defaultEffectsSpec(), label = "copyContent"
    )
    val pop = MaterialTheme.motionScheme.fastSpatialSpec<Float>()
    val fade = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    Button(
        onClick = { onCopy(); haptics.success(); copied = true },
        shapes = ButtonDefaults.shapes(),
        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content)
    ) {
        androidx.compose.animation.AnimatedContent(
            targetState = copied,
            transitionSpec = {
                (androidx.compose.animation.scaleIn(pop, initialScale = 0.6f) + androidx.compose.animation.fadeIn(fade)) togetherWith
                    androidx.compose.animation.fadeOut(fade)
            },
            label = "copyLabel"
        ) { done ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(if (done) Icons.Rounded.Check else Icons.Rounded.ContentCopy, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(if (done) "Copied" else "Copy command")
            }
        }
    }
}
