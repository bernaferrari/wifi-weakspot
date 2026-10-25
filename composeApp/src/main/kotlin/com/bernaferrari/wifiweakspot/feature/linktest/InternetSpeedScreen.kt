@file:OptIn(ExperimentalMaterial3Api::class)

package com.bernaferrari.wifiweakspot.feature.linktest

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bernaferrari.wifiweakspot.core.i18n.strings
import com.bernaferrari.wifiweakspot.core.platform.LocalWebContent

/** Internet throughput is optional and runs on fast.com, kept apart from the local Wi-Fi test. */
@Composable
fun InternetSpeedScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.history.internetSpeedTitle) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, strings.common.back) } }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            LocalWebContent.current("https://fast.com", Modifier.fillMaxSize())
        }
    }
}
