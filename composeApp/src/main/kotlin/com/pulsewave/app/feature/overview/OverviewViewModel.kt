package com.pulsewave.app.feature.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.model.NetworkStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class OverviewViewModel(
    private val networkStatusRepository: NetworkStatusRepository
) : ViewModel() {

    val networkStatus: StateFlow<NetworkStatus> = networkStatusRepository.getCurrentNetworkStatus()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = NetworkStatus.Disconnected
        )

    val airHealth: StateFlow<Int> = networkStatusRepository.getAirHealth()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
}
