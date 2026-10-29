@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.bernaferrari.wifiweakspot.feature.analyze

import com.bernaferrari.wifiweakspot.core.designsystem.exceptBottom
import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.material.icons.outlined.Info
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material.icons.rounded.LockOpen
import com.bernaferrari.wifiweakspot.core.wifi.isSiblingOf

import androidx.compose.animation.togetherWith
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocationOff
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NetworkWifi1Bar
import androidx.compose.material.icons.rounded.NetworkWifi2Bar
import androidx.compose.material.icons.rounded.NetworkWifi3Bar
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SignalWifi0Bar
import androidx.compose.material.icons.rounded.SignalWifi4Bar
import com.bernaferrari.wifiweakspot.core.designsystem.component.NetworksIcons
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.WifiFind
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bernaferrari.wifiweakspot.core.datastore.SettingsRepository
import com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon
import com.bernaferrari.wifiweakspot.core.designsystem.component.polygon
import com.bernaferrari.wifiweakspot.core.designsystem.rememberWifiWeakspotHapticFeedback
import com.bernaferrari.wifiweakspot.core.designsystem.signalColors
import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import com.bernaferrari.wifiweakspot.core.permissions.PermissionGate
import com.bernaferrari.wifiweakspot.core.wifi.ScanThrottleManager
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import com.bernaferrari.wifiweakspot.core.wifi.ThrottleState
import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import com.bernaferrari.wifiweakspot.core.wifi.bandLabel
import com.bernaferrari.wifiweakspot.core.wifi.frequencyToChannel
import com.bernaferrari.wifiweakspot.core.wifi.getVendorName
import com.bernaferrari.wifiweakspot.core.wifi.isNetworkSecured
import com.bernaferrari.wifiweakspot.core.wifi.rememberRadioState
import com.bernaferrari.wifiweakspot.core.wifi.securityLabel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel


/** Follow the connected radio until the person chooses a band, including a connection arriving later. */
internal fun analyzerBand(connected: NetworkStatus, chosen: FrequencyBand?): FrequencyBand =
    chosen ?: FrequencyBand.entries.firstOrNull { connected.isConnected && it.contains(connected.frequency) }
        ?: FrequencyBand.BAND_2_4

/** Unthrottled: near-live. Throttled: one scan every 30 s stays inside 4 per 2 minutes. */
private fun throttleIntervalMs(state: ThrottleState): Long = when (state) {
    ThrottleState.Unlimited -> 4_000L
    is ThrottleState.Exhausted -> (state.secondsUntilNextScan + 1) * 1_000L
    else -> 30_000L
}

@Composable
fun AnalyzeScreen(
    modifier: Modifier = Modifier,
    viewModel: AnalyzeViewModel = koinViewModel(),
    onNavigateToPowerTools: () -> Unit = {}
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val wideScreen = maxWidth >= com.bernaferrari.wifiweakspot.core.designsystem.WindowWidth.Expanded
        val uiState by viewModel.uiState.collectAsStateWithLifecycle()
        val connected by viewModel.connected.collectAsStateWithLifecycle()
        val permissionGate: PermissionGate = koinInject()
        val permissions by permissionGate.state.collectAsStateWithLifecycle()
        val throttleManager: ScanThrottleManager = koinInject()
        val throttle by throttleManager.throttleState.collectAsStateWithLifecycle()
        val radios = rememberRadioState()
        val platform: com.bernaferrari.wifiweakspot.core.platform.PlatformActions = org.koin.compose.koinInject()
        val haptics = rememberWifiWeakspotHapticFeedback()
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        val canScan = permissions.canScanWifi && radios.wifiEnabled && radios.locationEnabled

        // Refresh while the screen is visible, paced to what Android allows.
        LaunchedEffect(canScan) {
            if (!canScan) return@LaunchedEffect
            while (isActive) {
                if (throttleManager.throttleState.value !is ThrottleState.Exhausted) viewModel.startScan()
                delay(throttleIntervalMs(throttleManager.throttleState.value))
            }
        }

        val requestPermission = com.bernaferrari.wifiweakspot.core.platform.rememberScanPermissionRequest()

        val networks = (uiState as? AnalyzeUiState.Success)?.networks.orEmpty()
        val isScanning = (uiState as? AnalyzeUiState.Success)?.isScanning == true || uiState is AnalyzeUiState.Loading
        // The band lives here so the collapsed top bar can switch it too.
        var chosenBand by rememberSaveable { mutableStateOf<FrequencyBand?>(null) }
        val band = analyzerBand(connected, chosenBand)
        val counts = remember(networks) { FrequencyBand.entries.associateWith { b -> networks.count { b.contains(it.frequency) } } }

        val appBarActions: @Composable RowScope.() -> Unit = {
            if (canScan && throttle !is ThrottleState.Unlimited) {
                IconButton(onClick = { haptics.click(); onNavigateToPowerTools() }) {
                    Icon(Icons.Rounded.Bolt, strings.common.fasterScans,
                        tint = if (throttle is ThrottleState.Exhausted) MaterialTheme.colorScheme.primary else androidx.compose.material3.LocalContentColor.current)
                }
            }
        }
        val changeBand: (FrequencyBand) -> Unit = { haptics.light(); chosenBand = it }
        val showBands = canScan && networks.isNotEmpty()
        Scaffold(
            modifier = Modifier.fillMaxSize().then(if (wideScreen) Modifier else Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)),
            topBar = {
                if (!wideScreen || !showBands) NetworksHeader(
                    band, counts, showBands, false, scrollBehavior, canScan && throttle !is ThrottleState.Unlimited, appBarActions, changeBand
                )
            }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding.exceptBottom())) {
                when {
                    !permissions.canScanWifi -> CenteredMessage(
                        icon = Icons.Rounded.WifiFind,
                        title = strings.common.permissionScreenTitle,
                        body = strings.analyze.scanPermissionBody,
                        action = strings.common.permissionAllowAccess,
                        onAction = requestPermission
                    )
                    !radios.wifiEnabled -> CenteredMessage(Icons.Rounded.WifiOff, strings.analyze.wifiOffTitle, strings.analyze.wifiOffBody, strings.common.openWifiSettings) { platform.openWifiSettings() }
                    !radios.locationEnabled -> CenteredMessage(Icons.Rounded.LocationOff, strings.analyze.locationOffTitle, strings.analyze.locationOffBody, strings.analyze.turnOnLocation) { platform.openLocationSettings() }
                    networks.isEmpty() && uiState !is AnalyzeUiState.Success -> {
                        if (uiState is AnalyzeUiState.Loading) {
                            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                LoadingIndicator(Modifier.size(96.dp))
                                Spacer(Modifier.height(16.dp))
                                Text(strings.survey.scanning, style = MaterialTheme.typography.titleMedium)
                            }
                        } else {
                            val exhausted = throttle as? ThrottleState.Exhausted
                            CenteredMessage(
                                icon = Icons.Rounded.WifiFind,
                                title = if (exhausted != null) strings.analyze.scanPausedTitle else strings.common.readyToScan,
                                body = if (exhausted != null) strings.analyze.scanPausedBody(exhausted.secondsUntilNextScan)
                                else strings.analyze.readyToScanBody,
                                action = if (exhausted != null) strings.analyze.turnOffThrottling else strings.analyze.scan,
                                onAction = { if (exhausted != null) onNavigateToPowerTools() else viewModel.startScan() }
                            )
                        }
                    }
                    else -> NetworksContent(
                        networks = networks,
                        history = (uiState as? AnalyzeUiState.Success)?.history.orEmpty(),
                        connected = connected,
                        band = band,
                        leftHeader = { NetworksHeader(band, counts, true, true, null, canScan && throttle !is ThrottleState.Unlimited, appBarActions, changeBand) },
                        bottomInset = padding.calculateBottomPadding(),
                        onBandChange = changeBand
                    )
                }
            }
        }
    }
}


/** Standard app-bar actions own the same band controls throughout the collapse. */
@Composable
private fun NetworksHeader(
    band: FrequencyBand,
    counts: Map<FrequencyBand, Int>,
    showBands: Boolean,
    tablet: Boolean,
    scrollBehavior: TopAppBarScrollBehavior?,
    extraAction: Boolean,
    actions: @Composable RowScope.() -> Unit,
    onBandChange: (FrequencyBand) -> Unit
) {
    val windowWidth = with(androidx.compose.ui.platform.LocalDensity.current) {
        androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.width.toDp()
    }
    val expandedWidth = minOf(272.dp, windowWidth - if (extraAction) 80.dp else 32.dp)
    MediumFlexibleTopAppBar(
        title = { Text(strings.common.tabNetworks, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        actions = {
            Row(Modifier.padding(end = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                if (showBands) AppBarBandSelector(band, counts, scrollBehavior?.state?.collapsedFraction ?: 0f, expandedWidth, onBandChange)
                actions()
            }
        },
        windowInsets = if (tablet) androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0) else TopAppBarDefaults.windowInsets,
        scrollBehavior = scrollBehavior
    )
}


@Composable
private fun CenteredMessage(icon: ImageVector, title: String, body: String, action: String, onAction: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        ShapeIcon(icon, MaterialShapes.Cookie9Sided, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.onPrimaryContainer, size = 120.dp, iconSize = 56.dp)
        Spacer(Modifier.height(24.dp))
        Text(title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(body, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAction, shapes = ButtonDefaults.shapes(), modifier = Modifier.heightIn(min = ButtonDefaults.MediumContainerHeight)) {
            Text(action, style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
        }
    }
}

@Composable
private fun NetworksContent(
    networks: List<WifiNetwork>,
    history: SignalTimeline,
    connected: NetworkStatus,
    band: FrequencyBand,
    leftHeader: @Composable () -> Unit,
    bottomInset: androidx.compose.ui.unit.Dp,
    onBandChange: (FrequencyBand) -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val reduceMotion = LocalReduceMotion.current
        val haptics = rememberWifiWeakspotHapticFeedback()
        var showHistory by rememberSaveable { mutableStateOf(false) }
        var showHelp by remember { mutableStateOf(false) }
        var showExplorer by rememberSaveable { mutableStateOf(false) }
        var selected by remember { mutableStateOf<WifiNetwork?>(null) }
        // Tapping the highlighted network again clears it; null always clears.
        val toggle: (WifiNetwork?) -> Unit = { pick ->
            haptics.light()
            selected = if (pick == null || pick.bssid == selected?.bssid) null else pick
        }
        LaunchedEffect(band) { selected = null }
        var detail by remember { mutableStateOf<WifiNetwork?>(null) }
        // Your own connection leads the list; everything else is strongest first.
        // Rows hold their places between scans (see settledOrder), so taps land where you aim.
        val lastOrder = remember(band) { arrayOf(emptyList<String>()) }
        val inBand = remember(networks, band, connected.bssid) {
            settledOrder(
                lastOrder[0],
                networks.filter { band.contains(it.frequency) },
                id = { it.bssid },
                strength = { it.rssi },
                pinned = { connected.isConnected && it.bssid.equals(connected.bssid, ignoreCase = true) }
            ).also { lastOrder[0] = it.map { n -> n.bssid } }
        }

        val windowWidth = maxWidth
        val wide = maxWidth >= com.bernaferrari.wifiweakspot.core.designsystem.WindowWidth.Expanded
        val palette = rememberNetworkPalette(inBand, connected.bssid.takeIf { connected.isConnected })
        val channelContext: @Composable (Modifier) -> Unit = { contextModifier ->
            androidx.compose.animation.AnimatedVisibility(
                visible = !showHistory || selected != null,
                modifier = contextModifier,
                enter = if (reduceMotion) androidx.compose.animation.EnterTransition.None else
                    androidx.compose.animation.fadeIn(androidx.compose.animation.core.tween(150)) + androidx.compose.animation.expandVertically(androidx.compose.animation.core.tween(180)),
                exit = if (reduceMotion) androidx.compose.animation.ExitTransition.None else
                    androidx.compose.animation.fadeOut(androidx.compose.animation.core.tween(100)) + androidx.compose.animation.shrinkVertically(androidx.compose.animation.core.tween(150))
            ) {
                ChannelContext(
                    networks = networks, band = band, connected = connected,
                    selectedNetwork = selected?.let { pick -> inBand.firstOrNull { it.bssid == pick.bssid } ?: pick },
                    palette = palette,
                    showRecommendation = !showHistory,
                    onClearSelection = { toggle(null) }
                )
            }
        }
        val chart: @Composable (Modifier) -> Unit = { chartModifier ->
            Card(modifier = chartModifier, shape = WifiWeakspotShapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column((if (wide) Modifier.fillMaxSize() else Modifier).padding(vertical = 12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (showHistory) strings.common.historyTitle else strings.analyze.channels, style = if (windowWidth < 360.dp) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f, fill = false), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            IconButton(onClick = { haptics.click(); showHelp = true }) {
                                Icon(Icons.Outlined.Info, strings.analyze.howToReadThisChart, Modifier.size(18.dp))
                            }
                        }
                        // Match the band selector's visible gap; each segment fills its 48 dp width.
                        Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                                listOf(false to (NetworksIcons.Filled to strings.analyze.channels), true to (Icons.AutoMirrored.Rounded.ShowChart to strings.analyze.signalOverTime))
                                    .forEachIndexed { index, (history, look) ->
                                        ToggleButton(
                                            checked = showHistory == history,
                                            onCheckedChange = { haptics.light(); showHistory = history },
                                            shapes = if (index == 0) ButtonGroupDefaults.connectedLeadingButtonShapes() else ButtonGroupDefaults.connectedTrailingButtonShapes(),
                                            contentPadding = PaddingValues(horizontal = 12.dp),
                                            colors = ToggleButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                                            modifier = Modifier.size(48.dp)
                                        ) { Icon(look.first, look.second, Modifier.size(20.dp)) }
                                    }
                        }
                        IconButton(onClick = { haptics.click(); showExplorer = true }) { Icon(Icons.Rounded.Fullscreen, if (showHistory) strings.analyze.expandSignalHistory else strings.analyze.expandChannels) }
                    }
                    Box(Modifier.fillMaxWidth().then(if (wide) Modifier.weight(1f) else Modifier.height(340.dp))) {
                        if (inBand.isEmpty()) {
                            Text(
                                strings.analyze.noBandNetworksNearby(band.label),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else if (showHistory) {
                            SignalHistoryGraph(
                                history = history.filterKeys { bssid -> inBand.any { it.bssid == bssid } },
                                networks = inBand,
                                selectedNetwork = selected,
                                connectedBssid = connected.bssid.takeIf { connected.isConnected },
                                onNetworkSelected = { selected = it },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            ChannelGraph(
                                networks = inBand,
                                selectedBand = band,
                                selectedNetwork = selected,
                                connectedBssid = connected.bssid.takeIf { connected.isConnected },
                                onNetworkSelected = toggle,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
        val controls: LazyListScope.() -> Unit = {
            item(key = "chart") {
                chart(Modifier)
            }
            item(key = "advice") { channelContext(Modifier) }
        }
        var showWeak by rememberSaveable(band) { mutableStateOf(false) }
        val list: LazyListScope.() -> Unit = {
            item(key = "listHeader") {
                Text(
                    strings.analyze.networksOnBand(inBand.size, band.label),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 4.dp, top = 20.dp, bottom = 8.dp)
                )
            }
            // With dozens of neighbors, the unusable ones fold away behind one row.
            val weak = if (inBand.size > 10) inBand.filter { SignalQuality.of(it.rssi) == SignalQuality.DeadZone && !it.bssid.equals(connected.bssid, true) } else emptyList()
            val visible = if (showWeak) inBand else inBand - weak.toSet()
            itemsIndexed(visible, key = { _, n -> n.bssid }) { index, network ->
                NetworkRow(
                    network = network,
                    index = index,
                    count = visible.size,
                    isConnected = connected.isConnected && network.bssid.equals(connected.bssid, ignoreCase = true),
                    isYourRouter = connected.isConnected && network.isSiblingOf(connected.bssid, connected.frequency),
                    isSelected = selected?.bssid == network.bssid,
                    chartColor = palette.color(network.bssid),
                    onClick = { toggle(network) },
                    onDetails = { haptics.click(); detail = network },
                    modifier = if (reduceMotion) Modifier else Modifier.animateItem()
                )
            }
            if (weak.isNotEmpty()) {
                item(key = "weakToggle") {
                    TextButton(
                        onClick = { haptics.light(); showWeak = !showWeak },
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text(if (showWeak) strings.analyze.hideVeryWeakNetworks(weak.size) else strings.analyze.showVeryWeakNetworks(weak.size))
                    }
                }
            }
        }

        val listState = androidx.compose.foundation.lazy.rememberLazyListState()
        Box(Modifier.fillMaxSize()) {
            val padding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = bottomInset + 32.dp)
            if (wide) {
                Row(Modifier.fillMaxSize()) {
                    Column(
                        Modifier.weight(1.2f).fillMaxSize().padding(bottom = bottomInset + 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        leftHeader()
                        chart(Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp))
                    }
                    Column(Modifier.weight(1f).fillMaxSize()) {
                        channelContext(Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 4.dp, bottom = 8.dp))
                        LazyColumn(Modifier.fillMaxWidth().weight(1f), contentPadding = padding,
                            verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap), content = list)
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxSize(), state = listState, contentPadding = padding, verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    controls()
                    list()
                }
            }
        }

        if (showHelp) ChartGuideSheet { showHelp = false }
        if (showExplorer) ChartExplorer(
            allNetworks = networks,
            band = band,
            onBandChange = onBandChange,
            connectedBssid = connected.bssid.takeIf { connected.isConnected },
            connectedFrequency = connected.frequency.takeIf { connected.isConnected },
            history = history,
            showHistory = showHistory,
            onDismiss = { showExplorer = false }
        )
        detail?.let { network ->
            val settings: SettingsRepository = koinInject()
            val favorites by settings.favoriteBssids.collectAsState(initial = emptySet())
            val scope = rememberCoroutineScope()
            val platform: com.bernaferrari.wifiweakspot.core.platform.PlatformActions = org.koin.compose.koinInject()
            val common = strings.common
            val sCatalog = strings
            val s = strings.analyze
            ModalBottomSheet(onDismissRequest = { detail = null }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                WifiDetailScreen(
                    network = network,
                    onBack = { detail = null },
                    isFavorite = network.bssid in favorites,
                    onToggleFavorite = { scope.launch { settings.toggleFavorite(network.bssid) } },
                    onTrack = {
                        selected = network; showHistory = true; detail = null
                        // Bring the graph into view; it lives at the top of the list.
                        if (!wide) scope.launch {
                            if (reduceMotion) listState.scrollToItem(0) else listState.animateScrollToItem(0)
                        }
                    },
                    onShare = {
                        val text = buildString {
                            appendLine(network.ssid.ifEmpty { common.hiddenNetwork })
                            appendLine(s.shareNetworkLine(network.rssi, frequencyToChannel(network.frequency), bandLabel(network.frequency)))
                            appendLine(network.bssid)
                        }
                        platform.share(text, sCatalog.common.share)
                    }
                )
            }
        }
    }
}


@Composable
private fun AppBarBandSelector(band: FrequencyBand, counts: Map<FrequencyBand, Int>, collapse: Float, expandedWidth: androidx.compose.ui.unit.Dp, onSelect: (FrequencyBand) -> Unit) {
    val fraction = collapse.coerceIn(0f, 1f)
    val density = androidx.compose.ui.platform.LocalDensity.current
    val unitStyle = MaterialTheme.typography.labelSmall
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    val unitWidth = with(density) { measurer.measure("GHz", unitStyle).size.width.toDp() + 4.dp }
    val includeCounts = expandedWidth >= 256.dp && density.fontScale <= 1.2f
    Row(
        Modifier.width(androidx.compose.ui.unit.lerp(expandedWidth, 152.dp, fraction)).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        FrequencyBand.entries.forEachIndexed { index, entry ->
            val description = "${entry.label}, ${strings.analyze.bandNetworkCount(counts[entry] ?: 0)}"
            ToggleButton(
                checked = band == entry,
                onCheckedChange = { onSelect(entry) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    FrequencyBand.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                contentPadding = PaddingValues(horizontal = 6.dp),
                colors = ToggleButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                modifier = Modifier.weight(1f).height(48.dp).semantics { contentDescription = description }
            ) {
                Row(Modifier.clearAndSetSemantics { }, verticalAlignment = Alignment.CenterVertically) {
                    Text(entry.label.removeSuffix(" GHz"), style = MaterialTheme.typography.labelLarge)
                    Box(Modifier.width(androidx.compose.ui.unit.lerp(unitWidth, 0.dp, fraction))
                        .graphicsLayer { alpha = 1f - fraction; clip = true }, contentAlignment = Alignment.CenterEnd) {
                        Text("GHz", style = unitStyle, maxLines = 1, softWrap = false)
                    }
                    Spacer(Modifier.width(if (includeCounts) androidx.compose.ui.unit.lerp(4.dp, 0.dp, fraction) else 0.dp))
                    Box(Modifier.width(if (includeCounts) androidx.compose.ui.unit.lerp(24.dp, 0.dp, fraction) else 0.dp)
                        .then(Modifier.graphicsLayer { alpha = 1f - fraction; clip = true }), contentAlignment = Alignment.Center) {
                        val ink = androidx.compose.material3.LocalContentColor.current
                        Badge(containerColor = ink.copy(alpha = 0.12f), contentColor = ink) {
                            Text((counts[entry] ?: 0).toString(), maxLines = 1, softWrap = false, style = MaterialTheme.typography.labelSmall.copy(fontFeatureSettings = "tnum"))
                        }
                    }
                }
            }
        }
    }
}

/** The band picker squeezed into the collapsed top bar: just "2.4 · 5 · 6". */
@Composable
internal fun CompactBandSelector(band: FrequencyBand, onSelect: (FrequencyBand) -> Unit) {
    Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
        FrequencyBand.entries.forEachIndexed { index, entry ->
            ToggleButton(
                checked = band == entry,
                onCheckedChange = { onSelect(entry) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    FrequencyBand.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                contentPadding = PaddingValues(horizontal = 14.dp),
                // Same connected group as the full selector; unselected segments get a tone so they
                // read as buttons against the app bar.
                colors = ToggleButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                modifier = Modifier.heightIn(min = 48.dp).widthIn(min = 48.dp).semantics { contentDescription = entry.label }
            ) { Text(entry.label.removeSuffix(" GHz"), style = MaterialTheme.typography.labelLarge) }
        }
    }
}

@Composable
private fun NetworkRow(
    network: WifiNetwork,
    index: Int,
    count: Int,
    isConnected: Boolean,
    isYourRouter: Boolean,
    isSelected: Boolean,
    chartColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    onDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quality = SignalQuality.of(network.rssi)
    val colors = signalColors(quality)
    val scheme = MaterialTheme.colorScheme
    val vendor = remember(network.bssid) { getVendorName(network.bssid) }
    val protocol = securityLabel(network.capabilities.ifBlank { network.security })
    val secured = isNetworkSecured(network.capabilities) || isNetworkSecured(network.security)
    val common = strings.common
    val sCatalog = strings
    val s = strings.analyze
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.colors(containerColor = if (isSelected) scheme.secondaryContainer else scheme.surfaceContainer),
        modifier = modifier.semantics {
            selected = isSelected
            stateDescription = if (isSelected) s.highlightedOnChart else s.notHighlightedOnChart
        },
        // The Wi-Fi symbol shares the same quality scale as Home and Rooms.
        leadingContent = { com.bernaferrari.wifiweakspot.core.designsystem.component.WifiBadge(network.rssi) },
        trailingContent = {
            IconButton(onClick = onDetails) { Icon(Icons.Outlined.Info, s.detailsForNetwork(network.ssid)) }
        },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Most networks have a password; only the exception gets marked.
                if (!secured || protocol == "Open") {
                    Icon(Icons.Rounded.LockOpen, s.noPassword, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    // Guest or hidden names on your own radio need no more than that.
                    if (isYourRouter) s.alsoYourRouter(quality.label)
                    else listOfNotNull(
                        quality.label,
                        s.channelShort(frequencyToChannel(network.frequency)),
                        if (network.channelWidth > 20) "${network.channelWidth} MHz" else null
                    ).joinToString(" · "),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Matches this network's hill and line in the chart above.
            Box(Modifier.size(10.dp).background(chartColor, androidx.compose.foundation.shape.CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(
                network.ssid.ifEmpty { common.hiddenNetwork },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isConnected) {
                Spacer(Modifier.width(8.dp))
                Surface(shape = WifiWeakspotShapes.full, color = scheme.primary, contentColor = scheme.onPrimary) {
                    Text(s.connected, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
        }
    }
}


