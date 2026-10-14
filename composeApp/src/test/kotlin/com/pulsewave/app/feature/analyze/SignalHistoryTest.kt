package com.pulsewave.app.feature.analyze

import com.pulsewave.app.core.wifi.WifiNetwork
import org.junit.Assert.*
import org.junit.Test

class SignalHistoryTest {
    private fun network(bssid: String, time: Long, rssi: Int = -50) = WifiNetwork(
        "Network", bssid, rssi, 5180, 20, "WPA2", "", time,
        observedElapsedRealtimeMicros = time * 1000
    )

    @Test fun repeatedCacheAndOlderSamplesAreNotNewHistory() {
        val history = SignalHistory()
        assertEquals(1, history.record(listOf(network("a", 100))).size)
        assertTrue(history.record(listOf(network("a", 100), network("a", 99))).isEmpty())
        assertEquals(listOf(-50), history.snapshot()["a"])
    }

    @Test fun epochConversionDriftDoesNotDuplicatePlatformObservation() {
        val history = SignalHistory()
        val first = network("a", 100)
        history.record(listOf(first))
        assertTrue(history.record(listOf(first.copy(timestamp = 101))).isEmpty())
    }

    @Test fun limitsBothNetworksAndSamplesAndEvictsLeastRecentlyObserved() {
        val history = SignalHistory(maxNetworks = 2, maxSamples = 2)
        history.record(listOf(network("a", 1), network("b", 1)))
        history.record(listOf(network("a", 2, -60), network("a", 3, -70)))
        history.record(listOf(network("c", 1)))
        assertEquals(setOf("a", "c"), history.snapshot().keys)
        assertEquals(listOf(-60, -70), history.snapshot()["a"])
    }
}
