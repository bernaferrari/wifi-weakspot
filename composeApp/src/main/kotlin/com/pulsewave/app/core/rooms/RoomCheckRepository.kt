package com.pulsewave.app.core.rooms

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.pulsewave.app.core.wifi.SignalQuality
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** A few seconds of connected-link readings taken while standing in one room. */
@Serializable
data class RoomReading(
    val id: String,
    val name: String,
    val averageRssi: Int,
    val minRssi: Int,
    val linkSpeedMbps: Int,
    val frequencyMhz: Int,
    val ssid: String,
    val bssid: String,
    val samples: Int,
    val timestamp: Long,
    /** Median router response while saving, when the router answered. */
    val routerMs: Float? = null,
    /** Phone-to-router speed from a Wi-Fi speed test taken here. */
    val speedMbps: Float? = null
) {
    val quality: SignalQuality get() = SignalQuality.of(averageRssi)
}

/** Builds one reading from link samples; returns null when nothing was measured. */
fun summarizeRoom(
    id: String,
    name: String,
    rssiSamples: List<Int>,
    linkSpeedsMbps: List<Int>,
    frequencyMhz: Int,
    ssid: String,
    bssid: String,
    timestamp: Long,
    routerMs: Float? = null
): RoomReading? {
    val valid = rssiSamples.filter { it in -100..-1 }
    if (valid.isEmpty()) return null
    return RoomReading(
        id = id,
        name = name.trim(),
        averageRssi = kotlin.math.round(valid.average()).toInt(),
        minRssi = valid.min(),
        linkSpeedMbps = linkSpeedsMbps.filter { it > 0 }.takeIf { it.isNotEmpty() }?.let { kotlin.math.round(it.average()).toInt() } ?: 0,
        frequencyMhz = frequencyMhz,
        ssid = ssid,
        bssid = bssid,
        samples = valid.size,
        timestamp = timestamp,
        routerMs = routerMs
    )
}

/** Re-checking a room replaces its earlier reading, matched by name. */
fun List<RoomReading>.upsert(reading: RoomReading): List<RoomReading> =
    filterNot { it.name.equals(reading.name, ignoreCase = true) } + reading

class RoomCheckRepository(private val dataStore: DataStore<Preferences>) {
    private val key = stringPreferencesKey("room_readings_v1")
    private val json = Json { ignoreUnknownKeys = true }

    val readings: Flow<List<RoomReading>> = dataStore.data.map { prefs ->
        prefs[key]?.let { runCatching { json.decodeFromString<List<RoomReading>>(it) }.getOrNull() } ?: emptyList()
    }

    suspend fun save(reading: RoomReading) = update { it.upsert(reading) }

    suspend fun delete(id: String) = update { list -> list.filterNot { it.id == id } }

    suspend fun clear() = update { emptyList() }

    private suspend fun update(transform: (List<RoomReading>) -> List<RoomReading>) {
        dataStore.edit { prefs ->
            val current = prefs[key]?.let { runCatching { json.decodeFromString<List<RoomReading>>(it) }.getOrNull() } ?: emptyList()
            prefs[key] = json.encodeToString(transform(current))
        }
    }
}
