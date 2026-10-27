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

/** Shared scan frames preserve missing readings at their actual positions. */
internal fun historyValueAt(values: List<SignalObservation?>, index: Int, sampleCount: Int): Int? =
    if (index in 0 until sampleCount) values.getOrNull(index)?.rssi else null

/** Compare adjacent signals at one observation, skipping radios not yet observed then. */
internal fun historyNeighborBssid(
    history: SignalTimeline,
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
