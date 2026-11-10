package com.bernaferrari.wifiweakspot.core.permissions

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.bernaferrari.wifiweakspot.core.platform.FloorplanPick
import com.bernaferrari.wifiweakspot.core.platform.PermissionLaunchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import java.io.File

/**
 * Activity-result launchers the shared UI calls through [PermissionLaunchers].
 * After two denials Android answers instantly without a dialog; then the app's settings
 * page opens instead, so "Allow" always leads somewhere.
 */
@Composable
fun rememberAndroidPermissionLaunchers(): PermissionLaunchers {
    val gate: PermissionGate = koinInject()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pending = remember { arrayOfNulls<(Boolean) -> Unit>(3) }
    val startedAt = remember { longArrayOf(0L) }
    // Saved with the screen: Android may rebuild it while the picker is open, and the photo must
    // still reach the map it was picked for.
    var floorplanTarget by rememberSaveable { mutableStateOf(0L) }
    val floorplanPick = remember { MutableStateFlow<FloorplanPick?>(null) }

    val scan = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        gate.checkPermissions()
        val granted = results.values.any { it }
        if (!granted && System.currentTimeMillis() - startedAt[0] < NO_DIALOG_MS) gate.openAppSettings()
        pending[0]?.invoke(granted)
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        val target = floorplanTarget
        scope.launch {
            // Copy into private storage; the temporary picker grant need not survive a restart.
            val path = withContext(Dispatchers.IO) {
                runCatching {
                    val folder = File(context.filesDir, "survey_floorplans").apply { mkdirs() }
                    val file = File(folder, "floorplan_${target}_${System.currentTimeMillis()}")
                    try {
                        context.contentResolver.openInputStream(uri)?.use { input ->
                            file.outputStream().use { output ->
                                val buffer = ByteArray(8192)
                                var total = 0L
                                while (true) {
                                    val read = input.read(buffer)
                                    if (read < 0) break
                                    total += read
                                    require(total <= 20L * 1024 * 1024) { "Image too large" }
                                    output.write(buffer, 0, read)
                                }
                            }
                        } ?: error("Image unavailable")
                        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
                        android.graphics.BitmapFactory.decodeFile(file.absolutePath, bounds)
                        require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Image unreadable" }
                        storeUpright(file, bounds.outMimeType)
                        file.absolutePath
                    } catch (error: Throwable) { file.delete(); throw error }
                }.getOrNull()
            }
            if (path != null) floorplanPick.value = FloorplanPick(target, path)
        }
    }

    return remember(scan, picker) {
        PermissionLaunchers(
            scan = { onResult ->
                pending[0] = onResult
                startedAt[0] = System.currentTimeMillis()
                scan.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            pickFloorplan = { target ->
                floorplanTarget = target
                picker.launch("image/*")
            },
            floorplanPick = floorplanPick,
            consumeFloorplanPick = { floorplanPick.value = null }
        )
    }
}

private const val NO_DIALOG_MS = 1_000L
