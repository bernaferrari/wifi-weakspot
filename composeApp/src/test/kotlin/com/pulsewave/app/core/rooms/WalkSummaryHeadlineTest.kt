package com.pulsewave.app.core.rooms

import org.junit.Assert.assertEquals
import org.junit.Test

class WalkSummaryHeadlineTest {
    private fun walk(vararg rssi: Int) = summarizeWalk(rssi.mapIndexed { i, r -> WalkSample(i * 1_000L, r, null) })!!

    @Test fun okaySignalIsNotCalledStrong() = assertEquals("Okay everywhere measured", walk(-71, -72, -70).headline)
    @Test fun goodSignal() = assertEquals("Good everywhere measured", walk(-60, -64).headline)
    @Test fun strongSignal() = assertEquals("Strong everywhere measured", walk(-45, -50).headline)
}
