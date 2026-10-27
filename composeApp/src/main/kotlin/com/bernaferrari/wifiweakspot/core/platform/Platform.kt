package com.bernaferrari.wifiweakspot.core.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap

/**
 * Everything the shared UI needs from the device. Android implements it with intents and
 * system services; the web demo simulates it. Screens never touch platform APIs directly.
 */
interface PlatformActions {
    fun openWifiSettings()
    fun openLocationSettings()
    fun openAppSettings()
    fun openDeveloperOptions()
    fun openDeviceInfo()
    fun share(text: String, title: String)
    /** Brief, non-blocking notice such as "Command copied". */
    fun showMessage(text: String)
    fun copyToClipboard(label: String, text: String)
    /** Android 13+ asks before an app may post notifications. */
    fun needsNotificationPermission(): Boolean
    /** Decodes an image the app saved earlier (for example a floor plan). */
    suspend fun loadImage(path: String): ImageBitmap?
    /** Deletes a file the app owns, such as a replaced floor plan. */
    fun deleteOwnedFile(path: String)
    fun openUrl(url: String)
    /** Shares a picture, such as the floor-plan heatmap, through the system share sheet. */
    fun shareImage(image: ImageBitmap, title: String)
}

/** Look-and-feel hooks that only the platform can answer. */
interface PlatformTheme {
    /** Wallpaper-based colors, when the platform has them. */
    @Composable
    fun dynamicColorScheme(dark: Boolean): ColorScheme?

    /** Light or dark status and navigation bar icons. */
    @Composable
    fun ApplySystemBars(dark: Boolean)

    /** True when the system turned animations off. */
    @Composable
    fun systemReducedMotion(): Boolean
}

/** Launchers that need the host screen (Android activity results). */
class PermissionLaunchers(
    val scan: ((granted: Boolean) -> Unit) -> Unit = { it(true) },
    val notifications: ((granted: Boolean) -> Unit) -> Unit = { it(true) },
    /** Opens an image picker; the callback receives an app-owned path or null. */
    val pickFloorplan: (destinationId: Long, (path: String?) -> Unit) -> Unit = { _, done -> done(null) }
)

val LocalPermissionLaunchers = staticCompositionLocalOf { PermissionLaunchers() }

/** Asks for the location access Wi-Fi scans need. */
@Composable
fun rememberScanPermissionRequest(onResult: (granted: Boolean) -> Unit = {}): () -> Unit {
    val launchers = LocalPermissionLaunchers.current
    val latest = rememberUpdatedState(onResult)
    return remember(launchers) { { launchers.scan { latest.value(it) } } }
}

/** System back, where the platform has one (Android's back gesture). */
val LocalBackHandler = staticCompositionLocalOf<@Composable (enabled: Boolean, onBack: () -> Unit) -> Unit> { { _, _ -> } }

/** Shows a web page inside the app where the platform can (Android's WebView). */
val LocalWebContent = staticCompositionLocalOf<@Composable (url: String, modifier: Modifier) -> Unit> { { _, _ -> } }
