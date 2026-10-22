package com.pulsewave.app.feature.lan

import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.pulsewave.app.core.database.LAN_NETWORK_IDENTITY_MIGRATION
import org.junit.Assert.*
import org.junit.Test

class LanIdentityMigrationTest {
    @Test fun preservesLegacyHistoryWithoutAssigningItToANetworkAndAllowsRepeatedPrivateIps() {
        BundledSQLiteDriver().open(":memory:").use { db ->
            db.execSQL("""CREATE TABLE lan_devices (
                ipAddress TEXT NOT NULL PRIMARY KEY, macAddress TEXT, hostname TEXT, vendor TEXT,
                lastSeen INTEGER NOT NULL, isOnline INTEGER NOT NULL, isTrusted INTEGER NOT NULL,
                customName TEXT, announcedName TEXT, model TEXT, services TEXT)""")
            db.execSQL("""CREATE TABLE tool_results (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                timestamp INTEGER NOT NULL, toolType TEXT NOT NULL, target TEXT NOT NULL,
                resultJson TEXT NOT NULL, isSuccess INTEGER NOT NULL)""")
            db.execSQL("INSERT INTO lan_devices VALUES ('192.168.1.12', NULL, NULL, NULL, 100, 1, 1, 'My printer', NULL, NULL, NULL)")
            db.execSQL("INSERT INTO tool_results VALUES (7, 200, 'PORT_SCAN', '192.168.1.12', '80:HTTP', 1)")
            LAN_NETWORK_IDENTITY_MIGRATION.migrate(db)
            db.prepare("SELECT customName, isTrusted, networkKey FROM lan_devices").use {
                assertTrue(it.step())
                assertEquals("My printer", it.getText(0))
                assertEquals(1L, it.getLong(1))
                assertEquals("", it.getText(2))
            }
            db.prepare("SELECT id, resultJson, networkKey FROM tool_results").use {
                assertTrue(it.step())
                assertEquals(7L, it.getLong(0))
                assertEquals("80:HTTP", it.getText(1))
                assertTrue(it.isNull(2))
            }
            for (key in listOf("wifi:home", "wifi:office")) {
                db.execSQL("INSERT INTO lan_devices (ipAddress, lastSeen, isOnline, isTrusted, networkKey) VALUES ('192.168.1.12', 300, 1, 0, '$key')")
            }
            db.prepare("SELECT count(*) FROM lan_devices WHERE ipAddress = '192.168.1.12'").use {
                assertTrue(it.step())
                assertEquals(3L, it.getLong(0))
            }
        }
    }
}
