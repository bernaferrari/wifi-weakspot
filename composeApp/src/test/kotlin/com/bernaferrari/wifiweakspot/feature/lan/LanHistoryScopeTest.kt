package com.bernaferrari.wifiweakspot.feature.lan

import com.bernaferrari.wifiweakspot.core.database.entities.LanDeviceEntity
import com.bernaferrari.wifiweakspot.feature.history.HistoryKind
import com.bernaferrari.wifiweakspot.feature.history.buildHistory
import org.junit.Assert.*
import org.junit.Test

class LanHistoryScopeTest {
    @Test fun adjacentScansOnDifferentNetworksStaySeparateEvenWithEqualPrivateIpsAndTimestamps() {
        val devices = listOf(
            LanDeviceEntity("192.168.1.12", null, null, null, 100L, true,
                customName = "Home printer", networkKey = "wifi:home"),
            LanDeviceEntity("192.168.1.12", null, null, null, 100L, true,
                customName = "Office TV", networkKey = "wifi:office"),
            LanDeviceEntity("192.168.1.12", null, null, null, 100L, true,
                customName = "Legacy device")
        )
        val entries = buildHistory(emptyList(), devices, emptyList(), emptyList())
        assertEquals(3, entries.size)
        assertEquals(3, entries.map { it.key }.toSet().size)
        assertTrue(entries.all { it.kind == HistoryKind.Devices && it.title == "1 device found" })
        assertEquals(setOf("Home printer", "Office TV", "Legacy device"), entries.map { it.lines.single().title }.toSet())
    }
}
