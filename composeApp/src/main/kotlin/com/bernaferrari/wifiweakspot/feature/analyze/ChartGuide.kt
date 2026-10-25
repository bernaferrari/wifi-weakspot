@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.bernaferrari.wifiweakspot.feature.analyze

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings
import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.component.segmentedShape
import com.bernaferrari.wifiweakspot.core.designsystem.graphSeriesColors
import kotlin.math.abs
import kotlin.math.pow

private class GuideItem(val body: AnalyzeStrings.() -> String, val draw: DrawScope.(List<Color>, Color) -> Unit)

private fun DrawScope.hill(left: Float, right: Float, height: Float, color: Color, fill: Boolean = true) {
    val base = size.height - 2.dp.toPx()
    val path = Path().apply {
        moveTo(left, base)
        for (i in 0..24) {
            val t = i / 24f
            lineTo(left + (right - left) * t, base - height * (1f - abs(2 * t - 1).toDouble().pow(3.0).toFloat()))
        }
        lineTo(right, base)
    }
    if (fill) drawPath(path, color.copy(alpha = 0.25f))
    drawPath(path, color, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
}

private fun DrawScope.baseline(color: Color) =
    drawLine(color, Offset(0f, size.height - 1.dp.toPx()), Offset(size.width, size.height - 1.dp.toPx()), 1.dp.toPx())

private val Items = listOf(
    GuideItem({ guideTallerBody }) { c, line ->
        baseline(line); hill(4.dp.toPx(), size.width * 0.45f, size.height * 0.85f, c[0]); hill(size.width * 0.55f, size.width - 4.dp.toPx(), size.height * 0.4f, c[1])
    },
    GuideItem({ guideOverlapBody }) { c, line ->
        baseline(line); hill(4.dp.toPx(), size.width * 0.65f, size.height * 0.7f, c[0]); hill(size.width * 0.35f, size.width - 4.dp.toPx(), size.height * 0.6f, c[2])
    },
    GuideItem({ guideWiderBody }) { c, line ->
        baseline(line); hill(size.width * 0.05f, size.width * 0.3f, size.height * 0.7f, c[1]); hill(size.width * 0.38f, size.width * 0.97f, size.height * 0.7f, c[3])
    },
    GuideItem({ guideSameRouterBody }) { c, line ->
        baseline(line); hill(size.width * 0.15f, size.width * 0.85f, size.height * 0.55f, c[0])
        drawRoundRect(c[0].copy(alpha = 0.3f), Offset(size.width * 0.3f, 0f), Size(size.width * 0.4f, size.height * 0.32f), CornerRadius(6.dp.toPx()))
    },
    GuideItem({ guideCrowdBody }) { c, _ ->
        drawRoundRect(
            androidx.compose.ui.graphics.Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFFFFC107), Color(0xFFE53935), Color(0xFF4CAF50))),
            Offset(0f, size.height / 2 - 4.dp.toPx()), Size(size.width, 8.dp.toPx()), CornerRadius(4.dp.toPx())
        )
    },
    GuideItem({ guideSteadyBody }) { c, line ->
        baseline(line)
        drawLine(c[1], Offset(0f, size.height * 0.3f), Offset(size.width, size.height * 0.3f), 2.dp.toPx(), cap = StrokeCap.Round)
        val zig = Path().apply {
            moveTo(0f, size.height * 0.75f)
            listOf(0.15f to 0.55f, 0.3f to 0.85f, 0.5f to 0.5f, 0.7f to 0.9f, 0.85f to 0.6f, 1f to 0.8f).forEach { (x, y) -> lineTo(size.width * x, size.height * y) }
        }
        drawPath(zig, c[2], style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
    },
    GuideItem({ guideHighlightBody }) { c, line ->
        baseline(line); hill(4.dp.toPx(), size.width * 0.55f, size.height * 0.5f, line, fill = false); hill(size.width * 0.35f, size.width - 4.dp.toPx(), size.height * 0.85f, c[0])
    }
)

/** A short visual guide to the charts: one small drawing per idea, nothing to memorise. */
@Composable
fun ChartGuideSheet(onDismiss: () -> Unit) {
    val colors = graphSeriesColors()
    val line = MaterialTheme.colorScheme.outlineVariant
    val s = strings.analyze
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = androidx.compose.material3.rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 32.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            item {
                Text(s.chartGuideTitle, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(start = 8.dp, bottom = 16.dp))
            }
            itemsIndexed(Items) { index, item ->
                Surface(shape = segmentedShape(index, Items.size, outer = 24.dp, inner = 6.dp), color = MaterialTheme.colorScheme.surfaceContainerHigh, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                            Canvas(Modifier.padding(8.dp).size(width = 64.dp, height = 40.dp)) { item.draw(this, colors, line) }
                        }
                        Text(item.body(s), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
