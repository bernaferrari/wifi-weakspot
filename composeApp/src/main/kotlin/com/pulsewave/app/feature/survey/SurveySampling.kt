package com.pulsewave.app.feature.survey

import com.pulsewave.app.core.wifi.WifiNetwork

/** A spot on the map, 0 to 1 across and down. */
data class SurveyPosition(val x: Float, val y: Float) {
    fun clamped() = SurveyPosition(x.coerceIn(0f, 1f), y.coerceIn(0f, 1f))
}

fun normalizedSurveyPosition(x: Float, y: Float, width: Float, height: Float): SurveyPosition? {
    if (!x.isFinite() || !y.isFinite() || !width.isFinite() || !height.isFinite() || width <= 0f || height <= 0f) return null
    return SurveyPosition(x / width, y / height).clamped()
}

/** A live-link reading is three samples at least 500 ms apart: about this long, every time. */
const val LinkReadingMillis = 1_500

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

/**
 * When the phone is connected to the access point being mapped, its signal can be read every
 * second without a Wi-Fi scan (which Android rations to 4 every 2 minutes). Needs at least two
 * matching readings; the median steadies a single odd one.
 */
fun linkSurveyMeasurement(readings: List<com.pulsewave.app.core.model.NetworkStatus>, targetBssid: String, now: Long): WifiNetwork? {
    if (readings.size < 2) return null
    val ssid = readings.first().ssid
    if (readings.any { !it.hasSignalMeasurement || !it.bssid.equals(targetBssid, ignoreCase = true) ||
            it.ssid != ssid || it.sampledAtMillis <= 0 || it.sampledAtMillis > now + 1_000 ||
            now - it.sampledAtMillis > 5_000 }) return null
    // Replays and startup callback bursts do not form an independent sample window.
    if (readings.zipWithNext().any { (a, b) -> b.sampledAtMillis - a.sampledAtMillis < 500 }) return null
    val rssi = readings.map { it.rssi }.sorted()[readings.size / 2]
    val latest = readings.last()
    return WifiNetwork(latest.ssid, latest.bssid, rssi, latest.frequency, 20, latest.security, "", latest.sampledAtMillis)
}
