package com.pulsewave.app.core.designsystem

import androidx.compose.ui.graphics.Color
import com.materialkolor.hct.Hct
import com.materialkolor.ktx.toColor
import org.junit.Assert.*
import org.junit.Test

class SignalMeterColorsTest {
    @Test fun surfaceInkIsReadableOnBothMeterRegionsAcrossThemesAndSignalHues() {
        for (dark in listOf(false, true)) {
            val surface = if (dark) Color(0xFF0A082D) else Color(0xFFFFFBFF)
            for (hue in 0 until 360 step 30) for (chroma in listOf(0.0, 32.0, 64.0, 96.0)) {
                fun tone(value: Double) = Hct.from(hue.toDouble(), chroma, value).toColor()
                val signal = if (dark) SignalColors(tone(80.0), tone(30.0), tone(90.0))
                    else SignalColors(tone(50.0), tone(90.0), tone(10.0))
                val meter = signalMeterColors(signal, surface)
                val context = "dark=$dark, hue=$hue, chroma=$chroma"
                assertEquals(surface, meter.onContainer)
                assertTrue("Fill contrast: $context", contrastRatio(meter.onContainer, meter.accent) >= 7f)
                assertTrue("Track contrast: $context", contrastRatio(meter.onContainer, meter.container) >= 4.5f)
            }
        }
    }
}
