package com.bernaferrari.wifiweakspot.core.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SameRadioTest {
    private fun net(bssid: String, freq: Int = 5745) = WifiNetwork("x", bssid, -66, freq, 80, "", "", 0L)

    @Test fun locallyAdministeredVariantIsTheSameRadio() = assertTrue(sameRadio("b0:95:75:06:a8:0a", "b6:95:75:06:a8:0a"))
    @Test fun steppedLastByteIsTheSameRadio() = assertTrue(sameRadio("b0:95:75:06:a8:0a", "b0:95:75:06:a8:0b"))
    @Test fun differentDevicesAreNot() = assertFalse(sameRadio("b0:95:75:06:a8:0a", "60:a4:b7:c0:58:26"))
    @Test fun farLastByteIsNot() = assertFalse(sameRadio("b0:95:75:06:a8:0a", "b0:95:75:06:a8:9a"))

    @Test fun competitorsSkipYourOwnGuestNetworkButKeepOtherBands() {
        val mine = "b6:95:75:06:a8:0a"
        val all = listOf(net(mine), net("b0:95:75:06:a8:0a"), net("ba:95:75:06:a8:0a"), net("b0:95:75:06:a8:0b", freq = 2437), net("60:a4:b7:c0:58:26"))
        assertEquals(listOf("b0:95:75:06:a8:0b", "60:a4:b7:c0:58:26"), all.competitorsOf(mine, 5745).map { it.bssid })
    }

    @Test fun groupsNamesOfOneRadioUnderYourNetwork() {
        val mine = WifiNetwork("BatForce 5", "b6:95:75:06:a8:0a", -71, 5745, 80, "", "", 0L)
        val guest = WifiNetwork("Batcaverna", "b0:95:75:06:a8:0a", -70, 5745, 80, "", "", 0L)
        val hidden = WifiNetwork("", "ba:95:75:06:a8:0a", -69, 5745, 80, "", "", 0L)
        val neighbor = WifiNetwork("-5G", "aa:bb:cc:00:11:22", -90, 5220, 40, "", "", 0L)
        val groups = groupByRadio(listOf(guest, neighbor, hidden, mine), connectedBssid = mine.bssid)
        assertEquals(2, groups.size)
        assertEquals("BatForce 5", groups.first().lead.ssid)
        assertEquals(2, groups.first().extraNames)
    }
}
