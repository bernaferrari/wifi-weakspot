package com.pulsewave.app.core.permissions

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import org.koin.compose.koinInject

/**
 * Asks for the location access Wi-Fi scans need. After two denials Android stops showing the
 * dialog and answers instantly; then the app's settings page opens instead, so "Allow" always
 * leads somewhere.
 */
@Composable
fun rememberScanPermissionRequest(onResult: (granted: Boolean) -> Unit = {}): () -> Unit {
    val gate: PermissionGate = koinInject()
    val latestOnResult = rememberUpdatedState(onResult)
    val startedAt = remember { longArrayOf(0L) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        gate.checkPermissions()
        val granted = results.values.any { it }
        if (!granted && System.currentTimeMillis() - startedAt[0] < NO_DIALOG_MS) gate.openAppSettings()
        latestOnResult.value(granted)
    }
    return remember(launcher) {
        {
            startedAt[0] = System.currentTimeMillis()
            launcher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }
}

private const val NO_DIALOG_MS = 1_000L
