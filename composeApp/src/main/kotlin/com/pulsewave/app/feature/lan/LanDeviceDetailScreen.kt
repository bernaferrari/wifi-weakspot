package com.pulsewave.app.feature.lan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import java.text.DateFormat
import java.util.Date

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
    val device by viewModel.device.collectAsState()
    val scanTime by viewModel.portScanTime.collectAsState()
    val error by viewModel.error.collectAsState()
    var showRename by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(ip) { viewModel.loadDevice(ip) }
    DisposableEffect(lifecycle, ip) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadDevice(ip)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    Scaffold(topBar = {
        TopAppBar(title = { Text(device?.displayName ?: ip) }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        })
    }) { padding ->
        LazyColumn(modifier.padding(padding).fillMaxSize(), contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val current = device
            if (current == null) {
                item { CircularProgressIndicator() }
            } else {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(current.displayName, style = MaterialTheme.typography.headlineSmall)
                            Text(if (current.lastSeen == 0L) "Device not in scan history" else if (current.isOnline)
                                "Replied to last scan" else "Cached neighbor · reachability unconfirmed")
                            Text("IP: ${current.ipAddress}")
                            Text("MAC: ${current.macAddress ?: "Unavailable"}")
                            current.vendor?.let { Text("Vendor: $it") }
                            if (current.lastSeen > 0L) Text("Last discovered: ${DateFormat.getDateTimeInstance().format(Date(current.lastSeen))}")
                        }
                    }
                }
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onPing(ip) }, modifier = Modifier.weight(1f)) { Text("Ping") }
                        FilledTonalButton(onClick = { onPortScan(ip) }, modifier = Modifier.weight(1f)) { Text("Scan ports") }
                    }
                }
                item {
                    OutlinedButton(onClick = { name = current.customName.orEmpty(); showRename = true }, enabled = current.lastSeen > 0L) { Text("Rename device") }
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                }
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Last common-port scan", style = MaterialTheme.typography.titleMedium)
                            if (scanTime == null) Text("Not scanned. Run a port scan to check common TCP services.")
                            else {
                                Text(DateFormat.getDateTimeInstance().format(Date(scanTime!!)))
                                Text(if (current.openPorts.isEmpty()) "No open ports found among the common ports tested."
                                    else "Open TCP ports: ${current.openPorts.joinToString()}")
                                Text("A failed connection can mean a closed, filtered, or unreachable port.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
    if (showRename) AlertDialog(onDismissRequest = { showRename = false }, title = { Text("Rename device") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it.take(80) }, label = { Text("Device name") }, supportingText = { Text("Leave blank to use the discovered name.") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { viewModel.rename(name); showRename = false }) { Text("Save") } },
        dismissButton = { TextButton(onClick = { showRename = false }) { Text("Cancel") } })
}
