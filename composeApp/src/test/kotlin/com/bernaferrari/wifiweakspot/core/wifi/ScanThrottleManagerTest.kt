package com.bernaferrari.wifiweakspot.core.wifi

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Tests the scanQuotaState rules used by AndroidScanThrottleManager.
 */
class ScanThrottleManagerTest {

    // A clock and a list of scans; the rules themselves are the app's own scanQuotaState.
    private class TestScanThrottleManager(
        private val maxScans: Int = 4,
        private val windowMs: Long = 120_000L, // 2 minutes
        private val isThrottlingDisabled: () -> Boolean = { false }
    ) {
        private val scanTimestamps = mutableListOf<Long>()
        private var currentTimeMs: Long = 0

        fun setTime(timeMs: Long) {
            currentTimeMs = timeMs
        }

        fun recordScan() {
            scanTimestamps.add(currentTimeMs)
        }

        fun getThrottleState(): ThrottleState =
            if (isThrottlingDisabled()) ThrottleState.Unlimited
            else scanQuotaState(scanTimestamps, currentTimeMs, maxScans, windowMs)
    }

    @Test
    fun initialState_shouldBeReadyWithFullQuota() {
        val manager = TestScanThrottleManager()
        
        val state = manager.getThrottleState()
        
        assertTrue("Expected Ready state", state is ThrottleState.Ready)
        state as ThrottleState.Ready
        assertEquals(4, state.scansRemaining)
        assertEquals(4, state.maxScans)
    }

    @Test
    fun afterOneScan_shouldStillBeReady() {
        val manager = TestScanThrottleManager()
        manager.setTime(0)
        
        manager.recordScan()
        
        val state = manager.getThrottleState()
        assertTrue("Expected Ready state", state is ThrottleState.Ready)
        state as ThrottleState.Ready
        assertEquals(3, state.scansRemaining)
    }

    @Test
    fun afterThreeScans_shouldShowLowQuota() {
        val manager = TestScanThrottleManager()
        manager.setTime(0)
        
        repeat(3) { manager.recordScan() }
        
        val state = manager.getThrottleState()
        assertTrue("Expected LowQuota state", state is ThrottleState.LowQuota)
        state as ThrottleState.LowQuota
        assertEquals(1, state.scansRemaining)
    }

    @Test
    fun afterFourScans_shouldBeExhausted() {
        val manager = TestScanThrottleManager()
        manager.setTime(0)
        
        repeat(4) { manager.recordScan() }
        
        val state = manager.getThrottleState()
        assertTrue("Expected Exhausted state", state is ThrottleState.Exhausted)
    }

    @Test
    fun whenThrottlingDisabled_shouldReturnUnlimited() {
        val manager = TestScanThrottleManager(isThrottlingDisabled = { true })
        manager.setTime(0)
        
        // Even after many scans
        repeat(10) { manager.recordScan() }
        
        val state = manager.getThrottleState()
        assertTrue("Expected Unlimited state", state is ThrottleState.Unlimited)
    }

    @Test
    fun oldTimestamps_shouldBePruned() {
        val manager = TestScanThrottleManager(windowMs = 1000) // 1 second window for faster testing
        manager.setTime(0)
        
        repeat(4) { manager.recordScan() }
        
        // Move time forward past the window
        manager.setTime(1500)
        
        val state = manager.getThrottleState()
        assertTrue("Expected Ready state", state is ThrottleState.Ready)
        state as ThrottleState.Ready
        assertEquals(4, state.scansRemaining)
    }

    @Test
    fun exhaustedState_shouldCalculateCorrectRemainingTime() {
        val manager = TestScanThrottleManager(windowMs = 120_000L) // 2 minutes
        manager.setTime(0)
        
        repeat(4) { manager.recordScan() }
        
        // Move 30 seconds forward
        manager.setTime(30_000)
        
        val state = manager.getThrottleState()
        assertTrue("Expected Exhausted state", state is ThrottleState.Exhausted)
        state as ThrottleState.Exhausted
        assertEquals(90L, state.secondsUntilNextScan)
    }

    @Test
    fun partialQuotaRestore_afterOldestScanExpires() {
        val manager = TestScanThrottleManager(windowMs = 120_000L)
        
        // Spread scans over time so they expire at different times
        manager.setTime(0)
        manager.recordScan()      // Expires at t=120000
        
        manager.setTime(30_000)
        manager.recordScan()      // Expires at t=150000
        
        manager.setTime(60_000)
        manager.recordScan()      // Expires at t=180000
        
        manager.setTime(90_000)
        manager.recordScan()      // Expires at t=210000
        
        // At t=120001, first scan expires, leaving 3 scans (1 remaining = LowQuota)
        manager.setTime(120_001)
        
        val state = manager.getThrottleState()
        assertTrue("Expected LowQuota state, got $state", state is ThrottleState.LowQuota)
        state as ThrottleState.LowQuota
        assertEquals(1, state.scansRemaining)
    }

    @Test
    fun lastSecond_shouldStillShowOneSecond() {
        val manager = TestScanThrottleManager()
        repeat(4) { manager.recordScan() }
        manager.setTime(119_500)

        assertEquals(1L, (manager.getThrottleState() as ThrottleState.Exhausted).secondsUntilNextScan)
    }

    @Test
    fun exhausted_shouldNeverReportZeroSeconds() {
        val manager = TestScanThrottleManager()
        repeat(4) { manager.recordScan() }

        for (time in 0L..119_999L step 250L) {
            manager.setTime(time)
            val state = manager.getThrottleState()
            assertTrue(state is ThrottleState.Exhausted && state.secondsUntilNextScan >= 1L)
        }
    }

    @Test
    fun windowEnd_shouldFreeTheScanExactlyOnTime() {
        val manager = TestScanThrottleManager()
        for (time in listOf(0L, 1_000L, 2_000L, 3_000L)) {
            manager.setTime(time)
            manager.recordScan()
        }
        manager.setTime(120_000)

        assertEquals(1, (manager.getThrottleState() as ThrottleState.LowQuota).scansRemaining)
    }

    @Test
    fun futureTimestamps_shouldBeIgnored() {
        val manager = TestScanThrottleManager()
        manager.setTime(500_000)
        repeat(4) { manager.recordScan() }
        manager.setTime(1_000)

        assertEquals(4, (manager.getThrottleState() as ThrottleState.Ready).scansRemaining)
    }

    @Test
    fun moreScansThanTheQuota_shouldWaitForEnoughToExpire() {
        val manager = TestScanThrottleManager()
        for (time in 0L..40_000L step 10_000L) {
            manager.setTime(time)
            manager.recordScan()
        }
        manager.setTime(50_000)

        assertEquals(80L, (manager.getThrottleState() as ThrottleState.Exhausted).secondsUntilNextScan)
    }
}
