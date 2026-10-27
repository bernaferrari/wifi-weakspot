package com.bernaferrari.wifiweakspot.core.datastore

import com.bernaferrari.wifiweakspot.core.designsystem.ColorStyle
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.bernaferrari.wifiweakspot.core.wifi.SignalDisplay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SettingsRepository(
    private val dataStore: DataStore<Preferences>
) {
    private object Keys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        // val SHOW_6GHZ = booleanPreferencesKey("show_6ghz") // Unused for now
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val SEED_COLOR = intPreferencesKey("seed_color")
        val COLOR_STYLE = stringPreferencesKey("color_style")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val FAVORITE_BSSIDS = stringSetPreferencesKey("favorite_bssids")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val SHOW_DBM = booleanPreferencesKey("show_dbm")
        val SIGNAL_DISPLAY = stringPreferencesKey("signal_display")
    }

    val isDarkMode: Flow<Boolean?> = dataStore.data.map { it[Keys.DARK_MODE] }
    val dynamicColor: Flow<Boolean> = dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: true }
    val seedColor: Flow<Int> = dataStore.data.map { it[Keys.SEED_COLOR] ?: 0xFF3F51B5.toInt() } // Indigo Default
    val colorStyle: Flow<ColorStyle> = dataStore.data.map { ColorStyle.fromPreference(it[Keys.COLOR_STYLE]) }
    val hapticsEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.HAPTICS_ENABLED] ?: true }
    val reduceMotion: Flow<Boolean> = dataStore.data.map { it[Keys.REDUCE_MOTION] ?: false }
    /** New users get percentages; preserve an explicitly selected legacy display preference. */
    val signalDisplay: Flow<SignalDisplay> = dataStore.data.map { prefs ->
        SignalDisplay.entries.firstOrNull { it.name == prefs[Keys.SIGNAL_DISPLAY] }
            ?: when (prefs[Keys.SHOW_DBM]) {
                true -> SignalDisplay.Dbm
                false -> SignalDisplay.Bars
                null -> SignalDisplay.Percentage
            }
    }
    val favoriteBssids: Flow<Set<String>> = dataStore.data.map { it[Keys.FAVORITE_BSSIDS] ?: emptySet() }
    /** False until the first-run welcome is finished or skipped. */
    val onboardingDone: Flow<Boolean> = dataStore.data.map { it[Keys.ONBOARDING_DONE] ?: false }

    suspend fun setOnboardingDone(done: Boolean = true) {
        dataStore.edit { it[Keys.ONBOARDING_DONE] = done }
    }


    suspend fun setDarkMode(enabled: Boolean?) {
        dataStore.edit { if (enabled == null) it.remove(Keys.DARK_MODE) else it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setSeedColor(color: Int) {
        dataStore.edit { it[Keys.SEED_COLOR] = color }
    }

    suspend fun setColorStyle(style: ColorStyle) {
        dataStore.edit { it[Keys.COLOR_STYLE] = style.name }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setSignalDisplay(display: SignalDisplay) {
        dataStore.edit { it[Keys.SIGNAL_DISPLAY] = display.name }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        dataStore.edit { it[Keys.REDUCE_MOTION] = enabled }
    }

    suspend fun toggleFavorite(bssid: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.FAVORITE_BSSIDS] ?: emptySet()
            prefs[Keys.FAVORITE_BSSIDS] = if (bssid in current) current - bssid else current + bssid
        }
    }
}
