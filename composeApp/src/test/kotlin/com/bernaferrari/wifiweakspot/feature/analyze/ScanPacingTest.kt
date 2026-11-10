package com.bernaferrari.wifiweakspot.feature.analyze

import org.junit.Assert.assertEquals
import org.junit.Test

class ScanPacingTest {
    @Test fun firstScanIsImmediate() =
        assertEquals(0L, resumeDelayMs(30_000, 0, 500_000))

    @Test fun quickReturnWaitsOutTheRestOfTheInterval() =
        assertEquals(20_000L, resumeDelayMs(30_000, 100_000, 110_000))

    @Test fun longAbsenceScansRightAway() =
        assertEquals(0L, resumeDelayMs(30_000, 100_000, 400_000))

    @Test fun clockSetBackScansRightAway() =
        assertEquals(0L, resumeDelayMs(30_000, 100_000, 90_000))
}
