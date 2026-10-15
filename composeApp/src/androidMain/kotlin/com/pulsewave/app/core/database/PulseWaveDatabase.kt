package com.pulsewave.app.core.database

import androidx.room.*
import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.database.entities.*

@Database(
    entities = [
        WifiScanResultEntity::class,
        LanDeviceEntity::class,
        ToolResultEntity::class,
        SpeedTestResultEntity::class,
        SurveyProjectEntity::class,
        SurveyPointEntity::class
    ],
    version = 5
)
abstract class PulseWaveDatabase : RoomDatabase() {
    abstract fun dao(): PulseWaveDao
}
