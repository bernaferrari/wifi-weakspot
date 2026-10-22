@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.designsystem.component.ShapeIcon
import com.pulsewave.app.core.designsystem.component.SignalMorphBlob
import com.pulsewave.app.core.wifi.SignalQuality

/** Who made it and what it promises, in four lines rather than four paragraphs. */
@Composable
fun AboutScreen(onBack: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Scaffold(topBar = {
        TopAppBar(title = {}, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignalMorphBlob(quality = SignalQuality.Excellent, color = scheme.primaryContainer, size = 120.dp) {
                Icon(Icons.Rounded.Wifi, null, Modifier.size(52.dp), tint = scheme.onPrimaryContainer)
            }
            Spacer(Modifier.height(20.dp))
            Text("WiFi\u00A0Weakspot", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Version 1.0", style = MaterialTheme.typography.bodyLarge, color = scheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))

            val rows = listOf(
                AboutRow(Icons.Rounded.Lock, MaterialShapes.Cookie6Sided, "Private", "Your results never leave this phone."),
                AboutRow(Icons.Rounded.Straighten, MaterialShapes.Sunny, "Honest numbers", "Signal and distance are estimates. Walls change everything."),
                AboutRow(Icons.Rounded.Code, MaterialShapes.Cookie9Sided, "Open source", "Apache License 2.0"),
                AboutRow(Icons.Rounded.Favorite, MaterialShapes.Clover4Leaf, "Built with", "Kotlin, Compose and Material 3 Expressive")
            )
            Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                rows.forEachIndexed { index, row ->
                    SegmentedListItem(
                        onClick = {},
                        enabled = false,
                        shapes = ListItemDefaults.segmentedShapes(index = index, count = rows.size),
                        colors = ListItemDefaults.colors(containerColor = scheme.surfaceContainerLow, disabledContainerColor = scheme.surfaceContainerLow, disabledContentColor = scheme.onSurface),
                        leadingContent = { ShapeIcon(row.icon, row.shape, scheme.secondaryContainer, scheme.onSecondaryContainer, size = 40.dp, iconSize = 20.dp) },
                        supportingContent = { Text(row.body) }
                    ) { Text(row.title, style = MaterialTheme.typography.titleMedium) }
                }
            }
        }
    }
}

private class AboutRow(val icon: ImageVector, val shape: androidx.graphics.shapes.RoundedPolygon, val title: String, val body: String)
