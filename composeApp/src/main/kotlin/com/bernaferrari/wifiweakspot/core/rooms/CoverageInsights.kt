package com.bernaferrari.wifiweakspot.core.rooms

import com.bernaferrari.wifiweakspot.core.i18n.appStrings
import com.bernaferrari.wifiweakspot.core.wifi.bandLabel

data class CoverageSummary(
    /** Weakest first. */
    val rankedRooms: List<RoomReading>,
    val weakRooms: List<RoomReading>,
    val best: RoomReading?,
    val worst: RoomReading?,
    val accessPointCount: Int,
    val tips: List<String>,
    val history: List<RoomReading> = emptyList()
) {
    /** A summary of what was checked, never a claim about where the phone is right now. */
    val headline: String get() = when {
        rankedRooms.isEmpty() -> appStrings.domain.coverageNoRooms
        weakRooms.size == 1 -> appStrings.domain.coverageWeakInRoom(weakRooms.first().name)
        weakRooms.size > 1 -> appStrings.domain.coverageWeakInRooms(weakRooms.size)
        else -> appStrings.domain.coverageRoomsCheckedNoneWeak(rankedRooms.size)
    }
}

fun coverageSummary(readings: List<RoomReading>): CoverageSummary {
    val ranked = readings.sortedByDescending { it.timestamp }.distinctBy { it.spotKey }
        .sortedWith(compareBy({ it.averageRssi }, { it.name }))
    val weak = ranked.filter { it.quality.isProblem }
    val best = ranked.lastOrNull()
    val worst = ranked.firstOrNull()
    val accessPoints = ranked.map { it.bssid.lowercase() }.filter { it.isNotBlank() && it != "02:00:00:00:00:00" }.toSet().size
    val tips = buildList {
        val highBandWeak = weak.firstOrNull { it.frequencyMhz >= 5000 }
        if (highBandWeak != null) {
            add(appStrings.domain.tipHighBandWeak(highBandWeak.name, bandLabel(highBandWeak.frequencyMhz)))
        }
        if (best != null && worst != null && best !== worst && best.averageRssi - worst.averageRssi >= 20 && weak.isNotEmpty()) {
            add(appStrings.domain.tipSignalDrop(best.averageRssi - worst.averageRssi, best.name, worst.name))
        }
        if (accessPoints > 1) {
            add(appStrings.domain.tipMultipleAccessPoints(accessPoints))
        }
        if (weak.isNotEmpty()) {
            add(appStrings.domain.tipRouterPlacement)
        }
    }
    return CoverageSummary(ranked, weak, best, worst, accessPoints, tips, readings)
}

/**
 * Ready-made names so checking a room is two taps. In the current language; a picked name is
 * saved as typed, so rooms saved in another language keep their original name.
 */
val RoomSuggestions: List<String> get() = appStrings.domain.roomSuggestions

/** Changes in signal are meaningful only with their measurement context visible. */
fun spotComparison(current: RoomReading, previous: RoomReading?): String {
    if (previous == null) return appStrings.domain.spotFirstReading
    if (current.spotKey != previous.spotKey) return appStrings.domain.spotDifferentNetwork
    return appStrings.domain.spotComparison(
        deltaDb = current.averageRssi - previous.averageRssi,
        accessPointChanged = current.bssid != previous.bssid,
        bandChanged = bandLabel(current.frequencyMhz) != bandLabel(previous.frequencyMhz)
    )
}
