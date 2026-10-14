package com.pulsewave.app.core.designsystem.component

import com.pulsewave.app.core.designsystem.animateAccessibleFloat
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
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Custom illustration components for empty states.
 * These replace generic icons with hand-crafted, animated illustrations.
 */

/**
 * WiFi Network illustration - animated signal waves
 */
@Composable
fun WifiNetworkIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "wifi_illustration")

    // Animated arc opacity (staggered)
    val arc1Alpha by infiniteTransition.animateAccessibleFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "arc1"
    )
    val arc2Alpha by infiniteTransition.animateAccessibleFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ), label = "arc2"
    )
    val arc3Alpha by infiniteTransition.animateAccessibleFloat(
        initialValue = 0.2f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ), label = "arc3"
    )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    Canvas(modifier = modifier.size(size)) {
        val centerX = this.size.width / 2
        val centerY = this.size.height * 0.7f
        val baseRadius = this.size.width * 0.15f

        // Draw WiFi arcs
        listOf(
            Triple(baseRadius, arc1Alpha, 3.dp),
            Triple(baseRadius * 1.8f, arc2Alpha, 4.dp),
            Triple(baseRadius * 2.6f, arc3Alpha, 5.dp)
        ).forEach { (radius, alpha, strokeWidth) ->
            drawArc(
                color = primary.copy(alpha = alpha),
                startAngle = 225f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(centerX - radius, centerY - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }

        // Center dot
        drawCircle(
            color = primary,
            radius = 8.dp.toPx(),
            center = Offset(centerX, centerY)
        )

        // Decorative floating dots
        val dots = listOf(
            Offset(this.size.width * 0.2f, this.size.height * 0.3f) to tertiary,
            Offset(this.size.width * 0.8f, this.size.height * 0.25f) to primary.copy(alpha = 0.5f),
            Offset(this.size.width * 0.15f, this.size.height * 0.6f) to tertiary.copy(alpha = 0.4f),
            Offset(this.size.width * 0.85f, this.size.height * 0.55f) to primary.copy(alpha = 0.3f)
        )
        dots.forEach { (offset, color) ->
            drawCircle(color = color, radius = 4.dp.toPx(), center = offset)
        }
    }
}

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
 * Speed Test illustration - speedometer with dynamic needle
 */
@Composable
fun SpeedTestIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "speed_illustration")

    val needleAngle by infiniteTransition.animateAccessibleFloat(
        initialValue = -60f, targetValue = 60f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "needle"
    )

    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary
    val error = MaterialTheme.colorScheme.error
    val surface = MaterialTheme.colorScheme.surfaceContainerHigh

    Canvas(modifier = modifier.size(size)) {
        val center = Offset(this.size.width / 2, this.size.height * 0.6f)
        val arcRadius = this.size.width * 0.4f

        // Background arc
        drawArc(
            color = surface,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
            size = Size(arcRadius * 2, arcRadius * 2),
            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
        )

        // Gradient arc segments
        val segments = listOf(
            Triple(180f, 60f, error.copy(alpha = 0.6f)),
            Triple(240f, 60f, tertiary.copy(alpha = 0.6f)),
            Triple(300f, 60f, primary.copy(alpha = 0.8f))
        )
        segments.forEach { (start, sweep, color) ->
            drawArc(
                color = color,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(center.x - arcRadius, center.y - arcRadius),
                size = Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Butt)
            )
        }

        // Tick marks
        for (i in 0..6) {
            val angle = (180f + i * 30f) * (PI / 180f)
            val innerRadius = arcRadius - 20.dp.toPx()
            val outerRadius = arcRadius - 8.dp.toPx()
            drawLine(
                color = primary.copy(alpha = 0.5f),
                start = Offset(
                    center.x + innerRadius * cos(angle).toFloat(),
                    center.y + innerRadius * sin(angle).toFloat()
                ),
                end = Offset(
                    center.x + outerRadius * cos(angle).toFloat(),
                    center.y + outerRadius * sin(angle).toFloat()
                ),
                strokeWidth = 2.dp.toPx()
            )
        }

        // Animated needle
        rotate(needleAngle, pivot = center) {
            drawLine(
                color = primary,
                start = center,
                end = Offset(center.x, center.y - arcRadius + 20.dp.toPx()),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        // Center cap
        drawCircle(color = primary, radius = 8.dp.toPx(), center = center)
        drawCircle(color = Color.White, radius = 4.dp.toPx(), center = center)
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
 * Privacy illustration - shield with lock and glow
 */
@Composable
fun PrivacyIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "privacy_illustration")

    val glowAlpha by infiniteTransition.animateAccessibleFloat(
        initialValue = 0.2f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "glow"
    )

    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val surface = MaterialTheme.colorScheme.surfaceContainerHigh
    val outline = MaterialTheme.colorScheme.outline

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        val shieldPath = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            lineTo(w * 0.85f, h * 0.22f)
            lineTo(w * 0.78f, h * 0.78f)
            lineTo(w * 0.5f, h * 0.92f)
            lineTo(w * 0.22f, h * 0.78f)
            lineTo(w * 0.15f, h * 0.22f)
            close()
        }

        drawPath(
            path = shieldPath,
            brush = Brush.verticalGradient(
                colors = listOf(primary.copy(alpha = 0.85f), secondary.copy(alpha = 0.75f))
            )
        )

        drawPath(
            path = shieldPath,
            color = outline.copy(alpha = 0.25f),
            style = Stroke(width = 2.dp.toPx())
        )

        drawPath(
            path = shieldPath,
            brush = Brush.radialGradient(
                colors = listOf(primary.copy(alpha = glowAlpha), Color.Transparent),
                center = Offset(w * 0.5f, h * 0.4f),
                radius = w * 0.6f
            )
        )

        val lockWidth = w * 0.28f
        val lockHeight = h * 0.2f
        val lockTop = h * 0.48f
        val lockLeft = w * 0.5f - lockWidth / 2

        drawRoundRect(
            color = surface,
            topLeft = Offset(lockLeft, lockTop),
            size = Size(lockWidth, lockHeight),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
        )

        drawRoundRect(
            color = primary,
            topLeft = Offset(lockLeft, lockTop),
            size = Size(lockWidth, lockHeight),
            cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        val shackleRect = Rect(
            left = w * 0.5f - lockWidth * 0.35f,
            top = lockTop - lockHeight * 0.6f,
            right = w * 0.5f + lockWidth * 0.35f,
            bottom = lockTop + lockHeight * 0.1f
        )
        drawArc(
            color = primary,
            startAngle = 180f,
            sweepAngle = 180f,
            useCenter = false,
            topLeft = Offset(shackleRect.left, shackleRect.top),
            size = Size(shackleRect.width, shackleRect.height),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
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
            shape = PulseWaveShapes.extraExtraLarge,
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
