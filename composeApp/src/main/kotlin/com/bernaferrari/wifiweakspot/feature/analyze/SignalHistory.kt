package com.bernaferrari.wifiweakspot.feature.analyze

import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork

/** Original measurement time is retained even when nearby observations share a scan frame. */
data class SignalObservation(val rssi: Int, val timestampMillis: Long)

/** Every column is a bounded scan frame. Null means no observation in that frame. */
typealias SignalTimeline = Map<String, List<SignalObservation?>>

/**
 * Bounded scan frames preserve missing readings and never promote an old cache entry to latest.
 * Android observes APs at slightly different times during a scan. A frame groups at most five
 * seconds of observations; an initial mixed-age cache is split into separate historical frames.
 */
internal class SignalHistory(
    private val maxNetworks: Int = 100,
    private val maxSamples: Int = 100,
    private val maxFrameSpanMillis: Long = 5_000
) {
    private data class Frame(val firstMillis: Long, val lastMillis: Long)
    private data class Entry(val latest: WifiNetwork, val samples: Map<Long, SignalObservation>)
    private val entries = linkedMapOf<String, Entry>()
    private val frames = mutableListOf<Frame>()
    private var useMonotonicClock: Boolean? = null

    private fun observationTime(network: WifiNetwork): Long =
        if (useMonotonicClock == true) network.observedElapsedRealtimeMicros / 1_000 else network.timestamp

    init { require(maxNetworks > 0 && maxSamples > 0 && maxFrameSpanMillis >= 0) }

    fun record(networks: List<WifiNetwork>): List<WifiNetwork> {
        if (networks.isEmpty()) return emptyList()
        if (useMonotonicClock == null) useMonotonicClock = networks.any { it.observedElapsedRealtimeMicros > 0 }
        val accepted = linkedMapOf<String, WifiNetwork>()
        networks.forEach { network ->
            // Do not mix unknown/platform-less timestamps into an Android monotonic timeline.
            if (useMonotonicClock == true && network.observedElapsedRealtimeMicros <= 0) return@forEach
            val previous = accepted[network.bssid] ?: entries[network.bssid]?.latest
            val isNewer = previous == null || if (
                network.observedElapsedRealtimeMicros > 0 && previous.observedElapsedRealtimeMicros > 0
            ) {
                network.observedElapsedRealtimeMicros > previous.observedElapsedRealtimeMicros
            } else network.timestamp > previous.timestamp
            if (isNewer) accepted[network.bssid] = network
        }
        if (accepted.isEmpty()) return emptyList()
        val previousEnd = frames.lastOrNull()?.lastMillis
        val futureTimes = accepted.values.map(::observationTime)
            .filter { previousEnd == null || it > previousEnd }.distinct().sortedDescending()
        // Group backwards from the newest observation, bounding every frame's age spread.
        val added = mutableListOf<Frame>()
        var remaining = futureTimes
        while (remaining.isNotEmpty()) {
            val end = remaining.first()
            val start = maxOf(end - maxFrameSpanMillis, previousEnd?.plus(1) ?: Long.MIN_VALUE)
            added += Frame(start, end)
            remaining = remaining.dropWhile { it >= start }
        }
        frames += added.asReversed()
        while (frames.size > maxSamples) frames.removeAt(0)
        val retained = frames.map { it.lastMillis }.toSet()
        entries.keys.toList().forEach { bssid ->
            val entry = entries.getValue(bssid)
            entries[bssid] = entry.copy(samples = entry.samples.filterKeys { it in retained })
        }
        accepted.forEach { (bssid, network) ->
            val existing = entries.remove(bssid)
            val samples = existing?.samples.orEmpty().toMutableMap()
            val time = observationTime(network)
            val frame = frames.firstOrNull { time in it.firstMillis..it.lastMillis }
            if (frame != null) samples[frame.lastMillis] = SignalObservation(network.rssi, network.timestamp)
            // A late sample outside the retained frames is remembered for deduplication only.
            entries[bssid] = Entry(network, samples)
        }
        val excess = entries.size - maxNetworks
        if (excess > 0) entries.entries.sortedBy { observationTime(it.value.latest) }
            .take(excess).map { it.key }.forEach(entries::remove)
        return accepted.values.toList()
    }

    fun snapshot(): SignalTimeline = entries.mapValues { (_, entry) -> frames.map { entry.samples[it.lastMillis] } }
}
