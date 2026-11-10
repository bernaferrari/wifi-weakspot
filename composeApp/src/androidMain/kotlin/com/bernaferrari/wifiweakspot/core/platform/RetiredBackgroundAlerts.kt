package com.bernaferrari.wifiweakspot.core.platform

import android.app.NotificationManager
import android.content.Context

/**
 * Version 1 could watch Wi-Fi in the background and post alerts. The feature is gone; this removes
 * what it left on phones that had it: two notification categories in Android's settings and its
 * saved options. Safe to run every start: both calls do nothing when there is nothing to remove.
 */
fun removeRetiredBackgroundAlerts(context: Context) {
    runCatching {
        val notifications = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifications.deleteNotificationChannel("wifiweakspot_monitoring")
        notifications.deleteNotificationChannel("wifiweakspot_alerts")
        context.deleteSharedPreferences("monitoring_prefs")
    }
}
