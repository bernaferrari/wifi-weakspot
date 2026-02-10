package com.pulsewave.app.core.model.survey

import kotlinx.serialization.Serializable

@Serializable
data class SurveyProject(
    val id: Long,
    val name: String,
    val floorPlanPath: String?,
    val scaleMetersPerPixel: Float?,
    val targetSsids: List<String>,
    val targetBand: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class SurveyPoint(
    val id: Long,
    val projectId: Long,
    val x: Float,
    val y: Float,
    val bssid: String?,
    val rssi: Int,
    val frequency: Int,
    val timestamp: Long,
    val note: String?
)

@Serializable
data class HeatmapCell(
    val x: Float,
    val y: Float,
    val value: Float,
    val confidence: Float
)

@Serializable
data class Heatmap(
    val projectId: Long,
    val cells: List<HeatmapCell>,
    val minValue: Float,
    val maxValue: Float,
    val generatedAt: Long
)
