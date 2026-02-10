package com.pulsewave.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * PulseWave shape tokens for consistent corner radii across the app.
 * 
 * Usage:
 * - extraSmall (8.dp): Small chips, badges, inline indicators
 * - small (12.dp): Icon containers, small buttons, compact cards
 * - medium (16.dp): Input fields, secondary cards, tool buttons
 * - large (24.dp): Primary buttons, medium cards
 * - extraLarge (28.dp): Hero cards, bottom sheets, large surfaces
 * - full: Circular shapes (FABs, avatars, pills)
 */
object PulseWaveShapes {
    val extraSmall = RoundedCornerShape(8.dp)
    val small = RoundedCornerShape(12.dp)
    val medium = RoundedCornerShape(16.dp)
    val large = RoundedCornerShape(24.dp)
    val extraLarge = RoundedCornerShape(28.dp)
    val full = RoundedCornerShape(50)
}

/**
 * Standard alpha values for consistent transparency across the app.
 * 
 * Usage:
 * - disabled: Disabled states, inactive elements
 * - subtle: Very light backgrounds, watermarks
 * - light: Light overlays, secondary info
 * - medium: Semi-transparent overlays, secondary text
 * - strong: Prominent but not opaque elements
 * - emphasized: Near-opaque, emphasized elements
 */
object PulseWaveAlpha {
    const val disabled = 0.38f
    const val subtle = 0.1f
    const val light = 0.2f
    const val medium = 0.5f
    const val strong = 0.7f
    const val emphasized = 0.87f
}

/**
 * Material 3 Shapes configured with PulseWave tokens.
 */
val PulseWaveMaterialShapes = Shapes(
    extraSmall = PulseWaveShapes.extraSmall,
    small = PulseWaveShapes.small,
    medium = PulseWaveShapes.medium,
    large = PulseWaveShapes.large,
    extraLarge = PulseWaveShapes.extraLarge
)
