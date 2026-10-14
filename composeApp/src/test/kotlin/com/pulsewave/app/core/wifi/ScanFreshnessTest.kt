package com.pulsewave.app.core.wifi

import org.junit.Assert.*
import org.junit.Test

class ScanFreshnessTest {
    private fun network(observedMicros: Long) = WifiNetwork(
        "Network", "00:11:22:33:44:55", -50, 5180, 20, "WPA2", "", 1000,
        observedElapsedRealtimeMicros = observedMicros
    )

    @Test fun failedBroadcastCannotPromoteCacheToFreshMeasurements() {
        assertNull(freshScanResults(listOf(network(200)), false, 100))
    }

    @Test fun successfulBroadcastRejectsCachedAndUnknownObservations() {
        assertNull(freshScanResults(listOf(network(99), network(0)), true, 100))
        assertNull(freshScanResults(emptyList(), true, 100))
    }

    @Test fun mixedBatchContainsOnlyObservationsAfterRequest() {
        val fresh = network(200).copy(bssid = "new")
        assertEquals(listOf(fresh), freshScanResults(listOf(network(99), fresh), true, 100))
    }

    @Test fun wideUnknownCenterDoesNotInventOccupiedChannels() {
        assertEquals(listOf(FrequencySegment(5180, 20)),
            occupiedFrequencySegments(network(100).copy(channelWidth = 160)))
    }

    @Test fun real160And320CentersRetainWholeOccupiedWidth() {
        val wide = network(100).copy(channelWidth = 160, centerFreq0 = 5250)
        assertTrue(occupiedFrequencySegments(wide).single().overlaps(5320))
        assertFalse(occupiedFrequencySegments(wide).single().overlaps(5340))
        val six = wide.copy(frequency = 5955, channelWidth = 320, centerFreq0 = 6105)
        assertTrue(occupiedFrequencySegments(six).single().overlaps(6255))
        assertFalse(occupiedFrequencySegments(six).single().overlaps(6275))
    }

    @Test fun adjacent20MHzChannelsTouchButDoNotOverlap() {
        assertFalse(FrequencySegment(5180, 20).overlaps(5200))
        assertTrue(FrequencySegment(2437, 20).overlaps(2432))
    }
}
