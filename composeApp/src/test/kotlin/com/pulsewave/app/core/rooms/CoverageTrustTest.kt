package com.pulsewave.app.core.rooms

import com.pulsewave.app.core.router.LinkVerdict
import org.junit.Assert.*
import org.junit.Test

class CoverageTrustTest {
    private fun reading(id: String, ssid: String = "Home", time: Long = 1) =
        RoomReading(id, "Bedroom", -60, -62, 300, 5180, ssid, "ap-$ssid", 4, time)

    @Test fun recheckKeepsEarlierMeasurement() {
        val history = listOf(reading("before")).upsert(reading("after", time = 2))
        assertEquals(2, history.size)
    }

    @Test fun differentHomesDoNotReplaceEachOther() {
        val history = listOf(reading("home")).upsert(reading("office", "Office"))
        assertEquals(2, history.size)
    }

    @Test fun invalidReadingsCannotProduceStrongWalkHeadline() {
        val summary = summarizeWalk(listOf(WalkSample(0, -45, null), WalkSample(1000, 0, null), WalkSample(2000, -45, null)))!!
        assertFalse(summary.headline.startsWith("Strong everywhere"))
    }

    @Test fun multipleAccessPointsDoNotProveWorkingRoaming() {
        val summary = coverageSummary(listOf(reading("a"), reading("b").copy(name = "Office", bssid = "other")))
        assertFalse(summary.tips.any { it.contains("roaming is working") })
    }

    @Test fun localRouterVerdictDoesNotPromiseInternetPerformance() {
        assertFalse(LinkVerdict.Great.explanation.contains("4K"))
        assertFalse(LinkVerdict.Unreliable.explanation.contains("needs a closer"))
    }

    @Test fun summaryShowsLatestPerNetworkAndKeepsRecheckHistory() {
        val before = reading("before").copy(averageRssi = -85)
        val after = reading("after", time = 2).copy(averageRssi = -60)
        val other = reading("other", "Office")
        val history = listOf(before, after, other)
        val summary = coverageSummary(history.forNetwork("Home", "ap-Home"))
        assertEquals(listOf(after), summary.rankedRooms)
        assertEquals(2, summary.history.size)
        assertTrue(spotComparison(after, before).contains("25 dB stronger"))
        assertTrue(spotComparison(after.copy(frequencyMhz = 2437), before).contains("band changed"))
        assertTrue(history.forNetwork("", "02:00:00:00:00:00").isEmpty())
    }

    @Test fun timeSharesIncludeOutagesAndUnavailableIntervals() {
        val summary = summarizeWalk(listOf(
            WalkSample(0, -50, 2f, routerReplyId = 1),
            WalkSample(1000, null, null, connected = false),
            WalkSample(5000, null, null),
            WalkSample(6000, -50, 2f, routerReplyId = 1),
            WalkSample(10000, -50, 8f, routerReplyId = 2)
        ))!!
        assertEquals(0.4f, summary.disconnectedShare, 0.001f)
        assertEquals(0.1f, summary.unknownShare, 0.001f)
        assertEquals(0f, summary.weakShare, 0.001f)
        // One 2ms reply observed repeatedly must not outweigh the later 8ms reply.
        assertEquals(5f, summary.medianRouterMs)
    }

    @Test fun entirelyMissingWalkNeverInventsRssi() {
        val summary = summarizeWalk(listOf(WalkSample(0, null, null, false), WalkSample(4000, null, null, false)))!!
        assertNull(summary.bestRssi)
        assertNull(summary.worstRssi)
        assertEquals(1f, summary.disconnectedShare, 0f)
        assertTrue(summary.headline.contains("disconnected"))
    }
}
