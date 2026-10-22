package com.pulsewave.app.feature.walk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.rooms.*
import com.pulsewave.app.core.util.nowMillis
import com.pulsewave.app.core.util.randomId
import com.pulsewave.app.core.wifi.collectFreshLinkSamples
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Rooms: the live signal where you stand, and named readings you can save and recheck. */
class WalkViewModel(
    networkStatusRepository: NetworkStatusRepository,
    private val repository: RoomCheckRepository,
    private val clock: () -> Long = ::nowMillis
) : ViewModel() {
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()
    private val _saving = MutableStateFlow<String?>(null)
    val saving = _saving.asStateFlow()
    val live = networkStatusRepository.getCurrentNetworkStatus(refreshIntervalMs = LIVE_REFRESH_MS)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NetworkStatus.Disconnected)
    val readings = repository.readings.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    /** Null follows the connected network; an explicit selection lets old spots be inspected offline. */
    private val _selectedNetwork = MutableStateFlow<String?>(null)
    val networkChoices = readings.map { list -> list.distinctBy { it.networkKey }.filter { it.networkKey != null } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    val selectedNetwork = combine(_selectedNetwork, live) { selected, status ->
        selected ?: roomNetworkKey(status.ssid, status.bssid)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)
    val spots = combine(readings, selectedNetwork) { list, key -> coverageSummary(list.filter { key != null && it.networkKey == key }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), coverageSummary(emptyList()))
    fun selectNetwork(key: String?) { _selectedNetwork.value = key }
    private var saveJob: Job? = null

    /** Collect new readings only after the person has stopped at the named spot. */
    fun saveSpot(name: String) {
        val trimmed = name.trim().ifEmpty { return }
        if (saveJob?.isActive == true) return
        val initial = live.value
        val key = roomNetworkKey(initial.ssid, initial.bssid)
        if (!initial.hasSignalMeasurement) {
            _message.value = "No Wi-Fi signal to save. Connect to your Wi-Fi first."
            return
        }
        if (key == null) {
            _message.value = "Allow Wi-Fi scans in Networks so this reading can be linked to your network."
            return
        }
        if (_selectedNetwork.value != null && _selectedNetwork.value != key) {
            _message.value = "Connect to this spot's network before rechecking it."
            return
        }
        val requestedAt = clock()
        _saving.value = trimmed
        saveJob = viewModelScope.launch {
            try {
                val snapshots = checkNotNull(kotlinx.coroutines.withTimeoutOrNull(SPOT_SAMPLES * LIVE_REFRESH_MS + 2_000) {
                    collectFreshLinkSamples(live, initial.bssid, requestedAt, SPOT_SAMPLES,
                        LIVE_REFRESH_MS, initial.ssid, clock)
                }) { "Not enough fresh platform readings" }
                val reading = summarizeRoom(randomId(), trimmed, snapshots.map { it.rssi }, snapshots.map { it.linkSpeed },
                    initial.frequency, initial.ssid, initial.bssid, snapshots.last().sampledAtMillis)!!
                repository.save(reading)
                val previous = readings.value.filter { it.spotKey == reading.spotKey && it.id != reading.id }.maxByOrNull { it.timestamp }
                _message.value = when {
                    previous == null -> "Saved $trimmed"
                    reading.averageRssi - previous.averageRssi >= 4 -> "Saved $trimmed · better than last time"
                    reading.averageRssi - previous.averageRssi <= -4 -> "Saved $trimmed · worse than last time"
                    else -> "Saved $trimmed · same as last time"
                }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { _message.value = "Couldn't save a stable reading. Keep still on the same Wi-Fi and try again." }
            finally { _saving.value = null }
        }
    }
    fun restoreSpot(history: List<RoomReading>) { viewModelScope.launch {
        try { repository.saveAll(history) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { _message.value = "Couldn't restore this room. Try again." }
    } }
    fun deleteSpot(spot: RoomReading) { viewModelScope.launch {
        try { repository.deleteSpot(spot.spotKey) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { _message.value = "Couldn't remove this room. Try again." }
    } }
    fun restore(reading: RoomReading) { viewModelScope.launch { repository.save(reading) } }
    fun delete(reading: RoomReading) {
        viewModelScope.launch {
            try { repository.delete(reading.id) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { _message.value = "Couldn't remove this reading. Try again." }
        }
    }
    fun dismissMessage() { _message.value = null }
    companion object {
        const val LIVE_REFRESH_MS = 1_000L
        private const val SPOT_SAMPLES = 4
    }
}
