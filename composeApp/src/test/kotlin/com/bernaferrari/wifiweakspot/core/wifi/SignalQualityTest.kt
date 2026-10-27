package com.bernaferrari.wifiweakspot.core.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalQualityTest {
    @Test
    fun percentageFollowsTheVerdictBandsAndClamps() {
        for ((rssi, percentage) in listOf(-100 to 0, -90 to 0, -86 to 15, -82 to 30, -75 to 50, -67 to 70, -60 to 82, -55 to 90, -50 to 100, -40 to 100)) {
            assertEquals(percentage, SignalQuality.percentage(rssi))
        }
        assertEquals(0, SignalQuality.percentage(Int.MIN_VALUE))
        assertEquals(100, SignalQuality.percentage(Int.MAX_VALUE))
    }

    @Test
    fun strongerSignalsNeverHaveLowerPercentages() {
        for (rssi in -110..-20) {
            assertTrue(SignalQuality.percentage(rssi + 1) >= SignalQuality.percentage(rssi))
        }
    }

    @Test
    fun verdictFloorsMapToRoundPercentages() {
        for ((quality, floor) in listOf(SignalQuality.Excellent to 90, SignalQuality.Good to 70, SignalQuality.Fair to 50, SignalQuality.Weak to 30)) {
            assertEquals(floor, SignalQuality.percentage(quality.minRssi))
            assertTrue(SignalQuality.percentage(quality.minRssi - 1) < floor)
        }
    }

    @Test
    fun thresholdsAreInclusiveAtTheirFloor() {
        assertEquals(SignalQuality.Excellent, SignalQuality.of(-55))
        assertEquals(SignalQuality.Good, SignalQuality.of(-56))
        assertEquals(SignalQuality.Good, SignalQuality.of(-67))
        assertEquals(SignalQuality.Fair, SignalQuality.of(-68))
        assertEquals(SignalQuality.Weak, SignalQuality.of(-82))
        assertEquals(SignalQuality.DeadZone, SignalQuality.of(-83))
    }

    @Test
    fun onlyWeakAndDeadZoneAreProblems() {
        assertFalse(SignalQuality.Fair.isProblem)
        assertTrue(SignalQuality.Weak.isProblem)
        assertTrue(SignalQuality.DeadZone.isProblem)
    }

    @Test
    fun fractionIsClampedToUsefulRange() {
        assertEquals(0f, SignalQuality.fraction(-120))
        assertEquals(1f, SignalQuality.fraction(-20))
        assertEquals(0.5f, SignalQuality.fraction(-65), 0.001f)
    }
}
