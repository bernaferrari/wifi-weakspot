package com.bernaferrari.wifiweakspot.core.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AndroidPermissionGate(
    private val context: Context
) : PermissionGate {

    private val _state = MutableStateFlow(
        PermissionState(
            wifiScan = PermissionStatus.NOT_REQUESTED,
            location = PermissionStatus.NOT_REQUESTED,
            notifications = PermissionStatus.NOT_REQUESTED,
            nearbyDevices = PermissionStatus.NOT_REQUESTED
        )
    )
    override val state: StateFlow<PermissionState> = _state.asStateFlow()

    override fun checkPermissions() {
        val wifi = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            check(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            check(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
        
        val nearby = if (Build.VERSION.SDK_INT >= 33) {
            check(Manifest.permission.NEARBY_WIFI_DEVICES)
        } else {
            PermissionStatus.GRANTED // Not needed below 33
        }

        val notifications = if (Build.VERSION.SDK_INT >= 33) {
            check(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            PermissionStatus.GRANTED
        }

        _state.value = PermissionState(
            wifiScan = wifi,
            location = wifi, // Tied for now
            notifications = notifications,
            nearbyDevices = nearby
        )
    }

    private fun check(permission: String): PermissionStatus {
        return if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
            PermissionStatus.GRANTED
        } else {
            PermissionStatus.DENIED
        }
    }

    override suspend fun requestWifiScanPermission(): PermissionStatus {
        // In a real ViewModel/Activity/Composable flow, we'd trigger the launcher here.
        // For this non-UI class, we just re-check. The actual request must be initiated from UI.
        checkPermissions()
        return _state.value.wifiScan
    }

    override suspend fun requestNotificationPermission(): PermissionStatus {
        checkPermissions()
        return _state.value.notifications
    }

    override fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
