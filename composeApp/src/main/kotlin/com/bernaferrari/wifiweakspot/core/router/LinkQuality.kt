package com.bernaferrari.wifiweakspot.core.router

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
    Great("Router responds quickly", "Replies were quick and steady."),
    Good("Router response is usable", "Some replies were a little slow or uneven."),
    Slow("Router replies are delayed", "Replies were slow or uneven. Try again near the router to compare."),
    Unreliable("Router check inconclusive", "Many replies went missing. Some routers ignore these checks, so try again before changing anything.")
}

/** Judges local router replies only. There is no receiver-backed throughput measurement. */
fun linkVerdict(latency: RouterLatency): LinkVerdict = when {
    latency.samples == 0 || latency.lossPercent >= 10f || latency.medianMs > 100f -> LinkVerdict.Unreliable
    latency.lossPercent >= 3f || latency.medianMs > 30f || latency.jitterMs > 25f -> LinkVerdict.Slow
    latency.lossPercent > 0f || latency.medianMs > 10f || latency.jitterMs > 8f -> LinkVerdict.Good
    else -> LinkVerdict.Great
}
