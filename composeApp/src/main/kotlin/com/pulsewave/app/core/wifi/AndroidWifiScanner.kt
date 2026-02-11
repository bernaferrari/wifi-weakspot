package com.pulsewave.app.core.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.asStateFlow

@SuppressLint("MissingPermission")
class AndroidWifiScanner(
    private val context: Context
) : WifiScanner {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val _isScanning = MutableStateFlow(false)
    override val isScanning = _isScanning.asStateFlow()

    override val scanResults: Flow<List<WifiNetwork>> = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, intent: Intent) {
                if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                    _isScanning.value = false
                    if (hasWifiPermission()) {
                        val results = wifiManager.scanResults
                        trySend(results.map { it.toWifiNetwork() })
                    } else {
                         // Send empty list or error if permissions missing
                         trySend(emptyList()) 
                    }
                }
            }
        }

        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        context.registerReceiver(receiver, intentFilter)

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    override fun startScan() {
        if (!hasWifiPermission()) {
             return
        }
        
        @Suppress("DEPRECATION")
        val success = wifiManager.startScan()
        if (success) {
            _isScanning.value = true
        }
    }

    override fun stopScan() {
        _isScanning.value = false
    }

    private fun ScanResult.toWifiNetwork(): WifiNetwork {
        val ssidString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             wifiSsid?.toString()?.trim('"') ?: SSID ?: ""
        } else {
             SSID ?: ""
        }
        
        return WifiNetwork(
            ssid = ssidString,
            bssid = BSSID,
            rssi = level,
            frequency = frequency,
            channelWidth = channelWidth,
            security = capabilities,
            capabilities = capabilities,
            timestamp = timestamp
        )
    }

    private fun hasWifiPermission(): Boolean {
        val hasLocation = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasNearby = ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.NEARBY_WIFI_DEVICES
            ) == PackageManager.PERMISSION_GRANTED
            hasLocation && hasNearby
        } else {
            hasLocation
        }
    }
}
