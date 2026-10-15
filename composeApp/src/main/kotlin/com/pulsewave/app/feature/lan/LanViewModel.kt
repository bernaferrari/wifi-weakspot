package com.pulsewave.app.feature.lan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.database.entities.LanDeviceEntity
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.lan.LanScanner
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.model.lan.LanDevice
import com.pulsewave.app.core.util.nowMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LanUiState {
    data object Idle : LanUiState
    data object Scanning : LanUiState
    data object Cancelled : LanUiState
    data class Success(val devices: List<LanDevice>) : LanUiState
    data class Error(val message: String) : LanUiState
}

class LanViewModel(
    private val lanScanner: LanScanner,
    private val dao: PulseWaveDao,
    networkStatusRepository: NetworkStatusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LanUiState>(LanUiState.Idle)
    val uiState: StateFlow<LanUiState> = _uiState.asStateFlow()

    /** Devices from the last scan, shown while a new one runs. */
    private val _known = MutableStateFlow<List<LanDevice>>(emptyList())
    val known: StateFlow<List<LanDevice>> = _known.asStateFlow()

    val progress: StateFlow<Float> = lanScanner.scanProgress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    /** Gateway and own address, so the router and this phone can be named. */
    val status: StateFlow<NetworkStatus> = networkStatusRepository.getCurrentNetworkStatus(refreshIntervalMs = 5_000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NetworkStatus.Disconnected)

    private var scanJob: Job? = null

    init {
        viewModelScope.launch {
            val saved = runCatching { dao.getAllLanDevices().first() }.getOrDefault(emptyList())
            // Only the most recent scan; older sessions may describe a different network.
            val latest = saved.maxOfOrNull { it.lastSeen } ?: return@launch
            if (_known.value.isEmpty()) {
                _known.value = saved.filter { latest - it.lastSeen <= 2 * 60_000L }.map { it.toDevice() }
            }
        }
    }

    fun startScan() {
        if (scanJob?.isCompleted == false) return
        scanJob = viewModelScope.launch {
            _uiState.value = LanUiState.Scanning
            try {
                val result = lanScanner.scanSubnet()
                val devices = result.devices.map { device ->
                    val saved = dao.getLanDevice(device.ipAddress)
                    device.copy(
                        customName = saved?.customName,
                        isTrusted = saved?.isTrusted ?: false,
                        hostname = device.hostname ?: saved?.hostname,
                        // Devices don't always answer every scan; keep what they said last time.
                        announcedName = device.announcedName ?: saved?.announcedName,
                        model = device.model ?: saved?.model,
                        services = device.services.ifEmpty { saved?.services?.split(',')?.filter { it.isNotBlank() }.orEmpty() }
                    )
                }
                val seenAt = nowMillis()
                devices.forEach { device ->
                    dao.insertLanDevice(
                        LanDeviceEntity(
                            ipAddress = device.ipAddress,
                            macAddress = device.macAddress,
                            hostname = device.hostname,
                            vendor = device.vendor,
                            lastSeen = seenAt,
                            isOnline = device.isOnline,
                            isTrusted = device.isTrusted,
                            customName = device.customName,
                            announcedName = device.announcedName,
                            model = device.model,
                            services = device.services.joinToString(",").ifEmpty { null }
                        )
                    )
                }
                _known.value = devices
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

internal fun LanDeviceEntity.toDevice(openPorts: List<Int> = emptyList()) =
    LanDevice(
        ipAddress, macAddress, hostname, vendor, lastSeen, isOnline, isTrusted, customName, openPorts,
        announcedName = announcedName,
        model = model,
        services = services?.split(',')?.filter { it.isNotBlank() }.orEmpty()
    )
