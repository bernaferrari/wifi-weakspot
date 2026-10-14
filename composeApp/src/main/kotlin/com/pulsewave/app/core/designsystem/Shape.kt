@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ShapeDefaults

/**
 * Custom-surface radii mapped onto the Material 3 Expressive shape scale.
 * Component chrome (buttons, sheets, navigation) uses [androidx.compose.material3.Shapes]
 * defaults from the theme so press and selection morphs stay on-spec.
 *
 * - extraSmall: badges and inline indicators (8.dp)
 * - small: compact icon wells (12.dp)
 * - medium: fields and secondary cards (16.dp)
 * - large: grouped surfaces (28.dp)
 * - extraLarge: standard cards (32.dp)
 * - extraExtraLarge: hero surfaces (48.dp)
 * - full: pills, status chips, avatars
 */
object PulseWaveShapes {
    val extraSmall = ShapeDefaults.Small
    val small = ShapeDefaults.Medium
    val medium = ShapeDefaults.Large
    val large = ShapeDefaults.ExtraLarge
    val extraLarge = ShapeDefaults.ExtraLargeIncreased
    val extraExtraLarge = ShapeDefaults.ExtraExtraLarge
    val full = RoundedCornerShape(50)
}

/**
 * Standard alpha values for consistent transparency across the app.
 *
 * - disabled: disabled states
 * - subtle: watermarks and quiet fills
 * - light: light overlays
 * - medium: secondary text and scrims
 * - strong: prominent translucent elements
 * - emphasized: near-opaque emphasis
 */
object PulseWaveAlpha {
    const val disabled = 0.38f
    const val subtle = 0.1f
    const val light = 0.2f
    const val medium = 0.5f
    const val strong = 0.7f
    const val emphasized = 0.87f
}
