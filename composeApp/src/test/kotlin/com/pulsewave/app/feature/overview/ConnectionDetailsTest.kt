package com.pulsewave.app.feature.overview

import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.wifi.WifiNetwork
import org.junit.Assert.assertEquals
import org.junit.Test

class ConnectionDetailsTest {
    private val status = NetworkStatus("Home", "00:11:22:33:44:55", 5180, 36, -60, 100, "", true, "WPA2", sampledAtMillis = 100_000)
    private val scan = WifiNetwork(status.ssid, status.bssid, -50, 5180, 80, "WPA2", "", 99_000, centerFreq0 = 5210)

    @Test fun freshMatchingMetadataUsesTheLiveSignal() {
        val detail = connectionDetails(status, listOf(scan), 100_000)
        assertEquals(80, detail.channelWidth)
        assertEquals(-60, detail.rssi)
        assertEquals(100_000L, detail.timestamp)
    }

    @Test fun oldOrDifferentChannelMetadataCannotInventCurrentWidth() {
        for (unrelated in listOf(scan.copy(timestamp = 1), scan.copy(frequency = 2437))) {
            val detail = connectionDetails(status, listOf(unrelated), 100_000)
            assertEquals(0, detail.channelWidth)
            assertEquals(5180, detail.frequency)
        }
    }
}
