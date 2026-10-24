package com.bernaferrari.wifiweakspot.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.database.entities.SpeedTestResultEntity
import com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository
import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import com.bernaferrari.wifiweakspot.core.rooms.CoverageSummary
import com.bernaferrari.wifiweakspot.core.rooms.RoomCheckRepository
import com.bernaferrari.wifiweakspot.core.rooms.coverageSummary
import com.bernaferrari.wifiweakspot.core.lan.lanNetworkKey
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import com.bernaferrari.wifiweakspot.core.rooms.forNetwork
import kotlinx.coroutines.flow.stateIn

class OverviewViewModel(
    networkStatusRepository: NetworkStatusRepository,
    dao: WifiWeakspotDao,
    roomCheckRepository: RoomCheckRepository,
    wifiScanner: com.bernaferrari.wifiweakspot.core.wifi.WifiScanner
) : ViewModel() {

    /** Latest networks Android already knows about; Home never triggers a scan itself. */
    val nearby: StateFlow<List<com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork>> = wifiScanner.scanResults

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
    val knownDevices: StateFlow<Int?> = combine(dao.getAllLanDevices(), networkStatus) { devices, status ->
            val networkKey = lanNetworkKey(status) ?: return@combine null
            val scoped = devices.filter { it.networkKey == networkKey }
            val latest = scoped.maxOfOrNull { it.lastSeen } ?: return@combine null
            scoped.count { latest - it.lastSeen <= 2 * 60_000L }
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
