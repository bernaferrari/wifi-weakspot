@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.pulsewave.app.feature.analyze

import android.content.Intent
import android.provider.Settings
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.datastore.SettingsRepository
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

enum class FrequencyBand(val label: String) {
    BAND_2_4("2.4 GHz"),
    BAND_5("5 GHz"),
    BAND_6("6 GHz");

    fun contains(frequency: Int): Boolean = when (this) {
        BAND_2_4 -> frequency in 2400..2500
        BAND_5 -> frequency in 5150..5924
        BAND_6 -> frequency in 5925..7125
    }
}

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
    val context = LocalContext.current
    val haptics = rememberPulseWaveHapticFeedback()
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

    val requestPermission = com.pulsewave.app.core.permissions.rememberScanPermissionRequest()

    val networks = (uiState as? AnalyzeUiState.Success)?.networks.orEmpty()
    val isScanning = (uiState as? AnalyzeUiState.Success)?.isScanning == true || uiState is AnalyzeUiState.Loading

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Nearby Wi-Fi") },
                subtitle = { if (canScan) ScanSubtitle(networks, throttle) },
                actions = {
                    if (canScan) {
                        Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                            if (isScanning) LoadingIndicator(Modifier.size(32.dp))
                            else IconButton(onClick = {
                                // While Android has paused scans, the hint below explains why.
                                if (throttle is ThrottleState.Exhausted) haptics.error() else { haptics.click(); viewModel.startScan() }
                            }) { Icon(Icons.Rounded.Refresh, "Scan now") }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        val open: (String) -> Unit = { action -> context.startActivity(Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                !permissions.canScanWifi -> CenteredMessage(
                    icon = Icons.Rounded.WifiFind,
                    title = "See the Wi-Fi around you",
                    body = "Android only shares nearby network names with apps that have location access. PulseWave never records where you are.",
                    action = "Allow",
                    onAction = requestPermission
                )
                !radios.wifiEnabled -> CenteredMessage(Icons.Rounded.WifiOff, "Wi-Fi is off", "Turn on Wi-Fi to see nearby networks.", "Open Wi-Fi settings") { open(Settings.ACTION_WIFI_SETTINGS) }
                !radios.locationEnabled -> CenteredMessage(Icons.Rounded.LocationOff, "Location is off", "Android needs Location on to list nearby Wi-Fi. PulseWave doesn't use GPS.", "Turn on Location") { open(Settings.ACTION_LOCATION_SOURCE_SETTINGS) }
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
                    throttle = throttle,
                    onUnlock = onNavigateToPowerTools
                )
            }
        }
    }
}

@Composable
private fun ScanSubtitle(networks: List<WifiNetwork>, throttle: ThrottleState) {
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) { while (isActive) { delay(1_000); now = System.currentTimeMillis() } }
    val latest = networks.maxOfOrNull { it.timestamp }
    val age = latest?.let { ((now - it) / 1000).coerceAtLeast(0) }
    val count = when (networks.size) { 0 -> "No networks yet"; 1 -> "1 network"; else -> "${networks.size} networks" }
    val freshness = when {
        throttle is ThrottleState.Unlimited -> "live"
        age == null -> null
        age < 60 -> "updated ${age}s ago"
        else -> "updated ${age / 60} min ago"
    }
    Text(listOfNotNull(count, freshness).joinToString(" · "))
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
    throttle: ThrottleState,
    onUnlock: () -> Unit
) {
    val haptics = rememberPulseWaveHapticFeedback()
    val connectedBand = FrequencyBand.entries.firstOrNull { connected.isConnected && it.contains(connected.frequency) }
    var band by rememberSaveable { mutableStateOf(connectedBand ?: FrequencyBand.BAND_2_4) }
    var showHistory by rememberSaveable { mutableStateOf(false) }
    var showHelp by remember { mutableStateOf(false) }
    var selected by remember { mutableStateOf<WifiNetwork?>(null) }
    var detail by remember { mutableStateOf<WifiNetwork?>(null) }
    val inBand = remember(networks, band) { networks.filter { band.contains(it.frequency) }.sortedByDescending { it.rssi } }
    val counts = remember(networks) { FrequencyBand.entries.associateWith { b -> networks.count { b.contains(it.frequency) } } }

    val controls: LazyListScope.() -> Unit = {
        // Only when Android actually gets in the way; otherwise the subtitle says how fresh it is.
        if (throttle is ThrottleState.Exhausted) {
            item(key = "throttle") { ThrottleHint(throttle, onUnlock) }
        }
        item(key = "bands") {
            BandSelector(band, counts) { haptics.light(); band = it; selected = null }
        }
        item(key = "chart") {
            Card(shape = PulseWaveShapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
                Column(Modifier.padding(vertical = 12.dp)) {
                    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (showHistory) "Signal over time" else "Channels", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                        FilledTonalIconToggleButton(checked = !showHistory, onCheckedChange = { showHistory = false }, shapes = IconButtonDefaults.toggleableShapes()) {
                            Icon(Icons.Rounded.WaterfallChart, "Channels")
                        }
                        FilledTonalIconToggleButton(checked = showHistory, onCheckedChange = { showHistory = true }, shapes = IconButtonDefaults.toggleableShapes()) {
                            Icon(Icons.AutoMirrored.Rounded.ShowChart, "Signal over time")
                        }
                        IconButton(onClick = { showHelp = true }) { Icon(Icons.AutoMirrored.Rounded.HelpOutline, "How to read this") }
                    }
                    Box(Modifier.fillMaxWidth().height(280.dp).padding(horizontal = 4.dp)) {
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
                                onNetworkSelected = { selected = it },
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            ChannelGraph(
                                networks = inBand,
                                selectedBand = band,
                                selectedNetwork = selected,
                                connectedBssid = connected.bssid.takeIf { connected.isConnected },
                                onNetworkSelected = { selected = it },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
        item(key = "advice") { ChannelAdvice(networks, band, connected) }
    }
    val colorFor = rememberNetworkColors(inBand, connected.bssid.takeIf { connected.isConnected })
    val list: LazyListScope.() -> Unit = {
        item(key = "listHeader") {
            Text(
                when (inBand.size) { 0 -> "No networks on ${band.label}"; 1 -> "1 network on ${band.label}"; else -> "${inBand.size} networks on ${band.label}" },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
            )
        }
        itemsIndexed(inBand, key = { _, n -> n.bssid }) { index, network ->
            NetworkRow(
                network = network,
                index = index,
                count = inBand.size,
                isConnected = connected.isConnected && network.bssid.equals(connected.bssid, ignoreCase = true),
                isSelected = selected?.bssid == network.bssid,
                chartColor = colorFor(network.bssid),
                onClick = { haptics.click(); selected = network; detail = network },
                modifier = Modifier.animateItem()
            )
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val padding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp)
        if (maxWidth >= 840.dp) {
            Row(Modifier.fillMaxSize()) {
                LazyColumn(Modifier.weight(1.2f), contentPadding = padding, verticalArrangement = Arrangement.spacedBy(12.dp), content = controls)
                LazyColumn(Modifier.weight(1f), contentPadding = padding, verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap), content = list)
            }
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = padding, verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                controls()
                list()
            }
        }
    }

    if (showHelp) GraphLegendDialog { showHelp = false }
    detail?.let { network ->
        val settings: SettingsRepository = koinInject()
        val favorites by settings.favoriteBssids.collectAsState(initial = emptySet())
        val scope = rememberCoroutineScope()
        val context = LocalContext.current
        ModalBottomSheet(onDismissRequest = { detail = null }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            WifiDetailScreen(
                network = network,
                onBack = { detail = null },
                isFavorite = network.bssid in favorites,
                onToggleFavorite = { scope.launch { settings.toggleFavorite(network.bssid) } },
                onTrack = { selected = network; showHistory = true; detail = null },
                onShare = {
                    val text = buildString {
                        appendLine(network.ssid.ifEmpty { "Hidden network" })
                        appendLine("${network.rssi} dBm · Ch ${frequencyToChannel(network.frequency)} · ${bandLabel(network.frequency)}")
                        appendLine(network.bssid)
                    }
                    context.startActivity(
                        Intent.createChooser(Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text), "Share network")
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            )
        }
    }
}

@Composable
private fun BandSelector(band: FrequencyBand, counts: Map<FrequencyBand, Int>, onSelect: (FrequencyBand) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
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

@Composable
private fun ThrottleHint(throttle: ThrottleState, onUnlock: () -> Unit) {
    val text = (throttle as? ThrottleState.Exhausted)?.let { "Android paused scans for ${it.secondsUntilNextScan} s" }
        ?: "Android is limiting scans"
    Surface(
        onClick = onUnlock,
        shape = PulseWaveShapes.full,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier.padding(bottom = 6.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 2.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Bolt, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = onUnlock) { Text("Remove limit") }
        }
    }
}

@Composable
private fun ChannelAdvice(networks: List<WifiNetwork>, band: FrequencyBand, connected: NetworkStatus) {
    val scores = remember(networks, band, connected.bssid) {
        CongestionAnalyzer.analyze(networks.filter { band.contains(it.frequency) && !it.bssid.equals(connected.bssid, true) }, band)
    }
    val best = scores.firstOrNull { it.recommendation }
    val connectedHere = connected.isConnected && band.contains(connected.frequency)
    val sharing = if (connectedHere) networks.count {
        band.contains(it.frequency) && frequencyToChannel(it.frequency) == connected.channel && !it.bssid.equals(connected.bssid, true)
    } else 0
    val message = when {
        connectedHere && best != null && best.channelNumber == connected.channel ->
            "Your network is already on channel ${connected.channel}, the clearest choice nearby."
        connectedHere && best != null ->
            "Your network is on channel ${connected.channel}" +
                (if (sharing > 0) ", shared with $sharing other network${if (sharing == 1) "" else "s"}" else "") +
                ". Channel ${best.channelNumber} looks clearer; you can switch in your router's settings."
        best != null -> "Channel ${best.channelNumber} is the clearest on ${band.label} right now."
        band == FrequencyBand.BAND_2_4 -> null
        else -> "On ${band.label}, walls block neighbors quickly, so signal strength matters more than overlap."
    } ?: return
    Card(
        shape = PulseWaveShapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer, contentColor = MaterialTheme.colorScheme.onTertiaryContainer),
        modifier = Modifier.padding(top = 6.dp)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Lightbulb, null)
            Spacer(Modifier.width(16.dp))
            Text(message, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

private fun barsIcon(quality: SignalQuality): ImageVector = when (quality) {
    SignalQuality.Excellent -> Icons.Rounded.SignalWifi4Bar
    SignalQuality.Good -> Icons.Rounded.NetworkWifi3Bar
    SignalQuality.Fair -> Icons.Rounded.NetworkWifi2Bar
    SignalQuality.Weak -> Icons.Rounded.NetworkWifi1Bar
    SignalQuality.DeadZone -> Icons.Rounded.SignalWifi0Bar
}

@Composable
private fun NetworkRow(
    network: WifiNetwork,
    index: Int,
    count: Int,
    isConnected: Boolean,
    isSelected: Boolean,
    chartColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
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
        modifier = modifier,
        leadingContent = { ShapeIcon(barsIcon(quality), quality.polygon(), colors.container, colors.onContainer, size = 48.dp, iconSize = 24.dp) },
        supportingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (secured && protocol.isNotEmpty() && protocol != "Open") {
                    Icon(Icons.Rounded.Lock, "Secured", Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                }
                Text(
                    listOfNotNull(
                        vendor,
                        "Ch ${frequencyToChannel(network.frequency)}",
                        if (network.channelWidth > 20) "${network.channelWidth} MHz" else null,
                        when (protocol) { "" -> null; "Open" -> "No password"; else -> protocol }
                    ).joinToString(" · "),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${network.rssi}",
                    style = MaterialTheme.typography.titleLarge.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.semantics { contentDescription = "${network.rssi} dBm" }
                )
                Text(quality.label, style = MaterialTheme.typography.labelSmall, color = scheme.onSurfaceVariant)
            }
        }
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

@Composable
fun GraphLegendDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reading the graph") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LegendItem("Height", "Taller means stronger. Around -50 dBm is excellent; below -80 dBm is weak.")
                LegendItem("Position", "Each hill sits on its channel. On 2.4 GHz only channels 1, 6 and 11 don't overlap.")
                LegendItem("Width", "Wider hills use more spectrum (40, 80 or 160 MHz): faster, but they overlap more neighbors.")
                LegendItem("Signal over time", "Shows how steady each network has been. A jumpy line means an unstable link.")
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Got it") } }
    )
}

@Composable
private fun LegendItem(title: String, description: String) {
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
