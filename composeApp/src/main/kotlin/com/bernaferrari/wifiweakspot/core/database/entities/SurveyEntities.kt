package com.bernaferrari.wifiweakspot.core.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "survey_projects")
data class SurveyProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String?,
    val imagePath: String?, // Local path to floorplan
    val createdAt: Long,
    val updatedAt: Long,
    val targetSsid: String? = null,
    val targetBssid: String? = null,
    @androidx.room.ColumnInfo(defaultValue = "10.0") val widthMeters: Float = 10f,
    @androidx.room.ColumnInfo(defaultValue = "10.0") val heightMeters: Float = 10f,
    @androidx.room.ColumnInfo(defaultValue = "0.7") val stepLengthMeters: Float = 0.7f,
    @androidx.room.ColumnInfo(defaultValue = "0.0") val mapTopBearingDegrees: Float = 0f
)

@Entity(
    tableName = "survey_points",
    foreignKeys = [
        ForeignKey(
            entity = SurveyProjectEntity::class,
            parentColumns = ["id"],
            childColumns = ["projectId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index("projectId")]
)
data class SurveyPointEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val projectId: Long,
    val x: Float,
    val y: Float,
    val rssi: Int,
    val ssid: String?,
    val bssid: String?,
    val floorId: Int = 1, // Default to Floor 1
    val timestamp: Long,
    val label: String? = null
)
