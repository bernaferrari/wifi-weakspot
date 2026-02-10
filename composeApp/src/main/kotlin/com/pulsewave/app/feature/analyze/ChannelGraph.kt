package com.pulsewave.app.feature.analyze

import androidx.compose.animation.core.*
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
import kotlin.math.exp

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
    Color(0xFFFFEB3B), // Yellow
    Color(0xFF795548), // Brown
)

@Composable
fun ChannelGraph(
    networks: List<WifiNetwork>,
    selectedBand: FrequencyBand = FrequencyBand.BAND_2_4,
    selectedNetwork: WifiNetwork? = null,
    showHeatmap: Boolean = false,
    modifier: Modifier = Modifier.fillMaxSize(),
    onNetworkSelected: ((WifiNetwork?) -> Unit)? = null
) {
    var touchPosition by remember { mutableStateOf<Offset?>(null) }
    var isDragging by remember { mutableStateOf(false) }
    
    // Alive/Breathing Animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathScale"
    )
    
    val textMeasurer = rememberTextMeasurer()
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    
    // Network color mapping for consistency (Stable Colors)
    val networkColorMap = remember(networks) {
        networks.associate { network -> 
            // Deterministic color based on BSSID hash
            val colorIndex = kotlin.math.abs(network.bssid.hashCode()) % networkColors.size
            network.bssid to networkColors[colorIndex]
        }
    }

    Column(modifier = modifier) {
        // Chart with crosshair
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(networks) {
                        detectTapGestures(
                            onTap = { offset -> touchPosition = offset },
                            onLongPress = { offset -> touchPosition = offset },
                            onPress = { offset ->
                                touchPosition = offset
                                tryAwaitRelease()
                                touchPosition = null
                            }
                        )
                    }
                    .pointerInput(networks) {
                        detectDragGestures(
                            onDragStart = { offset -> touchPosition = offset },
                            onDrag = { change, _ -> touchPosition = change.position },
                            onDragEnd = { touchPosition = null },
                            onDragCancel = { touchPosition = null }
                        )
                    }
            ) {
                val leftPadding = 50.dp.toPx()
                val bottomPadding = 40.dp.toPx()
                val topPadding = 15.dp.toPx()
                val rightPadding = 15.dp.toPx()
                val graphWidth = size.width - leftPadding - rightPadding
                val graphHeight = size.height - bottomPadding - topPadding
                
                // Draw Y-axis grid lines (dBm levels)
                val yLevels = listOf(-30, -40, -50, -60, -70, -80, -90)
                yLevels.forEach { dBm ->
                    val yNorm = (dBm + 100f) / 80f
                    val y = topPadding + graphHeight * (1 - yNorm)
                    
                    drawLine(
                        color = outlineColor.copy(alpha = 0.3f),
                        start = Offset(leftPadding, y),
                        end = Offset(size.width - rightPadding, y),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    val label = "$dBm"
                    val labelResult = textMeasurer.measure(
                        label,
                        TextStyle(fontSize = 10.sp, color = onSurfaceColor.copy(alpha = 0.6f))
                    )
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(16.dp.toPx(), y - labelResult.size.height / 2)
                    )
                }
                
                // Draw X-axis
                drawLine(
                    color = outlineColor,
                    start = Offset(leftPadding, size.height - bottomPadding),
                    end = Offset(size.width - rightPadding, size.height - bottomPadding),
                    strokeWidth = 2.dp.toPx()
                )
                
                // Draw Y-axis
                drawLine(
                    color = outlineColor,
                    start = Offset(leftPadding, topPadding),
                    end = Offset(leftPadding, size.height - bottomPadding),
                    strokeWidth = 2.dp.toPx()
                )
                
                // X-axis channel labels
                // For 2.4GHz: Show Channels 1-14
                // For 5GHz/6GHz: Show spacing every few channels to avoid clutter
                
                val channelMarkers = mutableListOf<Pair<Int, Int>>() // Channel Number, Frequency
                
                if (selectedBand == FrequencyBand.BAND_2_4) {
                     // 1-14
                     listOf(
                        1 to 2412, 2 to 2417, 3 to 2422, 4 to 2427, 5 to 2432,
                        6 to 2437, 7 to 2442, 8 to 2447, 9 to 2452, 10 to 2457,
                        11 to 2462, 12 to 2467, 13 to 2472, 14 to 2484
                    ).forEach { channelMarkers.add(it) }
                } else if (selectedBand == FrequencyBand.BAND_5) {
                    // 5GHz channels (every 4th or 8th to save space)
                    // Common channels: 36, 40, 44, 48, 52... 165
                    val startCh = 36
                    val endCh = 165
                    val valid5G = listOf(36, 40, 44, 48, 52, 56, 60, 64, 100, 104, 108, 112, 116, 120, 124, 128, 132, 136, 140, 144, 149, 153, 157, 161, 165)
                    
                    valid5G.forEachIndexed { i, ch ->
                        // Show every 3rd label or if it's a key channel
                        if (i % 3 == 0 || ch == 36 || ch == 149) {
                            val freq = 5000 + ch * 5
                            if (freq in 5150..5925) {
                                channelMarkers.add(ch to freq)
                            }
                        }
                    }
                } else if (selectedBand == FrequencyBand.BAND_6) {
                    // 6GHz channels (start at 1, go up to 233)
                    // Freq = 5950 + 5 * channel_index
                    // Showing labels every 32 channels roughly
                    val startCh = 1
                    val endCh = 233
                    for (ch in startCh..endCh step 32) {
                        val freq = 5950 + 5 * ch
                            channelMarkers.add(ch to freq)
                    }
                }

                channelMarkers.forEach { (ch, freq) ->
                    val x = leftPadding + mapFreqToNormX(freq.toFloat(), selectedBand) * graphWidth
                    
                    // Vertical grid line for each channel
                    drawLine(
                        color = outlineColor.copy(alpha = 0.15f),
                        start = Offset(x, topPadding),
                        end = Offset(x, size.height - bottomPadding),
                        strokeWidth = 1.dp.toPx()
                    )
                    
                    // Channel label
                    val label = "$ch"
                    val labelResult = textMeasurer.measure(
                        label,
                        TextStyle(fontSize = 9.sp, color = onSurfaceColor.copy(alpha = 0.7f))
                    )
                    drawText(
                        textLayoutResult = labelResult,
                        topLeft = Offset(x - labelResult.size.width / 2, size.height - bottomPadding + 6.dp.toPx())
                    )
                }
                
                // "Channel" label
                val chLabel = if (selectedBand == FrequencyBand.BAND_6) "Channel (6GHz)" else "Channel"
                val chLabelResult = textMeasurer.measure(
                    chLabel,
                    TextStyle(fontSize = 11.sp, color = onSurfaceColor.copy(alpha = 0.5f), fontWeight = FontWeight.Medium)
                )
                
                val legendY = size.height - 10.dp.toPx()
                
                drawText(
                    textLayoutResult = chLabelResult,
                    topLeft = Offset(
                        leftPadding + graphWidth / 2 - chLabelResult.size.width / 2,
                        legendY - chLabelResult.size.height
                    )
                )
                
                // Calculate congestion for each channel marker (Heatmap logic)
                if (showHeatmap) {
                    val channelCongestion = channelMarkers.map { (ch, freq) ->
                        // Find networks within range of this channel (overlap)
                        val impacting = networks.filter { abs(it.frequency - freq) <= 10 }
                        // Heuristic: sum of signal strengths
                        val congestion = impacting.sumOf { (it.rssi + 100).coerceAtLeast(0) }
                        ch to (freq to congestion)
                    }
    
                    // Draw Heatmap Bar at the bottom of the axis
                    val barHeight = 4.dp.toPx()
                    channelCongestion.forEach { (_, data) ->
                        val (freq, score) = data
                        val x = leftPadding + mapFreqToNormX(freq.toFloat(), selectedBand) * graphWidth
                        
                        val color = when {
                            score < 40 -> Color(0xFF4CAF50).copy(alpha = 0.4f) // Healthy
                            score < 120 -> Color(0xFFFFC107).copy(alpha = 0.4f) // Busy
                            else -> Color(0xFFF44336).copy(alpha = 0.4f) // Congested
                        }
                        
                        // Box width based on band spacing
                        val boxWidth = if (selectedBand == FrequencyBand.BAND_2_4) {
                            (graphWidth / 14) * 0.8f
                        } else {
                            (graphWidth / (channelMarkers.size.coerceAtLeast(1))) * 0.6f
                        }
    
                        drawRect(
                            color = color,
                            topLeft = Offset(x - boxWidth / 2, size.height - bottomPadding - barHeight),
                            size = androidx.compose.ui.geometry.Size(boxWidth, barHeight)
                        )
                    }
                }
                
                // Draw networks (sorted by RSSI so stronger are on top)
                // Draw networks (sorted by RSSI so stronger are on top)
                val sortedNetworks = networks.sortedBy { it.rssi }
                sortedNetworks.forEach { network ->
                    val color = networkColorMap[network.bssid] ?: networkColors[0]
                    val isSelected = selectedNetwork == network
                    
                    // Ghosting Logic: If a network is selected, fade out others to 0.1f.
                    val alpha = if (selectedNetwork == null) 1f 
                               else if (isSelected) 1f 
                               else 0.1f
                    
                    drawNetworkCurve(
                        network = network,
                        color = color.copy(alpha = alpha),
                        graphLeft = leftPadding,
                        graphTop = topPadding,
                        graphWidth = graphWidth,
                        graphHeight = graphHeight,
                        band = selectedBand,
                        isSelected = isSelected,
                        breathScale = breathScale
                    )
                }
                
                // Draw crosshair if touching
                touchPosition?.let { pos ->
                    if (pos.x >= leftPadding && pos.x <= size.width - rightPadding &&
                        pos.y >= topPadding && pos.y <= size.height - bottomPadding) {
                        
                        // Vertical line
                        drawLine(
                            color = primaryColor,
                            start = Offset(pos.x, topPadding),
                            end = Offset(pos.x, size.height - bottomPadding),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                        
                        // Horizontal line
                        drawLine(
                            color = primaryColor,
                            start = Offset(leftPadding, pos.y),
                            end = Offset(size.width - rightPadding, pos.y),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                        
                        // Center dot
                        drawCircle(color = primaryColor, radius = 6.dp.toPx(), center = pos)
                        drawCircle(color = surfaceColor, radius = 4.dp.toPx(), center = pos)
                        
                        // Calculate values at touch
                        val normX = (pos.x - leftPadding) / graphWidth
                        val touchFreq = mapNormXToFreq(normX, selectedBand)
                        val touchDbm = -100 + 80 * (1 - (pos.y - topPadding) / graphHeight)
                        val channel = frequencyToChannel(touchFreq.toInt())
                        
                        // Tooltip
                        val tooltipText = "Ch $channel • ${touchDbm.toInt()} dBm"
                        val tooltipResult = textMeasurer.measure(
                            tooltipText,
                            TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = surfaceColor)
                        )
                        
                        val tooltipX = (pos.x - tooltipResult.size.width / 2).coerceIn(
                            leftPadding, size.width - rightPadding - tooltipResult.size.width
                        )
                        val tooltipY = (pos.y - tooltipResult.size.height - 20.dp.toPx()).coerceAtLeast(topPadding)
                        
                        drawRoundRect(
                            color = primaryColor,
                            topLeft = Offset(tooltipX - 10.dp.toPx(), tooltipY - 6.dp.toPx()),
                            size = androidx.compose.ui.geometry.Size(
                                tooltipResult.size.width + 20.dp.toPx(),
                                tooltipResult.size.height + 12.dp.toPx()
                            ),
                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx())
                        )
                        
                        drawText(textLayoutResult = tooltipResult, topLeft = Offset(tooltipX, tooltipY))
                        
                        // Auto-select closest network
                        val closestNetwork = findNetworkAtFrequency(networks, touchFreq)
                        if (closestNetwork != null && closestNetwork != selectedNetwork) {
                            onNetworkSelected?.invoke(closestNetwork)
                        }
                    }
                }
            }
            
            // Selected network info overlay
            selectedNetwork?.let { network ->
                val color = networkColorMap[network.bssid] ?: networkColors[0]
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    shape = PulseWaveShapes.small,
                    color = color.copy(alpha = PulseWaveAlpha.emphasized + 0.08f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            network.ssid.ifEmpty { "Hidden Network" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "${network.rssi} dBm • Ch ${frequencyToChannel(network.frequency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
        
        // Legend - scrollable, full names
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
                    val isSelected = selectedNetwork == network
                    
                    Surface(
                        onClick = { 
                            onNetworkSelected?.invoke(if (isSelected) null else network)
                        },
                        shape = PulseWaveShapes.extraSmall,
                        color = if (isSelected) color.copy(alpha = PulseWaveAlpha.light) else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(color, PulseWaveShapes.extraSmall)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                network.ssid.ifEmpty { "Hidden" },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun frequencyToChannel(frequency: Int): Int {
    return when {
        frequency in 2400..2500 -> ((frequency - 2412) / 5) + 1
        frequency in 5150..5350 -> ((frequency - 5180) / 5) + 36
        frequency in 5470..5730 -> ((frequency - 5500) / 5) + 100
        frequency in 5735..5900 -> ((frequency - 5745) / 5) + 149
        frequency in 5925..7125 -> ((frequency - 5950) / 5) + 1
        else -> 0
    }
}

private fun findNetworkAtFrequency(networks: List<WifiNetwork>, freq: Float): WifiNetwork? {
    return networks.minByOrNull { network ->
        val channelWidth = if (network.channelWidth > 0) network.channelWidth else 20
        val distance = abs(network.frequency - freq)
        if (distance <= channelWidth) distance else Float.MAX_VALUE
    }
}

private fun DrawScope.drawNetworkCurve(
    network: WifiNetwork,
    color: Color,
    graphLeft: Float,
    graphTop: Float,
    graphWidth: Float,
    graphHeight: Float,
    band: FrequencyBand,
    isSelected: Boolean,
    breathScale: Float
) {
    val centerFreq = network.frequency.toFloat()
    // NARROWER curves - half the channel width for tighter parabolas
    val curveWidth = if (network.channelWidth > 0) network.channelWidth.toFloat() / 2f else 10f
    val sigma = curveWidth / 2.5f

    val path = Path()
    val startFreq = centerFreq - curveWidth * 1.5f
    val endFreq = centerFreq + curveWidth * 1.5f
    val steps = 40
    
    var firstPoint = true
    var maxX = Float.MIN_VALUE
    
    for (i in 0..steps) {
        val currentFreq = startFreq + (endFreq - startFreq) * (i.toFloat() / steps)
        
        val xRel = currentFreq - centerFreq
        val yNormal = exp(-(xRel * xRel) / (2 * sigma * sigma))
        
        val normX = mapFreqToNormX(currentFreq, band)
        // Clip points outside valid range
        if (normX < 0f || normX > 1f) continue
        
        val xPx = graphLeft + normX * graphWidth
        
        val rssi = network.rssi.coerceIn(-100, -20)
        // Breathing Amplitude: Multiply base amplitude by breathScale
        val amplitude = ((rssi + 100) / 80f) * breathScale
        val yPx = graphTop + graphHeight * (1 - amplitude * yNormal)
        
        maxX = maxOf(maxX, xPx)
        
        if (firstPoint) {
            path.moveTo(xPx, graphTop + graphHeight)
            path.lineTo(xPx, yPx)
            firstPoint = false
        } else {
            path.lineTo(xPx, yPx)
        }
    }
    
    path.lineTo(maxX, graphTop + graphHeight)
    path.close()
    
    // Fill with Gradient
    val fillAlpha = if (isSelected) 0.6f else 0.3f
    val brush = Brush.verticalGradient(
        colors = listOf(
            color.copy(alpha = fillAlpha),
            color.copy(alpha = 0.05f) // Fade to transparent-ish
        ),
        startY = graphTop,
        endY = graphTop + graphHeight
    )
    drawPath(path, brush)
    
    // Stroke
    val strokePath = Path()
    firstPoint = true
    for (i in 0..steps) {
        val currentFreq = startFreq + (endFreq - startFreq) * (i.toFloat() / steps)
        
        val normX = mapFreqToNormX(currentFreq, band)
        if (normX < 0f || normX > 1f) continue
        
        val xRel = currentFreq - centerFreq
        val yNormal = exp(-(xRel * xRel) / (2 * sigma * sigma))
        val xPx = graphLeft + normX * graphWidth
        val rssi = network.rssi.coerceIn(-100, -20)
        // Breathing Amplitude
        val amplitude = ((rssi + 100) / 80f) * breathScale
        val yPx = graphTop + graphHeight * (1 - amplitude * yNormal)
        
        if (firstPoint) {
            strokePath.moveTo(xPx, yPx)
            firstPoint = false
        } else {
            strokePath.lineTo(xPx, yPx)
        }
    }
    
    drawPath(
        strokePath, 
        color, 
        style = Stroke(width = if (isSelected) 4.dp.toPx() else 2.5.dp.toPx())
    )
}

// Smart frequency mapping that eliminates gaps
// 2.4GHz: Uses actual channel frequencies (2412-2484) with minimal padding
// 5GHz: Compresses the DFS gap (5350-5470) to reduce visual jump
private fun mapFreqToNormX(freq: Float, band: FrequencyBand): Float {
    return when (band) {
        FrequencyBand.BAND_2_4 -> {
            // "Uniform Spacing" Logic:
            // Physically, Ch 13 is 2472MHz and Ch 14 is 2484MHz (12MHz gap).
            // Visually, we want them to look like 5MHz apart (standard spacing).
            // We map frequencies > 2472 to be shifted left by 7MHz (12 - 5).
            
            // Defines the visual range. 
            // Start: 2407 (Ch 1 - 5MHz)
            // End: 2484 (Ch 14) - 7 = 2477 + 5MHz padding = 2482.
            val startF = 2407f
            val endF = 2482f
            
            val visualFreq = if (freq > 2472f) freq - 7f else freq
            (visualFreq - startF) / (endF - startF)
        }
        FrequencyBand.BAND_5 -> {
            // 5GHz has a gap from 5350 (ch64) to 5470 (ch100)
            // We compress this gap to make the chart more readable
            when {
                freq <= 5350f -> {
                    // UNII-1 & UNII-2A: 5150-5350 → 0.0 to 0.4
                    (freq - 5150f) / (5350f - 5150f) * 0.4f
                }
                freq >= 5470f -> {
                    // UNII-2C & UNII-3: 5470-5850 → 0.45 to 1.0
                    0.45f + ((freq - 5470f) / (5850f - 5470f) * 0.55f)
                }
                else -> {
                    // In the gap, interpolate
                    0.4f + ((freq - 5350f) / (5470f - 5350f) * 0.05f)
                }
            }
        }
        FrequencyBand.BAND_6 -> (freq - 5925f) / (7125f - 5925f)
    }
}

private fun mapNormXToFreq(normX: Float, band: FrequencyBand): Float {
    return when (band) {
        FrequencyBand.BAND_2_4 -> {
            val startF = 2407f
            val endF = 2482f
            val visualFreq = startF + normX * (endF - startF)
            
            // Inverse the shift: if visualFreq > 2472, add 7 back
            if (visualFreq > 2472f) visualFreq + 7f else visualFreq
        }
        FrequencyBand.BAND_5 -> {
            when {
                normX <= 0.4f -> 5150f + (normX / 0.4f) * (5350f - 5150f)
                normX >= 0.45f -> 5470f + ((normX - 0.45f) / 0.55f) * (5850f - 5470f)
                else -> 5350f + ((normX - 0.4f) / 0.05f) * (5470f - 5350f)
            }
        }
        FrequencyBand.BAND_6 -> 5925f + normX * (7125f - 5925f)
    }
}
