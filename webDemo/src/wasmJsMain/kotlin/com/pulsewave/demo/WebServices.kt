package com.pulsewave.demo

import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.export.ExportArtifact
import com.pulsewave.app.core.export.ExportFormat
import com.pulsewave.app.core.export.ExportManager
import com.pulsewave.app.core.export.LanExportData
import com.pulsewave.app.core.export.SpeedTestExportData
import com.pulsewave.app.core.export.WifiExportData
import com.pulsewave.app.core.lan.LanScanner
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.model.lan.LanDevice
import com.pulsewave.app.core.model.lan.LanScanResult
import com.pulsewave.app.core.model.tools.DnsRecord
import com.pulsewave.app.core.model.tools.DnsRecordType
import com.pulsewave.app.core.model.tools.DnsResult
import com.pulsewave.app.core.model.tools.PingResult
import com.pulsewave.app.core.model.tools.PingSummary
import com.pulsewave.app.core.model.tools.PortResult
import com.pulsewave.app.core.model.tools.PortScanResult
import com.pulsewave.app.core.model.tools.PortStatus
import com.pulsewave.app.core.monitoring.BackgroundMonitor
import com.pulsewave.app.core.monitoring.MonitorSettings
import com.pulsewave.app.core.permissions.PermissionGate
import com.pulsewave.app.core.permissions.PermissionState
import com.pulsewave.app.core.permissions.PermissionStatus
import com.pulsewave.app.core.root.RootAccess
import com.pulsewave.app.core.root.RootState
import com.pulsewave.app.core.router.PingEvent
import com.pulsewave.app.core.router.RouterProbe
import com.pulsewave.app.core.tools.DnsClient
import com.pulsewave.app.core.tools.PingClient
import com.pulsewave.app.core.tools.PortScanner
import com.pulsewave.app.core.util.nowMillis
import com.pulsewave.app.core.wifi.RadioMonitor
import com.pulsewave.app.core.wifi.RadioState
import com.pulsewave.app.core.wifi.ScanSetupStatus
import com.pulsewave.app.core.wifi.ScanThrottleManager
import com.pulsewave.app.core.wifi.ThrottleState
import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.wifi.WifiScanner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.random.Random

/** Scans take about a second, like a real radio sweep, and return the simulated block. */
class SimulatedWifiScanner : WifiScanner {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val lock = Mutex()
    private val _results = MutableStateFlow(SimulatedHome.scan())
    private val _isScanning = MutableStateFlow(false)
    override val scanResults: StateFlow<List<WifiNetwork>> = _results.asStateFlow()
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    override fun startScan(): Boolean {
        if (_isScanning.value) return false
        scope.launch { sweep() }
        return true
    }

    override fun stopScan() {
        _isScanning.value = false
    }

    override suspend fun awaitNextScan(timeoutMs: Long): List<WifiNetwork>? =
        if (_isScanning.value) null else sweep()

    private suspend fun sweep(): List<WifiNetwork>? = lock.withLock {
        _isScanning.value = true
        delay(900L + Random.nextLong(600))
        if (!_isScanning.value) return@withLock null
        SimulatedHome.scan().also {
            _results.value = it
            _isScanning.value = false
        }
    }
}

class SimulatedNetworkStatus : NetworkStatusRepository {
    override fun getCurrentNetworkStatus(refreshIntervalMs: Long): Flow<NetworkStatus> = flow {
        while (true) {
            val rssi = SimulatedHome.connectedRssi()
            val rate = SimulatedHome.linkSpeedMbps(rssi)
            emit(
                NetworkStatus(
                    ssid = SimulatedHome.Ssid,
                    bssid = SimulatedHome.Bssid,
                    frequency = SimulatedHome.Frequency,
                    channel = 36,
                    rssi = rssi,
                    linkSpeed = rate,
                    ipAddress = SimulatedHome.PhoneIp,
                    isConnected = true,
                    security = "WPA3",
                    internetValidated = true,
                    gateway = SimulatedHome.Gateway,
                    txLinkSpeed = rate,
                    rxLinkSpeed = rate,
                    sampledAtMillis = com.pulsewave.app.core.util.nowMillis()
                )
            )
            delay(refreshIntervalMs.coerceAtLeast(500))
        }
    }

    override fun getAirHealth(): Flow<Int> = flowOf(72)
}

/** Developer options already lifted the scan limit, so scans run freely. */
class SimulatedScanThrottle : ScanThrottleManager {
    override val throttleState: StateFlow<ThrottleState> = MutableStateFlow(ThrottleState.Unlimited)
    override val isThrottlingDisabled: Flow<Boolean> = flowOf(true)
    override val setupStatus: StateFlow<ScanSetupStatus> =
        MutableStateFlow(ScanSetupStatus(developerOptionsEnabled = true, throttlingEnabled = false))
    override fun recordScan() = Unit
    override fun refresh() = Unit
}

/** Starts without location access so onboarding runs exactly as on a new phone. */
class SimulatedPermissionGate : PermissionGate {
    private val _state = MutableStateFlow(stateFor(PermissionStatus.NOT_REQUESTED))
    override val state: StateFlow<PermissionState> = _state.asStateFlow()

    fun grant() {
        _state.value = stateFor(PermissionStatus.GRANTED)
    }

    override fun checkPermissions() = Unit
    override suspend fun requestWifiScanPermission(): PermissionStatus { grant(); return PermissionStatus.GRANTED }
    override suspend fun requestNotificationPermission() = PermissionStatus.GRANTED
    override fun openAppSettings() = Unit

    private fun stateFor(location: PermissionStatus) =
        PermissionState(location, location, PermissionStatus.GRANTED, PermissionStatus.GRANTED)
}

class SimulatedRadios : RadioMonitor {
    override fun read() = RadioState(wifiEnabled = true, locationEnabled = true)
}

class NoRootAccess : RootAccess {
    override val state: StateFlow<RootState> = MutableStateFlow(RootState.NotRooted)
    override suspend fun enable() = false
    override fun disable() = Unit
    override fun requestScan() = false
}

/** Router replies and uplink speed follow the simulated signal, as they would at home. */
class SimulatedRouterProbe : RouterProbe {
    override fun ping(host: String, count: Int, intervalSeconds: Double): Flow<PingEvent> = flow {
        var received = 0
        repeat(count) { index ->
            delay((intervalSeconds * 1000).toLong())
            val rssi = SimulatedHome.connectedRssi()
            val lost = rssi < -80 && Random.nextInt(10) == 0
            if (!lost) {
                received++
                emit(PingEvent.Reply(index + 1, SimulatedHome.routerRttMs(rssi)))
            }
        }
        emit(PingEvent.Finished(count, received))
    }


}

class SimulatedPingClient : PingClient {
    override fun ping(host: String, count: Int, timeoutMs: Long): Flow<PingResult> = flow {
        repeat(count) { index ->
            delay(600)
            val rtt = if (host == SimulatedHome.Gateway) SimulatedHome.routerRttMs(SimulatedHome.connectedRssi()) else 14f + Random.nextFloat() * 9f
            emit(PingResult(host, ipFor(host), index + 1, 56, rtt, true, null))
        }
    }

    override suspend fun pingSummary(host: String, count: Int, timeoutMs: Long): PingSummary {
        val rtts = List(count) { 14f + Random.nextFloat() * 9f }
        delay(400L * count)
        return PingSummary(host, count, count, 0f, rtts.min(), rtts.average().toFloat(), rtts.max(), 2.1f)
    }
}

class SimulatedDnsClient : DnsClient {
    override suspend fun lookup(query: String, recordType: DnsRecordType, server: String?): DnsResult {
        delay(250)
        return DnsResult(
            query, server ?: SimulatedHome.Gateway,
            listOf(DnsRecord(recordType, query, if (recordType == DnsRecordType.AAAA) "2606:4700::6810:84e5" else ipFor(query), 300)),
            queryTimeMs = 18 + Random.nextLong(20), isSuccess = true, error = null
        )
    }
}

class SimulatedPortScanner : PortScanner {
    private val _progress = MutableStateFlow(0f)
    override val progress: Flow<Float> = _progress.asStateFlow()

    override suspend fun scanCommonPorts(host: String) = scanPorts(host, listOf(21, 22, 53, 80, 443, 445, 554, 8080, 8443))
    override suspend fun scanRange(host: String, startPort: Int, endPort: Int) = scanPorts(host, (startPort..endPort).toList())

    override suspend fun scanPorts(host: String, ports: List<Int>): PortScanResult {
        val open = if (host == SimulatedHome.Gateway) setOf(53, 80, 443) else setOf(80, 443)
        ports.forEachIndexed { i, _ -> if (i % 8 == 0) { _progress.value = i / ports.size.toFloat(); delay(60) } }
        _progress.value = 1f
        return PortScanResult(
            host,
            ports.map { PortResult(it, if (it in open) PortStatus.OPEN else PortStatus.CLOSED, serviceFor(it), null) },
            "TCP connect",
            durationMs = 60L * (ports.size / 8 + 1)
        )
    }

    override fun cancel() = Unit
}

class SimulatedLanScanner : LanScanner {
    private val _progress = MutableStateFlow(0f)
    private val _scanning = MutableStateFlow(false)
    override val scanProgress: Flow<Float> = _progress.asStateFlow()
    override val isScanning: Flow<Boolean> = _scanning.asStateFlow()

    override suspend fun scanSubnet(subnet: String?): LanScanResult = scan()
    override suspend fun scanRange(startIp: String, endIp: String): LanScanResult = scan()

    private suspend fun scan(): LanScanResult {
        _scanning.value = true
        for (step in 1..20) { delay(120); _progress.value = step / 20f }
        val now = nowMillis()
        val devices = listOf(
            Triple(SimulatedHome.Gateway, "router.lan", "TP-Link"),
            Triple(SimulatedHome.PhoneIp, "this-phone", "Google"),
            Triple("192.168.1.31", "living-room-tv", "Samsung"),
            Triple("192.168.1.40", "macbook", "Apple"),
            Triple("192.168.1.52", "printer", "HP"),
            Triple("192.168.1.66", "echo-kitchen", "Amazon"),
            Triple("192.168.1.71", null, "Espressif"),
        ).mapIndexed { i, (ip, name, vendor) ->
            LanDevice(ip, "3c:22:fb:00:00:${(10 + i)}", name, vendor, now, true, false, null, emptyList())
        }
        _scanning.value = false
        return LanScanResult(now, now, "192.168.1.0/24", devices, durationMs = 2_400)
    }

    override fun cancel() { _scanning.value = false }
}

/** Files can't be written from the browser; reports go to the clipboard instead. */
class ClipboardExportManager : ExportManager {
    override suspend fun exportWifiToCsv(networks: List<WifiExportData>, fileName: String) = Unit
    override suspend fun exportWifiToJson(networks: List<WifiExportData>, fileName: String) = Unit
    override suspend fun exportLanToCsv(devices: List<LanExportData>, fileName: String) = Unit
    override suspend fun exportLanToJson(devices: List<LanExportData>, fileName: String) = Unit
    override suspend fun exportSpeedTestToCsv(results: List<SpeedTestExportData>, fileName: String) = Unit
    override suspend fun shareReport(text: String, title: String) = copyText(text)
    override suspend fun exportChart(chartData: ByteArray, name: String) =
        ExportArtifact(0, ExportFormat.PNG, name, nowMillis(), chartData.size.toLong(), name)
}

class SimulatedBackgroundMonitor : BackgroundMonitor {
    private val _monitoring = MutableStateFlow(false)
    private val _settings = MutableStateFlow(MonitorSettings())
    override val isMonitoring: StateFlow<Boolean> = _monitoring.asStateFlow()
    override val error: StateFlow<String?> = MutableStateFlow(null)
    override val settings: StateFlow<MonitorSettings> = _settings.asStateFlow()
    override fun startMonitoring() { _monitoring.value = true }
    override fun stopMonitoring() { _monitoring.value = false }
    override fun updateSettings(settings: MonitorSettings) { _settings.value = settings }
}

private fun ipFor(host: String): String =
    if (host.firstOrNull()?.isDigit() == true) host else "104.16.${host.length * 7 % 250}.${host.hashCode().mod(250)}"

private fun serviceFor(port: Int): String? = mapOf(
    21 to "ftp", 22 to "ssh", 53 to "dns", 80 to "http", 443 to "https", 445 to "smb", 554 to "rtsp", 8080 to "http-alt", 8443 to "https-alt"
)[port]
