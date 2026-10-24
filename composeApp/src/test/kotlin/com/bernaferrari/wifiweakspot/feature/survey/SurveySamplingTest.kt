package com.bernaferrari.wifiweakspot.feature.survey

import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import org.junit.Assert.*
import org.junit.Test

class SurveySamplingTest {
    private val now = 1_000_000L
    private fun network(bssid: String, rssi: Int = -60, timestamp: Long = now) =
        WifiNetwork("Home", bssid, rssi, 2412, 20, "WPA2", "", timestamp)

    @Test fun tapsUseTheirActualLocationInsteadOfTheOrigin() {
        assertEquals(SurveyPosition(0.75f, 0.25f), normalizedSurveyPosition(300f, 200f, 400f, 800f))
        assertEquals(SurveyPosition(1f, 0f), normalizedSurveyPosition(450f, -20f, 400f, 800f))
        assertNull(normalizedSurveyPosition(20f, 20f, 0f, 800f))
    }

    @Test fun weakerSelectedAccessPointWinsOverStrongerNeighbor() {
        val selected = network("selected", -85)
        val neighbor = network("neighbor", -20)
        assertEquals(selected, freshSurveyMeasurement(listOf(neighbor, selected), "SELECTED", now, now))
        assertNull(freshSurveyMeasurement(listOf(neighbor), "selected", now, now))
    }

    @Test fun staleAndFutureObservationsAreNotSavedAsFreshSamples() {
        assertNull(freshSurveyMeasurement(listOf(network("selected", timestamp = now - 20_000)), "selected", now, now))
        assertNull(freshSurveyMeasurement(listOf(network("selected", timestamp = now + 2_000)), "selected", now, now))
        assertNull(freshSurveyMeasurement(emptyList(), "selected", now, now))
        assertNotNull(freshSurveyMeasurement(listOf(network("selected", timestamp = now - 2_000)), "selected", now, now))
    }

    @Test fun liveLinkReadingNeedsTheMappedAccessPoint() {
        fun status(bssid: String, rssi: Int, sampledAt: Long) = com.bernaferrari.wifiweakspot.core.model.NetworkStatus(
            "Home", bssid, 5180, 36, rssi, 400, "192.168.1.2", true, "WPA2", sampledAtMillis = sampledAt)
        // Median of the matching readings; a single odd one doesn't move it.
        assertEquals(-61, linkSurveyMeasurement(listOf(status("ap", -60, 1000), status("ap", -61, 1500), status("ap", -80, 2000)), "AP", 2000L)!!.rssi)
        // Roamed to another access point: no reading from the link.
        assertEquals(null, linkSurveyMeasurement(listOf(status("other", -50, 1000), status("other", -50, 1500)), "ap", 1500L))
    }

    @Test fun liveLinkRejectsRepeatedBurstAndMixedAccessPointReadings() {
        fun status(time: Long, bssid: String = "ap") = com.bernaferrari.wifiweakspot.core.model.NetworkStatus(
            "Home", bssid, 5180, 36, -60, 400, "192.168.1.2", true, "WPA2", sampledAtMillis = time)
        assertNull(linkSurveyMeasurement(listOf(status(1000), status(1000), status(1000)), "ap", 2000))
        assertNull(linkSurveyMeasurement(listOf(status(1000), status(1001), status(1002)), "ap", 2000))
        assertNull(linkSurveyMeasurement(listOf(status(1000), status(1500, "other"), status(2000)), "ap", 2000))
        val result = linkSurveyMeasurement(listOf(status(1000), status(1500), status(2000)), "ap", 2100)
        assertNotNull(result)
        assertEquals(2000L, result!!.timestamp)
    }
}
