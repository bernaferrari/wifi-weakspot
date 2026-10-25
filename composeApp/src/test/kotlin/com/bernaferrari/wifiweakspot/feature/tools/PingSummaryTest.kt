package com.bernaferrari.wifiweakspot.feature.tools

import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Test

class PingSummaryTest {
    private fun lines(vararg ms: Float?) = ms.map { if (it == null) "Request timed out: timeout" else "Reply from x: time=$it ms" }

    @Test fun steadyLocalRepliesAreResponsive() {
        val summary = PingSummary.of(lines(4f, 5f, 4.5f, 6f), "192.168.0.1")
        assertEquals("Responsive", summary.headline)
        assertTrue(summary.steady)
    }

    @Test fun spreadRepliesAreUneven() {
        val summary = PingSummary.of(lines(17.3f, 5.3f, 6.8f, 4.4f), "192.168.0.1")
        assertEquals("Responsive, but uneven", summary.headline)
        assertFalse(summary.steady)
    }

    @Test fun internetIsJudgedMoreGently() =
        assertEquals(SignalQuality.Excellent, PingSummary.of(lines(22f, 24f, 23f), "1.1.1.1").quality)

    @Test fun noRepliesIsDeadZone() =
        assertEquals("Didn't answer", PingSummary.of(lines(null, null), "google.com").headline)
}
