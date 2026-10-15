package com.pulsewave.app.feature.survey

import com.pulsewave.app.core.wifi.WifiNetwork
import kotlin.math.cos
import kotlin.math.sin

/** Canvas coordinates and physical movement share one normalized map space. */
data class SurveyPosition(val x: Float, val y: Float) {
    fun clamped() = SurveyPosition(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
}

fun normalizedSurveyPosition(x: Float, y: Float, width: Float, height: Float): SurveyPosition? {
    if (!x.isFinite() || !y.isFinite() || !width.isFinite() || !height.isFinite() || width <= 0f || height <= 0f) return null
    return SurveyPosition(x / width, y / height).clamped()
}

fun moveSurveyPosition(
    position: SurveyPosition,
    headingDegrees: Float,
    stepLengthMeters: Float,
    widthMeters: Float,
    heightMeters: Float,
    mapTopBearingDegrees: Float = 0f
): SurveyPosition {
    val radians = (headingDegrees - mapTopBearingDegrees).toDouble() * kotlin.math.PI / 180.0
    return SurveyPosition(
        position.x + (sin(radians) * stepLengthMeters / widthMeters).toFloat(),
        position.y - (cos(radians) * stepLengthMeters / heightMeters).toFloat()
    ).clamped()
}

/** A scan may contain cached observations for an AP that did not answer the latest scan. */
fun freshSurveyMeasurement(
    networks: List<WifiNetwork>,
    targetBssid: String,
    requestedAt: Long,
    now: Long
): WifiNetwork? = networks.firstOrNull {
    it.bssid.equals(targetBssid, ignoreCase = true) &&
        it.timestamp >= requestedAt - 5_000 &&
        it.timestamp <= now + 1_000 &&
        now - it.timestamp <= 15_000 &&
        it.rssi in -127..0
}
