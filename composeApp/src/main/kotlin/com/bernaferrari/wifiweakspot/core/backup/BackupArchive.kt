package com.bernaferrari.wifiweakspot.core.backup

import com.bernaferrari.wifiweakspot.core.database.entities.SurveyPointEntity
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyProjectEntity
import com.bernaferrari.wifiweakspot.core.rooms.RoomReading
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Portable, versioned data. Never serialize device paths or database primary keys. */
@Serializable
data class BackupArchive(
    val format: String = "wifiweakspot-backup",
    val version: Int = 1,
    val archiveId: String,
    val createdAt: Long,
    val projects: List<BackupProject>,
    val roomReadings: List<RoomReading>
)

@Serializable
data class BackupProject(
    val name: String,
    val description: String?,
    val imageEntry: String?,
    val createdAt: Long,
    val updatedAt: Long,
    val targetSsid: String?,
    val targetBssid: String?,
    val widthMeters: Float,
    val heightMeters: Float,
    val stepLengthMeters: Float,
    val mapTopBearingDegrees: Float,
    val points: List<BackupPoint>
) {
    fun toEntity(imagePath: String?) = SurveyProjectEntity(
        name = name, description = description, imagePath = imagePath,
        createdAt = createdAt, updatedAt = updatedAt, targetSsid = targetSsid, targetBssid = targetBssid,
        widthMeters = widthMeters, heightMeters = heightMeters, stepLengthMeters = stepLengthMeters,
        mapTopBearingDegrees = mapTopBearingDegrees
    )
}

@Serializable
data class BackupPoint(
    val x: Float, val y: Float, val rssi: Int, val ssid: String?, val bssid: String?,
    val floorId: Int, val timestamp: Long, val label: String? = null
) {
    fun toEntity(projectId: Long) = SurveyPointEntity(
        projectId = projectId, x = x, y = y, rssi = rssi, ssid = ssid, bssid = bssid,
        floorId = floorId, timestamp = timestamp, label = label
    )
}

internal fun SurveyProjectEntity.backup(points: List<SurveyPointEntity>, imageEntry: String?) = BackupProject(
    name, description, imageEntry, createdAt, updatedAt, targetSsid, targetBssid,
    widthMeters, heightMeters, stepLengthMeters, mapTopBearingDegrees,
    points.map { BackupPoint(it.x, it.y, it.rssi, it.ssid, it.bssid, it.floorId, it.timestamp, it.label) }
)

internal val backupJson = Json { encodeDefaults = true; ignoreUnknownKeys = true }

/** Validate before creating any database rows or app-owned files. */
internal fun BackupArchive.validate() {
    require(format == "wifiweakspot-backup" && version == 1) { "Unsupported backup format" }
    require(archiveId.matches(Regex("[a-zA-Z0-9-]{16,64}")) && createdAt >= 0)
    require(projects.size <= 1_000 && roomReadings.size <= 50_000)
    val imageEntries = projects.mapNotNull { it.imageEntry }
    require(imageEntries.distinct().size == imageEntries.size) { "Each map must own its image" }
    require(projects.sumOf { it.points.size.toLong() } <= 100_000)
    fun text(value: String?, limit: Int = 4_096) { require(value == null || value.length <= limit) }
    projects.forEach { p ->
        require(p.name.isNotBlank() && p.createdAt >= 0 && p.updatedAt >= 0)
        text(p.name); text(p.description); text(p.targetSsid); text(p.targetBssid)
        require(p.widthMeters.isFinite() && p.widthMeters > 0 && p.widthMeters <= 100_000)
        require(p.heightMeters.isFinite() && p.heightMeters > 0 && p.heightMeters <= 100_000)
        require(p.stepLengthMeters.isFinite() && p.stepLengthMeters > 0 && p.stepLengthMeters <= 100)
        require(p.mapTopBearingDegrees.isFinite())
        require(p.imageEntry == null || p.imageEntry.matches(Regex("images/[0-9]+\\.bin")))
        p.points.forEach { point ->
            require(point.x.isFinite() && point.x in 0f..1f && point.y.isFinite() && point.y in 0f..1f)
            require(point.rssi in -127..0 && point.floorId >= 0 && point.timestamp >= 0)
            text(point.ssid); text(point.bssid); text(point.label)
        }
    }
    require(roomReadings.map { it.id }.toSet().size == roomReadings.size)
    roomReadings.forEach { r ->
        require(r.id.isNotBlank() && r.timestamp >= 0 && r.samples > 0)
        require(r.averageRssi in -127..0 && r.minRssi in -127..0)
        require(r.linkSpeedMbps >= 0 && r.frequencyMhz >= 0)
        require(r.routerMs == null || r.routerMs.isFinite() && r.routerMs >= 0)
        require(r.speedMbps == null || r.speedMbps.isFinite())
        text(r.id); text(r.name); text(r.ssid); text(r.bssid)
    }
}

interface BackupService {
    val supported: Boolean get() = true
    suspend fun exportTo(destination: String)
    /** Appends maps, preserves existing room IDs, and ignores an already-restored archive. */
    suspend fun restoreFrom(source: String): RestoreResult
}

enum class RestoreResult { Restored, AlreadyRestored }

/** The browser/screenshot demo never opens or restores personal files. */
object NoBackupService : BackupService {
    override val supported = false
    override suspend fun exportTo(destination: String) = error("Backup unavailable")
    override suspend fun restoreFrom(source: String): RestoreResult = error("Backup unavailable")
}
