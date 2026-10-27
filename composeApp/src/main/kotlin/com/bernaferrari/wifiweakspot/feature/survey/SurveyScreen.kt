@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.survey

import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.material.icons.filled.NearMe
import com.bernaferrari.wifiweakspot.core.util.fixed

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
import androidx.compose.foundation.clickable
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
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyProjectEntity
import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import com.bernaferrari.wifiweakspot.core.designsystem.component.polygon
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SurveyScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    viewModel: SurveyViewModel = koinViewModel()
) {
    val display = com.bernaferrari.wifiweakspot.core.designsystem.LocalSignalDisplay.current
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
    var selectedPointId by remember(project?.id, selectedFloor) { mutableStateOf<Long?>(null) }
    var cursor by remember(project?.id, selectedFloor) { mutableStateOf(SurveyPosition(0.5f, 0.5f)) }
    val selectedPoint = points.find { it.id == selectedPointId }
    var renamingPoint by remember { mutableStateOf<com.bernaferrari.wifiweakspot.core.database.entities.SurveyPointEntity?>(null) }
    val networks by viewModel.availableNetworks.collectAsStateWithLifecycle()
    val isScanning by viewModel.isScanning.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()
    val platform: com.bernaferrari.wifiweakspot.core.platform.PlatformActions = org.koin.compose.koinInject()
    val launchers = com.bernaferrari.wifiweakspot.core.platform.LocalPermissionLaunchers.current
    val scope = rememberCoroutineScope()
    val haptics = com.bernaferrari.wifiweakspot.core.designsystem.rememberWifiWeakspotHapticFeedback()
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
        Column(controlsModifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SurveyToolbar(
                canUndo = points.isNotEmpty() && !isSampling && !isUpdatingProject,
                hasTarget = hasTarget,
                // One instruction at a time; the map itself shows the result.
                status = when {

                    isSampling -> strings.survey.measuringHoldStill
                    !hasTarget -> strings.survey.chooseWifi
                    points.isEmpty() -> strings.survey.tapWhereYouAreStanding
                    else -> strings.survey.tapWhereYouAreStanding
                },
                onUndo = viewModel::undoLastPoint,
                onReadings = { showPoints = true },
                onChooseNetwork = { showNetworks = true; viewModel.refreshNetworks() },
                modifier = Modifier.fillMaxWidth()
            )
        SurveyPlacementControls(
            cursor = cursor,
            selectedPoint = selectedPoint,
            enabled = hasTarget && !isSampling && !isUpdatingProject,
            canRename = !isSampling && !isUpdatingProject,
            onPositionChanged = { cursor = it; selectedPointId = null },
            onMeasure = { selectedPoint?.let(viewModel::recheckPoint) ?: viewModel.addPoint(cursor.x, cursor.y) },
            onRename = { renamingPoint = selectedPoint }
        )
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(project?.name ?: strings.survey.floorPlanTitle, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        if (project != null) Text(
                            when {
                                !hasTarget -> strings.survey.chooseWifi
                                else -> listOfNotNull(
                                    project?.targetSsid?.ifBlank { strings.common.hiddenNetwork },
                                    strings.survey.readingCount(points.size),
                                ).joinToString(" · ")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, strings.common.back) } },
                actions = {
                    // The finished map is worth sending: one tap shares it as a picture.
                    val shareMapLabel = strings.common.share
                    if (points.isNotEmpty()) IconButton(onClick = {
                        haptics.click()
                        scope.launch { platform.shareImage(mapLayer.toImageBitmap(), shareMapLabel) }
                    }) { Icon(Icons.Default.Share, shareMapLabel) }
                    if (project != null) Box {
                        IconButton(onClick = { actionsExpanded = true }, enabled = !isUpdatingProject) { Icon(Icons.Default.MoreVert, strings.survey.mapOptions) }
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
                            DropdownMenuItem(text = { Text(strings.survey.createMap) }, leadingIcon = { Icon(Icons.Default.Add, null) }, onClick = { actionsExpanded = false; viewModel.createMap() })
                            DropdownMenuItem(text = { Text(strings.common.rename) }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { actionsExpanded = false; showRename = true })
                            DropdownMenuItem(text = { Text(strings.survey.chooseWifi) }, leadingIcon = { Icon(Icons.Default.Router, null) }, onClick = { actionsExpanded = false; showNetworks = true; viewModel.refreshNetworks() })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text(if (project?.imagePath == null) strings.survey.usePlanPhoto else strings.survey.changePhoto) }, leadingIcon = { Icon(Icons.Default.Image, null) }, onClick = { actionsExpanded = false; importFloorplan(project?.id) })
                            if (project?.imagePath != null) DropdownMenuItem(text = { Text(strings.survey.removePhoto) }, leadingIcon = { Icon(Icons.Default.HideImage, null) }, onClick = { actionsExpanded = false; viewModel.setFloorplan(null, ::releaseOwnedFloorplan) })
                            DropdownMenuItem(text = { Text(strings.survey.exportReadingsCsv) }, leadingIcon = { Icon(Icons.Default.IosShare, null) }, onClick = { actionsExpanded = false; viewModel.exportProject() })
                            HorizontalDivider()
                            DropdownMenuItem(text = { Text(strings.common.delete) }, leadingIcon = { Icon(Icons.Default.DeleteOutline, null) }, onClick = { actionsExpanded = false; deletingProject = project })
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
                    .clip(com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes.extraLarge)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    // Recorded as it draws, so "Share map" captures exactly what's on screen.
                    .drawWithContent {
                        mapLayer.record { this@drawWithContent.drawContent() }
                        drawLayer(mapLayer)
                    }
            ) {
                key(project?.id, selectedFloor) { SurveyCanvas(
                    points = points, onPointAdded = viewModel::addPoint,
                    modifier = Modifier.fillMaxSize(),
                    canMeasure = !isSampling && !isUpdatingProject && hasTarget,
                    livePosition = position.takeIf { isSampling }, imagePath = project?.imagePath,
                    expectedReadingMillis = expectedReadingMillis,
                    readingIsTimed = readingIsTimed,
                    widthMeters = project?.widthMeters ?: 10f,
                    heightMeters = project?.heightMeters ?: 10f,
                    cursor = cursor, selectedPointId = selectedPointId,
                    onPositionChanged = { cursor = it },
                    onPointSelected = { selectedPointId = it },
                    onPointRecheck = { id -> points.find { it.id == id }?.let(viewModel::recheckPoint) }
                ) }
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
        val count = viewModel.floorReadingCount(floor)
        AlertDialog(
            onDismissRequest = { deletingFloor = null },
            title = { Text(strings.common.deleteItemTitle(strings.survey.floorName(floor))) },
            text = { Text(strings.survey.deleteFloorBody(count)) },
            confirmButton = { TextButton(onClick = { viewModel.deleteFloor(floor); deletingFloor = null }) { Text(strings.common.delete) } },
            dismissButton = { TextButton(onClick = { deletingFloor = null }) { Text(strings.common.cancel) } }
        )
    }
    if (showRename) project?.let { current -> SurveyNameDialog(current.name, onDismiss = { showRename = false }) { name -> viewModel.renameProject(name); showRename = false } }
    if (showNetworks) AlertDialog(
        onDismissRequest = { showNetworks = false }, title = { Text(strings.survey.chooseWifi) },
        text = {
            Column {
                Text(strings.survey.oneRouterAtATime, style = MaterialTheme.typography.bodySmall)
                LazyColumn(Modifier.heightIn(max = 360.dp)) {
                    if (networks.isEmpty()) item { Text(strings.survey.noNetworksFound, Modifier.padding(vertical = 16.dp)) }
                    items(networks.distinctBy { it.bssid }.sortedByDescending { it.rssi }, key = { it.bssid }) { network ->
                        TextButton(onClick = {
                            if (projectPointCount > 0 && !project?.targetBssid.equals(network.bssid, true)) replacingTarget = network
                            else viewModel.selectTarget(network)
                            showNetworks = false
                        }, modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.fillMaxWidth()) {
                                Text(network.ssid.ifBlank { strings.common.hiddenNetwork })
                                Text("${network.bssid} · ${network.rssi} dBm", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = viewModel::refreshNetworks, enabled = !isScanning) { Text(if (isScanning) strings.survey.scanning else strings.survey.refresh) } },
        dismissButton = { TextButton(onClick = { showNetworks = false }) { Text(strings.common.close) } }
    )
    replacingTarget?.let { network -> AlertDialog(
        onDismissRequest = { replacingTarget = null }, title = { Text(strings.survey.switchWifiTitle) },
        text = { Text(strings.survey.switchWifiBody) },
        confirmButton = { TextButton(onClick = { viewModel.selectTarget(network); replacingTarget = null }) { Text(strings.survey.clearAndSwitch) } },
        dismissButton = { TextButton(onClick = { replacingTarget = null }) { Text(strings.common.cancel) } }
    ) }
    deletingProject?.let { item -> AlertDialog(
        onDismissRequest = { deletingProject = null }, title = { Text(strings.common.deleteItemTitle(item.name)) }, text = { Text(strings.survey.deleteMapBody) },
        confirmButton = { TextButton(onClick = { viewModel.deleteProject(item, ::releaseOwnedFloorplan); deletingProject = null }) { Text(strings.common.delete) } },
        dismissButton = { TextButton(onClick = { deletingProject = null }) { Text(strings.common.cancel) } }
    ) }
    renamingPoint?.let { point ->
        SurveyNameDialog(point.label.orEmpty(), onDismiss = { renamingPoint = null }, onSave = {
            viewModel.renamePoint(point, it); renamingPoint = null
        })
    }
    if (showPoints) ModalBottomSheet(onDismissRequest = { showPoints = false }, sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        val ranked = points.sortedBy { it.rssi }
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            item {
                Column(Modifier.padding(start = 8.dp, bottom = 16.dp)) {
                    Text(strings.survey.floorName(selectedFloor), style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (ranked.isEmpty()) strings.survey.noReadingsYet
                        else strings.survey.weakestFirst,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            itemsIndexed(ranked, key = { _, point -> point.id }) { index, point ->
                val quality = com.bernaferrari.wifiweakspot.core.wifi.SignalQuality.of(point.rssi)
                val colors = com.bernaferrari.wifiweakspot.core.designsystem.signalColors(quality)
                Surface(
                    shape = com.bernaferrari.wifiweakspot.core.designsystem.component.segmentedShape(index, ranked.size, outer = 20.dp, inner = 4.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth().animateItem().clickable {
                        selectedPointId = point.id
                        cursor = SurveyPosition(point.x, point.y)
                        showPoints = false
                    }
                ) {
                    Row(Modifier.padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon(Icons.Default.Wifi, quality.polygon(), colors.container, colors.onContainer, size = 40.dp)
                        Column(Modifier.weight(1f)) {
                            Text(point.label ?: strings.survey.pointName(point.id), style = MaterialTheme.typography.titleMedium)
                            Text("${quality.label} · ${display.reading(point.rssi)}", style = MaterialTheme.typography.bodyLarge)
                            Text(com.bernaferrari.wifiweakspot.core.util.relativeTime(point.timestamp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { viewModel.deletePoint(point) }, enabled = !isUpdatingProject) { Icon(Icons.Default.DeleteOutline, strings.survey.remove) }
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
        com.bernaferrari.wifiweakspot.core.designsystem.component.SurveyIllustration()
        Spacer(Modifier.height(24.dp))
        Text(strings.survey.mapYourHome, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            strings.survey.mapYourHomeBody,
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
            Text(strings.survey.createMap, style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
        }
    }
}

/** Floors as a connected button group on the map; "+" adds the next one up (then a basement). */
@Composable
private fun FloorButtons(floors: List<Int>, selected: Int, onSelect: (Int) -> Unit, enabled: Boolean, modifier: Modifier = Modifier, onLongPress: (Int) -> Unit = {}) {
    val next = ((1..3).firstOrNull { it !in floors }) ?: 0.takeIf { it !in floors }
    val survey = strings.survey
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
                    contentDescription = survey.floorName(floor)
                    onLongClick(label = survey.deleteFloorReadingsAction) { onLongPress(floor); true }
                }
            if (shapes == null) FilledTonalToggleButton(
                checked = floor == selected,
                onCheckedChange = { onSelect(floor) },
                enabled = enabled,
                modifier = buttonModifier
            ) { Text(strings.survey.floorButtonLabel(floor)) } else FilledTonalToggleButton(
                checked = floor == selected,
                onCheckedChange = { onSelect(floor) },
                enabled = enabled,
                shapes = shapes,
                modifier = buttonModifier
            ) { Text(if (floor == selected) strings.survey.floorName(floor) else strings.survey.floorShortLabel(floor)) }
        }
        if (next != null) {
            Spacer(Modifier.width(8.dp))
            FilledTonalIconButton(onClick = { onSelect(next) }, enabled = enabled, shapes = IconButtonDefaults.shapes()) {
                Icon(Icons.Default.Add, strings.survey.addFloor(next))
            }
        }
    }
}

/** The legend uses the same blend and fixed dBm thresholds as the coverage field. */
@Composable
private fun SignalLegend() {
    val scheme = MaterialTheme.colorScheme
    val heat = com.bernaferrari.wifiweakspot.core.designsystem.signalHeatColors()
    val colors = remember(heat) { signalGradient(heat).let { blend -> (0..12).map { blend(-90f + it * 40f / 12f) } } }
    Surface(shape = com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes.full, color = scheme.surfaceContainerHigh.copy(alpha = 0.92f)) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(strings.analyze.axisWeak, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            Box(Modifier.size(64.dp, 8.dp).clip(com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes.full).background(androidx.compose.ui.graphics.Brush.horizontalGradient(colors)))
            Text(strings.analyze.axisStrong, style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
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
    Surface(modifier, shape = com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        Row(Modifier.padding(start = 20.dp, end = 8.dp, top = 8.dp, bottom = 8.dp).heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(status, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            if (!hasTarget) Button(onClick = onChooseNetwork, shapes = ButtonDefaults.shapes()) { Text(strings.survey.chooseWifi) }
            else {
                IconButton(onClick = onUndo, enabled = canUndo) { Icon(Icons.AutoMirrored.Filled.Undo, strings.survey.undoLastReading) }
            }
            IconButton(onClick = onReadings) { Icon(Icons.AutoMirrored.Filled.List, strings.survey.allReadings) }
        }
    }
}
