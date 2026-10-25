package com.bernaferrari.wifiweakspot.core.router

import com.bernaferrari.wifiweakspot.core.i18n.appStrings
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

enum class LinkVerdict {
    Great, Good, Slow, Unreliable;

    val headline: String get() = with(appStrings.domain) {
        when (this@LinkVerdict) {
            Great -> verdictGreatHeadline
            Good -> verdictGoodHeadline
            Slow -> verdictSlowHeadline
            Unreliable -> verdictUnreliableHeadline
        }
    }

    val explanation: String get() = with(appStrings.domain) {
        when (this@LinkVerdict) {
            Great -> verdictGreatExplanation
            Good -> verdictGoodExplanation
            Slow -> verdictSlowExplanation
            Unreliable -> verdictUnreliableExplanation
        }
    }
}

/** Judges local router replies only. There is no receiver-backed throughput measurement. */
fun linkVerdict(latency: RouterLatency): LinkVerdict = when {
    latency.samples == 0 || latency.lossPercent >= 10f || latency.medianMs > 100f -> LinkVerdict.Unreliable
    latency.lossPercent >= 3f || latency.medianMs > 30f || latency.jitterMs > 25f -> LinkVerdict.Slow
    latency.lossPercent > 0f || latency.medianMs > 10f || latency.jitterMs > 8f -> LinkVerdict.Good
    else -> LinkVerdict.Great
}
