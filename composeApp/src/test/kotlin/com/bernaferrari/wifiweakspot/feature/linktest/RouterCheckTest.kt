package com.bernaferrari.wifiweakspot.feature.linktest

import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.database.entities.SpeedTestResultEntity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository
import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import com.bernaferrari.wifiweakspot.core.rooms.*
import com.bernaferrari.wifiweakspot.core.router.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RouterCheckTest {
    private fun connection(ssid: String = "Home") = NetworkStatus(ssid, "ap-$ssid", 5180, 36, -50, 400,
        "192.168.1.2", true, "WPA2", gateway = "192.168.1.1")

    @Test fun savesOnlyResponseAndTagsExactResultWithOriginalNetwork() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var ownedModel: ViewModel? = null
        try {
            val link = MutableStateFlow(connection())
            val repository = mockk<NetworkStatusRepository>()
            every { repository.getCurrentNetworkStatus(any()) } returns link
            val dao = mockk<WifiWeakspotDao>(relaxed = true)
            every { dao.getSpeedTestHistory() } returns MutableStateFlow(emptyList())
            val saved = slot<SpeedTestResultEntity>()
            coEvery { dao.insertSpeedTest(capture(saved)) } returns 42L
            val rooms = mockk<RoomCheckRepository>(relaxed = true)
            every { rooms.readings } returns MutableStateFlow(emptyList())
            val probe = mockk<RouterProbe>()
            every { probe.ping(any(), any(), any()) } returns flowOf(PingEvent.Reply(1, 3f), PingEvent.Finished(1, 1))
            val model = LinkTestViewModel(repository, probe, dao, rooms)
            ownedModel = model
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.status.collect() }
            runCurrent()
            model.start()
            runCurrent()
            assertTrue(model.state.value is LinkTestState.Done)
            assertEquals(-1f, saved.captured.uploadMbps)
            assertEquals(-1f, saved.captured.downloadMbps)
            assertEquals(3f, saved.captured.pingMs)
            link.value = connection("Other")
            runCurrent()
            model.tagRoom("Bedroom")
            runCurrent()
            coVerify { dao.tagSpeedTest(42L, "Bedroom") }
            coVerify { rooms.save(match { it.ssid == "Home" && it.bssid == "ap-Home" && it.speedMbps == null }) }
        } finally { backgroundScope.cancel(); ownedModel?.viewModelScope?.cancel(); runCurrent(); Dispatchers.resetMain() }
    }

    @Test fun changedConnectionDoesNotProduceAResult() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        var ownedModel: ViewModel? = null
        try {
            val link = MutableStateFlow(connection())
            val repository = mockk<NetworkStatusRepository>()
            every { repository.getCurrentNetworkStatus(any()) } returns link
            val dao = mockk<WifiWeakspotDao>(relaxed = true)
            every { dao.getSpeedTestHistory() } returns MutableStateFlow(emptyList())
            val rooms = mockk<RoomCheckRepository>()
            every { rooms.readings } returns MutableStateFlow(emptyList())
            val probe = mockk<RouterProbe>()
            every { probe.ping(any(), any(), any()) } returns flow {
                emit(PingEvent.Reply(1, 3f))
                delay(1000)
                emit(PingEvent.Finished(1, 1))
            }
            val model = LinkTestViewModel(repository, probe, dao, rooms)
            ownedModel = model
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { model.status.collect() }
            runCurrent()
            model.start()
            runCurrent()
            link.value = NetworkStatus.Disconnected
            runCurrent()
            advanceTimeBy(1000)
            runCurrent()
            assertTrue(model.state.value is LinkTestState.Failed)
            coVerify(exactly = 0) { dao.insertSpeedTest(any()) }
        } finally { backgroundScope.cancel(); ownedModel?.viewModelScope?.cancel(); runCurrent(); Dispatchers.resetMain() }
    }
}
