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
    
    /** Device setup that decides how often Android lets this app scan. */
    val setupStatus: StateFlow<ScanSetupStatus>

    /** Force a refresh of the throttle state and setup status, e.g. on resume. */
    fun refresh()
}

/**
 * Android 9+ throttles foreground scans to 4 per 2 minutes. Android 10+ lets people
 * turn that off in Developer options; the steps below guide them there.
 */
data class ScanSetupStatus(
    val throttlingApplies: Boolean = true,
    val canDisableThrottling: Boolean = true,
    val developerOptionsEnabled: Boolean = false,
    val throttlingEnabled: Boolean = true,
    /** `su` exists on this phone, so root can lift the limit instead of Developer options. */
    val rootAvailable: Boolean = false,
    /** The person opted in to root scanning and it was granted. */
    val rootScanning: Boolean = false
) {
    /** Scans run as often as the app asks. */
    val isUnlimited: Boolean get() = !throttlingApplies || !throttlingEnabled || rootScanning
    /** Worth suggesting to everyone: the Developer options switch exists and is still on. */
    val needsAttention: Boolean get() = !isUnlimited && canDisableThrottling
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
