package com.pulsewave.app.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<PulseWaveDatabase> {
    val dbFile = context.getDatabasePath("pulsewave.db")
    return Room.databaseBuilder<PulseWaveDatabase>(
        context = context,
        name = dbFile.absolutePath
    ).addMigrations(SURVEY_TARGET_MIGRATION)
}

/** Preserve v1 projects and points; legacy maps require choosing a target before new samples. */
val SURVEY_TARGET_MIGRATION = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE survey_projects ADD COLUMN targetSsid TEXT")
        connection.execSQL("ALTER TABLE survey_projects ADD COLUMN targetBssid TEXT")
        connection.execSQL("ALTER TABLE survey_projects ADD COLUMN widthMeters REAL NOT NULL DEFAULT 10.0")
        connection.execSQL("ALTER TABLE survey_projects ADD COLUMN heightMeters REAL NOT NULL DEFAULT 10.0")
        connection.execSQL("ALTER TABLE survey_projects ADD COLUMN stepLengthMeters REAL NOT NULL DEFAULT 0.7")
        connection.execSQL("ALTER TABLE survey_projects ADD COLUMN mapTopBearingDegrees REAL NOT NULL DEFAULT 0.0")
    }
}
