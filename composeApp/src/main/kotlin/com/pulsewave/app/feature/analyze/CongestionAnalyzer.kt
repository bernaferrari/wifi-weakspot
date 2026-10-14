package com.pulsewave.app.feature.analyze

import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.wifi.occupiedFrequencySegments
import kotlin.math.pow

enum class TrafficRating(val label: String, val colorHex: Long) {
    LOW("Low overlap", 0xFF4CAF50),
    MEDIUM("Moderate overlap", 0xFFFFC107),
    HIGH("High overlap", 0xFFFF5722),
    SEVERE("Very high overlap", 0xFFB00020);
}

data class ChannelScore(
    val channelNumber: Int,
    val centerFreq: Int,
    val score: Double,
    val recommendation: Boolean = false
) {
    val rating: TrafficRating
        get() = when {
            score < 1000 -> TrafficRating.LOW
            score < 3000 -> TrafficRating.MEDIUM
            score < 6000 -> TrafficRating.HIGH
            else -> TrafficRating.SEVERE
        }
}

object CongestionAnalyzer {

    // 2.4GHz Channel Centers (channels 1-14)
    private val channels24 = (1..14).associateWith { ch ->
        if (ch == 14) 2484 else 2412 + (ch - 1) * 5
    }

    // 5GHz (U-NII bands: 1, 2A, 2C, 3)
    private val channels5 = listOf(36, 40, 44, 48, 52, 56, 60, 64, 100, 104, 108, 112, 116, 120, 124, 128, 132, 136, 140, 144, 149, 153, 157, 161, 165).associateWith { ch ->
        5180 + (ch - 36) * 5
    }

    // 6 GHz channel 1 is centered at 5955 MHz. Channels step by 4 (20 MHz).
    private val channels6 = (1..233 step 4).associate { ch ->
        ch to (5950 + ch * 5)
    }

    fun analyze(
        networks: List<WifiNetwork>,
        band: FrequencyBand,
        supportedChannels: Set<Int>? = null
    ): List<ChannelScore> {
        val targetChannels = when (band) {
            FrequencyBand.BAND_2_4 -> channels24
            FrequencyBand.BAND_5 -> channels5
            FrequencyBand.BAND_6 -> channels6
        }

        if (targetChannels.isEmpty()) return emptyList()

        val scores = targetChannels.map { (channel, centerFreq) ->
            var totalEnergy = 0.0

            networks.forEach { net ->
                if (occupiedFrequencySegments(net).any { it.overlaps(centerFreq) }) {
                    val signal = net.rssi
                    // Normalize -100 to 0. -30 (strong) -> 70.
                    // Power law: (Signal + 100)^2
                    // -90 -> 10^2 = 100
                    // -50 -> 50^2 = 2500
                    // -30 -> 70^2 = 4900
                    if (signal > -100) {
                        val normalized = (signal + 100).toDouble()
                        totalEnergy += normalized.pow(2)
                    }
                }
            }
            ChannelScore(channel, centerFreq, totalEnergy)
        }

        // Find best channel (lowest score)
        // Prefer channels 1, 6, 11 for 2.4GHz to minimize overlap
        // 5/6 GHz availability depends on country, DFS and router support. Do not
        // invent support from an empty scan or assume every plotted channel is usable.
        val hasBandObservations = networks.any { net ->
            when (band) {
                FrequencyBand.BAND_2_4 -> net.frequency in 2400..2500
                FrequencyBand.BAND_5 -> net.frequency in 5000..5924
                FrequencyBand.BAND_6 -> net.frequency in 5925..7125
            }
        }
        val candidates = supportedChannels ?: if (band == FrequencyBand.BAND_2_4) {
            setOf(1, 6, 11)
        } else emptySet()
        val bestChannel = if (hasBandObservations) {
            scores.filter { it.channelNumber in candidates }.minByOrNull { it.score }
        } else null

        return scores.map {
            if (it == bestChannel) it.copy(recommendation = true) else it
        }
    }
}
