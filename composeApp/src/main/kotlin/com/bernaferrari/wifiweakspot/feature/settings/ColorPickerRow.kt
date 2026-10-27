package com.bernaferrari.wifiweakspot.feature.settings

import com.bernaferrari.wifiweakspot.core.i18n.SettingsStrings
import com.bernaferrari.wifiweakspot.core.i18n.strings

import androidx.compose.ui.graphics.luminance
import androidx.compose.material.icons.rounded.Check
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Row
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.rememberWifiWeakspotHapticFeedback
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** A color choice for the app: the wallpaper's colors, or a seed the theme is built from. */
data class PaletteOption(val name: String, val seed: Long?, val preview: List<Color>)

/** Seed colors ordered warm to cool; the first is the app's own. */
val SeedPalettes: List<Pair<(SettingsStrings) -> String, Long>> = listOf(
    SettingsStrings::colorIndigo to 0xFF3F51B5,
    SettingsStrings::colorOrange to 0xFFFF5722,
    SettingsStrings::colorAmber to 0xFFFFC107,
    SettingsStrings::colorGreen to 0xFF4CAF50,
    SettingsStrings::colorTeal to 0xFF009688,
    SettingsStrings::colorCyan to 0xFF00BCD4,
    SettingsStrings::colorBlue to 0xFF2196F3,
    SettingsStrings::colorPurple to 0xFF9C27B0,
    SettingsStrings::colorPink to 0xFFE91E63
)

/**
 * The app's palettes as wrapping rows of swatches; each swatch names itself for screen readers. [wallpaper] is
 * null where the platform has no wallpaper colors, and then that option isn't offered.
 */
@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun ColorPickerRow(
    useWallpaper: Boolean,
    currentSeed: Int,
    wallpaper: List<Color>?,
    onWallpaper: () -> Unit,
    onSeed: (Int) -> Unit
) {
    val wallpaperChosen = useWallpaper && wallpaper != null
    Column(
        Modifier.fillMaxWidth().selectableGroup(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        androidx.compose.foundation.layout.FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Wallpaper colors come first, marked with a sparkle.
            if (wallpaper != null) PaletteSwatch(
                colors = wallpaper,
                selected = wallpaperChosen,
                label = strings.settings.wallpaperColors,
                icon = Icons.Rounded.AutoAwesome,
                onClick = onWallpaper
            )
            SeedPalettes.forEach { (name, seed) ->
                PaletteSwatch(
                    colors = listOf(Color(seed), lerp(Color(seed), Color.White, 0.45f)),
                    selected = !wallpaperChosen && seed.toInt() == currentSeed,
                    label = name(strings.settings),
                    onClick = { onSeed(seed.toInt()) }
                )
            }
        }
    }
}

/**
 * A gradient swatch with expressive motion: unselected it is a disc turned 45°; chosen, it springs
 * upright into a rounded square and gains a ring. The gradient counter-rotates, so the colors hold
 * still while the shape turns, and the change of shape (not only color) shows the selection.
 */
@Composable
fun PaletteSwatch(
    colors: List<Color>,
    selected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    icon: ImageVector? = null
) {
    val haptics = rememberWifiWeakspotHapticFeedback()
    val interactions = remember { MutableInteractionSource() }
    val focused by interactions.collectIsFocusedAsState()
    val bouncy = spring<Dp>(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
    val corner by animateDpAsState(if (selected) size * 0.3f else size / 2, bouncy, label = "corner")
    // The bounce overshoots; widths never go below zero.
    val ring = animateDpAsState(if (selected) 3.dp else 0.dp, bouncy, label = "ring").value.coerceAtLeast(0.dp)
    val rotation by animateFloatAsState(
        if (selected) 0f else 45f,
        spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "rotation"
    )
    val ringColor by animateColorAsState(if (selected) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent, label = "ringColor")
    val stops = if (colors.size == 1) colors + colors else colors
    val angle = -rotation * PI.toFloat() / 180f
    val brush = object : ShaderBrush() {
        override fun createShader(size: Size): Shader {
            val half = Offset(size.width * cos(angle) - size.height * sin(angle), size.width * sin(angle) + size.height * cos(angle)) / 2f
            val center = Offset(size.width / 2, size.height / 2)
            return LinearGradientShader(from = center - half, to = center + half, colors = stops)
        }
    }
    val outer = RoundedCornerShape(corner.coerceAtLeast(0.dp))
    val inner = RoundedCornerShape((corner - ring).coerceAtLeast(0.dp))
    val core = RoundedCornerShape((corner - ring - 2.dp).coerceAtLeast(0.dp))
    Box(
        modifier
            .size(size)
            .semantics { contentDescription = label }
            .selectable(selected = selected, role = Role.RadioButton, interactionSource = interactions, indication = null) {
                if (!selected) {
                    haptics.click()
                    onClick()
                }
            }
            .rotate(rotation)
            .clip(outer)
            .background(brush)
            .then(if (focused) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, outer) else Modifier)
            .padding(ring)
            .clip(inner)
            .background(ringColor)
            .padding(if (selected) 2.dp else 0.dp)
            .clip(core)
            .background(brush),
        contentAlignment = Alignment.Center
    ) {
        // A sparkle on the wallpaper swatch: "colours picked for you", in a deep or pale shade of the
        // swatch's own hue, so it stands out without the muddy grey of black over pastel.
        if (icon != null) {
            val base = colors.first()
            Icon(
                icon, null, Modifier.size(size * 0.42f).rotate(-rotation),
                tint = if (base.luminance() > 0.4f) lerp(base, Color.Black, 0.7f) else lerp(base, Color.White, 0.9f)
            )
        }
    }
}
