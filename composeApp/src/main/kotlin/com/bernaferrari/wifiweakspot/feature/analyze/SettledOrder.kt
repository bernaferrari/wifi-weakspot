package com.bernaferrari.wifiweakspot.feature.analyze

/**
 * Orders a live list without making it jump under the finger. Items keep their previous places
 * and only pass a neighbour when clearly stronger (by [margin] or more), so a 1–2 dB wobble
 * between scans never reshuffles rows. Items with a higher [rank] lead. The same signal rules
 * apply within each rank.
 */
internal fun <T> settledOrder(
    previous: List<String>,
    items: List<T>,
    id: (T) -> String,
    strength: (T) -> Int,
    rank: (T) -> Int = { 0 },
    margin: Int = 6
): List<T> {
    val byId = items.associateBy(id)
    val known = previous.distinct().mapNotNull { byId[it] }
    val seen = known.map(id).toSet()
    // Newcomers start in signal order behind the rows already on screen, then bubble up if strong.
    // They come from byId, so an id that arrives twice still makes one row.
    val list = (known + byId.values.filter { id(it) !in seen }.sortedByDescending(strength)).toMutableList()
    var changed = true
    while (changed) {
        changed = false
        for (i in 0 until list.size - 1) {
            val a = list[i]
            val b = list[i + 1]
            val swap = rank(b) > rank(a) ||
                (rank(a) == rank(b) && strength(b) - strength(a) >= margin)
            if (swap) {
                list[i] = b
                list[i + 1] = a
                changed = true
            }
        }
    }
    return list
}
