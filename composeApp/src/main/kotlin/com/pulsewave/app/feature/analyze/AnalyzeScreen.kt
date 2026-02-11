@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.analyze

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import kotlin.math.pow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.wifi.getVendorName
import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.wifi.ThrottleState
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.PulseWaveAlpha
import com.pulsewave.app.core.designsystem.ShimmerPlaceholder
import com.pulsewave.app.core.designsystem.component.ScanRadarIndicator
import com.pulsewave.app.core.datastore.SettingsRepository
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

enum class FrequencyBand(val label: String) {
    BAND_2_4("2.4 GHz"),
    BAND_5("5 GHz"),
    BAND_6("6 GHz")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InsightStrip(
    networks: List<WifiNetwork>,
    selectedBand: FrequencyBand,
    modifier: Modifier = Modifier
) {
    val filteredNetworks = networks.filter { network ->
        when (selectedBand) {
            FrequencyBand.BAND_2_4 -> network.frequency in 2400..2500
            FrequencyBand.BAND_5 -> network.frequency in 5150..5900
            FrequencyBand.BAND_6 -> network.frequency in 5925..7125
        }
    }
    
    if (filteredNetworks.isEmpty()) return
    
    // Use CongestionAnalyzer for sophisticated channel analysis
    val channelScores = remember(filteredNetworks, selectedBand) {
        CongestionAnalyzer.analyze(filteredNetworks, selectedBand)
    }
    
    val bestChannel = channelScores.find { it.recommendation }
    val congestedChannels = channelScores.filter { it.rating == TrafficRating.HIGH || it.rating == TrafficRating.SEVERE }
    
    val strongestNetwork = filteredNetworks.maxByOrNull { it.rssi }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = PulseWaveShapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Lightbulb,
                        contentDescription = "Insights",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Insights",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                
                // Network count chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Text(
                        "${filteredNetworks.size} networks",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Best channel recommendation (prominent)
            if (bestChannel != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (bestChannel.rating) {
                            TrafficRating.LOW -> Color(0xFF4CAF50)
                            TrafficRating.MEDIUM -> Color(0xFFFFC107)
                            else -> Color(0xFFFF5722)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = Color.White
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Ch ${bestChannel.channelNumber}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Best channel • ${bestChannel.rating.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // Congestion warnings
            if (congestedChannels.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Warning,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Avoid: Ch ${congestedChannels.take(3).joinToString(", ") { "${it.channelNumber}" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // Signal strength info
            strongestNetwork?.let { network ->
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SignalDot(network.rssi)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${network.ssid.ifEmpty { "Hidden" }} • ${network.rssi} dBm",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun SignalDot(rssi: Int) {
    val color = when {
        rssi > -50 -> Color(0xFF4CAF50)
        rssi > -60 -> Color(0xFF8BC34A)
        rssi > -70 -> Color(0xFFFFC107)
        else -> Color(0xFFFF5722)
    }
    Surface(
        shape = CircleShape,
        color = color,
        modifier = Modifier.size(8.dp)
    ) {}
}

@Composable
fun GraphLegendDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Understanding the Graph") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendItem(
                    title = "Signal Strength (Y-axis)",
                    description = "Higher peaks = stronger signal. -30 dBm is excellent, -80 dBm is weak."
                )
                LegendItem(
                    title = "Channel (X-axis)",
                    description = "Wi-Fi channels. In 2.4GHz, channels 1, 6, and 11 don't overlap."
                )
                LegendItem(
                    title = "Curve Width",
                    description = "Represents channel width (20/40/80 MHz). Wider = more bandwidth but more interference."
                )
                LegendItem(
                    title = "Heatmap Mode",
                    description = "Shows congestion intensity. Red = crowded, blue = clear."
                )
                LegendItem(
                    title = "History Mode",
                    description = "Shows signal strength changes over time for selected network."
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Got it")
            }
        }
    )
}

@Composable
private fun LegendItem(title: String, description: String) {
    Column {
        Text(
            title,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AnalyzeScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalyzeViewModel = koinViewModel(),
    onNavigateToPowerTools: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    // Permission state
    val permissionGate: com.pulsewave.app.core.permissions.PermissionGate = org.koin.compose.koinInject()
    val permissionState by permissionGate.state.collectAsStateWithLifecycle()
    
    // Check permissions on launch
    androidx.compose.runtime.LaunchedEffect(Unit) {
        permissionGate.checkPermissions()
    }
    
    // Get auto scan settings
    val settingsRepository: SettingsRepository = koinInject()
    val autoScan by settingsRepository.autoScan.collectAsState(initial = false)
    val scanInterval by settingsRepository.scanInterval.collectAsState(initial = 10000L)

    // Get the throttle manager directly for real-time state in auto-scan loop
    val throttleManager: com.pulsewave.app.core.wifi.ScanThrottleManager = org.koin.compose.koinInject()
    val throttleState by throttleManager.throttleState.collectAsStateWithLifecycle()

    // Respect AutoScan setting - but be throttle-aware!
    LaunchedEffect(permissionState.wifiScan, autoScan, throttleState) {
        if (permissionState.wifiScan == com.pulsewave.app.core.permissions.PermissionStatus.GRANTED) {
            // Only scan if not already exhausted
            if (throttleState !is ThrottleState.Exhausted) {
                viewModel.startScan()
            }
            
            // Only continue scanning if autoScan is enabled
            if (autoScan) {
                while (true) {
                    // Re-read current throttle state each iteration
                    val currentState = throttleManager.throttleState.value
                    
                    when (currentState) {
                        is ThrottleState.Exhausted -> {
                            // Wait for cooldown + buffer
                            val waitTime = (currentState.secondsUntilNextScan + 5) * 1000L
                            kotlinx.coroutines.delay(waitTime)
                        }
                        is ThrottleState.Ready, is ThrottleState.LowQuota, is ThrottleState.Unlimited -> {
                            kotlinx.coroutines.delay(scanInterval)
                            // Double-check before scanning
                            if (throttleManager.throttleState.value !is ThrottleState.Exhausted) {
                                viewModel.startScan()
                            }
                        }
                        is ThrottleState.Unknown -> {
                            kotlinx.coroutines.delay(scanInterval)
                            viewModel.startScan()
                        }
                    }
                }
            }
        }
    }
    
    // Permission launcher
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        permissionGate.checkPermissions()
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        val needsPermission = permissionState.wifiScan != com.pulsewave.app.core.permissions.PermissionStatus.GRANTED ||
            permissionState.nearbyDevices != com.pulsewave.app.core.permissions.PermissionStatus.GRANTED
        
        if (needsPermission) {
            PermissionRequestContent(
                onRequestPermission = {
                    val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.NEARBY_WIFI_DEVICES
                        )
                    } else {
                        arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                    permissionLauncher.launch(permissions)
                },
                onOpenSettings = { permissionGate.openAppSettings() }
            )
        } else {
            AnimatedContent(
                targetState = uiState,
                transitionSpec = {
                    when {
                        // Loading -> Success: crossfade with slide up
                        initialState is AnalyzeUiState.Loading && targetState is AnalyzeUiState.Success -> {
                            (fadeIn(tween(400)) + slideInVertically { it / 8 }) togetherWith 
                            (fadeOut(tween(200)))
                        }
                        else -> fadeIn(tween(300)) togetherWith fadeOut(tween(200))
                    }
                },
                label = "analyzeStateTransition"
            ) { state ->
                when (state) {
                    is AnalyzeUiState.Loading -> {
                        Column(
                            modifier = Modifier.padding(16.dp).fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            ShimmerPlaceholder(
                                visible = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(0.4f),
                                shape = PulseWaveShapes.medium
                            )
                            
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                ShimmerPlaceholder(visible = true, modifier = Modifier.width(100.dp).height(20.dp))
                                ShimmerPlaceholder(visible = true, modifier = Modifier.width(60.dp).height(20.dp))
                            }
                            
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(0.6f)
                            ) {
                                repeat(5) {
                                    WifiNetworkSkeleton()
                                }
                            }
                        }
                    }
                    is AnalyzeUiState.Empty -> {
                        EmptyStateContent(
                            onScan = { viewModel.startScan() },
                            throttleState = state.throttleState,
                            onNavigateToPowerTools = onNavigateToPowerTools
                        )
                    }
                    is AnalyzeUiState.Success -> {
                        WifiAnalyzerContent(
                            networks = state.networks,
                            history = state.history,
                            isScanning = state.isScanning,
                            throttleState = state.throttleState,
                            onScan = { viewModel.startScan() },
                            onNavigateToPowerTools = onNavigateToPowerTools
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestContent(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    // Animated background gradient
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f * gradientOffset),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f * (1f - gradientOffset))
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Animated WiFi waves
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 0.8f,
                targetValue = 1.2f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1500, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "pulseScale"
            )
            
            Box(contentAlignment = Alignment.Center) {
                // Outer pulse ring
                Surface(
                    modifier = Modifier
                        .size(160.dp)
                        .scale(pulseScale)
                        .graphicsLayer { alpha = 2f - pulseScale },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {}
                
                // Middle ring
                Surface(
                    modifier = Modifier.size(120.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {}
                
                // Center icon
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(48.dp))
            
            // Hero headline
            Text(
                "See Your\nInvisible World",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = MaterialTheme.typography.displaySmall.lineHeight
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Discover and analyze WiFi networks around you with beautiful visualizations",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Spacer(Modifier.height(48.dp))
            
            // Feature hints
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FeatureHint(Icons.Outlined.BarChart, "Channel\nAnalysis")
                FeatureHint(Icons.Outlined.Speed, "Signal\nStrength")
                FeatureHint(Icons.Outlined.Security, "Security\nInfo")
            }
            
            Spacer(Modifier.height(48.dp))
            
            // CTA Button
            Button(
                onClick = onRequestPermission,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Get Started", style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(Modifier.height(12.dp))
            
            TextButton(onClick = onOpenSettings) {
                Text("Already denied? Open Settings", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun FeatureHint(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun EmptyStateContent(
    onScan: () -> Unit,
    throttleState: ThrottleState = ThrottleState.Unknown,
    onNavigateToPowerTools: () -> Unit = {},
    wifiEnabled: Boolean = true,
    locationEnabled: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty")
    val haptics = com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback()
    
    val isThrottled = throttleState is ThrottleState.Exhausted
    val isLowQuota = throttleState is ThrottleState.LowQuota
    val isUnlimited = throttleState is ThrottleState.Unlimited
    
    // Multiple expanding rings for radar effect
    val ring1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing)
        ),
        label = "ring1"
    )
    val ring2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing, delayMillis = 500)
        ),
        label = "ring2"
    )
    val ring3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearOutSlowInEasing, delayMillis = 1000)
        ),
        label = "ring3"
    )
    
    val (title, subtitle, actionText, icon) = when {
        !wifiEnabled -> arrayOf(
            "Wi-Fi is Off",
            "Turn on Wi-Fi to scan for networks",
            "Open Wi-Fi Settings",
            Icons.Outlined.WifiOff
        )
        !locationEnabled -> arrayOf(
            "Location is Off",
            "Location services are required to scan Wi-Fi networks",
            "Open Location Settings",
            Icons.Outlined.LocationOff
        )
        isThrottled -> {
            val state = throttleState as ThrottleState.Exhausted
            val mins = state.secondsUntilNextScan / 60
            val secs = state.secondsUntilNextScan % 60
            arrayOf(
                "Scan Cooldown",
                "Android limits Wi-Fi scans to 4 per 2 minutes. Wait $mins:${"%02d".format(secs)} or disable throttling in Developer Options.",
                "Scan Anyway",
                Icons.Outlined.Schedule
            )
        }
        isLowQuota -> {
            val state = throttleState as ThrottleState.LowQuota
            arrayOf(
                "${state.scansRemaining} Scans Left",
                "You have ${state.scansRemaining} scans remaining in this 2-minute window.",
                "Scan Now",
                Icons.Outlined.History
            )
        }
        isUnlimited -> arrayOf(
            "Unlimited Scans",
            "Developer Mode detected! Scan throttling is disabled.",
            "Start Scanning",
            Icons.Outlined.Bolt
        )
        else -> arrayOf(
            "Looking for Signals",
            "Tap scan to discover the WiFi landscape around you",
            "Start Scanning",
            Icons.Outlined.WifiFind
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Use ScanRadarIndicator when throttled or low quota
        if (isThrottled || isLowQuota || isUnlimited) {
            ScanRadarIndicator(
                throttleState = throttleState,
                onScanNow = onScan,
                size = 180.dp
            )
        } else {
            // Animated radar with expanding rings
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(160.dp)
            ) {
                // Expanding rings (only show for normal state)
                if (wifiEnabled && locationEnabled) {
                    listOf(ring1, ring2, ring3).forEach { progress ->
                        Surface(
                            modifier = Modifier
                                .size((60 + 100 * progress).dp)
                                .graphicsLayer { alpha = 1f - progress },
                            shape = CircleShape,
                            color = Color.Transparent,
                            border = androidx.compose.foundation.BorderStroke(
                                2.dp,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f * (1f - progress))
                            )
                        ) {}
                    }
                }
                
                // Center icon
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = CircleShape,
                    color = when {
                        !wifiEnabled || !locationEnabled -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceContainerHighest
                    }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            icon as androidx.compose.ui.graphics.vector.ImageVector,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = when {
                                !wifiEnabled || !locationEnabled -> MaterialTheme.colorScheme.onErrorContainer
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            title as String,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            subtitle as String,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(32.dp))
        
        // Don't show scan button if exhausted (ScanRadarIndicator handles it)
        if (!isThrottled) {
            Button(
                onClick = onScan,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp),
                shape = RoundedCornerShape(26.dp)
            ) {
                Icon(Icons.Default.Radar, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(actionText as String, style = MaterialTheme.typography.titleSmall)
            }
        }
        
        // Learn more about throttling - link to PowerTools
        if (isThrottled || isLowQuota) {
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = {
                haptics.light()
                onNavigateToPowerTools()
            }) {
                Icon(Icons.Outlined.Build, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Learn how to disable throttling")
            }
        }
        
        // Show unlimited badge info
        if (isUnlimited) {
            Spacer(Modifier.height(16.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Text(
                    "  Dev Mode Active  ",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WifiAnalyzerContent(
    networks: List<WifiNetwork>,
    history: Map<String, List<Int>>,
    isScanning: Boolean,
    throttleState: ThrottleState = ThrottleState.Unknown,
    onScan: () -> Unit,
    onNavigateToPowerTools: () -> Unit = {}
) {
    var selectedBand by remember { mutableStateOf(FrequencyBand.BAND_2_4) }
    var selectedNetwork by remember { mutableStateOf<WifiNetwork?>(null) }
    var showHistoryChart by remember { mutableStateOf(false) }
    var showHeatmap by remember { mutableStateOf(false) }
    var showTrafficSheet by remember { mutableStateOf(false) }
    var showLegendDialog by remember { mutableStateOf(false) }
    var showThrottleWarning by remember { mutableStateOf(false) }
    val haptics = com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback()
    
    // Show throttle warning when low or exhausted
    val showThrottleBanner = throttleState is ThrottleState.LowQuota || throttleState is ThrottleState.Exhausted
    
    if (showLegendDialog) {
        GraphLegendDialog(onDismiss = { showLegendDialog = false })
    }
    
    // Throttle warning dialog
    if (showThrottleWarning) {
        AlertDialog(
            onDismissRequest = { showThrottleWarning = false },
            title = { Text("Scan Throttling") },
            text = {
                Column {
                    Text("Android limits Wi-Fi scans to 4 per 2 minutes for privacy and battery reasons.")
                    Spacer(Modifier.height(16.dp))
                    Text("When you run out of scans:", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))
                    Text("• Wait for the cooldown timer")
                    Text("• Or disable throttling in Developer Options")
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Text(
                            "Developer Options → Networking →\nWi-Fi scan throttling → OFF",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showThrottleWarning = false
                    onNavigateToPowerTools()
                }) {
                    Text("Open Power Tools")
                }
            },
            dismissButton = {
                TextButton(onClick = { showThrottleWarning = false }) {
                    Text("Got it")
                }
            }
        )
    }
    
    val bestNetwork = remember(networks) { networks.maxByOrNull { it.rssi } }
    
    // Filter out "my" network (best signal) from traffic report
    val congestionNetworks = remember(networks, bestNetwork) { 
        if (bestNetwork != null) networks - bestNetwork else networks 
    }
    
    val congestionScores = remember(congestionNetworks, selectedBand) {
        CongestionAnalyzer.analyze(congestionNetworks, selectedBand)
    }
    
    // Professional Export Logic
    val exportSummary = remember(networks) {
        val topNetworks = networks.sortedByDescending { it.rssi }.take(5)
        buildString {
            appendLine("WiFi Environment Report")
            appendLine("-----------------------")
            appendLine("Total Networks: ${networks.size}")
            appendLine("Best Signal: ${topNetworks.firstOrNull()?.ssid ?: "N/A"}")
            appendLine("\nTop 5 Networks:")
            topNetworks.forEach { 
                appendLine("- ${it.ssid} (${it.rssi} dBm) on Ch ${frequencyToChannel(it.frequency)}")
            }
        }
    }

    // Filter networks by band
    val filteredNetworks = remember(networks, selectedBand) {
        when (selectedBand) {
            FrequencyBand.BAND_2_4 -> networks.filter { it.frequency in 2400..2500 }
            FrequencyBand.BAND_5 -> networks.filter { it.frequency in 5150..5900 }
            FrequencyBand.BAND_6 -> networks.filter { it.frequency in 5925..7125 }
        }
    }

    Scaffold(
        // FAB removed per user request
    ) { innerPadding ->
        // Consume innerPadding here to prevent double padding, but pass it down if needed
        Column(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
                .fillMaxSize()
        ) {
            // Throttle Status Banner (shows when low quota or exhausted)
            if (showThrottleBanner) {
                ThrottleStatusBanner(
                    throttleState = throttleState,
                    onLearnMore = {
                        haptics.light()
                        showThrottleWarning = true
                    },
                    onScan = onScan,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            
            BoxWithConstraints(
                modifier = Modifier.weight(1f).fillMaxSize()
            ) {
                val isWideScreen = maxWidth > 600.dp
                val contentBottomPadding = innerPadding.calculateBottomPadding() + 80.dp // FAB space
            
            if (isWideScreen) {
                // Multi-pane Layout
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left Pane: Network List
                    Box(modifier = Modifier.weight(0.4f).fillMaxHeight()) {
                        NetworkList(
                            networks = filteredNetworks,
                            bestNetwork = bestNetwork,
                            selectedNetwork = selectedNetwork,
                            onNetworkSelected = { network ->
                                haptics.click()
                                selectedNetwork = if (selectedNetwork == network) null else network
                            },
                            onClearSelection = {
                                haptics.light()
                                selectedNetwork = null
                            },
                            contentPadding = PaddingValues(bottom = contentBottomPadding)
                        )
                    }
                    
                    VerticalDivider(modifier = Modifier.fillMaxHeight(), color = MaterialTheme.colorScheme.outlineVariant)
                    
                    // Right Pane: Charts & Details
                    Column(modifier = Modifier.weight(0.6f).fillMaxHeight()) {
                        ChartSection(
                            networks = networks,
                            filteredNetworks = filteredNetworks,
                            history = history,
                            selectedBand = selectedBand,
                            selectedNetwork = selectedNetwork,
                            showHistoryChart = showHistoryChart,
                            showHeatmap = showHeatmap,
                            onBandChanged = { band ->
                                haptics.light()
                                selectedBand = band
                            },
                            onChartTypeToggle = { isHistory ->
                                haptics.light()
                                showHistoryChart = isHistory
                            },
                            onToggleHeatmap = { 
                                haptics.light()
                                showHeatmap = it 
                            },
                            onNetworkSelected = { network ->
                                haptics.click()
                                selectedNetwork = network
                            },
                            onShowLegend = {
                                haptics.light()
                                showLegendDialog = true
                            }
                        )
                    }
                }
            } else {
                // Mobile Portrait Layout - Immersive Chart + Bottom Sheet
                val sheetState = rememberBottomSheetScaffoldState()
                
                BottomSheetScaffold(
                    scaffoldState = sheetState,
                    sheetPeekHeight = 180.dp,
                    sheetDragHandle = { BottomSheetDefaults.DragHandle() },
                    sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    sheetContent = {
                        // Network List in Bottom Sheet
                        NetworkList(
                            networks = filteredNetworks,
                            bestNetwork = bestNetwork,
                            selectedNetwork = selectedNetwork,
                            onNetworkSelected = { network ->
                                haptics.click()
                                selectedNetwork = if (selectedNetwork == network) null else network
                            },
                            onClearSelection = {
                                haptics.light()
                                selectedNetwork = null
                            },
                            contentPadding = PaddingValues(bottom = contentBottomPadding),
                            modifier = Modifier.fillMaxSize() // Allow list to fill sheet content naturally
                        )
                    }
                ) { sheetPadding ->
                    // Immersive Chart Content
                    Column(
                        modifier = Modifier
                            .padding(sheetPadding)
                            .padding(bottom = 80.dp) // Bottom padding for sheet handle visibility
                            .fillMaxSize()
                    ) {
                        // Floating Controls (Toolbar)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Connected Band Selector
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FrequencyBand.entries.forEachIndexed { index, band ->
                                    ToggleButton(
                                        checked = selectedBand == band,
                                        onCheckedChange = { 
                                            haptics.light()
                                            selectedBand = band 
                                        },
                                        shapes = when (index) {
                                            0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                            FrequencyBand.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                            else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                        },
                                        modifier = Modifier.height(40.dp)
                                    ) {
                                        Text(
                                            band.label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (selectedBand == band) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                }
                                
                                Spacer(Modifier.width(12.dp))
                                
                                // Chart toggle icons
                                FilledTonalIconToggleButton(
                                    checked = !showHistoryChart,
                                    onCheckedChange = { 
                                        haptics.light()
                                        showHistoryChart = false 
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.BarChart, "Channels", Modifier.size(20.dp))
                                }
                                FilledTonalIconToggleButton(
                                    checked = showHistoryChart,
                                    onCheckedChange = { 
                                        haptics.light()
                                        showHistoryChart = true 
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.Default.Timeline, "History", Modifier.size(20.dp))
                                }
                                
                                Spacer(Modifier.width(4.dp))
                                
                                // Help/Legend button
                                IconButton(
                                    onClick = { 
                                        haptics.light()
                                        showLegendDialog = true 
                                    },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Outlined.HelpOutline, "Help", Modifier.size(20.dp))
                                }
                            }
                        }
                        
                        // Insight Strip
                        InsightStrip(
                            networks = networks,
                            selectedBand = selectedBand,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                        
                        // Progress indicator
                        AnimatedVisibility(
                            visible = isScanning,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().height(2.dp))
                        }

                        // Main Chart Area
                        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                            if (showHistoryChart) {
                                SignalHistoryGraph(
                                    history = history.filterKeys { bssid ->
                                        val network = networks.find { it.bssid == bssid }
                                        network != null && ( 
                                            (selectedBand == FrequencyBand.BAND_2_4 && network.frequency in 2400..2500) ||
                                            (selectedBand == FrequencyBand.BAND_5 && network.frequency in 5150..5900) ||
                                            (selectedBand == FrequencyBand.BAND_6 && network.frequency in 5925..7125))
                                    },
                                    networks = filteredNetworks,
                                    onNetworkSelected = { network ->
                                        haptics.click()
                                        selectedNetwork = network
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                ChannelGraph(
                                    networks = filteredNetworks,
                                    selectedBand = selectedBand,
                                    selectedNetwork = selectedNetwork,
                                    showHeatmap = showHeatmap,
                                    onNetworkSelected = { network ->
                                        haptics.click()
                                        selectedNetwork = network
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }
        }
        }
    }

    if (showTrafficSheet) {
        ModalBottomSheet(
            onDismissRequest = { showTrafficSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            CongestionInsightSheet(scores = congestionScores)
        }
    }
}

@Composable
private fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NetworkList(
    networks: List<WifiNetwork>,
    bestNetwork: WifiNetwork?,
    selectedNetwork: WifiNetwork?,
    onNetworkSelected: (WifiNetwork) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    // Channel filter state
    var selectedChannel by remember { mutableStateOf<Int?>(null) }
    
    // Get unique channels with network counts
    val channelGroups = remember(networks) {
        networks.groupBy { frequencyToChannel(it.frequency) }
            .mapValues { it.value.size }
            .toList()
            .sortedBy { it.first }
    }
    
    // Filter networks by selected channel
    val displayNetworks = remember(networks, selectedChannel) {
        if (selectedChannel == null) {
            networks.sortedByDescending { it.rssi }
        } else {
            networks.filter { frequencyToChannel(it.frequency) == selectedChannel }
                .sortedByDescending { it.rssi }
        }
    }
    
    LazyColumn(
        contentPadding = contentPadding,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        // Header with network count
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${displayNetworks.size} Networks" + if (selectedChannel != null) " on Ch $selectedChannel" else " Nearby",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                AnimatedVisibility(visible = selectedNetwork != null || selectedChannel != null) {
                    TextButton(
                        onClick = { 
                            onClearSelection()
                            selectedChannel = null
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        
        // Channel filter chips - only show if we have more than 1 channel
        if (channelGroups.size > 1) {
            item {
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // "All" chip
                    FilterChip(
                        selected = selectedChannel == null,
                        onClick = { selectedChannel = null },
                        label = { Text("All", style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.height(28.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    
                    // Channel chips
                    channelGroups.forEach { (channel, count) ->
                        FilterChip(
                            selected = selectedChannel == channel,
                            onClick = { 
                                selectedChannel = if (selectedChannel == channel) null else channel
                            },
                            label = { 
                                Text("Ch $channel ($count)", style = MaterialTheme.typography.labelSmall)
                            },
                            modifier = Modifier.height(28.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        )
                    }
                }
            }
        }
        
        // Network cards
        items(displayNetworks.size, key = { displayNetworks[it].bssid }) { index ->
            val network = displayNetworks[index]
            
            // Custom shape for first/last items
            val shape = if (displayNetworks.size == 1) {
                RoundedCornerShape(16.dp)
            } else when (index) {
                0 -> RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                displayNetworks.size - 1 -> RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                else -> RoundedCornerShape(4.dp)
            }
            
            WifiNetworkCard(
                network = network,
                isSelected = selectedNetwork == network,
                onSelect = { onNetworkSelected(network) },
                shape = shape,
                modifier = Modifier.animateItem().padding(horizontal = 16.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ChartSection(
    networks: List<WifiNetwork>,
    filteredNetworks: List<WifiNetwork>,
    history: Map<String, List<Int>>,
    selectedBand: FrequencyBand,
    selectedNetwork: WifiNetwork?,
    showHistoryChart: Boolean,
    showHeatmap: Boolean,
    onBandChanged: (FrequencyBand) -> Unit,
    onChartTypeToggle: (Boolean) -> Unit,
    onToggleHeatmap: (Boolean) -> Unit,
    onNetworkSelected: (WifiNetwork?) -> Unit,
    onShowLegend: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Multi-row Chips Layout
        FlowRow(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Band Chips
            FrequencyBand.entries.forEach { band ->
                FilterChip(
                    selected = selectedBand == band,
                    onClick = { onBandChanged(band) },
                    label = { Text(band.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
            
            // View Toggle Chips
            FilterChip(
                selected = !showHistoryChart,
                onClick = { onChartTypeToggle(false) },
                label = { Text("Channels") },
                leadingIcon = { Icon(Icons.Default.BarChart, null, Modifier.size(16.dp)) }
            )
            FilterChip(
                selected = showHistoryChart,
                onClick = { onChartTypeToggle(true) },
                label = { Text("History") },
                leadingIcon = { Icon(Icons.Default.Timeline, null, Modifier.size(16.dp)) }
            )
            
            // Heatmap Toggle
            if (!showHistoryChart) {
                FilterChip(
                    selected = showHeatmap,
                    onClick = { onToggleHeatmap(!showHeatmap) },
                    label = { Text("Heatmap") },
                    leadingIcon = { 
                        Icon(
                            if(showHeatmap) Icons.Default.Visibility else Icons.Default.VisibilityOff, 
                            null, 
                            Modifier.size(16.dp)
                        ) 
                    }
                )
            }
            
            // Help/Legend button
            AssistChip(
                onClick = onShowLegend,
                label = { Text("Help") },
                leadingIcon = { Icon(Icons.AutoMirrored.Outlined.HelpOutline, null, Modifier.size(16.dp)) }
            )
        }
        
        // Insight Strip for wide screen
        InsightStrip(
            networks = networks,
            selectedBand = selectedBand,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )
        
        // Main chart
        Box(modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 8.dp)) {
            if (showHistoryChart) {
                SignalHistoryGraph(
                    history = history.filterKeys { bssid ->
                        val network = networks.find { it.bssid == bssid }
                        network != null && ( 
                            (selectedBand == FrequencyBand.BAND_2_4 && network.frequency in 2400..2500) ||
                            (selectedBand == FrequencyBand.BAND_5 && network.frequency in 5150..5900) ||
                            (selectedBand == FrequencyBand.BAND_6 && network.frequency in 5925..7125))
                    },
                    networks = filteredNetworks,
                    onNetworkSelected = onNetworkSelected
                )
            } else {
                ChannelGraph(
                    networks = filteredNetworks,
                    selectedBand = selectedBand,
                    selectedNetwork = selectedNetwork,
                    showHeatmap = showHeatmap,
                    onNetworkSelected = onNetworkSelected,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun NetworkList(
    networks: List<WifiNetwork>,
    selectedNetwork: WifiNetwork?,
    onNetworkSelected: (WifiNetwork) -> Unit,
    onClearSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        // Header with network count
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${networks.size} Networks",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                AnimatedVisibility(visible = selectedNetwork != null) {
                    TextButton(
                        onClick = onClearSelection,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Clear", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
        
        items(networks.sortedByDescending { it.rssi }, key = { it.bssid }) { network ->
            WifiNetworkCard(
                network = network,
                isSelected = selectedNetwork == network,
                onSelect = { onNetworkSelected(network) },
                modifier = Modifier.animateItem()
            )
        }
    }
}

@Composable
fun WifiNetworkCard(
    network: WifiNetwork,
    isSelected: Boolean = false,
    onSelect: () -> Unit = {},
    shape: androidx.compose.ui.graphics.Shape = PulseWaveShapes.small,
    modifier: Modifier = Modifier
) {
    val signalColor = when {
        network.rssi > -50 -> Color(0xFF4CAF50)
        network.rssi > -60 -> Color(0xFF8BC34A)
        network.rssi > -70 -> Color(0xFFFFC107)
        else -> Color(0xFFFF5722)
    }
    
    // Distance Calc
    val distance = remember(network.rssi, network.frequency) {
        val mhz = network.frequency.toDouble()
        val dbm = network.rssi.toDouble()
        val exp = (27.55 - (20 * kotlin.math.log10(mhz)) + kotlin.math.abs(dbm)) / 20.0
        10.0.pow(exp)
    }
    
    // Signal strength as percentage (0-100)
    val signalPercent = ((network.rssi + 100) / 60f * 100).coerceIn(0f, 100f)
    
    // Selection animation
    val cardScale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )
    
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primaryContainer 
        else 
            MaterialTheme.colorScheme.surfaceContainerLow,
        label = "cardColor"
    )
    
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) 
            MaterialTheme.colorScheme.primary 
        else 
            Color.Transparent,
        label = "borderColor"
    )

    Card(
        onClick = onSelect,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSelected) CardDefaults.outlinedCardBorder().copy(
            brush = Brush.linearGradient(listOf(borderColor, borderColor))
        ) else null,
        modifier = modifier
            .fillMaxWidth()
            .scale(cardScale)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Signal strength indicator (visual bars)
            SignalStrengthBars(
                rssi = network.rssi,
                color = signalColor,
                modifier = Modifier.padding(end = 12.dp)
            )
            
            // Network info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = network.ssid.ifEmpty { "Hidden Network" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val vendor = remember(network.bssid) { getVendorName(network.bssid) }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = (if (vendor != null) "$vendor • " else "") + "Ch ${frequencyToChannel(network.frequency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Security Info
                    val isSecured = network.security.isNotEmpty() && !network.security.contains("Open", ignoreCase = true)
                    if (isSecured) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Secured",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PulseWaveAlpha.strong)
                        )
                        // Show simplified security protocol (e.g. WPA3)
                        val securityLabel = network.security
                            .replace("WPA2", "WPA2")
                            .replace("WPA3", "WPA3")
                            .replace("ESS", "")
                            .replace("WPS", "")
                            .trim()
                            .split(" ")
                            .firstOrNull() ?: ""
                            
                        if (securityLabel.isNotEmpty()) {
                            Text(
                                text = securityLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = PulseWaveAlpha.strong),
                                modifier = Modifier.padding(start = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Signal dBm value - Compact pill
            Surface(
                shape = PulseWaveShapes.extraSmall,
                color = signalColor.copy(alpha = PulseWaveAlpha.subtle + 0.05f)
            ) {
                Text(
                    text = "${network.rssi}",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = signalColor
                )
            }
        }
    }
}

@Composable
private fun SignalStrengthBars(
    rssi: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    val bars = when {
        rssi > -50 -> 4
        rssi > -60 -> 3
        rssi > -70 -> 2
        else -> 1
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(4) { index ->
            val height = (8 + index * 4).dp
            val isActive = index < bars
            
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(height)
                    .clip(RoundedCornerShape(2.dp))
                    .background(
                        if (isActive) color 
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
            )
        }
    }
}

/**
 * Compact banner showing scan quota status with countdown and learn more link.
 */
@Composable
private fun ThrottleStatusBanner(
    throttleState: ThrottleState,
    onLearnMore: () -> Unit,
    onScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stateColor = when (throttleState) {
        is ThrottleState.LowQuota -> MaterialTheme.colorScheme.tertiary
        is ThrottleState.Exhausted -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.outline
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "banner")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = stateColor.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status with dots
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (throttleState) {
                    is ThrottleState.LowQuota -> {
                        // Quota dots
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            repeat(4) { i ->
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (i < throttleState.scansRemaining) stateColor
                                            else stateColor.copy(alpha = 0.2f)
                                        )
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "${throttleState.scansRemaining}/4 scans left",
                            style = MaterialTheme.typography.labelMedium,
                            color = stateColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    is ThrottleState.Exhausted -> {
                        // Countdown
                        val mins = throttleState.secondsUntilNextScan / 60
                        val secs = throttleState.secondsUntilNextScan % 60
                        Text(
                            "⏱ $mins:${"%02d".format(secs)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = stateColor.copy(alpha = pulseAlpha),
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "until next scan",
                            style = MaterialTheme.typography.labelMedium,
                            color = stateColor
                        )
                    }
                    else -> {}
                }
            }
            
            // Action buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = onLearnMore,
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Text("Help", style = MaterialTheme.typography.labelMedium, color = stateColor)
                }
                
                if (throttleState is ThrottleState.LowQuota) {
                    FilledTonalButton(
                        onClick = onScan,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = stateColor.copy(alpha = 0.2f),
                            contentColor = stateColor
                        )
                    ) {
                        Text("Scan", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

private fun frequencyToChannel(frequency: Int): Int {
    return when {
        frequency in 2400..2500 -> ((frequency - 2412) / 5) + 1
        frequency in 5150..5350 -> ((frequency - 5180) / 5) + 36
        frequency in 5470..5730 -> ((frequency - 5500) / 5) + 100
        frequency in 5735..5900 -> ((frequency - 5745) / 5) + 149
        frequency in 5925..7125 -> ((frequency - 5950) / 5) + 1
        else -> 0
    }
}
