@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.survey

import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.material.icons.filled.NearMe
import com.pulsewave.app.core.util.fixed

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.database.entities.SurveyProjectEntity
import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.designsystem.component.polygon
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SurveyScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: SurveyViewModel = koinViewModel()
) {
    val display = com.pulsewave.app.core.designsystem.LocalSignalDisplay.current
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val project by viewModel.currentProject.collectAsStateWithLifecycle()
    val points by viewModel.currentPoints.collectAsStateWithLifecycle()
    val projectPointCount by viewModel.projectPointCount.collectAsStateWithLifecycle()
    val position by viewModel.currentPosition.collectAsStateWithLifecycle()
    val isSampling by viewModel.isSampling.collectAsStateWithLifecycle()
    val expectedReadingMillis by viewModel.expectedReadingMillis.collectAsStateWithLifecycle()
    val readingIsTimed by viewModel.readingIsTimed.collectAsStateWithLifecycle()
    var deletingFloor by remember { mutableStateOf<Int?>(null) }
    val isUpdatingProject by viewModel.isUpdatingProject.collectAsStateWithLifecycle()
    val selectedFloor by viewModel.selectedFloor.collectAsStateWithLifecycle()
    val networks by viewModel.availableNetworks.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val platform: com.pulsewave.app.core.platform.PlatformActions = org.koin.compose.koinInject()
    val launchers = com.pulsewave.app.core.platform.LocalPermissionLaunchers.current
    val scope = rememberCoroutineScope()
    val haptics = com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback()
    val mapLayer = androidx.compose.ui.graphics.rememberGraphicsLayer()
    fun releaseOwnedFloorplan(path: String?) {
        if (path != null) platform.deleteOwnedFile(path)
    }
    val snackbar = remember { SnackbarHostState() }
    var showRename by remember { mutableStateOf(false) }
    var showNetworks by remember { mutableStateOf(false) }
    var showPoints by remember { mutableStateOf(false) }
    var projectsExpanded by remember { mutableStateOf(false) }
    var actionsExpanded by remember { mutableStateOf(false) }
    var deletingProject by remember { mutableStateOf<SurveyProjectEntity?>(null) }
    var replacingTarget by remember { mutableStateOf<WifiNetwork?>(null) }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(lifecycle, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) viewModel.onScreenStopped()
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            viewModel.onScreenStopped()
        }
    }
    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); viewModel.dismissMessage() }
    }

    fun importFloorplan(destinationProjectId: Long?) {
        launchers.pickFloorplan(destinationProjectId ?: 0L) { path ->
            when {
                path == null -> Unit
                viewModel.currentProject.value?.id == destinationProjectId -> viewModel.setFloorplan(path, ::releaseOwnedFloorplan)
                else -> platform.deleteOwnedFile(path)
            }
        }
    }

    val floors by viewModel.floorsInUse.collectAsStateWithLifecycle()
    val hasTarget = project?.targetBssid != null
    val window = androidx.compose.ui.platform.LocalWindowInfo.current.containerSize
    val shortLandscape = with(androidx.compose.ui.platform.LocalDensity.current) { window.width > window.height && window.height.toDp() < 480.dp }
    val controls: @Composable (Modifier) -> Unit = { controlsModifier ->
        SurveyToolbar(
                canUndo = points.isNotEmpty() && !isSampling && !isUpdatingProject,
                hasTarget = hasTarget,
                // One instruction at a time; the map itself shows the result.
                status = when {
                    isSampling && !readingIsTimed -> "Scanning for ${project?.targetSsid ?: "this Wi-Fi"}… hold still"
                    isSampling -> "Measuring… hold still"
                    !hasTarget -> "Choose the Wi-Fi you want to map"
                    points.isEmpty() -> "Tap the map where you're standing"
                    else -> "Walk to another room and tap where you are"
                },
                onUndo = viewModel::undoLastPoint,
                onReadings = { showPoints = true },
                onChooseNetwork = { showNetworks = true; viewModel.refreshNetworks() },
                modifier = controlsModifier
            )
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project?.name ?: "Floor plan", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (project != null) Text(
                            when {
                                !hasTarget -> "Pick the Wi-Fi to measure"
                                else -> listOfNotNull(
                                    project?.targetSsid?.ifBlank { "Hidden network" },
                                    if (points.size == 1) "1 reading" else "${points.size} readings",
                                ).joinToString(" · ")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    // The finished map is worth sending: one tap shares it as a picture.
                    if (points.isNotEmpty()) IconButton(onClick = {
                        haptics.click()
                        scope.launch { platform.shareImage(mapLayer.toImageBitmap(), "Share map") }
                    }) { Icon(Icons.Default.Share, "Share map") }
                    if (project != null) Box {
                        IconButton(onClick = { actionsExpanded = true }, enabled = !isUpdatingProject) { Icon(Icons.Default.MoreVert, "Map options") }
                        DropdownMenu(expanded = actionsExpanded, onDismissRequest = { actionsExpanded = false }) {
                            if (projects.size > 1) {
                                projects.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(item.name) },
                                        onClick = { actionsExpanded = false; viewModel.selectProject(item.id) },
                                        leadingIcon = { if (item.id == project?.id) Icon(Icons.Default.Check, null) else Icon(Icons.Default.Map, null) }
                                    )
                                }
                                HorizontalDivider()
                            }
                            DropdownMenuItem(text = { Text("New map") }, leadingIcon = { Icon(Icons.Default.Add, null) }, onClick = { actionsExpanded = false; viewModel.createMap() })
                            DropdownMenuItem(text = { Text("Rename map") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { actionsExpanded = false; showRename = true })
                            DropdownMenuItem(text = { Text("Wi-Fi to measure") }, leadingIcon = { Icon(Icons.Default.Router, null) }, onClick = { actionsExpanded = false; showNetworks = true; viewModel.refreshNetworks() })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text(if (project?.imagePath == null) "Use a photo of your plan" else "Change the photo") }, leadingIcon = { Icon(Icons.Default.Image, null) }, onClick = { actionsExpanded = false; importFloorplan(project?.id) })
                            if (project?.imagePath != null) DropdownMenuItem(text = { Text("Remove the photo") }, leadingIcon = { Icon(Icons.Default.HideImage, null) }, onClick = { actionsExpanded = false; viewModel.setFloorplan(null, ::releaseOwnedFloorplan) })
                            DropdownMenuItem(text = { Text("Export readings (CSV)") }, leadingIcon = { Icon(Icons.Default.IosShare, null) }, onClick = { actionsExpanded = false; viewModel.exportProject() })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("Delete this map") }, leadingIcon = { Icon(Icons.Default.DeleteOutline, null) }, onClick = { actionsExpanded = false; deletingProject = project })
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        bottomBar = {
            if (project != null && !shortLandscape) controls(Modifier.fillMaxWidth().navigationBarsPadding().padding(horizontal = 12.dp, vertical = 12.dp))
        }
    ) { padding ->
        if (projects.isEmpty() || project == null) {
            Box(Modifier.padding(padding).fillMaxSize()) {
                if (projects.isEmpty()) AnimatedEmptyState { viewModel.createMap() }
            }
            return@Scaffold
        }
        Row(Modifier.padding(padding).fillMaxSize()) {
            Box(Modifier.weight(1f).fillMaxHeight()) {
            // Controls occupy their own space, so they never obscure map readings.
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 12.dp)
                    .clip(com.pulsewave.app.core.designsystem.PulseWaveShapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    // Recorded as it draws, so "Share map" captures exactly what's on screen.
                    .drawWithContent {
                        mapLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(mapLayer)
                    }
            ) {
                SurveyCanvas(
                    points = points, onPointAdded = viewModel::addPoint,
                    modifier = Modifier.fillMaxSize(),
                    canMeasure = !isSampling && !isUpdatingProject && hasTarget,
                    livePosition = position.takeIf { isSampling }, imagePath = project?.imagePath,
                    expectedReadingMillis = expectedReadingMillis,
                    readingIsTimed = readingIsTimed
                )
                // Floors on the left, legend on the right; with many floors the legend wraps below
                // instead of colliding.
                androidx.compose.foundation.layout.FlowRow(
                    Modifier.align(Alignment.TopStart).fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    itemVerticalAlignment = Alignment.CenterVertically
                ) {
                    FloorButtons(
                        floors = floors,
                        selected = selectedFloor,
                        onSelect = viewModel::setFloor,
                        enabled = !isUpdatingProject,
                        onLongPress = { deletingFloor = it }
                    )
                    if (points.isNotEmpty()) SignalLegend()
                }
            }
            }
            if (shortLandscape) Box(Modifier.width(260.dp).fillMaxHeight().verticalScroll(rememberScrollState())) {
                controls(Modifier.fillMaxWidth().padding(end = 12.dp, top = 4.dp, bottom = 12.dp))
            }
        }
    }

    deletingFloor?.let { floor ->
        val name = if (floor == 0) "the basement" else "floor $floor"
        val count = viewModel.floorReadingCount(floor)
        AlertDialog(
            onDismissRequest = { deletingFloor = null },
            title = { Text("Delete ${name}?") },
            text = { Text(if (count == 1) "Its 1 reading will be deleted." else "Its $count readings will be deleted.") },
            confirmButton = { TextButton(onClick = { viewModel.deleteFloor(floor); deletingFloor = null }) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { deletingFloor = null }) { Text("Cancel") } }
        )
    }
    if (showRename) project?.let { current -> SurveyNameDialog(current.name, onDismiss = { showRename = false }) { name -> viewModel.renameProject(name); showRename = false } }
    if (showNetworks) AlertDialog(
        onDismissRequest = { showNetworks = false }, title = { Text("Which Wi-Fi to map?") },
        text = {
            Column {
                Text("A map follows one router at a time. Mesh systems show each unit separately.", style = MaterialTheme.typography.bodySmall)
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    if (networks.isEmpty()) item { Text("No networks found yet. Check that Wi-Fi and location are on.", Modifier.padding(vertical = 16.dp)) }
                    items(networks.distinctBy { it.bssid }.sortedByDescending { it.rssi }, key = { it.bssid }) { network ->
                        TextButton(onClick = {
                            if (projectPointCount > 0 && !project?.targetBssid.equals(network.bssid, true)) replacingTarget = network
                            else viewModel.selectTarget(network)
                            showNetworks = false
                        }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(network.ssid.ifBlank { "Hidden network" })
                                Text("${network.bssid} · ${network.rssi} dBm", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::refreshNetworks, enabled = !isScanning) { Text(if (isScanning) "Scanning…" else "Refresh") } },
        dismissButton = { TextButton(onClick = { showNetworks = false }) { Text("Close") } }
    )
    replacingTarget?.let { network -> AlertDialog(
        onDismissRequest = { replacingTarget = null }, title = { Text("Switch to another Wi-Fi?") },
        text = { Text("This clears the readings on every floor of this map. Create a new map to keep both.") },
        confirmButton = { TextButton(onClick = { viewModel.selectTarget(network); replacingTarget = null }) { Text("Clear and switch") } },
        dismissButton = { TextButton(onClick = { replacingTarget = null }) { Text("Cancel") } }
    ) }
    deletingProject?.let { item -> AlertDialog(
        onDismissRequest = { deletingProject = null }, title = { Text("Delete ${item.name}?") }, text = { Text("All readings on every floor will be deleted.") },
        confirmButton = { TextButton(onClick = { viewModel.deleteProject(item, ::releaseOwnedFloorplan); deletingProject = null }) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { deletingProject = null }) { Text("Cancel") } }
    ) }
    if (showPoints) ModalBottomSheet(onDismissRequest = { showPoints = false }, sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        val ranked = points.sortedBy { it.rssi }
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            item {
                Column(Modifier.padding(start = 8.dp, bottom = 16.dp)) {
                    Text(if (selectedFloor == 0) "Basement" else "Floor $selectedFloor", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (ranked.isEmpty()) "No readings here yet. Tap the map where you're standing."
                        else "Weakest first, so problem spots are on top.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            itemsIndexed(ranked, key = { _, point -> point.id }) { index, point ->
                val quality = com.pulsewave.app.core.wifi.SignalQuality.of(point.rssi)
                val colors = com.pulsewave.app.core.designsystem.signalColors(quality)
                Surface(
                    shape = com.pulsewave.app.core.designsystem.component.segmentedShape(index, ranked.size, outer = 20.dp, inner = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth().animateItem()
                ) {
                    Row(Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        com.pulsewave.app.core.designsystem.component.ShapeIcon(Icons.Default.Wifi, quality.polygon(), colors.container, colors.onContainer, size = 40.dp)
                        Column(Modifier.weight(1f)) {
                            Text("${quality.label} · ${display.reading(point.rssi)}", style = MaterialTheme.typography.bodyLarge)
                            Text(com.pulsewave.app.core.util.relativeTime(point.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.deletePoint(point) }, enabled = !isUpdatingProject) { Icon(Icons.Default.DeleteOutline, "Remove this reading") }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedEmptyState(onCreateProject: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        com.pulsewave.app.core.designsystem.component.SurveyIllustration()
        Spacer(Modifier.height(24.dp))
        Text("Map your home", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            "See your Wi-Fi on a map of your home. Use a photo of your plan or a blank grid.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onCreateProject,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.heightIn(min = ButtonDefaults.MediumContainerHeight)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(Modifier.width(8.dp))
            Text("Create a map", style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
        }
    }
}

private fun floorLabel(floor: Int) = if (floor == 0) "B" else "$floor"

/** Floors as a connected button group on the map; "+" adds the next one up (then a basement). */
@Composable
private fun FloorButtons(floors: List<Int>, selected: Int, onSelect: (Int) -> Unit, enabled: Boolean, modifier: Modifier = Modifier, onLongPress: (Int) -> Unit = {}) {
    val next = ((1..3).firstOrNull { it !in floors }) ?: 0.takeIf { it !in floors }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween), verticalAlignment = Alignment.CenterVertically) {
        floors.forEachIndexed { index, floor ->
            val shapes = when {
                floors.size == 1 -> null
                index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                index == floors.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }
            // Long press a floor to delete its readings.
            val buttonModifier = Modifier.heightIn(min = 40.dp)
                .pointerInput(floor, enabled) { if (enabled) detectTapGestures(onLongPress = { onLongPress(floor) }) }
                .semantics {
                    contentDescription = if (floor == 0) "Basement" else "Floor $floor"
                    onLongClick(label = "Delete this floor's readings") { onLongPress(floor); true }
                }
            if (shapes == null) FilledTonalToggleButton(
                checked = floor == selected,
                onCheckedChange = { onSelect(floor) },
                enabled = enabled,
                modifier = buttonModifier
            ) { Text("Floor ${floorLabel(floor)}") } else FilledTonalToggleButton(
                checked = floor == selected,
                onCheckedChange = { onSelect(floor) },
                enabled = enabled,
                shapes = shapes,
                modifier = buttonModifier
            ) { Text(if (floor == selected) (if (floor == 0) "Basement" else "Floor $floor") else floorLabel(floor)) }
        }
        if (next != null) {
            Spacer(Modifier.width(8.dp))
            FilledTonalIconButton(onClick = { onSelect(next) }, enabled = enabled, shapes = IconButtonDefaults.shapes()) {
                Icon(Icons.Default.Add, if (next == 0) "Add a basement" else "Add floor $next")
            }
        }
    }
}

/** The legend uses the same blend and fixed dBm thresholds as the coverage field. */
@Composable
private fun SignalLegend() {
    val scheme = MaterialTheme.colorScheme
    val heat = com.pulsewave.app.core.designsystem.signalHeatColors()
    val colors = remember(heat) { signalGradient(heat).let { blend -> (0..12).map { blend(-90f + it * 40f / 12f) } } }
    Surface(shape = com.pulsewave.app.core.designsystem.PulseWaveShapes.full, color = scheme.surfaceContainerHigh.copy(alpha = 0.92f)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Weak", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            Box(Modifier.size(64.dp, 8.dp).clip(com.pulsewave.app.core.designsystem.PulseWaveShapes.full).background(androidx.compose.ui.graphics.Brush.horizontalGradient(colors)))
            Text("Strong", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        }
    }
}

/** One clear next step, and the two things you might want after measuring. */
@Composable
private fun SurveyToolbar(
    canUndo: Boolean,
    hasTarget: Boolean,
    status: String,
    onUndo: () -> Unit,
    onReadings: () -> Unit,
    onChooseNetwork: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(modifier, shape = com.pulsewave.app.core.designsystem.PulseWaveShapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp).heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(status, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (!hasTarget) Button(onClick = onChooseNetwork, shapes = ButtonDefaults.shapes()) { Text("Choose Wi-Fi") }
            else {
                IconButton(onClick = onUndo, enabled = canUndo) { Icon(Icons.AutoMirrored.Filled.Undo, "Undo last reading") }
                IconButton(onClick = onReadings) { Icon(Icons.AutoMirrored.Filled.List, "All readings") }
            }
        }
    }
}
