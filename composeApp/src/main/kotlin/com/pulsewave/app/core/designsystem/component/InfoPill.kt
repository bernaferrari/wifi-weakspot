package com.pulsewave.app.core.designsystem.component

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.PulseWaveShapes

/** Compact fact such as "5 GHz" or "Up to 433 Mbps". */
@Composable
fun InfoPill(text: String, modifier: Modifier = Modifier) {
    Surface(shape = PulseWaveShapes.full, color = MaterialTheme.colorScheme.surfaceContainerHighest, modifier = modifier) {
        Text(
            text,
            style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        )
    }
}
