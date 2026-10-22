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
}
