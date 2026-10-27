package com.bernaferrari.wifiweakspot

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.bernaferrari.wifiweakspot.core.permissions.rememberAndroidPermissionLaunchers
import com.bernaferrari.wifiweakspot.core.platform.LocalBackHandler
import com.bernaferrari.wifiweakspot.core.platform.LocalPermissionLaunchers
import com.bernaferrari.wifiweakspot.core.platform.LocalWebContent

/** Android host for the shared [App]: activity-bound launchers, back gesture, and WebView. */
@Composable
fun AndroidApp() {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val locale = androidx.compose.ui.text.intl.Locale(configuration.locales[0].toLanguageTag())
    CompositionLocalProvider(
        com.bernaferrari.wifiweakspot.core.backup.LocalBackupDocumentPicker provides { onExport, onRestore ->
            com.bernaferrari.wifiweakspot.core.backup.rememberAndroidBackupDocumentPicker(onExport, onRestore)
        },
        LocalPermissionLaunchers provides rememberAndroidPermissionLaunchers(),
        LocalBackHandler provides { enabled, onBack -> BackHandler(enabled, onBack) },
        LocalWebContent provides { url, modifier -> AndroidWebPage(url, modifier) }
    ) {
        App(locale)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun AndroidWebPage(url: String, modifier: Modifier) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = WebViewClient()
                loadUrl(url)
            }
        },
        onRelease = { it.destroy() },
        modifier = modifier
    )
}
