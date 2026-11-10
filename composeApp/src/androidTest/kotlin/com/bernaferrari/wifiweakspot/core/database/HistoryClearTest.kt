package com.bernaferrari.wifiweakspot.core.database

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bernaferrari.wifiweakspot.core.database.entities.LanDeviceEntity
import com.bernaferrari.wifiweakspot.core.database.entities.SpeedTestResultEntity
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyPointEntity
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyProjectEntity
import com.bernaferrari.wifiweakspot.core.database.entities.ToolResultEntity
import com.bernaferrari.wifiweakspot.core.database.entities.WifiScanResultEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HistoryClearTest {
    @Test fun clearHistoryRemovesSavedHistoryAndKeepsMaps() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, WifiWeakspotDatabase::class.java)
            .setDriver(BundledSQLiteDriver()).build()
        try {
            val dao = database.dao()
            val bssid = "aa:bb:cc:dd:ee:01"
            dao.insertWifiScan(WifiScanResultEntity(
                timestamp = 1, ssid = "Home", bssid = bssid, rssi = -50,
                frequency = 2437, channelWidth = 20, security = "WPA2"
            ))
            dao.insertLanDevice(LanDeviceEntity(
                ipAddress = "192.168.1.10", macAddress = null, hostname = null, vendor = null,
                lastSeen = 1, isOnline = true, customName = "TV", networkKey = "home"
            ))
            dao.insertToolResult(ToolResultEntity(
                timestamp = 1, toolType = "PING", target = "example.com", resultJson = "{}", isSuccess = true
            ))
            dao.insertSpeedTest(SpeedTestResultEntity(
                timestamp = 1, downloadMbps = 10f, uploadMbps = 5f, pingMs = 20f, jitterMs = 2f, serverName = "test"
            ))
            val projectId = dao.insertSurveyProject(SurveyProjectEntity(
                name = "Home map", description = "A measured home", imagePath = null,
                createdAt = 100, updatedAt = 200, targetSsid = "Home", targetBssid = bssid,
                widthMeters = 20f, heightMeters = 10f
            ))
            dao.insertSurveyPoint(SurveyPointEntity(
                projectId = projectId, x = 0.25f, y = 0.75f, rssi = -67, ssid = "Home",
                bssid = bssid, floorId = 1, timestamp = 300, label = "Desk"
            ))

            dao.clearHistory()

            assertTrue(dao.getRecentWifiScans().first().isEmpty())
            assertTrue(dao.getAllLanDevices().first().isEmpty())
            assertTrue(dao.getToolHistory().first().isEmpty())
            assertTrue(dao.getSpeedTestHistory().first().isEmpty())
            assertEquals(projectId, dao.snapshotSurveyProjects().single().id)
            assertEquals("Desk", dao.snapshotSurveyPoints().single().label)
        } finally {
            database.close()
        }
    }
}
