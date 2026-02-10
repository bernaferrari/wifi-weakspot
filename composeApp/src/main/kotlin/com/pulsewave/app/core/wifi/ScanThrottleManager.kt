package com.pulsewave.app.core.wifi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Manages WiFi scan throttling state.
 * 
 * Android limits scans to 4 per 2 minutes (foreground).
 * This manager tracks quota and cooldown times.
 */
interface ScanThrottleManager {
    /** Current throttle state */
    val throttleState: StateFlow<ThrottleState>
    
    /** Whether developer mode throttling bypass is enabled */
    val isThrottlingDisabled: Flow<Boolean>
    
    /** Record a scan attempt */
    fun recordScan()
    
    /** Force a refresh of the throttle state */
    fun refresh()
}

sealed interface ThrottleState {
    /** Ready to scan, plenty of quota */
    data class Ready(val scansRemaining: Int, val maxScans: Int = 4) : ThrottleState
    
    /** Low quota warning */
    data class LowQuota(val scansRemaining: Int, val maxScans: Int = 4) : ThrottleState
    
    /** Quota exhausted, must wait */
    data class Exhausted(
        val secondsUntilNextScan: Long,
        val cooldownDurationSeconds: Long = 120
    ) : ThrottleState
    
    /** Developer mode detected - unlimited scans! */
    data object Unlimited : ThrottleState
    
    /** Unknown/checking state */
    data object Unknown : ThrottleState
}
