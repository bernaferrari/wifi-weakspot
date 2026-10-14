package com.pulsewave.app.feature.tools

import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.database.entities.ToolResultEntity
import com.pulsewave.app.core.model.tools.*
import com.pulsewave.app.core.tools.*
import io.mockk.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ToolsViewModelTest {
    @Test fun preservesDeviceTargetAndPersistsActualPortResult() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val scanner = mockk<PortScanner>()
            val dao = mockk<PulseWaveDao>(relaxed = true)
            val saved = slot<ToolResultEntity>()
            coEvery { scanner.scanCommonPorts("192.168.1.12") } returns PortScanResult("192.168.1.12",
                listOf(PortResult(8080, PortStatus.OPEN, "HTTP-Proxy", null), PortResult(22, PortStatus.CLOSED, "SSH", null)), "TCP Connect", 200)
            coEvery { dao.insertToolResult(capture(saved)) } just Runs
            val vm = ToolsViewModel(mockk(), mockk(), scanner, dao)
            vm.startTool(ToolType.PORT_SCAN, " 192.168.1.12 ")
            advanceUntilIdle()
            assertEquals(ToolsUiState.PortScanResult("192.168.1.12", listOf(8080 to "HTTP-Proxy")), vm.uiState.value)
            assertEquals("192.168.1.12", saved.captured.target)
            assertEquals(listOf(8080 to "HTTP-Proxy"), decodePortScan(saved.captured.resultJson))
        } finally { Dispatchers.resetMain() }
    }
    @Test fun doesNotOfferDoneBeforeMeasuredPortResultIsSaved() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val scanner = mockk<PortScanner>()
            val dao = mockk<PulseWaveDao>()
            val saved = CompletableDeferred<Unit>()
            coEvery { scanner.scanCommonPorts(any()) } returns PortScanResult("192.168.1.12", emptyList(), "TCP Connect", 10)
            coEvery { dao.insertToolResult(any()) } coAnswers { saved.await() }
            val vm = ToolsViewModel(mockk(), mockk(), scanner, dao)
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
            val vm = ToolsViewModel(mockk(), mockk(), scanner, mockk())
            vm.startTool(ToolType.PORT_SCAN, "https://example.test/path")
            assertTrue(vm.uiState.value is ToolsUiState.Error)
            coVerify(exactly = 0) { scanner.scanCommonPorts(any()) }
        } finally { Dispatchers.resetMain() }
    }
}
