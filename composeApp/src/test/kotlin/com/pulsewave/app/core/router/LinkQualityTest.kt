package com.pulsewave.app.core.router

import org.junit.Assert.assertEquals
import org.junit.Test

class LinkQualityTest {
    @Test
    fun latencySummaryUsesMedianAndCountsLoss() {
        val latency = summarizeLatency(listOf(3f, 1f, 2f, 10f), sent = 5)!!
        assertEquals(2.5f, latency.medianMs)
        assertEquals(20f, latency.lossPercent)
        assertEquals(4, latency.samples)
        assertEquals(11f / 3, latency.jitterMs, 0.001f) // (|1-3| + |2-1| + |10-2|) / 3
    }

    @Test
    fun noRepliesMeansUnreliable() {
        val latency = summarizeLatency(emptyList(), sent = 10)!!
        assertEquals(LinkVerdict.Unreliable, linkVerdict(latency, null))
    }

    @Test
    fun verdictWeighsSteadinessBeforeSpeed() {
        val fast = RouterLatency(medianMs = 2f, jitterMs = 1f, lossPercent = 0f, samples = 25)
        assertEquals(LinkVerdict.Great, linkVerdict(fast, 300.0))
        assertEquals(LinkVerdict.Good, linkVerdict(fast, 40.0))
        assertEquals(LinkVerdict.Slow, linkVerdict(fast.copy(lossPercent = 4f), 300.0))
        assertEquals(LinkVerdict.Unreliable, linkVerdict(fast.copy(medianMs = 150f), 300.0))
    }
}
