package com.bernaferrari.wifiweakspot

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.bernaferrari.wifiweakspot.core.permissions.rememberAndroidPermissionLaunchers
import com.bernaferrari.wifiweakspot.core.platform.LocalAppVersion
import com.bernaferrari.wifiweakspot.core.platform.LocalBackHandler
import com.bernaferrari.wifiweakspot.core.platform.LocalPermissionLaunchers
import com.bernaferrari.wifiweakspot.core.platform.LocalWebContent

/** Android host for the shared [App]: activity-bound launchers, back gesture, and WebView. */
@Composable
fun AndroidApp() {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val context = LocalContext.current
    val version = remember(context) {
        @Suppress("DEPRECATION")
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }
    val locale = androidx.compose.ui.text.intl.Locale(configuration.locales[0].toLanguageTag())
    CompositionLocalProvider(
        LocalAppVersion provides version,
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
    if (!isSpeedTestPage(Uri.parse(url))) return
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = false
                settings.allowContentAccess = false
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val target = request.url
                        if (isSpeedTestPage(target)) return false
                        if (request.isForMainFrame && request.hasGesture() && target.scheme == "https") {
                            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, target)) }
                        }
                        return true
                    }
                }
                loadUrl(url)
            }
        },
        onRelease = { it.destroy() },
        modifier = modifier
    )
}

private fun isSpeedTestPage(uri: Uri): Boolean =
    uri.scheme == "https" && uri.userInfo == null && (uri.port == -1 || uri.port == 443) &&
        (uri.host == "fast.com" || uri.host == "www.fast.com")
