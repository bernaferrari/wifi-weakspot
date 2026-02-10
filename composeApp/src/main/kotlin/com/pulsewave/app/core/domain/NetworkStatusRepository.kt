package com.pulsewave.app.core.domain

import com.pulsewave.app.core.model.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface NetworkStatusRepository {
    fun getCurrentNetworkStatus(): Flow<NetworkStatus>
    fun getAirHealth(): Flow<Int>
}
