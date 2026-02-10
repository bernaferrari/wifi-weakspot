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
        val SCAN_INTERVAL = longPreferencesKey("scan_interval")
        // val SHOW_6GHZ = booleanPreferencesKey("show_6ghz") // Unused for now
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val AUTO_SCAN = booleanPreferencesKey("auto_scan")
        val SEED_COLOR = intPreferencesKey("seed_color")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
    }

    val isDarkMode: Flow<Boolean?> = dataStore.data.map { it[Keys.DARK_MODE] }
    val scanInterval: Flow<Long> = dataStore.data.map { it[Keys.SCAN_INTERVAL] ?: 2000L }
    val dynamicColor: Flow<Boolean> = dataStore.data.map { it[Keys.DYNAMIC_COLOR] ?: true }
    val autoScan: Flow<Boolean> = dataStore.data.map { it[Keys.AUTO_SCAN] ?: false }
    val seedColor: Flow<Int> = dataStore.data.map { it[Keys.SEED_COLOR] ?: 0xFF3F51B5.toInt() } // Indigo Default
    val hapticsEnabled: Flow<Boolean> = dataStore.data.map { it[Keys.HAPTICS_ENABLED] ?: true }
    val reduceMotion: Flow<Boolean> = dataStore.data.map { it[Keys.REDUCE_MOTION] ?: false }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setScanInterval(ms: Long) {
        dataStore.edit { it[Keys.SCAN_INTERVAL] = ms }
    }

    suspend fun setDynamicColor(enabled: Boolean) {
        dataStore.edit { it[Keys.DYNAMIC_COLOR] = enabled }
    }

    suspend fun setAutoScan(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_SCAN] = enabled }
    }

    suspend fun setSeedColor(color: Int) {
        dataStore.edit { it[Keys.SEED_COLOR] = color }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.HAPTICS_ENABLED] = enabled }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        dataStore.edit { it[Keys.REDUCE_MOTION] = enabled }
    }
}
