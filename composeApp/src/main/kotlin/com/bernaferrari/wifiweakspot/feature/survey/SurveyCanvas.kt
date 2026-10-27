package com.bernaferrari.wifiweakspot.feature.survey

import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.*
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.onClick
import kotlin.math.roundToInt
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.rememberWifiWeakspotHapticFeedback

@Composable
fun SurveyCanvas(
    points: List<com.bernaferrari.wifiweakspot.core.database.entities.SurveyPointEntity>,
    onPointAdded: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
    canMeasure: Boolean = true,
    livePosition: SurveyPosition? = null,
    expectedReadingMillis: Int = LinkReadingMillis,
    /** False while waiting on a Wi-Fi scan, whose length is unknown: then only the rings show. */
    readingIsTimed: Boolean = true,
    imagePath: String? = null,
    widthMeters: Float = 10f,
    heightMeters: Float = 10f,
    cursor: SurveyPosition = SurveyPosition(0.5f, 0.5f),
    selectedPointId: Long? = null,
    onPositionChanged: (SurveyPosition) -> Unit = {},
    onPointSelected: (Long?) -> Unit = {},
    onPointRecheck: (Long) -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val gridColor = colorScheme.onSurface.copy(alpha = 0.08f)
    val pinHalo = colorScheme.surface
    val haptics = rememberWifiWeakspotHapticFeedback()
    val measureEnabled by rememberUpdatedState(canMeasure)
    val addLatest by rememberUpdatedState(onPointAdded)
    val pointsLatest by rememberUpdatedState(points)
    val selectLatest by rememberUpdatedState(onPointSelected)
    val moveLatest by rememberUpdatedState(onPositionChanged)
    val surveyStrings = strings.survey
    fun move(dx: Float, dy: Float): Boolean {
        onPointSelected(null)
        onPositionChanged(SurveyPosition(cursor.x + dx, cursor.y + dy).clamped())
        return true
    }
    fun measure(): Boolean {
        if (!canMeasure) return false
        if (selectedPointId == null) onPointAdded(cursor.x, cursor.y) else onPointRecheck(selectedPointId)
        return true
    }

    // Pinch to zoom (up to 4×) and drag while zoomed. Taps land in the map's own coordinates,
    // so a reading is placed where the finger is at any zoom.
    var zoom by remember { mutableStateOf(1f) }
    var pan by remember { mutableStateOf(Offset.Zero) }
    var viewport by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    var fittedPixels by remember { mutableStateOf(androidx.compose.ui.unit.IntSize.Zero) }
    fun clampPan(value: Offset, scale: Float): Offset {
        val maxX = ((fittedPixels.width * scale - viewport.width) / 2f).coerceAtLeast(0f)
        val maxY = ((fittedPixels.height * scale - viewport.height) / 2f).coerceAtLeast(0f)
        return Offset(value.x.coerceIn(-maxX, maxX), value.y.coerceIn(-maxY, maxY))
    }
    BoxWithConstraints(
        modifier
            .clipToBounds()
            .onSizeChanged { viewport = it }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, dragged, factor, _ ->
                    val next = (zoom * factor).coerceIn(1f, 4f)
                    val applied = next / zoom
                    val fromCenter = centroid - Offset(viewport.width / 2f, viewport.height / 2f)
                    // Keep the point under the fingers still while zooming.
                    pan = clampPan(pan * applied + fromCenter * (1f - applied) + dragged, next)
                    zoom = next
                }
            }
    ) {
    val fitted = fittedMapSize(maxWidth.value, maxHeight.value, widthMeters, heightMeters)
    val mapWidth = fitted.width.dp
    val mapHeight = fitted.height.dp
    Box(Modifier.align(Alignment.Center).size(mapWidth, mapHeight).onSizeChanged { fittedPixels = it }.graphicsLayer { scaleX = zoom; scaleY = zoom; translationX = pan.x; translationY = pan.y }) {
        val platform: com.bernaferrari.wifiweakspot.core.platform.PlatformActions = org.koin.compose.koinInject()
        val floorplan by produceState<androidx.compose.ui.graphics.ImageBitmap?>(null, imagePath) {
            value = null
            value = imagePath?.let { platform.loadImage(it) }
        }
        floorplan?.let { bitmap -> androidx.compose.foundation.Image(
            bitmap = bitmap, contentDescription = strings.survey.floorplanDescription,
            // Existing normalized pins refer to the full photo bounds. Fit the whole map above,
            // not the photo alone, so changing screens never separates pins from photo locations.
            contentScale = ContentScale.FillBounds, modifier = Modifier.fillMaxSize()
        ) }
        // Same palette and thresholds as every other signal reading in the app.
        val heatColors = com.bernaferrari.wifiweakspot.core.designsystem.signalHeatColors()
        val gradient = remember(heatColors) { signalGradient(heatColors) }
        val signalColor: (Int) -> Color = { rssi -> gradient(rssi.toFloat()) }
        // The same physical field is fitted to every screen and exported without recomputing distances.
        val coverage = remember(points, widthMeters, heightMeters, gradient) {
            if (points.isEmpty()) null
            else coverageGridForMap(points.map { PlanReading(it.x, it.y, it.rssi) }, widthMeters, heightMeters)
                .toBitmap(gradient, opacity = 0.95f)
        }
        // A new reading eases in: the updated field fades in over the old one and the new dot grows,
        // instead of the whole map snapping to its next state.
        val reduceMotion = com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion.current
        var shownField by remember { mutableStateOf(coverage) }
        var previousField by remember { mutableStateOf<androidx.compose.ui.graphics.ImageBitmap?>(null) }
        val reveal = remember { androidx.compose.animation.core.Animatable(1f) }
        // Long and soft: the map settles into its new state rather than flashing to it.
        val revealSpec = tween<Float>(900, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        val newestId = points.maxByOrNull { it.timestamp }?.id
        var growingId by remember { mutableStateOf<Long?>(null) }
        LaunchedEffect(coverage) {
            if (coverage === shownField) return@LaunchedEffect
            val grew = shownField != null && newestId != null
            previousField = shownField
            shownField = coverage
            if (reduceMotion) { previousField = null; return@LaunchedEffect }
            growingId = if (grew) newestId else null
            reveal.snapTo(0f)
            reveal.animateTo(1f, revealSpec)
            previousField = null
            growingId = null
        }
        // Readings take about as long as the last one did, so the pie fills over that time at a
        // steady pace. If one runs long, the pie waits full while the rings keep going.
        val progress = remember { androidx.compose.animation.core.Animatable(0f) }
        LaunchedEffect(livePosition) {
            progress.snapTo(0f)
            if (livePosition == null) return@LaunchedEffect
            progress.animateTo(1f, tween(expectedReadingMillis, easing = LinearEasing))
        }
        // While a reading is taken, soft rings pulse out from the spot, like sonar.
        val mapDescription = strings.survey.coverageMapDescription(points.size)
        val ripple = if (livePosition != null && !reduceMotion) {
            rememberInfiniteTransition(label = "measuring").animateFloat(
                0f, 1f, infiniteRepeatable(tween(2_800, easing = LinearEasing)), label = "ripple"
            ).value
        } else 0f
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(if (imagePath == null) colorScheme.surface else Color.Transparent)
                .semantics {
                    contentDescription = mapDescription
                    stateDescription = surveyStrings.mapPosition((cursor.x * 100).roundToInt(), (cursor.y * 100).roundToInt())
                    if (canMeasure) {
                        onClick(if (selectedPointId == null) surveyStrings.measureHere else surveyStrings.recheckReading) { measure() }
                    }
                    customActions = listOf(
                        CustomAccessibilityAction(surveyStrings.moveLeft) { move(-0.05f, 0f) },
                        CustomAccessibilityAction(surveyStrings.moveRight) { move(0.05f, 0f) },
                        CustomAccessibilityAction(surveyStrings.moveUp) { move(0f, -0.05f) },
                        CustomAccessibilityAction(surveyStrings.moveDown) { move(0f, 0.05f) }
                    )
                }
                .onKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) false else when (event.key) {
                        Key.DirectionLeft -> move(-0.05f, 0f)
                        Key.DirectionRight -> move(0.05f, 0f)
                        Key.DirectionUp -> move(0f, -0.05f)
                        Key.DirectionDown -> move(0f, 0.05f)
                        Key.Enter, Key.Spacebar -> measure()
                        else -> false
                    }
                }
                .focusable()
                .pointerInput(Unit) {
                    detectTapGestures { tap ->
                        val hit = nearestSurveyPoint(pointsLatest, tap.x, tap.y, size.width.toFloat(), size.height.toFloat(), 24.dp.toPx())
                        if (hit != null) {
                            selectLatest(hit.id)
                            moveLatest(SurveyPosition(hit.x, hit.y))
                        } else normalizedSurveyPosition(tap.x, tap.y, size.width.toFloat(), size.height.toFloat())?.let { position ->
                            selectLatest(null)
                            moveLatest(position)
                            if (measureEnabled) { haptics.light(); addLatest(position.x, position.y) }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            // A faint grid, so a blank map still reads as a floor.
            val gridSize = 48.dp.toPx()
            var x = 0f
            while (x <= width) { drawLine(gridColor, Offset(x, 0f), Offset(x, height), 0.5.dp.toPx()); x += gridSize }
            var y = 0f
            while (y <= height) { drawLine(gridColor, Offset(0f, y), Offset(width, y), 0.5.dp.toPx()); y += gridSize }

            // The coverage field: interpolated between readings, fading out where nobody measured.
            val dst = androidx.compose.ui.unit.IntSize(width.toInt(), height.toInt())
            val t = reveal.value
            previousField?.let { drawImage(it, dstSize = dst, alpha = 1f - t * t, filterQuality = androidx.compose.ui.graphics.FilterQuality.High) }
            shownField?.let { drawImage(it, dstSize = dst, alpha = if (previousField == null) 1f else t, filterQuality = androidx.compose.ui.graphics.FilterQuality.High) }

            // Small and quiet: the field carries the story, the dots just say "measured here".
            points.forEach { point ->
                val center = Offset(point.x * width, point.y * height)
                val grow = if (point.id == growingId) t else 1f
                drawCircle(color = pinHalo.copy(alpha = 0.85f), radius = 4.5.dp.toPx() * grow, center = center)
                drawCircle(color = signalColor(point.rssi), radius = 3.dp.toPx() * grow, center = center)
                if (point.id == selectedPointId) drawCircle(colorScheme.primary, radius = 12.dp.toPx(), center = center, style = Stroke(3.dp.toPx()))
            }

            if (livePosition == null && selectedPointId == null) {
                val at = Offset(cursor.x * width, cursor.y * height)
                drawCircle(pinHalo, 10.dp.toPx(), at, style = Stroke(4.dp.toPx()))
                drawCircle(colorScheme.primary, 10.dp.toPx(), at, style = Stroke(2.dp.toPx()))
            }
            // Where you last tapped, while it's being measured.
            if (livePosition != null) {
                val center = Offset(livePosition.x * width, livePosition.y * height)
                // Two slow, faint rings: a calm "listening", not an alarm.
                repeat(2) { ring ->
                    val phase = (ripple + ring / 2f) % 1f
                    val eased = 1f - (1f - phase) * (1f - phase)
                    drawCircle(
                        colorScheme.onSurface.copy(alpha = 0.35f * (1f - phase) * (1f - phase)),
                        radius = 18.dp.toPx() + eased * 40.dp.toPx(), center = center,
                        style = Stroke(width = 1.5.dp.toPx())
                    )
                }
                // A pie that fills clockwise over the reading's known length, on a white rim so it
                // reads on any colour of the heatmap.
                val pie = 14.dp.toPx()
                drawCircle(Color.White, radius = pie + 2.5.dp.toPx(), center = center)
                drawCircle(if (readingIsTimed) colorScheme.onPrimary else colorScheme.primary, radius = pie, center = center)
                if (readingIsTimed) drawArc(
                    colorScheme.primary, startAngle = -90f, sweepAngle = 360f * progress.value, useCenter = true,
                    topLeft = center - Offset(pie, pie), size = androidx.compose.ui.geometry.Size(pie * 2, pie * 2)
                )
            }
        }
    }
    }
}
