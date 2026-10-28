@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.settings

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.remember
import androidx.graphics.shapes.Morph
import com.bernaferrari.wifiweakspot.core.designsystem.component.MorphShape
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
 * The color style as four miniature app screens, each painted by that style from the chosen seed: the
 * signal badge and the channel waves. No labels. Every badge is
 * an outlined circle until chosen, when it fills into the app's scalloped cookie with a check, and the
 * screen lifts inside a ring. Style names stay for screen readers only.
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
        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
    ) {
        ColorStyle.entries.forEach { style ->
            val preview = rememberDynamicMaterialThemeState(seedColor = seedColor, isDark = dark, style = style.palette).colorScheme
            StylePreview(
                label = style.label(s),
                colors = preview,
                selected = style == selected,
                onClick = { onSelect(style) },
                modifier = Modifier.weight(1f, fill = false).widthIn(max = 96.dp)
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
            .aspectRatio(1f)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .semantics { contentDescription = label }
            // Clip first so the press and hover highlight follows the card's rounded shape.
            .clip(RoundedCornerShape(corner))
            .selectable(selected = selected, role = Role.RadioButton, onClick = { if (!selected) onClick() })
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .then(if (selected) Modifier.border(3.dp, ring, RoundedCornerShape(corner)) else Modifier)
                .padding(if (selected) 5.dp else 0.dp)
                .clip(RoundedCornerShape((corner.value - 5f).coerceAtLeast(12f).dp))
                .background(colors.surfaceContainerHigh)
                .padding(top = 10.dp)
        ) {
            // A miniature of the app in this style: the signal badge, and channel waves below it.
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Outlined circle until chosen; the chosen one fills into the app's scalloped cookie with a check.
                val shape by animateFloatAsState(if (selected) 1f else 0f, MaterialTheme.motionScheme.slowSpatialSpec(), label = "badgeMorph")
                val morph = remember { Morph(MaterialShapes.Circle, MaterialShapes.Cookie9Sided) }
                Box(
                    Modifier.fillMaxWidth(0.46f).aspectRatio(1f)
                        .border(2.5.dp, colors.primary.copy(alpha = 1f - shape), MorphShape(morph, shape))
                        .clip(MorphShape(morph, shape)).background(colors.primary.copy(alpha = shape)),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) Icon(Icons.Rounded.Check, contentDescription = null, tint = colors.onPrimary, modifier = Modifier.fillMaxSize(0.5f))
                }
                Spacer(Modifier.weight(1f))
                // Overlapping channel waves in the style's three accents, like the Networks chart.
                Canvas(Modifier.fillMaxWidth().fillMaxHeight(0.42f)) {
                    listOf(Triple(.3f, .95f, colors.tertiary), Triple(.7f, .75f, colors.secondary), Triple(.5f, 1f, colors.primary)).forEach { (cx, h, c) ->
                        val half = size.width * .36f
                        val top = size.height * (1f - h)
                        val path = androidx.compose.ui.graphics.Path().apply {
                            moveTo(size.width * cx - half, size.height)
                            cubicTo(size.width * cx - half * .45f, size.height, size.width * cx - half * .5f, top, size.width * cx, top)
                            cubicTo(size.width * cx + half * .5f, top, size.width * cx + half * .45f, size.height, size.width * cx + half, size.height)
                            close()
                        }
                        drawPath(path, c.copy(alpha = .35f))
                        drawPath(path, c, style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx()))
                    }
                }
            }
        }
    }
}
