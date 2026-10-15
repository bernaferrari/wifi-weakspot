package com.pulsewave.demo

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.database.entities.LanDeviceEntity
import com.pulsewave.app.core.database.entities.SpeedTestResultEntity
import com.pulsewave.app.core.database.entities.SurveyPointEntity
import com.pulsewave.app.core.database.entities.SurveyProjectEntity
import com.pulsewave.app.core.database.entities.ToolResultEntity
import com.pulsewave.app.core.database.entities.WifiScanResultEntity
import com.pulsewave.app.core.util.nowMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

/** Preferences for one browser session. */
class InMemoryPreferences : DataStore<Preferences> {
    private val state = MutableStateFlow(emptyPreferences())
    override val data: Flow<Preferences> = state

    override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences {
        val updated = transform(state.value)
        state.value = updated
        return updated
    }
}

/** The app's Room tables, held in memory for one browser session. */
class InMemoryDao : PulseWaveDao {
    private val seed = SeedHistory()
    private val scans = MutableStateFlow(seed.scans)
    private val lan = MutableStateFlow(seed.devices)
    private val tools = MutableStateFlow(seed.tools)
    private val speed = MutableStateFlow(seed.speedTests)
    private val projects = MutableStateFlow<List<SurveyProjectEntity>>(emptyList())
    private val points = MutableStateFlow<List<SurveyPointEntity>>(emptyList())
    private var nextId = 10_000L

    override suspend fun insertWifiScan(result: WifiScanResultEntity) = insertWifiScans(listOf(result))
    override suspend fun insertWifiScans(results: List<WifiScanResultEntity>) =
        scans.update { (results.map { it.copy(id = nextId++) } + it) }
    override suspend fun trimWifiScans(keep: Int) = scans.update { it.take(keep) }
    override fun getRecentWifiScans(): Flow<List<WifiScanResultEntity>> = scans.map { it.take(3000) }

    override suspend fun insertLanDevice(device: LanDeviceEntity) =
        lan.update { list -> (list.filterNot { it.ipAddress == device.ipAddress } + device).sortedByDescending { it.lastSeen } }
    override fun getAllLanDevices(): Flow<List<LanDeviceEntity>> = lan
    override suspend fun getLanDevice(ip: String) = lan.value.firstOrNull { it.ipAddress == ip }
    override suspend fun renameLanDevice(ip: String, name: String?) =
        lan.update { list -> list.map { if (it.ipAddress == ip) it.copy(customName = name) else it } }
    override suspend fun getLatestPortScan(host: String) =
        tools.value.firstOrNull { it.target == host && it.toolType == "PORT_SCAN" && it.isSuccess }

    override suspend fun insertToolResult(result: ToolResultEntity) = tools.update { listOf(result.copy(id = nextId++)) + it }
    override fun getToolHistory(): Flow<List<ToolResultEntity>> = tools

    override suspend fun insertSpeedTest(result: SpeedTestResultEntity) = speed.update { listOf(result.copy(id = nextId++)) + it }
    override fun getSpeedTestHistory(): Flow<List<SpeedTestResultEntity>> = speed
    override suspend fun tagLatestSpeedTest(room: String?) =
        speed.update { list -> list.sortedByDescending { it.timestamp }.mapIndexed { i, t -> if (i == 0) t.copy(room = room) else t } }

    override suspend fun insertSurveyProject(project: SurveyProjectEntity): Long {
        val id = nextId++
        projects.update { listOf(project.copy(id = id)) + it }
        return id
    }
    override fun getAllProjects(): Flow<List<SurveyProjectEntity>> = projects.map { list -> list.sortedByDescending { it.updatedAt } }
    override suspend fun updateSurveyProject(project: SurveyProjectEntity) =
        projects.update { list -> list.map { if (it.id == project.id) project else it } }
    override suspend fun markSurveyProjectUsed(projectId: Long, timestamp: Long) =
        projects.update { list -> list.map { if (it.id == projectId) it.copy(updatedAt = timestamp) else it } }
    override suspend fun deleteSurveyPoint(pointId: Long) = points.update { list -> list.filterNot { it.id == pointId } }
    override suspend fun insertSurveyPoint(point: SurveyPointEntity) = points.update { it + point.copy(id = nextId++) }
    override fun getPointsForProject(projectId: Long): Flow<List<SurveyPointEntity>> = points.map { list -> list.filter { it.projectId == projectId } }
    override fun getAllPoints(): Flow<List<SurveyPointEntity>> = points
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

    val devices = listOf(
        Triple(SimulatedHome.Gateway, "router.lan", "TP-Link"),
        Triple(SimulatedHome.PhoneIp, "this-phone", "Google"),
        Triple("192.168.1.31", "living-room-tv", "Samsung"),
        Triple("192.168.1.40", "macbook", "Apple"),
        Triple("192.168.1.66", "echo-kitchen", "Amazon"),
    ).map { (ip, name, vendor) -> LanDeviceEntity(ip, null, name, vendor, ago(1, 0.9), isOnline = true) }

    val tools = listOf(
        ToolResultEntity(
            id = 2_001, timestamp = ago(1, 0.5), toolType = "PING", target = SimulatedHome.Gateway,
            resultJson = listOf(3.1, 2.8, 4.6, 3.3).joinToString("\n") { "Reply from ${SimulatedHome.Gateway}: time=$it ms" },
            isSuccess = true
        )
    )

    class Seed(val days: Int, val hours: Double, val mbps: Float, val ms: Float, val jitter: Float, val rssi: Int)
}
