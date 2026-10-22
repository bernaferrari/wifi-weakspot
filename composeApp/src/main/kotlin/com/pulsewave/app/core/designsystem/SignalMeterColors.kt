package com.pulsewave.app.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

internal fun contrastRatio(first: Color, second: Color): Float {
    val a = first.luminance()
    val b = second.luminance()
    return (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
}

/** One surface-colored ink, with readable filled and empty parts of the meter. */
internal fun signalMeterColors(signal: SignalColors, surface: Color): SignalColors {
    val strongest = if (contrastRatio(surface, Color.Black) >= contrastRatio(surface, Color.White)) Color.Black else Color.White
    fun strengthen(candidate: Color, toward: Color, minimum: Float): Color {
        for (step in 0..20) {
            val color = lerp(candidate, toward, step / 20f)
            if (contrastRatio(surface, color) >= minimum) return color
        }
        return strongest
    }
    // Keep the signal hue; only move farther along its existing tonal roles when needed.
    val fill = strengthen(signal.accent, signal.onContainer, 7f)
    val track = strengthen(lerp(fill, surface, 0.35f), fill, 4.5f)
    return SignalColors(accent = fill, container = track, onContainer = surface)
}
