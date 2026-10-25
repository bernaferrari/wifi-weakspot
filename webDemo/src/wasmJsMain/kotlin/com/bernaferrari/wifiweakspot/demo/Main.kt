package com.bernaferrari.wifiweakspot.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.bernaferrari.wifiweakspot.App
import com.bernaferrari.wifiweakspot.core.platform.LocalPermissionLaunchers
import com.bernaferrari.wifiweakspot.core.platform.LocalWebContent
import com.bernaferrari.wifiweakspot.core.platform.PermissionLaunchers
import com.bernaferrari.wifiweakspot.di.sharedModule
import kotlinx.browser.document
import kotlinx.browser.window
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

/** The real app, with the phone's radios and storage simulated. */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin { modules(sharedModule, webModule) }
    // Render the real phone layout at store-quality pixel density without upscaling captures.
    // Ordinary demo visits retain the browser's density.
    val captureDensity = window.location.search.removePrefix("?").split('&')
        .firstOrNull { it.startsWith("captureDensity=") }?.substringAfter('=')
        ?.toFloatOrNull()?.takeIf { it.isFinite() && it in 1f..4f }
    ComposeViewport(document.body!!) {
        val gate = koinInject<SimulatedPermissionGate>()
        val launchers = remember(gate) {
            PermissionLaunchers(scan = { onResult -> gate.grant(); onResult(true) })
        }
        val density = LocalDensity.current
        CompositionLocalProvider(
            LocalDensity provides (captureDensity?.let { Density(it, density.fontScale) } ?: density),
            LocalPermissionLaunchers provides launchers,
            LocalWebContent provides { url, modifier -> ExternalPage(url, modifier) }
        ) {
            App()
        }
    }
}

/** Most sites refuse to be framed, so the page opens in a browser tab instead of a WebView. */
@OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExternalPage(url: String, modifier: Modifier) {
    Box(modifier.padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(url.removePrefix("https://"), style = MaterialTheme.typography.titleLarge)
            Button(onClick = { openTab(url) }, shapes = ButtonDefaults.shapes()) { Text("Open in a new tab") }
        }
    }
}
