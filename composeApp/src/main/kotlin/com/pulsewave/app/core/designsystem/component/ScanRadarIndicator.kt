package com.pulsewave.app.core.designsystem.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.wifi.ThrottleState
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * A beautiful radar-style scan indicator that shows:
 * - Quota remaining (dots/pips around the radar)
 * - Countdown timer when exhausted (sweeping hand)
 * - Unlimited badge when Developer Mode is active
 */
@Composable
fun ScanRadarIndicator(
    throttleState: ThrottleState,
    onScanNow: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    
    // Radar sweep animation (only when exhausted/waiting)
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep"
    )
    
    // Pulse animation for ready state
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val primaryColor = MaterialTheme.colorScheme.primary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary
    val errorColor = MaterialTheme.colorScheme.error
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    val stateColor = when (throttleState) {
        is ThrottleState.Ready -> primaryColor
        is ThrottleState.LowQuota -> tertiaryColor
        is ThrottleState.Exhausted -> errorColor
        is ThrottleState.Unlimited -> MaterialTheme.colorScheme.secondary
        is ThrottleState.Unknown -> surfaceColor
    }
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val clickScale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.4f, stiffness = 400f),
        label = "clickScale"
    )
    
    val isClickable = throttleState !is ThrottleState.Unknown
    
    Card(
        modifier = modifier
            .size(size)
            .scale(clickScale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = isClickable,
                onClick = onScanNow
            ),
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Radar rings and sweep
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                val center = Offset(this.size.width / 2, this.size.height / 2)
                val maxRadius = minOf(this.size.width, this.size.height) / 2
                
                // Draw concentric rings
                for (i in 1..3) {
                    val radius = maxRadius * (i / 3f)
                    drawCircle(
                        color = stateColor.copy(alpha = 0.1f),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                
                // Draw crosshairs
                val crossColor = stateColor.copy(alpha = 0.2f)
                drawLine(
                    color = crossColor,
                    start = Offset(center.x, 0f),
                    end = Offset(center.x, this.size.height),
                    strokeWidth = 1.dp.toPx()
                )
                drawLine(
                    color = crossColor,
                    start = Offset(0f, center.y),
                    end = Offset(this.size.width, center.y),
                    strokeWidth = 1.dp.toPx()
                )
                
                // Draw quota pips (4 dots around the edge)
                val pipRadius = 6.dp.toPx()
                val pipDistance = maxRadius - 8.dp.toPx()
                val scansRemaining = when (throttleState) {
                    is ThrottleState.Ready -> throttleState.scansRemaining
                    is ThrottleState.LowQuota -> throttleState.scansRemaining
                    is ThrottleState.Exhausted -> 0
                    is ThrottleState.Unlimited -> 4
                    is ThrottleState.Unknown -> 0
                }
                
                for (i in 0 until 4) {
                    val angle = (i * 90f - 90f) * (PI / 180f)
                    val pipCenter = Offset(
                        center.x + pipDistance * cos(angle).toFloat(),
                        center.y + pipDistance * sin(angle).toFloat()
                    )
                    val pipColor = if (i < scansRemaining) stateColor else stateColor.copy(alpha = 0.2f)
                    drawCircle(
                        color = pipColor,
                        radius = pipRadius,
                        center = pipCenter
                    )
                }
                
                // Radar sweep (when exhausted)
                if (throttleState is ThrottleState.Exhausted) {
                    rotate(sweepAngle, pivot = center) {
                        // Draw gradient sweep trail
                        val sweepBrush = Brush.sweepGradient(
                            colorStops = arrayOf(
                                0.0f to Color.Transparent,
                                0.7f to Color.Transparent,
                                1.0f to errorColor.copy(alpha = 0.4f)
                            ),
                            center = center
                        )
                        drawCircle(
                            brush = sweepBrush,
                            radius = maxRadius - 16.dp.toPx(),
                            center = center
                        )
                        
                        // Draw sweep line
                        drawLine(
                            color = errorColor,
                            start = center,
                            end = Offset(center.x, 16.dp.toPx()),
                            strokeWidth = 2.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
                
                // Pulse ring (when ready)
                if (throttleState is ThrottleState.Ready || throttleState is ThrottleState.LowQuota) {
                    val pulseRadius = maxRadius * 0.6f * pulseScale
                    drawCircle(
                        color = stateColor.copy(alpha = 0.15f),
                        radius = pulseRadius,
                        center = center
                    )
                }
            }
            
            // Center content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (throttleState) {
                    is ThrottleState.Ready -> {
                        Text(
                            "READY",
                            style = MaterialTheme.typography.labelLarge,
                            color = stateColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${throttleState.scansRemaining} scans",
                            style = MaterialTheme.typography.bodySmall,
                            color = onSurfaceColor.copy(alpha = 0.7f)
                        )
                    }
                    is ThrottleState.LowQuota -> {
                        Text(
                            "${throttleState.scansRemaining}",
                            style = MaterialTheme.typography.displaySmall,
                            color = stateColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "left",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceColor.copy(alpha = 0.7f)
                        )
                    }
                    is ThrottleState.Exhausted -> {
                        val minutes = throttleState.secondsUntilNextScan / 60
                        val seconds = throttleState.secondsUntilNextScan % 60
                        Text(
                            "$minutes:${"%02d".format(seconds)}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = stateColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "waiting",
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceColor.copy(alpha = 0.7f)
                        )
                    }
                    is ThrottleState.Unlimited -> {
                        Text(
                            "∞",
                            style = MaterialTheme.typography.displaySmall,
                            color = stateColor,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "DEV MODE",
                            style = MaterialTheme.typography.labelSmall,
                            color = stateColor.copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    is ThrottleState.Unknown -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
            
            // "Tap to scan" hint (only when ready)
            if (throttleState is ThrottleState.Ready || 
                throttleState is ThrottleState.LowQuota ||
                throttleState is ThrottleState.Unlimited) {
                Text(
                    "Tap to scan",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceColor.copy(alpha = 0.5f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                )
            }
        }
    }
}

/**
 * Compact status bar version for inline display.
 */
@Composable
fun ScanThrottleStatusBar(
    throttleState: ThrottleState,
    onScanNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stateColor = when (throttleState) {
        is ThrottleState.Ready -> MaterialTheme.colorScheme.primary
        is ThrottleState.LowQuota -> MaterialTheme.colorScheme.tertiary
        is ThrottleState.Exhausted -> MaterialTheme.colorScheme.error
        is ThrottleState.Unlimited -> MaterialTheme.colorScheme.secondary
        is ThrottleState.Unknown -> MaterialTheme.colorScheme.outline
    }
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = stateColor.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status info
            Column {
                val statusText = when (throttleState) {
                    is ThrottleState.Ready -> "Ready to scan"
                    is ThrottleState.LowQuota -> "${throttleState.scansRemaining} scans remaining"
                    is ThrottleState.Exhausted -> {
                        val mins = throttleState.secondsUntilNextScan / 60
                        val secs = throttleState.secondsUntilNextScan % 60
                        "Next scan in $mins:${"%02d".format(secs)}"
                    }
                    is ThrottleState.Unlimited -> "Developer Mode: Unlimited"
                    is ThrottleState.Unknown -> "Checking..."
                }
                
                Text(
                    statusText,
                    style = MaterialTheme.typography.titleSmall,
                    color = stateColor,
                    fontWeight = FontWeight.Bold
                )
                
                if (throttleState is ThrottleState.Ready || throttleState is ThrottleState.LowQuota) {
                    // Quota dots
                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val remaining = when (throttleState) {
                            is ThrottleState.Ready -> throttleState.scansRemaining
                            is ThrottleState.LowQuota -> throttleState.scansRemaining
                            else -> 0
                        }
                        for (i in 0 until 4) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i < remaining) stateColor 
                                        else stateColor.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }
                }
            }
            
            // Scan button
            if (throttleState !is ThrottleState.Exhausted && throttleState !is ThrottleState.Unknown) {
                FilledTonalButton(
                    onClick = onScanNow,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Scan Now")
                }
            }
        }
    }
}
