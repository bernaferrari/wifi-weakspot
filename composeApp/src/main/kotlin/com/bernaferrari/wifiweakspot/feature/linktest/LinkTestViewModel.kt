package com.bernaferrari.wifiweakspot.feature.linktest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.wifiweakspot.core.database.dao.WifiWeakspotDao
import com.bernaferrari.wifiweakspot.core.database.entities.SpeedTestResultEntity
import com.bernaferrari.wifiweakspot.core.domain.NetworkStatusRepository
import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import com.bernaferrari.wifiweakspot.core.rooms.*
import com.bernaferrari.wifiweakspot.core.router.*
import com.bernaferrari.wifiweakspot.core.util.nowMillis
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/** Snapshot of the tested connection; tagging never reads a later connection's identity. */
data class LinkTestResult(
    val latency: RouterLatency, val verdict: LinkVerdict,
    val txLinkMbps: Int, val rxLinkMbps: Int, val rssi: Int, val frequency: Int,
    val gateway: String, val ssid: String, val bssid: String, val timestamp: Long,
    val savedId: Long? = null
)

sealed interface LinkTestState {
    data object Idle : LinkTestState
    data class Pinging(val progress: Float, val latestMs: Float?) : LinkTestState
    data class Done(val result: LinkTestResult) : LinkTestState
    data class Failed(val message: String) : LinkTestState
}

/** Local ICMP responsiveness; throughput requires a cooperating receiver and is not claimed. */
class LinkTestViewModel(
    networkStatusRepository: NetworkStatusRepository,
    private val probe: RouterProbe,
    private val dao: WifiWeakspotDao,
    private val rooms: RoomCheckRepository
) : ViewModel() {
    val status = networkStatusRepository.getCurrentNetworkStatus(refreshIntervalMs = 1_000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NetworkStatus.Disconnected)
    val savedRooms = combine(rooms.readings, status) { readings, current ->
        readings.forNetwork(current.ssid, current.bssid).sortedByDescending { it.timestamp }
            .distinctBy { it.name.lowercase() }.map { it.name }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _taggedRoom = MutableStateFlow<String?>(null)
    val taggedRoom = _taggedRoom.asStateFlow()
    private val _message = MutableStateFlow<String?>(null)
    val message = _message.asStateFlow()
    val history = combine(dao.getSpeedTestHistory(), status) { tests, current ->
        com.bernaferrari.wifiweakspot.feature.history.routerTests(tests.filter { it.ssid == current.ssid })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    private val _state = MutableStateFlow<LinkTestState>(LinkTestState.Idle)
    val state = _state.asStateFlow()
    private var job: Job? = null
    private var tagging = false

    fun tagRoom(name: String) {
        val result = (_state.value as? LinkTestState.Done)?.result ?: return
        val room = name.trim().ifEmpty { return }
        val savedId = result.savedId ?: return
        if (tagging || _taggedRoom.value == room) return
        tagging = true
        viewModelScope.launch {
            try {
                // Only the result actually displayed is tagged, even if another test was saved.
                dao.tagSpeedTest(savedId, room)
                if (result.rssi in -100..-1 && roomNetworkKey(result.ssid, result.bssid) != null) {
                    rooms.save(RoomReading(
                        id = "router-result-$savedId", name = room, averageRssi = result.rssi, minRssi = result.rssi,
                        linkSpeedMbps = result.txLinkMbps, frequencyMhz = result.frequency,
                        ssid = result.ssid, bssid = result.bssid, samples = 1, timestamp = result.timestamp,
                        routerMs = result.latency.medianMs.takeIf { it.isFinite() }
                    ))
                }
                _taggedRoom.value = room
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { _message.value = "Couldn't save this spot. Try again." }
            finally { tagging = false }
        }
    }

    fun start() {
        if (job?.isActive == true || tagging) return
        _taggedRoom.value = null
        _message.value = null
        job = viewModelScope.launch {
            val current = status.value
            if (!current.isConnected || current.gateway.isBlank()) {
                _state.value = LinkTestState.Failed("Connect to your Wi-Fi first. This check measures replies from your router.")
                return@launch
            }
            try {
                _state.value = LinkTestState.Pinging(0f, null)
                val rtts = mutableListOf<Float>()
                var sent = PING_COUNT
                probe.ping(current.gateway, PING_COUNT).collect { event ->
                    val latest = status.value
                    check(latest.isConnected && latest.bssid == current.bssid && latest.gateway == current.gateway) {
                        "Connection changed"
                    }
                    when (event) {
                        is PingEvent.Reply -> {
                            rtts += event.rttMs
                            // The running median settles instead of flickering with every odd reply.
                            val sorted = rtts.sorted()
                            _state.value = LinkTestState.Pinging(event.sequence.toFloat() / PING_COUNT, sorted[sorted.size / 2])
                        }
                        is PingEvent.Finished -> sent = event.sent.coerceAtLeast(1)
                    }
                }
                val latency = summarizeLatency(rtts, sent) ?: RouterLatency(Float.NaN, Float.NaN, 100f, 0)
                val timestamp = nowMillis()
                val result = LinkTestResult(latency, linkVerdict(latency), current.txLinkSpeed, current.rxLinkSpeed,
                    current.rssi, current.frequency, current.gateway, current.ssid, current.bssid, timestamp)
                val savedId = try {
                    dao.insertSpeedTest(SpeedTestResultEntity(
                        timestamp = timestamp, downloadMbps = -1f, uploadMbps = -1f,
                        pingMs = latency.medianMs.takeIf { it.isFinite() } ?: -1f,
                        jitterMs = latency.jitterMs.takeIf { it.isFinite() } ?: -1f,
                        serverName = "$ROUTER_PREFIX${current.gateway}",
                        rssi = current.rssi.takeIf { it in -100..-1 }, frequencyMhz = current.frequency.takeIf { it > 0 },
                        ssid = current.ssid.takeIf { it.isNotBlank() }, lossPercent = latency.lossPercent
                    ))
                } catch (cancelled: CancellationException) { throw cancelled }
                catch (_: Exception) { _message.value = "Result measured, but history couldn't be saved."; null }
                _state.value = LinkTestState.Done(result.copy(savedId = savedId))
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) {
                _state.value = LinkTestState.Failed("The connection changed or the router check stopped. Keep this Wi-Fi connected and try again. Some routers block ping.")
            }
        }
    }

    fun cancel() { job?.cancel(); _state.value = LinkTestState.Idle }
    companion object {
        const val PING_COUNT = 25
        const val ROUTER_PREFIX = "Router response "
        const val LEGACY_ROUTER_PREFIX = "Router "
    }
}
