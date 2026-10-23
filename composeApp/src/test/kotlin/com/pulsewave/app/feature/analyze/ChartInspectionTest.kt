package com.pulsewave.app.feature.analyze

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import org.junit.Assert.*
import org.junit.Test

class ChartInspectionTest {
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
        assertNull(historyValueAt(listOf(-60, -55), 0, 5))
        assertEquals(-60, historyValueAt(listOf(-60, -55), 3, 5))
        assertEquals(-55, historyValueAt(listOf(-60, -55), 4, 5))
    }

    @Test fun compareNetworksUsesSignalAtInspectedTimeRatherThanLatestStrength() {
        val history = mapOf("a" to listOf(-50, -80), "b" to listOf(-70, -40), "c" to listOf(-90, -60))
        assertEquals("b", historyNeighborBssid(history, history.keys.toList(), 0, 2, "a", 1))
        assertEquals("a", historyNeighborBssid(history, history.keys.toList(), 0, 2, "b", -1))
        assertEquals("c", historyNeighborBssid(history, history.keys.toList(), 1, 2, "b", 1))
    }

    @Test fun compareNetworksSkipsMissingReadingsAndStopsAtTheEdges() {
        val history = mapOf("a" to listOf(-50, -50, -50), "b" to listOf(-60), "c" to listOf(-80, -80, -80))
        assertEquals("c", historyNeighborBssid(history, history.keys.toList(), 0, 3, "a", 1))
        assertEquals("a", historyNeighborBssid(history, history.keys.toList(), 0, 3, "a", -1))
        assertEquals("c", historyNeighborBssid(history, history.keys.toList(), 0, 3, "c", 1))
        assertNull(historyNeighborBssid(history, listOf("b"), 0, 3, "b", 1))
    }
}
