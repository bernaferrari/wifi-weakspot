package com.pulsewave.app.feature.lan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.model.lan.LanDevice
import com.pulsewave.app.core.tools.decodePortScan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LanDeviceDetailViewModel(private val dao: PulseWaveDao) : ViewModel() {
    private val _device = MutableStateFlow<LanDevice?>(null)
    val device = _device.asStateFlow()
    private val _portScanTime = MutableStateFlow<Long?>(null)
    val portScanTime = _portScanTime.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadDevice(ip: String) {
        viewModelScope.launch {
            val entity = dao.getLanDevice(ip)
            val scan = dao.getLatestPortScan(ip)
            _portScanTime.value = scan?.timestamp
            _device.value = entity?.let {
                LanDevice(it.ipAddress, it.macAddress, it.hostname, it.vendor, it.lastSeen,
                    it.isOnline, it.isTrusted, it.customName,
                    scan?.let { result -> decodePortScan(result.resultJson).map { entry -> entry.first } }.orEmpty())
            } ?: LanDevice(ip, null, null, null, 0L, false, false, null, emptyList())
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
