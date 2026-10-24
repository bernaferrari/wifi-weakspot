package com.bernaferrari.wifiweakspot.core.domain

import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import kotlinx.coroutines.flow.Flow

interface NetworkStatusRepository {
    /** [refreshIntervalMs] re-reads the link; Android does not throttle connected-link RSSI. */
    fun getCurrentNetworkStatus(refreshIntervalMs: Long = 3_000): Flow<NetworkStatus>
    fun getAirHealth(): Flow<Int>
}
