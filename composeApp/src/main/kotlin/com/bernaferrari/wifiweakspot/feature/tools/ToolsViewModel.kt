package com.bernaferrari.wifiweakspot.feature.tools

import com.bernaferrari.wifiweakspot.core.util.fixed
import com.bernaferrari.wifiweakspot.core.i18n.appStrings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.wifiweakspot.core.tools.*
import com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository
import com.bernaferrari.wifiweakspot.core.lan.lanNetworkKey
import com.bernaferrari.wifiweakspot.core.lan.sameLanConnection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private class ToolConnectionChanged : Exception()

class ToolsViewModel(
    private val pingClient: PingClient,
    private val dnsClient: DnsClient,
    private val portScanner: PortScanner,
    private val dao: com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao,
    private val networkStatusRepository: NetworkStatusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ToolsUiState>(ToolsUiState.Home)
    val uiState: StateFlow<ToolsUiState> = _uiState.asStateFlow()

    private var activeJob: Job? = null

    fun startTool(toolType: ToolType, target: String) {
        val normalized = target.trim()
        if (normalized.isBlank()) return
        if (normalized.startsWith("-") || normalized.any { it.isWhitespace() } || normalized.contains("://") || normalized.contains("/")) {
            _uiState.value = ToolsUiState.Error(appStrings.tools.invalidTarget)
            return
        }

        val previous = activeJob
        previous?.cancel()
        if (previous != null) portScanner.cancel()
        _uiState.value = ToolsUiState.Running(toolType, normalized)

        activeJob = viewModelScope.launch {
            try {
                previous?.join()
                when (toolType) {
                    ToolType.PING -> runPing(normalized)
                    ToolType.DNS -> runDns(normalized)
                    ToolType.PORT_SCAN -> runPortScan(normalized)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = ToolsUiState.Error(if (e is ToolConnectionChanged) appStrings.tools.connectionChangedRunAgain else appStrings.tools.diagnosticCouldNotComplete)
            }
        }
    }

    private suspend fun runPing(host: String) = kotlinx.coroutines.coroutineScope {
        val results = mutableListOf<String>()
        val collectorJob = launch {
            pingClient.ping(host).collect { result ->
                // Persisted diagnostic wire format; parsed by PingSummary and history. Do not translate.
                val line = if (result.isSuccess) {
                    "Reply from $host: time=${result.rttMs.fixed(1)} ms"
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
            com.bernaferrari.wifiweakspot.core.database.entities.ToolResultEntity(
                timestamp = com.bernaferrari.wifiweakspot.core.util.nowMillis(),
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

        // Save to DB
        dao.insertToolResult(
            com.bernaferrari.wifiweakspot.core.database.entities.ToolResultEntity(
                timestamp = com.bernaferrari.wifiweakspot.core.util.nowMillis(),
                toolType = "DNS",
                target = query,
                resultJson = lines.joinToString("\n"),
                isSuccess = result.isSuccess
            )
        )
        _uiState.value = ToolsUiState.DnsResult(query, lines)
    }

    private suspend fun runPortScan(host: String) {
        val connection = networkStatusRepository.getCurrentNetworkStatus().first()
        val result = portScanner.scanCommonPorts(host)
        if (!sameLanConnection(connection, networkStatusRepository.getCurrentNetworkStatus().first())) {
            throw ToolConnectionChanged()
        }
        val pairs = result.ports.filter { it.status == com.bernaferrari.wifiweakspot.core.model.tools.PortStatus.OPEN }.map { it.port to (it.service ?: UNKNOWN_SERVICE) }

        // Save to DB
        dao.insertToolResult(
            com.bernaferrari.wifiweakspot.core.database.entities.ToolResultEntity(
                timestamp = com.bernaferrari.wifiweakspot.core.util.nowMillis(),
                toolType = "PORT_SCAN",
                target = host,
                resultJson = encodePortScan(pairs),
                isSuccess = true,
                networkKey = lanNetworkKey(connection)
            )
        )
        _uiState.value = ToolsUiState.PortScanResult(host, pairs)
    }

    fun stopTool() {
        activeJob?.cancel()
        portScanner.cancel() // specific cancel for port scanner if needed
        _uiState.value = ToolsUiState.Home
    }

    fun reset() {
        activeJob?.cancel()
        portScanner.cancel()
        _uiState.value = ToolsUiState.Home
    }
}
