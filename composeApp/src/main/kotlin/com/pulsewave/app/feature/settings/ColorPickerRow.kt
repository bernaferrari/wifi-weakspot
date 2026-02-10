package com.pulsewave.app.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ColorPickerRow(
    currentSeed: Int,
    onColorSelected: (Int) -> Unit
) {
    val colors = listOf(
        0xFF3F51B5, // Indigo (Default)
        0xFF2196F3, // Blue
        0xFF00BCD4, // Cyan
        0xFF009688, // Teal
        0xFF4CAF50, // Green
        0xFFFFEB3B, // Yellow
        0xFFFF9800, // Orange
        0xFFFF5722, // Deep Orange
        0xFFF44336, // Red
        0xFFE91E63, // Pink
        0xFF9C27B0, // Purple
        0xFF673AB7, // Deep Purple
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        items(colors) { colorLong ->
            val colorInt = colorLong.toInt()
            val color = Color(colorLong)
            val isSelected = currentSeed == colorInt

            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onColorSelected(colorInt) }
                    .then(
                        if (isSelected) {
                            Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White, // Assuming dark-ish colors, or check contrast. 
                        // Simplified for now.
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
