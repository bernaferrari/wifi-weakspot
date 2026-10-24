package com.bernaferrari.wifiweakspot.core.wifi

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScanSetupStatusTest {
    @Test
    fun oldAndroidHasNoLimit() {
        val status = ScanSetupStatus(throttlingApplies = false, canDisableThrottling = false, throttlingEnabled = true)
        assertTrue(status.isUnlimited)
        assertFalse(status.needsAttention)
    }

    @Test
    fun throttledPhoneWithoutRootPointsToDeveloperOptions() {
        val status = ScanSetupStatus()
        assertFalse(status.isUnlimited)
        assertTrue(status.needsAttention)
    }

    @Test
    fun developerOptionsSwitchLiftsTheLimit() {
        val status = ScanSetupStatus(developerOptionsEnabled = true, throttlingEnabled = false)
        assertTrue(status.isUnlimited)
        assertFalse(status.needsAttention)
    }

    @Test
    fun rootLiftsTheLimitWithoutDeveloperOptions() {
        val status = ScanSetupStatus(rootAvailable = true, rootScanning = true)
        assertTrue(status.isUnlimited)
        assertFalse(status.needsAttention)
    }

    @Test
    fun rootAloneIsNeverSuggestedToEveryone() {
        // Android 9 has no Developer options switch; root stays an opt-in on the setup screen.
        val android9 = ScanSetupStatus(canDisableThrottling = false, rootAvailable = true)
        assertFalse(android9.needsAttention)
    }
}
