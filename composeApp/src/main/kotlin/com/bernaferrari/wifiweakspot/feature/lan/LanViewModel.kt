package com.bernaferrari.wifiweakspot.feature.lan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.database.entities.LanDeviceEntity
import com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository
import com.bernaferrari.wifiweakspot.core.lan.LanScanner
import com.bernaferrari.wifiweakspot.core.lan.lanNetworkKey
import com.bernaferrari.wifiweakspot.core.lan.sameLanConnection
import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import com.bernaferrari.wifiweakspot.core.model.lan.LanDevice
import com.bernaferrari.wifiweakspot.core.util.nowMillis
import com.bernaferrari.wifiweakspot.core.i18n.appStrings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LanUiState {
    data object Idle : LanUiState
    data object Scanning : LanUiState
    data object Cancelled : LanUiState
    data class Success(val devices: List<LanDevice>) : LanUiState
    data class Error(val message: String) : LanUiState
}

private class LanConnectionChanged : Exception()

class LanViewModel(
    private val lanScanner: LanScanner,
    private val dao: WifiWeakspotDao,
    private val networkStatusRepository: NetworkStatusRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<LanUiState>(LanUiState.Idle)
    val uiState: StateFlow<LanUiState> = _uiState.asStateFlow()

    /** Devices from the last scan, shown while a new one runs. */
    private val _known = MutableStateFlow<List<LanDevice>>(emptyList())
    val known: StateFlow<List<LanDevice>> = _known.asStateFlow()

    val progress: StateFlow<Float> = lanScanner.scanProgress.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0f)

    /** Gateway and own address, so the router and this phone can be named. */
    val status: StateFlow<NetworkStatus> = networkStatusRepository.getCurrentNetworkStatus(refreshIntervalMs = 5_000)
        .stateIn(viewModelScope, SharingStarted.Eagerly, NetworkStatus.Disconnected)

    private var scanJob: Job? = null
    private var displayedConnection: NetworkStatus? = null

    init {
        viewModelScope.launch {
            status.collect { current ->
                val previous = displayedConnection
                if (previous != null && !sameLanConnection(previous, current)) {
                    scanJob?.cancel()
                    lanScanner.cancel()
                    _uiState.value = LanUiState.Idle
                    _known.value = emptyList()
                }
                displayedConnection = current
                if (_uiState.value == LanUiState.Idle) {
                    val key = lanNetworkKey(current)
                    val saved = if (key == null) emptyList() else runCatching {
                        dao.getAllLanDevices().first().filter { it.networkKey == key }
                    }.getOrDefault(emptyList())
                    val latest = saved.maxOfOrNull { it.lastSeen }
                    _known.value = if (latest == null) emptyList() else saved
                        .filter { latest - it.lastSeen <= 2 * 60_000L }.map { it.toDevice() }
                }
            }
        }
    }

    fun startScan() {
        if (scanJob?.isCompleted == false) return
        scanJob = viewModelScope.launch {
            _uiState.value = LanUiState.Scanning
            try {
                val connection = networkStatusRepository.getCurrentNetworkStatus().first()
                val key = lanNetworkKey(connection)
                displayedConnection = connection
                val result = lanScanner.scanSubnet()
                if (!sameLanConnection(connection, networkStatusRepository.getCurrentNetworkStatus().first())) {
                    throw LanConnectionChanged()
                }
                val devices = result.devices.map { device ->
                    val saved = key?.let { dao.getLanDevice(device.ipAddress, it) }
                        ?.takeUnless { old -> old.macAddress != null && device.macAddress != null && !old.macAddress.equals(device.macAddress, ignoreCase = true) }
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
                if (key != null) devices.forEach { device ->
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
                            services = device.services.joinToString(",").ifEmpty { null },
                            networkKey = key
                        )
                    )
                }
                _known.value = devices
                _uiState.value = LanUiState.Success(devices)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.value = LanUiState.Error(if (e is LanConnectionChanged) appStrings.tools.connectionChangedLookAgain else appStrings.tools.scanFailed)
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
