package com.pulsewave.app.feature.analyze

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.wifi.WifiNetwork
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiDetailScreen(
    network: WifiNetwork,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(network.ssid.ifEmpty { "Hidden Network" }) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {}) {
                        Icon(Icons.Default.Share, "Share")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Signal Meter
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "${network.rssi} dBm",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            when {
                                network.rssi >= -50 -> "Excellent Signal"
                                network.rssi >= -60 -> "Good Signal"
                                network.rssi >= -70 -> "Fair Signal"
                                else -> "Poor Signal"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Icon(
                        Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Details Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Network Details", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    DetailRow("BSSID", network.bssid)
                    DetailRow("Frequency", "${network.frequency} MHz")
                    DetailRow("Channel Width", "${network.channelWidth} MHz")
                    DetailRow("Capabilities", network.capabilities.take(50) + "...")
                }
            }

            // Estimated Distance
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Estimated Distance", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    val distance = estimateDistance(network.rssi, network.frequency)
                    Text(
                        "~${distance} meters",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Note: This is an approximation based on signal strength. " +
                        "Actual distance varies with obstacles and environment.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Star, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Favorite")
                }
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Track")
                }
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

// Free Space Path Loss distance estimation
fun estimateDistance(rssi: Int, frequencyMhz: Int): Double {
    // FSPL formula: d = 10 ^ ((27.55 - (20 * log10(f)) + |RSSI|) / 20)
    // Simplified for indoor use
    val txPower = -40 // Assumed transmit power at 1 meter
    val n = 3.0 // Path loss exponent (indoor)
    return 10.0.pow((txPower - rssi) / (10 * n))
}
