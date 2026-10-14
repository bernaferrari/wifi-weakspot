package com.pulsewave.app.feature.walk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.rooms.CoverageSummary
import com.pulsewave.app.core.rooms.RoomCheckRepository
import com.pulsewave.app.core.rooms.RoomReading
import com.pulsewave.app.core.rooms.WalkSample
import com.pulsewave.app.core.rooms.WalkSummary
import com.pulsewave.app.core.rooms.coverageSummary
import com.pulsewave.app.core.rooms.summarizeRoom
import com.pulsewave.app.core.rooms.summarizeWalk
import com.pulsewave.app.core.router.PingEvent
import com.pulsewave.app.core.router.RouterProbe
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class WalkSession(
    val startedAt: Long,
    val samples: List<WalkSample> = emptyList(),
    val latestRouterMs: Float? = null
)

/**
 * Signal finder: the connected link is read every second (Android does not throttle it)
 * and the router is pinged once a second, so weak spots show up as you walk.
 */
class WalkViewModel(
    networkStatusRepository: NetworkStatusRepository,
    private val repository: RoomCheckRepository,
    private val probe: RouterProbe
) : ViewModel() {

    private val _trail = MutableStateFlow<List<Int>>(emptyList())
    val trail: StateFlow<List<Int>> = _trail.asStateFlow()

    private val _session = MutableStateFlow<WalkSession?>(null)
    val session: StateFlow<WalkSession?> = _session.asStateFlow()

    private val _finished = MutableStateFlow<Pair<WalkSummary, List<WalkSample>>?>(null)
    val finished: StateFlow<Pair<WalkSummary, List<WalkSample>>?> = _finished.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val live: StateFlow<NetworkStatus> = networkStatusRepository.getCurrentNetworkStatus(refreshIntervalMs = LIVE_REFRESH_MS)
        .onEach { status ->
            if (status.hasSignalMeasurement) _trail.value = (_trail.value + status.rssi).takeLast(TRAIL_SIZE)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NetworkStatus.Disconnected)

    val spots: StateFlow<CoverageSummary> = repository.readings.map(::coverageSummary)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), coverageSummary(emptyList()))

    private var walkJob: Job? = null

    fun startWalk() {
        if (walkJob?.isActive == true) return
        _finished.value = null
        val started = System.currentTimeMillis()
        _session.value = WalkSession(startedAt = started)
        walkJob = viewModelScope.launch {
            launch {
                val gateway = live.value.gateway
                if (gateway.isNotBlank()) {
                    runCatching {
                        probe.ping(gateway, count = MAX_WALK_SECONDS, intervalSeconds = 1.0).collect { event ->
                            if (event is PingEvent.Reply) _session.value = _session.value?.copy(latestRouterMs = event.rttMs)
                        }
                    }
                }
            }
            while (true) {
                val status = live.value
                val current = _session.value ?: break
                if (status.hasSignalMeasurement) {
                    val sample = WalkSample(System.currentTimeMillis() - started, status.rssi, current.latestRouterMs)
                    _session.value = current.copy(samples = (current.samples + sample).takeLast(MAX_WALK_SECONDS))
                }
                if (System.currentTimeMillis() - started > MAX_WALK_SECONDS * 1_000L) break
                delay(LIVE_REFRESH_MS)
            }
            finishWalk()
        }
    }

    fun finishWalk() {
        val samples = _session.value?.samples.orEmpty()
        _session.value = null
        walkJob?.cancel()
        walkJob = null
        _finished.value = summarizeWalk(samples)?.let { it to samples }
    }

    fun dismissSummary() {
        _finished.value = null
    }

    /** Saves the last few seconds where the person is standing. */
    fun saveSpot(name: String) {
        val trimmed = name.trim().ifEmpty { return }
        val status = live.value
        val recent = _trail.value.takeLast(SPOT_SAMPLES)
        val reading = summarizeRoom(
            id = UUID.randomUUID().toString(), name = trimmed, rssiSamples = recent,
            linkSpeedsMbps = listOf(status.linkSpeed), frequencyMhz = status.frequency,
            ssid = status.ssid, bssid = status.bssid, timestamp = System.currentTimeMillis(),
            routerMs = _session.value?.latestRouterMs
        )
        if (reading == null) {
            _message.value = "No Wi-Fi signal to save. Connect to your Wi-Fi first."
            return
        }
        viewModelScope.launch {
            runCatching { repository.save(reading) }
                .onSuccess { _message.value = "Saved ${reading.name} · ${reading.averageRssi} dBm" }
                .onFailure { _message.value = "Couldn't save this spot. Try again." }
        }
    }

    fun delete(reading: RoomReading) {
        viewModelScope.launch { repository.delete(reading.id) }
    }

    fun clearSpots() {
        viewModelScope.launch { repository.clear() }
    }

    fun dismissMessage() {
        _message.value = null
    }

    companion object {
        const val LIVE_REFRESH_MS = 1_000L
        const val MAX_WALK_SECONDS = 600
        private const val TRAIL_SIZE = 60
        private const val SPOT_SAMPLES = 4
    }
}
