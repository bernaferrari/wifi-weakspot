@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.pulsewave.app.feature.walk

import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.SwipeToDismissBox
import com.pulsewave.app.core.util.twoDigits

import com.pulsewave.app.core.util.fixed

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DinnerDining
import androidx.compose.material.icons.rounded.DoorFront
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Garage
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.PushPin
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.layout.onSizeChanged
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.component.ShapeIcon
import com.pulsewave.app.core.designsystem.component.SignalMorphBlob
import com.pulsewave.app.core.designsystem.component.polygon
import com.pulsewave.app.core.designsystem.component.WalkTimeline
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.rooms.CoverageSummary
import com.pulsewave.app.core.rooms.RoomReading
import com.pulsewave.app.core.rooms.RoomSuggestions
import com.pulsewave.app.core.rooms.WalkSample
import com.pulsewave.app.core.rooms.WalkSummary
import com.pulsewave.app.core.util.relativeTime
import com.pulsewave.app.core.wifi.SignalQuality
import com.pulsewave.app.core.wifi.bandLabel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WalkScreen(
    modifier: Modifier = Modifier,
    onOpenFloorPlan: () -> Unit = {},
    viewModel: WalkViewModel = koinViewModel()
) {
    val live by viewModel.live.collectAsStateWithLifecycle()
    val session by viewModel.session.collectAsStateWithLifecycle()
    val finished by viewModel.finished.collectAsStateWithLifecycle()
    val spots by viewModel.spots.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val haptics = rememberPulseWaveHapticFeedback()
    val snackbar = remember { SnackbarHostState() }
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    var pinning by rememberSaveable { mutableStateOf(false) }
    val platform: com.pulsewave.app.core.platform.PlatformActions = org.koin.compose.koinInject()
    val walking = session != null
    val quality = if (live.hasSignalMeasurement) SignalQuality.of(live.rssi) else null

    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); viewModel.dismissMessage() }
    }
    // Feel the signal: a tick when it improves, a buzz when you walk into a weak spot.
    var lastQuality by remember { mutableStateOf(quality) }
    LaunchedEffect(quality) {
        val previous = lastQuality
        if (previous != null && quality != null && quality != previous) {
            if (quality.isProblem && !previous.isProblem) haptics.error()
            else if (quality.ordinal < previous.ordinal) haptics.light()
        }
        lastQuality = quality
    }

    val scrollBehavior = androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.fillMaxSize().nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            Surface(color = MaterialTheme.colorScheme.surface) {
                // With a navigation rail there's no bottom bar to absorb the system gesture area.
                Box(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
                    when {
                        quality == null -> FilledTonalButton(
                            onClick = { platform.openWifiSettings() },
                            shapes = ButtonDefaults.shapes()
                        ) { Text("Open Wi-Fi settings") }
                        walking -> WalkingControls(
                            onPin = { haptics.click(); pinning = true },
                            onFinish = { haptics.heavy(); viewModel.finishWalk() }
                        )
                        else -> Button(
                            onClick = { haptics.heavy(); viewModel.startWalk() },
                            shapes = ButtonDefaults.shapes(),
                            contentPadding = ButtonDefaults.contentPaddingFor(ButtonDefaults.MediumContainerHeight),
                            modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight)
                        ) {
                            Icon(Icons.AutoMirrored.Rounded.DirectionsWalk, null, Modifier.size(ButtonDefaults.iconSizeFor(ButtonDefaults.MediumContainerHeight)))
                            Spacer(Modifier.width(ButtonDefaults.iconSpacingFor(ButtonDefaults.MediumContainerHeight)))
                            Text("Start walking", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
                        }
                    }
                }
            }
        },
        topBar = {
            MediumFlexibleTopAppBar(
                title = { Text("Walk your home") },
                subtitle = { if (walking) Text("Walking · keep the app open") },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        // The stage absorbs spare height, so tall screens get a bigger meter instead of a gap.
        val density = androidx.compose.ui.platform.LocalDensity.current
        val itemHeights = remember { androidx.compose.runtime.mutableStateMapOf<String, Int>() }
        fun Modifier.measured(key: String) = onSizeChanged { itemHeights[key] = it.height }
        androidx.compose.foundation.layout.BoxWithConstraints(Modifier.fillMaxSize()) {
        val bottomPadding = padding.calculateBottomPadding() + 24.dp
        val shown = buildList {
            add("headline")
            if (walking) add("walking") else {
                if (spots.rankedRooms.isNotEmpty()) add("spots")
                add("floorplan")
            }
        }
        val others = with(density) { shown.sumOf { itemHeights[it] ?: 0 }.toDp() }
        val gaps = 16.dp * shown.size
        val spare = maxHeight - padding.calculateTopPadding() - bottomPadding - others - gaps
        // Compact phones shrink the meter rather than pushing the hint under the button.
        val stageHeight = if (walking) spare.coerceIn(200.dp, 400.dp) else spare.coerceIn(220.dp, 460.dp)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = bottomPadding
            ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            item(key = "stage") {
                FinderStage(rssi = live.rssi.takeIf { live.hasSignalMeasurement }, active = walking, height = stageHeight)
            }
            item(key = "headline") {
                val fade = MaterialTheme.motionScheme.fastEffectsSpec<Float>()
                AnimatedContent(quality, transitionSpec = { fadeIn(fade) togetherWith fadeOut(fade) }, label = "finderHeadline") { q ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.fillMaxWidth().measured("headline")) {
                        Text(q?.headline ?: "Connect to your Wi-Fi", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
                        if (!walking) Text(
                            when {
                                q == null -> "The finder reads the Wi-Fi your phone is connected to."
                                // What the signal means here, not how to read the meter.
                                else -> q.explanation
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (walking) {
                item(key = "walking") { Box(Modifier.measured("walking")) { WalkingStats(session!!) } }
            }
            if (!walking && spots.rankedRooms.isNotEmpty()) {
                item(key = "spots") {
                    Box(Modifier.measured("spots")) {
                        SavedSpots(spots, onDelete = { spot ->
                            viewModel.delete(spot)
                            scope.launch {
                                val result = snackbar.showSnackbar("Removed ${spot.name}", actionLabel = "Undo", duration = SnackbarDuration.Short)
                                if (result == SnackbarResult.ActionPerformed) viewModel.restore(spot)
                            }
                        }, onClear = viewModel::clearSpots)
                    }
                }
            }
            if (!walking) {
                item(key = "floorplan") {
                    // An action, not another saved spot: a plain button sets it apart from the list.
                    FilledTonalButton(
                        onClick = onOpenFloorPlan,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.measured("floorplan")
                    ) {
                        Icon(Icons.Rounded.Map, null, Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text("Map it on a floor plan")
                    }
                }
            }
        }
        }
    }

    if (pinning) {
        PinSpotSheet(
            existing = spots.rankedRooms.map { it.name },
            onSave = { name -> pinning = false; haptics.success(); viewModel.saveSpot(name) },
            onDismiss = { pinning = false }
        )
    }
    finished?.let { (summary, samples) ->
        WalkSummarySheet(summary, samples, onDone = viewModel::dismissSummary, onAgain = { viewModel.dismissSummary(); viewModel.startWalk() })
    }
}

/** A shape that grows with signal, with ripples that quicken as it gets stronger. */
@Composable
private fun FinderStage(rssi: Int?, active: Boolean, height: androidx.compose.ui.unit.Dp) {
    val quality = rssi?.let(SignalQuality::of)
    val fraction = rssi?.let(SignalQuality::fraction) ?: 0f
    val colors = signalColors(quality ?: SignalQuality.DeadZone)
    val blobSize by animateDpAsState(
        lerp(140f, height.value - 56f, fraction).dp,
        MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "finderSize"
    )
    // The pulse is a second gauge, like sonar: strong signal sends quick, bold rings far out;
    // weak signal sends slow, faint rings that barely leave the shape.
    val strength by animateFloatAsState(fraction, MaterialTheme.motionScheme.slowEffectsSpec(), label = "finderStrength")
    val reduceMotion = com.pulsewave.app.core.designsystem.LocalReduceMotion.current
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
        Modifier.fillMaxWidth().height(height).semantics(mergeDescendants = true) {
            liveRegion = LiveRegionMode.Polite
            contentDescription = quality?.let { "$rssi dBm, ${it.headline}" } ?: "No Wi-Fi signal"
        },
        contentAlignment = Alignment.Center
    ) {
        if (quality != null) {
            // Full width, so strong signal can send rings well past the shape.
            Canvas(Modifier.fillMaxWidth().height(height)) {
                // Rings start hidden behind the shape and emerge from its edge, easing out as they fade.
                val start = blobSize.toPx() * 0.3f
                val edge = blobSize.toPx() / 2
                val reach = minOf(size.width / 2, size.height * 0.62f)
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
        // The same gauge as everywhere else, just bigger: its fill and shape track the signal live.
        if (rssi != null) {
            com.pulsewave.app.core.designsystem.component.WifiBadge(rssi, size = blobSize)
        } else {
            SignalMorphBlob(quality = null, color = MaterialTheme.colorScheme.surfaceContainerHighest, size = blobSize, spinning = false) {
                Icon(Icons.Rounded.WifiOff, null, Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun WalkingStats(session: WalkSession) {
    Card(shape = PulseWaveShapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val seconds = (session.samples.lastOrNull()?.elapsedMs ?: 0L) / 1000
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                StatPill(Icons.Rounded.Timer, "${seconds / 60}:${(seconds % 60).twoDigits()}")
                session.latestRouterMs?.let { StatPill(Icons.Rounded.Router, "Router ${it.fixed(0)} ms") }
            }
            WalkTimeline(session.samples, Modifier.fillMaxWidth().height(64.dp))
        }
    }
}

@Composable
private fun WalkingControls(onPin: () -> Unit, onFinish: () -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        FilledTonalButton(
            onClick = onPin,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.weight(1f).heightIn(min = ButtonDefaults.MediumContainerHeight)
        ) {
            Icon(Icons.Rounded.PushPin, null)
            Spacer(Modifier.width(8.dp))
            Text("Pin spot", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
        }
        Button(
            onClick = onFinish,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.weight(1f).heightIn(min = ButtonDefaults.MediumContainerHeight)
        ) {
            Icon(Icons.Rounded.Stop, null)
            Spacer(Modifier.width(8.dp))
            Text("Finish", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
        }
    }
}

@Composable
private fun StatPill(icon: ImageVector, text: String) {
    Surface(shape = PulseWaveShapes.full, color = MaterialTheme.colorScheme.surfaceContainerHighest) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"))
        }
    }
}

@Composable
private fun SavedSpots(spots: CoverageSummary, onDelete: (RoomReading) -> Unit, onClear: () -> Unit) {
    Column(Modifier.fillMaxWidth().animateContentSize(MaterialTheme.motionScheme.defaultSpatialSpec())) {
        Text(
            "Saved spots",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
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
                            Box(
                                Modifier.fillMaxSize().clip(ListItemDefaults.segmentedShapes(index = index, count = spots.rankedRooms.size).shape)
                                    .background(MaterialTheme.colorScheme.errorContainer).padding(horizontal = 24.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) { Icon(Icons.Rounded.DeleteOutline, "Delete ${spot.name}", tint = MaterialTheme.colorScheme.onErrorContainer) }
                        }
                    ) {
                        SegmentedListItem(
                            onClick = {},
                            shapes = ListItemDefaults.segmentedShapes(index = index, count = spots.rankedRooms.size),
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                            leadingContent = { com.pulsewave.app.core.designsystem.component.WifiBadge(spot.averageRssi) },
                            supportingContent = {
                                Text(
                                    listOfNotNull(
                                        // Speed, when tested here, is what people feel; it leads.
                                        spot.speedMbps?.let { "${it.fixed(0)} Mbps" },
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
        if (spots.tips.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            Card(
                shape = PulseWaveShapes.large,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Lightbulb, null)
                        Spacer(Modifier.width(12.dp))
                        Text(if (spots.weakRooms.isEmpty()) "Good to know" else "How to fix weak spots", style = MaterialTheme.typography.titleMedium)
                    }
                    spots.tips.forEach { Text(it, style = MaterialTheme.typography.bodyMedium) }
                }
            }
        }
    }
}

@Composable
private fun PinSpotSheet(existing: List<String>, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding()) {
            Text("Where are you?", style = MaterialTheme.typography.headlineSmall)
            Text("Tap a place to save this spot.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(16.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (existing + RoomSuggestions).distinctBy { it.lowercase() }.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { onSave(suggestion) },
                        label = { Text(suggestion) },
                        icon = { Icon(spotIcon(suggestion), null, Modifier.size(18.dp)) }
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it.take(40) },
                label = { Text("Or type a name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (name.isNotBlank()) onSave(name) }),
                trailingIcon = { if (name.isNotBlank()) TextButton(onClick = { onSave(name) }) { Text("Save") } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WalkSummarySheet(summary: WalkSummary, samples: List<WalkSample>, onDone: () -> Unit, onAgain: () -> Unit) {
    val worst = SignalQuality.of(summary.worstRssi)
    val colors = signalColors(worst)
    ModalBottomSheet(onDismissRequest = onDone, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SignalMorphBlob(quality = worst, color = colors.container, size = 120.dp, spinning = false) {
                Icon(Icons.AutoMirrored.Rounded.DirectionsWalk, null, Modifier.size(48.dp), tint = colors.onContainer)
            }
            Text(summary.headline, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            WalkTimeline(samples, Modifier.fillMaxWidth().height(64.dp), maxBars = samples.size.coerceIn(20, 90))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryTile("Strongest", "${summary.bestRssi}", "dBm", Modifier.weight(1f))
                SummaryTile("Weakest", "${summary.worstRssi}", "dBm", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SummaryTile("Weak signal", "${(summary.weakShare * 100).fixed(0)}", "% of walk", Modifier.weight(1f))
                SummaryTile("Router", summary.medianRouterMs?.let { "${it.fixed(0)}" } ?: "—", "ms typical", Modifier.weight(1f))
            }
            Text(
                if (summary.weakMoments == 0 && summary.floor == SignalQuality.Fair)
                    "No weak spots, but at ${summary.worstRssi} dBm calls and games may stutter. Moving the router closer would help."
                else if (summary.weakMoments == 0) "Your Wi-Fi reached every place you walked."
                else "Walk back to where the shape shrank and pin it, so you can compare after moving the router.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Button(onClick = onDone, shapes = ButtonDefaults.shapes(), modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight)) {
                Text("Done", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
            }
            TextButton(onClick = onAgain) { Text("Walk again") }
        }
    }
}

@Composable
private fun SummaryTile(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Card(shape = PulseWaveShapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh), modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(value, style = MaterialTheme.typography.headlineMedium.copy(fontFeatureSettings = "tnum"), fontWeight = FontWeight.Bold)
                Text(" $unit", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(bottom = 4.dp))
            }
        }
    }
}

private fun spotIcon(name: String): ImageVector {
    val n = name.lowercase()
    return when {
        "kitchen" in n -> Icons.Rounded.Kitchen
        "bed" in n -> Icons.Rounded.Bed
        "living" in n || "lounge" in n || "family" in n -> Icons.Rounded.Weekend
        "bath" in n || "toilet" in n -> Icons.Rounded.Bathtub
        "office" in n || "study" in n || "desk" in n -> Icons.Rounded.Computer
        "kid" in n || "nursery" in n -> Icons.Rounded.ChildCare
        "dining" in n -> Icons.Rounded.DinnerDining
        "hall" in n || "entr" in n -> Icons.Rounded.DoorFront
        "garage" in n -> Icons.Rounded.Garage
        "basement" in n || "attic" in n || "stair" in n -> Icons.Rounded.Stairs
        "balcony" in n || "terrace" in n || "patio" in n -> Icons.Rounded.Balcony
        "garden" in n || "yard" in n -> Icons.Rounded.Yard
        else -> Icons.Rounded.MeetingRoom
    }
}
