package com.pulsewave.app.feature.walk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.rooms.*
import com.pulsewave.app.core.router.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WalkTrustTest {
    private fun connection(ssid: String = "Home", bssid: String = "home-ap") = NetworkStatus(
        ssid, bssid, 5180, 36, -50, 400, "192.168.1.2", true, "WPA2", gateway = "192.168.1.1")

    @Test fun outageIsRetainedAndStaleRouterReplyExpires() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var ownedModel: ViewModel? = null
        try {
            val link = MutableStateFlow(connection())
            val repository = mockk<NetworkStatusRepository>()
            every { repository.getCurrentNetworkStatus(any()) } returns link
            val rooms = mockk<RoomCheckRepository>()
            every { rooms.readings } returns MutableStateFlow(emptyList())
            val probe = mockk<RouterProbe>()
            every { probe.ping(any(), any(), any()) } returns flow {
                emit(PingEvent.Reply(1, 2f))
                awaitCancellation()
            }
            val model = WalkViewModel(repository, rooms, probe) { testScheduler.currentTime }
            ownedModel = model
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.live.collect() }
            runCurrent()
            model.startWalk()
            runCurrent()
            advanceTimeBy(3500)
            runCurrent()
            assertNull(model.session.value!!.latestRouterMs)
            link.value = NetworkStatus.Disconnected
            runCurrent()
            advanceTimeBy(2500)
            runCurrent()
            model.finishWalk()
            val (summary, samples) = model.finished.value!!
            assertTrue(samples.any { !it.connected })
            assertTrue(summary.disconnectedShare > 0f)
            assertTrue(summary.headline.contains("disconnected"))
            assertEquals(2f, summary.medianRouterMs)
        } finally { backgroundScope.cancel(); ownedModel?.viewModelScope?.cancel(); runCurrent(); Dispatchers.resetMain() }
    }

    @Test fun recheckCollectsFreshReadingsAndRejectsNetworkSwitch() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var ownedModel: ViewModel? = null
        try {
            val link = MutableStateFlow(connection())
            val repository = mockk<NetworkStatusRepository>()
            every { repository.getCurrentNetworkStatus(any()) } returns link
            val rooms = mockk<RoomCheckRepository>(relaxed = true)
            every { rooms.readings } returns MutableStateFlow(emptyList())
            val model = WalkViewModel(repository, rooms, mockk()) { testScheduler.currentTime }
            ownedModel = model
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.live.collect() }
            runCurrent()
            model.saveSpot("Bedroom")
            runCurrent()
            advanceTimeBy(2000)
            runCurrent()
            coVerify(exactly = 0) { rooms.save(any()) }
            link.value = connection("Other home", "other-ap")
            runCurrent()
            advanceTimeBy(3000)
            runCurrent()
            coVerify(exactly = 0) { rooms.save(any()) }
            assertTrue(model.message.value!!.contains("stable reading"))
            model.saveSpot("Bedroom")
            runCurrent()
            advanceTimeBy(4000)
            runCurrent()
            coVerify(exactly = 1) { rooms.save(match { it.ssid == "Other home" && it.samples == 4 && it.averageRssi == -50 }) }
        } finally { backgroundScope.cancel(); ownedModel?.viewModelScope?.cancel(); runCurrent(); Dispatchers.resetMain() }
    }
}
