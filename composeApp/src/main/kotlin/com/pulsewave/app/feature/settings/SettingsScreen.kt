@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.settings

import androidx.compose.ui.draw.clip
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

import com.pulsewave.app.core.datastore.SettingsRepository
import com.pulsewave.app.core.designsystem.PulseWaveAlpha
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import com.pulsewave.app.core.monitoring.BackgroundMonitor
import com.pulsewave.app.core.monitoring.MonitorSettings
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
    val permissionGate: com.pulsewave.app.core.permissions.PermissionGate = koinInject()
    val permissionState by permissionGate.state.collectAsState()
    val scope = rememberCoroutineScope()
    val haptics = rememberPulseWaveHapticFeedback()

    // Background monitoring
    val monitorManager: BackgroundMonitor = koinInject()
    val isMonitoring by monitorManager.isMonitoring.collectAsState()
    val monitorSettings by monitorManager.settings.collectAsState()
    val monitorError by monitorManager.error.collectAsState()
    var intervalMinutes by remember(monitorSettings.scanIntervalMs) { mutableFloatStateOf(monitorSettings.intervalMinutes) }
    val platform: com.pulsewave.app.core.platform.PlatformActions = koinInject()
    val launchers = com.pulsewave.app.core.platform.LocalPermissionLaunchers.current

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
            // Power Tools Section
            item {
                SettingsSection("Scanning") {
                    val scanSetup = com.pulsewave.app.feature.setup.rememberScanSetupStatus()
                    SettingsRow(
                        icon = Icons.Default.Bolt,
                        title = "Faster scans",
                        subtitle = when {
                            scanSetup.rootScanning -> "Unlimited, using root"
                            scanSetup.isUnlimited -> "Unlimited scanning is on"
                            else -> "Limited to 4 scans every 2 minutes. Tap to fix"
                        },
                        onClick = {
                            haptics.click()
                            onNavigateToScanSetup()
                        }
                    )
                }
            }

            // Appearance Section
            item {
                SettingsSection("Appearance") {
                    val themes = listOf("System" to null, "Light" to false, "Dark" to true)
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
                    ) {
                        themes.forEachIndexed { index, (label, value) ->
                            ToggleButton(
                                checked = darkMode == value,
                                onCheckedChange = { haptics.light(); scope.launch { settingsRepository.setDarkMode(value) } },
                                shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    themes.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text(label) }
                        }
                    }
                    val platformTheme: com.pulsewave.app.core.platform.PlatformTheme = org.koin.compose.koinInject()
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
                SettingsSection("Background") {
                    // Main toggle
                    SettingsToggle(
                        icon = Icons.Default.Radar,
                        title = "Background alerts",
                        subtitle = "Tell me when nearby Wi-Fi changes",
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
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = PulseWaveAlpha.medium)
                        )

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

                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = PulseWaveAlpha.medium)
                        )

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
                        title = "About PulseWave",
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
    content: @Composable ColumnScope.() -> Unit
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
        Card(
            shape = PulseWaveShapes.extraLarge,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
            )
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
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
        animationSpec = if (com.pulsewave.app.core.designsystem.LocalReduceMotion.current) androidx.compose.animation.core.snap() else spring(
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
        com.pulsewave.app.core.designsystem.component.ShapeIcon(
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
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PulseWaveAlpha.strong)
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
        com.pulsewave.app.core.designsystem.component.ShapeIcon(
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
            shape = PulseWaveShapes.extraSmall,
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
        com.pulsewave.app.core.designsystem.component.ShapeIcon(
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
