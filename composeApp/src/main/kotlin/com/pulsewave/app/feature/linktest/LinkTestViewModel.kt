package com.pulsewave.app.feature.linktest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.database.dao.PulseWaveDao
import com.pulsewave.app.core.database.entities.SpeedTestResultEntity
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.router.LinkVerdict
import com.pulsewave.app.core.router.PingEvent
import com.pulsewave.app.core.router.RouterLatency
import com.pulsewave.app.core.router.RouterProbe
import com.pulsewave.app.core.router.linkVerdict
import com.pulsewave.app.core.router.summarizeLatency
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class LinkTestResult(
    val latency: RouterLatency,
    val uploadMbps: Double?,
    val verdict: LinkVerdict,
    val txLinkMbps: Int,
    val rxLinkMbps: Int,
    val rssi: Int,
    val frequency: Int,
    val gateway: String
)

sealed interface LinkTestState {
    data object Idle : LinkTestState
    data class Pinging(val progress: Float, val latestMs: Float?) : LinkTestState
    data class Uploading(val progress: Float, val mbps: Double) : LinkTestState
    data class Done(val result: LinkTestResult) : LinkTestState
    data class Failed(val message: String) : LinkTestState
}

/** Phone-to-router test: response time first, then a short local upload burst. */
class LinkTestViewModel(
    networkStatusRepository: NetworkStatusRepository,
    private val probe: RouterProbe,
    private val dao: PulseWaveDao,
    private val rooms: com.pulsewave.app.core.rooms.RoomCheckRepository
) : ViewModel() {

    /** Rooms already saved in Walk, offered first when tagging a result. */
    val savedRooms: StateFlow<List<String>> = rooms.readings
        .map { readings -> readings.sortedByDescending { it.timestamp }.map { it.name } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _taggedRoom = MutableStateFlow<String?>(null)
    val taggedRoom: StateFlow<String?> = _taggedRoom.asStateFlow()

    /**
     * Files the latest result under a room: History shows it, and the room's Walk spot gains the
     * speed (or is created from this test), so rooms can be compared by what people feel.
     */
    fun tagRoom(name: String) {
        val result = (_state.value as? LinkTestState.Done)?.result ?: return
        val room = name.trim().ifEmpty { return }
        _taggedRoom.value = room
        viewModelScope.launch {
            runCatching { dao.tagLatestSpeedTest(room) }
            val current = status.value
            val existing = rooms.readings.first().firstOrNull { it.name.equals(room, ignoreCase = true) }
            val speed = result.uploadMbps?.toFloat()
            val routerMs = result.latency.medianMs.takeIf { it.isFinite() }
            // A test in a saved room is a fresh measurement there: refresh its signal and time too.
            val reading = existing?.let {
                if (result.rssi !in -100..-1) it.copy(speedMbps = speed, routerMs = routerMs ?: it.routerMs)
                else it.copy(
                    speedMbps = speed, routerMs = routerMs ?: it.routerMs, averageRssi = result.rssi,
                    minRssi = minOf(it.minRssi, result.rssi), frequencyMhz = result.frequency,
                    timestamp = com.pulsewave.app.core.util.nowMillis()
                )
            }
                ?: com.pulsewave.app.core.rooms.RoomReading(
                    id = com.pulsewave.app.core.util.randomId(), name = room,
                    averageRssi = result.rssi, minRssi = result.rssi, linkSpeedMbps = result.txLinkMbps,
                    frequencyMhz = result.frequency, ssid = current.ssid, bssid = current.bssid,
                    samples = 1, timestamp = com.pulsewave.app.core.util.nowMillis(), routerMs = routerMs, speedMbps = speed
                ).takeIf { result.rssi in -100..-1 }
            reading?.let { runCatching { rooms.save(it) } }
        }
    }

    val status: StateFlow<NetworkStatus> = networkStatusRepository.getCurrentNetworkStatus(refreshIntervalMs = 2_000)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), NetworkStatus.Disconnected)

    /** Earlier router tests, oldest first, for "last time" and "your usual". */
    val history: StateFlow<List<com.pulsewave.app.feature.history.RouterTestPoint>> = dao.getSpeedTestHistory()
        .map { com.pulsewave.app.feature.history.routerTests(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _state = MutableStateFlow<LinkTestState>(LinkTestState.Idle)
    val state: StateFlow<LinkTestState> = _state.asStateFlow()
    private var job: Job? = null

    fun start() {
        if (job?.isActive == true) return
        _taggedRoom.value = null
        job = viewModelScope.launch {
            val current = status.value
            val gateway = current.gateway
            if (!current.isConnected || gateway.isBlank()) {
                _state.value = LinkTestState.Failed("Connect to your Wi-Fi first. This test measures the link between your phone and your router.")
                return@launch
            }
            try {
                _state.value = LinkTestState.Pinging(0f, null)
                val rtts = mutableListOf<Float>()
                var sent = PING_COUNT
                probe.ping(gateway, PING_COUNT).collect { event ->
                    when (event) {
                        is PingEvent.Reply -> {
                            rtts += event.rttMs
                            _state.value = LinkTestState.Pinging((event.sequence.toFloat() / PING_COUNT).coerceAtMost(1f), event.rttMs)
                        }
                        is PingEvent.Finished -> sent = event.sent.coerceAtLeast(1)
                    }
                }
                val latency = summarizeLatency(rtts, sent) ?: RouterLatency(Float.NaN, Float.NaN, 100f, 0)

                _state.value = LinkTestState.Uploading(0f, 0.0)
                val upload = runCatching {
                    val cap = current.txLinkSpeed.takeIf { it > 0 }?.let { it * 1.1 } ?: 1_000.0
                    probe.uploadMbps(gateway, UPLOAD_MS, capMbps = cap) { progress, mbps ->
                        _state.value = LinkTestState.Uploading(progress, mbps)
                    }
                }.getOrNull()?.takeIf { it > 0.0 }
                    // The radio cannot move more than its negotiated rate.
                    ?.let { measured -> current.txLinkSpeed.takeIf { it > 0 }?.let { measured.coerceAtMost(it.toDouble()) } ?: measured }

                val result = LinkTestResult(
                    latency = latency,
                    uploadMbps = upload,
                    verdict = linkVerdict(latency, upload),
                    txLinkMbps = current.txLinkSpeed,
                    rxLinkMbps = current.rxLinkSpeed,
                    rssi = current.rssi,
                    frequency = current.frequency,
                    gateway = gateway
                )
                _state.value = LinkTestState.Done(result)
                runCatching {
                    dao.insertSpeedTest(
                        SpeedTestResultEntity(
                            timestamp = com.pulsewave.app.core.util.nowMillis(),
                            downloadMbps = -1f,
                            uploadMbps = upload?.toFloat() ?: -1f,
                            pingMs = latency.medianMs.takeIf { it.isFinite() } ?: -1f,
                            jitterMs = latency.jitterMs.takeIf { it.isFinite() } ?: -1f,
                            serverName = "$ROUTER_PREFIX$gateway",
                            rssi = current.rssi.takeIf { it in -100..-1 },
                            frequencyMhz = current.frequency.takeIf { it > 0 },
                            ssid = current.ssid.takeIf { it.isNotBlank() },
                            lossPercent = latency.lossPercent
                        )
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.value = LinkTestState.Failed("Your router didn't answer. Some routers block replies; the signal reading above still applies.")
            }
        }
    }

    fun cancel() {
        job?.cancel()
        _state.value = LinkTestState.Idle
    }

    companion object {
        const val PING_COUNT = 25
        const val UPLOAD_MS = 5_000L
        /** History rows from this test carry this server-name prefix. */
        const val ROUTER_PREFIX = "Router "
    }
}
