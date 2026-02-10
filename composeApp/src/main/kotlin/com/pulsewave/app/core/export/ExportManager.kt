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

interface ExportManager {
    suspend fun exportToCsv(data: List<Any>, fileName: String)
    suspend fun exportToJson(data: Any, fileName: String)
    suspend fun shareReport(text: String, title: String)
    suspend fun exportChart(chartData: ByteArray, name: String): ExportArtifact
}
