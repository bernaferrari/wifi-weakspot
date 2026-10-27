package com.bernaferrari.wifiweakspot.demo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.database.entities.LanDeviceEntity
import com.bernaferrari.wifiweakspot.core.database.entities.SpeedTestResultEntity
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyPointEntity
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyProjectEntity
import com.bernaferrari.wifiweakspot.core.database.entities.ToolResultEntity
import com.bernaferrari.wifiweakspot.core.database.entities.WifiScanResultEntity
import com.bernaferrari.wifiweakspot.core.util.nowMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Preferences for one demo session. */
class InMemoryPreferences(var readDelayMillis: Long = 0) : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    // Optional capture probe for asynchronous Android preference delivery. Normal demos use zero.
    override val data: Flow<Preferences> = state.map {
        if (readDelayMillis > 0) kotlinx.coroutines.delay(readDelayMillis)
        it
    }

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

/** The app's Room tables, held in memory for one demo session. */
class InMemoryDao : WifiWeakspotDao {
    private val backupImports = mutableMapOf<String, com.bernaferrari.wifiweakspot.core.database.entities.BackupImportEntity>()
    override suspend fun insertBackupImport(record: com.bernaferrari.wifiweakspot.core.database.entities.BackupImportEntity) { backupImports[record.archiveId] = record }
    override suspend fun getBackupImport(archiveId: String) = backupImports[archiveId]
    override suspend fun getPendingBackupImports() = backupImports.values.filterNot { it.completed }
    override suspend fun completeBackupImport(archiveId: String) {
        backupImports[archiveId]?.let { backupImports[archiveId] = it.copy(completed = true, roomsJson = "[]") }
    }
    private val seed = SeedHistory()
    private val scans = MutableStateFlow(seed.scans)
    private val lan = MutableStateFlow(seed.devices)
    private val tools = MutableStateFlow(seed.tools)
    private val speed = MutableStateFlow(seed.speedTests)
    private val projects = MutableStateFlow(listOf(seed.floorPlan))
    private val points = MutableStateFlow(seed.floorPlanPoints)
    private var nextId = 10_000L

    override suspend fun insertWifiScan(result: WifiScanResultEntity) = insertWifiScans(listOf(result))
    override suspend fun insertWifiScans(results: List<WifiScanResultEntity>) =
        scans.update { (results.map { it.copy(id = nextId++) } + it) }
    override suspend fun trimWifiScans(keep: Int) = scans.update { it.take(keep) }
    override fun getRecentWifiScans(): Flow<List<WifiScanResultEntity>> = scans.map { it.take(3000) }

    override suspend fun insertLanDevice(device: LanDeviceEntity) =
        lan.update { list -> (list.filterNot { it.ipAddress == device.ipAddress && it.networkKey == device.networkKey } + device).sortedByDescending { it.lastSeen } }
    override fun getAllLanDevices(): Flow<List<LanDeviceEntity>> = lan
    override suspend fun getLanDevice(ip: String, networkKey: String) = lan.value.firstOrNull { it.ipAddress == ip && it.networkKey == networkKey }
    override suspend fun renameLanDevice(ip: String, networkKey: String, name: String?) =
        lan.update { list -> list.map { if (it.ipAddress == ip && it.networkKey == networkKey) it.copy(customName = name) else it } }
    override suspend fun getLatestPortScan(host: String, networkKey: String) =
        tools.value.firstOrNull { it.target == host && it.networkKey == networkKey && it.toolType == "PORT_SCAN" && it.isSuccess }

    override suspend fun insertToolResult(result: ToolResultEntity) = tools.update { listOf(result.copy(id = nextId++)) + it }
    override fun getToolHistory(): Flow<List<ToolResultEntity>> = tools

    override suspend fun insertSpeedTest(result: SpeedTestResultEntity): Long {
        val id = nextId++
        speed.update { listOf(result.copy(id = id)) + it }
        return id
    }
    override fun getSpeedTestHistory(): Flow<List<SpeedTestResultEntity>> = speed
    override suspend fun tagSpeedTest(id: Long, room: String?) =
        speed.update { list -> list.map { t -> if (t.id == id) t.copy(room = room) else t } }

    override suspend fun insertSurveyProject(project: SurveyProjectEntity): Long {
        val id = nextId++
        projects.update { listOf(project.copy(id = id)) + it }
        return id
    }
    override suspend fun snapshotSurveyProjects() = projects.value.sortedByDescending { it.updatedAt }
    override suspend fun snapshotSurveyPoints() = points.value
    override fun getAllProjects(): Flow<List<SurveyProjectEntity>> = projects.map { list -> list.sortedByDescending { it.updatedAt } }
    override suspend fun updateSurveyProject(project: SurveyProjectEntity) =
        projects.update { list -> list.map { if (it.id == project.id) project else it } }
    override suspend fun markSurveyProjectUsed(projectId: Long, timestamp: Long) =
        projects.update { list -> list.map { if (it.id == projectId) it.copy(updatedAt = timestamp) else it } }
    override suspend fun deleteSurveyPoint(pointId: Long) = points.update { list -> list.filterNot { it.id == pointId } }
    override suspend fun updateSurveyPoint(point: SurveyPointEntity) = points.update { list -> list.map { if (it.id == point.id) point else it } }
    override suspend fun insertSurveyPoint(point: SurveyPointEntity) = points.update { it + point.copy(id = nextId++) }
    override fun getPointsForProject(projectId: Long): Flow<List<SurveyPointEntity>> = points.map { list -> list.filter { it.projectId == projectId } }
    override fun getAllPoints(): Flow<List<SurveyPointEntity>> = points
    override suspend fun deleteFloorPoints(projectId: Long, floorId: Int) =
        points.update { list -> list.filterNot { it.projectId == projectId && it.floorId == floorId } }
    override suspend fun deletePointsForProject(projectId: Long) = points.update { list -> list.filterNot { it.projectId == projectId } }
    override suspend fun deleteProject(project: SurveyProjectEntity) {
        deletePointsForProject(project.id)
        projects.update { list -> list.filterNot { it.id == project.id } }
    }
}

/** About a week of earlier use, as a returning person would have. */
private class SeedHistory {
    private val now = nowMillis()
    private fun ago(days: Int, hours: Double) = now - days * 86_400_000L - (hours * 3_600_000).toLong()
    private val router = "Router ${SimulatedHome.Gateway}"

    val speedTests = listOf(
        // days ago, hours earlier, Mbps, ms, jitter, dBm
        Seed(6, 3.0, 438f, 3.1f, 0.8f, -47),
        Seed(5, 1.5, 61f, 18f, 9.5f, -76),
        Seed(4, 4.0, 405f, 3.8f, 1.2f, -52),
        Seed(3, 2.0, 396f, 4.4f, 1.6f, -54),
        Seed(2, 6.0, 142f, 9.2f, 4.1f, -68),
        Seed(1, 1.0, 421f, 3.3f, 0.9f, -49),
        Seed(0, 2.0, 412f, 4.2f, 1.1f, -51),
    ).mapIndexed { i, t ->
        SpeedTestResultEntity(
            id = i + 1L, timestamp = ago(t.days, t.hours), downloadMbps = -1f, uploadMbps = t.mbps, pingMs = t.ms,
            jitterMs = t.jitter, serverName = router, rssi = t.rssi, frequencyMhz = SimulatedHome.Frequency,
            ssid = SimulatedHome.Ssid, lossPercent = if (t.rssi < -70) 4f else 0f
        )
    }.reversed()

    val scans = SimulatedHome.scan().let { networks ->
        listOf(ago(0, 2.1), ago(2, 6.1)).flatMapIndexed { session, at ->
            networks.mapIndexed { i, n ->
                WifiScanResultEntity(id = 1_000L + session * 500 + i, timestamp = at, ssid = n.ssid, bssid = n.bssid, rssi = n.rssi,
                    frequency = n.frequency, channelWidth = n.channelWidth, security = n.security)
            }
        }
    }

    /**
     * A finished survey of an 8 x 12 m flat, upright like a phone so the map fills the screen: router
     * in the living room at the bottom, signal falling with distance and with each wall crossed, so the
     * far bedroom at the top ends up the weak spot.
     */
    val floorPlan = SurveyProjectEntity(
        id = 1, name = com.bernaferrari.wifiweakspot.core.i18n.appStrings.survey.defaultMapName(1), description = null, imagePath = null, createdAt = ago(3, 2.0), updatedAt = ago(0, 1.0),
        targetSsid = SimulatedHome.Ssid, targetBssid = SimulatedHome.Bssid, widthMeters = 8f, heightMeters = 12f
    )
    val floorPlanPoints: List<SurveyPointEntity> = buildList {
        val routerX = 0.30f; val routerY = 0.80f
        var id = 5_000L
        val random = kotlin.random.Random(7)
        for (row in 0 until 7) for (col in 0 until 4) {
            // A loose grid with some wobble, the way people actually stop and measure.
            val x = 0.12f + col * 0.25f + (random.nextFloat() - 0.5f) * 0.09f
            val y = 0.07f + row * 0.143f + (random.nextFloat() - 0.5f) * 0.06f
            val meters = kotlin.math.sqrt(((x - routerX) * 8f).let { it * it } + ((y - routerY) * 12f).let { it * it })
            val walls = (if (y < 0.55f) 1 else 0) + (if (y < 0.55f && x > 0.5f) 1 else 0) + (if (y < 0.28f) 1 else 0)
            val rssi = (-36f - 21f * kotlin.math.log10(1f + meters * 1.6f) - walls * 9f).toInt().coerceIn(-92, -35)
            add(SurveyPointEntity(id = id++, projectId = 1, x = x, y = y, rssi = rssi, ssid = SimulatedHome.Ssid,
                bssid = SimulatedHome.Bssid, timestamp = ago(0, 1.0) + id))
        }
    }

    val devices = listOf(
        Triple(SimulatedHome.Gateway, "router.lan", "TP-Link"),
        Triple(SimulatedHome.PhoneIp, "this-phone", "Google"),
        Triple("192.168.1.31", "living-room-tv", "Samsung"),
        Triple("192.168.1.40", "macbook", "Apple"),
        Triple("192.168.1.66", "echo-kitchen", "Amazon"),
    ).map { (ip, name, vendor) -> LanDeviceEntity(ip, null, name, vendor, ago(1, 0.9), isOnline = true, networkKey = "wifi:${SimulatedHome.Bssid.lowercase()}") }

    val tools = listOf(
        ToolResultEntity(
            id = 2_001, timestamp = ago(1, 0.5), toolType = "PING", target = SimulatedHome.Gateway,
            resultJson = listOf(3.1, 2.8, 4.6, 3.3).joinToString("\n") { "Reply from ${SimulatedHome.Gateway}: time=$it ms" },
            isSuccess = true
        )
    )

    class Seed(val days: Int, val hours: Double, val mbps: Float, val ms: Float, val jitter: Float, val rssi: Int)
}
