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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp

import com.pulsewave.app.core.datastore.SettingsRepository
import com.pulsewave.app.core.designsystem.PulseWaveAlpha
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import org.koin.compose.koinInject
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    onNavigateToPowerTools: () -> Unit = {},
    onNavigateToAbout: () -> Unit = {},
    settingsRepository: SettingsRepository = koinInject()
) {
    val dynamicColor by settingsRepository.dynamicColor.collectAsState(initial = true)
    val autoScan by settingsRepository.autoScan.collectAsState(initial = false)
    val seedColor by settingsRepository.seedColor.collectAsState(initial = 0xFF3F51B5.toInt())
    val scope = rememberCoroutineScope()
    val haptics = rememberPulseWaveHapticFeedback()
    
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
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
            // Appearance Section
            item {
                SettingsSection("Appearance") {
                    SettingsToggle(
                        icon = Icons.Default.Palette,
                        title = "Dynamic Color",
                        subtitle = "Use Material You colors",
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
                            "Theme Color",
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
    
            // Scanning Section
            item {
                SettingsSection("Scanning") {
                    SettingsToggle(
                        icon = Icons.Default.Refresh,
                        title = "Auto Scan",
                        subtitle = "Scan when opening WiFi tab",
                        checked = autoScan,
                        onCheckedChange = { 
                            haptics.click()
                            scope.launch { settingsRepository.setAutoScan(it) }
                        }
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp), 
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = PulseWaveAlpha.medium)
                    )
                    SettingsInfoRow(
                        icon = Icons.Default.Speed,
                        title = "Scan Mode",
                        value = "Adaptive"
                    )
                }
            }
    
            // Power Tools Section
            item {
                SettingsSection("Advanced") {
                    SettingsRow(
                        icon = Icons.Default.Build,
                        title = "Power Tools",
                        subtitle = "Throttling help, privileged mode",
                        onClick = {
                            haptics.click()
                            onNavigateToPowerTools()
                        }
                    )
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
    iconContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
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
        Surface(
            shape = PulseWaveShapes.small,
            color = iconContainerColor,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
    iconContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = PulseWaveShapes.small,
            color = iconContainerColor,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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
    iconContainerColor: Color = MaterialTheme.colorScheme.surfaceContainerHigh
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = PulseWaveShapes.small,
            color = iconContainerColor,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon, 
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PowerToolsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val clipboardManager = LocalClipboardManager.current
    val haptics = rememberPulseWaveHapticFeedback()
    var showCopiedSnackbar by remember { mutableStateOf(false) }
    
    val adbCommand = "adb shell settings put global wifi_scan_throttle_enabled 0"

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Power Tools") },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(onClick = {
                        haptics.light()
                        onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = {
            if (showCopiedSnackbar) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    shape = PulseWaveShapes.medium,
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                    action = {
                        TextButton(onClick = { showCopiedSnackbar = false }) {
                            Text("OK", color = MaterialTheme.colorScheme.inversePrimary)
                        }
                    }
                ) {
                    Text("ADB command copied to clipboard")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Throttling Help
            item {
                Card(
                    shape = PulseWaveShapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Build, null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Wi-Fi Scan Throttling",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Android limits Wi-Fi scans to 4 per 2 minutes in foreground. " +
                            "This is an OS-level restriction for privacy and battery.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Spacer(Modifier.height(24.dp))
                        
                        Surface(
                            shape = PulseWaveShapes.medium,
                            color = MaterialTheme.colorScheme.surface.copy(alpha = PulseWaveAlpha.medium),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    "To disable:",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Developer Options → Networking → Wi-Fi scan throttling → OFF",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                haptics.click()
                                clipboardManager.setText(AnnotatedString(adbCommand))
                                showCopiedSnackbar = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = PulseWaveShapes.small
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Copy ADB Command")
                        }
                    }
                }
            }

            // Privileged Mode
            item {
                Card(
                    shape = PulseWaveShapes.extraLarge,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "Privileged Mode",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            shape = PulseWaveShapes.extraSmall,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                "Not Available",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Root access enables additional diagnostics and allows toggling " +
                            "system settings for testing. Not required for normal use.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
