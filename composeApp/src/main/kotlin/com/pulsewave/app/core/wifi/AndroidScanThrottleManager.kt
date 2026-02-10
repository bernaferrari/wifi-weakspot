package com.pulsewave.app.core.wifi

import android.content.Context
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

/**
 * Android implementation of ScanThrottleManager.
 * 
 * Tracks scan timestamps and calculates remaining quota.
 * Also checks if Developer Options "WiFi scan throttling" is disabled.
 * 
 * Now persists timestamps to survive app restarts.
 */
class AndroidScanThrottleManager(
    private val context: Context
) : ScanThrottleManager {
    
    companion object {
        private const val MAX_SCANS = 4
        private const val WINDOW_MS = 2 * 60 * 1000L // 2 minutes
        private const val WIFI_SCAN_THROTTLE_ENABLED = "wifi_scan_throttle_enabled"
        private const val PREFS_NAME = "wifi_throttle_prefs"
        private const val KEY_SCAN_TIMESTAMPS = "scan_timestamps"
    }
    
    private val scope = CoroutineScope(Dispatchers.Default)
    private var countdownJob: Job? = null
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Timestamps of recent scans (within the 2-minute window)
    private val scanTimestamps = mutableListOf<Long>()
    
    private val _throttleState = MutableStateFlow<ThrottleState>(ThrottleState.Unknown)
    override val throttleState: StateFlow<ThrottleState> = _throttleState.asStateFlow()
    
    override val isThrottlingDisabled: Flow<Boolean> = flow {
        emit(checkThrottlingDisabled())
    }
    
    init {
        restoreTimestamps()
        refresh()
    }
    
    override fun recordScan() {
        val now = System.currentTimeMillis()
        scanTimestamps.add(now)
        pruneOldTimestamps(now)
        persistTimestamps()
        updateState()
    }
    
    override fun refresh() {
        val now = System.currentTimeMillis()
        pruneOldTimestamps(now)
        updateState()
    }
    
    private fun pruneOldTimestamps(now: Long) {
        val before = scanTimestamps.size
        scanTimestamps.removeAll { now - it > WINDOW_MS }
        // Persist if we removed any timestamps
        if (before != scanTimestamps.size) {
            persistTimestamps()
        }
    }
    
    private fun updateState() {
        // Check if throttling is disabled first
        if (checkThrottlingDisabled()) {
            _throttleState.value = ThrottleState.Unlimited
            countdownJob?.cancel()
            return
        }
        
        val remaining = MAX_SCANS - scanTimestamps.size
        
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
                val oldestScan = scanTimestamps.minOrNull() ?: return
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
                    refresh()
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
    
    /**
     * Check if WiFi scan throttling is disabled in Developer Options.
     * 
     * The setting is: Settings.Global.WIFI_SCAN_THROTTLE_ENABLED
     * When disabled (0), the user bypassed throttling.
     */
    private fun checkThrottlingDisabled(): Boolean {
        return try {
            val value = Settings.Global.getInt(
                context.contentResolver,
                WIFI_SCAN_THROTTLE_ENABLED,
                1 // Default: throttling enabled
            )
            value == 0 // 0 means throttling is DISABLED
        } catch (e: Exception) {
            false // Assume throttling is enabled if we can't read the setting
        }
    }
    
    /**
     * Persist scan timestamps to SharedPreferences.
     * Stores as comma-separated string of timestamps.
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
