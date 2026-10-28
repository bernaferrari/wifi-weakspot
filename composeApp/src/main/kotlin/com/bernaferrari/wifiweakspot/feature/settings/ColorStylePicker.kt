@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.ColorStyle
import com.bernaferrari.wifiweakspot.core.i18n.strings
import com.materialkolor.material3.rememberDynamicMaterialThemeState

/**
 * The color style as four miniature Home cards, each painted by that style from the chosen seed: the
 * scalloped signal badge, a tonal chip and an accent chip, on the style's own surface. No names; the
 * chosen card lifts, rounds out and wears a ring with a check badge. Names stay for screen readers.
 */
@Composable
internal fun ColorStylePicker(
    selected: ColorStyle,
    seedColor: Color,
    dark: Boolean,
    onSelect: (ColorStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = strings.settings
    Row(
        modifier.selectableGroup().semantics { contentDescription = s.colorStyle },
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ColorStyle.entries.forEach { style ->
            val preview = rememberDynamicMaterialThemeState(seedColor = seedColor, isDark = dark, style = style.palette).colorScheme
            StylePreview(
                label = style.label(s),
                colors = preview,
                selected = style == selected,
                onClick = { onSelect(style) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private typealias Color = androidx.compose.ui.graphics.Color

@Composable
private fun StylePreview(label: String, colors: ColorScheme, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val ring = MaterialTheme.colorScheme.primary
    val spatial = MaterialTheme.motionScheme.fastSpatialSpec<androidx.compose.ui.unit.Dp>()
    val corner by animateDpAsState(if (selected) 28.dp else 18.dp, spatial, label = "styleCorner")
    val scale by animateFloatAsState(if (selected) 1f else 0.94f, MaterialTheme.motionScheme.fastSpatialSpec(), label = "styleScale")
    Box(
        modifier
            .aspectRatio(0.82f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .semantics { contentDescription = label }
            .selectable(selected = selected, role = Role.RadioButton, onClick = { if (!selected) onClick() })
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .then(if (selected) Modifier.border(3.dp, ring, RoundedCornerShape(corner)) else Modifier)
                .padding(if (selected) 5.dp else 0.dp)
                .clip(RoundedCornerShape((corner.value - 5f).coerceAtLeast(12f).dp))
                .background(colors.surfaceContainerHigh)
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically)
        ) {
            // The app's own scalloped badge, in this style's primary.
            Box(Modifier.fillMaxWidth(0.62f).aspectRatio(1f).clip(MaterialShapes.Cookie9Sided.toShape()).background(colors.primary))
            Box(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(colors.secondaryContainer))
            Box(Modifier.fillMaxWidth(0.6f).height(10.dp).align(Alignment.Start).clip(CircleShape).background(colors.tertiaryContainer))
        }
        if (selected) {
            Box(
                Modifier.align(Alignment.TopEnd).offset(x = 4.dp, y = (-4).dp).size(24.dp)
                    .clip(MaterialShapes.Cookie6Sided.toShape()).background(ring),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(15.dp))
            }
        }
    }
}
