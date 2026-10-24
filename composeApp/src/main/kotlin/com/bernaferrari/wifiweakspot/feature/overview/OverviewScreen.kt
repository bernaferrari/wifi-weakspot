@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.bernaferrari.wifiweakspot.feature.overview

import com.bernaferrari.wifiweakspot.feature.history.quality
import com.bernaferrari.wifiweakspot.core.util.fixed

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.rounded.NetworkCheck
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
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.graphics.shapes.RoundedPolygon
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bernaferrari.wifiweakspot.core.datastore.SettingsRepository
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon
import com.bernaferrari.wifiweakspot.core.designsystem.component.SignalMorphBlob
import com.bernaferrari.wifiweakspot.core.designsystem.rememberWifiWeakspotHapticFeedback
import com.bernaferrari.wifiweakspot.core.designsystem.signalColors
import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import com.bernaferrari.wifiweakspot.core.permissions.PermissionGate
import com.bernaferrari.wifiweakspot.core.rooms.CoverageSummary
import com.bernaferrari.wifiweakspot.core.util.relativeTime
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import com.bernaferrari.wifiweakspot.core.wifi.bandLabel
import com.bernaferrari.wifiweakspot.core.wifi.rememberRadioState
import com.bernaferrari.wifiweakspot.core.designsystem.component.InfoPill
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
    onNavigateToDiagnose: () -> Unit = {},
    onNavigateToScanSetup: () -> Unit = {},
    onRequestScanPermission: () -> Unit = {},
    viewModel: OverviewViewModel = koinViewModel()
) {
    val status by viewModel.networkStatus.collectAsStateWithLifecycle()
    val latestSpeed by viewModel.latestSpeedTest.collectAsStateWithLifecycle()
    val knownDevices by viewModel.knownDevices.collectAsStateWithLifecycle()
    val savedTests by viewModel.savedTests.collectAsStateWithLifecycle()
    val nearby by viewModel.nearby.collectAsStateWithLifecycle()
    val permissionGate: PermissionGate = koinInject()
    val permissions by permissionGate.state.collectAsStateWithLifecycle()
    val settings: SettingsRepository = koinInject()
    val radios = rememberRadioState()
    val haptics = rememberWifiWeakspotHapticFeedback()
    val scope = rememberCoroutineScope()
    val platform: com.bernaferrari.wifiweakspot.core.platform.PlatformActions = org.koin.compose.koinInject()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var showConnectionDetail by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("WiFi\u00A0Weakspot") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        com.bernaferrari.wifiweakspot.core.designsystem.AdaptiveColumns(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 32.dp
            ),
            primary = {
            item(key = "hero") {
                ConnectionHero(
                    status = status,
                    wifiEnabled = radios.wifiEnabled,
                    onOpenWifiSettings = {
                        platform.openWifiSettings()
                    },
                    onClick = { haptics.click(); showConnectionDetail = true }
                )
            }
            // Fresh, live information first; the rooms check is an invitation underneath.
            item(key = "rooms") {
                FindWeakSpotsCard(onClick = { haptics.click(); onNavigateToRooms() })
            }

            val needsPermission = !permissions.canScanWifi
            val needsLocation = permissions.canScanWifi && !radios.locationEnabled
            if (needsPermission || needsLocation) {
                item(key = "setup") {
                    SetupCard(
                        icon = if (needsPermission) Icons.Rounded.WifiFind else Icons.Rounded.LocationOn,
                        title = if (needsPermission) "See every network around you" else "Turn on Location",
                        body = if (needsPermission) {
                            "Android shares nearby Wi-Fi names only with apps that have location access. WiFi\u00A0Weakspot never records where you are."
                        } else {
                            "Android needs Location on to list nearby Wi-Fi. WiFi\u00A0Weakspot doesn't use GPS."
                        },
                        action = if (needsPermission) "Allow" else "Turn on",
                        onAction = {
                            haptics.click()
                            if (needsPermission) onRequestScanPermission()
                            else platform.openLocationSettings()
                        }
                    )
                }
            }

            item(key = "nearby") {
                NearbySummaryCard(
                    networks = nearby,
                    connectedBssid = status.bssid.takeIf { status.isConnected },
                    connectedFrequency = if (status.isConnected) status.frequency else 0,
                    onClick = { haptics.click(); onNavigateToWifi() }
                )
            }

            },
            secondary = {

            item(key = "toolsHeader") {
                Text(
                    "Tools",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(start = 4.dp, top = 12.dp).semantics { heading() }
                )
            }

            item(key = "tiles") {
                val scheme = MaterialTheme.colorScheme
                // The gauge remembers last time: the needle swings right for a quick router, and
                // the tile takes the colour of that result.
                val routerGauge = latestSpeed?.takeIf { it.pingMs >= 0f }?.let { test ->
                    val verdict = com.bernaferrari.wifiweakspot.core.router.linkVerdict(
                        com.bernaferrari.wifiweakspot.core.router.RouterLatency(test.pingMs, test.jitterMs, test.lossPercent ?: 0f, 1)
                    )
                    // 2 ms or less pins the needle right; 80 ms or more rests it left, on a log scale.
                    val fraction = 1f - (kotlin.math.ln(test.pingMs.coerceIn(2f, 80f) / 2f) / kotlin.math.ln(40f))
                    RouterGauge(com.bernaferrari.wifiweakspot.core.designsystem.component.RouterGaugeIcon.of(fraction), com.bernaferrari.wifiweakspot.core.designsystem.signalColors(verdict.quality))
                }
                QuickTileGrid(
                    listOf(
                        QuickTileSpec(
                            "Router check",
                            latestSpeed?.pingMs?.takeIf { it >= 0f }?.let { "${it.fixed(1)} ms last time" }
                                ?: "Response time",
                            routerGauge?.icon ?: Icons.Rounded.Speed, MaterialShapes.Sunny,
                            routerGauge?.colors?.container ?: scheme.primaryContainer, routerGauge?.colors?.onContainer ?: scheme.onPrimaryContainer, onNavigateToSpeedTest
                        ),
                        QuickTileSpec("Devices", knownDevices?.let { "$it found last scan" } ?: "Who's connected", Icons.Rounded.Devices, MaterialShapes.Clover8Leaf, scheme.tertiaryContainer, scheme.onTertiaryContainer, onNavigateToLan),
                        QuickTileSpec("Test a website", "Ping, DNS, ports", com.bernaferrari.wifiweakspot.core.designsystem.component.SiteCheckIcon.Filled, MaterialShapes.Cookie6Sided, scheme.secondaryContainer, scheme.onSecondaryContainer, onNavigateToDiagnose),
                        QuickTileSpec("History", when (savedTests) { 0 -> "Past results"; 1 -> "1 check"; else -> "$savedTests checks" }, Icons.Rounded.History, MaterialShapes.Puffy, scheme.surfaceContainerHighest, scheme.onSurface, onNavigateToHistory)
                    )
                )
            }

            if (status.isConnected && !status.internetValidated) {
                item(key = "advice") {
                    val advice = connectionAdvice(status)
                    Card(
                        shape = WifiWeakspotShapes.large,
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
        )
    }

    if (showConnectionDetail && status.hasSignalMeasurement) {
        val network = connectionDetails(status, nearby, com.bernaferrari.wifiweakspot.core.util.nowMillis())
        val favorites by settings.favoriteBssids.collectAsState(initial = emptySet())
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showConnectionDetail = false },
            sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            com.bernaferrari.wifiweakspot.feature.analyze.WifiDetailScreen(
                network = network,
                onBack = { showConnectionDetail = false },
                isFavorite = network.bssid in favorites,
                onToggleFavorite = { scope.launch { settings.toggleFavorite(network.bssid) } },
                onShare = {
                    platform.share(
                        "${network.ssid}\n${network.rssi} dBm · Ch ${com.bernaferrari.wifiweakspot.core.wifi.frequencyToChannel(network.frequency)} · ${com.bernaferrari.wifiweakspot.core.wifi.bandLabel(network.frequency)}",
                        "Share network"
                    )
                }
            )
        }
    }
}

@Composable
private fun ConnectionHero(status: NetworkStatus, wifiEnabled: Boolean, onOpenWifiSettings: () -> Unit, onClick: () -> Unit) {
    val quality = if (status.hasSignalMeasurement) SignalQuality.of(status.rssi) else null
    val colors = signalColors(quality ?: SignalQuality.DeadZone)
    val scheme = MaterialTheme.colorScheme
    // Tapping the live card opens everything about this connection, like any network in the list.
    Card(
        onClick = onClick,
        enabled = quality != null,
        shape = WifiWeakspotShapes.extraExtraLarge,
        colors = CardDefaults.cardColors(containerColor = scheme.surfaceContainerLow, disabledContainerColor = scheme.surfaceContainerLow, disabledContentColor = scheme.onSurface)
    ) {
        Column(Modifier.fillMaxWidth().padding(24.dp)) {
            BoxWithConstraints(Modifier.fillMaxWidth()) {
                val badgeSize = if (maxWidth < 300.dp) 88.dp else 116.dp
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // The same Wi-Fi symbol and quality colors as the network and room lists.
                    if (quality != null) {
                        com.bernaferrari.wifiweakspot.core.designsystem.component.WifiBadge(status.rssi, size = badgeSize)
                    } else {
                        SignalMorphBlob(quality = null, color = scheme.surfaceContainerHighest, size = badgeSize, spinning = false) {
                            Icon(Icons.Rounded.WifiOff, null, Modifier.size(40.dp), tint = scheme.onSurfaceVariant)
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            when {
                                quality != null -> quality.headline
                                status.isConnected -> "Signal unavailable"
                                !wifiEnabled -> "Wi-Fi is off"
                                else -> "Not on Wi-Fi"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(minFontSize = 18.sp, maxFontSize = MaterialTheme.typography.headlineSmall.fontSize)
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
                                    if (status.internetValidated) "Online" else "Internet not confirmed",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
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
                    if (status.linkSpeed > 0) InfoPill("Link rate ${status.linkSpeed} Mbps")
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
        shape = WifiWeakspotShapes.large,
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

/** An entry point, not a report: saved readings go stale, so Home only invites a fresh check. */
@Composable
private fun FindWeakSpotsCard(onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Card(
        onClick = onClick,
        shape = WifiWeakspotShapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = scheme.tertiaryContainer, contentColor = scheme.onTertiaryContainer)
    ) {
        Row(Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Find weak spots", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("See which rooms are weak.", style = MaterialTheme.typography.bodyLarge)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                    Text("Check your rooms", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.width(16.dp))
            ShapeIcon(Icons.Rounded.TravelExplore, MaterialShapes.SoftBurst, scheme.tertiary, scheme.onTertiary, size = 72.dp, iconSize = 34.dp)
        }
    }
}

private class RouterGauge(val icon: androidx.compose.ui.graphics.vector.ImageVector, val colors: com.bernaferrari.wifiweakspot.core.designsystem.SignalColors)
