package com.bernaferrari.wifiweakspot.feature.analyze

import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import org.junit.Assert.*
import org.junit.Test

class SignalHistoryTest {
    private fun network(bssid: String, time: Long, rssi: Int = -50) = WifiNetwork(
        "Network", bssid, rssi, 5180, 20, "WPA2", "", time,
        observedElapsedRealtimeMicros = time * 1000
    )

    private fun SignalHistory.rssis() = snapshot().mapValues { (_, values) -> values.map { it?.rssi } }

    @Test fun repeatedCacheAndOlderSamplesAreNotNewHistory() {
        val history = SignalHistory()
        assertEquals(1, history.record(listOf(network("a", 100))).size)
        assertTrue(history.record(listOf(network("a", 100), network("a", 99))).isEmpty())
        assertEquals(listOf(-50), history.rssis()["a"])
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
        history.record(listOf(network("a", 2, -60)))
        history.record(listOf(network("a", 3, -70), network("c", 3)))
        assertEquals(setOf("a", "c"), history.snapshot().keys)
        assertEquals(listOf(-60, -70), history.rssis()["a"])
    }
    @Test fun missedMiddleObservationKeepsItsGapWhenRadioReturns() {
        val history = SignalHistory()
        history.record(listOf(network("a", 1, -50), network("b", 1, -60)))
        history.record(listOf(network("a", 2, -51), network("b", 1, -60)))
        history.record(listOf(network("a", 3, -52), network("b", 3, -62)))
        assertEquals(listOf(-50, -51, -52), history.rssis()["a"])
        assertEquals(listOf(-60, null, -62), history.rssis()["b"])
        assertNull(historyValueAt(history.snapshot().getValue("b"), 1, 3))
        assertEquals(-60, historyValueAt(history.snapshot().getValue("b"), 0, 3))
    }

    @Test fun cachedFinalObservationNeverMovesToLatestFrame() {
        val history = SignalHistory()
        history.record(listOf(network("a", 1), network("b", 1, -60)))
        history.record(listOf(network("a", 2), network("b", 1, -60)))
        assertEquals(listOf(-60, null), history.rssis()["b"])
        assertNull(historyValueAt(history.snapshot().getValue("b"), 1, 2))
        assertTrue(history.record(listOf(network("a", 2), network("b", 1, -60))).isEmpty())
        assertEquals(2, history.snapshot().getValue("a").size)
    }

    @Test fun lateDiscoveryAndWindowTrimmingPreserveSharedColumns() {
        val history = SignalHistory(maxSamples = 3)
        history.record(listOf(network("a", 1)))
        history.record(listOf(network("a", 2)))
        history.record(listOf(network("a", 3), network("b", 3, -60)))
        assertEquals(listOf(null, null, -60), history.rssis()["b"])
        history.record(listOf(network("a", 4)))
        assertEquals(listOf(null, -60, null), history.rssis()["b"])
        assertTrue(history.snapshot().values.all { it.size == 3 })
    }
    @Test fun differentRadiosWithSameTimestampShareFrameWithoutLosingEither() {
        val history = SignalHistory()
        history.record(listOf(network("a", 10_000, -50)))
        history.record(listOf(network("b", 10_000, -60)))
        assertEquals(listOf(-50), history.rssis()["a"])
        assertEquals(listOf(-60), history.rssis()["b"])
    }

    @Test fun lateOlderObservationBackfillsItsFrameWithoutMovingToLatest() {
        val history = SignalHistory()
        history.record(listOf(network("a", 10_000, -50)))
        history.record(listOf(network("a", 20_000, -51)))
        history.record(listOf(network("a", 30_000, -52), network("b", 19_000, -60)))
        assertEquals(listOf(null, -60, null), history.rssis()["b"])
        assertEquals(19_000L, history.snapshot().getValue("b")[1]!!.timestampMillis)
    }

    @Test fun baselineCacheWithMixedAgesIsSplitIntoBoundedFrames() {
        val history = SignalHistory()
        history.record(listOf(network("a", 10_000, -50), network("b", 30_000, -60), network("c", 29_000, -70)))
        assertEquals(listOf(-50, null), history.rssis()["a"])
        assertEquals(listOf(null, -60), history.rssis()["b"])
        assertEquals(listOf(null, -70), history.rssis()["c"])
    }

    @Test fun lateObservationOutsideRetainedWindowDoesNotBecomeLatest() {
        val history = SignalHistory(maxSamples = 2)
        history.record(listOf(network("a", 10_000)))
        history.record(listOf(network("a", 20_000)))
        history.record(listOf(network("a", 30_000), network("b", 10_000)))
        assertEquals(listOf(null, null), history.rssis()["b"])
    }
    @Test fun wallClockChangeDoesNotMoveNewPlatformSampleIntoOlderFrame() {
        val history = SignalHistory()
        history.record(listOf(network("a", 10_000, -50)))
        history.record(listOf(network("a", 20_000, -60).copy(timestamp = 1_000)))
        assertEquals(listOf(-50, -60), history.rssis()["a"])
        assertEquals(1_000L, history.snapshot().getValue("a").last()!!.timestampMillis)
    }

    @Test fun unknownMonotonicTimestampDoesNotContaminatePlatformTimeline() {
        val history = SignalHistory()
        history.record(listOf(network("a", 10_000)))
        history.record(listOf(network("a", 20_000), network("b", 20_000).copy(observedElapsedRealtimeMicros = 0)))
        assertFalse(history.snapshot().containsKey("b"))
    }
    @Test fun lateStaleRadioDoesNotEvictMoreRecentlyObservedRadios() {
        val history = SignalHistory(maxNetworks = 2)
        history.record(listOf(network("a", 20_000), network("b", 20_000)))
        history.record(listOf(network("c", 1_000)))
        assertEquals(setOf("a", "b"), history.snapshot().keys)
    }
}
