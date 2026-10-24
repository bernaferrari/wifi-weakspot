package com.bernaferrari.wifiweakspot.feature.survey

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.bernaferrari.wifiweakspot.core.database.SURVEY_TARGET_MIGRATION
import org.junit.Assert.*
import org.junit.Test

/** Run real SQLite DDL against an existing v1 project and its measurements. */
class SurveyMigrationTest {
    @Test fun migrationPreservesProjectsPointsAndAddsUsableCalibrationDefaults() {
        BundledSQLiteDriver().open(":memory:").use { connection ->
            connection.execSQL("PRAGMA foreign_keys = ON")
            connection.execSQL("""
                CREATE TABLE survey_projects (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL,
                    description TEXT, imagePath TEXT, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL
                )
            """.trimIndent())
            connection.execSQL("""
                CREATE TABLE survey_points (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, projectId INTEGER NOT NULL,
                    x REAL NOT NULL, y REAL NOT NULL, rssi INTEGER NOT NULL,
                    ssid TEXT, bssid TEXT, floorId INTEGER NOT NULL, timestamp INTEGER NOT NULL,
                    FOREIGN KEY(projectId) REFERENCES survey_projects(id) ON DELETE CASCADE
                )
            """.trimIndent())
            connection.execSQL("INSERT INTO survey_projects VALUES (7, 'Living room', NULL, '/old/map.png', 100, 200)")
            connection.execSQL("INSERT INTO survey_points VALUES (9, 7, 0.25, 0.75, -67, 'Home', 'radio', 2, 300)")

            SURVEY_TARGET_MIGRATION.migrate(connection)

            connection.prepare("SELECT id, name, imagePath, createdAt, updatedAt, targetSsid, targetBssid, widthMeters, heightMeters, stepLengthMeters, mapTopBearingDegrees FROM survey_projects").use { query ->
                assertTrue(query.step())
                assertEquals(7L, query.getLong(0))
                assertEquals("Living room", query.getText(1))
                assertEquals("/old/map.png", query.getText(2))
                assertEquals(100L, query.getLong(3))
                assertEquals(200L, query.getLong(4))
                assertTrue(query.isNull(5))
                assertTrue(query.isNull(6))
                assertEquals(10.0, query.getDouble(7), 0.0)
                assertEquals(10.0, query.getDouble(8), 0.0)
                assertEquals(0.7, query.getDouble(9), 0.00001)
                assertEquals(0.0, query.getDouble(10), 0.0)
                assertFalse(query.step())
            }
            connection.prepare("SELECT id, projectId, x, y, rssi, ssid, bssid, floorId, timestamp FROM survey_points").use { query ->
                assertTrue(query.step())
                assertEquals(9L, query.getLong(0))
                assertEquals(7L, query.getLong(1))
                assertEquals(0.25, query.getDouble(2), 0.0)
                assertEquals(0.75, query.getDouble(3), 0.0)
                assertEquals(-67L, query.getLong(4))
                assertEquals("Home", query.getText(5))
                assertEquals("radio", query.getText(6))
                assertEquals(2L, query.getLong(7))
                assertEquals(300L, query.getLong(8))
                assertFalse(query.step())
            }
            // Old points remain related to their project after the table is altered.
            connection.execSQL("DELETE FROM survey_projects WHERE id = 7")
            connection.prepare("SELECT count(*) FROM survey_points").use { query ->
                assertTrue(query.step())
                assertEquals(0L, query.getLong(0))
            }
        }
    }
}
