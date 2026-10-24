package com.bernaferrari.wifiweakspot.feature.analyze

import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork

/** Bounded observations, rather than the number of times Android republishes its cache. */
internal class SignalHistory(
    private val maxNetworks: Int = 100,
    private val maxSamples: Int = 100
) {
    private data class Entry(val latest: WifiNetwork, val samples: List<Int>)
    private val entries = linkedMapOf<String, Entry>()

    fun record(networks: List<WifiNetwork>): List<WifiNetwork> {
        val accepted = mutableListOf<WifiNetwork>()
        networks.forEach { network ->
            val previous = entries[network.bssid]
            val isNewer = previous == null || if (
                network.observedElapsedRealtimeMicros > 0 && previous.latest.observedElapsedRealtimeMicros > 0
            ) {
                network.observedElapsedRealtimeMicros > previous.latest.observedElapsedRealtimeMicros
            } else network.timestamp > previous.latest.timestamp
            if (!isNewer) return@forEach
            val samples = ((previous?.samples ?: emptyList()) + network.rssi).takeLast(maxSamples)
            entries.remove(network.bssid)
            entries[network.bssid] = Entry(network, samples)
            accepted.add(network)
            while (entries.size > maxNetworks) entries.remove(entries.keys.first())
        }
        return accepted
    }

    fun snapshot(): Map<String, List<Int>> = entries.mapValues { it.value.samples }
}
