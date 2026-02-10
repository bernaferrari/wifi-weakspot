package com.pulsewave.app.feature.lan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.model.lan.LanDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LanDeviceDetailViewModel(
    private val dao: com.pulsewave.app.core.database.dao.PulseWaveDao
) : ViewModel() {

    private val _device = MutableStateFlow<LanDevice?>(null)
    val device: StateFlow<LanDevice?> = _device.asStateFlow()

    fun loadDevice(ip: String) {
        viewModelScope.launch {
            val entity = dao.getLanDevice(ip)
            if (entity != null) {
                _device.value = LanDevice(
                    ipAddress = entity.ipAddress,
                    macAddress = entity.macAddress,
                    hostname = entity.hostname,
                    vendor = entity.vendor,
                    lastSeen = entity.lastSeen,
                    isOnline = entity.isOnline,
                    isTrusted = entity.isTrusted,
                    customName = entity.customName,
                    openPorts = emptyList() // Needs port scan for real data
                )
            } else {
                // Return a placeholder with the IP if not in DB yet
                _device.value = LanDevice(
                    ipAddress = ip,
                    macAddress = null,
                    hostname = null,
                    vendor = null,
                    lastSeen = System.currentTimeMillis(),
                    isOnline = true,
                    isTrusted = false,
                    customName = null,
                    openPorts = emptyList()
                )
            }
        }
    }
}
