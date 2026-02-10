package com.pulsewave.app.feature.analyze

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import com.pulsewave.app.core.designsystem.PulseWaveAlpha
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsewave.app.core.wifi.WifiNetwork
import kotlin.math.abs

// Vibrant, distinct colors for different networks
private val networkColors = listOf(
    Color(0xFF4CAF50), // Green
    Color(0xFF2196F3), // Blue
    Color(0xFFF44336), // Red
    Color(0xFFFF9800), // Orange
    Color(0xFF9C27B0), // Purple
    Color(0xFF00BCD4), // Cyan
    Color(0xFFE91E63), // Pink
    Color(0xFF3F51B5), // Indigo
    Color(0xFF009688), // Teal
    Color(0xFF795548), // Brown
)

/**
 * Real-time Signal History Graph
 * Shows RSSI values over time for each network, creating a live "heartbeat" view
 * that helps users understand which networks are most stable.
 */
@Composable
fun SignalHistoryGraph(
    history: Map<String, List<Int>>, // BSSID -> List of RSSI readings
    networks: List<WifiNetwork>,
    modifier: Modifier = Modifier.fillMaxSize(),
    onNetworkSelected: ((WifiNetwork?) -> Unit)? = null
) {
    var selectedBssid by remember { mutableStateOf<String?>(null) }
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    
    val textMeasurer = rememberTextMeasurer()
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    // Network color mapping
    val networkColorMap = remember(networks) {
        networks.mapIndexed { index, network -> 
            network.bssid to networkColors[index % networkColors.size]
        }.toMap()
    }
    
    // Calculate stability for each network (lower is more stable)
    val stabilityScores = remember(history) {
        history.mapValues { (_, rssiList) ->
            if (rssiList.size < 2) 0f
            else {
                val variance = rssiList.zipWithNext { a, b -> abs(a - b) }.average()
                variance.toFloat()
            }
        }
    }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(networks) {
                        detectTapGestures { offset ->
                            touchPosition = offset
                        }
                    }
                    .pointerInput(networks) {
                        detectDragGestures(
                            onDrag = { change, _ -> touchPosition = change.position },
                            onDragEnd = { touchPosition = null }
                        )
                    }
            ) {
                val leftPadding = 50.dp.toPx()
                val bottomPadding = 45.dp.toPx()
                val topPadding = 20.dp.toPx()
                val rightPadding = 15.dp.toPx()
                val graphWidth = size.width - leftPadding - rightPadding
                val graphHeight = size.height - bottomPadding - topPadding
                
                val maxPoints = 60 // Show last 60 readings (about 2 minutes at 2s intervals)
                
                // Draw decorative gradient background
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.03f),
                            Color.Transparent
                        )
                    )
                )
                
                // Draw Y-axis grid lines (dBm levels) with zone colors
                val yLevels = listOf(
                    -30 to "Excellent" to Color(0xFF4CAF50).copy(alpha = 0.1f),
                    -50 to "Good" to Color(0xFF8BC34A).copy(alpha = 0.1f),
                    -70 to "Fair" to Color(0xFFFF9800).copy(alpha = 0.1f),
                    -90 to "Weak" to Color(0xFFF44336).copy(alpha = 0.1f)
                )
                
                yLevels.forEachIndexed { index, (labelPair, zoneColor) ->
                    val (dBm, label) = labelPair
                    val yNorm = (dBm + 100f) / 80f
                    val y = topPadding + graphHeight * (1 - yNorm)
                    
                    // Zone background
                    if (index < yLevels.size - 1) {
                        val nextY = topPadding + graphHeight * (1 - (yLevels[index + 1].first.first + 100f) / 80f)
                        drawRect(
                            color = zoneColor,
                            topLeft = Offset(leftPadding, y),
                            size = androidx.compose.ui.geometry.Size(graphWidth, nextY - y)
                        )
                    }
                    
                    // Grid line
                    drawLine(
                        color = outlineColor.copy(alpha = 0.4f),
                        start = Offset(leftPadding, y),
                        end = Offset(size.width - rightPadding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    // dBm label
                    val dBmLabel = "$dBm"
                    val labelResult = textMeasurer.measure(
                        dBmLabel,
                        TextStyle(fontSize = 10.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    )
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(4.dp.toPx(), y - labelResult.size.height / 2)
                    )
                    
                    // Zone label (Excellent, Good, etc.)
                    val zoneLabelResult = textMeasurer.measure(
                        label,
                        TextStyle(fontSize = 8.sp, color = onSurfaceColor.copy(alpha = 0.4f))
                    )
                    drawText(
                        textLayoutResult = zoneLabelResult,
                        topLeft = Offset(size.width - rightPadding - zoneLabelResult.size.width - 4.dp.toPx(), y + 4.dp.toPx())
                    )
                }
                
                // Draw axes
                drawLine(
                    color = outlineColor,
                    start = Offset(leftPadding, topPadding),
                    end = Offset(leftPadding, size.height - bottomPadding),
                    strokeWidth = 2.dp.toPx()
                )
                drawLine(
                    color = outlineColor,
                    start = Offset(leftPadding, size.height - bottomPadding),
                    end = Offset(size.width - rightPadding, size.height - bottomPadding),
                    strokeWidth = 2.dp.toPx()
                )
                
                // Time labels on X-axis
                val timeLabels = listOf("Now", "-30s", "-1m", "-1.5m", "-2m")
                timeLabels.forEachIndexed { index, label ->
                    val x = leftPadding + graphWidth * (1 - index.toFloat() / (timeLabels.size - 1))
                    val labelResult = textMeasurer.measure(
                        label,
                        TextStyle(fontSize = 9.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    )
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(x - labelResult.size.width / 2, size.height - bottomPadding + 8.dp.toPx())
                    )
                }
                
                // "Time" label
                val timeLabel = "← Time"
                val timeLabelResult = textMeasurer.measure(
                    timeLabel,
                    TextStyle(fontSize = 10.sp, color = onSurfaceColor.copy(alpha = 0.5f))
                )
                drawText(
                    textLayoutResult = timeLabelResult,
                    topLeft = Offset(leftPadding + graphWidth / 2 - timeLabelResult.size.width / 2, size.height - 12.dp.toPx())
                )
                
                // Draw signal lines for each network
                var colorIndex = 0
                history.forEach { (bssid, rssiList) ->
                    if (rssiList.size < 2) {
                        colorIndex++
                        return@forEach
                    }
                    
                    val color = networkColorMap[bssid] ?: networkColors[colorIndex % networkColors.size]
                    val isSelected = selectedBssid == bssid
                    val alpha = if (selectedBssid == null || isSelected) 1f else 0.3f
                    
                    val path = Path()
                    val fillPath = Path()
                    val pointsToDraw = rssiList.takeLast(maxPoints)
                    
                    pointsToDraw.forEachIndexed { index, rssi ->
                        val x = leftPadding + graphWidth * (index.toFloat() / (maxPoints - 1))
                        val yNorm = (rssi + 100f) / 80f
                        val y = topPadding + graphHeight * (1 - yNorm)
                        
                        if (index == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, size.height - bottomPadding)
                            fillPath.lineTo(x, y)
                        } else {
                            path.lineTo(x, y)
                            fillPath.lineTo(x, y)
                        }
                    }
                    
                    // Close fill path
                    val lastX = leftPadding + graphWidth * ((pointsToDraw.size - 1).toFloat() / (maxPoints - 1))
                    fillPath.lineTo(lastX, size.height - bottomPadding)
                    fillPath.close()
                    
                    // Draw gradient fill
                    drawPath(
                        fillPath, 
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                color.copy(alpha = if (isSelected) 0.4f else 0.15f * alpha),
                                color.copy(alpha = 0.02f)
                            )
                        )
                    )
                    
                    // Draw line
                    drawPath(
                        path = path,
                        color = color.copy(alpha = alpha),
                        style = Stroke(
                            width = if (isSelected) 4.dp.toPx() else 2.5.dp.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                    
                    // Draw current value dot at the end
                    val lastRssi = pointsToDraw.lastOrNull() ?: return@forEach
                    val lastY = topPadding + graphHeight * (1 - (lastRssi + 100f) / 80f)
                    
                    // Pulsing dot effect
                    drawCircle(
                        color = color.copy(alpha = 0.3f * alpha),
                        radius = 8.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = color.copy(alpha = alpha),
                        radius = 5.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                    drawCircle(
                        color = surfaceColor,
                        radius = 2.5.dp.toPx(),
                        center = Offset(lastX, lastY)
                    )
                    
                    colorIndex++
                }
                
                // Draw vertical time marker on touch
                touchPosition?.let { pos ->
                    if (pos.x >= leftPadding && pos.x <= size.width - rightPadding) {
                        drawLine(
                            color = primaryColor,
                            start = Offset(pos.x, topPadding),
                            end = Offset(pos.x, size.height - bottomPadding),
                            strokeWidth = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                        )
                        
                        // Calculate which time point
                        val timeIndex = ((pos.x - leftPadding) / graphWidth * maxPoints).toInt()
                        val secondsAgo = ((maxPoints - timeIndex) * 2).coerceAtLeast(0)
                        
                        val timeText = when {
                            secondsAgo == 0 -> "Now"
                            secondsAgo < 60 -> "${secondsAgo}s ago"
                            else -> "${secondsAgo / 60}m ${secondsAgo % 60}s ago"
                        }
                        
                        val timeResult = textMeasurer.measure(
                            timeText,
                            TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold, color = surfaceColor)
                        )
                        
                        val tooltipX = (pos.x - timeResult.size.width / 2).coerceIn(
                            leftPadding, size.width - rightPadding - timeResult.size.width
                        )
                        
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(tooltipX - 8.dp.toPx(), topPadding - 4.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(
                                timeResult.size.width + 16.dp.toPx(),
                                timeResult.size.height + 8.dp.toPx()
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
                        )
                        
                        drawText(textLayoutResult = timeResult, topLeft = Offset(tooltipX, topPadding))
                    }
                }
            }
        }
        
        // Legend with stability indicators
        if (networks.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                networks.forEach { network ->
                    val color = networkColorMap[network.bssid] ?: networkColors[0]
                    val isSelected = selectedBssid == network.bssid
                    val stability = stabilityScores[network.bssid] ?: 0f
                    val stabilityLabel = when {
                        stability < 2 -> "Stable"
                        stability < 5 -> "Variable"
                        else -> "Unstable"
                    }
                    
                    Surface(
                        onClick = { 
                            selectedBssid = if (selectedBssid == network.bssid) null else network.bssid
                            onNetworkSelected?.invoke(if (selectedBssid != null) network else null)
                        },
                        shape = PulseWaveShapes.small,
                        color = if (isSelected) color.copy(alpha = PulseWaveAlpha.subtle + 0.05f) else Color.Transparent
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(color, PulseWaveShapes.extraSmall)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    network.ssid.ifEmpty { "Hidden" },
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            Text(
                                "${network.rssi}dBm • $stabilityLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
