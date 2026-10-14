package com.pulsewave.app.core.wifi

import com.pulsewave.app.core.export.csvField
import com.pulsewave.app.feature.history.formatDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiRadioTest {

    @Test
    fun frequencyToChannel_mapsStandardCenters() {
        assertEquals(1, frequencyToChannel(2412))
        assertEquals(6, frequencyToChannel(2437))
        assertEquals(14, frequencyToChannel(2484))
        assertEquals(36, frequencyToChannel(5180))
        assertEquals(100, frequencyToChannel(5500))
        assertEquals(149, frequencyToChannel(5745))
        assertEquals(165, frequencyToChannel(5825))
        assertEquals(1, frequencyToChannel(5955))
        assertEquals(233, frequencyToChannel(7115))
    }

    @Test
    fun channelWidthMhz_convertsEnumAndKeepsRealWidths() {
        assertEquals(20, channelWidthMhz(0))
        assertEquals(40, channelWidthMhz(1))
        assertEquals(80, channelWidthMhz(2))
        assertEquals(320, channelWidthMhz(5))
        assertEquals(20, channelWidthMhz(20))
    }

    @Test
    fun distance_growsAsSignalDrops_andFiveGigahertzIsCloser() {
        val near = estimateDistanceMeters(-40, 2437)
        val mid = estimateDistanceMeters(-70, 2437)
        val far = estimateDistanceMeters(-90, 2437)
        assertTrue(near < mid)
        assertTrue(mid < far)

        val atFive = estimateDistanceMeters(-70, 5200)
        val atTwo = estimateDistanceMeters(-70, 2437)
        assertTrue(atFive < atTwo)
    }

    @Test
    fun openCapabilities_areNotSecured() {
        assertFalse(isNetworkSecured("[ESS]"))
        assertEquals("Open", securityLabel("[ESS]"))
        assertTrue(isNetworkSecured("[WPA2-PSK-CCMP][RSN]"))
        assertEquals("WPA2", securityLabel("[WPA2-PSK-CCMP][RSN]"))
        assertEquals("WPA3", securityLabel("[SAE]"))
        assertEquals("", securityLabel(""))
    }

    @Test
    fun scanTimestamp_isAgeBeforeNow() {
        val now = 1_700_000_000_000L
        val elapsedMicros = 20_000_000L
        val scanMicros = 5_000_000L
        assertEquals(now - 15_000L, scanTimestampToEpochMillis(scanMicros, now, elapsedMicros))
        assertEquals(now, scanTimestampToEpochMillis(elapsedMicros + 1_000L, now, elapsedMicros))
    }

    @Test
    fun csvField_doublesQuotes() {
        assertEquals("\"say \"\"hi\"\"\"", csvField("say \"hi\""))
    }

    @Test
    fun formatDate_padsHoursAndMinutes() {
        val formatted = formatDate(1_700_000_000_000L)
        assertTrue(formatted.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")))
    }
}
