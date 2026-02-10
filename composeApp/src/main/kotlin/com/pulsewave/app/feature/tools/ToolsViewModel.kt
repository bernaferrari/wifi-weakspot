package com.pulsewave.app.feature.tools

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.tools.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ToolsViewModel(
    private val pingClient: PingClient,
    private val dnsClient: DnsClient,
    private val portScanner: PortScanner,
    private val dao: com.pulsewave.app.core.database.dao.PulseWaveDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<ToolsUiState>(ToolsUiState.Home)
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    private var activeJob: Job? = null

    fun startTool(toolType: ToolType, target: String) {
        if (target.isBlank()) return
        
        activeJob?.cancel()
        _uiState.value = ToolsUiState.Running(toolType, target)

        activeJob = viewModelScope.launch {
            when (toolType) {
                ToolType.PING -> runPing(target)
                ToolType.DNS -> runDns(target)
                ToolType.PORT_SCAN -> runPortScan(target)
            }
        }
    }

    private suspend fun runPing(host: String) = kotlinx.coroutines.coroutineScope {
        val results = mutableListOf<String>()
        val collectorJob = launch {
            pingClient.ping(host).collect { result ->
                val line = if (result.isSuccess) {
                    "Reply from $host: time=%.1f ms".format(result.rttMs)
                } else {
                    "Request timed out: ${result.error}"
                }
                results.add(line)
                _uiState.value = ToolsUiState.PingResult(host, results.toList())
            }
        }
        collectorJob.join() // Wait for flow to complete
        
        // Save to DB
        dao.insertToolResult(
            com.pulsewave.app.core.database.entities.ToolResultEntity(
                timestamp = System.currentTimeMillis(),
                toolType = "PING",
                target = host,
                resultJson = results.joinToString("\n"),
                isSuccess = results.any { it.startsWith("Reply") }
            )
        )
    }

    private suspend fun runDns(query: String) {
        val result = dnsClient.lookup(query)
        val lines = if (result.isSuccess) {
            result.records.map { "${it.type}: ${it.value}" }
        } else {
            listOf("Error: ${result.error}")
        }
        _uiState.value = ToolsUiState.DnsResult(query, lines)
        
        // Save to DB
        dao.insertToolResult(
            com.pulsewave.app.core.database.entities.ToolResultEntity(
                timestamp = System.currentTimeMillis(),
                toolType = "DNS",
                target = query,
                resultJson = lines.joinToString("\n"),
                isSuccess = result.isSuccess
            )
        )
    }

    private suspend fun runPortScan(host: String) {
        val result = portScanner.scanCommonPorts(host)
        val pairs = result.ports.map { it.port to (it.service ?: "unknown") }
        _uiState.value = ToolsUiState.PortScanResult(host, pairs)
        
        // Save to DB
        dao.insertToolResult(
            com.pulsewave.app.core.database.entities.ToolResultEntity(
                timestamp = System.currentTimeMillis(),
                toolType = "PORT_SCAN",
                target = host,
                resultJson = pairs.joinToString(",") { "${it.first}:${it.second}" },
                isSuccess = true
            )
        )
    }

    fun stopTool() {
        activeJob?.cancel()
        portScanner.cancel() // specific cancel for port scanner if needed
        _uiState.value = ToolsUiState.Home
    }

    fun reset() {
        activeJob?.cancel()
        _uiState.value = ToolsUiState.Home
    }
}
