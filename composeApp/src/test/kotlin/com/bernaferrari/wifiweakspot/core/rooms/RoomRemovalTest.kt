package com.bernaferrari.wifiweakspot.core.rooms

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class RoomRemovalTest {
    private class MemoryStore : DataStore<Preferences> {
        override val data = MutableStateFlow(emptyPreferences())
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            transform(data.value).also { data.value = it }
    }

    @Test fun removingRoomCannotRevealOldCheckAndUndoRestoresHistory() = runTest {
        val repo = RoomCheckRepository(MemoryStore())
        val before = RoomReading("before", "Kitchen", -75, -78, 100, 5180, "Home", "ap", 4, 1)
        val after = before.copy(id = "after", averageRssi = -60, timestamp = 2)
        val otherHome = before.copy(id = "other-home", ssid = "Office")
        val otherRoom = before.copy(id = "other-room", name = "Bedroom")
        repo.saveAll(listOf(before, after, otherHome, otherRoom))
        repo.deleteSpot(after.spotKey)
        assertEquals(setOf(otherHome.id, otherRoom.id), repo.readings.first().map { it.id }.toSet())
        repo.saveAll(listOf(before, after))
        repo.saveAll(listOf(before, after))
        assertEquals(4, repo.readings.first().size)
        assertEquals(listOf(before, after), repo.readings.first().filter { it.spotKey == after.spotKey })
    }
}
