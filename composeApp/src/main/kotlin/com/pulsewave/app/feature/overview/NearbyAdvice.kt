package com.pulsewave.app.feature.overview

import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.wifi.competitorsOf
import com.pulsewave.app.core.wifi.frequencyToChannel
import com.pulsewave.app.core.wifi.groupByRadio
import com.pulsewave.app.core.wifi.occupiedFrequencySegments

internal data class NearbyAdvice(val fresh: Boolean, val detail: String, val networkCount: Int)

/** Scan observations describe overlap, not traffic, throughput or router channel support. */
internal fun nearbyAdvice(
    networks: List<WifiNetwork>,
    connectedBssid: String?,
    connectedFrequency: Int,
    nowMillis: Long
): NearbyAdvice {
    val latest = networks.maxOfOrNull { it.timestamp } ?: 0L
    val fresh = latest > 0 && nowMillis - latest in 0..45_000
    if (!fresh) return NearbyAdvice(false, "From your last scan. Tap to refresh.", networks.size)
    val recent = networks.filter { it.timestamp > 0 && nowMillis - it.timestamp in 0..45_000 }
    val connected = recent.firstOrNull {
        it.bssid.equals(connectedBssid, ignoreCase = true) &&
            it.frequency == connectedFrequency && nowMillis - it.timestamp in 0..45_000
    } ?: return NearbyAdvice(true, "See channels and signal for each network.", recent.size)
    val occupied = occupiedFrequencySegments(connected)
    val overlap = groupByRadio(recent.competitorsOf(connectedBssid, connectedFrequency))
        .count { radio ->
            occupiedFrequencySegments(radio.lead).any { other ->
                occupied.any { own -> own.lowerMhz < other.upperMhz && own.upperMhz > other.lowerMhz }
            }
        }
    val channel = frequencyToChannel(connectedFrequency)
    return NearbyAdvice(true, when (overlap) {
        0 -> "No nearby overlap detected on channel $channel."
        1 -> "1 nearby signal overlaps your channel $channel."
        else -> "$overlap nearby signals overlap your channel $channel."
    }, recent.size)
}
