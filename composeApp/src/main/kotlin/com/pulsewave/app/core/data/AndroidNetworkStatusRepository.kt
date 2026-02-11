package com.pulsewave.app.core.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.model.NetworkStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart

@SuppressLint("MissingPermission")
class AndroidNetworkStatusRepository(
    private val context: Context
) : NetworkStatusRepository {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun getCurrentNetworkStatus(): Flow<NetworkStatus> = callbackFlow {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                val wifiInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    capabilities.transportInfo as? WifiInfo
                } else {
                    @Suppress("DEPRECATION")
                    wifiManager.connectionInfo
                }
                trySend(mapWifiInfo(wifiInfo))
            }

            override fun onLost(network: Network) {
                trySend(NetworkStatus.Disconnected)
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, callback)
        
        // Initial state
        @Suppress("DEPRECATION")
        trySend(mapWifiInfo(wifiManager.connectionInfo))

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.onStart {
        @Suppress("DEPRECATION")
        emit(mapWifiInfo(wifiManager.connectionInfo))
    }.conflate()

    override fun getAirHealth(): Flow<Int> = flow {
        getCurrentNetworkStatus().collect { status ->
            if (!status.isConnected) {
                emit(0)
            } else {
                emit(calculateHealth(status.rssi))
            }
        }
    }

    private fun mapWifiInfo(info: WifiInfo?): NetworkStatus {
        if (info == null) return NetworkStatus.Disconnected
        
        // On Android 12+, networkId returns -1 if permission is missing
        // Still check SSID as a fallback signal
        val ssidRaw = info.ssid?.trim('"')
        val hasValidSsid = ssidRaw != null && ssidRaw != "<unknown ssid>" && ssidRaw.isNotBlank()
        
        if (!hasValidSsid && info.networkId == -1) {
            return NetworkStatus.Disconnected
        }
        
        return NetworkStatus(
            ssid = ssidRaw ?: "Unknown",
            bssid = info.bssid ?: "",
            frequency = info.frequency,
            channel = frequencyToChannel(info.frequency),
            rssi = info.rssi,
            linkSpeed = info.linkSpeed,
            ipAddress = "Dynamic", 
            isConnected = true,
            security = "WPA2/WPA3" // Simplified for display
        )
    }

    private fun frequencyToChannel(freq: Int): Int {
        return when {
            freq in 2412..2484 -> (freq - 2412) / 5 + 1
            freq in 5170..5825 -> (freq - 5170) / 5 + 34
            else -> 0
        }
    }

    private fun calculateHealth(rssi: Int): Int {
        return when {
            rssi >= -50 -> 100
            rssi <= -100 -> 0
            else -> ((rssi + 100) * 2).coerceIn(0, 100)
        }
    }
}
