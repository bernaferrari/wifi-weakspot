@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon
import com.bernaferrari.wifiweakspot.core.designsystem.component.SignalMorphBlob
import com.bernaferrari.wifiweakspot.core.designsystem.rememberWifiWeakspotHapticFeedback
import com.bernaferrari.wifiweakspot.core.designsystem.signalColors
import com.bernaferrari.wifiweakspot.core.permissions.PermissionGate
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import kotlinx.coroutines.delay
import org.koin.compose.koinInject

/**
 * First run, on one page: what the app does, then the single permission it needs, asked in
 * the same breath so starting is one tap. Faster scans are offered later, when they matter.
 */
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    modifier: Modifier = Modifier
) {
    val permissionGate: PermissionGate = koinInject()
    val permissions by permissionGate.state.collectAsStateWithLifecycle()
    val haptics = rememberWifiWeakspotHapticFeedback()
    // Fixed at launch so granting access doesn't swap the buttons mid-tap.
    val needsAccess = remember { !permissions.canScanWifi }
    val finish = { haptics.click(); onFinish() }
    val requestPermission = com.bernaferrari.wifiweakspot.core.platform.rememberScanPermissionRequest { finish() }

    Surface(modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        // A phone-width column, centered, so tablets and desktop windows don't stretch it.
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 560.dp).fillMaxSize().safeDrawingPadding().padding(horizontal = 24.dp, vertical = 16.dp)) {
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    WelcomePage()
                }
                Column(Modifier.fillMaxWidth().padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    if (needsAccess) {
                        PrivacyNote()
                        PrimaryButton("Allow nearby Wi-Fi", onClick = requestPermission)
                        TextButton(onClick = finish) { Text("Not now") }
                    } else {
                        PrimaryButton("Get started", onClick = finish)
                    }
                }
            }
        }
    }
}

/** Why Android will ask about location, said before the system dialog appears. */
@Composable
private fun PrivacyNote() {
    Text(
        "Android calls this location access. This app never saves where you are, and your results stay on this device.",
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PrimaryButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shapes = ButtonDefaults.shapes(),
        modifier = Modifier.fillMaxWidth().heightIn(min = ButtonDefaults.MediumContainerHeight)
    ) { Text(text, style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight)) }
}

/** The shape cycles from dead zone to excellent: the same language the whole app uses. */
@Composable
private fun WelcomePage() {
    val reduceMotion = LocalReduceMotion.current
    var quality by remember { mutableStateOf(if (reduceMotion) SignalQuality.Excellent else SignalQuality.DeadZone) }
    LaunchedEffect(reduceMotion) {
        if (reduceMotion) return@LaunchedEffect
        val cycle = SignalQuality.entries.reversed()
        var i = 0
        while (true) {
            quality = cycle[i % cycle.size]
            delay(if (quality == SignalQuality.Excellent) 2_400 else 1_100)
            i++
        }
    }
    val colors = signalColors(quality)
    SignalMorphBlob(quality = quality, color = colors.container, size = 168.dp) {
        Icon(Icons.Rounded.Wifi, null, Modifier.size(68.dp), tint = colors.onContainer)
    }
    Spacer(Modifier.height(24.dp))
    BoxWithConstraints(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Text(
            "Find bad Wi\u2011Fi\nat home",
            style = if (maxWidth < 320.dp) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() }
        )
    }
    Spacer(Modifier.height(12.dp))
    Text(
        "See where it drops and how to fix it.",
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
    Spacer(Modifier.height(28.dp))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        val scheme = MaterialTheme.colorScheme
        FeatureRow(Icons.Rounded.MeetingRoom, MaterialShapes.SoftBurst, scheme.tertiaryContainer, scheme.onTertiaryContainer,
            "Check every room", "Find the rooms where it drops.")
        FeatureRow(Icons.Rounded.Speed, MaterialShapes.Sunny, scheme.primaryContainer, scheme.onPrimaryContainer,
            "Check your router", "See how quickly it answers.")
        FeatureRow(com.bernaferrari.wifiweakspot.core.designsystem.component.NetworksIcons.Filled, MaterialShapes.Cookie7Sided, scheme.secondaryContainer, scheme.onSecondaryContainer,
            "See nearby networks", "Pick a clearer channel.")
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    shape: RoundedPolygon,
    container: androidx.compose.ui.graphics.Color,
    content: androidx.compose.ui.graphics.Color,
    title: String,
    body: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        ShapeIcon(icon, shape, container, content, size = 48.dp)
        Spacer(Modifier.width(16.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
