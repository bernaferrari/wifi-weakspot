package com.pulsewave.app.core.wifi

import android.content.Context
import android.location.LocationManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

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

data class RadioState(val wifiEnabled: Boolean, val locationEnabled: Boolean)

/** Wi-Fi and Location switches, re-read whenever the screen resumes. */
@androidx.compose.runtime.Composable
fun rememberRadioState(): RadioState {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var state by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(RadioState(context.isWifiRadioEnabled(), context.isLocationRadioEnabled()))
    }
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                state = RadioState(context.isWifiRadioEnabled(), context.isLocationRadioEnabled())
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return state
}
