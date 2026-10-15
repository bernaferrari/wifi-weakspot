package com.pulsewave.app.core.monitoring

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Elegant manager for background monitoring.
 * Provides a clean API for the UI layer.
 */
class BackgroundMonitorManager(
    private val context: Context
) : BackgroundMonitor {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        BackgroundMonitorService.PREF_KEY,
        Context.MODE_PRIVATE
    )

    override val isMonitoring: StateFlow<Boolean> = BackgroundMonitorService.runningState
    override val error: StateFlow<String?> = BackgroundMonitorService.errorState

    private val _settings = MutableStateFlow(loadSettings())
    override val settings: StateFlow<MonitorSettings> = _settings.asStateFlow()

    override fun startMonitoring() {
        val intent = Intent(context, BackgroundMonitorService::class.java).apply {
            action = BackgroundMonitorService.ACTION_START
        }
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else context.startService(intent)
            BackgroundMonitorService.errorState.value = null
        } catch (_: Exception) {
            BackgroundMonitorService.errorState.value = "Monitoring could not start. Check scan permission and try again."
        }
    }

    override fun stopMonitoring() {
        context.stopService(Intent(context, BackgroundMonitorService::class.java))

    }

    fun toggleMonitoring() {
        if (isMonitoring.value) stopMonitoring() else startMonitoring()
    }

    override fun updateSettings(settings: MonitorSettings) {
        prefs.edit()
            .putLong(BackgroundMonitorService.PREF_INTERVAL, settings.scanIntervalMs)
            .putBoolean(BackgroundMonitorService.PREF_NOTIFY_NEW, settings.notifyNewNetworks)
            .putBoolean(BackgroundMonitorService.PREF_NOTIFY_SIGNAL, settings.notifySignalChange)
            .putInt(BackgroundMonitorService.PREF_SIGNAL_THRESHOLD, settings.signalThresholdDb)
            .apply()
        _settings.value = settings
    }

    private fun loadSettings(): MonitorSettings {
        return MonitorSettings(
            scanIntervalMs = prefs.getLong(
                BackgroundMonitorService.PREF_INTERVAL,
                BackgroundMonitorService.DEFAULT_INTERVAL
            ),
            notifyNewNetworks = prefs.getBoolean(BackgroundMonitorService.PREF_NOTIFY_NEW, true),
            notifySignalChange = prefs.getBoolean(BackgroundMonitorService.PREF_NOTIFY_SIGNAL, false),
            signalThresholdDb = prefs.getInt(BackgroundMonitorService.PREF_SIGNAL_THRESHOLD, 15)
        )
    }
}
