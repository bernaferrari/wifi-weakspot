package com.bernaferrari.wifiweakspot.feature.analyze

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import org.junit.Assert.*
import org.junit.Test

class ChartInspectionTest {
    private fun readings(vararg values: Int?) = values.mapIndexed { index, value -> value?.let { SignalObservation(it, index.toLong()) } }

    @Test fun maximumSignalLabelStaysInsideChartAtBothEdges() {
        val bounds = Rect(52f, 4f, 320f, 300f)
        for (x in listOf(bounds.left, bounds.right)) {
            val label = chartLabelRect(Offset(x, 12f), Size(160f, 24f), bounds, 4f)
            assertTrue("Top label is clipped: $label", label.top >= bounds.top)
            assertTrue(label.left >= bounds.left)
            assertTrue(label.right <= bounds.right)
        }
    }

    @Test fun laterRadioHasNoInventedOlderReading() {
        assertNull(historyValueAt(readings(null, null, null, -60, -55), 0, 5))
        assertEquals(-60, historyValueAt(readings(null, null, null, -60, -55), 3, 5))
        assertEquals(-55, historyValueAt(readings(null, null, null, -60, -55), 4, 5))
    }

    @Test fun compareNetworksUsesSignalAtInspectedTimeRatherThanLatestStrength() {
        val history = mapOf("a" to readings(-50, -80), "b" to readings(-70, -40), "c" to readings(-90, -60))
        assertEquals("b", historyNeighborBssid(history, history.keys.toList(), 0, 2, "a", 1))
        assertEquals("a", historyNeighborBssid(history, history.keys.toList(), 0, 2, "b", -1))
        assertEquals("c", historyNeighborBssid(history, history.keys.toList(), 1, 2, "b", 1))
    }

    @Test fun compareNetworksSkipsMissingReadingsAndStopsAtTheEdges() {
        val history = mapOf("a" to readings(-50, -50, -50), "b" to readings(null, null, -60), "c" to readings(-80, -80, -80))
        assertEquals("c", historyNeighborBssid(history, history.keys.toList(), 0, 3, "a", 1))
        assertEquals("a", historyNeighborBssid(history, history.keys.toList(), 0, 3, "a", -1))
        assertEquals("c", historyNeighborBssid(history, history.keys.toList(), 0, 3, "c", 1))
        assertNull(historyNeighborBssid(history, listOf("b"), 0, 3, "b", 1))
    }
    @Test fun channelNavigationCrossesFiveGhzGapsAndClampsToEndpoints() {
        val channels = exploreChannels(FrequencyBand.BAND_5)
        val beforeGap = channels.indexOfFirst { it.first == 64 }
        assertEquals(100, channelAtProgress(FrequencyBand.BAND_5, (beforeGap + 1).toFloat()))
        assertEquals(64, channelAtProgress(FrequencyBand.BAND_5, beforeGap.toFloat()))
        assertEquals(36, channelAtProgress(FrequencyBand.BAND_5, -1f))
        assertEquals(165, channelAtProgress(FrequencyBand.BAND_5, 1000f))
        assertNull(channelAtProgress(FrequencyBand.BAND_5, Float.NaN))
        assertNull(channelAtProgress(FrequencyBand.BAND_5, Float.POSITIVE_INFINITY))
    }

    @Test fun channelNavigationExposesEverySixGhzChannel() {
        val channels = exploreChannels(FrequencyBand.BAND_6)
        assertEquals((1..233 step 4).toList(), channels.indices.map { channelAtProgress(FrequencyBand.BAND_6, it.toFloat()) })
    }
}
