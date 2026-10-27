@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.settings

import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.BrightnessAuto
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.selection.selectableGroup
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.animation.Crossfade

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
    // The app theme stays composed between tabs and already owns the loaded preferences.
    // Starting fresh collectors here briefly selected Auto/wallpaper/defaults on every visit.
    val themeSettings = com.bernaferrari.wifiweakspot.core.designsystem.LocalThemeSettings.current
    val dynamicColor = themeSettings.dynamicColor
    val seedColor = themeSettings.seedColor
    val darkMode = themeSettings.darkMode
    val reduceMotion = themeSettings.reduceMotion
    val hapticsEnabled = com.bernaferrari.wifiweakspot.core.designsystem.LocalHapticsEnabled.current
    val signalDisplay = com.bernaferrari.wifiweakspot.core.designsystem.LocalSignalDisplay.current
    val permissionGate: com.bernaferrari.wifiweakspot.core.permissions.PermissionGate = koinInject()
    val permissionState by permissionGate.state.collectAsState()
    val networkStatusRepository: com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository = koinInject()
    val networkStatus by remember(networkStatusRepository) { networkStatusRepository.getCurrentNetworkStatus() }.collectAsState(initial = null)
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
                title = { Text(strings.common.tabSettings) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.common.back) }
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
                if (!scanSetup.isUnlimited || scanSetup.rootScanning) SettingsSection(strings.settings.sectionScanning) {
                    // Shows the real state. Only Developer options (or root) can change it, so the
                    // switch opens the short guide rather than pretending to flip it.
                    SettingsToggle(
                        icon = Icons.Default.Bolt,
                        title = strings.common.fasterScans,
                        subtitle = when {
                            scanSetup.rootScanning -> strings.survey.unlimitedWithRootTitle
                            scanSetup.isUnlimited -> strings.survey.unlimitedOnBody
                            else -> strings.settings.fasterScansThrottled
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
                SettingsSection(strings.settings.sectionAppearance) {
                    val themes = listOf(
                        Triple(strings.settings.themeAuto, null, Icons.Rounded.BrightnessAuto),
                        Triple(strings.settings.themeLight, false, Icons.Rounded.LightMode),
                        Triple(strings.settings.themeDark, true, Icons.Rounded.DarkMode)
                    )
                    Box(Modifier.fillMaxWidth().padding(16.dp)) {
                        SettingsChoices(themes, themes.first { it.second == darkMode }, onSelect = { (_, value, _) ->
                            haptics.light(); scope.launch { settingsRepository.setDarkMode(value) }
                        }) { (label, _, icon) ->
                            Icon(icon, null, Modifier.size(ToggleButtonDefaults.IconSize))
                            Spacer(Modifier.width(ToggleButtonDefaults.IconSpacing))
                            Text(label, maxLines = 1)
                        }
                    }
                    val platformTheme: com.bernaferrari.wifiweakspot.core.platform.PlatformTheme = org.koin.compose.koinInject()
                    val wallpaperScheme = platformTheme.dynamicColorScheme(darkMode ?: androidx.compose.foundation.isSystemInDarkTheme())
                    Box(Modifier.fillMaxWidth().padding(16.dp)) {
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
                    ColorStylePicker(
                        selected = themeSettings.colorStyle,
                        seedColor = themeSettings.resolvedSeed,
                        dark = themeSettings.effectiveDark,
                        onSelect = { style ->
                            haptics.light()
                            scope.launch { settingsRepository.setColorStyle(style) }
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                    SettingsBlock(
                        icon = Icons.Default.SignalCellularAlt,
                        title = strings.settings.showSignalAs,
                        // The real network badge with your current signal (a sample 84% when offline),
                        // so each choice previews exactly what the lists will show.
                        leading = {
                            Crossfade(signalDisplay, label = "signalPreview") { display ->
                                CompositionLocalProvider(com.bernaferrari.wifiweakspot.core.designsystem.LocalSignalDisplay provides display) {
                                    com.bernaferrari.wifiweakspot.core.designsystem.component.WifiBadge(rssi = networkStatus?.takeIf { it.isConnected }?.rssi ?: -58, size = 40.dp)
                                }
                            }
                        }
                    ) {
                        val displays = com.bernaferrari.wifiweakspot.core.wifi.SignalDisplay.entries
                        SettingsChoices(displays, signalDisplay, onSelect = { display ->
                            haptics.light(); scope.launch { settingsRepository.setSignalDisplay(display) }
                        }) { Text(it.label, maxLines = 1) }
                    }
                }
            }

            item {
                SettingsSection(strings.settings.sectionComfort) {
                    SettingsToggle(icon = Icons.Default.Animation, title = strings.settings.reduceMotionTitle, checked = reduceMotion,
                        onCheckedChange = { scope.launch { settingsRepository.setReduceMotion(it) } })
                    SettingsToggle(icon = Icons.Default.Vibration, title = strings.settings.hapticsSubtitle, checked = hapticsEnabled,
                        onCheckedChange = { scope.launch { settingsRepository.setHapticsEnabled(it) } })
                }
            }

            // Background Monitoring Section
            item {
                SettingsSection(strings.settings.sectionAlerts) {
                    // Main toggle
                    val notificationsDenied = strings.settings.notificationsDenied
                    SettingsToggle(
                        icon = Icons.Default.Radar,
                        title = strings.settings.backgroundAlertsTitle,
                        subtitle = strings.settings.backgroundAlertsSubtitle,
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
                                    else platform.showMessage(notificationsDenied)
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
                        SettingsBlock(
                            icon = Icons.Default.Schedule,
                            title = strings.settings.checkEvery,
                            trailing = {
                                Text(
                                    if (intervalMinutes < 1f) strings.settings.intervalSeconds(30) else strings.settings.intervalMinutes(intervalMinutes.roundToInt()),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        ) {
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
                            title = strings.settings.newNetworkAlertsTitle,

                            checked = monitorSettings.notifyNewNetworks,
                            onCheckedChange = {
                                haptics.click()
                                monitorManager.updateSettings(monitorSettings.copy(notifyNewNetworks = it))
                            }
                        )

                        SettingsToggle(
                            icon = Icons.Default.SignalCellularAlt,
                            title = strings.settings.signalChangeAlertsTitle,
                            subtitle = strings.settings.signalChangeAlertsSubtitle,
                            checked = monitorSettings.notifySignalChange,
                            onCheckedChange = {
                                haptics.click()
                                monitorManager.updateSettings(monitorSettings.copy(notifySignalChange = it))
                            }
                        )
                    }
                }
            }

            item(key = "backup") { BackupSettings() }

            // About Section
            item {
                SettingsSection(strings.settings.sectionAbout) {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = strings.settings.aboutApp("WiFi\u00A0Weakspot"),
                        subtitle = strings.settings.version("1.0.0"),
                        onClick = {
                            haptics.click()
                            onNavigateToAbout()
                        }
                    )
                    SettingsRow(
                        icon = Icons.Default.School,
                        title = strings.settings.replayIntroTitle,

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
    content: @Composable () -> Unit
) {
    Column {
        // Inset past the card edge, so the header sits over its group rather than the screen margin.
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp)
        )
        SegmentedGroup(content)
    }
}

/**
 * Expressive settings groups: every row is its own segment with a hairline gap. The outer corners
 * are fully round and the inner ones nearly square, so the rows read as one group yet stay distinct.
 */
@Composable
private fun SegmentedGroup(content: @Composable () -> Unit) {
    val color = MaterialTheme.colorScheme.surfaceContainer
    val density = androidx.compose.ui.platform.LocalDensity.current
    val outer = with(density) { 24.dp.toPx() }
    val inner = with(density) { 6.dp.toPx() }
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
        val gap = 2.dp.roundToPx()
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

/** A soft primary tint behind a primary glyph: one calm accent per row, the same on or off. */
@Composable
private fun RowIcon(icon: ImageVector, tint: Color = MaterialTheme.colorScheme.primary) {
    com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon(
        icon = icon,
        shape = androidx.compose.material3.MaterialShapes.Cookie6Sided,
        containerColor = tint.copy(alpha = 0.12f),
        contentColor = tint,
        size = 40.dp,
        iconSize = 22.dp
    )
}

@Composable
private fun RowText(title: String, subtitle: String?, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium)
        subtitle?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

private val RowPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
private val RowMinHeight = 72.dp

@Composable
fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, role = androidx.compose.ui.semantics.Role.Button, onClick = onClick)
            .heightIn(min = RowMinHeight)
            .padding(RowPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RowIcon(icon)
        RowText(title, subtitle, Modifier.weight(1f))
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SettingsToggle(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    // The whole row is the switch: bigger target, one tap anywhere.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = androidx.compose.ui.semantics.Role.Switch, onValueChange = onCheckedChange)
            .heightIn(min = RowMinHeight)
            .padding(RowPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        RowIcon(icon)
        RowText(title, subtitle, Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}

/** A titled setting whose control sits underneath it, full width (button groups, swatches, sliders). */
@Composable
fun SettingsBlock(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    trailing: (@Composable () -> Unit)? = null,
    leading: @Composable () -> Unit = { RowIcon(icon) },
    content: @Composable () -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            leading()
            RowText(title, subtitle, Modifier.weight(1f))
            trailing?.invoke()
        }
        content()
    }
}

/** Exclusive choices as a connected button group, with colors that hold up on the card. */
@Composable
fun <T> SettingsChoices(
    options: List<T>,
    selected: T?,
    onSelect: (T) -> Unit,
    label: @Composable RowScope.(T) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        options.forEachIndexed { index, option ->
            ToggleButton(
                checked = option == selected,
                onCheckedChange = { if (it) onSelect(option) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.colors(
                    checkedContainerColor = MaterialTheme.colorScheme.primary,
                    checkedContentColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.weight(1f).heightIn(min = 48.dp)
            ) { label(option) }
        }
    }
}
