@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.survey

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SurveyScreen(
    modifier: Modifier = Modifier,
    viewModel: SurveyViewModel = koinViewModel()
) {
    val projects: List<com.pulsewave.app.core.database.entities.SurveyProjectEntity> by viewModel.projects.collectAsState()
    val points: List<com.pulsewave.app.core.database.entities.SurveyPointEntity> by viewModel.currentPoints.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var projectName by remember { mutableStateOf("") }
    var fabExpanded by remember { mutableStateOf(false) }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    val isRecording: Boolean by viewModel.isRecording.collectAsState()
    val currentHeading: Float by viewModel.currentHeading.collectAsState()
    val surveyMode: SurveyViewModel.SurveyMode by viewModel.surveyMode.collectAsState()
    val ghostPath: List<Pair<Float, Float>> by viewModel.ghostPath.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            LargeTopAppBar(
                title = { 
                    Column {
                        Text("Site Survey")
                        if (isRecording) {
                            Text(
                                "🔴 Recording Path", 
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                // Recording Controls
                if (projects.isNotEmpty()) {
                    // Manual Anchor Button (Only visible when recording in MANUAL mode)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = isRecording && surveyMode == SurveyViewModel.SurveyMode.MANUAL_ANCHOR,
                        enter =   androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                    ) {
                        FloatingActionButton(
                            onClick = { 
                                // In a real app, we'd pop a dialog for name here
                                viewModel.addManualAnchor() 
                            },
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ) {
                            Icon(Icons.Default.AddLocation, "Add Point")
                        }
                    }

                    // Start/Stop Walk Button
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.toggleRecording() },
                        containerColor = if (isRecording) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (isRecording) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        icon = { 
                            if (isRecording) 
                                Icon(Icons.Default.Stop, null)
                            else 
                                Icon(Icons.Default.DirectionsWalk, null)
                        },
                        text = { Text(if (isRecording) "Stop Walk" else "Start Walk") }
                    )
                }

                // Unified Expressive FAB with speed-dial
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // A+ POLISH: Floor Selector (Elevator Style)
                    if (projects.isNotEmpty() && !isRecording && !fabExpanded) {
                         Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                            shadowElevation = 4.dp
                        ) {
                            Column(modifier = Modifier.padding(6.dp)) {
                                val floors = listOf(3, 2, 1, 0) // L3, L2, L1, B1
                                val selectedFloor: Int by viewModel.selectedFloor.collectAsState()
                                
                                floors.forEach { floor ->
                                    val isSelected = selectedFloor == floor
                                    val label = if (floor == 0) "B1" else "L$floor"
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                                            .clickable { viewModel.setFloor(floor) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Start of Speed Dial items
                    
                    // Mode Toggle (Only visible when NOT recording)
                    androidx.compose.animation.AnimatedVisibility(
                        visible = fabExpanded && !isRecording && projects.isNotEmpty(),
                        enter =   androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically { it / 2 },
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutVertically { it / 2 }
                    ) {
                        // Custom small toggle for mode
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shadowElevation = 4.dp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Row(modifier = Modifier.padding(4.dp)) {
                                val modes = listOf(
                                    SurveyViewModel.SurveyMode.AUTO_SAMPLE to "Auto",
                                    SurveyViewModel.SurveyMode.MANUAL_ANCHOR to "Manual"
                                )
                                modes.forEach { (mode, label) ->
                                    val isSelected = surveyMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.clickable { viewModel.setSurveyMode(mode) }
                                    ) {
                                        Text(
                                            label, 
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Mini FABs - animated visibility
                    androidx.compose.animation.AnimatedVisibility(
                        visible = fabExpanded,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.scaleIn(),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.scaleOut()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Export button
                            SmallFloatingActionButton(
                                onClick = { 
                                    fabExpanded = false
                                    viewModel.exportProject()
                                },
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Icon(Icons.Default.Share, "Export", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                            // New Project button
                            SmallFloatingActionButton(
                                onClick = { 
                                    fabExpanded = false
                                    showCreateDialog = true 
                                },
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Icon(Icons.Default.Add, "New", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }
                    
                    // Main FAB with rotation animation
                    val rotation by animateFloatAsState(
                        targetValue = if (fabExpanded) 45f else 0f,
                        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
                        label = "fabRotation"
                    )
                    
                    LargeFloatingActionButton(
                        onClick = { fabExpanded = !fabExpanded },
                        shape = RoundedCornerShape(28.dp),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Icon(
                            Icons.Default.Add, 
                            "Menu",
                            modifier = Modifier.size(36.dp).graphicsLayer { rotationZ = rotation },
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (projects.isEmpty()) {
                AnimatedEmptyState(onCreateProject = { showCreateDialog = true })
            } else {
                SurveyCanvas(
                    points = points,
                    onPointAdded = { x, y -> 
                        if (isRecording) {
                            // If recording, tapping sets a manual anchor or start point
                            if (surveyMode == SurveyViewModel.SurveyMode.MANUAL_ANCHOR) {
                                viewModel.setStartPoint(x, y) // Updates internal position
                                // In manual mode, maybe we don't auto-add point on tap unless it's the (+) button
                                // But the prompt says "Tap to map signal", so let's allow it.
                            } else {
                                // Auto mode tap = just force add a point? Or redundant?
                                // Let's keep it simple: Tap always adds a point.
                                viewModel.addPoint(x, y)
                            }
                        } else {
                            viewModel.addPoint(x, y)
                        }
                    },
                    onPositionCorrected = { x, y ->
                        viewModel.correctCurrentPosition(x, y)
                    },
                    modifier = Modifier.fillMaxSize(),
                    isRecording = isRecording,
                    heading = currentHeading,
                    ghostPath = ghostPath
                )
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            icon = {
                Icon(Icons.Default.Map, null, modifier = Modifier.size(28.dp))
            },
            title = { 
                Text(
                    "New Survey", 
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                ) 
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Create a floorplan survey to map WiFi coverage.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        value = projectName,
                        onValueChange = { projectName = it },
                        label = { Text("Project Name") },
                        placeholder = { Text("e.g. Living Room") },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (projectName.isNotBlank()) {
                            viewModel.createProject(projectName)
                            showCreateDialog = false
                            projectName = ""
                        }
                    },
                    modifier = Modifier.padding(end = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Create Project") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
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
                modifier = Modifier.height(52.dp),
                shape = RoundedCornerShape(28.dp)
            ) {
                Icon(Icons.Default.Add, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Create Survey", style = MaterialTheme.typography.titleMedium)
            }
        }
    )
}

// ============================================================
// A+ POLISH: Signal Legend Component
// ============================================================
@Composable
fun SignalLegend(modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = Color(0xFF4CAF50), label = "Excellent")
            LegendItem(color = Color(0xFFFFC107), label = "Good")
            LegendItem(color = Color(0xFFFF9800), label = "Fair")
            LegendItem(color = Color(0xFFF44336), label = "Poor")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================================
// A+ POLISH: Summary Stats Card
// ============================================================
@Composable
fun SurveySummaryCard(
    points: List<com.pulsewave.app.core.database.entities.SurveyPointEntity>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    
    val avgRssi = points.map { it.rssi }.average()
    val minRssi = points.minOfOrNull { it.rssi } ?: -100
    val maxRssi = points.maxOfOrNull { it.rssi } ?: -100
    
    val qualityLabel = when {
        avgRssi > -50 -> "Excellent"
        avgRssi > -60 -> "Good"
        avgRssi > -70 -> "Fair"
        else -> "Poor"
    }
    
    val qualityColor = when {
        avgRssi > -50 -> Color(0xFF4CAF50)
        avgRssi > -60 -> Color(0xFFFFC107)
        avgRssi > -70 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 8.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Quality Badge
            Surface(
                shape = CircleShape,
                color = qualityColor.copy(alpha = 0.15f)
            ) {
                Text(
                    qualityLabel,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = qualityColor
                )
            }
            
            // Stats
            Column {
                Text(
                    "${points.size} points",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Avg: ${avgRssi.toInt()} dBm • Range: $maxRssi to $minRssi",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================
// MAIN CANVAS with A+ Polish
// ============================================================
@Composable
fun SurveyCanvas(
    points: List<com.pulsewave.app.core.database.entities.SurveyPointEntity>,
    onPointAdded: (Float, Float) -> Unit,
    onPositionCorrected: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    isRecording: Boolean = false,
    heading: Float = 0f,
    ghostPath: List<Pair<Float, Float>> = emptyList()
) {
    // ... (existing color defs)
    val colorScheme = MaterialTheme.colorScheme
    val primary = colorScheme.primary
    val tertiary = colorScheme.tertiary
    val error = colorScheme.error
    val onSurface = colorScheme.onSurface
    val gridColor = onSurface.copy(alpha = 0.08f)
    
    // ... (existing animations)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    val haptics = rememberPulseWaveHapticFeedback()

    Box(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(colorScheme.surface)
                .pointerInput(isRecording) {
                    detectDragGestures(
                        onDragStart = { _ ->
                            haptics.light()
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            if (isRecording) {
                                onPositionCorrected(change.position.x / size.width, change.position.y / size.height)
                            }
                        }
                    )
                }
                .pointerInput(isRecording) {
                    detectTapGestures { _ ->
                         haptics.light()
                         onPointAdded(0f, 0f)
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            
            // 1. Draw Technical "Blueprint" Grid (refined)
            val gridSize = 48.dp.toPx() // Slightly larger for cleaner look
            
            // Vertical lines
            var x = 0f
            while (x <= width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, height),
                    strokeWidth = 0.5.dp.toPx()
                )
                x += gridSize
            }
            
            // Horizontal lines
            var y = 0f
            while (y <= height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 0.5.dp.toPx()
                )
                y += gridSize
            }
            
            // 2. Draw Gradient Heatmap (Soft Splats) - uses consistent palette
            points.forEach { point ->
                val center = Offset(point.x * width, point.y * height)
                val rssi = point.rssi
                
                // A+ Polish: Refined color palette (still vibrant but more cohesive)
                val signalColor = when {
                    rssi > -50 -> Color(0xFF00C853) // Vibrant Green
                    rssi > -60 -> Color(0xFF64DD17) // Light Green
                    rssi > -70 -> Color(0xFFFFD600) // Bright Yellow
                    rssi > -80 -> Color(0xFFFF9100) // Orange
                    else -> Color(0xFFFF1744)       // Vivid Red
                }
                
                // Large soft glow
                val radius = 100.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            signalColor.copy(alpha = 0.5f),
                            signalColor.copy(alpha = 0.15f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = radius
                    ),
                    radius = radius,
                    center = center
                )
                
                // Intense center core
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            signalColor.copy(alpha = 0.9f),
                            signalColor.copy(alpha = 0.0f)
                        ),
                        center = center,
                        radius = 24.dp.toPx()
                    ),
                    radius = 24.dp.toPx(),
                    center = center
                )
            }

            // 3. Draw Live Path Snake (Connecting lines)
            // A) Official Points Path
            if (points.size > 1) {
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(points.first().x * width, points.first().y * height)
                
                points.forEach { p -> 
                    path.lineTo(p.x * width, p.y * height)
                }
                
                // Solid gradient stroke for premium look
                drawPath(
                    path = path,
                    color = primary.copy(alpha = 0.7f),
                    style = Stroke(
                        width = 3.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                    )
                )
            }
            
            // B) Ghost Path (Manual Mode)
            if (isRecording && ghostPath.isNotEmpty()) {
                val path = androidx.compose.ui.graphics.Path()
                path.moveTo(ghostPath.first().first * width, ghostPath.first().second * height)
                
                ghostPath.forEach { (gx, gy) ->
                    path.lineTo(gx * width, gy * height)
                }
                 
                drawPath(
                    path = path,
                    color = tertiary.copy(alpha = 0.4f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                        join = androidx.compose.ui.graphics.StrokeJoin.Round,
                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                    )
                )
            }
            
            // 4. Draw Physical Points (Markers) on top
            points.forEach { point ->
                val center = Offset(point.x * width, point.y * height)
                val rssi = point.rssi
                
                // Use semantic colors from theme
                val pointColor = when {
                    rssi > -60 -> primary
                    rssi > -80 -> tertiary
                    else -> error
                }

                // Outer ring with shadow effect
                drawCircle(
                    color = Color.White,
                    radius = 8.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = pointColor,
                    radius = 6.dp.toPx(),
                    center = center
                )
                // Inner dot
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = center
                )
            }

            // 5. A+ Polish: Pulsing "Current Location" Indicator with Heading
            if (isRecording && points.isNotEmpty()) {
                val last = points.last()
                val center = Offset(last.x * width, last.y * height)
                
                // Pulsing outer ring (animated)
                drawCircle(
                    color = error.copy(alpha = pulseAlpha),
                    radius = 20.dp.toPx() * pulseScale,
                    center = center
                )
                
                // Solid center
                drawCircle(
                    color = error,
                    radius = 8.dp.toPx(),
                    center = center
                )
                drawCircle(
                    color = Color.White,
                    radius = 3.dp.toPx(),
                    center = center
                )
                
                // Heading Arrow - rotate points manually around center
                val rad = Math.toRadians(heading.toDouble())
                val cos = kotlin.math.cos(rad).toFloat()
                val sin = kotlin.math.sin(rad).toFloat()

                fun rotatePoint(x: Float, y: Float): Offset {
                    val dx = x - center.x
                    val dy = y - center.y
                    return Offset(
                        center.x + dx * cos - dy * sin,
                        center.y + dx * sin + dy * cos
                    )
                }

                val arrowPath = androidx.compose.ui.graphics.Path().apply {
                    val tip = rotatePoint(center.x, center.y - 28.dp.toPx())
                    val right = rotatePoint(center.x + 10.dp.toPx(), center.y - 8.dp.toPx())
                    val notch = rotatePoint(center.x, center.y - 14.dp.toPx())
                    val left = rotatePoint(center.x - 10.dp.toPx(), center.y - 8.dp.toPx())

                    moveTo(tip.x, tip.y)
                    lineTo(right.x, right.y)
                    lineTo(notch.x, notch.y)
                    lineTo(left.x, left.y)
                    close()
                }
                drawPath(arrowPath, color = error)
            }
        }
        
        // A+ POLISH: Signal Legend (top-left)
        SignalLegend(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        )
        
        // A+ POLISH: Summary Stats (bottom)
        if (points.isNotEmpty() && !isRecording) {
            SurveySummaryCard(
                points = points,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
        
        // Instruction overlay when empty
        if (points.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                   shape = RoundedCornerShape(28.dp),
                   color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.95f),
                   border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                   shadowElevation = 8.dp
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp)
                    ) {
                        // Animated icon
                        val iconScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.15f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "iconPulse"
                        )
                        Icon(
                            Icons.Default.TouchApp, 
                            null, 
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp).scale(iconScale)
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Tap anywhere to map signal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "or start walking to auto-record",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
