package com.pulsewave.app.core.wifi

import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build

fun Context.isWifiRadioEnabled(): Boolean {
    val manager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    return manager.isWifiEnabled
}

fun Context.isLocationRadioEnabled(): Boolean {
    val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        manager.isLocationEnabled
    } else {
        @Suppress("DEPRECATION")
        manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}

class AndroidRadioMonitor(private val context: Context) : RadioMonitor {
    override fun read() = RadioState(context.isWifiRadioEnabled(), context.isLocationRadioEnabled())
}
