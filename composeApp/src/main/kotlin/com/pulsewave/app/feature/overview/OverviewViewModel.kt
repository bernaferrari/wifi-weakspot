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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class OverviewViewModel(
    networkStatusRepository: NetworkStatusRepository,
    dao: PulseWaveDao,
    roomCheckRepository: RoomCheckRepository
) : ViewModel() {

    val networkStatus: StateFlow<NetworkStatus> = networkStatusRepository.getCurrentNetworkStatus(refreshIntervalMs = 2_000)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkStatus.Disconnected
        )

    val latestSpeedTest: StateFlow<SpeedTestResultEntity?> = dao.getSpeedTestHistory()
        .map { it.firstOrNull() }
        .catch { emitAll(flowOf(null)) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val rooms: StateFlow<CoverageSummary> = roomCheckRepository.readings
        .map(::coverageSummary)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), coverageSummary(emptyList()))
}
