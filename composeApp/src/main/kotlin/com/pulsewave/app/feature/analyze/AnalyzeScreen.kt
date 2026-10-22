@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.pulsewave.app.feature.analyze

import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material3.toShape
import androidx.compose.ui.draw.clip
import com.pulsewave.app.core.wifi.competitorsOf
import com.pulsewave.app.core.wifi.isSiblingOf

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
import androidx.compose.material.icons.rounded.WaterfallChart
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.WifiFind
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.datastore.SettingsRepository
import com.pulsewave.app.core.designsystem.LocalReduceMotion
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.component.ShapeIcon
import com.pulsewave.app.core.designsystem.component.polygon
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.permissions.PermissionGate
import com.pulsewave.app.core.wifi.ScanThrottleManager
import com.pulsewave.app.core.wifi.SignalQuality
import com.pulsewave.app.core.wifi.ThrottleState
import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.wifi.bandLabel
import com.pulsewave.app.core.wifi.frequencyToChannel
import com.pulsewave.app.core.wifi.getVendorName
import com.pulsewave.app.core.wifi.isNetworkSecured
import com.pulsewave.app.core.wifi.rememberRadioState
import com.pulsewave.app.core.wifi.securityLabel
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
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val connected by viewModel.connected.collectAsStateWithLifecycle()
    val permissionGate: PermissionGate = koinInject()
    val permissions by permissionGate.state.collectAsStateWithLifecycle()
    val throttleManager: ScanThrottleManager = koinInject()
    val throttle by throttleManager.throttleState.collectAsStateWithLifecycle()
    val radios = rememberRadioState()
    val platform: com.pulsewave.app.core.platform.PlatformActions = org.koin.compose.koinInject()
    val haptics = rememberPulseWaveHapticFeedback()
    val reduceMotion = LocalReduceMotion.current
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

    val requestPermission = com.pulsewave.app.core.platform.rememberScanPermissionRequest()

    val networks = (uiState as? AnalyzeUiState.Success)?.networks.orEmpty()
    val isScanning = (uiState as? AnalyzeUiState.Success)?.isScanning == true || uiState is AnalyzeUiState.Loading
    // The band lives here so the collapsed top bar can switch it too.
    var chosenBand by rememberSaveable { mutableStateOf<FrequencyBand?>(null) }
    val band = analyzerBand(connected, chosenBand)
    val counts = remember(networks) { FrequencyBand.entries.associateWith { b -> networks.count { b.contains(it.frequency) } } }
    val collapsed by remember { androidx.compose.runtime.derivedStateOf { scrollBehavior.state.collapsedFraction > 0.6f } }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Networks") },
                actions = {
                    // Scrolled down, the bands stay one tap away in the bar.
                    androidx.compose.animation.AnimatedVisibility(
                        visible = collapsed && canScan && networks.isNotEmpty(),
                        enter = if (reduceMotion) androidx.compose.animation.EnterTransition.None else androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(initialScale = 0.9f),
                        exit = if (reduceMotion) androidx.compose.animation.ExitTransition.None else androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut(targetScale = 0.9f)
                    ) {
                        CompactBandSelector(band) { haptics.light(); chosenBand = it }
                    }
                    // Scanning is automatic; the only thing worth a button is lifting Android's limit.
                    if (canScan && throttle !is ThrottleState.Unlimited) {
                        IconButton(onClick = { haptics.click(); onNavigateToPowerTools() }) {
                            Icon(Icons.Rounded.Bolt, "Faster scans", tint = if (throttle is ThrottleState.Exhausted) MaterialTheme.colorScheme.primary else androidx.compose.material3.LocalContentColor.current)
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                !permissions.canScanWifi -> CenteredMessage(
                    icon = Icons.Rounded.WifiFind,
                    title = "See the Wi-Fi around you",
                    body = "Android only shares nearby network names with apps that have location access. WiFi\u00A0Weakspot never records where you are.",
                    action = "Allow",
                    onAction = requestPermission
                )
                !radios.wifiEnabled -> CenteredMessage(Icons.Rounded.WifiOff, "Wi-Fi is off", "Turn on Wi-Fi to see nearby networks.", "Open Wi-Fi settings") { platform.openWifiSettings() }
                !radios.locationEnabled -> CenteredMessage(Icons.Rounded.LocationOff, "Location is off", "Android needs Location on to list nearby Wi-Fi. WiFi\u00A0Weakspot doesn't use GPS.", "Turn on Location") { platform.openLocationSettings() }
                networks.isEmpty() && uiState !is AnalyzeUiState.Success -> {
                    if (uiState is AnalyzeUiState.Loading) {
                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            LoadingIndicator(Modifier.size(96.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Listening for networks…", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        val exhausted = throttle as? ThrottleState.Exhausted
                        CenteredMessage(
                            icon = Icons.Rounded.WifiFind,
                            title = if (exhausted != null) "Android paused scanning" else "Ready to scan",
                            body = if (exhausted != null) "The next scan is allowed in ${exhausted.secondsUntilNextScan} s. Turn off scan throttling to skip the wait."
                            else "Tap scan to see every network nearby.",
                            action = if (exhausted != null) "Turn off throttling" else "Scan",
                            onAction = { if (exhausted != null) onNavigateToPowerTools() else viewModel.startScan() }
                        )
                    }
                }
                else -> NetworksContent(
                    networks = networks,
                    history = (uiState as? AnalyzeUiState.Success)?.history.orEmpty(),
                    connected = connected,
                    band = band,
                    counts = counts,
                    onBandChange = { haptics.light(); chosenBand = it }
                )
            }
        }
    }
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
    history: Map<String, List<Int>>,
    connected: NetworkStatus,
    band: FrequencyBand,
    counts: Map<FrequencyBand, Int>,
    onBandChange: (FrequencyBand) -> Unit
) {
    val display = com.pulsewave.app.core.designsystem.LocalSignalDisplay.current
    val reduceMotion = LocalReduceMotion.current
    val haptics = rememberPulseWaveHapticFeedback()
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

    val wide = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize.width.let {
        with(androidx.compose.ui.platform.LocalDensity.current) { it.toDp() }
    } >= com.pulsewave.app.core.designsystem.WindowWidth.Expanded
    val palette = rememberNetworkPalette(inBand, connected.bssid.takeIf { connected.isConnected })
    val controls: LazyListScope.() -> Unit = {
        item(key = "bands") {
            BandSelector(band, counts) { onBandChange(it) }
        }
        item(key = "chart") {
            Card(shape = PulseWaveShapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(vertical = 12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (showHistory) "Signal over time" else "Channels", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 2)
                        // Match the band selector's visible gap; each segment fills its 48 dp width.
                        Row(modifier = Modifier.selectableGroup(), horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                                listOf(false to (Icons.Rounded.WaterfallChart to "Channels"), true to (Icons.AutoMirrored.Rounded.ShowChart to "Signal over time"))
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
                        IconButton(onClick = { haptics.click(); showExplorer = true }) { Icon(Icons.Rounded.Fullscreen, if (showHistory) "Expand signal history" else "Expand channels") }
                    }
                    Box(Modifier.fillMaxWidth().height(if (wide) 440.dp else 340.dp).padding(horizontal = 4.dp)) {
                        if (inBand.isEmpty()) {
                            Text(
                                "No ${band.label} networks nearby.",
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
                                onNetworkSelected = toggle,
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
                    if (!showHistory) Row(
                        Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 20.dp, end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(selected?.ssid?.ifBlank { "Hidden network" } ?: "Explore channels", style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(
                                selected?.let { "${SignalQuality.of(it.rssi).label} · ${display.reading(it.rssi)} · Ch ${frequencyToChannel(it.frequency)}" }
                                    ?: "Tap a hill or slide across the chart",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { showHelp = true }) { Text("Guide") }
                    }
                }
            }
        }
        item(key = "advice") { ChannelAdvice(networks, band, connected) }
    }
    var showWeak by rememberSaveable(band) { mutableStateOf(false) }
    val list: LazyListScope.() -> Unit = {
        item(key = "listHeader") {
            Text(
                when (inBand.size) { 0 -> "No networks on ${band.label}"; 1 -> "1 network on ${band.label}"; else -> "${inBand.size} networks on ${band.label}" },
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
                    Text(if (showWeak) "Hide ${weak.size} very weak networks" else "Show ${weak.size} very weak networks")
                }
            }
        }
    }

    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val padding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp)
        if (maxWidth >= 840.dp) {
            Row(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.weight(1.2f), contentPadding = padding, verticalArrangement = Arrangement.spacedBy(12.dp), content = controls)
                LazyColumn(Modifier.weight(1f), contentPadding = padding, verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap), content = list)
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
        val platform: com.pulsewave.app.core.platform.PlatformActions = org.koin.compose.koinInject()
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
                        appendLine(network.ssid.ifEmpty { "Hidden network" })
                        appendLine("${network.rssi} dBm · Ch ${frequencyToChannel(network.frequency)} · ${bandLabel(network.frequency)}")
                        appendLine(network.bssid)
                    }
                    platform.share(text, "Share network")
                }
            )
        }
    }
}

@Composable
private fun BandSelector(band: FrequencyBand, counts: Map<FrequencyBand, Int>, onSelect: (FrequencyBand) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp).selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        FrequencyBand.entries.forEachIndexed { index, entry ->
            ToggleButton(
                checked = band == entry,
                onCheckedChange = { onSelect(entry) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    FrequencyBand.entries.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                colors = ToggleButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                modifier = Modifier.weight(1f).heightIn(min = 56.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(entry.label, style = MaterialTheme.typography.labelLarge)
                    Text(
                        when (val n = counts[entry] ?: 0) { 0 -> "none"; 1 -> "1 network"; else -> "$n networks" },
                        style = MaterialTheme.typography.labelSmall
                    )
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
private fun ChannelAdvice(networks: List<WifiNetwork>, band: FrequencyBand, connected: NetworkStatus) {
    // Your router's guest or hidden names on the same radio aren't neighbors.
    val competitors = remember(networks, connected.bssid, connected.frequency) {
        networks.competitorsOf(connected.bssid.takeIf { connected.isConnected }, connected.frequency)
    }
    val scores = remember(competitors, band) {
        CongestionAnalyzer.analyze(competitors.filter { band.contains(it.frequency) }, band)
    }
    val connectedHere = connected.isConnected && band.contains(connected.frequency)
    val (channel, text) = channelHint(scores, connected.channel.takeIf { connectedHere }) ?: return
    val scheme = MaterialTheme.colorScheme
    // A small hint, sized to its words: useful, but not the point of the screen.
    Surface(
        shape = PulseWaveShapes.full,
        color = scheme.tertiaryContainer,
        contentColor = scheme.onTertiaryContainer,
        modifier = Modifier.padding(top = 6.dp).clearAndSetSemantics { contentDescription = "$text: channel $channel" }
    ) {
        Row(Modifier.padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).clip(MaterialShapes.Cookie9Sided.toShape()).background(scheme.onTertiaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text("$channel", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.tertiaryContainer)
            }
            Spacer(Modifier.width(10.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index = index, count = count),
        colors = ListItemDefaults.colors(containerColor = if (isSelected) scheme.secondaryContainer else scheme.surfaceContainer),
        modifier = modifier.semantics {
            selected = isSelected
            stateDescription = if (isSelected) "Highlighted on the chart" else "Not highlighted on the chart"
        },
        // The Wi-Fi symbol shares the same quality scale as Home and Rooms.
        leadingContent = { com.pulsewave.app.core.designsystem.component.WifiBadge(network.rssi) },
        trailingContent = {
            IconButton(onClick = onDetails) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Details for ${network.ssid.ifEmpty { "hidden network" }}") }
        },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Most networks have a password; only the exception gets marked.
                if (!secured || protocol == "Open") {
                    Icon(Icons.Rounded.LockOpen, "No password", Modifier.size(14.dp), tint = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    // Guest or hidden names on your own radio need no more than that.
                    if (isYourRouter) "${quality.label} · Also your router"
                    else listOfNotNull(
                        quality.label,
                        "Ch ${frequencyToChannel(network.frequency)}",
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
                network.ssid.ifEmpty { "Hidden network" },
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )
            if (isConnected) {
                Spacer(Modifier.width(8.dp))
                Surface(shape = PulseWaveShapes.full, color = scheme.primary, contentColor = scheme.onPrimary) {
                    Text("Connected", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                }
            }
        }
    }
}


