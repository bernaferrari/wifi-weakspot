@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.history

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import kotlinx.coroutines.launch
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    dao: PulseWaveDao = org.koin.compose.koinInject(),
    onBack: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("WiFi", "LAN", "Tools")
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    val wifiHistory by dao.getRecentWifiScans()
        .map { scans -> 
            scans.map { 
                HistoryItem(it.id, "WiFi", it.ssid.ifBlank { "Hidden" }, "${it.bssid} • ${it.rssi}dBm", formatDate(it.timestamp))
            } 
        }.collectAsState(initial = emptyList())
        
    val lanHistory by dao.getAllLanDevices()
        .map { devices ->
            devices.map {
                HistoryItem(it.ipAddress.hashCode().toLong(), "LAN", it.hostname ?: it.ipAddress, it.macAddress ?: "Unknown MAC", formatDate(it.lastSeen))
            }
        }.collectAsState(initial = emptyList())
        
    val toolHistory by dao.getToolHistory()
        .map { results ->
            results.map {
                HistoryItem(it.id, "Tools", "${it.toolType} ${it.target}", if(it.isSuccess) "Success" else "Failed", formatDate(it.timestamp))
            }
        }.collectAsState(initial = emptyList())

    val combinedHistory = remember(selectedTab, wifiHistory, lanHistory, toolHistory) {
        val all = when (selectedTab) {
            0 -> wifiHistory
            1 -> lanHistory
            2 -> toolHistory
            else -> emptyList()
        }
        all.sortedByDescending { it.timestamp }
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("History & Logs") },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
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
                        // Staggered animation
                        val animatedAlpha = remember { Animatable(0f) }
                        val animatedOffset = remember { Animatable(30f) }
                        
                        LaunchedEffect(item.id) {
                            kotlinx.coroutines.delay(index * 40L)
                            launch {
                                animatedAlpha.animateTo(1f, animationSpec = tween(250))
                            }
                            launch {
                                animatedOffset.animateTo(0f, animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMediumLow
                                ))
                            }
                        }
                        
                        HistoryCard(
                            item = item,
                            modifier = Modifier.graphicsLayer {
                                alpha = animatedAlpha.value
                                translationY = animatedOffset.value
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedEmptyHistoryState() {
    com.pulsewave.app.core.designsystem.component.EmptyStateWithIllustration(
        illustration = { com.pulsewave.app.core.designsystem.component.HistoryIllustration() },
        title = "No history recorded",
        subtitle = "Scans and tool results will appear here"
    )
}

@Composable
fun HistoryCard(item: HistoryItem, modifier: Modifier = Modifier) {
    Card(
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
                            "LAN" -> Icons.Default.List
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
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    item.subtitle, 
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
