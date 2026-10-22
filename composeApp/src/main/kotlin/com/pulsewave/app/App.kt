@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.pulsewave.app

import com.pulsewave.app.navigation.open
import com.pulsewave.app.navigation.pop
import com.pulsewave.app.navigation.selectTab
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.pulsewave.app.feature.linktest.LinkTestScreen
import com.pulsewave.app.feature.linktest.InternetSpeedScreen
import com.pulsewave.app.feature.survey.SurveyScreen
import com.pulsewave.app.feature.tools.ToolsScreen
import org.koin.compose.koinInject
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun App() {
    val settingsRepository: SettingsRepository = koinInject()
    val hapticsEnabled by settingsRepository.hapticsEnabled.collectAsState(initial = true)
    val signalDisplay by settingsRepository.signalDisplay.collectAsState(initial = com.pulsewave.app.core.wifi.SignalDisplay.Percentage)
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
    val requestScanPermission = com.pulsewave.app.core.platform.rememberScanPermissionRequest()
    PulseWaveTheme {
        CompositionLocalProvider(
            com.pulsewave.app.core.designsystem.LocalHapticsEnabled provides hapticsEnabled,
            com.pulsewave.app.core.designsystem.LocalSignalDisplay provides signalDisplay
        ) {
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
                "The floor plan map needs to see your router's signal. Android asks for location access for that; WiFi\u00A0Weakspot never saves where you are.",
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

/**
 * Navigation 3, the same model as ChangeDetection: tabs replace the root of the back stack and fade
 * through; everything else is a full screen pushed over them with a parallax slide and predictive
 * back. The navigation bar lives outside the transitions, so it never slides with a screen.
 */
@Composable
fun MainAppContent(
    hapticsEnabled: Boolean = true,
    canScanWifi: Boolean = false,
    onRequestScanPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val backStack = androidx.navigation3.runtime.rememberNavBackStack(
        com.pulsewave.app.navigation.pulseWaveNavConfiguration,
        com.pulsewave.app.navigation.HomeDestination
    )
    val reduceMotion = com.pulsewave.app.core.designsystem.LocalReduceMotion.current
    val push = com.pulsewave.app.navigation.rememberPushTransitionMetadata(reduceMotion)
    // Tabs are swapped, not stacked, so their models live at app scope: Networks keeps its signal
    // history and Walk its session when you hop between tabs.
    val overviewViewModel: com.pulsewave.app.feature.overview.OverviewViewModel = org.koin.compose.viewmodel.koinViewModel()
    val analyzeViewModel: com.pulsewave.app.feature.analyze.AnalyzeViewModel = org.koin.compose.viewmodel.koinViewModel()
    val walkViewModel: com.pulsewave.app.feature.walk.WalkViewModel = org.koin.compose.viewmodel.koinViewModel()
    val open: (androidx.navigation3.runtime.NavKey) -> Unit = { backStack.open(it) }

    // Back from any other tab returns Home first, like other Material apps; only Home leaves.
    com.pulsewave.app.core.platform.LocalBackHandler.current(
        backStack.size == 1 && backStack.first() != com.pulsewave.app.navigation.HomeDestination
    ) { backStack[0] = com.pulsewave.app.navigation.HomeDestination }

    // Pushed screens are single-column; keep them readable on tablets and desktop windows.
    @Composable
    fun Readable(content: @Composable () -> Unit) = com.pulsewave.app.core.designsystem.ReadableWidth { content() }

    com.pulsewave.app.navigation.PulseWaveNavigationSuite(backStack) {
        androidx.navigation3.ui.NavDisplay(
            backStack = backStack,
            onBack = { backStack.pop() },
            transitionSpec = com.pulsewave.app.navigation.fadeThrough(reduceMotion),
            popTransitionSpec = com.pulsewave.app.navigation.fadeThrough(reduceMotion),
            entryDecorators = listOf(
                androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator(),
                androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator()
            ),
            entryProvider = androidx.navigation3.runtime.entryProvider {
                entry<com.pulsewave.app.navigation.HomeDestination> {
                    OverviewScreen(
                        viewModel = overviewViewModel,
                        onNavigateToRooms = { backStack.selectTab(com.pulsewave.app.navigation.WalkDestination) },
                        onNavigateToWifi = { backStack.selectTab(com.pulsewave.app.navigation.NetworksDestination) },
                        onNavigateToLan = { open(com.pulsewave.app.navigation.DevicesDestination) },
                        onNavigateToSpeedTest = { open(com.pulsewave.app.navigation.SpeedTestDestination) },
                        onNavigateToHistory = { open(com.pulsewave.app.navigation.HistoryDestination) },
                        onNavigateToDiagnose = { open(com.pulsewave.app.navigation.ToolsDestination()) },
                        onNavigateToScanSetup = { open(com.pulsewave.app.navigation.ScanSetupDestination) },
                        onRequestScanPermission = onRequestScanPermission
                    )
                }
                entry<com.pulsewave.app.navigation.NetworksDestination> {
                    AnalyzeScreen(viewModel = analyzeViewModel, onNavigateToPowerTools = { open(com.pulsewave.app.navigation.ScanSetupDestination) })
                }
                entry<com.pulsewave.app.navigation.WalkDestination> {
                    Readable { WalkScreen(viewModel = walkViewModel, onOpenFloorPlan = { open(com.pulsewave.app.navigation.FloorPlanDestination) }) }
                }
                entry<com.pulsewave.app.navigation.SettingsDestination> {
                    Readable {
                        SettingsScreen(
                            onNavigateToScanSetup = { open(com.pulsewave.app.navigation.ScanSetupDestination) },
                            onNavigateToAbout = { open(com.pulsewave.app.navigation.AboutDestination) },
                            onRequestScanPermission = onRequestScanPermission
                        )
                    }
                }
                entry<com.pulsewave.app.navigation.DevicesDestination>(metadata = push) { key ->
                    Readable { LanScreen(onDeviceClick = { ip -> open(com.pulsewave.app.navigation.DeviceDestination(ip)) }, onBack = { backStack.pop(key) }) }
                }
                entry<com.pulsewave.app.navigation.DeviceDestination>(metadata = push) { key ->
                    Readable {
                        LanDeviceDetailScreen(
                            ip = key.ip,
                            onBack = { backStack.pop(key) },
                            onPing = { target -> open(com.pulsewave.app.navigation.ToolsDestination(target, ToolType.PING.name)) },
                            onPortScan = { target -> open(com.pulsewave.app.navigation.ToolsDestination(target, ToolType.PORT_SCAN.name)) }
                        )
                    }
                }
                entry<com.pulsewave.app.navigation.ToolsDestination>(metadata = push) { key ->
                    Readable {
                        ToolsScreen(
                            initialTarget = key.target,
                            initialTool = ToolType.entries.firstOrNull { it.name == key.tool },
                            onBack = { backStack.pop(key) }
                        )
                    }
                }
                entry<com.pulsewave.app.navigation.SpeedTestDestination>(metadata = push) { key ->
                    Readable { LinkTestScreen(
                        onBack = { backStack.pop(key) },
                        onOpenInternetTest = { open(com.pulsewave.app.navigation.InternetSpeedDestination) },
                        onOpenHistory = { open(com.pulsewave.app.navigation.HistoryDestination) }
                    ) }
                }
                entry<com.pulsewave.app.navigation.InternetSpeedDestination>(metadata = push) { key ->
                    Readable { InternetSpeedScreen(onBack = { backStack.pop(key) }) }
                }
                entry<com.pulsewave.app.navigation.HistoryDestination>(metadata = push) { key ->
                    Readable { HistoryScreen(onBack = { backStack.pop(key) }) }
                }
                entry<com.pulsewave.app.navigation.FloorPlanDestination>(metadata = push) { key ->
                    Readable {
                        if (canScanWifi) SurveyScreen(onBack = { backStack.pop(key) })
                        else PermissionRequiredScreen(onRequestScanPermission, onOpenSettings) { backStack.pop(key) }
                    }
                }
                entry<com.pulsewave.app.navigation.AboutDestination>(metadata = push) { key ->
                    Readable { com.pulsewave.app.feature.settings.AboutScreen(onBack = { backStack.pop(key) }) }
                }
                entry<com.pulsewave.app.navigation.ScanSetupDestination>(metadata = push) { key ->
                    Readable { ScanSetupScreen(onBack = { backStack.pop(key) }) }
                }
            }
        )
    }
}
