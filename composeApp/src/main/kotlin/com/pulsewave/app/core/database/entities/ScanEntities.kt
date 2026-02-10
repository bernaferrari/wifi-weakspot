package com.pulsewave.app.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wifi_scan_results")
data class WifiScanResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val channelWidth: Int,
    val security: String,
    val vendor: String? = null
)

@Entity(tableName = "lan_devices")
data class LanDeviceEntity(
    @PrimaryKey val ipAddress: String,
    val macAddress: String?,
    val hostname: String?,
    val vendor: String?,
    val lastSeen: Long,
    val isOnline: Boolean,
    val isTrusted: Boolean = false,
    val customName: String? = null
)
