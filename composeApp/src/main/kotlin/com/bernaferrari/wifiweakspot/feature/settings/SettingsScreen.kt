@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.settings

import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.selection.toggleable
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.nestedscroll.nestedScroll

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

import com.bernaferrari.wifiweakspot.core.datastore.SettingsRepository
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotAlpha
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.designsystem.rememberWifiWeakspotHapticFeedback
import com.bernaferrari.wifiweakspot.core.monitoring.BackgroundMonitor
import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSettings
import org.koin.compose.koinInject
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToScanSetup: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    onBack: (() -> Unit)? = null,
    onRequestScanPermission: () -> Unit = {},
    settingsRepository: SettingsRepository = koinInject()
) {
    val dynamicColor by settingsRepository.dynamicColor.collectAsState(initial = true)
    val seedColor by settingsRepository.seedColor.collectAsState(initial = 0xFF3F51B5.toInt())
    val darkMode by settingsRepository.isDarkMode.collectAsState(initial = null)
    val reduceMotion by settingsRepository.reduceMotion.collectAsState(initial = false)
    val hapticsEnabled by settingsRepository.hapticsEnabled.collectAsState(initial = true)
    val signalDisplay by settingsRepository.signalDisplay.collectAsState(initial = com.bernaferrari.wifiweakspot.core.wifi.SignalDisplay.Percentage)
    val permissionGate: com.bernaferrari.wifiweakspot.core.permissions.PermissionGate = koinInject()
    val permissionState by permissionGate.state.collectAsState()
    val scope = rememberCoroutineScope()
    val haptics = rememberWifiWeakspotHapticFeedback()

    // Background monitoring
    val monitorManager: BackgroundMonitor = koinInject()
    val isMonitoring by monitorManager.isMonitoring.collectAsState()
    val monitorSettings by monitorManager.settings.collectAsState()
    val monitorError by monitorManager.error.collectAsState()
    var intervalMinutes by remember(monitorSettings.scanIntervalMs) { mutableFloatStateOf(monitorSettings.intervalMinutes) }
    val platform: com.bernaferrari.wifiweakspot.core.platform.PlatformActions = koinInject()
    val launchers = com.bernaferrari.wifiweakspot.core.platform.LocalPermissionLaunchers.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            // Room below the last card, matching the gap between sections.
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Only while there's something to do: Android still limits scans, or root is in use.
            item(key = "scanning") {
                val scanSetup = com.bernaferrari.wifiweakspot.feature.setup.rememberScanSetupStatus()
                if (!scanSetup.isUnlimited || scanSetup.rootScanning) SettingsSection("Scanning") {
                    // Shows the real state. Only Developer options (or root) can change it, so the
                    // switch opens the short guide rather than pretending to flip it.
                    SettingsToggle(
                        icon = Icons.Default.Bolt,
                        title = "Faster scans",
                        subtitle = when {
                            scanSetup.rootScanning -> "Unlimited, using root"
                            scanSetup.isUnlimited -> "Networks refresh every few seconds"
                            else -> "Android allows 4 scans every 2 minutes"
                        },
                        checked = scanSetup.isUnlimited,
                        onCheckedChange = {
                            haptics.click()
                            onNavigateToScanSetup()
                        }
                    )
                }
            }

            // Appearance Section
            item {
                SettingsSection("Appearance") {
                    val themes = listOf(
                        Triple("Auto", null, Icons.Rounded.BrightnessAuto),
                        Triple("Light", false, Icons.Rounded.LightMode),
                        Triple("Dark", true, Icons.Rounded.DarkMode)
                    )
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                    ) {
                        themes.forEachIndexed { index, (label, value, icon) ->
                            ToggleButton(
                                checked = darkMode == value,
                                onCheckedChange = { haptics.light(); scope.launch { settingsRepository.setDarkMode(value) } },
                                shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    themes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(icon, null, Modifier.size(ToggleButtonDefaults.IconSize))
                                Spacer(Modifier.width(ToggleButtonDefaults.IconSpacing))
                                Text(label, maxLines = 1)
                            }
                        }
                    }
                    val platformTheme: com.bernaferrari.wifiweakspot.core.platform.PlatformTheme = org.koin.compose.koinInject()
                    val wallpaperScheme = platformTheme.dynamicColorScheme(androidx.compose.foundation.isSystemInDarkTheme())
                    ColorPickerRow(
                        useWallpaper = dynamicColor,
                        currentSeed = seedColor,
                        wallpaper = wallpaperScheme?.let { listOf(it.primary, it.secondary, it.tertiary) },
                        onWallpaper = { scope.launch { settingsRepository.setDynamicColor(true) } },
                        onSeed = { color ->
                            scope.launch {
                                settingsRepository.setSeedColor(color)
                                settingsRepository.setDynamicColor(false)
                            }
                        }
                    )
                    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Show signal as", style = MaterialTheme.typography.bodyLarge)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                            val displays = com.bernaferrari.wifiweakspot.core.wifi.SignalDisplay.entries
                            displays.forEachIndexed { index, display ->
                                ToggleButton(
                                    checked = signalDisplay == display,
                                    onCheckedChange = { haptics.light(); scope.launch { settingsRepository.setSignalDisplay(display) } },
                                    shapes = when (index) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        displays.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) { Text(display.label) }
                            }
                        }
                        Text("Signal strength on a fixed scale. Higher is stronger.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item {
                SettingsSection("Comfort") {
                    SettingsToggle(icon = Icons.Default.Animation, title = "Reduce motion", subtitle = "Calmer animations", checked = reduceMotion,
                        onCheckedChange = { scope.launch { settingsRepository.setReduceMotion(it) } })
                    SettingsToggle(icon = Icons.Default.Vibration, title = "Haptic feedback", subtitle = "Vibrate on taps and results", checked = hapticsEnabled,
                        onCheckedChange = { scope.launch { settingsRepository.setHapticsEnabled(it) } })
                }
            }

            // Background Monitoring Section
            item {
                SettingsSection("Alerts") {
                    // Main toggle
                    SettingsToggle(
                        icon = Icons.Default.Radar,
                        title = "Background alerts",
                        subtitle = "When nearby Wi-Fi changes",
                        checked = isMonitoring,
                        onCheckedChange = { enabled ->
                            haptics.click()
                            if (!enabled) {
                                monitorManager.stopMonitoring()
                            } else if (!permissionState.canScanWifi) {
                                onRequestScanPermission()
                            } else if (platform.needsNotificationPermission()) {
                                launchers.notifications { granted ->
                                    if (granted) monitorManager.startMonitoring()
                                    else platform.showMessage("Allow notifications to see monitoring status and alerts.")
                                }
                            } else {
                                monitorManager.startMonitoring()
                            }
                        }
                    )
                    monitorError?.let { message ->
                        Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
                    }

                    // Settings (only visible when enabled)
                    if (isMonitoring) {
                        // Scan interval slider
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Check every",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    if (intervalMinutes < 1f) "30 sec" else "${intervalMinutes.roundToInt()} min",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = intervalMinutes,
                                onValueChange = { intervalMinutes = it },
                                onValueChangeFinished = { monitorManager.updateSettings(monitorSettings.withIntervalMinutes(intervalMinutes)) },
                                valueRange = 0.5f..10f,
                                steps = 18
                            )
                        }

                        // Notification options
                        SettingsToggle(
                            icon = Icons.Default.Notifications,
                            title = "New network alerts",
                            subtitle = "When a network appears nearby",
                            checked = monitorSettings.notifyNewNetworks,
                            onCheckedChange = {
                                haptics.click()
                                monitorManager.updateSettings(monitorSettings.copy(notifyNewNetworks = it))
                            }
                        )

                        SettingsToggle(
                            icon = Icons.Default.SignalCellularAlt,
                            title = "Signal change alerts",
                            subtitle = "When your signal rises or drops a lot",
                            checked = monitorSettings.notifySignalChange,
                            onCheckedChange = {
                                haptics.click()
                                monitorManager.updateSettings(monitorSettings.copy(notifySignalChange = it))
                            }
                        )
                    }
                }
            }

            // About Section
            item {
                SettingsSection("About") {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = "About WiFi\u00A0Weakspot",
                        subtitle = "Version 1.0.0",
                        onClick = {
                            haptics.click()
                            onNavigateToAbout()
                        }
                    )
                    SettingsRow(
                        icon = Icons.Default.School,
                        title = "Show the introduction again",
                        subtitle = "Replay the welcome and setup steps",
                        onClick = {
                            haptics.click()
                            scope.launch { settingsRepository.setOnboardingDone(false) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    icon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
        ) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
        SegmentedGroup(content)
    }
}

/**
 * Expressive settings groups: every row is its own segment with a hairline gap. The outer corners
 * are fully round and the inner ones nearly square, so the rows read as one group yet stay distinct.
 */
@Composable
private fun SegmentedGroup(content: @Composable () -> Unit) {
    val color = MaterialTheme.colorScheme.surfaceContainerLow
    val density = androidx.compose.ui.platform.LocalDensity.current
    val outer = with(density) { 24.dp.toPx() }
    val inner = with(density) { 8.dp.toPx() }
    var segments by remember { mutableStateOf(emptyList<androidx.compose.ui.geometry.Rect>()) }
    androidx.compose.ui.layout.Layout(
        content = content,
        modifier = Modifier
            .fillMaxWidth()
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(24.dp))
            .drawBehind {
                segments.forEachIndexed { index, rect ->
                    val top = androidx.compose.ui.geometry.CornerRadius(if (index == 0) outer else inner)
                    val bottom = androidx.compose.ui.geometry.CornerRadius(if (index == segments.lastIndex) outer else inner)
                    val path = androidx.compose.ui.graphics.Path().apply {
                        addRoundRect(androidx.compose.ui.geometry.RoundRect(rect, top, top, bottom, bottom))
                    }
                    drawPath(path, color)
                }
            }
    ) { measurables, constraints ->
        val gap = 4.dp.roundToPx()
        val placeables = measurables.map { it.measure(constraints.copy(minHeight = 0)) }.filter { it.height > 0 }
        val width = constraints.maxWidth
        val height = placeables.sumOf { it.height } + gap * (placeables.size - 1).coerceAtLeast(0)
        layout(width, height) {
            var y = 0
            val rects = placeables.map { placeable ->
                placeable.place(0, y)
                androidx.compose.ui.geometry.Rect(0f, y.toFloat(), width.toFloat(), (y + placeable.height).toFloat()).also { y += placeable.height + gap }
            }
            if (rects != segments) segments = rects
        }
    }
}

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    iconContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = if (com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion.current) androidx.compose.animation.core.snap() else spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "pressScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(),
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon(
            icon = icon,
            shape = androidx.compose.material3.MaterialShapes.Cookie6Sided,
            containerColor = iconContainerColor,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            size = 40.dp,
            iconSize = 20.dp
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = WifiWeakspotAlpha.strong)
        )
    }
}

@Composable
fun SettingsInfoRow(
    icon: ImageVector,
    title: String,
    value: String,
    iconContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon(
            icon = icon,
            shape = androidx.compose.material3.MaterialShapes.Cookie6Sided,
            containerColor = iconContainerColor,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            size = 40.dp,
            iconSize = 20.dp
        )
        Spacer(Modifier.width(16.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
        Surface(
            shape = WifiWeakspotShapes.extraSmall,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                value,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    iconContainerColor: Color = MaterialTheme.colorScheme.secondaryContainer
) {
    // The whole row is the switch: bigger target, one tap anywhere. The icon fills in when on.
    val scheme = MaterialTheme.colorScheme
    val container by androidx.compose.animation.animateColorAsState(if (checked) scheme.primary else iconContainerColor, label = "toggleIcon")
    val content by androidx.compose.animation.animateColorAsState(if (checked) scheme.onPrimary else scheme.onSecondaryContainer, label = "toggleIconContent")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .toggleable(value = checked, role = androidx.compose.ui.semantics.Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon(
            icon = icon,
            shape = androidx.compose.material3.MaterialShapes.Cookie6Sided,
            containerColor = container,
            contentColor = content,
            size = 40.dp,
            iconSize = 20.dp
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = null)
    }
}
