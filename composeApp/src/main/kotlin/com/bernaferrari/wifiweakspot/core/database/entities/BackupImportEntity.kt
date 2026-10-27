package com.bernaferrari.wifiweakspot.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Durable handoff from the Room transaction to room readings in DataStore. */
@Entity(tableName = "backup_imports")
data class BackupImportEntity(
    @PrimaryKey val archiveId: String,
    val roomsJson: String,
    @ColumnInfo(defaultValue = "0") val completed: Boolean = false
)
