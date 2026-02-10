package com.pulsewave.app.core.designsystem.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * PulseWave Signature Radar Animation
 * 
 * A beautiful, brand-defining radar animation that pulses with WiFi-like waves.
 * Use this as loading indicator, empty state decoration, or brand element.
 */
@Composable
fun PulseWaveRadar(
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    isAnimating: Boolean = true,
    primaryColor: Color = MaterialTheme.colorScheme.primary,
    secondaryColor: Color = MaterialTheme.colorScheme.tertiary
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsewave_radar")
    
    // Main rotation
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    // Pulse rings (3 staggered)
    val ring1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring1"
    )
    val ring2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing, delayMillis = 666),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring2"
    )
    val ring3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing, delayMillis = 1333),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring3"
    )
    
    // Breathing glow
    val glow by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val maxRadius = minOf(this.size.width, this.size.height) / 2
        
        // Draw expanding pulse rings
        listOf(ring1, ring2, ring3).forEach { progress ->
            val ringRadius = maxRadius * 0.3f + (maxRadius * 0.7f * progress)
            val ringAlpha = (1f - progress) * 0.4f
            drawCircle(
                color = primaryColor.copy(alpha = ringAlpha),
                radius = ringRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }
        
        // Center glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = glow),
                    primaryColor.copy(alpha = 0f)
                ),
                center = center,
                radius = maxRadius * 0.4f
            ),
            radius = maxRadius * 0.4f,
            center = center
        )
        
        // Rotating sweep beam
        if (isAnimating) {
            rotate(rotation, pivot = center) {
                // Draw gradient sweep
                val sweepPath = Path().apply {
                    moveTo(center.x, center.y)
                    lineTo(center.x, center.y - maxRadius * 0.8f)
                }
                drawPath(
                    path = sweepPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            primaryColor,
                            primaryColor.copy(alpha = 0f)
                        ),
                        startY = center.y - maxRadius * 0.8f,
                        endY = center.y
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
                
                // Sweep arc trail
                drawArc(
                    brush = Brush.sweepGradient(
                        colorStops = arrayOf(
                            0.0f to Color.Transparent,
                            0.85f to Color.Transparent,
                            1.0f to primaryColor.copy(alpha = 0.3f)
                        ),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = true,
                    topLeft = Offset(center.x - maxRadius * 0.7f, center.y - maxRadius * 0.7f),
                    size = androidx.compose.ui.geometry.Size(maxRadius * 1.4f, maxRadius * 1.4f)
                )
            }
        }
        
        // Center dot
        drawCircle(
            color = primaryColor,
            radius = 4.dp.toPx(),
            center = center
        )
        
        // Draw signal arcs (WiFi-style)
        val arcAngles = listOf(-30f, -15f, 0f, 15f, 30f)
        arcAngles.forEach { offsetAngle ->
            for (i in 1..3) {
                val arcRadius = maxRadius * 0.15f * i
                val arcAlpha = (0.6f - i * 0.15f).coerceAtLeast(0.1f)
                val startAngle = 270f - 20f + offsetAngle
                val sweepAngle = 40f
                
                drawArc(
                    color = secondaryColor.copy(alpha = arcAlpha),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                    size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

/**
 * Compact loading indicator using the signature radar style
 */
@Composable
fun PulseWaveLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    PulseWaveRadar(
        modifier = modifier,
        size = size,
        isAnimating = true
    )
}
