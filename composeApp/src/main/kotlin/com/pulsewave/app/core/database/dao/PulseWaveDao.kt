package com.pulsewave.app.core.database.dao

import androidx.room.*
import com.pulsewave.app.core.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PulseWaveDao {
    // WiFi Scan Results
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWifiScan(result: WifiScanResultEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWifiScans(results: List<WifiScanResultEntity>)

    @Query(
        """
        DELETE FROM wifi_scan_results
        WHERE id NOT IN (
            SELECT id FROM wifi_scan_results ORDER BY timestamp DESC LIMIT :keep
        )
        """
    )
    suspend fun trimWifiScans(keep: Int)

    @Query("SELECT * FROM wifi_scan_results ORDER BY timestamp DESC LIMIT 3000")
    fun getRecentWifiScans(): Flow<List<WifiScanResultEntity>>

    // LAN Devices
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanDevice(device: LanDeviceEntity)

    @Query("SELECT * FROM lan_devices ORDER BY lastSeen DESC")
    fun getAllLanDevices(): Flow<List<LanDeviceEntity>>

    @Query("SELECT * FROM lan_devices WHERE ipAddress = :ip AND networkKey = :networkKey")
    suspend fun getLanDevice(ip: String, networkKey: String): LanDeviceEntity?

    @Query("UPDATE lan_devices SET customName = :name WHERE ipAddress = :ip AND networkKey = :networkKey")
    suspend fun renameLanDevice(ip: String, networkKey: String, name: String?)

    @Query("SELECT * FROM tool_results WHERE target = :host AND networkKey = :networkKey AND toolType = 'PORT_SCAN' AND isSuccess = 1 ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestPortScan(host: String, networkKey: String): ToolResultEntity?

    // Tool Results
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToolResult(result: ToolResultEntity)

    @Query("SELECT * FROM tool_results ORDER BY timestamp DESC")
    fun getToolHistory(): Flow<List<ToolResultEntity>>

    // Speed Test Results
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedTest(result: SpeedTestResultEntity): Long

    @Query("UPDATE speed_test_results SET room = :room WHERE id = :id")
    suspend fun tagSpeedTest(id: Long, room: String?)

    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC")
    fun getSpeedTestHistory(): Flow<List<SpeedTestResultEntity>>

    // Survey
    @Insert
    suspend fun insertSurveyProject(project: SurveyProjectEntity): Long

    @Query("SELECT * FROM survey_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<SurveyProjectEntity>>

    @Update
    suspend fun updateSurveyProject(project: SurveyProjectEntity)

    @Query("UPDATE survey_projects SET updatedAt = :timestamp WHERE id = :projectId")
    suspend fun markSurveyProjectUsed(projectId: Long, timestamp: Long)

    @Transaction
    suspend fun replaceSurveyTarget(project: SurveyProjectEntity) {
        deletePointsForProject(project.id)
        updateSurveyProject(project)
    }

    @Query("DELETE FROM survey_points WHERE id = :pointId")
    suspend fun deleteSurveyPoint(pointId: Long)

    @Insert
    suspend fun insertSurveyPoint(point: SurveyPointEntity)

    @Query("SELECT * FROM survey_points WHERE projectId = :projectId")
    fun getPointsForProject(projectId: Long): Flow<List<SurveyPointEntity>>

    @Query("SELECT * FROM survey_points")
    fun getAllPoints(): Flow<List<SurveyPointEntity>>

    @Query("DELETE FROM survey_points WHERE projectId = :projectId AND floorId = :floorId")
    suspend fun deleteFloorPoints(projectId: Long, floorId: Int)

    @Query("DELETE FROM survey_points WHERE projectId = :projectId")
    suspend fun deletePointsForProject(projectId: Long)

    @Delete
    suspend fun deleteProject(project: SurveyProjectEntity)
}
