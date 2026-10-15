package com.pulsewave.app.core.rooms

import com.pulsewave.app.core.wifi.SignalQuality

/** An interval is retained even when Wi-Fi disconnects or Android cannot provide RSSI. */
data class WalkSample(
    val elapsedMs: Long,
    val rssi: Int?,
    val routerMs: Float?,
    val connected: Boolean = true,
    /** A reply is counted once, rather than repeating it until the next reply arrives. */
    val routerReplyId: Long? = null
) {
    val measuredRssi: Int? get() = rssi?.takeIf { connected && it in -100..-1 }
}

data class WalkSummary(
    val durationMs: Long,
    val bestRssi: Int?,
    val worstRssi: Int?,
    val weakShare: Float,
    val weakMoments: Int,
    val medianRouterMs: Float?,
    val disconnectedShare: Float = 0f,
    val unknownShare: Float = 0f,
    val hadDisconnection: Boolean = false,
    val hadUnavailableReading: Boolean = false
) {
    val floor: SignalQuality? get() = worstRssi?.let(SignalQuality::of)
    val headline: String get() = when {
        hadDisconnection -> "Wi-Fi disconnected during your walk"
        hadUnavailableReading -> "Part of your walk couldn't be measured"
        weakMoments == 0 && floor == SignalQuality.Excellent -> "Strong everywhere measured"
        weakMoments == 0 && floor == SignalQuality.Good -> "Good everywhere measured"
        weakMoments == 0 -> "Okay everywhere measured"
        weakMoments == 1 -> "1 weak stretch on your walk"
        else -> "$weakMoments weak stretches on your walk"
    }
}

fun summarizeWalk(samples: List<WalkSample>): WalkSummary? {
    if (samples.isEmpty()) return null
    // Each sample describes the following interval; the final sample is an end marker.
    // Never call an unavailable interval strong and never infer a physical location from a dip.
    val intervals = samples.zipWithNext { a, b -> a to (b.elapsedMs - a.elapsedMs).coerceAtLeast(0L) }
    val duration = intervals.sumOf { it.second }
    fun share(predicate: (WalkSample) -> Boolean): Float = if (duration == 0L) 0f
        else intervals.filter { predicate(it.first) }.sumOf { it.second }.toFloat() / duration
    var moments = 0
    var inWeak = false
    samples.forEach { sample ->
        val weak = !sample.connected || sample.measuredRssi?.let { SignalQuality.of(it).isProblem } == true
        if (weak && !inWeak) moments++
        inWeak = weak
    }
    val readings = samples.mapNotNull { it.measuredRssi }
    val router = samples.filter { it.routerMs != null }
        .distinctBy { it.routerReplyId ?: it.elapsedMs }.mapNotNull { it.routerMs?.takeIf { ms -> ms.isFinite() } }.sorted()
    val median = router.takeIf { it.isNotEmpty() }?.let {
        if (it.size % 2 == 0) (it[it.size / 2 - 1] + it[it.size / 2]) / 2 else it[it.size / 2]
    }
    return WalkSummary(
        durationMs = duration, bestRssi = readings.maxOrNull(), worstRssi = readings.minOrNull(),
        weakShare = share { it.measuredRssi?.let { r -> SignalQuality.of(r).isProblem } == true },
        weakMoments = moments, medianRouterMs = median,
        disconnectedShare = share { !it.connected },
        unknownShare = share { it.connected && it.measuredRssi == null },
        hadDisconnection = samples.any { !it.connected },
        hadUnavailableReading = samples.any { it.connected && it.measuredRssi == null }
    )
}
