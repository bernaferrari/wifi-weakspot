package com.bernaferrari.wifiweakspot.core.wifi

import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Android implementation of ScanThrottleManager.
 *
 * Tracks scan timestamps and calculates remaining quota.
 * Also checks if Developer Options "WiFi scan throttling" is disabled.
 *
 * Now persists timestamps to survive app restarts.
 * Thread-safe for concurrent access.
 */
class AndroidScanThrottleManager(
    private val context: Context,
    private val root: com.bernaferrari.wifiweakspot.core.root.RootAccess
) : ScanThrottleManager {

    companion object {
        private const val MAX_SCANS = 4
        private const val WINDOW_MS = 2 * 60 * 1000L // 2 minutes
        private const val COUNTDOWN_TICK_MS = 500L
        private const val WIFI_SCAN_THROTTLE_ENABLED = "wifi_scan_throttle_enabled"
        /** Held only by privileged system apps; those scans are never throttled. */
        private const val PREFS_NAME = "wifi_throttle_prefs"
        private const val KEY_SCAN_TIMESTAMPS = "scan_timestamps"
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var countdownJob: Job? = null
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // Mutex for synchronized access to scanTimestamps
    private val mutex = Mutex()

    // Thread-safe timestamps of recent scans (within the 2-minute window)
    private val scanTimestamps = CopyOnWriteArrayList<Long>()

    private val _throttleState = MutableStateFlow<ThrottleState>(ThrottleState.Unknown)
    override val throttleState: StateFlow<ThrottleState> = _throttleState.asStateFlow()

    override val isThrottlingDisabled: Flow<Boolean> = flow {
        emit(checkThrottlingDisabled())
    }

    private val _setupStatus = MutableStateFlow(readSetupStatus())
    override val setupStatus: StateFlow<ScanSetupStatus> = _setupStatus.asStateFlow()

    init {
        restoreTimestamps()
        refresh()
        // Opting in or out of root changes the limit immediately.
        scope.launch { root.state.collect { updateState() } }
    }

    override fun recordScan() {
        scope.launch {
            mutex.withLock {
                val now = System.currentTimeMillis()
                scanTimestamps.add(now)
                pruneOldTimestampsInternal(now)
                persistTimestamps()
            }
            updateState()
        }
    }

    override fun refresh() {
        scope.launch {
            mutex.withLock {
                pruneOldTimestampsInternal(System.currentTimeMillis())
            }
            updateState()
        }
    }

    private fun pruneOldTimestampsInternal(now: Long) {
        // Internal method - must be called while holding mutex
        scanTimestamps.removeAll { !scanStillCounts(it, now, WINDOW_MS) }
    }

    private suspend fun updateState() {
        mutex.withLock {
            val setup = readSetupStatus()
            _setupStatus.value = setup
            val state = if (setup.isUnlimited) ThrottleState.Unlimited
                else scanQuotaState(scanTimestamps, System.currentTimeMillis(), MAX_SCANS, WINDOW_MS)
            _throttleState.value = state
            if (state is ThrottleState.Exhausted) {
                if (countdownJob == null) startCountdown()
            } else {
                countdownJob?.cancel()
                countdownJob = null
            }
        }
    }

    // Start under the mutex. The countdown clears its slot before releasing the lock on its last tick.
    private fun startCountdown() {
        countdownJob = scope.launch {
            while (true) {
                delay(COUNTDOWN_TICK_MS)
                val exhausted = mutex.withLock {
                    val now = System.currentTimeMillis()
                    pruneOldTimestampsInternal(now)
                    val state = scanQuotaState(scanTimestamps, now, MAX_SCANS, WINDOW_MS)
                    _throttleState.value = state
                    if (state !is ThrottleState.Exhausted) {
                        persistTimestamps()
                        countdownJob = null
                    }
                    state is ThrottleState.Exhausted
                }
                if (!exhausted) break
            }
        }
    }

    /** Android 8.1 and older do not throttle foreground scans. */
    private fun checkThrottlingDisabled(): Boolean = readSetupStatus().isUnlimited

    private fun readSetupStatus(): ScanSetupStatus {
        val sdk = Build.VERSION.SDK_INT
        val developerOptions = runCatching {
            Settings.Global.getInt(context.contentResolver, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1
        }.getOrDefault(false)
        val throttled = when {
            sdk < Build.VERSION_CODES.P -> false
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> runCatching {
                (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager).isScanThrottleEnabled
            }.getOrElse { readThrottleSetting() }
            else -> readThrottleSetting()
        }
        val rootState = root.state.value
        return ScanSetupStatus(
            throttlingApplies = sdk >= Build.VERSION_CODES.P,
            // The Developer options toggle first shipped in Android 10.
            canDisableThrottling = sdk >= Build.VERSION_CODES.Q,
            developerOptionsEnabled = developerOptions,
            throttlingEnabled = throttled,
            rootAvailable = rootState != com.bernaferrari.wifiweakspot.core.root.RootState.NotRooted,
            rootScanning = rootState == com.bernaferrari.wifiweakspot.core.root.RootState.Enabled
        )
    }

    /** 0 means the person turned "Wi-Fi scan throttling" off in Developer options. */
    private fun readThrottleSetting(): Boolean = runCatching {
        Settings.Global.getInt(context.contentResolver, WIFI_SCAN_THROTTLE_ENABLED, 1) != 0
    }.getOrDefault(true)

    /**
     * Persist scan timestamps to SharedPreferences.
     * Stores as comma-separated string of timestamps.
     * MUST be called while holding mutex.
     */
    private fun persistTimestamps() {
        val timestampString = scanTimestamps.joinToString(",")
        prefs.edit()
            .putString(KEY_SCAN_TIMESTAMPS, timestampString)
            .apply()
    }

    /**
     * Restore scan timestamps from SharedPreferences.
     * Only restores timestamps that are still within the 2-minute window.
     */
    private fun restoreTimestamps() {
        val timestampString = prefs.getString(KEY_SCAN_TIMESTAMPS, "") ?: ""
        if (timestampString.isNotEmpty()) {
            val now = System.currentTimeMillis()
            timestampString.split(",")
                .mapNotNull { it.toLongOrNull() }
                .filter { scanStillCounts(it, now, WINDOW_MS) }
                .forEach { scanTimestamps.add(it) }
        }
    }
}
