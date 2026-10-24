package com.bernaferrari.wifiweakspot.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_results")
data class ToolResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val toolType: String, // PING, DNS, PORT_SCAN
    val target: String,
    val resultJson: String, // Simplified storage for complex result types
    val isSuccess: Boolean,
    /** Connection used for this check; null for legacy or unidentified connections. */
    val networkKey: String? = null
)

@Entity(tableName = "speed_test_results")
data class SpeedTestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val downloadMbps: Float,
    val uploadMbps: Float,
    val pingMs: Float,
    val jitterMs: Float,
    val serverName: String,
    /** Where the phone stood during the test; null for results saved before version 3. */
    val rssi: Int? = null,
    val frequencyMhz: Int? = null,
    val ssid: String? = null,
    val lossPercent: Float? = null,
    /** Where the person said they were, such as "Office"; null when not tagged. */
    val room: String? = null
)
