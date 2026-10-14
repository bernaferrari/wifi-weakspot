@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.pulsewave.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AboutScreen(onBack: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("About PulseWave") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Understand your connection", style = MaterialTheme.typography.headlineMedium)
            Text("PulseWave combines nearby Wi-Fi observations, coverage surveys, LAN discovery, and network diagnostics. Version 1.0 · Apache License 2.0.")
            Text("Your measurements", style = MaterialTheme.typography.titleLarge)
            Text("Scan history, device names, and surveys stay in the app's local database. Exports are shared only when you choose to share them. Surveys store relative points, not GPS coordinates.")
            Text("What each test measures", style = MaterialTheme.typography.titleLarge)
            Text("Wi-Fi signal is measured in dBm. Channel overlap is an estimate based on visible access points, not traffic or airtime. Distance from signal is approximate and affected by obstacles and router power.")
            Text("LAN discovery can miss devices that block probes or are isolated by the router. A cached neighbor is not proof that a device is reachable. Port scans report TCP connections to the tested ports only.")
            Text("Speed tests transfer data to public test servers and can use substantial data. HTTP latency includes the server's response time. Results describe the current route to the selected server, including mobile data or a VPN when active.")
            Text("Android scan limits", style = MaterialTheme.typography.titleLarge)
            Text("Android limits how often apps can request Wi-Fi scans. Cached values and unavailable readings should not be treated as new measurements. Wi-Fi scans require Location services and the scan permission.")
        }
    }
}
