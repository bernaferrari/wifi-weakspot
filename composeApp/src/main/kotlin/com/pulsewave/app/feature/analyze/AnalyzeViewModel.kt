package com.pulsewave.app.feature.analyze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.wifi.WifiScanner
import com.pulsewave.app.core.wifi.frequencyToChannel
import com.pulsewave.app.core.wifi.ThrottleState
import com.pulsewave.app.core.export.ExportManager
import com.pulsewave.app.core.export.WifiExportData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class AnalyzeViewModel(
    private val wifiScanner: WifiScanner,
    private val throttleManager: com.pulsewave.app.core.wifi.ScanThrottleManager,
    private val dao: com.pulsewave.app.core.database.dao.PulseWaveDao,
    private val exportManager: ExportManager,
    networkStatusRepository: com.pulsewave.app.core.domain.NetworkStatusRepository
) : ViewModel() {

    /** The network this phone is on, so the list and advice can point it out. */
    val connected: StateFlow<com.pulsewave.app.core.model.NetworkStatus> = networkStatusRepository.getCurrentNetworkStatus()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.pulsewave.app.core.model.NetworkStatus.Disconnected)

    // BSSID -> History of RSSI (single source of truth, updated only in init)
    private val signalHistory = SignalHistory()
    private val _history = MutableStateFlow<Map<String, List<Int>>>(emptyMap())

    // Pure combination - no side effects
    val uiState: StateFlow<AnalyzeUiState> = combine(
        wifiScanner.scanResults,
        wifiScanner.isScanning,
        _history,
        throttleManager.throttleState
    ) { results, isScanning, history, throttle ->
        when {
            results.isEmpty() && isScanning -> AnalyzeUiState.Loading
            results.isNotEmpty() -> AnalyzeUiState.Success(
                networks = results.sortedByDescending { it.level },
                isScanning = isScanning,
                history = history,
                throttleState = throttle
            )
            isScanning -> AnalyzeUiState.Loading
            else -> AnalyzeUiState.Empty(throttleState = throttle)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyzeUiState.Empty()
    )

    init {
        // Single place for history updates and DB persistence
        viewModelScope.launch {
            wifiScanner.scanResults.collect { results ->
                if (results.isEmpty()) return@collect

                val observations = signalHistory.record(results)
                if (observations.isEmpty()) return@collect
                _history.value = signalHistory.snapshot()
                persistScanResults(observations)
            }
        }
    }

    private suspend fun persistScanResults(results: List<WifiNetwork>) {
        withContext(Dispatchers.IO) {
            val rows = results.map { network ->
                com.pulsewave.app.core.database.entities.WifiScanResultEntity(
                    timestamp = network.timestamp,
                    ssid = network.ssid,
                    bssid = network.bssid,
                    rssi = network.rssi,
                    frequency = network.frequency,
                    channelWidth = network.channelWidth,
                    security = network.security
                )
            }
            try {
                dao.insertWifiScans(rows)
                dao.trimWifiScans(keep = 500)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun startScan() {
        if (throttleManager.throttleState.value is ThrottleState.Exhausted) return
        if (wifiScanner.startScan()) {
            throttleManager.recordScan()
        }
    }

    fun stopScan() {
        wifiScanner.stopScan()
    }

    fun exportToCsv(networks: List<WifiNetwork>) {
        viewModelScope.launch {
            val data = networks.map { it.toExportData() }
            val fileName = "wifi_scan_${timestamp()}"
            exportManager.exportWifiToCsv(data, fileName)
        }
    }

    fun exportToJson(networks: List<WifiNetwork>) {
        viewModelScope.launch {
            val data = networks.map { it.toExportData() }
            val fileName = "wifi_scan_${timestamp()}"
            exportManager.exportWifiToJson(data, fileName)
        }
    }

    fun shareSummary(networks: List<WifiNetwork>) {
        viewModelScope.launch {
            val summary = buildString {
                appendLine("PulseWave WiFi Scan Report")
                appendLine("=========================")
                appendLine("Time: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())}")
                appendLine("Total Networks: ${networks.size}")
                appendLine()
                appendLine("Networks:")
                networks.sortedByDescending { it.rssi }.take(15).forEach { n ->
                    val ch = frequencyToChannel(n.frequency)
                    appendLine("• ${n.ssid.ifEmpty { "Hidden" }} - ${n.rssi}dBm, Ch $ch, ${n.security}")
                }
            }
            exportManager.shareReport(summary, "WiFi Scan Report")
        }
    }

    private fun timestamp(): String = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
}

private fun WifiNetwork.toExportData() = WifiExportData(
    ssid = ssid,
    bssid = bssid,
    rssi = rssi,
    frequency = frequency,
    channel = frequencyToChannel(frequency),
    security = security,
    timestamp = timestamp
)

sealed interface AnalyzeUiState {
    data object Loading : AnalyzeUiState
    data class Empty(
        val throttleState: com.pulsewave.app.core.wifi.ThrottleState = com.pulsewave.app.core.wifi.ThrottleState.Unknown
    ) : AnalyzeUiState
    data class Success(
        val networks: List<WifiNetwork>,
        val isScanning: Boolean,
        val history: Map<String, List<Int>> = emptyMap(),
        val throttleState: com.pulsewave.app.core.wifi.ThrottleState = com.pulsewave.app.core.wifi.ThrottleState.Unknown
    ) : AnalyzeUiState
}
