package com.pulsewave.app.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.materialkolor.hct.Hct
import com.materialkolor.ktx.harmonize
import com.materialkolor.ktx.toColor
import com.materialkolor.ktx.toHct
import com.pulsewave.app.core.wifi.SignalQuality

// App colors come from the dynamic or seeded scheme in PulseWaveTheme.
// Signal colors are the only fixed hues; they are harmonized toward the theme's primary
// and toned like Material roles so they keep contrast in light and dark themes.

@Immutable
data class SignalColors(
    /** Strong mark: meters, dots, graph strokes. */
    val accent: Color,
    val container: Color,
    val onContainer: Color
)

private val SignalHues = mapOf(
    SignalQuality.Excellent to Color(0xFF1E8E3E),
    SignalQuality.Good to Color(0xFF00897B),
    SignalQuality.Fair to Color(0xFFF9AB00),
    SignalQuality.Weak to Color(0xFFF57C00),
    SignalQuality.DeadZone to Color(0xFFD93025)
)

private val signalColorCache = HashMap<Triple<SignalQuality, Int, Boolean>, SignalColors>()

private fun Hct.tone(tone: Double): Color = Hct.from(hue, chroma, tone).toColor()

@Composable
@ReadOnlyComposable
fun signalColors(quality: SignalQuality): SignalColors {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    val primary = scheme.primary
    val key = Triple(quality, primary.hashCode(), dark)
    return signalColorCache.getOrPut(key) {
        val hct = SignalHues.getValue(quality).harmonize(primary).toHct()
        if (dark) SignalColors(accent = hct.tone(80.0), container = hct.tone(30.0), onContainer = hct.tone(90.0))
        else SignalColors(accent = hct.tone(50.0), container = hct.tone(90.0), onContainer = hct.tone(10.0))
    }
}

@Composable
@ReadOnlyComposable
fun signalColors(rssi: Int): SignalColors = signalColors(SignalQuality.of(rssi))

private val heatColorCache = HashMap<Pair<Int, Boolean>, Map<SignalQuality, Color>>()

/**
 * Vivid signal colors for large fields such as the floor-plan heatmap. Same hues as [signalColors],
 * but pushed to high chroma at a mid tone (HCT is perceptual, like OKLCH), so a whole map of them
 * reads as clear green-to-red instead of the pale tints that suit text and badges.
 */
@Composable
@ReadOnlyComposable
fun signalHeatColors(): Map<SignalQuality, Color> {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.surface.luminance() < 0.5f
    return heatColorCache.getOrPut(0 to dark) {
        // Not harmonized: on a map, green must stay green whatever the theme colour is.
        // Each hue gets its own tone, because yellow only reads as yellow when it's bright;
        // at one shared tone the middle of the scale turns olive.
        SignalQuality.entries.associateWith { quality ->
            val hue = SignalHues.getValue(quality).toHct().hue
            // Chroma asks for the most the screen can show at that tone (HCT clamps to gamut).
            val (chroma, tone) = when (quality) {
                SignalQuality.Excellent -> 120.0 to if (dark) 68.0 else 56.0
                SignalQuality.Good -> 120.0 to if (dark) 70.0 else 60.0
                SignalQuality.Fair -> 120.0 to if (dark) 82.0 else 78.0
                SignalQuality.Weak -> 120.0 to if (dark) 66.0 else 64.0
                SignalQuality.DeadZone -> 120.0 to if (dark) 56.0 else 52.0
            }
            Hct.from(hue, chroma, tone).toColor()
        }
    }
}

/** Distinguishable series colors for graphs, toned for the light or dark theme. */
@Composable
@ReadOnlyComposable
fun graphSeriesColors(): List<Color> {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return if (dark) DarkGraphColors else LightGraphColors
}

// Toned, not harmonized: series must stay distinguishable from each other.
private val LightGraphColors by lazy { GraphHues.map { it.toHct().tone(50.0) } }
private val DarkGraphColors by lazy { GraphHues.map { it.toHct().tone(75.0) } }

private val GraphHues = listOf(
    Color(0xFF4285F4), Color(0xFF00A86B), Color(0xFFE8453C), Color(0xFFF2A600), Color(0xFF9334E6),
    Color(0xFF00ACC1), Color(0xFFE52592), Color(0xFFFA7B17), Color(0xFF3F51B5), Color(0xFF7CB342)
)
