package com.pulsewave.app

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.pulsewave.app.core.permissions.rememberAndroidPermissionLaunchers
import com.pulsewave.app.core.platform.LocalBackHandler
import com.pulsewave.app.core.platform.LocalPermissionLaunchers
import com.pulsewave.app.core.platform.LocalWebContent

/** Android host for the shared [App]: activity-bound launchers, back gesture, and WebView. */
@Composable
fun AndroidApp() {
    CompositionLocalProvider(
        LocalPermissionLaunchers provides rememberAndroidPermissionLaunchers(),
        LocalBackHandler provides { enabled, onBack -> BackHandler(enabled, onBack) },
        LocalWebContent provides { url, modifier -> AndroidWebPage(url, modifier) }
    ) {
        App()
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
