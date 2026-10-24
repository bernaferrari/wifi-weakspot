package com.bernaferrari.wifiweakspot.core.wifi

import kotlinx.serialization.Serializable

/**
 * One access point from a scan.
 *
 * @param channelWidth Occupied width in MHz, already converted from the platform enum.
 * @param timestamp Unix epoch milliseconds when this result was observed.
 */
@Serializable
data class WifiNetwork(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val channelWidth: Int,
    val security: String,
    val capabilities: String,
    val timestamp: Long,
    val centerFreq0: Int = 0,
    val centerFreq1: Int = 0,
    val is80Plus80: Boolean = false,
    /** Platform observation time since boot, used to distinguish fresh scans from cache. */
    val observedElapsedRealtimeMicros: Long = 0
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
