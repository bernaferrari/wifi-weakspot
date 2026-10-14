@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.history

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.saveable.rememberSaveable
import com.pulsewave.app.core.tools.decodePortScan
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import com.pulsewave.app.core.database.dao.PulseWaveDao
import kotlinx.coroutines.flow.map

data class HistoryItem(
    val id: Long,
    val type: String,
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val timestampMillis: Long,
    val details: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    dao: PulseWaveDao = org.koin.compose.koinInject(),
    onBack: (() -> Unit)? = null
) {
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var selectedItem by remember { mutableStateOf<HistoryItem?>(null) }
    val tabs = listOf("Wi-Fi", "LAN", "Tools", "Speed")
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    val wifiHistory by remember(dao) {
        dao.getRecentWifiScans().map { scans -> 
            scans.map { 
                HistoryItem(it.id, "WiFi", it.ssid.ifBlank { "Hidden" }, "${it.bssid} · ${it.rssi} dBm", formatDate(it.timestamp), it.timestamp, "Network: ${it.ssid.ifBlank { "Hidden" }}\nAccess point: ${it.bssid}\nObserved: ${formatDate(it.timestamp)}\nSignal: ${it.rssi} dBm\nFrequency: ${it.frequency} MHz\nWidth: ${it.channelWidth} MHz\nSecurity: ${it.security}")
            } 
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())
        
    val lanHistory by remember(dao) {
        dao.getAllLanDevices().map { devices ->
            devices.map {
                HistoryItem(it.ipAddress.hashCode().toLong(), "LAN", it.customName ?: it.hostname ?: it.ipAddress, it.ipAddress, formatDate(it.lastSeen), it.lastSeen, "Address: ${it.ipAddress}\nName: ${it.customName ?: it.hostname ?: "Unknown"}\nMAC: ${it.macAddress ?: "Unavailable"}\nVendor: ${it.vendor ?: "Unknown"}\nObserved: ${formatDate(it.lastSeen)}\nReachability at last scan: ${if (it.isOnline) "Replied" else "Unconfirmed"}")
            }
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())
        
    val toolHistory by remember(dao) {
        dao.getToolHistory().map { results ->
            results.map {
                HistoryItem(it.id, "Tools", "${it.toolType} ${it.target}", if(it.isSuccess) "Completed · Tap to inspect" else "Failed · Tap to inspect", formatDate(it.timestamp), it.timestamp, if (it.toolType == "PORT_SCAN") decodePortScan(it.resultJson).joinToString("\n") { port -> "TCP ${port.first}: ${port.second}" }.ifEmpty { "No open ports found among the tested common TCP ports." } else it.resultJson)
            }
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val speedHistory by remember(dao) {
        dao.getSpeedTestHistory().map { results ->
            results.map {
                val upload = if (it.uploadMbps < 0f) "Not measured" else "%.1f Mbps".format(it.uploadMbps)
                val prefix = com.pulsewave.app.feature.linktest.LinkTestViewModel.ROUTER_PREFIX
                if (it.serverName.startsWith(prefix)) {
                    val router = it.serverName.removePrefix(prefix)
                    val response = if (it.pingMs < 0f) "No reply" else "%.1f ms".format(it.pingMs)
                    HistoryItem(it.id, "Speed",
                        if (it.uploadMbps < 0f) "Wi-Fi test · $response" else "%.0f Mbps to router".format(it.uploadMbps),
                        "Router $router · $response", formatDate(it.timestamp), it.timestamp,
                        "Wi-Fi speed test (phone to router)\nRouter: $router\nObserved: ${formatDate(it.timestamp)}\nUpload to router: $upload\nResponse time: $response\nSteadiness: %.1f ms".format(it.jitterMs))
                } else {
                    val download = "%.1f".format(it.downloadMbps)
                    HistoryItem(it.id, "Speed", "${download} Mbps download", it.serverName, formatDate(it.timestamp), it.timestamp,
                        "Server: ${it.serverName}\nObserved: ${formatDate(it.timestamp)}\nDownload: $download Mbps\nUpload: $upload\nHTTP latency: %.1f ms\nHTTP latency variation: %.1f ms".format(it.pingMs, it.jitterMs))
                }
            }
        }
    }.collectAsStateWithLifecycle(initialValue = emptyList())

    val combinedHistory = remember(selectedTab, wifiHistory, lanHistory, toolHistory, speedHistory) {
        val all = when (selectedTab) {
            0 -> wifiHistory
            1 -> lanHistory
            2 -> toolHistory
            3 -> speedHistory
            else -> emptyList()
        }
        all.sortedByDescending { it.timestampMillis }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("History") },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Expressive Tab Row with pill-style indicator
            PrimaryTabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { 
                            Text(
                                title, 
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if(selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            ) 
                        }
                    )
                }
            }

            // Content
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (combinedHistory.isEmpty()) {
                    item {
                        AnimatedEmptyHistoryState()
                    }
                } else {
                    itemsIndexed(combinedHistory, key = { _, item -> "${item.type}-${item.id}" }) { index, item ->
                        HistoryCard(item = item, onClick = { selectedItem = item })
                    }
                }
            }
        }
    }
    selectedItem?.let { item ->
        AlertDialog(
            onDismissRequest = { selectedItem = null },
            title = { Text(item.title) },
            text = {
                SelectionContainer {
                    Column(Modifier.heightIn(max = 440.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(item.timestamp, style = MaterialTheme.typography.labelLarge)
                        Text(item.details.ifEmpty { item.subtitle }, style = MaterialTheme.typography.bodyMedium)
                        Text("This is a saved observation. Run a new test to check the current connection.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedItem = null }) { Text("Done") } }
        )
    }
}

@Composable
fun AnimatedEmptyHistoryState() {
    com.pulsewave.app.core.designsystem.component.EmptyStateWithIllustration(
        illustration = { com.pulsewave.app.core.designsystem.component.HistoryIllustration() },
        title = "Nothing saved yet",
        subtitle = "Scans, speed tests and tool results are saved here automatically."
    )
}

@Composable
fun HistoryCard(item: HistoryItem, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Card(
        onClick = onClick,
        shape = PulseWaveShapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconContainerColor = when (item.type) {
                "WiFi" -> MaterialTheme.colorScheme.primaryContainer
                "LAN" -> MaterialTheme.colorScheme.secondaryContainer
                else -> MaterialTheme.colorScheme.tertiaryContainer
            }
            val iconTint = when (item.type) {
                "WiFi" -> MaterialTheme.colorScheme.onPrimaryContainer
                "LAN" -> MaterialTheme.colorScheme.onSecondaryContainer
                else -> MaterialTheme.colorScheme.onTertiaryContainer
            }
            
            Surface(
                shape = PulseWaveShapes.medium,
                color = iconContainerColor,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        when (item.type) {
                            "WiFi" -> Icons.Default.Search
                            "LAN" -> Icons.AutoMirrored.Filled.List
                            "Speed" -> Icons.Default.PlayArrow
                            else -> Icons.Default.Build
                        },
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = iconTint
                    )
                }
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.title,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    item.subtitle,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    item.timestamp,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}
