@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.lan

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Laptop
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Smartphone
import androidx.compose.material.icons.rounded.Speaker
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind

/** The router and this phone get their own shapes and colors; everything else shares one calm style. */
@Composable
fun DeviceIcon(kind: DeviceKind, modifier: Modifier = Modifier, size: Dp = 44.dp, dimmed: Boolean = false) {
    val scheme = MaterialTheme.colorScheme
    val icon = when (kind) {
        DeviceKind.Router -> Icons.Rounded.Router
        DeviceKind.ThisPhone, DeviceKind.Phone -> Icons.Rounded.Smartphone
        DeviceKind.Computer -> Icons.Rounded.Laptop
        DeviceKind.Tv -> Icons.Rounded.Tv
        DeviceKind.Speaker -> Icons.Rounded.Speaker
        DeviceKind.Printer -> Icons.Rounded.Print
        DeviceKind.Console -> Icons.Rounded.SportsEsports
        DeviceKind.Camera -> Icons.Rounded.Videocam
        DeviceKind.SmartHome -> Icons.Rounded.Lightbulb
        DeviceKind.Unknown -> Icons.Rounded.Devices
    }
    val (shape, container, content) = when {
        dimmed -> Triple(MaterialShapes.Square, scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
        kind == DeviceKind.Router -> Triple(MaterialShapes.Cookie9Sided, scheme.primaryContainer, scheme.onPrimaryContainer)
        kind == DeviceKind.ThisPhone -> Triple(MaterialShapes.Pill, scheme.tertiaryContainer, scheme.onTertiaryContainer)
        kind == DeviceKind.Unknown -> Triple(MaterialShapes.Square, scheme.surfaceContainerHighest, scheme.onSurfaceVariant)
        else -> Triple(MaterialShapes.Square, scheme.secondaryContainer, scheme.onSecondaryContainer)
    }
    ShapeIcon(icon, shape, container, content, modifier, size = size, iconSize = size * 0.5f)
}
