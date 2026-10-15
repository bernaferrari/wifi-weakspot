@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.NetworkWifi1Bar
import androidx.compose.material.icons.rounded.NetworkWifi2Bar
import androidx.compose.material.icons.rounded.NetworkWifi3Bar
import androidx.compose.material.icons.rounded.SignalWifi0Bar
import androidx.compose.material.icons.rounded.SignalWifi4Bar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.material3.toShape
import androidx.compose.runtime.getValue
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.drawText
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.wifi.SignalQuality

/** The status-bar Wi-Fi glyph for a level: more bars, better signal. */
fun SignalQuality.barsIcon(): ImageVector = when (this) {
    SignalQuality.Excellent -> Icons.Rounded.SignalWifi4Bar
    SignalQuality.Good -> Icons.Rounded.NetworkWifi3Bar
    SignalQuality.Fair -> Icons.Rounded.NetworkWifi2Bar
    SignalQuality.Weak -> Icons.Rounded.NetworkWifi1Bar
    SignalQuality.DeadZone -> Icons.Rounded.SignalWifi0Bar
}

/**
 * The battery idea in the app's own signal language: the quality shape (more lobes, better
 * signal) fills from the bottom with a 0–100 score, and the score sits inside in heavy type.
 * Where the fill passes under the digits, they flip color, so they always read.
 */
@Composable
fun WifiBadge(rssi: Int, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 48.dp) {
    val quality = SignalQuality.of(rssi)
    val colors = signalColors(quality)
    val score = SignalQuality.score(rssi)
    // The fill always shows the score; the digits follow the person's choice in Settings.
    val showDbm = com.pulsewave.app.core.designsystem.LocalShowDbm.current
    val label = if (showDbm) "$rssi" else "$score"
    val level by androidx.compose.animation.core.animateFloatAsState(
        (score / 100f).coerceIn(0.12f, 1f), MaterialTheme.motionScheme.defaultSpatialSpec(), label = "signalLevel"
    )
    val measurer = androidx.compose.ui.text.rememberTextMeasurer()
    // Digits scale with the badge, so the large version in the details reads as boldly as the row.
    val style = MaterialTheme.typography.titleMedium.copy(
        fontSize = (size.value * 0.36f).sp, fontWeight = FontWeight.Black, fontFeatureSettings = "tnum", letterSpacing = (-0.5).sp
    )
    // Like Apple's battery: fill and track are two strengths of one color, and the digits are the
    // background color "cut out" of both, so contrast stays the same at every level.
    val surface = MaterialTheme.colorScheme.surface
    val track = androidx.compose.ui.graphics.lerp(colors.accent, surface, 0.35f)
    val polygon = quality.polygon()
    val shape = polygon.toShape()
    androidx.compose.foundation.Canvas(
        modifier
            .size(size)
            .clearAndSetSemantics { contentDescription = "${quality.label} signal, $score out of 100, $rssi dBm" }
    ) {
        val outline = when (val o = shape.createOutline(this.size, layoutDirection, this)) {
            is androidx.compose.ui.graphics.Outline.Generic -> o.path
            else -> Path().apply { addRect(Rect(Offset.Zero, this@Canvas.size)) }
        }
        val line = this.size.height * (1f - level)
        clipPath(outline) {
            drawRect(track)
            drawRect(colors.accent, topLeft = Offset(0f, line), size = Size(this.size.width, this.size.height - line))
        }
        val text = measurer.measure(label, if (showDbm) style.copy(fontSize = style.fontSize * 0.8f) else style)
        val topLeft = Offset((this.size.width - text.size.width) / 2f, (this.size.height - text.size.height) / 2f)
        drawText(text, color = surface, topLeft = topLeft)
    }
}
