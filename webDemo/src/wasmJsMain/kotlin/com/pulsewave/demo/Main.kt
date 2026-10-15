package com.pulsewave.demo

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
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.pulsewave.app.App
import com.pulsewave.app.core.platform.LocalPermissionLaunchers
import com.pulsewave.app.core.platform.LocalWebContent
import com.pulsewave.app.core.platform.PermissionLaunchers
import com.pulsewave.app.di.sharedModule
import kotlinx.browser.document
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

/** The real app, with the phone's radios and storage simulated. */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    startKoin { modules(sharedModule, webModule) }
    ComposeViewport(document.body!!) {
        val gate = koinInject<SimulatedPermissionGate>()
        val launchers = remember(gate) {
            PermissionLaunchers(scan = { onResult -> gate.grant(); onResult(true) })
        }
        CompositionLocalProvider(
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
