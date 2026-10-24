package com.bernaferrari.wifiweakspot.feature.overview

import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ConnectionAdviceTest {
    private fun connected(rssi: Int, validated: Boolean) = NetworkStatus(
        "Home", "", 2412, 1, rssi, 100, "", true, "WPA2", validated
    )

    @Test fun strongSignalDoesNotHideMissingInternetAccess() {
        assertEquals("Check internet access first", connectionAdvice(connected(-40, false)).title)
    }
    @Test fun weakSignalSuggestsARepeatableComparison() {
        assertTrue(connectionAdvice(connected(-80, true)).explanation.contains("repeat near the router"))
    }
    @Test fun redactedSignalIsNotPresentedAsExcellent() {
        assertEquals("Read your Wi-Fi signal", connectionAdvice(connected(-127, true)).title)
    }
    @Test fun disconnectedWifiStillExplainsAvailableTools() {
        assertTrue(connectionAdvice(NetworkStatus.Disconnected).explanation.contains("still run"))
    }
}
