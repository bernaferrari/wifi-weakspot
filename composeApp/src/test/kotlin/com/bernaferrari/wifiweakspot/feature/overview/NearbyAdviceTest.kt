package com.bernaferrari.wifiweakspot.feature.overview

import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NearbyAdviceTest {
    private val now = 100_000L
    private val home = WifiNetwork("Home", "00:11:22:33:44:50", -50, 2412, 20, "WPA2", "", now)
    private val neighbor = home.copy(ssid = "Neighbor", bssid = "aa:bb:cc:dd:ee:ff")

    @Test fun adjacentChannelsCountAsOverlap() {
        val advice = nearbyAdvice(listOf(home, neighbor.copy(frequency = 2422)), home.bssid, home.frequency, now)
        assertEquals("1 nearby signal overlaps your channel 1.", advice.detail)
    }

    @Test fun wideChannelsUseTheirOccupiedBand() {
        val wide = home.copy(frequency = 5180, channelWidth = 80, centerFreq0 = 5210)
        val advice = nearbyAdvice(listOf(wide, neighbor.copy(frequency = 5240)), wide.bssid, wide.frequency, now)
        assertEquals("1 nearby signal overlaps your channel 36.", advice.detail)
        assertFalse(advice.detail.contains("clearest"))
    }

    @Test fun oldScansRequestRefreshWithoutGivingCurrentAdvice() {
        val advice = nearbyAdvice(listOf(home.copy(timestamp = 1)), home.bssid, home.frequency, now)
        assertFalse(advice.fresh)
        assertEquals("From your last scan. Tap to refresh.", advice.detail)
    }

    @Test fun staleConnectionEntryCannotAnchorFreshChannelAdvice() {
        val advice = nearbyAdvice(listOf(home.copy(timestamp = 1), neighbor), home.bssid, home.frequency, now)
        assertTrue(advice.fresh)
        assertEquals("See channels and signal for each network.", advice.detail)
    }

    @Test fun staleNeighborsDoNotInflateCurrentOverlapOrCount() {
        val advice = nearbyAdvice(listOf(home, neighbor.copy(timestamp = 1)), home.bssid, home.frequency, now)
        assertEquals(1, advice.networkCount)
        assertEquals("No nearby overlap detected on channel 1.", advice.detail)
    }

    @Test fun siblingNetworkNamesDoNotCompeteWithTheirOwnRouter() {
        val sibling = home.copy(ssid = "Guest", bssid = "00:11:22:33:44:51")
        val advice = nearbyAdvice(listOf(home, sibling), home.bssid, home.frequency, now)
        assertEquals("No nearby overlap detected on channel 1.", advice.detail)
    }
}
