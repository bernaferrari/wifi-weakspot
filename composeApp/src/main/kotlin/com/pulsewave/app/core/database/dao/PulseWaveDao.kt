package com.pulsewave.app.core.database.dao

import androidx.room.*
import com.pulsewave.app.core.database.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PulseWaveDao {
    // WiFi Scan Results
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWifiScan(result: WifiScanResultEntity)

    @Query("SELECT * FROM wifi_scan_results ORDER BY timestamp DESC LIMIT 100")
    fun getRecentWifiScans(): Flow<List<WifiScanResultEntity>>

    // LAN Devices
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLanDevice(device: LanDeviceEntity)

    @Query("SELECT * FROM lan_devices ORDER BY lastSeen DESC")
    fun getAllLanDevices(): Flow<List<LanDeviceEntity>>

    @Query("SELECT * FROM lan_devices WHERE ipAddress = :ip")
    suspend fun getLanDevice(ip: String): LanDeviceEntity?

    // Tool Results
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToolResult(result: ToolResultEntity)

    @Query("SELECT * FROM tool_results ORDER BY timestamp DESC")
    fun getToolHistory(): Flow<List<ToolResultEntity>>

    // Speed Test Results
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeedTest(result: SpeedTestResultEntity)

    @Query("SELECT * FROM speed_test_results ORDER BY timestamp DESC")
    fun getSpeedTestHistory(): Flow<List<SpeedTestResultEntity>>

    // Survey
    @Insert
    suspend fun insertSurveyProject(project: SurveyProjectEntity): Long

    @Query("SELECT * FROM survey_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<SurveyProjectEntity>>

    @Insert
    suspend fun insertSurveyPoint(point: SurveyPointEntity)

    @Query("SELECT * FROM survey_points WHERE projectId = :projectId")
    fun getPointsForProject(projectId: Long): Flow<List<SurveyPointEntity>>

    @Query("SELECT * FROM survey_points")
    fun getAllPoints(): Flow<List<SurveyPointEntity>>

    @Delete
    suspend fun deleteProject(project: SurveyProjectEntity)
}
