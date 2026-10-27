package com.bernaferrari.wifiweakspot.core.backup

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.database.entities.BackupImportEntity
import com.bernaferrari.wifiweakspot.core.rooms.RoomCheckRepository
import com.bernaferrari.wifiweakspot.core.rooms.RoomReading
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test

class BackupRecoveryTest {
    private class MemoryStore : DataStore<Preferences> {
        override val data = MutableStateFlow(emptyPreferences())
        var failWrites = false
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences {
            if (failWrites) throw IOException("Storage unavailable")
            return transform(data.value).also { data.value = it }
        }
    }

    private fun reading(id: String, name: String = "Kitchen") =
        RoomReading(id, name, -60, -65, 100, 5180, "Home", "ap", 4, 1_000)

    @Test fun retryAfterCompletionFailurePreservesLocalEditsAndAddsMissingOnlyOnce() = runTest {
        val store = MemoryStore()
        val rooms = RoomCheckRepository(store)
        val local = reading("existing", "Renamed locally").copy(averageRssi = -50)
        rooms.save(local)
        val imported = reading("new")
        val pending = BackupImportEntity("archive-id", backupJson.encodeToString(listOf(reading("existing"), imported)))
        val dao = mockk<WifiWeakspotDao>()
        var completed = false
        var failCompletion = true
        coEvery { dao.getPendingBackupImports() } answers { if (completed) emptyList() else listOf(pending) }
        coEvery { dao.completeBackupImport(pending.archiveId) } answers {
            if (failCompletion) throw IOException("Commit marker unavailable")
            completed = true
        }

        try {
            replayPendingBackupImports(dao, rooms)
            fail("The simulated completion failure should propagate")
        } catch (_: IOException) { }
        assertFalse(completed)
        assertEquals(listOf(local, imported), rooms.readings.first())

        // The durable room write succeeded before the marker failed. A user may edit it before retry.
        val editedImported = imported.copy(name = "Changed after restore")
        rooms.save(editedImported)
        failCompletion = false
        replayPendingBackupImports(dao, rooms)
        replayPendingBackupImports(dao, rooms)
        assertTrue(completed)
        assertEquals(listOf(local, editedImported), rooms.readings.first())
        coVerify(exactly = 2) { dao.completeBackupImport(pending.archiveId) }
    }

    @Test fun failedRoomWriteLeavesImportPendingAndCanBeReplayed() = runTest {
        val store = MemoryStore()
        val rooms = RoomCheckRepository(store)
        val local = reading("local")
        rooms.save(local)
        val imported = reading("new")
        val pending = BackupImportEntity("archive-id", backupJson.encodeToString(listOf(imported)))
        val dao = mockk<WifiWeakspotDao>()
        var completed = false
        coEvery { dao.getPendingBackupImports() } answers { if (completed) emptyList() else listOf(pending) }
        coEvery { dao.completeBackupImport(pending.archiveId) } answers { completed = true }
        store.failWrites = true

        try {
            replayPendingBackupImports(dao, rooms)
            fail("The simulated DataStore failure should propagate")
        } catch (_: IOException) { }
        assertFalse(completed)
        assertEquals(listOf(local), rooms.readings.first())
        coVerify(exactly = 0) { dao.completeBackupImport(any()) }

        store.failWrites = false
        replayPendingBackupImports(dao, rooms)
        assertTrue(completed)
        assertEquals(listOf(local, imported), rooms.readings.first())
        coVerify(exactly = 1) { dao.completeBackupImport(pending.archiveId) }
    }
}
