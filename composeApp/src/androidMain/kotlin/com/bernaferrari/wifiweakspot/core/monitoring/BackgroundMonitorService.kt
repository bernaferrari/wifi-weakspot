package com.bernaferrari.wifiweakspot.core.monitoring

import com.bernaferrari.wifiweakspot.core.i18n.appStrings
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import com.bernaferrari.wifiweakspot.core.wifi.WifiScanner
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

/**
 * Elegant background monitoring service.
 * Scans WiFi periodically and notifies on changes.
 */
class BackgroundMonitorService : Service(), KoinComponent {

    private val wifiScanner: WifiScanner by inject()
    private val binder = LocalBinder()

    private var monitoringJob: Job? = null
    private val settingsChanges = kotlinx.coroutines.channels.Channel<Unit>(kotlinx.coroutines.channels.Channel.CONFLATED)
    private val settingsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == PREF_INTERVAL) settingsChanges.trySend(Unit)
    }
    private var stopping = false
    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var knownNetworks = mutableSetOf<String>()
    private var lastScanResults = listOf<WifiNetwork>()
    private var lastNotificationTime = 0L

    val isMonitoring: StateFlow<Boolean> = runningState

    private val _networksFound = MutableStateFlow(0)
    val networksFound: StateFlow<Int> = _networksFound

    private val _lastScanTime = MutableStateFlow("")
    val lastScanTime: StateFlow<String> = _lastScanTime

    companion object {
        const val CHANNEL_ID_MONITORING = "wifiweakspot_monitoring"
        const val CHANNEL_ID_ALERTS = "wifiweakspot_alerts"
        const val NOTIFICATION_ID_MONITORING = 2001
        const val NOTIFICATION_ID_ALERT = 2002

        const val ACTION_START = "com.bernaferrari.wifiweakspot.action.START_MONITORING"
        const val ACTION_STOP = "com.bernaferrari.wifiweakspot.action.STOP_MONITORING"

        const val PREF_KEY = "monitoring_prefs"
        const val PREF_INTERVAL = "scan_interval_ms"
        const val PREF_NOTIFY_NEW = "notify_new_networks"
        const val PREF_NOTIFY_SIGNAL = "notify_signal_change"
        const val PREF_SIGNAL_THRESHOLD = "signal_threshold_db"

        const val DEFAULT_INTERVAL = 60_000L
        const val MIN_NOTIFICATION_INTERVAL = 60_000L // 1 minute between alerts

        private val _isMonitoring = MutableStateFlow(false)
        val runningState: StateFlow<Boolean> = _isMonitoring
        val errorState = MutableStateFlow<String?>(null)

        private val _serviceInstance = MutableStateFlow<BackgroundMonitorService?>(null)
        val serviceInstance: StateFlow<BackgroundMonitorService?> = _serviceInstance

        fun isRunning(): Boolean = _isMonitoring.value
    }

    inner class LocalBinder : Binder() {
        fun getService(): BackgroundMonitorService = this@BackgroundMonitorService
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        _serviceInstance.value = this
        getPrefs().registerOnSharedPreferenceChangeListener(settingsListener)
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return when (intent?.action) {
            ACTION_STOP -> {
                stopMonitoring()
                START_NOT_STICKY
            }
            else -> {
                try {
                    startMonitoring()
                } catch (error: Exception) {
                    errorState.value = appStrings.domain.monitorStartFailed
                    stopMonitoring()
                }
                START_NOT_STICKY
            }
        }
    }

    override fun onDestroy() {
        stopping = true
        monitoringJob?.cancel()
        monitoringJob = null
        _isMonitoring.value = false
        _serviceInstance.value = null
        getPrefs().unregisterOnSharedPreferenceChangeListener(settingsListener)
        settingsChanges.close()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Monitoring status channel (low importance - no sound)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_MONITORING,
                    appStrings.domain.monitorChannelName,
                    NotificationManager.IMPORTANCE_LOW
                ).apply {
                    description = appStrings.domain.monitorChannelDescription
                    setShowBadge(false)
                }
            )

            // Alerts channel (high importance - with sound)
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    appStrings.domain.alertsChannelName,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = appStrings.domain.alertsChannelDescription
                    enableVibration(true)
                }
            )
        }
    }

    private fun getPrefs(): SharedPreferences = getSharedPreferences(PREF_KEY, Context.MODE_PRIVATE)

    fun startMonitoring() {
        stopping = false
        startInForeground()
        if (_isMonitoring.value && monitoringJob?.isActive == true) return

        _isMonitoring.value = true
        errorState.value = null

        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                try {
                    performScan()
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    errorState.value = appStrings.survey.couldNotScan
                }
                val interval = getPrefs().getLong(PREF_INTERVAL, DEFAULT_INTERVAL).coerceIn(30_000L, 600_000L)
                withTimeoutOrNull(interval) { settingsChanges.receive() }
            }
        }

        updateNotification()
    }

    private fun startInForeground() {
        val notification = createMonitoringNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID_MONITORING,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID_MONITORING, notification)
        }
    }

    fun stopMonitoring() {
        if (stopping) return
        stopping = true
        monitoringJob?.cancel()
        monitoringJob = null
        _isMonitoring.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private suspend fun performScan() {
        val networks = wifiScanner.awaitNextScan(timeoutMs = 8_000)
        if (networks == null) {
            errorState.value = appStrings.domain.monitorWaitingForScanLimited
            updateNotification()
            return
        }
        errorState.value = null
        val previous = lastScanResults
        lastScanResults = networks
        _networksFound.value = networks.size
        _lastScanTime.value = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

        checkForChanges(previous, networks)
        updateNotification()
    }

    private fun checkForChanges(previous: List<WifiNetwork>, networks: List<WifiNetwork>) {
        val prefs = getPrefs()
        val notifyNew = prefs.getBoolean(PREF_NOTIFY_NEW, true)
        val notifySignal = prefs.getBoolean(PREF_NOTIFY_SIGNAL, false)
        val signalThreshold = prefs.getInt(PREF_SIGNAL_THRESHOLD, 15)

        val now = System.currentTimeMillis()
        val currentBssids = networks.map { it.bssid }.toSet()
        val newNetworks = currentBssids - knownNetworks
        val hadBaseline = knownNetworks.isNotEmpty()
        knownNetworks.addAll(currentBssids)
        if (knownNetworks.size > 2_000) knownNetworks = currentBssids.toMutableSet()
        if (now - lastNotificationTime < MIN_NOTIFICATION_INTERVAL) return

        // Check for new networks
        if (notifyNew) {
            if (newNetworks.isNotEmpty() && hadBaseline) {
                val newSsids = networks
                    .filter { it.bssid in newNetworks }
                    .map { it.ssid.ifEmpty { appStrings.common.hiddenNetworkShort } }
                    .take(3)

                showNewNetworkAlert(newSsids)
                lastNotificationTime = now
            }
        }

        // Check for significant signal changes
        if (notifySignal && previous.isNotEmpty() && now - lastNotificationTime >= MIN_NOTIFICATION_INTERVAL) {
            val previousBest = previous.maxByOrNull { it.rssi }
            val currentBest = networks.maxByOrNull { it.rssi }

            if (previousBest != null && currentBest != null && previousBest.bssid == currentBest.bssid) {
                val signalChange = abs(currentBest.rssi - previousBest.rssi)
                if (signalChange >= signalThreshold) {
                    showSignalChangeAlert(currentBest.ssid.ifEmpty { appStrings.common.yourNetwork }, currentBest.rssi)
                    lastNotificationTime = now
                }
            }
        }

    }

    private fun showNewNetworkAlert(newSsids: List<String>) {
        showAlertNotification(
            appStrings.domain.newNetworksTitle(newSsids.size),
            appStrings.domain.newNetworksText(newSsids)
        )
    }

    private fun showSignalChangeAlert(networkName: String, newRssi: Int) {
        val level = when {
            newRssi > -50 -> MonitorSignalLevel.Excellent
            newRssi > -60 -> MonitorSignalLevel.Good
            newRssi > -70 -> MonitorSignalLevel.Fair
            else -> MonitorSignalLevel.Weak
        }

        showAlertNotification(
            appStrings.domain.signalChangeTitle,
            appStrings.domain.signalChangeText(networkName, level, newRssi)
        )
    }

    private fun showAlertNotification(title: String, text: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID_ALERTS)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notifySafely(NOTIFICATION_ID_ALERT + (System.currentTimeMillis() % 1000).toInt(), notification)
    }

    private fun notifySafely(id: Int, notification: Notification) {
        if (Build.VERSION.SDK_INT >= 33 &&
            androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this).notify(id, notification)
    }

    private fun createMonitoringNotification(text: String = appStrings.domain.monitorWaitingForScan): Notification {
        val stopIntent = Intent(this, BackgroundMonitorService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_MONITORING)
            .setContentTitle("WiFi\u00A0Weakspot")
            .setContentText(text)
            .setContentIntent(PendingIntent.getActivity(this, 1, packageManager.getLaunchIntentForPackage(packageName), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT))
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setOngoing(true)
            .setSilent(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, appStrings.common.stop, stopPendingIntent)
            .build()
    }

    private fun updateNotification() {
        if (!_isMonitoring.value) return

        val text = errorState.value ?: appStrings.domain.monitorStatus(_networksFound.value, _lastScanTime.value)
        val notification = createMonitoringNotification(text)

        notifySafely(NOTIFICATION_ID_MONITORING, notification)
    }
}
