package com.bernaferrari.wifiweakspot.core.database

import androidx.room.*
import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.database.entities.*

@Database(
    entities = [
        WifiScanResultEntity::class,
        LanDeviceEntity::class,
        ToolResultEntity::class,
        SpeedTestResultEntity::class,
        SurveyProjectEntity::class,
        SurveyPointEntity::class,
        BackupImportEntity::class
    ],
    version = 7
)
abstract class WifiWeakspotDatabase : RoomDatabase() {
    abstract fun dao(): WifiWeakspotDao
}
