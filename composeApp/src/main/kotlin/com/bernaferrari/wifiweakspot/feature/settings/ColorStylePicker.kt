@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.ColorStyle
import com.bernaferrari.wifiweakspot.core.i18n.strings
import com.materialkolor.material3.rememberDynamicMaterialThemeState

/**
 * The color style as four swatches and nothing else, like Android's wallpaper-color picker: each one is
 * split into the colors that style really generates from the chosen seed. The selected swatch morphs from
 * a circle into a rounded square inside a ring. Names stay available to screen readers only.
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
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ColorStyle.entries.forEach { style ->
            val preview = rememberDynamicMaterialThemeState(seedColor = seedColor, isDark = dark, style = style.palette).colorScheme
            StyleSwatch(
                label = style.label(s),
                top = preview.primary,
                bottomStart = preview.secondaryContainer,
                bottomEnd = preview.tertiary,
                selected = style == selected,
                onClick = { onSelect(style) }
            )
        }
    }
}

@Composable
private fun StyleSwatch(label: String, top: Color, bottomStart: Color, bottomEnd: Color, selected: Boolean, onClick: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val spec = MaterialTheme.motionScheme.fastSpatialSpec<androidx.compose.ui.unit.Dp>()
    // Circle when idle; a rounded square when chosen (an Expressive shape change), with a ring around it.
    val corner by animateDpAsState(if (selected) 18.dp else 28.dp, spec, label = "swatchCorner")
    val ringCorner by animateDpAsState(if (selected) 24.dp else 34.dp, spec, label = "ringCorner")
    Box(
        Modifier
            .size(68.dp)
            .semantics { contentDescription = label }
            .selectable(selected = selected, role = Role.RadioButton, onClick = { if (!selected) onClick() })
            .then(if (selected) Modifier.border(3.dp, scheme.primary, RoundedCornerShape(ringCorner)) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size(56.dp).clip(RoundedCornerShape(corner))) {
            val half = size.height / 2f
            drawRect(top, size = Size(size.width, half))
            drawRect(bottomStart, topLeft = Offset(0f, half), size = Size(size.width / 2f, half))
            drawRect(bottomEnd, topLeft = Offset(size.width / 2f, half), size = Size(size.width / 2f, half))
        }
    }
}
