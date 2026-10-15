package com.pulsewave.app.core.designsystem

import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lets a horizontally scrolling row reach past its parent's side padding to the screen edges,
 * so chips slide off the edge instead of being cut short inside the margin. Pair it with the
 * same [margin] as inner padding so the first item still lines up with the content above.
 */
fun Modifier.bleed(margin: Dp = 16.dp): Modifier = layout { measurable, constraints ->
    val extra = margin.roundToPx() * 2
    val placeable = measurable.measure(constraints.copy(minWidth = constraints.minWidth + extra, maxWidth = constraints.maxWidth + extra))
    layout(placeable.width - extra, placeable.height) { placeable.place(-extra / 2, 0) }
}
