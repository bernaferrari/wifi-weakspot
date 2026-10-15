package com.pulsewave.app.core.rooms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CoverageTest {
    private fun spot(name: String, rssi: Int, freq: Int = 5180, bssid: String = "aa:bb:cc:dd:ee:01") =
        RoomReading(name, name, rssi, rssi, 400, freq, "Home", bssid, 4, 0L)

    @Test
    fun summarizeRoomIgnoresInvalidSamples() {
        val reading = summarizeRoom("1", " Kitchen ", listOf(0, -60, -62, 5), listOf(300, 0), 2437, "Home", "b", 1L)!!
        assertEquals("Kitchen", reading.name)
        assertEquals(-61, reading.averageRssi)
        assertEquals(-62, reading.minRssi)
        assertEquals(300, reading.linkSpeedMbps)
        assertEquals(2, reading.samples)
        assertNull(summarizeRoom("2", "Empty", listOf(0), emptyList(), 0, "", "", 1L))
    }

    @Test
    fun newMeasurementPreservesPreviousSameName() {
        val list = listOf(spot("Kitchen", -60)).upsert(spot("kitchen", -70))
        assertEquals(listOf(-60, -70), list.map { it.averageRssi })
    }

    @Test
    fun summaryRanksWeakestFirstAndExplainsHighBandDrop() {
        val summary = coverageSummary(listOf(spot("Office", -50), spot("Garage", -85), spot("Bedroom", -78)))
        assertEquals(listOf("Garage", "Bedroom", "Office"), summary.rankedRooms.map { it.name })
        assertEquals("2 weak spots found", summary.headline)
        assertTrue(summary.tips.first().contains("Garage"))
        assertTrue(summary.tips.any { it.contains("35 dB") })
    }

    @Test
    fun walkSummaryCountsSeparateWeakStretches() {
        val samples = listOf(-50, -85, -86, -60, -84, -55).mapIndexed { i, rssi -> WalkSample(i * 1000L, rssi, if (i % 2 == 0) 3f else 9f) }
        val summary = summarizeWalk(samples)!!
        assertEquals(2, summary.weakMoments)
        assertEquals(-50, summary.bestRssi)
        assertEquals(-86, summary.worstRssi)
        assertEquals(0.6f, summary.weakShare, 0.001f)
        assertEquals(5_000L, summary.durationMs)
        assertEquals(6f, summary.medianRouterMs)
        assertNull(summarizeWalk(emptyList()))
    }
}
