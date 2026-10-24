package com.bernaferrari.wifiweakspot.core.designsystem.component

import com.bernaferrari.wifiweakspot.core.designsystem.animateAccessibleFloat
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotAlpha
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.permissions.PermissionStatus

enum class PermissionType {
    WIFI_SCAN,
    LOCATION,
    NEARBY_DEVICES
}

@Composable
fun PermissionRequiredCard(
    permissionType: PermissionType,
    status: PermissionStatus,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false
) {
    val infiniteTransition = rememberInfiniteTransition(label = "permission")
    val pulseScale by infiniteTransition.animateAccessibleFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val (icon, title, description, rationale) = when (permissionType) {
        PermissionType.WIFI_SCAN -> Quadruple(
            Icons.Default.Wifi,
            "Wi-Fi Scanning",
            "Discover and analyze networks around you",
            "Android requires location permission to scan Wi-Fi networks. Your location is never stored or shared."
        )
        PermissionType.LOCATION -> Quadruple(
            Icons.Default.LocationOn,
            "Location Access",
            "Required for Wi-Fi scanning on Android",
            "We only use location to enable Wi-Fi scanning. No location data is stored."
        )
        PermissionType.NEARBY_DEVICES -> Quadruple(
            Icons.Default.Wifi,
            "Nearby Devices",
            "Required on Android 13+ for Wi-Fi scanning",
            "This permission allows the app to discover nearby Wi-Fi networks."
        )
    }

    val isPermanentlyDenied = status == PermissionStatus.PERMANENTLY_DENIED

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = WifiWeakspotShapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(80.dp)
                    .then(if (!reduceMotion) Modifier.scale(pulseScale) else Modifier),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(Modifier.height(8.dp))

            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = WifiWeakspotAlpha.strong),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                shape = WifiWeakspotShapes.medium,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        rationale,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            if (isPermanentlyDenied) {
                Button(
                    onClick = onOpenSettings,
                    modifier = Modifier.fillMaxWidth(),
                    shape = WifiWeakspotShapes.medium
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Open Settings")
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    "Permission was denied. Please enable it in Settings.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = WifiWeakspotAlpha.medium),
                    textAlign = TextAlign.Center
                )
            } else {
                Button(
                    onClick = onRequestPermission,
                    modifier = Modifier.fillMaxWidth(),
                    shape = WifiWeakspotShapes.medium
                ) {
                    Text("Enable Permission")
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun FullScreenPermissionRequest(
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit,
    reduceMotion: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientOffset by infiniteTransition.animateAccessibleFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientOffset"
    )

    val actualGradientOffset = if (reduceMotion) 0.5f else gradientOffset

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = WifiWeakspotAlpha.light * actualGradientOffset),
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = WifiWeakspotAlpha.subtle * (1f - actualGradientOffset))
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WifiNetworkIllustration(size = 140.dp)

            Spacer(Modifier.height(48.dp))

            Text(
                "See Your\nInvisible World",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Text(
                "Discover and analyze Wi-Fi networks with beautiful visualizations",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            Surface(
                shape = WifiWeakspotShapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Why we need location",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Android requires location permission to scan Wi-Fi networks. We never store or share your location data.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = onRequestPermission,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = WifiWeakspotShapes.extraLarge
            ) {
                Text("Get Started", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onOpenSettings) {
                Text("Already denied? Open Settings", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
