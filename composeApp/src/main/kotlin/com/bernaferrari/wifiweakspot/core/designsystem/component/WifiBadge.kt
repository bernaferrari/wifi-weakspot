@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.core.designsystem.component

import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.wifiweakspot.core.designsystem.signalColors
import com.bernaferrari.wifiweakspot.core.designsystem.signalMeterColors
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import com.bernaferrari.wifiweakspot.core.wifi.SignalDisplay

/** Signal level inside a quality shape: percentage by default, bars or dBm by preference. */
@Composable
fun WifiBadge(rssi: Int, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 48.dp) {
    val quality = SignalQuality.of(rssi)
    val signal = signalColors(quality)
    val display = com.bernaferrari.wifiweakspot.core.designsystem.LocalSignalDisplay.current
    val surface = MaterialTheme.colorScheme.surface
    val colors = androidx.compose.runtime.remember(signal, display, surface) {
        if (display == SignalDisplay.Percentage) signalMeterColors(signal, surface) else signal
    }
    val badgeDescription = strings.common.signalBadgeDescription(quality.label, display.reading(rssi))
    androidx.compose.foundation.layout.Box(
        modifier
            .size(size)
            .clip(quality.polygon().toShape())
            .background(colors.container)
            .clearAndSetSemantics { contentDescription = badgeDescription },
        contentAlignment = Alignment.Center
    ) {
        if (display == SignalDisplay.Percentage) {
            PercentageFill(SignalQuality.percentage(rssi), colors, size)
        } else if (display == SignalDisplay.Dbm) {
            val unitSize = (size.value * 0.13f).coerceIn(8f, 18f).sp
            androidx.compose.foundation.layout.Column(
                Modifier.fillMaxWidth(0.7f), horizontalAlignment = Alignment.CenterHorizontally
            ) {
                androidx.compose.material3.Text(
                    "$rssi",
                    color = colors.onContainer,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = (size.value * 0.27f).sp,
                        lineHeight = (size.value * 0.32f).sp,
                        fontWeight = FontWeight.Bold,
                        fontFeatureSettings = "tnum"
                    ),
                    maxLines = 1,
                    autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(
                        minFontSize = 8.sp, maxFontSize = (size.value * 0.27f).sp
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                androidx.compose.material3.Text(
                    "dBm", color = colors.onContainer,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = unitSize, lineHeight = unitSize * 1.15f, letterSpacing = 0.sp),
                    maxLines = 1
                )
            }
        } else {
            WifiSignalIcon(quality, colors.onContainer, Modifier.size(size * 0.52f))
        }
    }
}

/** Number and fill share the same percentage; one surface-colored label spans both tones. */
@Composable
private fun androidx.compose.foundation.layout.BoxScope.PercentageFill(
    percentage: Int,
    colors: com.bernaferrari.wifiweakspot.core.designsystem.SignalColors,
    badgeSize: androidx.compose.ui.unit.Dp
) {
    val level by androidx.compose.animation.core.animateFloatAsState(
        percentage / 100f,
        MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "signalPercentageFill"
    )
    androidx.compose.foundation.Canvas(Modifier.matchParentSize()) {
        val top = size.height * (1f - level.coerceIn(0f, 1f))
        drawRect(colors.accent, topLeft = androidx.compose.ui.geometry.Offset(0f, top), size = androidx.compose.ui.geometry.Size(size.width, size.height - top))
    }
    androidx.compose.foundation.layout.Box(Modifier.matchParentSize(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.Text(
            "$percentage%",
            color = colors.onContainer,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = (badgeSize.value * 0.28f).sp,
                lineHeight = (badgeSize.value * 0.36f).sp,
                fontWeight = FontWeight.Bold,
                fontFeatureSettings = "tnum",
                letterSpacing = 0.sp
            ),
            maxLines = 1,
            autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(
                minFontSize = 8.sp, maxFontSize = (badgeSize.value * 0.28f).sp
            ),
            modifier = Modifier.fillMaxWidth(0.74f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/** The same silhouette at every level: dot, then inner-to-outer arcs brighten with signal. */
@Composable
private fun WifiSignalIcon(quality: SignalQuality, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    androidx.compose.foundation.Canvas(modifier) {
        val unit = size.minDimension / 24f
        val center = androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f + 7.5f * unit)
        val activeArcs = (quality.bars - 1).coerceAtLeast(0)
        listOf(5.4f, 10.3f, 15.2f).forEachIndexed { index, radius ->
            val r = radius * unit
            drawArc(
                color.copy(alpha = color.alpha * if (index < activeArcs) 1f else 0.22f),
                startAngle = 225f, sweepAngle = 90f, useCenter = false,
                topLeft = center - androidx.compose.ui.geometry.Offset(r, r),
                size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
                style = androidx.compose.ui.graphics.drawscope.Stroke(2.6f * unit, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        drawCircle(color.copy(alpha = color.alpha * if (quality.bars > 0) 1f else 0.22f), 1.55f * unit, center)
    }
}
