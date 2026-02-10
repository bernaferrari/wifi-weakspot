package com.pulsewave.app.feature.analyze

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.wifi.WifiScanner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AnalyzeViewModel(
    private val wifiScanner: WifiScanner,
    private val throttleManager: com.pulsewave.app.core.wifi.ScanThrottleManager,
    private val dao: com.pulsewave.app.core.database.dao.PulseWaveDao
) : ViewModel() {

    // BSSID -> History of RSSI
    private val _history = MutableStateFlow<Map<String, List<Int>>>(emptyMap())

    val uiState: StateFlow<AnalyzeUiState> = combine(
        wifiScanner.scanResults,
        wifiScanner.isScanning,
        _history,
        throttleManager.throttleState
    ) { results, isScanning, history, throttle ->
        // Update history side-effect (a bit hacky for VM, but simple for now)
        if (results.isNotEmpty()) {
            val currentHistory = _history.value.toMutableMap()
            results.forEach { network ->
                val list = currentHistory.getOrPut(network.bssid) { emptyList() }
                currentHistory[network.bssid] = list + network.rssi
            }
        }
        
        if (results.isEmpty() && isScanning) {
            AnalyzeUiState.Loading
        } else if (results.isNotEmpty()) {
            AnalyzeUiState.Success(
                networks = results.sortedByDescending { it.level },
                isScanning = isScanning,
                history = history,
                throttleState = throttle
            )
        } else if (isScanning) {
            AnalyzeUiState.Loading
        } else {
             AnalyzeUiState.Empty(throttleState = throttle)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AnalyzeUiState.Loading
    )
    
    // History collection happens in the next init block

    init {
        // Collect results to update history
        viewModelScope.launch {
            wifiScanner.scanResults.collect { results ->
                 val currentHistory = _history.value.toMutableMap()
                 results.forEach { network ->
                      val list = currentHistory.getOrPut(network.bssid) { emptyList() }
                      // Limit history size
                      val newList = (list + network.rssi).takeLast(100)
                      currentHistory[network.bssid] = newList
                      
                      // Persist to DB
                      dao.insertWifiScan(
                          com.pulsewave.app.core.database.entities.WifiScanResultEntity(
                              timestamp = System.currentTimeMillis(),
                              ssid = network.ssid,
                              bssid = network.bssid,
                              rssi = network.rssi,
                              frequency = network.frequency,
                              channelWidth = network.channelWidth,
                              security = network.security
                          )
                      )
                 }
                 _history.value = currentHistory
            }
        }
    }

    fun startScan() {
        throttleManager.recordScan()
        wifiScanner.startScan()
    }

    fun stopScan() {
        wifiScanner.stopScan()
    }
}

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
