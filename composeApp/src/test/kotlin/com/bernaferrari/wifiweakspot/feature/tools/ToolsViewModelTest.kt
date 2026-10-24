package com.bernaferrari.wifiweakspot.feature.tools

import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.database.entities.ToolResultEntity
import com.bernaferrari.wifiweakspot.core.model.tools.*
import com.bernaferrari.wifiweakspot.core.tools.*
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ToolsViewModelTest {
    private val connection = com.bernaferrari.wifiweakspot.core.model.NetworkStatus.Disconnected.copy(
        isConnected = true, ssid = "Home", bssid = "aa:bb:cc:dd:ee:01", ipAddress = "192.168.1.2"
    )
    private fun status() = mockk<com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository> {
        every { getCurrentNetworkStatus(any()) } returns kotlinx.coroutines.flow.flowOf(connection)
    }

    @Test fun preservesDeviceTargetAndPersistsActualPortResult() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val scanner = mockk<PortScanner>()
            val dao = mockk<WifiWeakspotDao>(relaxed = true)
            val saved = slot<ToolResultEntity>()
            coEvery { scanner.scanCommonPorts("192.168.1.12") } returns PortScanResult("192.168.1.12",
                listOf(PortResult(8080, PortStatus.OPEN, "HTTP-Proxy", null), PortResult(22, PortStatus.CLOSED, "SSH", null)), "TCP Connect", 200)
            coEvery { dao.insertToolResult(capture(saved)) } just Runs
            val vm = ToolsViewModel(mockk(), mockk(), scanner, dao, status())
            vm.startTool(ToolType.PORT_SCAN, " 192.168.1.12 ")
            advanceUntilIdle()
            assertEquals(ToolsUiState.PortScanResult("192.168.1.12", listOf(8080 to "HTTP-Proxy")), vm.uiState.value)
            assertEquals("192.168.1.12", saved.captured.target)
            assertEquals("wifi:aa:bb:cc:dd:ee:01", saved.captured.networkKey)
            assertEquals(listOf(8080 to "HTTP-Proxy"), decodePortScan(saved.captured.resultJson))
        } finally { Dispatchers.resetMain() }
    }
    @Test fun doesNotOfferDoneBeforeMeasuredPortResultIsSaved() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val scanner = mockk<PortScanner>()
            val dao = mockk<WifiWeakspotDao>()
            val saved = CompletableDeferred<Unit>()
            coEvery { scanner.scanCommonPorts(any()) } returns PortScanResult("192.168.1.12", emptyList(), "TCP Connect", 10)
            coEvery { dao.insertToolResult(any()) } coAnswers { saved.await() }
            val vm = ToolsViewModel(mockk(), mockk(), scanner, dao, status())
            vm.startTool(ToolType.PORT_SCAN, "192.168.1.12")
            runCurrent()
            assertTrue(vm.uiState.value is ToolsUiState.Running)
            saved.complete(Unit)
            advanceUntilIdle()
            assertTrue(vm.uiState.value is ToolsUiState.PortScanResult)
        } finally { Dispatchers.resetMain() }
    }
    @Test fun invalidUrlsDoNotStartNetworkDiagnostics() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val scanner = mockk<PortScanner>()
            val vm = ToolsViewModel(mockk(), mockk(), scanner, mockk(), status())
            vm.startTool(ToolType.PORT_SCAN, "https://example.test/path")
            assertTrue(vm.uiState.value is ToolsUiState.Error)
            coVerify(exactly = 0) { scanner.scanCommonPorts(any()) }
        } finally { Dispatchers.resetMain() }
    }

    @Test fun changedConnectionDoesNotSavePortsForTheOriginalDevice() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val current = kotlinx.coroutines.flow.MutableStateFlow(connection)
            val repository = mockk<com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository> {
                every { getCurrentNetworkStatus(any()) } returns current
            }
            val scanner = mockk<PortScanner>()
            val result = CompletableDeferred<PortScanResult>()
            coEvery { scanner.scanCommonPorts(any()) } coAnswers { result.await() }
            val dao = mockk<WifiWeakspotDao>(relaxed = true)
            val vm = ToolsViewModel(mockk(), mockk(), scanner, dao, repository)
            vm.startTool(ToolType.PORT_SCAN, "192.168.1.12")
            runCurrent()
            current.value = connection.copy(bssid = "aa:bb:cc:dd:ee:02", ssid = "Office")
            result.complete(PortScanResult("192.168.1.12", emptyList(), "TCP Connect", 10))
            advanceUntilIdle()
            assertTrue(vm.uiState.value is ToolsUiState.Error)
            coVerify(exactly = 0) { dao.insertToolResult(any()) }
        } finally { Dispatchers.resetMain() }
    }
}
