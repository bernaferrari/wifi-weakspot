@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.lan

import com.bernaferrari.wifiweakspot.core.designsystem.exceptBottom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.OpenInBrowser
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bernaferrari.wifiweakspot.core.designsystem.component.segmentedShape
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind
import com.bernaferrari.wifiweakspot.core.lan.identify
import com.bernaferrari.wifiweakspot.core.lan.deviceWebUrl
import com.bernaferrari.wifiweakspot.core.platform.PlatformActions
import com.bernaferrari.wifiweakspot.core.i18n.strings
import com.bernaferrari.wifiweakspot.feature.tools.serviceLabel
import com.bernaferrari.wifiweakspot.feature.history.momentLabel
import org.koin.compose.koinInject

@Composable
fun LanDeviceDetailScreen(
    ip: String,
    onBack: () -> Unit,
    onPing: (String) -> Unit = {},
    onPortScan: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: LanDeviceDetailViewModel = org.koin.compose.viewmodel.koinViewModel()
) {
    val device by viewModel.device.collectAsStateWithLifecycle()
    val scanTime by viewModel.portScanTime.collectAsStateWithLifecycle()
    val ports by viewModel.ports.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()
    val status by viewModel.status.collectAsStateWithLifecycle()
    val platform: PlatformActions = koinInject()
    var showRename by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(ip) { viewModel.loadDevice(ip) }
    DisposableEffect(lifecycle, ip) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadDevice(ip)
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }
    val current = device
    val identity = current?.identify(status.gateway.takeIf { it.isNotBlank() }, status.ipAddress.takeIf { it.isNotBlank() })
    val scanned = current != null && current.lastSeen > 0L

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(identity?.name ?: ip) },
                subtitle = { Text(identity?.kind?.takeIf { it != com.bernaferrari.wifiweakspot.core.lan.DeviceKind.Unknown }?.label ?: ip) },
                scrollBehavior = scrollBehavior,
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.common.back) } },
                actions = {
                    if (scanned) IconButton(onClick = { showRename = true }) { Icon(Icons.Rounded.Edit, strings.common.rename) }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            )
        }
    ) { padding ->
        if (current == null || identity == null) return@Scaffold
        val webPage = deviceWebUrl(ip, ports.map { it.first }, router = identity.kind == DeviceKind.Router)
        val facts = listOfNotNull(
            strings.common.address to current.ipAddress,
            current.vendor?.let { strings.common.maker to it },
            current.hostname?.let { strings.tools.factNetworkName to it },
            strings.tools.factHardwareAddress to (current.macAddress ?: strings.tools.macHiddenByAndroid),
            current.customName?.let { strings.tools.factYourName to it }
        )
        val detailsTitle = strings.tools.detailsTitle
        val openPortsTitle = strings.common.openPorts
        val notCheckedYet = strings.tools.portsNotCheckedYet
        val noneOpen = scanTime?.let { strings.tools.portsNoneOpenWhenChecked(momentLabel(it)) }
        val checkedAt = scanTime?.let { strings.tools.portsCheckedAt(momentLabel(it)) }
        val portNames = ports.map { serviceLabel(it.second) }
        LazyColumn(
            Modifier.padding(padding.exceptBottom()).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = padding.calculateBottomPadding() + 32.dp)
        ) {
            item(key = "hero") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(start = 8.dp, bottom = 20.dp)
                ) {
                    DeviceIcon(identity.kind, size = 64.dp, dimmed = !current.isOnline)
                    Column(Modifier.weight(1f)) {
                        Text(
                            when {
                                !scanned -> strings.tools.deviceNotScannedYet
                                current.isOnline -> strings.tools.deviceAnsweredLastScan
                                else -> strings.tools.deviceSeenButNoAnswer
                            },
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (scanned) {
                            Text(momentLabel(current.lastSeen), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item(key = "actions") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                    Button(onClick = { onPing(ip) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.NetworkPing, null, Modifier.size(ButtonDefaults.IconSize))
                        Text("Ping", Modifier.padding(start = ButtonDefaults.IconSpacing))
                    }
                    FilledTonalButton(onClick = { onPortScan(ip) }, shapes = ButtonDefaults.shapes(), modifier = Modifier.weight(1f)) {
                        Icon(Icons.Rounded.Shield, null, Modifier.size(ButtonDefaults.IconSize))
                        Text(strings.tools.actionCheckPorts, Modifier.padding(start = ButtonDefaults.IconSpacing))
                    }
                }
            }
            if (webPage != null) {
                item(key = "web") {
                    FilledTonalButton(
                        onClick = { platform.openUrl(webPage) },
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    ) {
                        Icon(Icons.Rounded.OpenInBrowser, null, Modifier.size(ButtonDefaults.IconSize))
                        Text(
                            if (identity.kind == DeviceKind.Router) strings.tools.openRouterSettings else strings.tools.openItsWebPage,
                            Modifier.padding(start = ButtonDefaults.IconSpacing)
                        )
                    }
                }
            }
            error?.let { message ->
                item(key = "error") { Text(message, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(8.dp)) }
            }

            item(key = "facts-title") { SectionTitle(detailsTitle) }
            itemsIndexed(facts, key = { _, fact -> "fact-${fact.first}" }) { index, (label, value) ->
                FactRow(label, value, segmentedShape(index, facts.size, outer = 20.dp, inner = 4.dp), Modifier.padding(top = if (index == 0) 0.dp else 2.dp))
            }

            item(key = "ports-title") { SectionTitle(openPortsTitle) }
            if (scanTime == null) {
                item(key = "ports-none") {
                    Note(notCheckedYet)
                }
            } else if (ports.isEmpty()) {
                item(key = "ports-closed") { Note(noneOpen!!) }
            } else {
                itemsIndexed(ports, key = { _, port -> "port-${port.first}" }) { index, (port, service) ->
                    FactRow(portNames[index], "TCP $port", segmentedShape(index, ports.size, outer = 20.dp, inner = 4.dp), Modifier.padding(top = if (index == 0) 0.dp else 2.dp))
                }
                item(key = "ports-when") { Note(checkedAt!!) }
            }
        }
    }
    if (showRename && current != null) {
        var name by remember { mutableStateOf(current.customName.orEmpty()) }
        AlertDialog(
            onDismissRequest = { showRename = false },
            title = { Text(strings.tools.renameDeviceTitle) },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it.take(80) },
                    label = { Text(strings.common.nameLabel) },
                    placeholder = { Text(identity?.name.orEmpty()) },
                    supportingText = { Text(strings.tools.renameSupporting) },
                    singleLine = true
                )
            },
            confirmButton = { TextButton(onClick = { viewModel.rename(name); showRename = false }) { Text(strings.common.save) } },
            dismissButton = { TextButton(onClick = { showRename = false }) { Text(strings.common.cancel) } }
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
private fun Note(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

@Composable
private fun FactRow(label: String, value: String, shape: androidx.compose.ui.graphics.Shape, modifier: Modifier = Modifier) {
    Surface(shape = shape, color = MaterialTheme.colorScheme.surfaceContainer, modifier = modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
        }
    }
}
