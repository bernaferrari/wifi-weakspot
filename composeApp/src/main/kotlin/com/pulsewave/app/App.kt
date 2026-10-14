@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.pulsewave.app

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.navigation.navArgument
import android.net.Uri
import com.pulsewave.app.feature.tools.ToolType
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.ui.unit.IntOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.pulsewave.app.feature.setup.ScanSetupScreen
import com.pulsewave.app.feature.walk.WalkScreen
import androidx.compose.animation.*
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.pulsewave.app.feature.linktest.LinkTestScreen
import com.pulsewave.app.feature.linktest.InternetSpeedScreen
import com.pulsewave.app.feature.survey.SurveyScreen
import com.pulsewave.app.feature.tools.ToolsScreen
import org.koin.compose.koinInject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector,
    val label: String
)

val bottomNavItems = listOf(
    BottomNavItem("home", Icons.Outlined.Home, Icons.Filled.Home, "Home"),
    BottomNavItem("walk", Icons.AutoMirrored.Outlined.DirectionsWalk, Icons.AutoMirrored.Filled.DirectionsWalk, "Walk"),
    BottomNavItem("wifi", Icons.Outlined.Radar, Icons.Filled.Radar, "Networks"),
    BottomNavItem("tools", Icons.Outlined.Handyman, Icons.Filled.Handyman, "Tools")
)

@Composable
fun App() {
    val settingsRepository: SettingsRepository = koinInject()
    val reduceMotion by settingsRepository.reduceMotion.collectAsState(initial = false)
    val hapticsEnabled by settingsRepository.hapticsEnabled.collectAsState(initial = true)
    val permissionGate: PermissionGate = koinInject()
    val permissionState by permissionGate.state.collectAsState()
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, permissionGate) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) permissionGate.checkPermissions()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        permissionGate.checkPermissions()
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val requestScanPermission = com.pulsewave.app.core.permissions.rememberScanPermissionRequest()
    PulseWaveTheme(reduceMotion = reduceMotion || com.pulsewave.app.core.designsystem.rememberSystemMotionReduced()) {
        CompositionLocalProvider(com.pulsewave.app.core.designsystem.LocalHapticsEnabled provides hapticsEnabled) {
            // Null while preferences load, so returning users never see the welcome flash by.
            val onboardingDone by remember(settingsRepository) {
                settingsRepository.onboardingDone.map<Boolean, Boolean?> { it }
            }.collectAsState(initial = null)
            val scope = rememberCoroutineScope()
            when (onboardingDone) {
                null -> Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {}
                false -> com.pulsewave.app.feature.onboarding.OnboardingScreen(onFinish = {
                    scope.launch { settingsRepository.setOnboardingDone() }
                })
                true -> MainAppContent(
                    hapticsEnabled = hapticsEnabled,
                    canScanWifi = permissionState.canScanWifi,
                    onRequestScanPermission = requestScanPermission,
                    onOpenSettings = permissionGate::openAppSettings
                )
            }
        }
    }
}

@Composable
fun PermissionRequiredScreen(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    onBack: () -> Unit = {}
) {
    Scaffold(topBar = {
        TopAppBar(title = {}, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
                .verticalScroll(androidx.compose.foundation.rememberScrollState()).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            com.pulsewave.app.core.designsystem.component.ShapeIcon(
                Icons.Outlined.WifiFind, MaterialShapes.Cookie9Sided,
                MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer,
                size = 120.dp, iconSize = 56.dp
            )
            Spacer(Modifier.height(24.dp))
            Text("Allow access to nearby Wi-Fi", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text(
                "The floor plan map needs to see your router's signal. Android asks for location access for that; PulseWave never saves where you are.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRequestPermission, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight)) {
                Text("Allow access", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
            }
            TextButton(onClick = onOpenSettings) { Text("Open app settings") }
        }
    }
}

private enum class Tab(val item: BottomNavItem) { Home(bottomNavItems[0]), Walk(bottomNavItems[1]), Networks(bottomNavItems[2]), Tools(bottomNavItems[3]) }

/**
 * Two levels: the tabbed "main" screen owns the navigation bar, and everything else is a
 * full screen pushed over it. Pushes use the Material shared-axis slide (with predictive
 * back); tab switches fade through and keep each tab's scroll position.
 */
@Composable
fun MainAppContent(
    hapticsEnabled: Boolean = true,
    canScanWifi: Boolean = false,
    onRequestScanPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val navController = rememberNavController()
    val reduceMotion = com.pulsewave.app.core.designsystem.LocalReduceMotion.current
    val slide = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val fadeInSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
    val fadeOutSpec = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
    val offset: (Int) -> Int = { (it * 0.18f).toInt() }
    val go: (String) -> Unit = { route -> navController.navigate(route) { launchSingleTop = true } }
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(
        navController = navController,
        startDestination = "main",
        modifier = Modifier.fillMaxSize(),
        enterTransition = {
            if (reduceMotion) EnterTransition.None
            else slideInHorizontally(slide) { offset(it) } + fadeIn(fadeInSpec)
        },
        exitTransition = {
            if (reduceMotion) ExitTransition.None
            else slideOutHorizontally(slide) { -offset(it) } + fadeOut(fadeOutSpec)
        },
        popEnterTransition = {
            if (reduceMotion) EnterTransition.None
            else slideInHorizontally(slide) { -offset(it) } + fadeIn(fadeInSpec)
        },
        popExitTransition = {
            if (reduceMotion) ExitTransition.None
            else slideOutHorizontally(slide) { offset(it) } + fadeOut(fadeOutSpec)
        }
    ) {
        composable("main") {
            MainTabs(
                hapticsEnabled = hapticsEnabled,
                reduceMotion = reduceMotion,
                onNavigate = go,
                onRequestScanPermission = onRequestScanPermission
            )
        }
        composable("lan") {
            LanScreen(onDeviceClick = { ip -> go("lan_device/$ip") }, onBack = back)
        }
        composable("lan_device/{ip}") { entry ->
            LanDeviceDetailScreen(
                ip = entry.arguments?.getString("ip").orEmpty(),
                onBack = back,
                onPing = { target -> go("diagnose?target=${Uri.encode(target)}&tool=PING") },
                onPortScan = { target -> go("diagnose?target=${Uri.encode(target)}&tool=PORT_SCAN") }
            )
        }
        composable(
            "diagnose?target={target}&tool={tool}",
            arguments = listOf(navArgument("target") { defaultValue = "" }, navArgument("tool") { defaultValue = "" })
        ) { entry ->
            ToolsScreen(
                initialTarget = entry.arguments?.getString("target").orEmpty(),
                initialTool = entry.arguments?.getString("tool")?.let { name -> ToolType.entries.firstOrNull { it.name == name } },
                onBack = back
            )
        }
        composable("speedtest") {
            LinkTestScreen(onBack = back, onOpenInternetTest = { go("internet_speed") })
        }
        composable("internet_speed") { InternetSpeedScreen(onBack = back) }
        composable("history") { HistoryScreen(onBack = back) }
        composable("survey") {
            if (canScanWifi) SurveyScreen(onBack = back)
            else PermissionRequiredScreen(onRequestScanPermission, onOpenSettings, back)
        }
        composable("settings") {
            SettingsScreen(
                onNavigateToScanSetup = { go("scan_setup") },
                onNavigateToAbout = { go("about") },
                onRequestScanPermission = onRequestScanPermission,
                onBack = back
            )
        }
        composable("about") { com.pulsewave.app.feature.settings.AboutScreen(onBack = back) }
        composable("scan_setup") { ScanSetupScreen(onBack = back) }
    }
}

@Composable
private fun MainTabs(
    hapticsEnabled: Boolean,
    reduceMotion: Boolean,
    onNavigate: (String) -> Unit,
    onRequestScanPermission: () -> Unit
) {
    var tab by rememberSaveable { mutableStateOf(Tab.Home) }
    val haptics = rememberPulseWaveHapticFeedback()
    val tabStates = androidx.compose.runtime.saveable.rememberSaveableStateHolder()
    val select: (Tab) -> Unit = { target ->
        if (target != tab) {
            if (hapticsEnabled) haptics.click()
            tab = target
        }
    }
    // Back from any tab returns Home before leaving the app, like other Material apps.
    androidx.activity.compose.BackHandler(enabled = tab != Tab.Home) { tab = Tab.Home }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            BoxWithConstraints {
                ShortNavigationBar(
                    arrangement = if (maxWidth >= 600.dp) ShortNavigationBarArrangement.Centered
                    else ShortNavigationBarArrangement.EqualWeight
                ) {
                    Tab.entries.forEach { entry ->
                        val selected = tab == entry
                        ShortNavigationBarItem(
                            selected = selected,
                            onClick = { select(entry) },
                            icon = { Icon(if (selected) entry.item.selectedIcon else entry.item.icon, contentDescription = null) },
                            label = { Text(entry.item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        val fadeIn = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()
        val fadeOut = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
        val scaleIn = MaterialTheme.motionScheme.defaultSpatialSpec<Float>()
        AnimatedContent(
            targetState = tab,
            transitionSpec = {
                if (reduceMotion) EnterTransition.None togetherWith ExitTransition.None
                // Fade through: the old tab fades out quickly, the new one fades and settles in.
                else (fadeIn(fadeIn) + scaleIn(scaleIn, initialScale = 0.97f)) togetherWith fadeOut(fadeOut)
            },
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding).fillMaxSize(),
            label = "tabs"
        ) { current ->
            tabStates.SaveableStateProvider(current.name) {
                when (current) {
                    Tab.Home -> OverviewScreen(
                        onNavigateToRooms = { select(Tab.Walk) },
                        onNavigateToWifi = { select(Tab.Networks) },
                        onNavigateToLan = { onNavigate("lan") },
                        onNavigateToSpeedTest = { onNavigate("speedtest") },
                        onNavigateToHistory = { onNavigate("history") },
                        onNavigateToSettings = { onNavigate("settings") },
                        onNavigateToScanSetup = { onNavigate("scan_setup") },
                        onRequestScanPermission = onRequestScanPermission
                    )
                    Tab.Walk -> WalkScreen(onOpenFloorPlan = { onNavigate("survey") })
                    Tab.Networks -> AnalyzeScreen(onNavigateToPowerTools = { onNavigate("scan_setup") })
                    Tab.Tools -> ToolsScreen(
                        onOpenSpeedTest = { onNavigate("speedtest") },
                        onOpenDevices = { onNavigate("lan") },
                        onOpenHistory = { onNavigate("history") }
                    )
                }
            }
        }
    }
}
