package com.pulsewave.app.feature.history

import com.pulsewave.app.core.database.entities.SpeedTestResultEntity
import com.pulsewave.app.core.database.entities.ToolResultEntity
import com.pulsewave.app.core.database.entities.WifiScanResultEntity
import com.pulsewave.app.core.router.LinkVerdict
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryModelTest {
    private fun scan(time: Long, ssid: String, bssid: String, rssi: Int, freq: Int = 5180) =
        WifiScanResultEntity(timestamp = time, ssid = ssid, bssid = bssid, rssi = rssi, frequency = freq, channelWidth = 80, security = "WPA2")

    private fun routerTest(time: Long, mbps: Float, ms: Float = 4f, rssi: Int? = -50) =
        SpeedTestResultEntity(timestamp = time, downloadMbps = -1f, uploadMbps = mbps, pingMs = ms, jitterMs = 1f, serverName = "Router 192.168.1.1", rssi = rssi)

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
    fun routerTestsReadAsSpeedWithVerdict() {
        val entry = buildHistory(emptyList(), emptyList(), emptyList(), listOf(routerTest(1, 412f))).single()
        assertEquals("412 Mbps to router", entry.title)
        assertEquals(LinkVerdict.Great, entry.verdict)
        assertTrue(entry.summary.startsWith("Great Wi-Fi"))
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
        assertEquals("2 of 3 replied · 15 ms average", entry.summary)
        assertEquals("Lost", entry.lines[1].trailing)
    }

    @Test
    fun insightComparesWithUsualSpeed() {
        val points = routerTests(listOf(routerTest(1, 400f), routerTest(2, 420f), routerTest(3, 100f)))
        assertTrue(trendInsight(points).startsWith("Slower than your usual"))
        assertTrue(trendInsight(points.take(2)).startsWith("About your usual"))
        assertTrue(trendInsight(points.take(1)).startsWith("Test again"))
    }
}
