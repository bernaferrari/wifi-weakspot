package com.pulsewave.app.core.designsystem.component

import com.pulsewave.app.core.designsystem.animateAccessibleFloat
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
fun LinearWavyProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 4.dp,
    amplitude: Dp = 4.dp,
    wavelength: Dp = 20.dp,
    progress: () -> Float // 0.0 to 1.0
) {
    val infiniteTransition = rememberInfiniteTransition()
    val phase by infiniteTransition.animateAccessibleFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(modifier = modifier.height(amplitude * 4)) {
        val width = size.width
        val midHeight = size.height / 2
        val waveLen = wavelength.toPx()
        val amp = amplitude.toPx()

        // Draw Track
        val progressVal = progress().coerceIn(0f, 1f)
        val activeWidth = width * progressVal

        // Function to Create Wave Path
        fun createWave(endX: Float, phaseOffset: Float): Path {
            val path = Path()
            path.moveTo(0f, midHeight + amp * sin(phaseOffset * 2 * PI.toFloat()))
            var x = 0f
            while (x <= endX) {
                val angle = (x / waveLen + phaseOffset) * 2 * PI
                val y = midHeight + amp * sin(angle).toFloat()
                path.lineTo(x, y)
                x += 2f // Step size
            }
            return path
        }

        // Draw Track
        drawPath(
            path = createWave(width, phase),
            color = trackColor,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )

        // Draw Progress
        if (progressVal > 0) {
             drawPath(
                path = createWave(activeWidth, phase),
                color = color,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}
