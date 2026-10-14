@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.pulsewave.app.feature.overview

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material.icons.rounded.WifiFind
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.datastore.SettingsRepository
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.component.ShapeIcon
import com.pulsewave.app.core.designsystem.component.SignalMorphBlob
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.permissions.PermissionGate
import com.pulsewave.app.core.rooms.CoverageSummary
import com.pulsewave.app.core.util.relativeTime
import com.pulsewave.app.core.wifi.SignalQuality
import com.pulsewave.app.core.wifi.bandLabel
import com.pulsewave.app.core.wifi.rememberRadioState
import com.pulsewave.app.core.designsystem.component.InfoPill
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OverviewScreen(
    modifier: Modifier = Modifier,
    onNavigateToRooms: () -> Unit = {},
    onNavigateToWifi: () -> Unit = {},
    onNavigateToLan: () -> Unit = {},
    onNavigateToSpeedTest: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToScanSetup: () -> Unit = {},
    onRequestScanPermission: () -> Unit = {}
) {
    val viewModel: OverviewViewModel = koinViewModel()
    val status by viewModel.networkStatus.collectAsStateWithLifecycle()
    val latestSpeed by viewModel.latestSpeedTest.collectAsStateWithLifecycle()
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val permissionGate: PermissionGate = koinInject()
    val permissions by permissionGate.state.collectAsStateWithLifecycle()
    val settings: SettingsRepository = koinInject()
    val radios = rememberRadioState()
    val haptics = rememberPulseWaveHapticFeedback()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text("PulseWave", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) { Icon(Icons.Rounded.Settings, "Settings") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 32.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "hero") {
                ConnectionHero(
                    status = status,
                    wifiEnabled = radios.wifiEnabled,
                    onOpenWifiSettings = {
                        context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }
                )
            }

            item(key = "rooms") {
                FindWeakSpotsCard(rooms = rooms, onClick = { haptics.click(); onNavigateToRooms() })
            }

            val needsPermission = !permissions.canScanWifi
            val needsLocation = permissions.canScanWifi && !radios.locationEnabled
            if (needsPermission || needsLocation) {
                item(key = "setup") {
                    SetupCard(
                        icon = if (needsPermission) Icons.Rounded.WifiFind else Icons.Rounded.LocationOn,
                        title = if (needsPermission) "See every network around you" else "Turn on Location",
                        body = if (needsPermission) {
                            "Android shares nearby Wi-Fi names only with apps that have location access. PulseWave never records where you are."
                        } else {
                            "Android needs Location on to list nearby Wi-Fi. PulseWave doesn't use GPS."
                        },
                        action = if (needsPermission) "Allow" else "Turn on",
                        onAction = {
                            haptics.click()
                            if (needsPermission) onRequestScanPermission()
                            else context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        }
                    )
                }
            }

            item(key = "toolsHeader") {
                Text(
                    "Quick checks",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp).semantics { heading() }
                )
            }

            item(key = "tiles") {
                val scheme = MaterialTheme.colorScheme
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickTile(
                            title = "Wi-Fi speed",
                            subtitle = latestSpeed?.takeIf { it.uploadMbps >= 0f }
                                ?.let { "%.0f Mbps · %s".format(it.uploadMbps, relativeTime(it.timestamp)) }
                                ?: "Phone to router, no internet needed",
                            icon = Icons.Rounded.Speed,
                            shape = MaterialShapes.Sunny,
                            container = scheme.primaryContainer,
                            content = scheme.onPrimaryContainer,
                            onClick = onNavigateToSpeedTest,
                            modifier = Modifier.weight(1f)
                        )
                        QuickTile(
                            title = "Nearby Wi-Fi",
                            subtitle = "Channels and neighbors",
                            icon = Icons.Rounded.Radar,
                            shape = MaterialShapes.Cookie7Sided,
                            container = scheme.secondaryContainer,
                            content = scheme.onSecondaryContainer,
                            onClick = onNavigateToWifi,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        QuickTile(
                            title = "Devices",
                            subtitle = "Who's on your network",
                            icon = Icons.Rounded.Devices,
                            shape = MaterialShapes.Clover8Leaf,
                            container = scheme.tertiaryContainer,
                            content = scheme.onTertiaryContainer,
                            onClick = onNavigateToLan,
                            modifier = Modifier.weight(1f)
                        )
                        QuickTile(
                            title = "History",
                            subtitle = "Past tests and scans",
                            icon = Icons.Rounded.History,
                            shape = MaterialShapes.Puffy,
                            container = scheme.surfaceContainerHighest,
                            content = scheme.onSurface,
                            onClick = onNavigateToHistory,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            if (status.isConnected && !status.internetValidated) {
                item(key = "advice") {
                    val advice = connectionAdvice(status)
                    Card(
                        shape = PulseWaveShapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(advice.title, style = MaterialTheme.typography.titleMedium)
                            Text(advice.explanation, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionHero(status: NetworkStatus, wifiEnabled: Boolean, onOpenWifiSettings: () -> Unit) {
    val quality = if (status.hasSignalMeasurement) SignalQuality.of(status.rssi) else null
    val colors = signalColors(quality ?: SignalQuality.DeadZone)
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = PulseWaveShapes.extraExtraLarge,
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow)
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SignalMorphBlob(
                    quality = quality,
                    color = if (quality != null) colors.container else scheme.surfaceContainerHighest,
                    size = 116.dp,
                    spinning = quality != null
                ) {
                    if (quality != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "${status.rssi}",
                                style = MaterialTheme.typography.headlineLarge.copy(fontFeatureSettings = "tnum"),
                                fontWeight = FontWeight.Black,
                                color = colors.onContainer
                            )
                            Text("dBm", style = MaterialTheme.typography.labelMedium, color = colors.onContainer)
                        }
                    } else {
                        Icon(Icons.Rounded.WifiOff, null, Modifier.size(40.dp), tint = scheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.width(20.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        when {
                            quality != null -> quality.headline
                            !wifiEnabled -> "Wi-Fi is off"
                            else -> "Not on Wi-Fi"
                        },
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (status.isConnected) status.ssid else "Connect to the network you want to check",
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (status.isConnected) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (status.internetValidated) signalColors(SignalQuality.Excellent).accent else scheme.error,
                                modifier = Modifier.size(8.dp)
                            ) {}
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (status.internetValidated) "Online" else "No internet access",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }
            if (quality != null) {
                Spacer(Modifier.height(16.dp))
                Text(quality.explanation, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    InfoPill(bandLabel(status.frequency))
                    if (status.channel > 0) InfoPill("Channel ${status.channel}")
                    if (status.linkSpeed > 0) InfoPill("${status.linkSpeed} Mbps link")
                    if (status.security.isNotBlank()) InfoPill(if (status.security == "Open") "No password" else status.security)
                }
            } else if (!status.isConnected) {
                Spacer(Modifier.height(16.dp))
                FilledTonalButton(onClick = onOpenWifiSettings, shapes = ButtonDefaults.shapes()) {
                    Text(if (wifiEnabled) "Choose a network" else "Turn on Wi-Fi")
                }
            }
        }
    }
}

@Composable
private fun SetupCard(
    icon: ImageVector,
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit,
    onDismiss: (() -> Unit)? = null
) {
    val scheme = MaterialTheme.colorScheme
    Card(
        shape = PulseWaveShapes.large,
        colors = CardDefaults.cardColors(containerColor = scheme.secondaryContainer, contentColor = scheme.onSecondaryContainer)
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 20.dp, top = 20.dp, bottom = 20.dp, end = 8.dp)) {
            ShapeIcon(icon, MaterialShapes.Cookie9Sided, scheme.secondary, scheme.onSecondary, size = 44.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f).padding(end = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(body, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onAction, shapes = ButtonDefaults.shapes()) { Text(action) }
            }
            if (onDismiss != null) {
                IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, "Dismiss") }
            }
        }
    }
}

@Composable
private fun FindWeakSpotsCard(rooms: CoverageSummary, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = PulseWaveShapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = scheme.tertiaryContainer, contentColor = scheme.onTertiaryContainer)
    ) {
        Row(Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Find weak spots", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    when (rooms.rankedRooms.size) {
                        0 -> "Walk around your home. The shape shrinks wherever Wi-Fi gets weak."
                        1 -> "1 saved spot · ${rooms.headline}"
                        else -> "${rooms.rankedRooms.size} saved spots · ${rooms.headline}"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Start walking",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            ShapeIcon(
                Icons.Rounded.TravelExplore, MaterialShapes.SoftBurst,
                scheme.tertiary, scheme.onTertiary, size = 88.dp, iconSize = 40.dp
            )
        }
    }
}

@Composable
private fun QuickTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    shape: RoundedPolygon,
    container: Color,
    content: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        shape = PulseWaveShapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier.heightIn(min = 148.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            ShapeIcon(icon, shape, container, content, size = 52.dp, iconSize = 26.dp)
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
