package com.pulsewave.app.core.rooms

import com.pulsewave.app.core.wifi.bandLabel

data class CoverageSummary(
    /** Weakest first. */
    val rankedRooms: List<RoomReading>,
    val weakRooms: List<RoomReading>,
    val best: RoomReading?,
    val worst: RoomReading?,
    val accessPointCount: Int,
    val tips: List<String>
) {
    val headline: String get() = when {
        rankedRooms.isEmpty() -> "No spots saved yet"
        // Okay-but-not-good spots get honest wording; "good" is reserved for good signal.
        weakRooms.isEmpty() && rankedRooms.size == 1 && rankedRooms.first().quality == com.pulsewave.app.core.wifi.SignalQuality.Fair ->
            "${rankedRooms.first().name} is okay"
        weakRooms.isEmpty() && rankedRooms.size == 1 -> "${rankedRooms.first().name} looks good"
        weakRooms.isEmpty() && rankedRooms.any { it.quality == com.pulsewave.app.core.wifi.SignalQuality.Fair } ->
            "No weak spots; ${rankedRooms.first().name} is the weakest"
        weakRooms.isEmpty() -> "All ${rankedRooms.size} spots look good"
        weakRooms.size == 1 -> "1 weak spot: ${weakRooms.first().name}"
        else -> "${weakRooms.size} weak spots found"
    }
}

fun coverageSummary(readings: List<RoomReading>): CoverageSummary {
    val ranked = readings.sortedWith(compareBy({ it.averageRssi }, { it.name }))
    val weak = ranked.filter { it.quality.isProblem }
    val best = ranked.lastOrNull()
    val worst = ranked.firstOrNull()
    val accessPoints = readings.map { it.bssid.lowercase() }.filter { it.isNotBlank() && it != "02:00:00:00:00:00" }.toSet().size
    val tips = buildList {
        val highBandWeak = weak.firstOrNull { it.frequencyMhz >= 5000 }
        if (highBandWeak != null) {
            add("In ${highBandWeak.name} your phone used ${bandLabel(highBandWeak.frequencyMhz)}, which carries less far through walls. If your router can split bands, 2.4 GHz may hold on better there.")
        }
        if (best != null && worst != null && best !== worst && best.averageRssi - worst.averageRssi >= 20 && weak.isNotEmpty()) {
            add("Signal drops ${best.averageRssi - worst.averageRssi} dB from ${best.name} to ${worst.name}. A more central, raised router spot or a mesh point between them usually fixes this.")
        }
        if (accessPoints > 1) {
            add("Your phone used $accessPoints access points across these rooms, so mesh or extender roaming is working.")
        }
        if (weak.isNotEmpty()) {
            add("Keep the router in the open, off the floor, and away from microwaves, mirrors, and fish tanks.")
        }
    }
    return CoverageSummary(ranked, weak, best, worst, accessPoints, tips)
}

/** Ready-made names so checking a room is two taps. */
val RoomSuggestions = listOf(
    "Living room", "Kitchen", "Bedroom", "Office", "Kids' room", "Bathroom",
    "Dining room", "Hallway", "Garage", "Basement", "Balcony", "Garden"
)
