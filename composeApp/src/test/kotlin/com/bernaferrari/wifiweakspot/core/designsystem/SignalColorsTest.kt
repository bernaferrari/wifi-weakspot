package com.bernaferrari.wifiweakspot.core.designsystem

import androidx.compose.ui.graphics.Color
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import com.materialkolor.ktx.toHct
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalColorsTest {
    private val lightSurface = Color(0xFFFBF8FF)
    private val darkSurface = Color(0xFF121318)

    @Test fun badgeTextIsReadableOnItsContainerInBothThemes() {
        for (dark in listOf(false, true)) for (quality in SignalQuality.entries) {
            val colors = signalColorsFor(quality, dark)
            val ratio = contrastRatio(colors.container, colors.onContainer)
            assertTrue("Badge contrast $ratio for $quality, dark=$dark", ratio >= 4.5f)
        }
    }

    @Test fun accentReadsAsTextOnTheSurfaceInBothThemes() {
        for (dark in listOf(false, true)) for (quality in SignalQuality.entries) {
            val surface = if (dark) darkSurface else lightSurface
            val ratio = contrastRatio(signalColorsFor(quality, dark).accent, surface)
            assertTrue("Accent contrast $ratio for $quality, dark=$dark", ratio >= 4.5f)
        }
    }

    @Test fun endsAreRethinksGreenAndMaterialErrorRed() {
        // Re-Rethink: allowed is the Fidelity primary of #1E9E5A, blocked is Material's error.
        assertEquals(156.0, signalColorsFor(SignalQuality.Excellent, dark = false).accent.toHct().hue, 3.0)
        assertEquals(Color(0xFFBA1A1A), signalColorsFor(SignalQuality.DeadZone, dark = false).accent)
        assertEquals(Color(0xFFFFB4AB), signalColorsFor(SignalQuality.DeadZone, dark = true).accent)
        assertEquals(Color(0xFFFFDAD6), signalColorsFor(SignalQuality.DeadZone, dark = false).container)
        assertEquals(Color(0xFF93000A), signalColorsFor(SignalQuality.DeadZone, dark = true).container)
    }

    @Test fun neighbouringHeatColorsStayDistinct() {
        for (dark in listOf(false, true)) {
            val heat = signalHeatColorsFor(dark)
            SignalQuality.entries.zipWithNext().forEach { (a, b) ->
                val ca = heat.getValue(a).toHct()
                val cb = heat.getValue(b).toHct()
                val hueGap = kotlin.math.abs(ca.hue - cb.hue).let { minOf(it, 360 - it) }
                val toneGap = kotlin.math.abs(ca.tone - cb.tone)
                assertTrue("$a vs $b too close, dark=$dark", hueGap >= 10 || toneGap >= 10)
            }
        }
    }
}
