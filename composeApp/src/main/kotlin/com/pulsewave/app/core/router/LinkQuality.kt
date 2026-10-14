package com.pulsewave.app.core.router

import kotlin.math.abs

/** Response time, steadiness and loss for the phone-to-router hop. */
data class RouterLatency(
    val medianMs: Float,
    val jitterMs: Float,
    val lossPercent: Float,
    val samples: Int
)

fun summarizeLatency(rtts: List<Float>, sent: Int): RouterLatency? {
    if (sent <= 0) return null
    val received = rtts.size.coerceAtMost(sent)
    val loss = (sent - received) * 100f / sent
    if (rtts.isEmpty()) return RouterLatency(Float.NaN, Float.NaN, loss, 0)
    val sorted = rtts.sorted()
    val median = if (sorted.size % 2 == 1) sorted[sorted.size / 2]
    else (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2f
    val jitter = if (rtts.size < 2) 0f else rtts.zipWithNext { a, b -> abs(a - b) }.average().toFloat()
    return RouterLatency(median, jitter, loss, rtts.size)
}

enum class LinkVerdict(val headline: String, val explanation: String) {
    Great("Great Wi-Fi here", "Fast, quick to respond, and steady. Calls, games, and 4K video will feel smooth."),
    Good("Good Wi-Fi here", "Plenty for streaming and video calls. Busy moments may add a little delay."),
    Slow("Slow Wi-Fi here", "Expect buffering or lag. Try moving closer to the router or switching bands."),
    Unreliable("Unreliable Wi-Fi here", "The router is dropping or delaying replies. This spot needs a closer access point.")
}

/**
 * Judges what people feel: steadiness and loss first, then raw speed.
 * Upload is null when the burst could not run.
 */
fun linkVerdict(latency: RouterLatency, uploadMbps: Double?): LinkVerdict = when {
    latency.samples == 0 || latency.lossPercent >= 10f || latency.medianMs > 100f -> LinkVerdict.Unreliable
    latency.lossPercent >= 3f || latency.medianMs > 30f || latency.jitterMs > 25f ||
        (uploadMbps != null && uploadMbps < 10.0) -> LinkVerdict.Slow
    latency.lossPercent > 0f || latency.medianMs > 10f || latency.jitterMs > 8f ||
        (uploadMbps != null && uploadMbps < 80.0) -> LinkVerdict.Good
    else -> LinkVerdict.Great
}
