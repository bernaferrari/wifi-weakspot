package com.pulsewave.app.core.permissions

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class PermissionStatus {
    GRANTED,
    DENIED,
    NOT_REQUESTED,
    PERMANENTLY_DENIED
}

data class PermissionState(
    val wifiScan: PermissionStatus,
    val location: PermissionStatus,
    val notifications: PermissionStatus,
    val nearbyDevices: PermissionStatus
) {
    val canScanWifi: Boolean
        get() = wifiScan == PermissionStatus.GRANTED || 
                location == PermissionStatus.GRANTED ||
                nearbyDevices == PermissionStatus.GRANTED
}

interface PermissionGate {
    val state: StateFlow<PermissionState>
    
    fun checkPermissions()
    suspend fun requestWifiScanPermission(): PermissionStatus
    suspend fun requestNotificationPermission(): PermissionStatus
    fun openAppSettings()
}
