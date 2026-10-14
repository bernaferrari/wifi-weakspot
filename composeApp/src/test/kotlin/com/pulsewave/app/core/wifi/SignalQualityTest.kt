package com.pulsewave.app.core.wifi

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SignalQualityTest {
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
