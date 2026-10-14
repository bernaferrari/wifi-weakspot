@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.settings

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
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat

import com.pulsewave.app.core.datastore.SettingsRepository
import com.pulsewave.app.core.designsystem.PulseWaveAlpha
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import com.pulsewave.app.core.monitoring.BackgroundMonitorManager
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
    val monitorManager: BackgroundMonitorManager = koinInject()
    val isMonitoring by monitorManager.isMonitoring.collectAsState()
    val monitorSettings by monitorManager.settings.collectAsState()
    val monitorError by monitorManager.error.collectAsState()
    var intervalMinutes by remember(monitorSettings.scanIntervalMs) { mutableFloatStateOf(monitorSettings.intervalMinutes) }
    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) monitorManager.startMonitoring()
        else android.widget.Toast.makeText(context, "Allow notifications to see monitoring status and alerts.", android.widget.Toast.LENGTH_LONG).show()
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
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
            contentPadding = PaddingValues(bottom = 24.dp),
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
                    SettingsToggle(
                        icon = Icons.Default.Palette,
                        title = "Match my wallpaper",
                        subtitle = "Use your phone's colors",
                        checked = dynamicColor,
                        onCheckedChange = {
                            haptics.click()
                            scope.launch { settingsRepository.setDynamicColor(it) }
                        }
                    )

                    if (!dynamicColor) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = PulseWaveAlpha.medium)
                        )
                        Text(
                            "App color",
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        ColorPickerRow(
                            currentSeed = seedColor,
                            onColorSelected = { color ->
                                haptics.click()
                                scope.launch { settingsRepository.setSeedColor(color) }
                            }
                        )
                    }
                }
            }

            item {
                SettingsSection("Comfort") {
                    SettingsToggle(icon = Icons.Default.Animation, title = "Reduce motion", subtitle = "Stop decorative loops and use instant transitions", checked = reduceMotion,
                        onCheckedChange = { scope.launch { settingsRepository.setReduceMotion(it) } })
                    SettingsToggle(icon = Icons.Default.Vibration, title = "Haptic feedback", subtitle = "Feel feedback for taps and results", checked = hapticsEnabled,
                        onCheckedChange = { scope.launch { settingsRepository.setHapticsEnabled(it) } })
                }
            }

            // Background Monitoring Section
            item {
                SettingsSection("Background monitoring") {
                    // Main toggle
                    SettingsToggle(
                        icon = if (isMonitoring) Icons.Default.Radar else Icons.Default.Radar,
                        title = "Watch in the background",
                        subtitle = if (isMonitoring) "On. Checking in the background" else "Off",
                        checked = isMonitoring,
                        onCheckedChange = { enabled ->
                            haptics.click()
                            if (!enabled) {
                                monitorManager.stopMonitoring()
                            } else if (!permissionState.canScanWifi) {
                                onRequestScanPermission()
                            } else if (needsNotificationPermission(context)) {
                                notificationPermissionLauncher.launch(
                                    android.Manifest.permission.POST_NOTIFICATIONS
                                )
                            } else {
                                monitorManager.startMonitoring()
                            }
                        }
                    )
                    Text("Checks nearby Wi-Fi every few minutes and tells you about changes. Stop it anytime from its notification.",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), style = MaterialTheme.typography.bodySmall)
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
                            subtitle = "Notify when new networks appear",
                            checked = monitorSettings.notifyNewNetworks,
                            onCheckedChange = {
                                haptics.click()
                                monitorManager.updateSettings(monitorSettings.copy(notifyNewNetworks = it))
                            }
                        )

                        SettingsToggle(
                            icon = Icons.Default.SignalCellularAlt,
                            title = "Signal change alerts",
                            subtitle = "Notify on significant signal changes",
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
        Column(modifier = Modifier.weight(1f)) {
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

private fun needsNotificationPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED
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
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
