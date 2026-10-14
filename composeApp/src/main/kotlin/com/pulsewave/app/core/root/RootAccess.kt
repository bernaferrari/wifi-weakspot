package com.pulsewave.app.core.root

import android.content.Context
import android.os.Build
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.concurrent.TimeUnit

enum class RootState {
    /** No `su` on this phone: the normal, unrooted path. */
    NotRooted,
    /** `su` exists; the person has not opted in. */
    Available,
    /** Opted in and granted: scans run through the system shell. */
    Enabled,
    /** The root manager refused or timed out. */
    Denied
}

/**
 * Optional root support. Scans requested by the system shell are not throttled and their
 * results reach every app, so rooted phones get live scanning without Developer options.
 * Root is only requested after the person taps "Use root".
 */
interface RootAccess {
    val state: StateFlow<RootState>
    val isEnabled: Boolean get() = state.value == RootState.Enabled

    /** Shows the root manager's prompt. Returns true when root was granted. */
    suspend fun enable(): Boolean
    fun disable()

    /** Fire-and-forget scan through the root shell. Returns false when root is not enabled. */
    fun requestScan(): Boolean
}

class AndroidRootAccess(context: Context) : RootAccess {
    private val prefs = context.getSharedPreferences("root_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val _state = MutableStateFlow(initialState())
    override val state: StateFlow<RootState> = _state.asStateFlow()

    private fun initialState(): RootState = when {
        !isSuInstalled() -> RootState.NotRooted
        prefs.getBoolean(KEY_ENABLED, false) -> RootState.Enabled
        else -> RootState.Available
    }

    override suspend fun enable(): Boolean = withContext(Dispatchers.IO) {
        if (!isSuInstalled()) {
            _state.value = RootState.NotRooted
            return@withContext false
        }
        val granted = run("id", timeoutSeconds = 30)?.contains("uid=0") == true
        if (granted && Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            // Android 9 and 10 keep the throttle in a global setting that root can switch off.
            run("settings put global wifi_scan_throttle_enabled 0", timeoutSeconds = 10)
        }
        prefs.edit().putBoolean(KEY_ENABLED, granted).apply()
        _state.value = if (granted) RootState.Enabled else RootState.Denied
        granted
    }

    override fun disable() {
        prefs.edit().putBoolean(KEY_ENABLED, false).apply()
        _state.value = if (isSuInstalled()) RootState.Available else RootState.NotRooted
    }

    override fun requestScan(): Boolean {
        if (!isEnabled) return false
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return false // Throttle already off; use the app scan.
        scope.launch {
            if (run("cmd wifi start-scan", timeoutSeconds = 10) == null) {
                // Root was revoked in the root manager; fall back to the normal path.
                prefs.edit().putBoolean(KEY_ENABLED, false).apply()
                _state.value = RootState.Denied
            }
        }
        return true
    }

    private fun run(command: String, timeoutSeconds: Long): String? = runCatching {
        val process = ProcessBuilder("su", "-c", command).redirectErrorStream(true).start()
        if (!process.waitFor(timeoutSeconds, TimeUnit.SECONDS)) {
            process.destroy()
            return null
        }
        val output = process.inputStream.bufferedReader().use { it.readText() }
        output.takeIf { process.exitValue() == 0 }
    }.getOrNull()

    private fun isSuInstalled(): Boolean =
        SuPaths.any { File(it).exists() } ||
            System.getenv("PATH").orEmpty().split(':').any { it.isNotBlank() && File(it, "su").exists() }

    private companion object {
        const val KEY_ENABLED = "root_scans_enabled"
        val SuPaths = listOf("/system/bin/su", "/system/xbin/su", "/sbin/su", "/su/bin/su", "/debug_ramdisk/su")
    }
}
