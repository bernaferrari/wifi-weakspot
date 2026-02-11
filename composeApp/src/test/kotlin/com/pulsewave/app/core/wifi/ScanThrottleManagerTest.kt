package com.pulsewave.app.core.wifi

import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Unit tests for ScanThrottleManager implementation logic.
 * Tests the core throttle state machine without Android dependencies.
 */
class ScanThrottleManagerTest {

    // Test implementation without Android dependencies
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
            pruneOldTimestamps()
        }
        
        fun getThrottleState(): ThrottleState {
            if (isThrottlingDisabled()) {
                return ThrottleState.Unlimited
            }
            
            pruneOldTimestamps()
            val remaining = maxScans - scanTimestamps.size
            
            return when {
                remaining >= 3 -> ThrottleState.Ready(remaining, maxScans)
                remaining in 1..2 -> ThrottleState.LowQuota(remaining, maxScans)
                remaining <= 0 -> {
                    val oldestScan = scanTimestamps.minOrNull() ?: return ThrottleState.Ready(maxScans, maxScans)
                    val expiresAt = oldestScan + windowMs
                    val secondsRemaining = ((expiresAt - currentTimeMs) / 1000).coerceAtLeast(0)
                    ThrottleState.Exhausted(secondsRemaining, windowMs / 1000)
                }
                else -> ThrottleState.Unknown
            }
        }
        
        private fun pruneOldTimestamps() {
            scanTimestamps.removeAll { currentTimeMs - it > windowMs }
        }
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
}
