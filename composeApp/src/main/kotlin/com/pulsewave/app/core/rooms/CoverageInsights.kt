package com.pulsewave.app.core.rooms

import com.pulsewave.app.core.wifi.bandLabel

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
        rankedRooms.isEmpty() -> "No rooms checked yet"
        weakRooms.size == 1 -> "Weak Wi-Fi in ${weakRooms.first().name}"
        weakRooms.size > 1 -> "Weak Wi-Fi in ${weakRooms.size} rooms"
        rankedRooms.size == 1 -> "1 room checked, none weak"
        else -> "${rankedRooms.size} rooms checked, none weak"
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
            add("In ${highBandWeak.name} your device used ${bandLabel(highBandWeak.frequencyMhz)}, which carries less far through walls. If your router can split bands, 2.4 GHz may hold on better there.")
        }
        if (best != null && worst != null && best !== worst && best.averageRssi - worst.averageRssi >= 20 && weak.isNotEmpty()) {
            add("Signal drops ${best.averageRssi - worst.averageRssi} dB from ${best.name} to ${worst.name}. Try a more central, raised router position, then recheck the same spots to see whether it helps.")
        }
        if (accessPoints > 1) {
            add("Your device used $accessPoints access points across these rooms, which may reflect different bands or access points. This alone does not confirm reliable roaming.")
        }
        if (weak.isNotEmpty()) {
            add("Keep the router in the open, off the floor, and away from microwaves, mirrors, and fish tanks.")
        }
    }
    return CoverageSummary(ranked, weak, best, worst, accessPoints, tips, readings)
}

/** Ready-made names so checking a room is two taps. */
val RoomSuggestions = listOf(
    "Living room", "Kitchen", "Bedroom", "Office", "Kids' room", "Bathroom",
    "Dining room", "Hallway", "Garage", "Basement", "Balcony", "Garden"
)

/** Changes in signal are meaningful only with their measurement context visible. */
fun spotComparison(current: RoomReading, previous: RoomReading?): String {
    if (previous == null) return "First reading · recheck after making a change"
    if (current.spotKey != previous.spotKey) return "Different network or spot · start a new comparison"
    val delta = current.averageRssi - previous.averageRssi
    val signal = when {
        delta >= 4 -> "Signal $delta dB stronger"
        delta <= -4 -> "Signal ${-delta} dB weaker"
        else -> "Signal about the same"
    }
    val changed = buildList {
        if (current.bssid != previous.bssid) add("access point changed")
        if (bandLabel(current.frequencyMhz) != bandLabel(previous.frequencyMhz)) add("band changed")
    }
    return signal + if (changed.isEmpty()) " since last check" else " · ${changed.joinToString("; ")}"
}
