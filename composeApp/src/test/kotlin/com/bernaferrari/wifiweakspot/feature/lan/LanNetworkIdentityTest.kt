package com.bernaferrari.wifiweakspot.feature.lan

import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.database.entities.LanDeviceEntity
import com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository
import com.bernaferrari.wifiweakspot.core.lan.LanScanner
import com.bernaferrari.wifiweakspot.core.lan.deviceWebUrl
import com.bernaferrari.wifiweakspot.core.lan.lanNetworkKey
import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import com.bernaferrari.wifiweakspot.core.model.lan.LanDevice
import com.bernaferrari.wifiweakspot.core.model.lan.LanScanResult
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LanNetworkIdentityTest {
    private val home = NetworkStatus.Disconnected.copy(isConnected = true, ssid = "Home",
        bssid = "aa:bb:cc:dd:ee:01", ipAddress = "192.168.1.2", gateway = "192.168.1.1")
    private val office = home.copy(ssid = "Office", bssid = "aa:bb:cc:dd:ee:02")
    private val ip = "192.168.1.12"
    private val device = LanDevice(ip, null, null, null, 100L, true, false, null, emptyList())
    private fun result() = LanScanResult(1L, 100L, "192.168.1.0/24", listOf(device), 10L)
    private fun scanner() = mockk<LanScanner>(relaxed = true) {
        every { scanProgress } returns flowOf(0f)
    }
    private fun repository(connection: MutableStateFlow<NetworkStatus>) = mockk<NetworkStatusRepository> {
        every { getCurrentNetworkStatus(any()) } returns connection
    }

    @Test fun samePrivateIpOnAnotherNetworkDoesNotInheritNameTrustOrServices() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val savedHome = LanDeviceEntity(ip, null, "home-printer", null, 100L, true,
                isTrusted = true, customName = "My printer", services = "_printer._tcp", networkKey = lanNetworkKey(home)!!)
            val dao = mockk<WifiWeakspotDao>(relaxed = true)
            every { dao.getAllLanDevices() } returns flowOf(listOf(savedHome))
            coEvery { dao.getLanDevice(ip, lanNetworkKey(office)!!) } returns null
            val inserted = slot<LanDeviceEntity>()
            coEvery { dao.insertLanDevice(capture(inserted)) } just Runs
            val scanner = scanner()
            coEvery { scanner.scanSubnet(any()) } returns result()
            val vm = LanViewModel(scanner, dao, repository(MutableStateFlow(office)))
            runCurrent()
            assertTrue(vm.known.value.isEmpty())
            vm.startScan()
            advanceUntilIdle()
            assertNull(inserted.captured.customName)
            assertFalse(inserted.captured.isTrusted)
            assertNull(inserted.captured.hostname)
            assertNull(inserted.captured.services)
            assertEquals(lanNetworkKey(office), inserted.captured.networkKey)
            coVerify(exactly = 0) { dao.getLanDevice(ip, lanNetworkKey(home)!!) }
        } finally { Dispatchers.resetMain() }
    }

    @Test fun connectionChangeClearsSavedListAndDiscardsRunningScan() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val connection = MutableStateFlow(home)
            val dao = mockk<WifiWeakspotDao>(relaxed = true)
            every { dao.getAllLanDevices() } returns flowOf(listOf(
                LanDeviceEntity(ip, null, "printer", null, 100L, true, networkKey = lanNetworkKey(home)!!)))
            val completed = CompletableDeferred<LanScanResult>()
            val scanner = scanner()
            coEvery { scanner.scanSubnet(any()) } coAnswers { completed.await() }
            val vm = LanViewModel(scanner, dao, repository(connection))
            runCurrent()
            assertEquals(1, vm.known.value.size)
            vm.startScan()
            runCurrent()
            connection.value = office
            runCurrent()
            completed.complete(result())
            advanceUntilIdle()
            assertTrue(vm.known.value.isEmpty())
            assertEquals(LanUiState.Idle, vm.uiState.value)
            coVerify(exactly = 0) { dao.insertLanDevice(any()) }
        } finally { Dispatchers.resetMain() }
    }

    @Test fun redactedIdentityNeverReusesPersistentDeviceIdentity() {
        assertNull(lanNetworkKey(home.copy(bssid = "02:00:00:00:00:00")))
        assertNull(lanNetworkKey(home.copy(bssid = "")))
        assertNull(lanNetworkKey(NetworkStatus.Disconnected))
        assertNotEquals(lanNetworkKey(home), lanNetworkKey(home.copy(bssid = office.bssid)))
        assertEquals(lanNetworkKey(home), lanNetworkKey(home.copy(bssid = home.bssid.uppercase())))
    }

    @Test fun webPageUsesActualAlternatePort() {
        assertEquals("http://$ip:8080", deviceWebUrl(ip, listOf(8080)))
        assertEquals("https://$ip:8443", deviceWebUrl(ip, listOf(8443)))
        assertEquals("https://$ip", deviceWebUrl(ip, listOf(443)))
        assertEquals("http://$ip", deviceWebUrl(ip, emptyList(), router = true))
        assertNull(deviceWebUrl(ip, listOf(22)))
    }
}
