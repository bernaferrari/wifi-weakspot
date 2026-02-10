package com.pulsewave.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.pulsewave.app.core.datastore.SettingsRepository
import com.pulsewave.app.core.designsystem.PulseWaveAlpha
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.PulseWaveTheme
import com.pulsewave.app.core.designsystem.component.WifiNetworkIllustration
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import com.pulsewave.app.core.permissions.PermissionGate
import com.pulsewave.app.core.permissions.PermissionStatus
import com.pulsewave.app.feature.analyze.AnalyzeScreen
import com.pulsewave.app.feature.history.HistoryScreen
import com.pulsewave.app.feature.lan.LanScreen
import com.pulsewave.app.feature.lan.LanDeviceDetailScreen
import com.pulsewave.app.feature.overview.OverviewScreen
import com.pulsewave.app.feature.settings.SettingsScreen
import com.pulsewave.app.feature.settings.PowerToolsScreen
import com.pulsewave.app.feature.speedtest.SpeedTestScreen
import com.pulsewave.app.feature.tools.ToolsScreen
import org.koin.compose.koinInject

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem("home", Icons.Outlined.Home, Icons.Filled.Home, "Home"),
    BottomNavItem("wifi", Icons.Outlined.Wifi, Icons.Filled.Wifi, "Analyze"),
    BottomNavItem("tools", Icons.Outlined.Build, Icons.Filled.Build, "Tools"),
    BottomNavItem("settings", Icons.Outlined.Settings, Icons.Filled.Settings, "Settings")
)

@Composable
fun App() {
    val settingsRepository: SettingsRepository = koinInject()
    val reduceMotion by settingsRepository.reduceMotion.collectAsState(initial = false)
    val hapticsEnabled by settingsRepository.hapticsEnabled.collectAsState(initial = true)
    
    PulseWaveTheme {
        val permissionGate: PermissionGate = koinInject()
        val permissionState by permissionGate.state.collectAsState()
        
        LaunchedEffect(Unit) {
            permissionGate.checkPermissions()
        }
        
        val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
            contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
        ) { _ ->
            permissionGate.checkPermissions()
        }
        
        val needsPermission = permissionState.wifiScan != PermissionStatus.GRANTED &&
            permissionState.nearbyDevices != PermissionStatus.GRANTED &&
            permissionState.location != PermissionStatus.GRANTED
        
        if (needsPermission) {
            PermissionRequiredScreen(
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
                onOpenSettings = { permissionGate.openAppSettings() },
                reduceMotion = reduceMotion
            )
        } else {
            MainAppContent(
                reduceMotion = reduceMotion,
                hapticsEnabled = hapticsEnabled
            )
        }
    }
}

@Composable
fun PermissionRequiredScreen(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    reduceMotion: Boolean = false
) {
    val haptics = rememberPulseWaveHapticFeedback()
    
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = if (reduceMotion) tween(0) else tween(4000, easing = LinearEasing),
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
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = PulseWaveAlpha.light * gradientOffset),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = PulseWaveAlpha.subtle * (1f - gradientOffset))
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
            Surface(
                shape = PulseWaveShapes.extraLarge,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                shadowElevation = 4.dp,
                modifier = Modifier.size(180.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    WifiNetworkIllustration(size = 140.dp)
                }
            }
            
            Spacer(Modifier.height(48.dp))
            
            Text(
                "See Your\nInvisible World",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.displaySmall.lineHeight
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Discover and analyze WiFi networks around you with beautiful visualizations",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = {
                    haptics.click()
                    onRequestPermission()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = PulseWaveShapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Get Started", style = MaterialTheme.typography.titleMedium)
            }
            
            Spacer(Modifier.height(12.dp))
            
            TextButton(onClick = {
                haptics.light()
                onOpenSettings()
            }) {
                Text("Already denied? Open Settings", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
fun MainAppContent(
    reduceMotion: Boolean = false,
    hapticsEnabled: Boolean = true
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val haptics = rememberPulseWaveHapticFeedback()

    val showBottomNav = currentRoute in listOf("home", "wifi", "tools", "settings")

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route

                        val iconScale by animateFloatAsState(
                            targetValue = if (selected) 1.1f else 1f,
                            animationSpec = if (reduceMotion) {
                                tween(0)
                            } else {
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            },
                            label = "iconScale"
                        )

                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (!selected) {
                                    if (hapticsEnabled) haptics.click()
                                    navController.navigate(item.route) {
                                        popUpTo("home") {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    if (selected) item.selectedIcon else item.icon,
                                    contentDescription = item.label,
                                    modifier = Modifier.scale(iconScale)
                                )
                            },
                            label = {
                                Text(
                                    item.label,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding).fillMaxSize()
        ) {
            composable("home") {
                OverviewScreen(
                    onNavigateToWifi = { navController.navigate("wifi") },
                    onNavigateToLan = { navController.navigate("lan") },
                    onNavigateToSpeedTest = { navController.navigate("speedtest") },
                    onNavigateToHistory = { navController.navigate("history") }
                )
            }
            
            composable("wifi") {
                AnalyzeScreen(
                    onNavigateToPowerTools = { navController.navigate("power_tools") }
                )
            }
            
            composable("lan") {
                LanScreen(
                    onDeviceClick = { ip -> navController.navigate("lan_device/$ip") },
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("lan_device/{ip}") { backStackEntry ->
                val ip = backStackEntry.arguments?.getString("ip") ?: ""
                LanDeviceDetailScreen(
                    ip = ip,
                    onBack = { navController.popBackStack() },
                    onPing = { target -> 
                        navController.navigate("tools")
                    },
                    onPortScan = { target ->
                        navController.navigate("tools")
                    }
                )
            }
            
            composable("speedtest") {
                SpeedTestScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("history") {
                HistoryScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            
            composable("tools") {
                ToolsScreen()
            }
            
            composable("settings") {
                SettingsScreen(
                    onNavigateToPowerTools = { navController.navigate("power_tools") },
                    onNavigateToAbout = { }
                )
            }
            
            composable("power_tools") {
                PowerToolsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
