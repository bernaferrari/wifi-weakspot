package com.bernaferrari.wifiweakspot.feature.analyze

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

/** Compare adjacent signals at one observation, skipping radios not yet observed then. */
internal fun historyNeighborBssid(
    history: Map<String, List<Int>>,
    bssids: List<String>,
    index: Int,
    sampleCount: Int,
    currentBssid: String?,
    direction: Int
): String? {
    val ordered = bssids.mapNotNull { bssid ->
        historyValueAt(history[bssid].orEmpty(), index, sampleCount)?.let { bssid to it }
    }.sortedWith(compareByDescending<Pair<String, Int>> { it.second }.thenBy { it.first })
    if (ordered.isEmpty()) return null
    val current = ordered.indexOfFirst { it.first == currentBssid }
    val next = if (current < 0) 0 else (current + direction.coerceIn(-1, 1)).coerceIn(ordered.indices)
    return ordered[next].first
}
