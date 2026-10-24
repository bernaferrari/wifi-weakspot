package com.bernaferrari.wifiweakspot.feature.lan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository
import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import com.bernaferrari.wifiweakspot.core.model.lan.LanDevice
import com.bernaferrari.wifiweakspot.core.lan.lanNetworkKey
import com.bernaferrari.wifiweakspot.core.lan.sameLanConnection
import com.bernaferrari.wifiweakspot.core.tools.decodePortScan
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class LanDeviceDetailViewModel(
    private val dao: WifiWeakspotDao,
    private val networkStatusRepository: NetworkStatusRepository
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
        .stateIn(viewModelScope, SharingStarted.Eagerly, NetworkStatus.Disconnected)
    private var loadedConnection: NetworkStatus? = null
    private var loadJob: Job? = null
    private var targetIp: String? = null

    init {
        viewModelScope.launch {
            status.collect { current ->
                val loaded = loadedConnection
                if (loaded != null && !sameLanConnection(loaded, current)) targetIp?.let(::loadDevice)
            }
        }
    }

    fun loadDevice(ip: String) {
        targetIp = ip
        loadJob?.cancel()
        _device.value = null
        _ports.value = emptyList()
        _portScanTime.value = null
        loadJob = viewModelScope.launch {
            try {
                val connection = networkStatusRepository.getCurrentNetworkStatus().first()
                loadedConnection = connection
                val key = lanNetworkKey(connection)
                val entity = key?.let { dao.getLanDevice(ip, it) }
                val scan = key?.let { dao.getLatestPortScan(ip, it) }
                if (!sameLanConnection(connection, networkStatusRepository.getCurrentNetworkStatus().first())) {
                    _device.value = LanDevice(ip, null, null, null, 0L, false, false, null, emptyList())
                    _error.value = "The connection changed. Open this device again on the new network."
                    return@launch
                }
                val ports = scan?.let { decodePortScan(it.resultJson) }.orEmpty()
                _portScanTime.value = scan?.timestamp
                _ports.value = ports
                _device.value = entity?.toDevice(ports.map { it.first })
                    ?: LanDevice(ip, null, null, null, 0L, false, false, null, emptyList())
                _error.value = null
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) {
                _device.value = LanDevice(ip, null, null, null, 0L, false, false, null, emptyList())
                _error.value = "Couldn't load saved details. Try opening this device again."
            }
        }
    }

    fun rename(name: String) {
        val device = _device.value ?: return
        val connection = loadedConnection ?: return
        val key = lanNetworkKey(connection) ?: return
        viewModelScope.launch {
            try {
                if (!sameLanConnection(connection, networkStatusRepository.getCurrentNetworkStatus().first())) {
                    _error.value = "The connection changed. Open this device again before renaming it."
                } else if (dao.getLanDevice(device.ipAddress, key) == null) {
                    _error.value = "Scan this device before saving a name."
                } else {
                    val customName = name.trim().takeIf { it.isNotEmpty() }
                    dao.renameLanDevice(device.ipAddress, key, customName)
                    _device.value = device.copy(customName = customName)
                    _error.value = null
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { _error.value = "Couldn't save this name. Try again." }
        }
    }
}
