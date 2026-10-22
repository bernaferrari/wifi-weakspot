@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.lan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.designsystem.component.EmptyStateWithIllustration
import com.pulsewave.app.core.designsystem.component.LanDeviceIllustration
import com.pulsewave.app.core.designsystem.component.segmentedShape
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import com.pulsewave.app.core.lan.DeviceIdentity
import com.pulsewave.app.core.lan.DeviceKind
import com.pulsewave.app.core.lan.identify
import com.pulsewave.app.core.model.lan.LanDevice
import org.koin.compose.viewmodel.koinViewModel

private class ShownDevice(val device: LanDevice, val identity: DeviceIdentity)

@Composable
fun LanScreen(
    modifier: Modifier = Modifier,
    viewModel: LanViewModel = koinViewModel(),
    onDeviceClick: (String) -> Unit = {},
    onBack: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val known by viewModel.known.collectAsStateWithLifecycle()
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val haptics = rememberPulseWaveHapticFeedback()

    LaunchedEffect(Unit) { viewModel.startScan() }

    val isScanning = uiState is LanUiState.Scanning
    val devices = (uiState as? LanUiState.Success)?.devices ?: known
    val gateway = status.gateway.takeIf { it.isNotBlank() }
    val phoneIp = status.ipAddress.takeIf { it.isNotBlank() }
    val shown = remember(devices, gateway, phoneIp) {
        devices.map { ShownDevice(it, it.identify(gateway, phoneIp)) }
            .sortedWith(compareBy<ShownDevice> { it.identity.kind.ordinal }.thenBy { it.identity.kind == DeviceKind.Unknown }.thenBy { it.identity.name.lowercase() })
    }
    val home = shown.filter { it.identity.kind == DeviceKind.Router || it.identity.kind == DeviceKind.ThisPhone }
    // Devices that answered but shared nothing about themselves fold away under one row.
    val quiet = shown.filter { it.identity.kind == DeviceKind.Unknown && it.device.customName == null && it.device.announcedName == null && it.device.hostname == null && it.device.vendor == null }
    val others = shown - home.toSet() - quiet.toSet()
    var showQuiet by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Devices") },
                subtitle = {
                    Text(
                        when {
                            isScanning && shown.isEmpty() -> "Looking for devices…"
                            shown.isEmpty() -> "Find devices on your Wi-Fi"
                            status.ssid.isNotBlank() -> "${shown.size} on ${status.ssid}"
                            else -> "${shown.size} on your network"
                        }
                    )
                },
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = { haptics.light(); onBack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { haptics.click(); if (isScanning) viewModel.stopScan() else viewModel.startScan() }) {
                        Icon(
                            if (isScanning) Icons.Rounded.Stop else Icons.Rounded.Refresh,
                            contentDescription = if (isScanning) "Stop looking" else "Look again"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        PullToRefreshBox(
            // The progress row below shows the scan; the pull indicator would duplicate it.
            isRefreshing = false,
            onRefresh = { haptics.click(); viewModel.startScan() },
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 32.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item(key = "status") {
                    ScanStatus(
                        scanning = isScanning,
                        progress = progress,
                        modifier = Modifier.animateItem().padding(bottom = 8.dp)
                    )
                }
                val error = uiState as? LanUiState.Error
                when {
                    error != null -> item(key = "error") {
                        ProblemCard("Couldn't look for devices", error.message, onRetry = viewModel::startScan)
                    }
                    shown.isEmpty() && !isScanning -> item(key = "empty") {
                        EmptyStateWithIllustration(
                            illustration = { LanDeviceIllustration() },
                            title = if (uiState is LanUiState.Cancelled) "Stopped looking" else "No devices found",
                            subtitle = "Some devices stay quiet when asked. Try again in a moment, or check that this phone is on your home Wi-Fi.",
                            modifier = Modifier.padding(top = 32.dp)
                        )
                    }
                }
                deviceGroup("Your network", home, isScanning, onDeviceClick = { haptics.click(); onDeviceClick(it) })
                deviceGroup(
                    if (others.isEmpty()) null else "Other devices · ${others.size}",
                    others,
                    isScanning,
                    onDeviceClick = { haptics.click(); onDeviceClick(it) }
                )
                if (quiet.isNotEmpty()) {
                    item(key = "quiet-toggle") {
                        QuietToggle(
                            count = quiet.size,
                            expanded = showQuiet,
                            onToggle = { haptics.light(); showQuiet = !showQuiet },
                            modifier = Modifier.animateItem().padding(top = 16.dp)
                        )
                    }
                    if (showQuiet) deviceGroup(null, quiet, isScanning, onDeviceClick = { haptics.click(); onDeviceClick(it) })
                }
                if (shown.isNotEmpty()) {
                    item(key = "footer") {
                        Text(
                            "Some devices hide from scans, so a missing one may still be connected.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.animateItem().padding(start = 8.dp, end = 8.dp, top = 20.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun LazyListScope.deviceGroup(title: String?, devices: List<ShownDevice>, scanning: Boolean, onDeviceClick: (String) -> Unit) {
    if (devices.isEmpty()) return
    title?.let {
        item(key = "title-${devices.first().device.ipAddress}") {
            Text(
                it,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.animateItem().padding(start = 8.dp, top = 16.dp, bottom = 8.dp)
            )
        }
    }
    itemsIndexed(devices, key = { _, shown -> shown.device.ipAddress }) { index, shown ->
        DeviceRow(
            shown = shown,
            stale = scanning,
            onClick = { onDeviceClick(shown.device.ipAddress) },
            modifier = Modifier.animateItem().padding(top = if (index == 0) 0.dp else 2.dp),
            shape = segmentedShape(index, devices.size)
        )
    }
}

@Composable
private fun QuietToggle(count: Int, expanded: Boolean, onToggle: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onToggle,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth().padding(bottom = if (expanded) 8.dp else 0.dp)
    ) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(if (count == 1) "1 quiet device" else "$count quiet devices", style = MaterialTheme.typography.titleMedium)
                Text(
                    "They didn't say what they are",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                contentDescription = if (expanded) "Hide" else "Show"
            )
        }
    }
}

@Composable
private fun ScanStatus(scanning: Boolean, progress: Float, modifier: Modifier = Modifier) {
    if (!scanning) return
    Column(modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        LinearWavyProgressIndicator(progress = { progress.coerceIn(0.02f, 1f) }, modifier = Modifier.fillMaxWidth())
        Text(
            "Checking local addresses · ${(progress * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeviceRow(shown: ShownDevice, stale: Boolean, onClick: () -> Unit, shape: androidx.compose.ui.graphics.Shape, modifier: Modifier = Modifier) {
    val quiet = !shown.device.isOnline
    Surface(onClick = onClick, shape = shape, color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(start = 16.dp, end = 20.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DeviceIcon(shown.identity.kind, dimmed = quiet || stale)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(shown.identity.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    if (quiet) "${shown.identity.detail} · didn't answer" else shown.identity.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun ProblemCard(title: String, message: String, onRetry: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Button(onClick = onRetry, shapes = ButtonDefaults.shapes()) { Text("Try again") }
        }
    }
}
