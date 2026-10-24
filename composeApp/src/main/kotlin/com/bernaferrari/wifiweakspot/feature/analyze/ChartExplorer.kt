@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.analyze

import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bernaferrari.wifiweakspot.core.designsystem.component.WifiBadge
import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import com.bernaferrari.wifiweakspot.core.wifi.frequencyToChannel

/**
 * The channel chart, as big as the screen allows. Drag across it and a cursor snaps from channel
 * to channel; underneath, every network using that channel, strongest first.
 */
@Composable
internal fun ChartExplorer(
    allNetworks: List<WifiNetwork>,
    band: FrequencyBand,
    onBandChange: (FrequencyBand) -> Unit,
    connectedBssid: String?,
    connectedFrequency: Int?,
    history: Map<String, List<Int>> = emptyMap(),
    showHistory: Boolean = false,
    onDismiss: () -> Unit
) {
    var here by remember { mutableStateOf<Pair<Int, List<WifiNetwork>>?>(null) }
    var showHelp by remember { mutableStateOf(false) }
    var detail by remember { mutableStateOf<WifiNetwork?>(null) }
    val scheme = MaterialTheme.colorScheme
    val networks = remember(allNetworks, band) { allNetworks.filter { band.contains(it.frequency) } }
    // Start where it matters: your own channel, or the strongest network when you're on another band.
    val connectedChannel = connectedFrequency?.takeIf { band.contains(it) }?.let(::frequencyToChannel)
        ?: networks.maxByOrNull { it.rssi }?.let { frequencyToChannel(it.frequency) }
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(Modifier.fillMaxSize(), color = scheme.surface) {
            Column(Modifier.fillMaxSize().safeDrawingPadding()) {
                TopAppBar(
                    // The band is the title: switch without leaving, and the Networks screen follows.
                    title = { CompactBandSelector(band) { here = null; onBandChange(it) } },
                    navigationIcon = { IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, "Close") } },
                    actions = { IconButton(onClick = { showHelp = true }) { Icon(Icons.Outlined.Info, "How to read the chart") } }
                )
                BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
                    // Landscape: chart on the left, the channel's networks beside it.
                    val sideBySide = maxWidth > maxHeight
                    if (showHistory) {
                        SignalHistoryGraph(history, networks, connectedBssid = connectedBssid, modifier = Modifier.fillMaxSize().padding(12.dp), allowVerticalScrubbing = true)
                        return@BoxWithConstraints
                    }
                    val chart = @Composable { modifier: Modifier ->
                        ChannelGraph(
                            networks = networks,
                            selectedBand = band,
                            connectedBssid = connectedBssid,
                            modifier = modifier,
                            onExplore = { channel, list -> here = channel to list },
                            initialChannel = connectedChannel
                        )
                    }
                    if (sideBySide) Row(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
                        chart(Modifier.weight(1.6f).fillMaxHeight().padding(bottom = 12.dp))
                        ChannelNetworks(here, Modifier.weight(1f).fillMaxHeight(), onOpen = { detail = it })
                    } else {
                        // Portrait: the chart keeps the whole screen; the channel's networks live in a
                        // sheet that peeks with its count and pulls up for the list.
                        val peek = 132.dp
                        val sheetMax = maxHeight * 0.6f
                        BottomSheetScaffold(
                            scaffoldState = rememberBottomSheetScaffoldState(rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)),
                            sheetPeekHeight = peek,
                            containerColor = scheme.surface,
                            sheetContainerColor = scheme.surfaceContainerLow,
                            sheetContent = { ChannelNetworks(here, Modifier.fillMaxWidth().height(sheetMax), onOpen = { detail = it }) }
                        ) {
                            chart(Modifier.fillMaxSize().padding(start = 12.dp, end = 12.dp, bottom = peek))
                        }
                    }
                }
            }
        }
        if (showHelp) ChartGuideSheet { showHelp = false }
        // Same details as tapping a network in the list on the Networks screen.
        detail?.let { network ->
            val settings: com.bernaferrari.wifiweakspot.core.datastore.SettingsRepository = org.koin.compose.koinInject()
            val favorites by settings.favoriteBssids.collectAsState(initial = emptySet())
            val scope = rememberCoroutineScope()
            ModalBottomSheet(onDismissRequest = { detail = null }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
                WifiDetailScreen(
                    network = network,
                    onBack = { detail = null },
                    isFavorite = network.bssid in favorites,
                    onToggleFavorite = { scope.launch { settings.toggleFavorite(network.bssid) } }
                )
            }
        }
    }
}

@Composable
private fun ChannelNetworks(here: Pair<Int, List<WifiNetwork>>?, modifier: Modifier = Modifier, onOpen: (WifiNetwork) -> Unit = {}) {
    val display = com.bernaferrari.wifiweakspot.core.designsystem.LocalSignalDisplay.current
    val (channel, list) = here ?: (0 to emptyList())
    LazyColumn(modifier, contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
        item(key = "header") {
            Text(
                when (list.size) { 0 -> "Channel $channel is clear"; 1 -> "1 network overlaps channel $channel"; else -> "${list.size} networks overlap channel $channel" },
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )
        }
        itemsIndexed(list, key = { _, n -> n.bssid }) { index, network ->
            SegmentedListItem(
                onClick = { onOpen(network) },
                trailingContent = { Icon(androidx.compose.material.icons.Icons.AutoMirrored.Rounded.KeyboardArrowRight, null) },
                shapes = ListItemDefaults.segmentedShapes(index = index, count = list.size),
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer, disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer, disabledContentColor = MaterialTheme.colorScheme.onSurface),
                leadingContent = { WifiBadge(network.rssi, size = 40.dp) },
                supportingContent = {
                    val home = frequencyToChannel(network.frequency)
                    // Wide networks reach this channel from another one; say where they actually sit.
                    Text(if (home == channel) "${com.bernaferrari.wifiweakspot.core.wifi.SignalQuality.of(network.rssi).label} · ${display.reading(network.rssi)}" else "From channel $home · ${display.reading(network.rssi)}")
                },
                modifier = Modifier.animateItem()
            ) { Text(network.ssid.ifEmpty { "Hidden network" }, maxLines = 1, overflow = TextOverflow.Ellipsis) }
        }
    }
}
