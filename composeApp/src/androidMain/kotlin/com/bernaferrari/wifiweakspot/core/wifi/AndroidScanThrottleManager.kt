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
            val now = System.currentTimeMillis()
            mutex.withLock {
                scanTimestamps.add(now)
                pruneOldTimestampsInternal(now)
                persistTimestamps()
            }
            updateState()
        }
    }

    override fun refresh() {
        scope.launch {
            val now = System.currentTimeMillis()
            mutex.withLock {
                pruneOldTimestampsInternal(now)
            }
            updateState()
        }
    }

    private fun pruneOldTimestampsInternal(now: Long) {
        // Internal method - must be called while holding mutex
        scanTimestamps.removeAll { now - it > WINDOW_MS }
    }

    private suspend fun getTimestampCount(): Int {
        return mutex.withLock {
            scanTimestamps.size
        }
    }

    private suspend fun getOldestTimestamp(): Long? {
        return mutex.withLock {
            scanTimestamps.minOrNull()
        }
    }

    private suspend fun updateState() {
        _setupStatus.value = readSetupStatus()
        // Check if throttling is disabled first
        if (checkThrottlingDisabled()) {
            _throttleState.value = ThrottleState.Unlimited
            countdownJob?.cancel()
            return
        }

        val timestampCount = getTimestampCount()
        val remaining = MAX_SCANS - timestampCount

        when {
            remaining >= 3 -> {
                _throttleState.value = ThrottleState.Ready(remaining, MAX_SCANS)
                countdownJob?.cancel()
            }
            remaining in 1..2 -> {
                _throttleState.value = ThrottleState.LowQuota(remaining, MAX_SCANS)
                countdownJob?.cancel()
            }
            remaining <= 0 -> {
                // Calculate when the oldest scan will expire
                val oldestScan = getOldestTimestamp()
                if (oldestScan == null) {
                    _throttleState.value = ThrottleState.Ready(MAX_SCANS, MAX_SCANS)
                    return
                }

                val expiresAt = oldestScan + WINDOW_MS
                val now = System.currentTimeMillis()
                val secondsRemaining = ((expiresAt - now) / 1000).coerceAtLeast(0)

                _throttleState.value = ThrottleState.Exhausted(
                    secondsUntilNextScan = secondsRemaining,
                    cooldownDurationSeconds = WINDOW_MS / 1000
                )

                // Start countdown
                startCountdown(expiresAt)
            }
        }
    }

    private fun startCountdown(expiresAt: Long) {
        countdownJob?.cancel()
        countdownJob = scope.launch {
            while (true) {
                val now = System.currentTimeMillis()
                val secondsRemaining = ((expiresAt - now) / 1000).coerceAtLeast(0)

                if (secondsRemaining <= 0) {
                    // Quota restored!
                    mutex.withLock {
                        pruneOldTimestampsInternal(System.currentTimeMillis())
                        persistTimestamps()
                    }
                    updateState()
                    break
                }

                _throttleState.value = ThrottleState.Exhausted(
                    secondsUntilNextScan = secondsRemaining,
                    cooldownDurationSeconds = WINDOW_MS / 1000
                )

                delay(1000) // Update every second
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
                .filter { now - it <= WINDOW_MS } // Only restore valid timestamps
                .forEach { scanTimestamps.add(it) }
        }
    }
}
