package com.pulsewave.app.core.wifi

import kotlin.math.log10
import kotlin.math.pow

data class FrequencySegment(val centerMhz: Int, val widthMhz: Int) {
    val lowerMhz: Double get() = centerMhz - widthMhz / 2.0
    val upperMhz: Double get() = centerMhz + widthMhz / 2.0

    fun overlaps(centerMhz: Int, widthMhz: Int = 20): Boolean =
        lowerMhz < centerMhz + widthMhz / 2.0 && upperMhz > centerMhz - widthMhz / 2.0
}

/** Unknown wide-channel centers are shown as their known primary 20 MHz only. */
fun occupiedFrequencySegments(network: WifiNetwork): List<FrequencySegment> {
    if (network.channelWidth <= 20 || network.centerFreq0 <= 0) {
        return listOf(FrequencySegment(network.frequency, 20))
    }
    if (network.is80Plus80) {
        return buildList {
            add(FrequencySegment(network.centerFreq0, 80))
            if (network.centerFreq1 > 0) add(FrequencySegment(network.centerFreq1, 80))
        }
    }
    return listOf(FrequencySegment(network.centerFreq0, network.channelWidth))
}

/** No observation from before the request may become a measurement at a new location. */
fun freshScanResults(
    results: List<WifiNetwork>,
    resultsUpdated: Boolean,
    requestedElapsedRealtimeMicros: Long
): List<WifiNetwork>? {
    if (!resultsUpdated) return null
    return results.filter {
        it.observedElapsedRealtimeMicros > 0 &&
            it.observedElapsedRealtimeMicros >= requestedElapsedRealtimeMicros
    }.takeIf { it.isNotEmpty() }
}

/**
 * Maps a center frequency in MHz to an 802.11 channel number.
 * 6 GHz channel 1 is 5955 MHz. Channel 14 is 2484 MHz.
 */
fun frequencyToChannel(frequency: Int): Int = when {
    frequency in 2412..2472 -> ((frequency - 2412) / 5) + 1
    frequency in 2484..2495 -> 14
    frequency in 5160..5885 -> (frequency - 5000) / 5
    frequency in 5955..7115 -> (frequency - 5950) / 5
    else -> 0
}

fun bandLabel(frequencyMhz: Int): String = when {
    frequencyMhz >= 5925 -> "6 GHz"
    frequencyMhz >= 5000 -> "5 GHz"
    frequencyMhz > 0 -> "2.4 GHz"
    else -> "Unknown"
}

/**
 * [android.net.wifi.ScanResult.channelWidth] is an enum, not a width in MHz:
 * 0 = 20, 1 = 40, 2 = 80, 3 = 160, 4 = 80+80, 5 = 320.
 * Values that are already at least 20 are left alone.
 */
fun channelWidthMhz(raw: Int): Int = when (raw) {
    0 -> 20
    1 -> 40
    2 -> 80
    3 -> 160
    4 -> 80
    5 -> 320
    else -> if (raw >= 20) raw else 20
}

/**
 * Converts a scan timestamp (microseconds since boot) to epoch milliseconds.
 */
fun scanTimestampToEpochMillis(
    scanTimestampMicros: Long,
    nowEpochMillis: Long,
    elapsedRealtimeMicros: Long
): Long {
    val ageMicros = (elapsedRealtimeMicros - scanTimestampMicros).coerceAtLeast(0)
    return nowEpochMillis - ageMicros / 1_000
}

/**
 * Indoor log-distance estimate. Reference is free-space loss at 1 meter
 * for a 0 dBm transmitter, with a path-loss exponent of 3.
 * The same inputs always produce the same distance on every screen.
 */
fun estimateDistanceMeters(rssi: Int, frequencyMhz: Int): Double {
    val freq = frequencyMhz.coerceAtLeast(1).toDouble()
    val pathLossAtOneMeter = 20.0 * log10(freq) - 27.55
    val pathLoss = -rssi.toDouble()
    val exponent = (pathLoss - pathLossAtOneMeter) / 30.0
    return 10.0.pow(exponent).coerceIn(0.1, 500.0)
}

fun formatDistance(meters: Double): String {
    if (meters >= 10.0) return "~${meters.toInt()} m"
    val tenths = kotlin.math.round(meters * 10).toInt()
    return "~${tenths / 10}.${tenths % 10} m"
}

fun isNetworkSecured(capabilities: String): Boolean {
    val caps = capabilities.uppercase()
    return caps.contains("WPA") || caps.contains("WEP") || caps.contains("SAE") || caps.contains("EAP")
}

fun securityLabel(capabilities: String): String {
    val caps = capabilities.uppercase()
    return when {
        caps.contains("WPA3") || caps.contains("SAE") -> "WPA3"
        caps.contains("WPA2") || caps.contains("RSN") -> "WPA2"
        caps.contains("WPA") -> "WPA"
        caps.contains("WEP") -> "WEP"
        caps.isBlank() -> ""
        else -> "Open"
    }
}
