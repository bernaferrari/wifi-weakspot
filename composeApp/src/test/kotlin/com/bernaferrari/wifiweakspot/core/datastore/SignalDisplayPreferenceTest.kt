package com.bernaferrari.wifiweakspot.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.bernaferrari.wifiweakspot.core.wifi.SignalDisplay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SignalDisplayPreferenceTest {
    private class MemoryStore(initial: Preferences = emptyPreferences()) : DataStore<Preferences> {
        override val data = MutableStateFlow(initial)
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            transform(data.value).also { data.value = it }
    }

    @Test fun percentageIsDefaultAndExplicitChoicePersists() = runTest {
        val store = MemoryStore()
        val repo = SettingsRepository(store)
        assertEquals(SignalDisplay.Percentage, repo.signalDisplay.first())
        repo.setSignalDisplay(SignalDisplay.Bars)
        assertEquals(SignalDisplay.Bars, SettingsRepository(store).signalDisplay.first())
    }

    @Test fun legacyChoicesSurviveUntilNewPreferenceOverridesThem() = runTest {
        for ((legacy, expected) in listOf(true to SignalDisplay.Dbm, false to SignalDisplay.Bars)) {
            val store = MemoryStore(preferencesOf(booleanPreferencesKey("show_dbm") to legacy))
            val repo = SettingsRepository(store)
            assertEquals(expected, repo.signalDisplay.first())
            repo.setSignalDisplay(SignalDisplay.Percentage)
            assertEquals(SignalDisplay.Percentage, SettingsRepository(store).signalDisplay.first())
        }
    }
}
