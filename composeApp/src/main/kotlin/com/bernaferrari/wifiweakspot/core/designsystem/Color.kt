package com.bernaferrari.wifiweakspot.core.designsystem

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.materialkolor.hct.Hct
import com.materialkolor.ktx.toColor
import com.materialkolor.ktx.toHct
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality

// App colors come from the dynamic or seeded scheme in WifiWeakspotTheme.
// Signal colors are the only fixed hues. They share Re-Rethink's status pair: its "allowed"
// green (seed #1E9E5A, hue ~156) and Material's error red (hue 25), with the middle verdicts
// stepping between them through a muted sand, so the scale reads green -> red without the
// loud yellow and orange of a traffic light. They are toned like Material roles (and not
// harmonized to the theme, so green stays green on any seed) to keep contrast in both themes.

@Immutable
data class SignalColors(
    /** Strong mark: meters, dots, graph strokes. */
    val accent: Color,
    val container: Color,
    val onContainer: Color
)

/**
 * One verdict's place on the green-to-red scale. [chroma] is for tonal roles (text, badges,
 * charts); [heatChroma] and the heat tones are for large map fields, which want more color.
 */
private class SignalTone(
    val hue: Double,
    val chroma: Double,
    val heatChroma: Double,
    val heatToneLight: Double,
    val heatToneDark: Double,
)

private val SignalTones = mapOf(
    // Re-Rethink's allowed green.
    SignalQuality.Excellent to SignalTone(hue = 156.0, chroma = 48.0, heatChroma = 120.0, heatToneLight = 66.0, heatToneDark = 64.0),
    // A fresh leaf green: still "fine", one step toward the middle.
    SignalQuality.Good to SignalTone(hue = 140.0, chroma = 44.0, heatChroma = 64.0, heatToneLight = 80.0, heatToneDark = 76.0),
    // A clear warm amber for the midpoint. Low-chroma sand turned olive brown at dark tones.
    SignalQuality.Fair to SignalTone(hue = 72.0, chroma = 46.0, heatChroma = 56.0, heatToneLight = 84.0, heatToneDark = 76.0),
    // A soft coral, one step short of the full red.
    SignalQuality.Weak to SignalTone(hue = 38.0, chroma = 50.0, heatChroma = 64.0, heatToneLight = 76.0, heatToneDark = 70.0),
    // Material's error red, as Re-Rethink uses for "blocked".
    SignalQuality.DeadZone to SignalTone(hue = 25.0, chroma = 84.0, heatChroma = 120.0, heatToneLight = 58.0, heatToneDark = 56.0),
)

private fun hct(hue: Double, chroma: Double, tone: Double): Color = Hct.from(hue, chroma, tone).toColor()

private fun Hct.tone(tone: Double): Color = Hct.from(hue, chroma, tone).toColor()

/**
 * Tonal roles for a verdict, matching how Material builds `error`/`errorContainer`: accent at
 * tone 40 (light) or 80 (dark), container at 90 or 30, and its text at 30 or 90 (about 7:1).
 */
internal fun signalColorsFor(quality: SignalQuality, dark: Boolean): SignalColors {
    val t = SignalTones.getValue(quality)
    return if (dark) {
        SignalColors(accent = hct(t.hue, t.chroma, 80.0), container = hct(t.hue, t.chroma, 30.0), onContainer = hct(t.hue, t.chroma, 90.0))
    } else {
        SignalColors(accent = hct(t.hue, t.chroma, 40.0), container = hct(t.hue, t.chroma, 90.0), onContainer = hct(t.hue, t.chroma, 30.0))
    }
}

/**
 * The percentage badge: a level meter inside the verdict shape. Light mode is tonal, like a Material chip:
 * a pale track, a soft fill up to the percentage, and dark text in the same hue. Dark mode mirrors it.
 */
internal fun signalBadgeColorsFor(quality: SignalQuality, dark: Boolean): SignalColors {
    val t = SignalTones.getValue(quality)
    return if (dark) {
        SignalColors(accent = hct(t.hue, t.chroma, 42.0), container = hct(t.hue, t.chroma * .8, 24.0), onContainer = hct(t.hue, t.chroma * .5, 94.0))
    } else {
        SignalColors(accent = hct(t.hue, t.chroma + 10, 82.0), container = hct(t.hue, t.chroma * .7, 94.0), onContainer = hct(t.hue, t.chroma, 22.0))
    }
}

private val LightBadgeColors by lazy { SignalQuality.entries.associateWith { signalBadgeColorsFor(it, dark = false) } }
private val DarkBadgeColors by lazy { SignalQuality.entries.associateWith { signalBadgeColorsFor(it, dark = true) } }

@Composable
@ReadOnlyComposable
fun signalBadgeColors(quality: SignalQuality): SignalColors {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return (if (dark) DarkBadgeColors else LightBadgeColors).getValue(quality)
}

private val LightSignalColors by lazy { SignalQuality.entries.associateWith { signalColorsFor(it, dark = false) } }
private val DarkSignalColors by lazy { SignalQuality.entries.associateWith { signalColorsFor(it, dark = true) } }

@Composable
@ReadOnlyComposable
fun signalColors(quality: SignalQuality): SignalColors {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return (if (dark) DarkSignalColors else LightSignalColors).getValue(quality)
}

@Composable
@ReadOnlyComposable
fun signalColors(rssi: Int): SignalColors = signalColors(SignalQuality.of(rssi))

/**
 * Vivid signal colors for large fields such as the floor-plan heatmap. Same hues as [signalColors],
 * but pushed to more chroma at a mid-to-high tone (HCT is perceptual, like OKLCH), so a whole map
 * reads as clear green-to-red instead of the pale tints that suit text and badges. The two ends
 * ask for the most the screen can show (HCT clamps to gamut); the middle stays soft on purpose.
 */
internal fun signalHeatColorsFor(dark: Boolean): Map<SignalQuality, Color> =
    SignalQuality.entries.associateWith { quality ->
        val t = SignalTones.getValue(quality)
        // Light mode keeps tones high so the map reads bright, not muddy, on a light surface.
        hct(t.hue, t.heatChroma, if (dark) t.heatToneDark else t.heatToneLight)
    }

private val LightHeatColors by lazy { signalHeatColorsFor(dark = false) }
private val DarkHeatColors by lazy { signalHeatColorsFor(dark = true) }

@Composable
@ReadOnlyComposable
fun signalHeatColors(): Map<SignalQuality, Color> =
    if (MaterialTheme.colorScheme.surface.luminance() < 0.5f) DarkHeatColors else LightHeatColors

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
