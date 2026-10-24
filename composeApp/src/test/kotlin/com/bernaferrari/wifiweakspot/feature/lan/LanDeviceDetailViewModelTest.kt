package com.bernaferrari.wifiweakspot.feature.lan

import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.database.entities.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LanDeviceDetailViewModelTest {
    private val key = "wifi:aa:bb:cc:dd:ee:01"
    private val connection = com.bernaferrari.wifiweakspot.core.model.NetworkStatus.Disconnected.copy(
        isConnected = true, ssid = "Home", bssid = "aa:bb:cc:dd:ee:01", ipAddress = "192.168.1.2"
    )
    private fun status() = mockk<com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository> {
        every { getCurrentNetworkStatus(any()) } returns kotlinx.coroutines.flow.flowOf(connection)
    }

    @Test fun missingDeviceNeverClaimsOnlineOrInventsPorts() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dao = mockk<WifiWeakspotDao>()
            coEvery { dao.getLanDevice(any(), any()) } returns null
            coEvery { dao.getLatestPortScan(any(), any()) } returns null
            val vm = LanDeviceDetailViewModel(dao, status())
            vm.loadDevice("192.168.1.4")
            advanceUntilIdle()
            assertFalse(vm.device.value!!.isOnline)
            assertEquals(0L, vm.device.value!!.lastSeen)
            assertTrue(vm.device.value!!.openPorts.isEmpty())
            assertNull(vm.portScanTime.value)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun loadsMeasuredPortsAndSavesCustomName() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dao = mockk<WifiWeakspotDao>(relaxed = true)
            val ip = "192.168.1.4"
            coEvery { dao.getLanDevice(ip, key) } returns LanDeviceEntity(ip, null, null, null, 10L, true)
            coEvery { dao.getLatestPortScan(ip, key) } returns ToolResultEntity(timestamp = 20L, toolType = "PORT_SCAN", target = ip, resultJson = "8080:HTTP-Proxy", isSuccess = true)
            val vm = LanDeviceDetailViewModel(dao, status())
            vm.loadDevice(ip)
            advanceUntilIdle()
            assertEquals(listOf(8080), vm.device.value!!.openPorts)
            assertEquals(20L, vm.portScanTime.value)
            vm.rename(" Office printer ")
            advanceUntilIdle()
            coVerify { dao.renameLanDevice(ip, key, "Office printer") }
            assertEquals("Office printer", vm.device.value!!.displayName)
        } finally { Dispatchers.resetMain() }
    }
}
