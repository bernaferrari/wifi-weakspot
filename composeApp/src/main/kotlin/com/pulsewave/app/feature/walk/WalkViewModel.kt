package com.pulsewave.app.feature.walk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.rooms.*
import com.pulsewave.app.core.router.PingEvent
import com.pulsewave.app.core.router.RouterProbe
import com.pulsewave.app.core.util.nowMillis
import com.pulsewave.app.core.util.randomId
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class WalkSession(
    val startedAt: Long,
    val samples: List<WalkSample> = emptyList(),
    val latestRouterMs: Float? = null,
    val routerReplyAt: Long? = null,
    val networkKey: String? = null
)

class WalkViewModel(
    networkStatusRepository: NetworkStatusRepository,
    private val repository: RoomCheckRepository,
    private val probe: RouterProbe,
    private val clock: () -> Long = ::nowMillis
) : ViewModel() {
    private val _session = MutableStateFlow<WalkSession?>(null)
    val session = _session.asStateFlow()
    private val _finished = MutableStateFlow<Pair<WalkSummary, List<WalkSample>>?>(null)
    val finished = _finished.asStateFlow()
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
    private var walkJob: Job? = null
    private var saveJob: Job? = null
    private var walkNetwork: String? = null

    private fun sample(elapsed: Long): WalkSample {
        val status = live.value
        val current = _session.value
        val connected = status.isConnected && roomNetworkKey(status.ssid, status.bssid) == walkNetwork
        val fresh = current?.routerReplyAt?.let { clock() - it in 0..ROUTER_FRESH_MS } == true
        return WalkSample(elapsed, status.rssi.takeIf { connected && status.hasSignalMeasurement },
            current?.latestRouterMs?.takeIf { connected && fresh }, connected, current?.routerReplyAt?.takeIf { fresh })
    }

    fun startWalk() {
        if (walkJob?.isActive == true) return
        val status = live.value
        walkNetwork = roomNetworkKey(status.ssid, status.bssid)
        if (!status.hasSignalMeasurement || walkNetwork == null) {
            _message.value = "Connect to your Wi-Fi and allow signal readings before walking."
            return
        }
        _selectedNetwork.value = null
        _finished.value = null
        val started = clock()
        _session.value = WalkSession(started, networkKey = walkNetwork)
        walkJob = viewModelScope.launch {
            launch {
                live.map { current ->
                    if (current.isConnected && roomNetworkKey(current.ssid, current.bssid) == walkNetwork && current.gateway.isNotBlank())
                        current.bssid to current.gateway else null
                }.distinctUntilChanged().collectLatest { target ->
                    _session.value = _session.value?.copy(latestRouterMs = null, routerReplyAt = null)
                    if (target != null) try {
                        probe.ping(target.second, count = MAX_WALK_SECONDS, intervalSeconds = 1.0).collect { event ->
                            if (event is PingEvent.Reply) _session.value = _session.value?.copy(latestRouterMs = event.rttMs, routerReplyAt = clock())
                        }
                    } catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { _session.value = _session.value?.copy(latestRouterMs = null, routerReplyAt = null) }
                }
            }
            while (clock() - started < MAX_WALK_SECONDS * 1_000L) {
                val current = _session.value ?: break
                val elapsed = clock() - started
                val next = sample(elapsed)
                _session.value = current.copy(samples = current.samples + next,
                    latestRouterMs = next.routerMs, routerReplyAt = next.routerReplyId)
                delay(LIVE_REFRESH_MS)
            }
            finishWalk()
        }
    }

    fun finishWalk() {
        val current = _session.value ?: return
        val samples = current.samples + sample(clock() - current.startedAt)
        _session.value = null
        walkJob?.cancel()
        walkJob = null
        _finished.value = summarizeWalk(samples)?.let { it to samples }
    }
    fun dismissSummary() { _finished.value = null }

    /** Collect new readings only after the person has stopped at the named spot. */
    fun saveSpot(name: String) {
        val trimmed = name.trim().ifEmpty { return }
        if (saveJob?.isActive == true) return
        val initial = live.value
        val key = roomNetworkKey(initial.ssid, initial.bssid)
        if (!initial.hasSignalMeasurement || key == null) {
            _message.value = "No Wi-Fi signal to save. Connect to your Wi-Fi first."
            return
        }
        if (_selectedNetwork.value != null && _selectedNetwork.value != key) {
            _message.value = "Connect to this spot's network before rechecking it."
            return
        }
        _saving.value = trimmed
        saveJob = viewModelScope.launch {
            try {
                val snapshots = mutableListOf<NetworkStatus>()
                repeat(SPOT_SAMPLES) {
                    delay(LIVE_REFRESH_MS)
                    val next = live.value
                    check(next.hasSignalMeasurement && roomNetworkKey(next.ssid, next.bssid) == key && next.bssid == initial.bssid)
                    snapshots += next
                }
                val reading = summarizeRoom(randomId(), trimmed, snapshots.map { it.rssi }, snapshots.map { it.linkSpeed },
                    initial.frequency, initial.ssid, initial.bssid, clock())!!
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
        const val MAX_WALK_SECONDS = 600
        private const val SPOT_SAMPLES = 4
        private const val ROUTER_FRESH_MS = 2_000L
    }
}
