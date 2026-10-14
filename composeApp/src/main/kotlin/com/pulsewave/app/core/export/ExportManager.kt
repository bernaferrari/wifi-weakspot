package com.pulsewave.app.core.export

import kotlinx.serialization.Serializable

@Serializable
enum class ExportFormat {
    CSV, JSON, PNG, PDF
}

@Serializable
data class ExportArtifact(
    val id: Long,
    val format: ExportFormat,
    val path: String,
    val createdAt: Long,
    val sizeBytes: Long,
    val description: String
)

/**
 * WiFi scan data for export. [timestamp] is Unix epoch milliseconds.
 */
@Serializable
data class WifiExportData(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val channel: Int,
    val security: String,
    val timestamp: Long
)

/**
 * LAN device data for export. [lastSeen] is Unix epoch milliseconds.
 */
@Serializable
data class LanExportData(
    val ipAddress: String,
    val macAddress: String?,
    val hostname: String?,
    val vendor: String?,
    val isOnline: Boolean,
    val lastSeen: Long
)

/**
 * Speed test data for export
 */
data class SpeedTestExportData(
    val timestamp: Long,
    val downloadMbps: Float,
    val uploadMbps: Float,
    val pingMs: Float,
    val server: String
)

interface ExportManager {
    suspend fun exportWifiToCsv(networks: List<WifiExportData>, fileName: String)
    suspend fun exportWifiToJson(networks: List<WifiExportData>, fileName: String)
    suspend fun exportLanToCsv(devices: List<LanExportData>, fileName: String)
    suspend fun exportLanToJson(devices: List<LanExportData>, fileName: String)
    suspend fun exportSpeedTestToCsv(results: List<SpeedTestExportData>, fileName: String)
    suspend fun shareReport(text: String, title: String)
    suspend fun exportChart(chartData: ByteArray, name: String): ExportArtifact

    // Legacy support
    suspend fun exportToCsv(data: List<Any>, fileName: String) {
        // Default implementation for backwards compatibility
    }
    suspend fun exportToJson(data: Any, fileName: String) {
        // Default implementation for backwards compatibility
    }
}

/** RFC 4180 field: wrap in quotes and double any quote inside the value. */
fun csvField(value: String): String = "\"${value.replace("\"", "\"\"")}\""
