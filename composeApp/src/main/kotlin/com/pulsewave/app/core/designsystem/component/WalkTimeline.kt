package com.pulsewave.app.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.signalColors
import com.pulsewave.app.core.rooms.WalkSample
import com.pulsewave.app.core.wifi.SignalQuality

/** One rounded bar per second, colored and sized by signal, newest on the right. */
@Composable
fun WalkTimeline(samples: List<WalkSample>, modifier: Modifier = Modifier, maxBars: Int = 60, fullSession: Boolean = false) {
    val palette = SignalQuality.entries.associateWith { signalColors(it).accent }
    val disconnected = MaterialTheme.colorScheme.error
    val empty = MaterialTheme.colorScheme.surfaceContainerHighest
    Canvas(modifier) {
        val gap = 3.dp.toPx()
        val barWidth = (size.width - gap * (maxBars - 1)) / maxBars
        val visible = if (!fullSession || samples.size <= maxBars) samples.takeLast(maxBars) else
            samples.chunked((samples.size + maxBars - 1) / maxBars).map { interval ->
                interval.firstOrNull { !it.connected }
                    ?: interval.firstOrNull { it.measuredRssi == null }
                    ?: interval.minBy { it.measuredRssi!! }
            }
        repeat(maxBars) { index ->
            val sample = visible.getOrNull(index - (maxBars - visible.size))
            val x = index * (barWidth + gap)
            val fraction = sample?.measuredRssi?.let { SignalQuality.fraction(it).coerceAtLeast(0.08f) } ?: 0.08f
            val height = size.height * fraction
            drawRoundRect(
                color = when {
                    sample != null && !sample.connected -> disconnected
                    sample?.measuredRssi != null -> palette.getValue(SignalQuality.of(sample.measuredRssi!!))
                    else -> empty
                },
                topLeft = Offset(x, size.height - height),
                size = Size(barWidth, height),
                cornerRadius = CornerRadius(barWidth / 2)
            )
        }
    }
}

