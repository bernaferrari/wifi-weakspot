package com.bernaferrari.wifiweakspot.core.lan

import com.bernaferrari.wifiweakspot.core.model.lan.LanScanResult
import kotlinx.coroutines.flow.Flow

interface LanScanner {
    val scanProgress: Flow<Float>
    val isScanning: Flow<Boolean>

    suspend fun scanSubnet(subnet: String? = null): LanScanResult
    suspend fun scanRange(startIp: String, endIp: String): LanScanResult
    fun cancel()
}


internal enum class LanUnavailableReason { Vpn, NoLocalConnection }

internal class LanUnavailable(val reason: LanUnavailableReason) : Exception()
