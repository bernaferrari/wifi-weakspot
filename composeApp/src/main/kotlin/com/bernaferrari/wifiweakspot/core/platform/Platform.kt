package com.bernaferrari.wifiweakspot.core.platform

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

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

/** A photo picked for the map with id [destinationId], already copied to the app-owned [path]. */
data class FloorplanPick(val destinationId: Long, val path: String)

/** Launchers that need the host screen (Android activity results). */
class PermissionLaunchers(
    val scan: ((granted: Boolean) -> Unit) -> Unit = { it(true) },
    /** Opens an image picker for the map with this id. The result arrives in [floorplanPick]. */
    val pickFloorplan: (destinationId: Long) -> Unit = {},
    /** The picked photo stays here until the map screen receives it. */
    val floorplanPick: StateFlow<FloorplanPick?> = MutableStateFlow(null),
    /** Call once the pick has been taken, so it is not applied twice. */
    val consumeFloorplanPick: () -> Unit = {}
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
