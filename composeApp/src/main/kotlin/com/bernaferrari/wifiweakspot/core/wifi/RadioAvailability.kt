package com.bernaferrari.wifiweakspot.core.wifi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import org.koin.compose.koinInject

data class RadioState(val wifiEnabled: Boolean, val locationEnabled: Boolean)

/** Reads whether the Wi-Fi and Location switches are on. */
interface RadioMonitor {
    fun read(): RadioState
}

/** Wi-Fi and Location switches, re-read whenever the screen resumes. */
@Composable
fun rememberRadioState(monitor: RadioMonitor = koinInject()): RadioState {
    val lifecycleOwner = LocalLifecycleOwner.current
    var state by remember(monitor) { mutableStateOf(monitor.read()) }
    DisposableEffect(lifecycleOwner, monitor) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) state = monitor.read()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return state
}
