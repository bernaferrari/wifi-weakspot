package com.pulsewave.app.core.permissions

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.pulsewave.app.core.platform.PermissionLaunchers
import kotlinx.coroutines.Dispatchers
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
    val floorplanTarget = remember { longArrayOf(0L) }
    val floorplanCallback = remember { arrayOfNulls<(String?) -> Unit>(1) }

    val scan = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { results ->
        gate.checkPermissions()
        val granted = results.values.any { it }
        if (!granted && System.currentTimeMillis() - startedAt[0] < NO_DIALOG_MS) gate.openAppSettings()
        pending[0]?.invoke(granted)
    }
    val notifications = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { pending[1]?.invoke(it) }
    val activity = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { pending[2]?.invoke(it) }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val done = floorplanCallback[0] ?: return@rememberLauncherForActivityResult
        if (uri == null) { done(null); return@rememberLauncherForActivityResult }
        scope.launch {
            // Copy into private storage; the temporary picker grant need not survive a restart.
            val path = withContext(Dispatchers.IO) {
                runCatching {
                    val folder = File(context.filesDir, "survey_floorplans").apply { mkdirs() }
                    val file = File(folder, "floorplan_${floorplanTarget[0]}_${System.currentTimeMillis()}")
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
                        file.absolutePath
                    } catch (error: Exception) { file.delete(); throw error }
                }.getOrNull()
            }
            done(path)
        }
    }

    return remember(scan, notifications, activity, picker) {
        PermissionLaunchers(
            scan = { onResult ->
                pending[0] = onResult
                startedAt[0] = System.currentTimeMillis()
                scan.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
            },
            notifications = { onResult ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) onResult(true)
                else { pending[1] = onResult; notifications.launch(Manifest.permission.POST_NOTIFICATIONS) }
            },
            activityRecognition = { onResult ->
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) onResult(true)
                else { pending[2] = onResult; activity.launch(Manifest.permission.ACTIVITY_RECOGNITION) }
            },
            pickFloorplan = { target, onPicked ->
                floorplanTarget[0] = target
                floorplanCallback[0] = onPicked
                picker.launch("image/*")
            }
        )
    }
}

private const val NO_DIALOG_MS = 1_000L
