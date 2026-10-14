package com.pulsewave.app.feature.survey

import com.pulsewave.app.core.wifi.WifiNetwork
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

    @Test fun stepMovementUsesPhysicalMapDimensions() {
        val east = moveSurveyPosition(SurveyPosition(0.5f, 0.5f), 90f, 0.7f, 14f, 7f)
        assertEquals(0.55f, east.x, 0.0001f)
        assertEquals(0.5f, east.y, 0.0001f)
        val north = moveSurveyPosition(SurveyPosition(0.5f, 0.5f), 0f, 0.7f, 14f, 7f)
        assertEquals(0.5f, north.x, 0.0001f)
        assertEquals(0.4f, north.y, 0.0001f)
        assertEquals(0f, moveSurveyPosition(SurveyPosition(0f, 0f), 0f, 0.7f, 14f, 7f).y, 0f)
    }

    @Test fun mapBearingAlignsMovementWithRotatedFloorplan() {
        // The floorplan's top points east: an eastward step moves upward on the map.
        val position = moveSurveyPosition(SurveyPosition(0.5f, 0.5f), 90f, 0.7f, 14f, 7f, mapTopBearingDegrees = 90f)
        assertEquals(0.5f, position.x, 0.0001f)
        assertEquals(0.4f, position.y, 0.0001f)
    }
}
