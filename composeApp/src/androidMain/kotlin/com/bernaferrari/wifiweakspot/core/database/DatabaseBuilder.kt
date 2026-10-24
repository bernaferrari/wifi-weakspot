package com.bernaferrari.wifiweakspot.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<WifiWeakspotDatabase> {
    val dbFile = context.getDatabasePath("wifiweakspot.db")
    return Room.databaseBuilder<WifiWeakspotDatabase>(
        context = context,
        name = dbFile.absolutePath
    ).addMigrations(SURVEY_TARGET_MIGRATION, SPEED_TEST_CONTEXT_MIGRATION, DEVICE_ANNOUNCEMENT_MIGRATION, SPEED_TEST_ROOM_MIGRATION, LAN_NETWORK_IDENTITY_MIGRATION)
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

/** Speed tests remember the signal and band they were measured at. */
val SPEED_TEST_CONTEXT_MIGRATION = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE speed_test_results ADD COLUMN rssi INTEGER")
        connection.execSQL("ALTER TABLE speed_test_results ADD COLUMN frequencyMhz INTEGER")
        connection.execSQL("ALTER TABLE speed_test_results ADD COLUMN ssid TEXT")
        connection.execSQL("ALTER TABLE speed_test_results ADD COLUMN lossPercent REAL")
    }
}

/** Devices remember the name, model and services they announce over mDNS. */
val DEVICE_ANNOUNCEMENT_MIGRATION = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE lan_devices ADD COLUMN announcedName TEXT")
        connection.execSQL("ALTER TABLE lan_devices ADD COLUMN model TEXT")
        connection.execSQL("ALTER TABLE lan_devices ADD COLUMN services TEXT")
    }
}

/** Speed tests remember which room they were taken in. */
val SPEED_TEST_ROOM_MIGRATION = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE speed_test_results ADD COLUMN room TEXT")
    }
}

/** Keep old device history, without assigning its names or trust to an unidentified network. */
val LAN_NETWORK_IDENTITY_MIGRATION = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("""
            CREATE TABLE lan_devices_scoped (
                ipAddress TEXT NOT NULL, macAddress TEXT, hostname TEXT, vendor TEXT,
                lastSeen INTEGER NOT NULL, isOnline INTEGER NOT NULL, isTrusted INTEGER NOT NULL,
                customName TEXT, announcedName TEXT, model TEXT, services TEXT,
                networkKey TEXT NOT NULL DEFAULT '', PRIMARY KEY(networkKey, ipAddress)
            )
        """.trimIndent())
        connection.execSQL("""
            INSERT INTO lan_devices_scoped (ipAddress, macAddress, hostname, vendor, lastSeen,
                isOnline, isTrusted, customName, announcedName, model, services, networkKey)
            SELECT ipAddress, macAddress, hostname, vendor, lastSeen, isOnline, isTrusted,
                customName, announcedName, model, services, '' FROM lan_devices
        """.trimIndent())
        connection.execSQL("DROP TABLE lan_devices")
        connection.execSQL("ALTER TABLE lan_devices_scoped RENAME TO lan_devices")
        connection.execSQL("ALTER TABLE tool_results ADD COLUMN networkKey TEXT")
    }
}
