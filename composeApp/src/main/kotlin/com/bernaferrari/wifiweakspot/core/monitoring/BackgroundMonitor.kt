package com.bernaferrari.wifiweakspot.core.monitoring

import kotlinx.coroutines.flow.StateFlow

/** Optional background checks for new networks and signal changes. */
interface BackgroundMonitor {
    val isMonitoring: StateFlow<Boolean>
    val error: StateFlow<String?>
    val settings: StateFlow<MonitorSettings>
    fun startMonitoring()
    fun stopMonitoring()
    fun updateSettings(settings: MonitorSettings)
}

data class MonitorSettings(
    val scanIntervalMs: Long = 60_000L,
    val notifyNewNetworks: Boolean = true,
    val notifySignalChange: Boolean = false,
    val signalThresholdDb: Int = 15
) {
    val intervalMinutes: Float
        get() = scanIntervalMs / 60_000f

    fun withIntervalMinutes(minutes: Float) = copy(scanIntervalMs = (minutes.coerceIn(0.5f, 10f) * 60_000).toLong())
}

/** Signal level named in a background signal-change alert. */
enum class MonitorSignalLevel { Excellent, Good, Fair, Weak }
