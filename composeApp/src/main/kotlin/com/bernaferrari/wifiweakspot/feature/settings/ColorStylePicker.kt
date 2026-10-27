@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.ColorStyle
import com.bernaferrari.wifiweakspot.core.i18n.strings
import com.materialkolor.material3.rememberDynamicMaterialThemeState
import kotlin.math.roundToInt

/** One row of abstract theme previews; names remain available to accessibility services. */
@Composable
internal fun ColorStylePicker(
    selected: ColorStyle,
    seedColor: Color,
    dark: Boolean,
    onSelect: (ColorStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val s = strings.settings
    val density = LocalDensity.current
    BoxWithConstraints(modifier) {
        val width = (((maxWidth - 24.dp) / 4) * 0.82f).coerceIn(48.dp, 78.dp)
        val initialScroll = with(density) { ((width + 8.dp).toPx() * selected.ordinal).roundToInt() }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState(initialScroll)).selectableGroup()
                .semantics { contentDescription = s.colorStyle },
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            ColorStyle.entries.forEach { style ->
                val preview = rememberDynamicMaterialThemeState(seedColor = seedColor, isDark = dark, style = style.palette).colorScheme
                StyleChoice(
                    label = style.label(s), selected = style == selected, colors = preview,
                    onClick = { onSelect(style) }, modifier = Modifier.width(width)
                )
            }
        }
    }
}

/** Owns selection, touch feedback, and accessibility; the illustration stays decorative. */
@Composable
private fun StyleChoice(label: String, selected: Boolean, colors: ColorScheme, onClick: () -> Unit, modifier: Modifier) {
    val scheme = MaterialTheme.colorScheme
    Box(
        modifier.heightIn(min = 48.dp).semantics { contentDescription = label }
            .selectable(selected = selected, role = Role.RadioButton, onClick = { if (!selected) onClick() }),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            Modifier.fillMaxWidth().aspectRatio(1.25f),
            shape = RoundedCornerShape(14.dp), color = colors.surface,
            border = BorderStroke(1.dp, if (selected) scheme.outlineVariant else scheme.outlineVariant.copy(alpha = 0.35f))
        ) {
            Box(Modifier.padding(6.dp).clearAndSetSemantics { }) {
                ThemeStylePreview(colors, selected, Modifier.fillMaxSize().clip(RoundedCornerShape(8.dp)))
            }
        }
    }
}

/** Theme roles as a primary action and two tonal surfaces, not a chart recoloring promise. */
@Composable
private fun ThemeStylePreview(colors: ColorScheme, selected: Boolean, modifier: Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        drawRect(colors.surface)
        fun tile(x: Float, y: Float, width: Float, height: Float, radius: Float, color: Color) {
            drawRoundRect(color, Offset(x * w, y * h), Size(width * w, height * h), CornerRadius(radius * w))
        }
        // A filled action, a quiet content card, and an accent control share the
        // actual generated scheme. No network identity or signal status colors.
        tile(.06f, .12f, .88f, .25f, .125f, colors.primary)
        tile(.06f, .45f, .51f, .43f, .12f, colors.secondaryContainer)
        tile(.65f, .45f, .29f, .43f, .12f, colors.tertiaryContainer)
        tile(.17f, .58f, .27f, .035f, .018f, colors.onSecondaryContainer.copy(alpha = .7f))
        tile(.17f, .69f, .19f, .035f, .018f, colors.onSecondaryContainer.copy(alpha = .35f))
        val center = Offset(w * .795f, h * .665f)
        val radius = w * .115f
        drawCircle(colors.tertiary, radius, center)
        if (selected) {
            val check = Path().apply {
                moveTo(center.x - radius * .48f, center.y)
                lineTo(center.x - radius * .12f, center.y + radius * .34f)
                lineTo(center.x + radius * .52f, center.y - radius * .35f)
            }
            drawPath(check, colors.onTertiary,
                style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}
