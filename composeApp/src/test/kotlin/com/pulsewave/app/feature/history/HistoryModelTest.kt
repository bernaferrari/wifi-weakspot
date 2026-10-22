package com.pulsewave.app.feature.history

import com.pulsewave.app.core.database.entities.SpeedTestResultEntity
import com.pulsewave.app.core.database.entities.ToolResultEntity
import com.pulsewave.app.core.database.entities.WifiScanResultEntity
import com.pulsewave.app.core.router.LinkVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryModelTest {
    @Test fun internetTestsHaveTheirOwnFilterAndNeverEnterRouterTrends() {
        val internet = SpeedTestResultEntity(timestamp = 1, downloadMbps = 200f, uploadMbps = 40f,
            pingMs = 20f, jitterMs = 2f, serverName = "Internet test")
        val entry = buildHistory(emptyList(), emptyList(), emptyList(), listOf(internet)).single()
        assertEquals(HistoryKind.Internet, entry.kind)
        assertTrue(routerTests(listOf(internet)).isEmpty())
    }

    private fun scan(time: Long, ssid: String, bssid: String, rssi: Int, freq: Int = 5180) =
        WifiScanResultEntity(timestamp = time, ssid = ssid, bssid = bssid, rssi = rssi, frequency = freq, channelWidth = 80, security = "WPA2")

    private fun routerTest(time: Long, mbps: Float, ms: Float = 4f, rssi: Int? = -50) =
        SpeedTestResultEntity(timestamp = time, downloadMbps = -1f, uploadMbps = mbps, pingMs = ms, jitterMs = 1f, serverName = "Router 192.168.1.1", rssi = rssi, ssid = "Home")

    @Test
    fun unchangedScansWithinAnHourShowOnce() {
        val minute = 60_000L
        val rows = listOf(
            scan(0, "Home", "a", -50), scan(0, "Neighbour", "c", -80),
            scan(20 * minute, "Home", "a", -52), scan(20 * minute, "Neighbour", "c", -79),
            // A new network appeared, so this one is worth keeping.
            scan(40 * minute, "Home", "a", -51), scan(40 * minute, "Cafe", "d", -85)
        )
        val entries = buildHistory(rows, emptyList(), emptyList(), emptyList())
        assertEquals(listOf(40 * minute, 20 * minute), entries.map { it.timestamp })
    }

    @Test
    fun scansSavedTogetherBecomeOneEntryPerSession() {
        val minute = 60_000L
        val rows = listOf(
            scan(10 * minute, "Home", "a", -48),
            scan(10 * minute, "Home", "b", -70, freq = 2437),
            scan(10 * minute, "Neighbour", "c", -80),
            scan(11 * minute, "Home", "a", -50),
            // Far earlier: a separate session.
            scan(0, "Home", "a", -60)
        )
        val entries = buildHistory(rows, emptyList(), emptyList(), emptyList())
        assertEquals(2, entries.size)
        val latest = entries.first()
        assertEquals("2 networks nearby", latest.title)
        assertEquals("Strongest: Home", latest.summary)
        assertEquals("2 access points · 2.4 GHz + 5 GHz", latest.lines.first().supporting)
    }

    @Test
    fun legacySenderEstimatesAreHiddenWithoutLosingLatency() {
        val entry = buildHistory(emptyList(), emptyList(), emptyList(), listOf(routerTest(1, 412f))).single()
        assertEquals("4 ms to router", entry.title)
        assertEquals(LinkVerdict.Great, entry.verdict)
        assertTrue(entry.summary.startsWith("Router responds quickly"))
    }

    @Test
    fun silentRouterIsUnreliable() {
        val entry = buildHistory(emptyList(), emptyList(), emptyList(), listOf(routerTest(1, -1f, ms = -1f))).single()
        assertEquals("Router didn't answer", entry.title)
        assertEquals(LinkVerdict.Unreliable, entry.verdict)
    }

    @Test
    fun pingSummaryCountsReplies() {
        val ping = ToolResultEntity(timestamp = 1, toolType = "PING", target = "1.1.1.1",
            resultJson = "Reply from 1.1.1.1: time=10.0 ms\nRequest timed out: timeout\nReply from 1.1.1.1: time=20.0 ms", isSuccess = true)
        val entry = buildHistory(emptyList(), emptyList(), listOf(ping), emptyList()).single()
        assertEquals("15 ms · 2 of 3 replied", entry.summary)
        assertEquals("Lost", entry.lines[1].trailing)
    }

    @Test
    fun insightComparesWithUsualSpeed() {
        val points = routerTests(listOf(routerTest(1, 400f, ms = 4f), routerTest(2, 420f, ms = 4f), routerTest(3, 100f, ms = 40f)))
        assertTrue(trendInsight(points).startsWith("Slower"))
        assertTrue(trendInsight(points.take(2)).startsWith("About the same"))
        assertTrue(trendInsight(points.take(1)).startsWith("Recheck"))
    }

    @Test fun trendsNeverCompareUnrelatedNetworksOrUnverifiedSenderRates() {
        val home = routerTest(1, 900f, ms = 2f)
        val office = routerTest(2, 20f, ms = 80f).copy(ssid = "Office")
        val points = routerTests(listOf(home, office))
        assertEquals(1, points.size)
        assertEquals("Office", points.single().networkName)
        assertEquals(80f, points.single().responseMs)
        val entry = buildHistory(emptyList(), emptyList(), emptyList(), listOf(home)).single()
        assertTrue(entry.metrics.none { it.unit == "Mbps" })
    }
}
