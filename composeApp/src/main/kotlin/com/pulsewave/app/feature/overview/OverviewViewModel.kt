package com.pulsewave.app.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.database.entities.SpeedTestResultEntity
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.rooms.CoverageSummary
import com.pulsewave.app.core.rooms.RoomCheckRepository
import com.pulsewave.app.core.rooms.coverageSummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import com.pulsewave.app.core.rooms.forNetwork
import kotlinx.coroutines.flow.stateIn

class OverviewViewModel(
    networkStatusRepository: NetworkStatusRepository,
    dao: PulseWaveDao,
    roomCheckRepository: RoomCheckRepository,
    wifiScanner: com.pulsewave.app.core.wifi.WifiScanner
) : ViewModel() {

    /** Latest networks Android already knows about; Home never triggers a scan itself. */
    val nearby: StateFlow<List<com.pulsewave.app.core.wifi.WifiNetwork>> = wifiScanner.scanResults

    val networkStatus: StateFlow<NetworkStatus> = networkStatusRepository.getCurrentNetworkStatus(refreshIntervalMs = 2_000)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkStatus.Disconnected
        )

    val latestSpeedTest: StateFlow<SpeedTestResultEntity?> = combine(dao.getSpeedTestHistory(), networkStatus) { tests, status ->
            tests.firstOrNull { status.isConnected && it.ssid == status.ssid && (it.serverName.startsWith("Router ")) }
        }
        .catch { emitAll(flowOf(null)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Devices from the most recent device scan, so the tile can say "15 devices". */
    val knownDevices: StateFlow<Int?> = dao.getAllLanDevices()
        .map { devices ->
            val latest = devices.maxOfOrNull { it.lastSeen } ?: return@map null
            devices.count { latest - it.lastSeen <= 2 * 60_000L }
        }
        .catch { emitAll(flowOf(null)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /** Saved router tests, so the History tile says what it holds. */
    val savedTests: StateFlow<Int> = dao.getSpeedTestHistory()
        .map { it.size }
        .catch { emitAll(flowOf(0)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val rooms: StateFlow<CoverageSummary> = combine(roomCheckRepository.readings, networkStatus) { list, status ->
            coverageSummary(list.forNetwork(status.ssid, status.bssid))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), coverageSummary(emptyList()))
}
