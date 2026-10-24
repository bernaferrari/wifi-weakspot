package com.bernaferrari.wifiweakspot.core.database.entities

import androidx.room.Entity
import androidx.room.ColumnInfo
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

@Entity(tableName = "lan_devices", primaryKeys = ["networkKey", "ipAddress"])
data class LanDeviceEntity(
    val ipAddress: String,
    val macAddress: String?,
    val hostname: String?,
    val vendor: String?,
    val lastSeen: Long,
    val isOnline: Boolean,
    val isTrusted: Boolean = false,
    val customName: String? = null,
    /** What the device announces over mDNS; null for rows saved before version 4. */
    val announcedName: String? = null,
    val model: String? = null,
    /** Comma-separated mDNS service types. */
    val services: String? = null,
    /** Empty for legacy records whose connection identity was never captured. */
    @ColumnInfo(defaultValue = "''") val networkKey: String = ""
)
