package com.pulsewave.app.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tool_results")
data class ToolResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val toolType: String, // PING, DNS, PORT_SCAN
    val target: String,
    val resultJson: String, // Simplified storage for complex result types
    val isSuccess: Boolean
)

@Entity(tableName = "speed_test_results")
data class SpeedTestResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val downloadMbps: Float,
    val uploadMbps: Float,
    val pingMs: Float,
    val jitterMs: Float,
    val serverName: String
)
