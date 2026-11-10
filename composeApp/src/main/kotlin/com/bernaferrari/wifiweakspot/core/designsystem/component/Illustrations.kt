package com.bernaferrari.wifiweakspot.core.designsystem.component

import com.bernaferrari.wifiweakspot.core.designsystem.animateAccessibleFloat
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom illustration components for empty states.
 * These replace generic icons with hand-crafted, animated illustrations.
 */

/**
 * LAN Device illustration - connected nodes
 */
@Composable
fun LanDeviceIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "lan_illustration")

    val pulse by infiniteTransition.animateAccessibleFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulse"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val surface = MaterialTheme.colorScheme.surfaceContainerHigh

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val nodeRadius = 12.dp.toPx()
        val orbitRadius = this.size.width * 0.3f

        // Draw connecting lines first
        val nodePositions = (0 until 5).map { i ->
            val angle = (i * 72f - 90f) * (PI / 180f)
            Offset(
                center.x + orbitRadius * cos(angle).toFloat(),
                center.y + orbitRadius * sin(angle).toFloat()
            )
        }

        // Lines from center to nodes
        nodePositions.forEach { pos ->
            drawLine(
                color = primary.copy(alpha = 0.3f),
                start = center,
                end = pos,
                strokeWidth = 2.dp.toPx()
            )
        }

        // Lines between adjacent nodes
        nodePositions.forEachIndexed { i, pos ->
            val nextPos = nodePositions[(i + 1) % nodePositions.size]
            drawLine(
                color = tertiary.copy(alpha = 0.2f),
                start = pos,
                end = nextPos,
                strokeWidth = 1.dp.toPx()
            )
        }

        // Animated data pulse traveling along a line
        val pulseNodeIndex = (pulse * 5).toInt() % 5
        val pulsePos = nodePositions[pulseNodeIndex]
        val pulseProgress = (pulse * 5) % 1f
        val pulseOffset = Offset(
            center.x + (pulsePos.x - center.x) * pulseProgress,
            center.y + (pulsePos.y - center.y) * pulseProgress
        )
        drawCircle(
            color = primary,
            radius = 4.dp.toPx(),
            center = pulseOffset
        )

        // Draw outer nodes
        val nodeColors = listOf(primary, secondary, tertiary, primary.copy(alpha = 0.7f), secondary.copy(alpha = 0.7f))
        nodePositions.forEachIndexed { i, pos ->
            // Node background
            drawCircle(
                color = surface,
                radius = nodeRadius,
                center = pos
            )
            // Node border
            drawCircle(
                color = nodeColors[i],
                radius = nodeRadius,
                center = pos,
                style = Stroke(width = 2.dp.toPx())
            )
            // Node inner dot
            drawCircle(
                color = nodeColors[i],
                radius = 4.dp.toPx(),
                center = pos
            )
        }

        // Center hub
        drawCircle(
            color = primary,
            radius = nodeRadius * 1.3f,
            center = center
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.3f),
            radius = nodeRadius * 0.5f,
            center = center
        )
    }
}

/**
 * Survey/Heatmap illustration - floor plan with signal spots
 */
@Composable
fun SurveyIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "survey_illustration")

    val spot1 by infiniteTransition.animateAccessibleFloat(
        initialValue = 0.4f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "spot1"
    )
    val spot2 by infiniteTransition.animateAccessibleFloat(
        initialValue = 0.5f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ), label = "spot2"
    )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val error = MaterialTheme.colorScheme.error
    val outline = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier.size(size)) {
        val padding = 16.dp.toPx()
        val left = padding
        val top = padding
        val right = this.size.width - padding
        val bottom = this.size.height - padding

        // Floor plan outline
        val floorPath = Path().apply {
            moveTo(left, top)
            lineTo(right, top)
            lineTo(right, bottom * 0.6f)
            lineTo(right * 0.6f, bottom * 0.6f)
            lineTo(right * 0.6f, bottom)
            lineTo(left, bottom)
            close()
        }
        drawPath(
            path = floorPath,
            color = outline.copy(alpha = 0.3f),
            style = Stroke(width = 2.dp.toPx())
        )

        // Interior walls
        drawLine(
            color = outline.copy(alpha = 0.2f),
            start = Offset(left, bottom * 0.5f),
            end = Offset(right * 0.4f, bottom * 0.5f),
            strokeWidth = 2.dp.toPx()
        )

        // Signal heatmap spots
        val spots = listOf(
            Triple(Offset(left + 30.dp.toPx(), top + 30.dp.toPx()), primary, spot1),
            Triple(Offset(right - 40.dp.toPx(), top + 40.dp.toPx()), tertiary, spot2),
            Triple(Offset(left + 40.dp.toPx(), bottom - 30.dp.toPx()), error, 0.5f)
        )

        spots.forEach { (center, color, alpha) ->
            // Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = alpha * 0.6f), Color.Transparent),
                    center = center,
                    radius = 30.dp.toPx()
                ),
                radius = 30.dp.toPx(),
                center = center
            )
            // Core
            drawCircle(color = color, radius = 6.dp.toPx(), center = center)
        }
    }
}

/**
 * History/Clock illustration - timeline with events
 */
@Composable
fun HistoryIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "history_illustration")

    val handRotation by infiniteTransition.animateAccessibleFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "hand"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val surface = MaterialTheme.colorScheme.surfaceContainerHigh
    val outline = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2, this.size.height / 2)
        val radius = this.size.width * 0.4f

        // Clock face
        drawCircle(color = surface, radius = radius, center = center)
        drawCircle(color = outline.copy(alpha = 0.3f), radius = radius, center = center, style = Stroke(2.dp.toPx()))

        // Hour markers
        for (i in 0 until 12) {
            val angle = (i * 30f - 90f) * (PI / 180f)
            val innerR = radius - 10.dp.toPx()
            val outerR = radius - 4.dp.toPx()
            drawLine(
                color = outline.copy(alpha = 0.5f),
                start = Offset(center.x + innerR * cos(angle).toFloat(), center.y + innerR * sin(angle).toFloat()),
                end = Offset(center.x + outerR * cos(angle).toFloat(), center.y + outerR * sin(angle).toFloat()),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Hour hand (fixed)
        val hourAngle = 45f * (PI / 180f)
        drawLine(
            color = primary,
            start = center,
            end = Offset(center.x + radius * 0.5f * cos(hourAngle).toFloat(), center.y + radius * 0.5f * sin(hourAngle).toFloat()),
            strokeWidth = 4.dp.toPx(),
            cap = StrokeCap.Round
        )

        // Minute hand (animated)
        rotate(handRotation, pivot = center) {
            drawLine(
                color = secondary,
                start = center,
                end = Offset(center.x, center.y - radius * 0.7f),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Center dot
        drawCircle(color = primary, radius = 5.dp.toPx(), center = center)
    }
}

/**
 * Complete empty state component with illustration + text
 */
@Composable
fun EmptyStateWithIllustration(
    illustration: @Composable () -> Unit,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = WifiWeakspotShapes.extraExtraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            shadowElevation = 0.dp,
            modifier = Modifier.size(160.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                illustration()
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        action?.let {
            Spacer(Modifier.height(24.dp))
            it()
        }
    }
}
