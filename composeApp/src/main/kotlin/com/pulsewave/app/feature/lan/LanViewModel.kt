package com.pulsewave.app.feature.lan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.lan.LanScanner
import com.pulsewave.app.core.model.lan.LanDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException

sealed interface LanUiState {
    data object Idle : LanUiState
    data object Scanning : LanUiState
    data object Cancelled : LanUiState
    data class Success(val devices: List<LanDevice>) : LanUiState
    data class Error(val message: String) : LanUiState
}

class LanViewModel(
    private val lanScanner: LanScanner,
    private val dao: com.pulsewave.app.core.database.dao.PulseWaveDao
) : ViewModel() {

    private val _uiState = MutableStateFlow<LanUiState>(LanUiState.Idle)
    val uiState: StateFlow<LanUiState> = _uiState.asStateFlow()

    private var scanJob: Job? = null

    fun startScan() {
        if (scanJob?.isCompleted == false) return
        scanJob = viewModelScope.launch {
            _uiState.value = LanUiState.Scanning
            try {
                val result = lanScanner.scanSubnet()

                // Save to DB
                val devices = result.devices.map { device ->
                    val saved = dao.getLanDevice(device.ipAddress)
                    device.copy(customName = saved?.customName, isTrusted = saved?.isTrusted ?: false, hostname = device.hostname ?: saved?.hostname)
                }
                devices.forEach { device ->
                    dao.insertLanDevice(
                        com.pulsewave.app.core.database.entities.LanDeviceEntity(
                            ipAddress = device.ipAddress,
                            macAddress = device.macAddress,
                            hostname = device.hostname,
                            vendor = device.vendor,
                            lastSeen = System.currentTimeMillis(),
                            isOnline = device.isOnline,
                            isTrusted = device.isTrusted,
                            customName = device.customName
                        )
                    )
                }
                _uiState.value = LanUiState.Success(devices)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = LanUiState.Error(e.message ?: "Scan failed")
            }
        }
    }

    fun stopScan() {
        scanJob?.cancel()
        lanScanner.cancel()
        if (_uiState.value is LanUiState.Scanning) {
            _uiState.value = LanUiState.Cancelled
        }
    }
}
