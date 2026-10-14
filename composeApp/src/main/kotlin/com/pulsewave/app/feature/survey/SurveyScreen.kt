@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.survey

import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pulsewave.app.core.database.entities.SurveyProjectEntity
import com.pulsewave.app.core.wifi.WifiNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.viewmodel.koinViewModel
import java.io.File

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    fun releaseOwnedFloorplan(path: String?) {
        if (path == null) return
        scope.launch(Dispatchers.IO) {
            val ownedDirectory = File(context.filesDir, "survey_floorplans").canonicalFile
            val file = File(path).canonicalFile
            if (file.parentFile == ownedDirectory) file.delete()
        }
    }
    val snackbar = remember { SnackbarHostState() }
    var showCreateDialog by remember { mutableStateOf(false) }
    var showNetworks by remember { mutableStateOf(false) }
    var showMapSettings by remember { mutableStateOf(false) }
    var showCoordinates by remember { mutableStateOf(false) }
    var showPoints by remember { mutableStateOf(false) }
    var projectsExpanded by remember { mutableStateOf(false) }
    var actionsExpanded by remember { mutableStateOf(false) }
    var deletingProject by remember { mutableStateOf<SurveyProjectEntity?>(null) }
    var replacingTarget by remember { mutableStateOf<WifiNetwork?>(null) }
    var importingProjectId by remember { mutableStateOf<Long?>(null) }

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

    val activityRecognitionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) viewModel.toggleRecording()
        else viewModel.reportMessage("Walking needs activity recognition. You can still map points manually.")
    }
    val importFloorplan = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            // Copy into private storage; the temporary picker grant need not survive a restart.
            val destinationProjectId = importingProjectId
            scope.launch {
                try {
                    val path = withContext(Dispatchers.IO) {
                        val folder = File(context.filesDir, "survey_floorplans").apply { mkdirs() }
                        val file = File(folder, "floorplan_${destinationProjectId}_${System.currentTimeMillis()}")
                        try {
                            context.contentResolver.openInputStream(uri)?.use { input ->
                                file.outputStream().use { output ->
                                    val buffer = ByteArray(8192)
                                    var total = 0L
                                    while (true) {
                                        val read = input.read(buffer)
                                        if (read < 0) break
                                        total += read
                                        require(total <= 20L * 1024 * 1024) { "Image too large" }
                                        output.write(buffer, 0, read)
                                    }
                                }
                            } ?: error("Image unavailable")
                            val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                            android.graphics.BitmapFactory.decodeFile(file.absolutePath, bounds)
                            require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Image unreadable" }
                            file.absolutePath
                        } catch (error: Exception) { file.delete(); throw error }
                    }
                    if (viewModel.currentProject.value?.id == destinationProjectId) viewModel.setFloorplan(path, ::releaseOwnedFloorplan)
                    else File(path).delete()
                } catch (_: Exception) {
                    viewModel.reportMessage("Choose a readable floorplan image smaller than 20 MB.")
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Site Survey") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton(onClick = { actionsExpanded = true }, enabled = project != null && !isUpdatingProject) { Icon(Icons.Default.MoreVert, "Project actions") }
                    DropdownMenu(expanded = actionsExpanded, onDismissRequest = { actionsExpanded = false }) {
                        DropdownMenuItem(text = { Text("Map dimensions") }, onClick = { actionsExpanded = false; showMapSettings = true })
                        DropdownMenuItem(text = { Text("Import floorplan image") }, onClick = { actionsExpanded = false; importingProjectId = project?.id; importFloorplan.launch("image/*") })
                        if (project?.imagePath != null) DropdownMenuItem(text = { Text("Remove floorplan image") }, onClick = { actionsExpanded = false; viewModel.setFloorplan(null, ::releaseOwnedFloorplan) })
                        DropdownMenuItem(text = { Text("Export all floors to CSV") }, onClick = { actionsExpanded = false; viewModel.exportProject() })
                        DropdownMenuItem(text = { Text("Delete project") }, onClick = { actionsExpanded = false; deletingProject = project })
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        if (projects.isEmpty()) {
            Box(Modifier.padding(padding).fillMaxSize()) { AnimatedEmptyState { showCreateDialog = true } }
        } else {
            BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
                val mapHeight = (maxHeight - 420.dp).coerceIn(200.dp, 700.dp)
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.weight(1f)) {
                        OutlinedButton(onClick = { projectsExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                            Text(project?.name ?: "Choose project", modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                        DropdownMenu(expanded = projectsExpanded, onDismissRequest = { projectsExpanded = false }) {
                            projects.forEach { item -> DropdownMenuItem(
                                text = { Text(item.name) },
                                onClick = { viewModel.selectProject(item.id); projectsExpanded = false },
                                leadingIcon = { if (item.id == project?.id) Icon(Icons.Default.Check, null) }
                            ) }
                        }
                    }
                    IconButton(onClick = { showCreateDialog = true }, enabled = !isUpdatingProject) { Icon(Icons.Default.Add, "Create project") }
                }
                OutlinedButton(onClick = { showNetworks = true; viewModel.refreshNetworks() }, modifier = Modifier.fillMaxWidth(), enabled = !isRecording && !isUpdatingProject) {
                    Icon(Icons.Default.Wifi, null)
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(project?.targetSsid?.ifBlank { "Hidden network" } ?: "Choose an access point", maxLines = 1)
                        project?.targetBssid?.let { Text(it, style = MaterialTheme.typography.labelSmall) }
                    }
                    Icon(Icons.Default.ArrowDropDown, null)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    SurveyFloorPicker(selectedFloor, viewModel::setFloor, modifier = Modifier.weight(1f), enabled = !isRecording && !isUpdatingProject)
                    TextButton(onClick = { showPoints = true }) { Text("${points.size} points") }
                    IconButton(onClick = viewModel::undoLastPoint, enabled = points.isNotEmpty() && !isSampling && !isUpdatingProject) { Icon(Icons.Default.Undo, "Undo latest point on this floor") }
                }
                Text(
                    when {
                        isRecording -> "Walk estimate · drag to correct your position. Set map dimensions and bearing in project actions."
                        project?.targetBssid == null && projectPointCount > 0 -> "This older map has no fixed access point. Export it before replacing measurements with a selected radio."
                        project?.targetBssid == null -> "Choose an access point, then tap the map or enter coordinates to measure its coverage."
                        else -> "Tap to measure the selected access point. Stay at that location until the sample finishes."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (points.isNotEmpty()) Text(
                    "Average ${points.map { it.rssi }.average().toInt()} dBm · weakest ${points.minOf { it.rssi }} dBm",
                    style = MaterialTheme.typography.labelMedium
                )
                Box(Modifier.height(mapHeight).fillMaxWidth()) {
                    SurveyCanvas(
                        points = points, onPointAdded = viewModel::setStartPoint,
                        onPositionCorrected = viewModel::correctCurrentPosition,
                        modifier = Modifier.fillMaxSize(), isRecording = isRecording,
                        heading = currentHeading - (project?.mapTopBearingDegrees ?: 0f), ghostPath = ghostPath,
                        livePosition = position, imagePath = project?.imagePath
                    )
                    if (isSampling) Surface(Modifier.align(Alignment.TopCenter).padding(8.dp), shape = MaterialTheme.shapes.medium) {
                        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                            Text("Waiting for fresh signal…", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedButton(onClick = { showCoordinates = true }, modifier = Modifier.weight(1f), enabled = !isSampling && !isUpdatingProject) { Text("Add at coordinates") }
                    FilledTonalButton(onClick = {
                        if (isRecording) viewModel.stopRecording()
                        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && ContextCompat.checkSelfPermission(context,
                            android.Manifest.permission.ACTIVITY_RECOGNITION) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            activityRecognitionLauncher.launch(android.Manifest.permission.ACTIVITY_RECOGNITION)
                        } else viewModel.toggleRecording()
                    }, enabled = project?.targetBssid != null && position != null && !isUpdatingProject) {
                        Icon(if (isRecording) Icons.Default.Stop else Icons.AutoMirrored.Filled.DirectionsWalk, null)
                        Spacer(Modifier.width(6.dp)); Text(if (isRecording) "Stop walk" else "Start walk")
                    }
                }
                if (isRecording) {
                    SurveyModeToggle(surveyMode, viewModel::setSurveyMode, enabled = false)
                    Button(onClick = viewModel::addManualAnchor, enabled = !isSampling, modifier = Modifier.fillMaxWidth()) { Text("Sample here") }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SurveyModeToggle(surveyMode, viewModel::setSurveyMode, enabled = !isRecording && !isUpdatingProject)
                        Text("Auto: at most every 30 s", style = MaterialTheme.typography.labelSmall, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(4.dp))
                }
            }
        }
    }

    if (showCreateDialog) SurveyNameDialog(onDismiss = { showCreateDialog = false }) { name -> viewModel.createProject(name); showCreateDialog = false }
    if (showNetworks) AlertDialog(
        onDismissRequest = { showNetworks = false }, title = { Text("Choose access point") },
        text = {
            Column {
                Text("Map one radio at a time. Matching SSIDs can have different access points.", style = MaterialTheme.typography.bodySmall)
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    if (networks.isEmpty()) item { Text("No access points available. Turn on Wi-Fi and location, then refresh.", Modifier.padding(vertical = 16.dp)) }
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
        onDismissRequest = { replacingTarget = null }, title = { Text("Replace mapped access point?") },
        text = { Text("This clears all measurements on every floor of this project. Create another project to keep both maps.") },
        confirmButton = { TextButton(onClick = { viewModel.selectTarget(network); replacingTarget = null }) { Text("Clear and switch") } },
        dismissButton = { TextButton(onClick = { replacingTarget = null }) { Text("Cancel") } }
    ) }
    deletingProject?.let { item -> AlertDialog(
        onDismissRequest = { deletingProject = null }, title = { Text("Delete ${item.name}?") }, text = { Text("All measurements on every floor will be deleted.") },
        confirmButton = { TextButton(onClick = { viewModel.deleteProject(item, ::releaseOwnedFloorplan); deletingProject = null }) { Text("Delete") } },
        dismissButton = { TextButton(onClick = { deletingProject = null }) { Text("Cancel") } }
    ) }
    if (showMapSettings) project?.let { item -> SurveyMapSettingsDialog(item, onDismiss = { showMapSettings = false }) { width, height, step, bearing ->
        viewModel.configureMap(width, height, step, bearing); showMapSettings = false
    } }
    if (showCoordinates) project?.let { item -> SurveyCoordinateDialog(item, position, onDismiss = { showCoordinates = false }) { x, y ->
        viewModel.addPoint(x, y); showCoordinates = false
    } }
    if (showPoints) AlertDialog(
        onDismissRequest = { showPoints = false }, title = { Text("Floor $selectedFloor measurements") },
        text = { LazyColumn(Modifier.heightIn(max = 400.dp)) {
            if (points.isEmpty()) item { Text("No measurements on this floor yet.") }
            items(points, key = { it.id }) { point ->
                Column(Modifier.padding(vertical = 8.dp)) {
                    Text("${point.rssi} dBm · ${point.ssid ?: "Unknown access point"}")
                    Text("X ${"%.1f".format(point.x * (project?.widthMeters ?: 10f))} m, Y ${"%.1f".format(point.y * (project?.heightMeters ?: 10f))} m", style = MaterialTheme.typography.bodySmall)
                    Text(point.bssid ?: "Unknown radio", style = MaterialTheme.typography.bodySmall)
                }
            }
        } }, confirmButton = { TextButton(onClick = { showPoints = false }) { Text("Close") } }
    )
}

@Composable
fun AnimatedEmptyState(onCreateProject: () -> Unit) {
    com.pulsewave.app.core.designsystem.component.EmptyStateWithIllustration(
        illustration = { com.pulsewave.app.core.designsystem.component.SurveyIllustration() },
        title = "Map Your Space",
        subtitle = "Create a site survey to visualize WiFi signal strength across different areas.",
        action = {
            Button(
                onClick = onCreateProject,
                modifier = Modifier.height(ButtonDefaults.ExtraLargeContainerHeight),
                shapes = ButtonDefaults.shapes()
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Create Survey", style = MaterialTheme.typography.titleMedium)
            }
        }
    )
}

@Composable
private fun SurveyFloorPicker(
    selectedFloor: Int,
    onFloorSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier) {
        OutlinedButton(onClick = { expanded = true }, enabled = enabled) {
            Text(if (selectedFloor == 0) "Basement" else "Floor $selectedFloor")
            Icon(Icons.Default.ArrowDropDown, null)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(3, 2, 1, 0).forEach { floor ->
                DropdownMenuItem(text = { Text(if (floor == 0) "Basement" else "Floor $floor") },
                    onClick = { onFloorSelected(floor); expanded = false })
            }
        }
    }
}

@Composable
private fun SurveyModeToggle(
    mode: SurveyViewModel.SurveyMode,
    onModeSelected: (SurveyViewModel.SurveyMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        val modes = listOf(
            SurveyViewModel.SurveyMode.AUTO_SAMPLE to "Auto",
            SurveyViewModel.SurveyMode.MANUAL_ANCHOR to "Manual"
        )
        modes.forEachIndexed { index, (value, label) ->
            ToggleButton(
                checked = mode == value,
                enabled = enabled,
                onCheckedChange = { onModeSelected(value) },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    else -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                },
                modifier = Modifier.height(48.dp)
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
