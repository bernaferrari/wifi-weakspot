package com.bernaferrari.wifiweakspot.core.permissions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PermissionStateTest {
    @Test fun scanPermissionDoesNotRequireNearbyConnectionPermission() {
        val state = PermissionState(
            wifiScan = PermissionStatus.GRANTED,
            location = PermissionStatus.GRANTED,
            notifications = PermissionStatus.DENIED,
            nearbyDevices = PermissionStatus.DENIED
        )
        assertTrue(state.canScanWifi)
    }

    @Test fun nearbyPermissionAloneDoesNotAllowLocationSensitiveScanning() {
        val state = PermissionState(
            wifiScan = PermissionStatus.DENIED,
            location = PermissionStatus.DENIED,
            notifications = PermissionStatus.GRANTED,
            nearbyDevices = PermissionStatus.GRANTED
        )
        assertFalse(state.canScanWifi)
    }
}
