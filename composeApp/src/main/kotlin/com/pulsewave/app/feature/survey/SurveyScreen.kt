@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.survey

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
    val projects by viewModel.projects.collectAsStateWithLifecycle()
    val project by viewModel.currentProject.collectAsStateWithLifecycle()
    val points by viewModel.currentPoints.collectAsStateWithLifecycle()
    val projectPointCount by viewModel.projectPointCount.collectAsStateWithLifecycle()
    val isRecording by viewModel.isRecording.collectAsStateWithLifecycle()
    val currentHeading by viewModel.currentHeading.collectAsStateWithLifecycle()
    val position by viewModel.currentPosition.collectAsStateWithLifecycle()
    val surveyMode by viewModel.surveyMode.collectAsStateWithLifecycle()
    val ghostPath by viewModel.ghostPath.collectAsStateWithLifecycle()
    val isSampling by viewModel.isSampling.collectAsStateWithLifecycle()
    val isUpdatingProject by viewModel.isUpdatingProject.collectAsStateWithLifecycle()
    val selectedFloor by viewModel.selectedFloor.collectAsStateWithLifecycle()
    val networks by viewModel.availableNetworks.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val platform: com.pulsewave.app.core.platform.PlatformActions = org.koin.compose.koinInject()
    val launchers = com.pulsewave.app.core.platform.LocalPermissionLaunchers.current
    val scope = rememberCoroutineScope()
    fun releaseOwnedFloorplan(path: String?) {
        if (path != null) platform.deleteOwnedFile(path)
    }
    val snackbar = remember { SnackbarHostState() }
    var showRename by remember { mutableStateOf(false) }
    var showNetworks by remember { mutableStateOf(false) }
    var showMapSettings by remember { mutableStateOf(false) }
    var showCoordinates by remember { mutableStateOf(false) }
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
    val walkReady = hasTarget && position != null && !isUpdatingProject
    val startWalk = {
        launchers.activityRecognition { granted ->
            if (granted) viewModel.toggleRecording()
            else viewModel.reportMessage("Walking needs activity recognition. You can still tap spots on the map.")
        }
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
                            DropdownMenuItem(text = { Text("Wi-Fi to measure") }, leadingIcon = { Icon(Icons.Default.Router, null) }, onClick = { actionsExpanded = false; showNetworks = true; viewModel.refreshNetworks() }, enabled = !isRecording)
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text(if (project?.imagePath == null) "Use a photo of your plan" else "Change the photo") }, leadingIcon = { Icon(Icons.Default.Image, null) }, onClick = { actionsExpanded = false; importFloorplan(project?.id) })
                            if (project?.imagePath != null) DropdownMenuItem(text = { Text("Remove the photo") }, leadingIcon = { Icon(Icons.Default.HideImage, null) }, onClick = { actionsExpanded = false; viewModel.setFloorplan(null, ::releaseOwnedFloorplan) })
                            DropdownMenuItem(text = { Text("Size and direction") }, leadingIcon = { Icon(Icons.Default.Straighten, null) }, onClick = { actionsExpanded = false; showMapSettings = true })
                            DropdownMenuItem(text = { Text("Add a reading by distance") }, leadingIcon = { Icon(Icons.Default.PinDrop, null) }, onClick = { actionsExpanded = false; showCoordinates = true })
                            DropdownMenuItem(text = { Text("Export readings (CSV)") }, leadingIcon = { Icon(Icons.Default.IosShare, null) }, onClick = { actionsExpanded = false; viewModel.exportProject() })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text("Delete this map") }, leadingIcon = { Icon(Icons.Default.DeleteOutline, null) }, onClick = { actionsExpanded = false; deletingProject = project })
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (projects.isEmpty() || project == null) {
            Box(Modifier.padding(padding).fillMaxSize()) {
                if (projects.isEmpty()) AnimatedEmptyState { viewModel.createMap() }
            }
            return@Scaffold
        }
        Box(Modifier.padding(padding).fillMaxSize()) {
            // The map is the screen; everything else floats on it.
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 104.dp)
                    .clip(com.pulsewave.app.core.designsystem.PulseWaveShapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            ) {
                SurveyCanvas(
                    points = points, onPointAdded = viewModel::setStartPoint,
                    onPositionCorrected = viewModel::correctCurrentPosition,
                    modifier = Modifier.fillMaxSize(), isRecording = isRecording,
                    heading = currentHeading - (project?.mapTopBearingDegrees ?: 0f), ghostPath = ghostPath,
                    livePosition = position, imagePath = project?.imagePath
                )
                FloorButtons(
                    floors = floors,
                    selected = selectedFloor,
                    onSelect = viewModel::setFloor,
                    enabled = !isRecording && !isUpdatingProject,
                    modifier = Modifier.align(Alignment.TopStart).padding(12.dp)
                )
                MapHint(
                    text = when {
                        isSampling -> "Measuring… hold still"
                        !hasTarget -> "Pick the Wi-Fi to measure from the menu"
                        isRecording -> "Your dot follows your steps. Drag it if it drifts."
                        points.isEmpty() -> "Tap where you're standing"
                        points.size < 3 -> "Move to another spot and tap it"
                        else -> null
                    },
                    busy = isSampling,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(12.dp)
                )
            }
            SurveyToolbar(
                walking = isRecording,
                autoMeasure = surveyMode == SurveyViewModel.SurveyMode.AUTO_SAMPLE,
                canUndo = points.isNotEmpty() && !isSampling && !isUpdatingProject,
                canWalk = walkReady,
                canMeasure = !isSampling,
                onUndo = viewModel::undoLastPoint,
                onReadings = { showPoints = true },
                onWalk = startWalk,
                onWalkUnavailable = {
                    viewModel.reportMessage(if (!hasTarget) "Pick the Wi-Fi to measure first, from the menu." else "Tap where you're standing first, then walk from there.")
                },
                onStop = viewModel::stopRecording,
                onMeasure = viewModel::addManualAnchor,
                onAutoMeasure = { viewModel.setSurveyMode(if (it) SurveyViewModel.SurveyMode.AUTO_SAMPLE else SurveyViewModel.SurveyMode.MANUAL_ANCHOR) },
                modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)
            )
        }
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
    if (showMapSettings) project?.let { item -> SurveyMapSettingsDialog(item, onDismiss = { showMapSettings = false }) { width, height, step, bearing ->
        viewModel.configureMap(width, height, step, bearing); showMapSettings = false
    } }
    if (showCoordinates) project?.let { item -> SurveyCoordinateDialog(item, position, onDismiss = { showCoordinates = false }) { x, y ->
        viewModel.addPoint(x, y); showCoordinates = false
    } }
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
                            Text("${quality.label} · ${point.rssi} dBm", style = MaterialTheme.typography.bodyLarge)
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
private fun FloorButtons(floors: List<Int>, selected: Int, onSelect: (Int) -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    val next = ((1..3).firstOrNull { it !in floors }) ?: 0.takeIf { it !in floors }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween), verticalAlignment = Alignment.CenterVertically) {
        floors.forEachIndexed { index, floor ->
            val shapes = when {
                floors.size == 1 -> null
                index == 0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                index == floors.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }
            val buttonModifier = Modifier.heightIn(min = 40.dp).semantics { contentDescription = if (floor == 0) "Basement" else "Floor $floor" }
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

/** One short line on the map, only while it helps. */
@Composable
private fun MapHint(text: String?, busy: Boolean, modifier: Modifier = Modifier) {
    androidx.compose.animation.AnimatedVisibility(
        visible = text != null,
        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
        exit = androidx.compose.animation.fadeOut(),
        modifier = modifier
    ) {
        var shown by remember { mutableStateOf(text.orEmpty()) }
        if (text != null) shown = text
        Surface(shape = com.pulsewave.app.core.designsystem.PulseWaveShapes.full, color = MaterialTheme.colorScheme.inverseSurface, contentColor = MaterialTheme.colorScheme.inverseOnSurface) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (busy) LoadingIndicator(Modifier.size(20.dp), color = MaterialTheme.colorScheme.inversePrimary)
                Text(shown, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

/**
 * The expressive floating toolbar: quiet tools on the left, the main action as its FAB.
 * Idle, the FAB starts a walk; walking, it stops, and "Measure" takes the toolbar.
 */
@Composable
private fun SurveyToolbar(
    walking: Boolean,
    autoMeasure: Boolean,
    canUndo: Boolean,
    canWalk: Boolean,
    canMeasure: Boolean,
    onUndo: () -> Unit,
    onReadings: () -> Unit,
    onWalk: () -> Unit,
    onWalkUnavailable: () -> Unit,
    onStop: () -> Unit,
    onMeasure: () -> Unit,
    onAutoMeasure: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalFloatingToolbar(
        expanded = true,
        modifier = modifier,
        floatingActionButton = {
            FloatingToolbarDefaults.VibrantFloatingActionButton(onClick = { if (walking) onStop() else if (canWalk) onWalk() else onWalkUnavailable() }) {
                Icon(
                    if (walking) Icons.Default.Stop else Icons.AutoMirrored.Filled.DirectionsWalk,
                    if (walking) "Stop walking" else "Walk and map"
                )
            }
        },
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors()
    ) {
        if (walking) {
            TextButton(onClick = onMeasure, enabled = canMeasure) {
                Icon(Icons.Default.PinDrop, null, Modifier.size(20.dp)); Spacer(Modifier.width(6.dp)); Text("Measure")
            }
            IconToggleButton(checked = autoMeasure, onCheckedChange = onAutoMeasure) {
                Icon(Icons.Default.Timer, if (autoMeasure) "Stop measuring every 30 seconds" else "Measure every 30 seconds")
            }
        } else {
            IconButton(onClick = onUndo, enabled = canUndo) { Icon(Icons.AutoMirrored.Filled.Undo, "Undo the last reading") }
            IconButton(onClick = onReadings) { Icon(Icons.AutoMirrored.Filled.List, "All readings") }
        }
    }
}
