package com.pulsewave.app.core.designsystem.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * The Networks tab: two overlapping hills on a baseline, the same picture as the channel chart,
 * so the icon says "networks side by side" rather than a generic radar.
 */
object NetworksIcons {
    private fun PathBuilder.tall() {
        moveTo(2.5f, 20f); curveTo(4.5f, 20f, 5.5f, 5f, 8.5f, 5f); curveTo(11.5f, 5f, 12.5f, 20f, 14.5f, 20f)
    }
    private fun PathBuilder.short() {
        moveTo(9.5f, 20f); curveTo(11.5f, 20f, 12.8f, 10f, 15.5f, 10f); curveTo(18.2f, 10f, 19.5f, 20f, 21.5f, 20f)
    }

    val Outlined: ImageVector by lazy {
        ImageVector.Builder("Networks.Outlined", 24.dp, 24.dp, 24f, 24f).apply {
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { tall() }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) { short() }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round) { moveTo(2f, 20f); lineTo(22f, 20f) }
        }.build()
    }

    val Filled: ImageVector by lazy {
        ImageVector.Builder("Networks.Filled", 24.dp, 24.dp, 24f, 24f).apply {
            path(fill = SolidColor(Color.Black), fillAlpha = 0.55f) { short(); close() }
            path(fill = SolidColor(Color.Black)) { tall(); close() }
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round) { moveTo(2f, 20f); lineTo(22f, 20f) }
        }.build()
    }
}

/**
 * "Test a website": a browser window whose page is a heartbeat line, i.e. "is this site alive and
 * how fast does it answer", rather than a generic globe or gauge.
 */
object SiteCheckIcon {
    val Filled: ImageVector by lazy {
        ImageVector.Builder("SiteCheck", 24.dp, 24.dp, 24f, 24f).apply {
            // Window frame with a title bar.
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineJoin = StrokeJoin.Round) {
                moveTo(5f, 3.5f); lineTo(19f, 3.5f)
                curveTo(20.4f, 3.5f, 21.5f, 4.6f, 21.5f, 6f); lineTo(21.5f, 18f)
                curveTo(21.5f, 19.4f, 20.4f, 20.5f, 19f, 20.5f); lineTo(5f, 20.5f)
                curveTo(3.6f, 20.5f, 2.5f, 19.4f, 2.5f, 18f); lineTo(2.5f, 6f)
                curveTo(2.5f, 4.6f, 3.6f, 3.5f, 5f, 3.5f); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(2.5f, 6f); curveTo(2.5f, 4.6f, 3.6f, 3.5f, 5f, 3.5f); lineTo(19f, 3.5f)
                curveTo(20.4f, 3.5f, 21.5f, 4.6f, 21.5f, 6f); lineTo(21.5f, 8f); lineTo(2.5f, 8f); close()
            }
            // The page: a pulse, the site answering.
            path(stroke = SolidColor(Color.Black), strokeLineWidth = 2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
                moveTo(5.5f, 14.5f); lineTo(8.5f, 14.5f); lineTo(10.2f, 11f); lineTo(12.6f, 17.5f); lineTo(14.6f, 12.5f); lineTo(15.8f, 14.5f); lineTo(18.5f, 14.5f)
            }
        }.build()
    }
}

/**
 * A speed gauge whose needle shows how quickly the router answered last time:
 * [fraction] 1 swings it fully right (fast), 0 leaves it resting left (slow).
 */
object RouterGaugeIcon {
    private val cache = mutableMapOf<Int, ImageVector>()

    fun of(fraction: Float): ImageVector {
        // A handful of steps is plenty at icon size, and keeps the cache tiny.
        val step = (fraction.coerceIn(0f, 1f) * 12).toInt()
        return cache.getOrPut(step) { build(step / 12f) }
    }

    private fun build(fraction: Float): ImageVector {
        val cx = 12f; val cy = 13f; val r = 9f
        // The dial spans 240°, open at the bottom; angles run clockwise from the right, y down.
        val start = 150f; val sweep = 240f
        fun point(deg: Float, radius: Float): Pair<Float, Float> {
            val rad = deg * kotlin.math.PI / 180.0
            return (cx + radius * kotlin.math.cos(rad)).toFloat() to (cy + radius * kotlin.math.sin(rad)).toFloat()
        }
        val angle = start + sweep * fraction
        val (sx, sy) = point(start, r)
        val (ex, ey) = point(start + sweep, r)
        val (px, py) = point(angle, r)
        // A tapered needle that stops well short of the dial, so the two never touch.
        val (tx, ty) = point(angle, 5.4f)
        val (lx, ly) = point(angle - 90f, 1.6f)
        val (rx, ry) = point(angle + 90f, 1.6f)
        return ImageVector.Builder("RouterGauge.$fraction", 24.dp, 24.dp, 24f, 24f).apply {
            // Faint full track, then the solid reading on top of it.
            path(stroke = SolidColor(Color.Black), strokeAlpha = 0.35f, strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round) {
                moveTo(sx, sy)
                arcTo(r, r, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = ex, y1 = ey)
            }
            if (fraction > 0.02f) path(stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round) {
                moveTo(sx, sy)
                arcTo(r, r, 0f, isMoreThanHalf = sweep * fraction > 180f, isPositiveArc = true, x1 = px, y1 = py)
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(lx, ly); lineTo(tx, ty); lineTo(rx, ry); close()
            }
            path(fill = SolidColor(Color.Black)) {
                moveTo(cx + 2f, cy)
                arcTo(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx - 2f, y1 = cy)
                arcTo(2f, 2f, 0f, isMoreThanHalf = true, isPositiveArc = true, x1 = cx + 2f, y1 = cy)
                close()
            }
        }.build()
    }
}
