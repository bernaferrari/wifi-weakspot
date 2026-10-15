package com.pulsewave.app.feature.survey

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.animateAccessibleFloat
import com.pulsewave.app.core.designsystem.rememberPulseWaveHapticFeedback

@Composable
fun SurveyCanvas(
    points: List<com.pulsewave.app.core.database.entities.SurveyPointEntity>,
    onPointAdded: (Float, Float) -> Unit,
    onPositionCorrected: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    isRecording: Boolean = false,
    heading: Float = 0f,
    ghostPath: List<Pair<Float, Float>> = emptyList(),
    livePosition: SurveyPosition? = null,
    imagePath: String? = null
) {
    val colorScheme = MaterialTheme.colorScheme
    val primary = colorScheme.primary
    val tertiary = colorScheme.tertiary
    val error = colorScheme.error
    val onSurface = colorScheme.onSurface
    val gridColor = onSurface.copy(alpha = 0.08f)
    val pinHalo = colorScheme.surface

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (isRecording) infiniteTransition.animateAccessibleFloat(
        initialValue = 1f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    ) else rememberUpdatedState(1f)
    val pulseAlpha by if (isRecording) infiniteTransition.animateAccessibleFloat(
        initialValue = 0.6f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    ) else rememberUpdatedState(0f)

    val haptics = rememberPulseWaveHapticFeedback()

    Box(modifier = modifier) {
        val platform: com.pulsewave.app.core.platform.PlatformActions = org.koin.compose.koinInject()
        val floorplan = remember(imagePath) { imagePath?.let(platform::loadImage) }
        if (floorplan != null) androidx.compose.foundation.Image(
            bitmap = floorplan, contentDescription = "Project floorplan",
            contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize()
        )
        // Same palette and thresholds as every other signal reading in the app.
        val heatColors = com.pulsewave.app.core.wifi.SignalQuality.entries.associateWith { com.pulsewave.app.core.designsystem.signalColors(it).accent }
        val signalColor: (Int) -> Color = { rssi -> heatColors.getValue(com.pulsewave.app.core.wifi.SignalQuality.of(rssi)) }
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(if (imagePath == null) colorScheme.surface else Color.Transparent)
                .semantics { contentDescription = "Coverage map with ${points.size} measurements. Use Add at coordinates to place measurements without touch gestures." }
                .pointerInput(isRecording) {
                    if (isRecording) detectDragGestures(
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
                    detectTapGestures { tap ->
                        normalizedSurveyPosition(tap.x, tap.y, size.width.toFloat(), size.height.toFloat())?.let { position ->
                            haptics.light()
                            onPointAdded(position.x, position.y)
                        }
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
                val heat = signalColor(rssi)

                // Large soft glow
                val radius = 100.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            heat.copy(alpha = 0.5f),
                            heat.copy(alpha = 0.15f),
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
                            heat.copy(alpha = 0.9f),
                            heat.copy(alpha = 0.0f)
                        ),
                        center = center,
                        radius = 24.dp.toPx()
                    ),
                    radius = 24.dp.toPx(),
                    center = center
                )
            }

            // 3. While walking, the trail of readings shows the route; at rest the dots stand alone.
            if (isRecording && points.size > 1) {
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

                val pointColor = signalColor(rssi)

                drawCircle(
                    color = pinHalo,
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
                    color = pinHalo,
                    radius = 2.dp.toPx(),
                    center = center
                )
            }

            // 5. A+ Polish: Pulsing "Current Location" Indicator with Heading
            if (livePosition != null) {
                val center = Offset(livePosition.x * width, livePosition.y * height)

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

                if (isRecording) {
                // Heading Arrow - rotate points manually around center
                val rad = heading.toDouble() * kotlin.math.PI / 180.0
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
        }


    }
}

