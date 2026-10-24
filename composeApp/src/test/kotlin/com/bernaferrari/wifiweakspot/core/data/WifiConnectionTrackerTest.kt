package com.bernaferrari.wifiweakspot.core.data

import org.junit.Assert.*
import org.junit.Test

class WifiConnectionTrackerTest {
    private fun connectedTracker() = WifiConnectionTracker<String, String, String>().apply {
        available("old")
        capabilities("old", "Old SSID", true)
        link("old", "192.168.1.2")
    }

    @Test fun handoffDoesNotCombineNewSsidAndOldAddress() {
        val tracker = connectedTracker()
        tracker.available("new")
        tracker.capabilities("new", "New SSID", false)
        assertEquals("New SSID", tracker.current()!!.second.info)
        assertNull(tracker.current()!!.second.link)
        assertFalse(tracker.current()!!.second.validated)
        tracker.link("new", "10.0.0.2")
        assertEquals("10.0.0.2", tracker.current()!!.second.link)
    }

    @Test fun lateOldCallbacksAndLossDoNotReplaceNewConnection() {
        val tracker = connectedTracker()
        tracker.available("new")
        tracker.capabilities("new", "New SSID", true)
        tracker.link("new", "10.0.0.2")
        tracker.link("old", "192.168.1.9")
        tracker.capabilities("old", "Old SSID", false)
        tracker.lost("old")
        assertEquals("new", tracker.current()!!.first)
        assertEquals("10.0.0.2", tracker.current()!!.second.link)
        assertTrue(tracker.current()!!.second.validated)
    }

    @Test fun losingSelectedConnectionFallsBackToAnotherKnownLink() {
        val tracker = connectedTracker()
        tracker.available("new")
        tracker.capabilities("new", "New SSID", true)
        tracker.lost("new")
        assertEquals("old", tracker.current()!!.first)
        assertEquals("192.168.1.2", tracker.current()!!.second.link)
        tracker.lost("old")
        assertNull(tracker.current())
    }

    @Test fun delayedPollingCannotOverwriteHandoffOrResurrectLostLink() {
        val tracker = connectedTracker()
        val beforeHandoff = tracker.selection()
        tracker.available("new")
        assertFalse(tracker.refresh(beforeHandoff, "old", "Old SSID", "old IP", true))
        val beforeLoss = tracker.selection()
        tracker.lost("new")
        assertFalse(tracker.refresh(beforeLoss, "new", "New SSID", "new IP", true))
        assertEquals("old", tracker.current()!!.first)
    }

    @Test fun refreshCanUpdateSelectedPhysicalWifiWithoutDefaultNetworkIdentity() {
        val tracker = connectedTracker()
        // The adapter refreshes this tracked physical Wi-Fi even if the default is a VPN.
        assertTrue(tracker.refresh(tracker.selection(), "old", "Updated signal", "192.168.1.2", true))
        assertEquals("old", tracker.current()!!.first)
        assertEquals("Updated signal", tracker.current()!!.second.info)
    }

    @Test fun callbacksFromAnUnavailableNetworkCannotCreateConnection() {
        val tracker = connectedTracker()
        tracker.capabilities("unavailable", "Other SSID", true)
        tracker.link("unavailable", "other IP")
        assertEquals("old", tracker.current()!!.first)
    }
}
