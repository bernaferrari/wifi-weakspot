package com.pulsewave.app.feature.analyze

import com.pulsewave.app.core.wifi.WifiNetwork
import kotlin.math.pow

enum class TrafficRating(val label: String, val colorHex: Long) {
    LOW("Open Road", 0xFF4CAF50),      // Green
    MEDIUM("Moderate", 0xFFFFC107),    // Amber
    HIGH("Congested", 0xFFFF5722),     // Orange
    SEVERE("Gridlock", 0xFFB00020);    // Red
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

    // 6GHz (WiFi 6E - 1200 MHz spectrum, channels 1-233 in steps of 4)
    // Center frequencies from 5940 MHz (ch 1) to 7105 MHz (ch 233)
    private val channels6 = (1..233 step 4).associate { ch ->
        ch to (5940 + (ch - 1) * 5)
    }

    fun analyze(networks: List<WifiNetwork>, band: FrequencyBand): List<ChannelScore> {
        val targetChannels = when (band) {
            FrequencyBand.BAND_2_4 -> channels24
            FrequencyBand.BAND_5 -> channels5
            FrequencyBand.BAND_6 -> channels6
        }

        if (targetChannels.isEmpty()) return emptyList()

        val scores = targetChannels.map { (channel, centerFreq) ->
            var totalEnergy = 0.0
            
            networks.forEach { net ->
                // Check if network overlaps this channel
                // WiFi channel width is typically 20MHz (+/- 10MHz form center)
                // We consider a network relevant if its center freq is within 12MHz of the channel center
                // (Simplistic overlap model)
                val diff = kotlin.math.abs(net.frequency - centerFreq)
                if (diff < 12) {
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
        val bestChannel = if (band == FrequencyBand.BAND_2_4) {
            val nonOverlapping = listOf(1, 6, 11)
            scores.filter { it.channelNumber in nonOverlapping }
                .minByOrNull { it.score } 
                ?: scores.minByOrNull { it.score }
        } else {
            scores.minByOrNull { it.score }
        }

        return scores.map { 
            if (it == bestChannel) it.copy(recommendation = true) else it
        }
    }
}
