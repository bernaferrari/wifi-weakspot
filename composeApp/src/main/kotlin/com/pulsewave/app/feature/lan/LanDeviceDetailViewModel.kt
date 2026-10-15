package com.pulsewave.app.feature.lan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.model.lan.LanDevice
import com.pulsewave.app.core.tools.decodePortScan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class LanDeviceDetailViewModel(
    private val dao: PulseWaveDao,
    networkStatusRepository: NetworkStatusRepository
) : ViewModel() {
    private val _device = MutableStateFlow<LanDevice?>(null)
    val device = _device.asStateFlow()
    private val _portScanTime = MutableStateFlow<Long?>(null)
    val portScanTime = _portScanTime.asStateFlow()
    /** Open ports from the last check, with their usual service names. */
    private val _ports = MutableStateFlow<List<Pair<Int, String>>>(emptyList())
    val ports = _ports.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    val status: StateFlow<NetworkStatus> = networkStatusRepository.getCurrentNetworkStatus(refreshIntervalMs = 10_000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NetworkStatus.Disconnected)

    fun loadDevice(ip: String) {
        viewModelScope.launch {
            val entity = dao.getLanDevice(ip)
            val scan = dao.getLatestPortScan(ip)
            val ports = scan?.let { decodePortScan(it.resultJson) }.orEmpty()
            _portScanTime.value = scan?.timestamp
            _ports.value = ports
            _device.value = entity?.toDevice(ports.map { it.first })
                ?: LanDevice(ip, null, null, null, 0L, false, false, null, emptyList())
        }
    }

    fun rename(name: String) {
        val device = _device.value ?: return
        viewModelScope.launch {
            if (dao.getLanDevice(device.ipAddress) == null) {
                _error.value = "Scan this device before saving a name."
            } else {
                val customName = name.trim().takeIf { it.isNotEmpty() }
                dao.renameLanDevice(device.ipAddress, customName)
                _device.value = device.copy(customName = customName)
                _error.value = null
            }
        }
    }
}
