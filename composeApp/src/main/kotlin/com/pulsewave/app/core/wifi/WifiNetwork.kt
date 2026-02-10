package com.pulsewave.app.core.wifi

import kotlinx.serialization.Serializable

@Serializable
data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val channelWidth: Int,
    val security: String,
    val capabilities: String,
    val timestamp: Long
) {
    val level: Int
        get() = calculateLevel(rssi)

    // Helper to calculate signal level (0-4)
    private fun calculateLevel(rssi: Int): Int {
        return when {
            rssi <= -100 -> 0
            rssi >= -55 -> 4
            else -> (rssi + 100) * 4 / 45
        }
    }
}
