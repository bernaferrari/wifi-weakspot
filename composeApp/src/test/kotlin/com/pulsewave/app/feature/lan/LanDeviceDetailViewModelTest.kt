package com.pulsewave.app.feature.lan

import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.database.entities.*
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LanDeviceDetailViewModelTest {
    @Test fun missingDeviceNeverClaimsOnlineOrInventsPorts() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val dao = mockk<PulseWaveDao>()
            coEvery { dao.getLanDevice(any()) } returns null
            coEvery { dao.getLatestPortScan(any()) } returns null
            val vm = LanDeviceDetailViewModel(dao)
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
            val dao = mockk<PulseWaveDao>(relaxed = true)
            val ip = "192.168.1.4"
            coEvery { dao.getLanDevice(ip) } returns LanDeviceEntity(ip, null, null, null, 10L, true)
            coEvery { dao.getLatestPortScan(ip) } returns ToolResultEntity(timestamp = 20L, toolType = "PORT_SCAN", target = ip, resultJson = "8080:HTTP-Proxy", isSuccess = true)
            val vm = LanDeviceDetailViewModel(dao)
            vm.loadDevice(ip)
            advanceUntilIdle()
            assertEquals(listOf(8080), vm.device.value!!.openPorts)
            assertEquals(20L, vm.portScanTime.value)
            vm.rename(" Office printer ")
            advanceUntilIdle()
            coVerify { dao.renameLanDevice(ip, "Office printer") }
            assertEquals("Office printer", vm.device.value!!.displayName)
        } finally { Dispatchers.resetMain() }
    }
}
