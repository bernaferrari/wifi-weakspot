package com.bernaferrari.wifiweakspot.core.wifi

/**
 * Whether a scan made at [timestamp] still counts against the quota at [now]. It stops counting
 * the moment its window ends. A timestamp in the future means the clock was set back; it is
 * ignored rather than blocking scans for however far the clock moved.
 */
fun scanStillCounts(timestamp: Long, now: Long, windowMs: Long): Boolean =
    timestamp <= now && now - timestamp < windowMs

/**
 * The quota state for scans made at [timestamps], seen at [now]. All times are in milliseconds
 * on the same clock. While blocked, the seconds left are rounded up, so the state never says
 * "0 seconds" before a scan is actually allowed.
 */
fun scanQuotaState(timestamps: List<Long>, now: Long, maxScans: Int = 4, windowMs: Long = 120_000L): ThrottleState {
    val counted = timestamps.filter { scanStillCounts(it, now, windowMs) }.sorted()
    val remaining = maxScans - counted.size
    return when {
        remaining >= 3 -> ThrottleState.Ready(remaining, maxScans)
        remaining >= 1 -> ThrottleState.LowQuota(remaining, maxScans)
        else -> {
            // The next scan is allowed once enough of the oldest ones have left the window.
            val waitMs = counted[counted.size - maxScans] + windowMs - now
            ThrottleState.Exhausted(
                secondsUntilNextScan = (waitMs + 999) / 1000,
                cooldownDurationSeconds = windowMs / 1000
            )
        }
    }
}
