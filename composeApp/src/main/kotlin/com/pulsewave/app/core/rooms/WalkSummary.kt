package com.pulsewave.app.core.rooms

import com.pulsewave.app.core.wifi.SignalQuality

/** One reading while walking: connected-link RSSI plus the latest router reply, if any. */
data class WalkSample(val elapsedMs: Long, val rssi: Int, val routerMs: Float?)

data class WalkSummary(
    val durationMs: Long,
    val bestRssi: Int,
    val worstRssi: Int,
    /** 0..1 share of samples in Weak or Dead zone. */
    val weakShare: Float,
    /** Separate stretches of weak signal, so one long dip counts once. */
    val weakMoments: Int,
    val medianRouterMs: Float?
) {
    val headline: String get() = when {
        weakMoments == 0 -> "Strong everywhere you walked"
        weakMoments == 1 -> "1 weak spot on your walk"
        else -> "$weakMoments weak spots on your walk"
    }
}

fun summarizeWalk(samples: List<WalkSample>): WalkSummary? {
    if (samples.isEmpty()) return null
    var moments = 0
    var inWeak = false
    samples.forEach { sample ->
        val weak = SignalQuality.of(sample.rssi).isProblem
        if (weak && !inWeak) moments++
        inWeak = weak
    }
    val router = samples.mapNotNull { it.routerMs }.sorted()
    return WalkSummary(
        durationMs = samples.last().elapsedMs - samples.first().elapsedMs,
        bestRssi = samples.maxOf { it.rssi },
        worstRssi = samples.minOf { it.rssi },
        weakShare = samples.count { SignalQuality.of(it.rssi).isProblem }.toFloat() / samples.size,
        weakMoments = moments,
        medianRouterMs = router.takeIf { it.isNotEmpty() }?.let { it[it.size / 2] }
    )
}
