package com.pulsewave.app.feature.lan

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

import com.pulsewave.app.core.model.lan.LanDevice

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanDeviceDetailScreen(
    ip: String,
    onBack: () -> Unit,
    onPing: (String) -> Unit = {},
    onPortScan: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LanDeviceDetailViewModel = org.koin.compose.viewmodel.koinViewModel()
) {
    LaunchedEffect(ip) {
        viewModel.loadDevice(ip)
    }
    
    val deviceState by viewModel.device.collectAsState()
    val device = deviceState ?: LanDevice(
        ipAddress = ip,
        macAddress = null,
        hostname = null,
        vendor = null,
        lastSeen = System.currentTimeMillis(),
        isOnline = true,
        isTrusted = false,
        customName = null,
        openPorts = emptyList()
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device.hostname ?: device.ipAddress) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier.padding(padding).fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (device.isOnline) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(24.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            if (device.isOnline) "Online" else "Offline",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(device.ipAddress, style = MaterialTheme.typography.bodyLarge)
                    }
                    Icon(
                        if (device.isOnline) Icons.Default.CheckCircle else Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = if (device.isOnline) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
            }

            // Device Info
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Device Information", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    InfoRow("IP Address", device.ipAddress)
                    InfoRow("Hostname", device.hostname ?: "Unknown")
                    InfoRow("Vendor", device.vendor ?: "Unknown")
                    InfoRow("MAC Address", device.macAddress ?: "Unknown")
                    InfoRow("Last Seen", "Just now")
                }
            }

            // Actions
            Text("Actions", style = MaterialTheme.typography.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionCard(
                    icon = Icons.Default.Search,
                    title = "Ping",
                    onClick = { onPing(device.ipAddress) },
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    icon = Icons.Default.Lock,
                    title = "Port Scan",
                    onClick = { onPortScan(device.ipAddress) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ActionCard(
                    icon = Icons.Default.Share,
                    title = "Wake on LAN",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
                ActionCard(
                    icon = Icons.Default.Edit,
                    title = "Rename",
                    onClick = {},
                    modifier = Modifier.weight(1f)
                )
            }

            // Open Ports (if any)
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Open Ports", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        AssistChip(onClick = {}, label = { Text("22 (SSH)") })
                        AssistChip(onClick = {}, label = { Text("80 (HTTP)") })
                        AssistChip(onClick = {}, label = { Text("443 (HTTPS)") })
                    }
                }
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.height(4.dp))
            Text(title, style = MaterialTheme.typography.labelMedium)
        }
    }
}
