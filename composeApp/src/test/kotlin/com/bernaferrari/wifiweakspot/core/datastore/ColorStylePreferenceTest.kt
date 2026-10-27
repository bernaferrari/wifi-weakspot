package com.bernaferrari.wifiweakspot.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.bernaferrari.wifiweakspot.core.designsystem.ColorStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ColorStylePreferenceTest {
    private class MemoryStore(initial: Preferences = emptyPreferences()) : DataStore<Preferences> {
        override val data = MutableStateFlow(initial)
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            transform(data.value).also { data.value = it }
    }

    @Test fun existingUsersKeepExpressiveWithoutAStoredStyle() = runTest {
        assertEquals(ColorStyle.Expressive, SettingsRepository(MemoryStore()).colorStyle.first())
    }

    @Test fun allFourChoicesSurviveRepositoryRecreation() = runTest {
        val store = MemoryStore()
        for (style in ColorStyle.entries) {
            SettingsRepository(store).setColorStyle(style)
            assertEquals(style, SettingsRepository(store).colorStyle.first())
        }
    }

    @Test fun unknownFutureStyleFallsBackWithoutLosingOtherPreferences() = runTest {
        val store = MemoryStore(preferencesOf(stringPreferencesKey("color_style") to "FutureStyle",
            booleanPreferencesKey("dark_mode") to false))
        val repo = SettingsRepository(store)
        assertEquals(ColorStyle.Expressive, repo.colorStyle.first())
        assertEquals(false, repo.isDarkMode.first())
    }

    @Test fun selectingAStylePreservesWallpaperSeedAndMode() = runTest {
        val repo = SettingsRepository(MemoryStore())
        repo.setDynamicColor(true)
        repo.setSeedColor(0xFF009688.toInt())
        repo.setDarkMode(true)
        repo.setColorStyle(ColorStyle.Cmf)
        assertEquals(true, repo.dynamicColor.first())
        assertEquals(0xFF009688.toInt(), repo.seedColor.first())
        assertEquals(true, repo.isDarkMode.first())
    }
}
