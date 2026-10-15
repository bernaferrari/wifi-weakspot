package com.pulsewave.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
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
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val FAVORITE_BSSIDS = stringSetPreferencesKey("favorite_bssids")
        val ONBOARDING_DONE = booleanPreferencesKey("onboarding_done")
        val SHOW_DBM = booleanPreferencesKey("show_dbm")
    }

    val isDarkMode: Flow<Boolean?> = dataStore.data.map { it[Keys.DARK_MODE] }
    val dynamicColor: Flow<Boolean> = dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: true }
    val seedColor: Flow<Int> = dataStore.data.map { it[Keys.SEED_COLOR] ?: 0xFF3F51B5.toInt() } // Indigo Default
    val hapticsEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.HAPTICS_ENABLED] ?: true }
    val reduceMotion: Flow<Boolean> = dataStore.data.map { it[Keys.REDUCE_MOTION] ?: false }
    /** Show raw dBm instead of the 0–100 score, for people who think in dBm. */
    val showDbm: Flow<Boolean> = dataStore.data.map { it[Keys.SHOW_DBM] ?: false }
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

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setShowDbm(enabled: Boolean) {
        dataStore.edit { it[Keys.SHOW_DBM] = enabled }
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
