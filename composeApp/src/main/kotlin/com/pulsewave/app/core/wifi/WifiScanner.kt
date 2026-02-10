package com.pulsewave.app.core.wifi

import kotlinx.coroutines.flow.Flow

interface WifiScanner {
    val scanResults: Flow<List<WifiNetwork>>
    val isScanning: Flow<Boolean>
    
    fun startScan()
    fun stopScan()
}
