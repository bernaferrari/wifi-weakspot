package com.pulsewave.app.feature.analyze

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size

/** Place a peak label above its reading, within the available chart area. */
internal fun chartLabelRect(anchor: Offset, label: Size, bounds: Rect, gap: Float): Rect = Rect(
    Offset(
        (anchor.x - label.width / 2).coerceIn(bounds.left, (bounds.right - label.width).coerceAtLeast(bounds.left)),
        (anchor.y - label.height - gap).coerceIn(bounds.top, (bounds.bottom - label.height).coerceAtLeast(bounds.top))
    ),
    label
)

/** Histories are right-aligned: a radio discovered later has no earlier readings. */
internal fun historyValueAt(values: List<Int>, index: Int, sampleCount: Int): Int? =
    values.getOrNull(index - (sampleCount - values.size))
