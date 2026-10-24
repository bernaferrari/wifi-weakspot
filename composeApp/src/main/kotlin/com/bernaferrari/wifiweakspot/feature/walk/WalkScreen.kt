@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.bernaferrari.wifiweakspot.feature.walk

import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBox

import com.bernaferrari.wifiweakspot.core.util.fixed

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.horizontalScroll
import com.bernaferrari.wifiweakspot.core.designsystem.bleed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Balcony
import androidx.compose.material.icons.rounded.Bathtub
import androidx.compose.material.icons.rounded.Bed
import androidx.compose.material.icons.rounded.ChildCare
import androidx.compose.material.icons.rounded.Computer
import androidx.compose.material.icons.rounded.Check
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DinnerDining
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Garage
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.outlined.AddLocationAlt
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Stairs
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.Weekend
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material.icons.rounded.Yard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon
import com.bernaferrari.wifiweakspot.core.designsystem.component.SignalMorphBlob
import com.bernaferrari.wifiweakspot.core.designsystem.component.polygon
import com.bernaferrari.wifiweakspot.core.designsystem.rememberWifiWeakspotHapticFeedback
import com.bernaferrari.wifiweakspot.core.designsystem.signalColors
import com.bernaferrari.wifiweakspot.core.rooms.CoverageSummary
import com.bernaferrari.wifiweakspot.core.rooms.RoomReading
import com.bernaferrari.wifiweakspot.core.rooms.RoomSuggestions
import com.bernaferrari.wifiweakspot.core.rooms.networkKey
import com.bernaferrari.wifiweakspot.core.rooms.spotKey
import com.bernaferrari.wifiweakspot.core.rooms.spotComparison
import com.bernaferrari.wifiweakspot.core.util.relativeTime
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import com.bernaferrari.wifiweakspot.core.wifi.bandLabel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WalkScreen(
    modifier: Modifier = Modifier,
    onOpenFloorPlan: () -> Unit = {},
    viewModel: WalkViewModel = koinViewModel()
) {
    val live by viewModel.live.collectAsStateWithLifecycle()
    val spots by viewModel.spots.collectAsStateWithLifecycle()
    val saving by viewModel.saving.collectAsStateWithLifecycle()
    val choices by viewModel.networkChoices.collectAsStateWithLifecycle()
    val selectedNetwork by viewModel.selectedNetwork.collectAsStateWithLifecycle()
    var openedSpot by rememberSaveable { mutableStateOf<String?>(null) }
    val message by viewModel.message.collectAsStateWithLifecycle()
    val haptics = rememberWifiWeakspotHapticFeedback()
    val snackbar = remember { SnackbarHostState() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var pinning by rememberSaveable { mutableStateOf(false) }
    val platform: com.bernaferrari.wifiweakspot.core.platform.PlatformActions = org.koin.compose.koinInject()
    val quality = if (live.hasSignalMeasurement) SignalQuality.of(live.rssi) else null

    LaunchedEffect(message) {
        message?.let {
            if (it.startsWith("Saved ")) haptics.success()
            else if (it.startsWith("Couldn't")) haptics.error()
            snackbar.showSnackbar(it)
            viewModel.dismissMessage()
        }
    }
    // Feel the signal while moving between rooms: a tick when it improves, a buzz on a weak spot.
    var lastQuality by remember { mutableStateOf(quality) }
    LaunchedEffect(quality) {
        val previous = lastQuality
        if (previous != null && quality != null && quality != previous) {
            if (quality.isProblem && !previous.isProblem) haptics.error()
            else if (quality.ordinal < previous.ordinal) haptics.light()
        }
        lastQuality = quality
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                // With a navigation rail there's no bottom bar to absorb the system gesture area.
                Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                    if (quality == null) FilledTonalButton(
                        onClick = { platform.openWifiSettings() },
                        shapes = ButtonDefaults.shapes()
                    ) { Text("Open Wi-Fi settings") }
                    else RoomControls(
                        enabled = saving == null,
                        saving = saving != null,
                        onMap = { haptics.click(); onOpenFloorPlan() },
                        onSave = { haptics.click(); pinning = true }
                    )
                }
            }
        },
        topBar = {
            androidx.compose.material3.TopAppBar(
                title = { Text("Rooms") },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    scrolledContainerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { padding ->
        val connectedKey = com.bernaferrari.wifiweakspot.core.rooms.roomNetworkKey(live.ssid, live.bssid)
        // Only worth showing once rooms were saved on some other Wi-Fi too.
        val showNetworks = choices.any { it.networkKey != connectedKey }
        val hasList = spots.rankedRooms.isNotEmpty()
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
            // Draw below the transparent app bar; the whole page scrolls on short screens.
            // Anything under the meter (rooms or the network chips) gets room; an empty screen gives the meter most of it.
            val stageHeight = ((maxHeight - padding.calculateTopPadding()) * if (hasList || showNetworks) 0.42f else 0.62f).coerceIn(180.dp, 420.dp)
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "live-signal") {
                    FinderStage(rssi = live.rssi.takeIf { live.hasSignalMeasurement }, height = stageHeight)
                }
                item(key = "signal-label") {
                    Column(
                        Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            quality?.headline ?: if (live.isConnected) "Signal unavailable" else "Connect to your Wi-Fi",
                            style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center
                        )
                        Text(
                            when {
                                saving != null -> "Checking $saving… keep still"
                                quality != null -> live.ssid.ifBlank { "Connected Wi-Fi" }
                                live.isConnected -> "Your device is connected, but no signal reading is available yet."
                                else -> "Connect to the Wi-Fi you want to check."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                    if (showNetworks) {
                        item(key = "networks") {
                            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (connectedKey != null) FilterChip(selected = selectedNetwork == connectedKey,
                                    onClick = { viewModel.selectNetwork(null) }, label = { Text(live.ssid) })
                                choices.filter { it.networkKey != connectedKey }.forEach { choice ->
                                    FilterChip(selected = choice.networkKey == selectedNetwork,
                                        onClick = { viewModel.selectNetwork(choice.networkKey) },
                                        label = { Text(choice.ssid.ifBlank { choice.bssid }) })
                                }
                            }
                        }
                    }
                    if (hasList) {
                        item(key = "spots") {
                            Box(Modifier.padding(horizontal = 16.dp)) {
                            SavedSpots(spots, onOpen = { openedSpot = it.spotKey }, onDelete = { spot ->
                                val history = spots.history.filter { it.spotKey == spot.spotKey }
                                viewModel.deleteSpot(spot)
                                scope.launch {
                                    val result = snackbar.showSnackbar("Removed ${spot.name}", actionLabel = "Undo", duration = SnackbarDuration.Short)
                                    if (result == SnackbarResult.ActionPerformed) viewModel.restoreSpot(history)
                                }
                            })
                            }
                        }
                    } else {
                        item(key = "empty") {
                            // The two ways in, in one sentence: a quick reading per room, or the whole home.
                            Text(
                                "Stand in a room and save a reading. Check another room to find where the signal gets weaker.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                        }
                    }
            }
        }
    }

    openedSpot?.let { key ->
        val history = spots.history.filter { it.spotKey == key }.sortedByDescending { it.timestamp }
        if (history.isNotEmpty()) SpotHistorySheet(
            readings = history, saving = saving != null,
            canRecheck = live.hasSignalMeasurement && com.bernaferrari.wifiweakspot.core.rooms.roomNetworkKey(live.ssid, live.bssid) == history.first().networkKey,
            onRecheck = { viewModel.saveSpot(history.first().name) },
            onRemove = {
                openedSpot = null
                viewModel.deleteSpot(history.first())
                scope.launch {
                    val result = snackbar.showSnackbar("Removed ${history.first().name}", actionLabel = "Undo", duration = SnackbarDuration.Short)
                    if (result == SnackbarResult.ActionPerformed) viewModel.restoreSpot(history)
                }
            },
            onDismiss = { openedSpot = null }
        )
    }
    if (pinning) {
        RoomPickerSheet(
            existing = spots.rankedRooms.map { it.name },
            onSave = { name -> pinning = false; haptics.click(); viewModel.saveSpot(name) },
            onDismiss = { pinning = false }
        )
    }
}

/** A shape that grows with signal, with ripples that quicken as it gets stronger. */
@Composable
private fun FinderStage(rssi: Int?, height: androidx.compose.ui.unit.Dp) {
    val display = com.bernaferrari.wifiweakspot.core.designsystem.LocalSignalDisplay.current
    val quality = rssi?.let(SignalQuality::of)
    val fraction = rssi?.let(SignalQuality::fraction) ?: 0f
    val colors = signalColors(quality ?: SignalQuality.DeadZone)
    val blobSize by animateDpAsState(
        lerp(120f, height.value - 48f, fraction).dp,
        MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "finderSize"
    )
    // The pulse is a second gauge, like sonar: strong signal sends quick, bold rings far out;
    // weak signal sends slow, faint rings that barely leave the shape.
    val strength by animateFloatAsState(fraction, MaterialTheme.motionScheme.slowEffectsSpec(), label = "finderStrength")
    val reduceMotion = com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion.current
    var ripple by remember { mutableFloatStateOf(0f) }
    val latestRssi by androidx.compose.runtime.rememberUpdatedState(rssi)
    if (quality != null && !reduceMotion) {
        LaunchedEffect(Unit) {
            var last = withFrameNanos { it }
            while (true) {
                withFrameNanos { now ->
                    // Calm at every level: full pace (2.2 s) from -65 dBm up, slowing to 6 s at -90.
                    val pace = ((latestRssi ?: -95) + 90).coerceIn(0, 25) / 25f
                    val periodMs = lerp(6_000f, 2_200f, pace)
                    ripple = (ripple + (now - last) / 1_000_000f / periodMs) % 1f
                    last = now
                }
            }
        }
    }
    val rings = 1 + (quality?.bars ?: 0).coerceAtMost(3)
    val ringColor = colors.accent
    Box(
        Modifier.fillMaxWidth().height(height).clearAndSetSemantics {
            contentDescription = quality?.let { "${display.reading(rssi!!)}, ${it.headline}" } ?: "No Wi-Fi signal"
        },
        contentAlignment = Alignment.Center
    ) {
        if (quality != null) {
            // Full width, so strong signal can send rings well past the shape.
            Canvas(Modifier.fillMaxWidth().height(height)) {
                // Rings start hidden behind the shape and emerge from its edge, easing out as they fade.
                val start = blobSize.toPx() * 0.3f
                val edge = blobSize.toPx() / 2
                // Wide windows (tablets, unfolded phones) would send rings far past the title;
                // there the meter's own height sets the reach.
                val reach = if (size.width > size.height * 1.2f) size.height * 0.6f else maxOf(size.width / 2, size.height * 0.75f)
                val end = edge + (reach - edge).coerceAtLeast(0f) * lerp(0.45f, 1f, strength)
                val peak = lerp(0.18f, 0.5f, strength)
                val stroke = lerp(1.5f, 4f, strength).dp.toPx()
                repeat(rings) { index ->
                    val t = (ripple + index / rings.toFloat()) % 1f
                    val eased = 1f - (1f - t) * (1f - t)
                    val radius = start + eased * (end - start)
                    val alpha = peak * (1f - t) * (1f - t)
                    drawCircle(ringColor.copy(alpha = alpha), radius = radius, style = Stroke(width = stroke))
                }
            }
        }
        // A consistent Wi-Fi symbol inside the same quality shape used in every list.
        if (rssi != null) {
            com.bernaferrari.wifiweakspot.core.designsystem.component.WifiBadge(rssi, size = blobSize)
        } else {
            SignalMorphBlob(quality = null, color = MaterialTheme.colorScheme.surfaceContainerHighest, size = blobSize, spinning = false) {
                Icon(Icons.Rounded.WifiOff, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun RoomControls(enabled: Boolean, saving: Boolean, onMap: () -> Unit, onSave: () -> Unit) {
    // Two ways to measure: a quick named reading, or the whole home on a map.
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledTonalButton(
            onClick = onMap,
            enabled = enabled,
            shapes = ButtonDefaults.shapes(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.weight(1f).heightIn(min = ButtonDefaults.MediumContainerHeight)
        ) {
            Icon(Icons.Rounded.Map, null)
            Spacer(Modifier.width(8.dp))
            Text("Map home", style = MaterialTheme.typography.titleMedium, maxLines = 1,
                autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = MaterialTheme.typography.titleMedium.fontSize))
        }
        Button(
            onClick = onSave,
            enabled = enabled,
            shapes = ButtonDefaults.shapes(),
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.weight(1f).heightIn(min = ButtonDefaults.MediumContainerHeight)
        ) {
            if (saving) androidx.compose.material3.LoadingIndicator(Modifier.size(24.dp))
            else Icon(Icons.Outlined.AddLocationAlt, null)
            Spacer(Modifier.width(8.dp))
            Text(if (saving) "Checking…" else "Save room", style = MaterialTheme.typography.titleMedium, maxLines = 1,
                autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = MaterialTheme.typography.titleMedium.fontSize))
        }
    }
}

/** How a room changed since its last check, in a few words; nothing on a first reading. */
private fun shortChange(current: RoomReading, previous: RoomReading?): String? {
    if (previous == null) return null
    val delta = current.averageRssi - previous.averageRssi
    return when {
        delta >= 4 -> "$delta dB stronger"
        delta <= -4 -> "${-delta} dB weaker"
        else -> "Same as before"
    }
}

@Composable
private fun SavedSpots(spots: CoverageSummary, onOpen: (RoomReading) -> Unit, onDelete: (RoomReading) -> Unit) {
    val display = com.bernaferrari.wifiweakspot.core.designsystem.LocalSignalDisplay.current
    Column(Modifier.fillMaxWidth().animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec())) {
        Text(
            "Saved rooms",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
            spots.rankedRooms.forEachIndexed { index, spot ->
                androidx.compose.runtime.key(spot.id) {
                    // Swipe a spot away to forget it; the row stays calm otherwise.
                    // Not saveable on purpose: a spot brought back by Undo must start settled, not dismissed.
                    val dismiss = remember(spot.id) { androidx.compose.material3.SwipeToDismissBoxState(SwipeToDismissBoxValue.Settled) { it * 0.4f } }
                    SwipeToDismissBox(
                        state = dismiss,
                        enableDismissFromStartToEnd = false,
                        onDismiss = { onDelete(spot) },
                        backgroundContent = {
                            // Only while swiping; at rest the red would peek out around the rounded corners.
                            if (dismiss.dismissDirection == SwipeToDismissBoxValue.EndToStart) Box(
                                Modifier.fillMaxSize().clip(ListItemDefaults.segmentedShapes(index = index, count = spots.rankedRooms.size).shape)
                                    .background(MaterialTheme.colorScheme.errorContainer).padding(horizontal = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) { Icon(Icons.Rounded.DeleteOutline, "Delete ${spot.name}", tint = MaterialTheme.colorScheme.onErrorContainer) }
                        }
                    ) {
                        SegmentedListItem(
                            modifier = Modifier.semantics {
                                contentDescription = "${spot.name}, ${spot.quality.label} signal, ${display.reading(spot.averageRssi)}. Review readings and recheck this spot."
                            },
                            onClick = { onOpen(spot) },
                            shapes = ListItemDefaults.segmentedShapes(index = index, count = spots.rankedRooms.size),
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            leadingContent = {
                                Box(Modifier.clearAndSetSemantics { }) {
                                    com.bernaferrari.wifiweakspot.core.designsystem.component.WifiBadge(spot.averageRssi)
                                }
                            },
                            supportingContent = {
                                Text(
                                    listOfNotNull(
                                        spot.quality.label,
                                        shortChange(spot, spots.history.filter { it.spotKey == spot.spotKey && it.id != spot.id }.maxByOrNull { it.timestamp }),
                                        relativeTime(spot.timestamp)
                                    ).joinToString(" · "),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        ) { Text(spot.name, style = MaterialTheme.typography.titleMedium) }
                    }
                }
            }
        }
    }
}
