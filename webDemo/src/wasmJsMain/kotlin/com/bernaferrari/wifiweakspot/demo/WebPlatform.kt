package com.bernaferrari.wifiweakspot.demo

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import com.bernaferrari.wifiweakspot.core.platform.PlatformActions
import com.bernaferrari.wifiweakspot.core.platform.PlatformTheme

/** The browser stands in for the phone: no system Settings, but links and the clipboard work. */
class WebPlatformActions : PlatformActions {
    override fun openWifiSettings() = Unit
    override fun openLocationSettings() = Unit
    override fun openAppSettings() = Unit
    override fun openDeveloperOptions() = Unit
    override fun openDeviceInfo() = Unit
    override fun share(text: String, title: String) = copyText(text)
    override fun showMessage(text: String) = logMessage(text)
    override fun copyToClipboard(label: String, text: String) = copyText(text)
    override fun needsNotificationPermission() = false
    override fun loadImage(path: String): ImageBitmap? = null
    override fun deleteOwnedFile(path: String) = Unit
    override fun openUrl(url: String) = openTab(url)
    override fun shareImage(image: ImageBitmap, title: String) = logMessage(com.bernaferrari.wifiweakspot.core.i18n.appStrings.common.mapSharingAndroidOnly)
}

class WebPlatformTheme : PlatformTheme {
    @Composable
    override fun dynamicColorScheme(dark: Boolean): ColorScheme? = null

    @Composable
    override fun ApplySystemBars(dark: Boolean) = Unit

    @Composable
    override fun systemReducedMotion(): Boolean = remember { prefersReducedMotion() }
}

fun copyText(text: String): Unit = js("{ navigator.clipboard && navigator.clipboard.writeText(text); }")
private fun logMessage(text: String): Unit = js("console.info(text)")
internal fun openTab(url: String): Unit = js("{ window.open(url, '_blank', 'noopener'); }")
private fun prefersReducedMotion(): Boolean = js("window.matchMedia('(prefers-reduced-motion: reduce)').matches")
