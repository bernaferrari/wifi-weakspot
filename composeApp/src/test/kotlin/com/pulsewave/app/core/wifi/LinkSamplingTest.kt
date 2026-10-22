package com.pulsewave.app.core.wifi

import com.pulsewave.app.core.model.NetworkStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Assert.*
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LinkSamplingTest {
    private fun status(time: Long, bssid: String = "ap", ssid: String = "Home") = NetworkStatus(
        ssid, bssid, 5180, 36, -60, 400, "192.168.1.2", true, "WPA2", sampledAtMillis = time)

    @Test fun acceptsUnchangedSignalOnlyWhenPlatformReadsAreFreshAndSpaced() = runTest {
        val source = flow {
            emit(status(0))
            repeat(3) {
                delay(500)
                emit(status(testScheduler.currentTime))
            }
        }
        val samples = collectFreshLinkSamples(source, "AP", 0, 3, 500, clock = { testScheduler.currentTime })
        assertEquals(listOf(500L, 1000L, 1500L), samples.map { it.sampledAtMillis })
        assertEquals(3, samples.size)
        assertTrue(samples.all { it.rssi == -60 })
    }

    @Test fun replayingACachedStateNeverCompletesTheBatch() = runTest {
        val source = MutableStateFlow(status(10))
        val samples = withTimeoutOrNull(4000) {
            collectFreshLinkSamples(source, "ap", 10, 3, 500, clock = { testScheduler.currentTime })
        }
        assertNull(samples)
    }

    @Test fun callbacksWithoutReadTimeAndStartupBurstsAreNotIndependentSamples() = runTest {
        try {
            collectFreshLinkSamples(flowOf(status(0), status(100), status(101), status(102)),
                "ap", 0, 3, 500, clock = { 102 })
            fail("An immediate callback burst must not complete a sample window")
        } catch (_: IllegalStateException) { }
    }

    @Test fun aRoamAndReturnInvalidatesTheWholeBatch() = runTest {
        val source = flow {
            delay(500); emit(status(500))
            delay(500); emit(status(1000, bssid = "other"))
            delay(500); emit(status(1500))
        }
        try {
            collectFreshLinkSamples(source, "ap", 0, 2, 500, clock = { testScheduler.currentTime })
            fail("Mixed access points must not be summarized together")
        } catch (_: IllegalStateException) { }
    }

    @Test fun networkNameChangeOnSameAccessPointAlsoInvalidatesBatch() = runTest {
        try {
            collectFreshLinkSamples(flowOf(status(500), status(1000, ssid = "Other")),
                "ap", 0, 2, 500, clock = { 1000 })
            fail("A changed network must invalidate the batch")
        } catch (_: IllegalStateException) { }
    }

    @Test fun futureAndOldReadTimesCannotCompleteBatch() = runTest {
        try {
            collectFreshLinkSamples(flowOf(status(1000), status(9000)),
                "ap", 0, 2, 500, clock = { 5000 })
            fail("Stale and future-dated values must be discarded")
        } catch (_: IllegalStateException) { }
    }

    @Test fun unknownAgeMetadataBetweenPlatformReadsDoesNotAbortOrCount() = runTest {
        val source = flow {
            emit(status(0, bssid = "02:00:00:00:00:00", ssid = "Your Wi-Fi").copy(rssi = -127))
            repeat(3) {
                delay(500)
                emit(status(testScheduler.currentTime))
                emit(status(0, ssid = "Your Wi-Fi"))
            }
        }
        val samples = collectFreshLinkSamples(source, "ap", 0, 3, 500,
            expectedSsid = "Home", clock = { testScheduler.currentTime })
        assertEquals(listOf(500L, 1000L, 1500L), samples.map { it.sampledAtMillis })
    }

    @Test fun unknownAgeCallbacksStillAbortOnConfirmedRoamOrDisconnect() = runTest {
        listOf(status(0, bssid = "other"), NetworkStatus.Disconnected).forEach { changed ->
            try {
                collectFreshLinkSamples(flowOf(changed), "ap", 0, 3, 500, clock = { 0 })
                fail("A known roam/disconnect must abort even without an RSSI sample time")
            } catch (_: IllegalStateException) { }
        }
    }
}
