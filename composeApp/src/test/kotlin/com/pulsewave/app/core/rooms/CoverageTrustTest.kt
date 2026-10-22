package com.pulsewave.app.core.rooms

import com.pulsewave.app.core.router.LinkVerdict
import org.junit.Assert.*
import org.junit.Test

class CoverageTrustTest {
    @Test fun redactedDisplayNamesCannotCreateAGlobalRoomIdentity() {
        listOf("Your Wi-Fi", "Wi-Fi network", "<unknown ssid>", "Connected Wi-Fi").forEach { placeholder ->
            assertNull(roomNetworkKey(placeholder, "02:00:00:00:00:00"))
            assertEquals("ap:aa:bb:cc:dd:ee:01", roomNetworkKey(placeholder, "AA:BB:CC:DD:EE:01"))
        }
        assertNull(roomNetworkKey("\"<unknown ssid>\"", ""))
    }

    @Test fun knownMeshNameStillGroupsItsDistinctAccessPoints() {
        assertEquals(roomNetworkKey("Home", "ap-one"), roomNetworkKey("Home", "ap-two"))
    }

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

}
