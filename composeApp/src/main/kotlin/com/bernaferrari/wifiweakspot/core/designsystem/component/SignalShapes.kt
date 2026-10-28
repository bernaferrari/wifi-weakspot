@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.core.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toPath
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import com.bernaferrari.wifiweakspot.core.designsystem.animateAccessibleFloat
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality

/** More lobes means more signal; a dead zone is a little ghost. */
fun SignalQuality.polygon(): RoundedPolygon = when (this) {
    SignalQuality.Excellent -> MaterialShapes.Cookie12Sided
    SignalQuality.Good -> MaterialShapes.Cookie9Sided
    SignalQuality.Fair -> MaterialShapes.Cookie6Sided
    SignalQuality.Weak -> MaterialShapes.Cookie4Sided
    SignalQuality.DeadZone -> MaterialShapes.Ghostish
}

internal class MorphShape(private val morph: Morph, private val progress: Float) : Shape {
    private val path = Path()
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        path.rewind()
        morph.toPath(progress, path)
        path.transform(Matrix().apply { scale(size.width, size.height) })
        return Outline.Generic(path)
    }
}

/**
 * A Material shape that morphs whenever signal quality changes and turns slowly
 * while it is live. Content stays upright on top.
 */
@Composable
fun SignalMorphBlob(
    quality: SignalQuality?,
    color: Color,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    spinning: Boolean = true,
    /** A fixed shape for screens that measure something other than signal; colour still carries the verdict. */
    shape: RoundedPolygon? = null,
    content: @Composable BoxScope.() -> Unit = {}
) {
    val target = shape ?: quality?.polygon() ?: MaterialShapes.Circle
    var from by remember { mutableStateOf(target) }
    var to by remember { mutableStateOf(target) }
    val progress = remember { Animatable(1f) }
    val morphSpec = MaterialTheme.motionScheme.slowSpatialSpec<Float>()
    LaunchedEffect(target) {
        if (target === to) return@LaunchedEffect
        from = to
        to = target
        progress.snapTo(0f)
        progress.animateTo(1f, morphSpec)
    }
    val morph = remember(from, to) { Morph(from, to) }
    val rotation by rememberInfiniteTransition(label = "blobSpin").animateAccessibleFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(24_000, easing = LinearEasing), RepeatMode.Restart),
        label = "blobRotation"
    )
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(size)
                .graphicsLayer { rotationZ = if (spinning) rotation else 0f }
                .clip(MorphShape(morph, progress.value))
                .background(color)
        )
        content()
    }
}

/** An icon framed by an expressive Material shape, for tiles and list rows. */
@Composable
fun ShapeIcon(
    icon: ImageVector,
    shape: RoundedPolygon,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = size * 0.5f
) {
    Box(
        modifier.size(size).clip(shape.toShape()).background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(iconSize))
    }
}

/** Smooth line of recent RSSI readings, scaled to the useful -95..-35 dBm range. */
@Composable
fun SignalSparkline(
    values: List<Int>,
    color: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 4.dp
) {
    Canvas(modifier) {
        if (values.size < 2) return@Canvas
        val stroke = strokeWidth.toPx()
        val usableHeight = size.height - stroke
        val step = size.width / (values.size - 1).coerceAtLeast(1)
        fun point(index: Int) = Offset(
            index * step,
            stroke / 2 + usableHeight * (1f - SignalQuality.fraction(values[index]))
        )
        val path = Path().apply {
            val first = point(0)
            moveTo(first.x, first.y)
            for (i in 1 until values.size) {
                val previous = point(i - 1)
                val current = point(i)
                val midX = (previous.x + current.x) / 2
                cubicTo(midX, previous.y, midX, current.y, current.x, current.y)
            }
        }
        drawPath(path, color, style = Stroke(stroke, cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawCircle(color, radius = stroke * 1.4f, center = point(values.lastIndex))
    }
}
