package com.pulsewave.app.core.wifi

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.ActivityCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

@SuppressLint("MissingPermission")
class AndroidWifiScanner(
    private val context: Context,
    private val root: com.pulsewave.app.core.root.RootAccess
) : WifiScanner {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _scanResults = MutableStateFlow<List<WifiNetwork>>(emptyList())
    override val scanResults: StateFlow<List<WifiNetwork>> = _scanResults.asStateFlow()

    private data class ScanCompletion(val requestId: Long, val results: List<WifiNetwork>?)
    private val completion = MutableStateFlow(ScanCompletion(0, null))
    private val scanLock = Any()
    private val awaitMutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var timeoutJob: Job? = null
    private var requestId = 0L
    private var requestedElapsedMicros = 0L

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(c: Context, intent: Intent) {
            if (intent.action != WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) return
            if (!hasWifiPermission()) {
                synchronized(scanLock) { finishScan(null) }
                return
            }
            val results = readCurrentResults()
            synchronized(scanLock) {
                if (_isScanning.value) {
                    val fresh = freshScanResults(
                        results,
                        intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false),
                        requestedElapsedMicros
                    )
                    finishScan(fresh)
                } else if (intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)) {
                    // Passive observations may update the list, but cannot complete a request.
                    _scanResults.value = results
                }
            }
        }
    }

    init {
        val filter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(receiver, filter)
        }
        if (hasWifiPermission()) {
            _scanResults.value = readCurrentResults()
        }
    }

    override fun startScan(): Boolean = requestScan(8_000) != null

    private fun requestScan(timeoutMs: Long): Long? = synchronized(scanLock) {
        if (_isScanning.value || !hasWifiPermission()) return@synchronized null
        requestId += 1
        val currentRequest = requestId
        requestedElapsedMicros = SystemClock.elapsedRealtimeNanos() / 1_000
        _isScanning.value = true
        val accepted = try {
            // Root scans come from the system shell, which Android does not throttle.
            root.requestScan() || platformStartScan()
        } catch (_: SecurityException) {
            false
        } catch (_: IllegalStateException) {
            false
        }
        if (!accepted) {
            finishScan(null)
            return@synchronized null
        }
        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(timeoutMs.coerceAtLeast(1))
            synchronized(scanLock) {
                if (requestId == currentRequest && _isScanning.value) finishScan(null)
            }
        }
        currentRequest
    }

    override fun stopScan() {
        synchronized(scanLock) { finishScan(null) }
    }

    override suspend fun awaitNextScan(timeoutMs: Long): List<WifiNetwork>? = awaitMutex.withLock {
        val currentRequest = requestScan(timeoutMs) ?: return@withLock null
        try {
            withTimeoutOrNull(timeoutMs.coerceAtLeast(1)) {
                completion.first { it.requestId == currentRequest }.results
            }
        } finally {
            synchronized(scanLock) {
                if (requestId == currentRequest && _isScanning.value) finishScan(null)
            }
        }
    }

    // Caller holds scanLock. Failed/timeout scans keep the displayed cache unchanged.
    private fun finishScan(fresh: List<WifiNetwork>?) {
        if (fresh != null) _scanResults.value = fresh
        _isScanning.value = false
        timeoutJob?.cancel()
        timeoutJob = null
        completion.value = ScanCompletion(requestId, fresh)
    }

    private fun readCurrentResults(): List<WifiNetwork> {
        return try {
            wifiManager.scanResults.map { it.toWifiNetwork() }
        } catch (_: SecurityException) {
            emptyList()
        } catch (_: IllegalStateException) {
            emptyList()
        }
    }

    private fun ScanResult.toWifiNetwork(): WifiNetwork {
        val ssidString = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            wifiSsid?.toString()?.trim('"') ?: ""
        } else {
            getSsidLegacy()
        }
        val elapsedMicros = SystemClock.elapsedRealtimeNanos() / 1_000
        return WifiNetwork(
            ssid = ssidString,
            bssid = BSSID,
            rssi = level,
            frequency = frequency,
            channelWidth = channelWidthMhz(channelWidth),
            security = securityLabel(capabilities),
            capabilities = capabilities,
            centerFreq0 = centerFreq0,
            centerFreq1 = centerFreq1,
            is80Plus80 = channelWidth == ScanResult.CHANNEL_WIDTH_80MHZ_PLUS_MHZ,
            observedElapsedRealtimeMicros = timestamp,
            timestamp = scanTimestampToEpochMillis(
                scanTimestampMicros = timestamp,
                nowEpochMillis = System.currentTimeMillis(),
                elapsedRealtimeMicros = elapsedMicros
            )
        )
    }

    @Suppress("DEPRECATION")
    private fun platformStartScan(): Boolean = wifiManager.startScan()

    @Suppress("DEPRECATION")
    private fun ScanResult.getSsidLegacy(): String = SSID ?: ""

    private fun hasWifiPermission(): Boolean {
        val fine = ActivityCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ActivityCompat.checkSelfPermission(context,
            Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) fine else fine || coarse
    }
}
