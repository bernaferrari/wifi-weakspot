package com.bernaferrari.wifiweakspot.feature.survey

import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Paint
import com.materialkolor.hct.Hct
import com.materialkolor.ktx.toColor
import com.materialkolor.ktx.toHct
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

/** A reading on the plan: [x] and [y] are 0..1 across the plan, [rssi] in dBm. */
internal data class PlanReading(val x: Float, val y: Float, val rssi: Int)

/**
 * The plan estimated cell by cell from nearby readings. [rssi] is NaN where nothing was measured;
 * [reach] is how much to trust a cell (1 next to a reading, fading to 0 away from all of them).
 */
internal class CoverageGrid(val columns: Int, val rows: Int, val rssi: FloatArray, val reach: FloatArray)

/** A canonical field in physical map space. Rendering size must never change its predictions. */
internal fun coverageGridForMap(
    readings: List<PlanReading>,
    widthMeters: Float,
    heightMeters: Float
): CoverageGrid {
    val aspect = mapAspectRatio(widthMeters, heightMeters)
    return coverageGrid(readings, aspect)
}

/**
 * Inverse-distance weighting over the readings, measured in units of the plan's shorter side so a
 * wide plan doesn't stretch coverage sideways. Cells far from every reading fade out, so the map
 * never paints confident colour over rooms nobody walked into.
 */
internal fun coverageGrid(
    readings: List<PlanReading>,
    aspect: Float,
    columns: Int = 56,
    fullReach: Float = 0.18f,
    noReach: Float = 0.42f
): CoverageGrid {
    val safeAspect = aspect.takeIf { it.isFinite() && it > 0f } ?: 1f
    // Bound raster size separately from physical distances, including long narrow maps.
    val rows = max(1, (columns / safeAspect.coerceIn(0.2f, 5f)).roundToInt())
    // Scale each axis so distances are measured against the plan's shorter side.
    val shorter = min(safeAspect, 1f)
    val sx = safeAspect / shorter
    val sy = 1f / shorter
    val rssi = FloatArray(columns * rows) { Float.NaN }
    val reach = FloatArray(columns * rows)
    if (readings.isEmpty()) return CoverageGrid(columns, rows, rssi, reach)
    for (j in 0 until rows) {
        val cy = (j + 0.5f) / rows
        for (i in 0 until columns) {
            val cx = (i + 0.5f) / columns
            var weighted = 0f
            var total = 0f
            var nearest = Float.MAX_VALUE
            readings.forEach { r ->
                val dx = (r.x - cx) * sx
                val dy = (r.y - cy) * sy
                val d2 = dx * dx + dy * dy
                nearest = min(nearest, d2)
                val w = 1f / (d2 + 1e-4f)
                weighted += w * r.rssi
                total += w
            }
            val index = j * columns + i
            rssi[index] = weighted / total
            reach[index] = 1f - smoothStep(fullReach, noReach, sqrt(nearest))
        }
    }
    return CoverageGrid(columns, rows, rssi, reach)
}

/** Paints the grid one pixel per cell; drawn scaled up with filtering it reads as a smooth field. */
internal fun CoverageGrid.toBitmap(color: (Float) -> Color, opacity: Float = 0.7f): ImageBitmap {
    val bitmap = ImageBitmap(columns, rows)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    for (j in 0 until rows) for (i in 0 until columns) {
        val index = j * columns + i
        val value = rssi[index]
        if (value.isNaN() || reach[index] <= 0f) continue
        paint.color = color(value).copy(alpha = opacity * reach[index])
        canvas.drawRect(i.toFloat(), j.toFloat(), i + 1f, j + 1f, paint)
    }
    return bitmap
}

/**
 * A continuous version of the app's signal palette: each quality's colour sits at the middle of
 * its band, and readings in between blend, so the map shows gradients instead of hard steps.
 */
internal fun signalGradient(colors: Map<SignalQuality, Color>): (Float) -> Color {
    val stops = listOf(
        -90f to colors.getValue(SignalQuality.DeadZone),
        -78.5f to colors.getValue(SignalQuality.Weak),
        -71f to colors.getValue(SignalQuality.Fair),
        -61f to colors.getValue(SignalQuality.Good),
        -50f to colors.getValue(SignalQuality.Excellent)
    )
    return { rssi ->
        when {
            rssi <= stops.first().first -> stops.first().second
            rssi >= stops.last().first -> stops.last().second
            else -> {
                val upper = stops.indexOfFirst { it.first >= rssi }
                val (a, ca) = stops[upper - 1]
                val (b, cb) = stops[upper]
                hueLerp(ca, cb, (rssi - a) / (b - a))
            }
        }
    }
}

/**
 * Blends around the hue circle (HCT, a perceptual space like OKLCH) rather than straight across
 * it, so teal to amber passes through lime instead of a muddy grey.
 */
private fun hueLerp(from: Color, to: Color, t: Float): Color {
    val a = from.toHct()
    val b = to.toHct()
    var delta = b.hue - a.hue
    if (delta > 180) delta -= 360
    if (delta < -180) delta += 360
    val hue = (a.hue + delta * t + 360) % 360
    return Hct.from(hue, a.chroma + (b.chroma - a.chroma) * t, a.tone + (b.tone - a.tone) * t).toColor()
}

private fun smoothStep(edge0: Float, edge1: Float, x: Float): Float {
    val t = ((x - edge0) / (edge1 - edge0)).coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}

/** A map keeps its calibrated aspect on every viewport. Invalid imported sizes fall back to a square. */
internal fun mapAspectRatio(widthMeters: Float, heightMeters: Float): Float =
    if (widthMeters.isFinite() && heightMeters.isFinite() && widthMeters > 0f && heightMeters > 0f)
        (widthMeters / heightMeters).takeIf { it.isFinite() && it > 0f } ?: 1f
    else 1f

internal data class FittedMapSize(val width: Float, val height: Float)

internal fun fittedMapSize(viewportWidth: Float, viewportHeight: Float, widthMeters: Float, heightMeters: Float): FittedMapSize {
    val aspect = mapAspectRatio(widthMeters, heightMeters)
    val width = minOf(viewportWidth.coerceAtLeast(0f), viewportHeight.coerceAtLeast(0f) * aspect)
    return FittedMapSize(width, width / aspect)
}
