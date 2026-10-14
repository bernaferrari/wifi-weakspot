package com.pulsewave.app.core.lan

import com.pulsewave.app.core.model.lan.LanDevice
import com.pulsewave.app.core.model.lan.LanScanResult
import kotlinx.coroutines.flow.Flow

interface LanScanner {
    val scanProgress: Flow<Float>
    val isScanning: Flow<Boolean>

    suspend fun scanSubnet(subnet: String? = null): LanScanResult
    suspend fun scanRange(startIp: String, endIp: String): LanScanResult
    fun cancel()
}

interface NameResolver {
    suspend fun resolve(ipAddress: String): String?
    suspend fun resolveAll(devices: List<LanDevice>): List<LanDevice>
}

interface VendorResolver {
    fun getVendor(macAddress: String): String?
}
