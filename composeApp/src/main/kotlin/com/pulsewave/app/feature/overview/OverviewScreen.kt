@file:OptIn(ExperimentalMaterial3Api::class)

package com.pulsewave.app.feature.overview

import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.component.LinearWavyProgressIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverviewScreen(
    modifier: Modifier = Modifier,
    onNavigateToWifi: () -> Unit = {},
    onNavigateToLan: () -> Unit = {},
    onNavigateToSpeedTest: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    val viewModel: OverviewViewModel = org.koin.compose.viewmodel.koinViewModel()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val airHealth by viewModel.airHealth.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        "PulseWave",
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp), // Common horizontal padding
            verticalArrangement = Arrangement.spacedBy(16.dp), // Tighter spacing for connected feel
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            
            // STATUS CARD
            item {
                ConnectionStatusCard(networkStatus)
            }

            // TOOLS HEADER
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Tools",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // EXPRESSIVE GRID (Simulating ButtonGroup/Masonry)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExpressiveActionCard(
                            title = "WiFi Scan",
                            subtitle = "Analyze",
                            icon = Icons.Outlined.Search,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            onClick = onNavigateToWifi
                        )
                        ExpressiveActionCard(
                            title = "LAN Scan",
                            subtitle = "Devices",
                            icon = Icons.Outlined.Devices,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            onClick = onNavigateToLan
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ExpressiveActionCard(
                            title = "Speed",
                            subtitle = "Test",
                            icon = Icons.Default.PlayArrow,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            onClick = onNavigateToSpeedTest
                        )
                        ExpressiveActionCard(
                            title = "History",
                            subtitle = "Logs",
                            icon = Icons.Outlined.History,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            onClick = onNavigateToHistory
                        )
                    }
                }
            }

            // HEALTH & STATS
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Network Health",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            item {
                HealthScoreCard(airHealth)
            }
        }
    }
}

@Composable
fun ConnectionStatusCard(status: com.pulsewave.app.core.model.NetworkStatus) {
    // "Hero" Card with gradient
    val brush = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
        )
    )

    Card(
        shape = RoundedCornerShape(32.dp), // Extra Large
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent) // Use Box for gradient
    ) {
        Box(
            modifier = Modifier
                .background(brush)
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (status.isConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        contentColor = if (status.isConnected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError,
                        shape = RoundedCornerShape(100),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
                            Text(
                                if (status.isConnected) "Connected" else "Disconnected",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                    
                    Icon(
                        Icons.Default.Wifi,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                
                Spacer(Modifier.height(32.dp))
                
                Text(
                    if (status.isConnected) status.ssid else "No Network",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(Modifier.height(24.dp))
                
                if (status.isConnected) {
                    // Chips Row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val freqLabel = if (status.frequency > 5000) "5 GHz" else "2.4 GHz"
                        MetadataChip(freqLabel, MaterialTheme.colorScheme.surface)
                        MetadataChip("Ch ${status.channel}", MaterialTheme.colorScheme.surface)
                        MetadataChip("${status.rssi} dBm", MaterialTheme.colorScheme.surface)
                    }
                }
            }
        }
    }
}

@Composable
fun MetadataChip(text: String, containerColor: Color) {
    Surface(
        color = containerColor.copy(alpha = 0.8f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(32.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 12.dp)) {
            Text(
                text,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ExpressiveActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Elastic Scale Animation
    // Use MotionScheme tokens for "Spatial" (transform) animations
    // Typically: MaterialTheme.motionScheme.fastSpatial() or similar
    // Note: Since MotionScheme is experimental and tokens might be tricky to access directly without lookups,
    // we use the recommended physics spring but derived from the theme logic if possible, 
    // OR we use the new `animateScaleAsState` if available in M3 Expressive.
    // For now, let's use the explicit Spring which matches the "Expressive" feel, 
    // as direct token access (MotionScheme.expressive().spatial...) might be internal or complex.
    // user said "feel free to google it" -> Google results say "MaterialTheme.motionScheme"
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f)
    )

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(160.dp) // Tall, vertical rhythm
            .scale(scale),
        shape = RoundedCornerShape(28.dp), // Expressive Large
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier.padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.1f), // Subtle backing for icon
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Column {
                Text(
                    title,
                    style = MaterialTheme.typography.headlineSmall, // Big Font
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun HealthScoreCard(score: Int) {
    val healthLabel = when {
        score >= 90 -> "Excellent"
        score >= 70 -> "Good"
        score >= 50 -> "Fair"
        else -> "Poor"
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("AirHealth", style = MaterialTheme.typography.titleMedium)
                    Text(
                        healthLabel,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Score Circle
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "$score",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Wavy Progress!
            Text("Signal Stability", style = MaterialTheme.typography.labelSmall)
            Spacer(Modifier.height(8.dp))
            LinearWavyProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                amplitude = 3.dp,
                wavelength = 24.dp
            )
        }
    }
}


