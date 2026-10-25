@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.analyze

import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ShowChart
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.designsystem.component.SignalMorphBlob
import com.bernaferrari.wifiweakspot.core.designsystem.signalColors
import com.bernaferrari.wifiweakspot.core.util.relativeTime
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import com.bernaferrari.wifiweakspot.core.wifi.bandLabel
import com.bernaferrari.wifiweakspot.core.wifi.estimateDistanceMeters
import com.bernaferrari.wifiweakspot.core.wifi.formatDistance
import com.bernaferrari.wifiweakspot.core.wifi.frequencyToChannel
import com.bernaferrari.wifiweakspot.core.wifi.getVendorName
import com.bernaferrari.wifiweakspot.core.wifi.securityLabel

/** Bottom-sheet content for one access point: the verdict first, then plain facts. */
@Composable
fun WifiDetailScreen(
    network: WifiNetwork,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    isFavorite: Boolean = false,
    onToggleFavorite: () -> Unit = {},
    /** Null hides "Watch", for places that can't show the live graph. */
    onTrack: (() -> Unit)? = null,
    onShare: () -> Unit = {}
) {
    val quality = SignalQuality.of(network.rssi)
    val colors = signalColors(quality)
    val security = securityLabel(network.capabilities.ifBlank { network.security })
    val common = strings.common
    val sharedCatalog = strings
    val s = strings.analyze
    Column(
        modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // The same Wi-Fi symbol and quality shape as the network list.
            com.bernaferrari.wifiweakspot.core.designsystem.component.WifiBadge(network.rssi, size = 88.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    network.ssid.ifEmpty { common.hiddenNetwork },
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(quality.headline, style = MaterialTheme.typography.titleMedium, color = colors.accent)
                Text(
                    s.seenReading(network.rssi, relativeTime(network.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Card(shape = WifiWeakspotShapes.large, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)) {
            Column(Modifier.padding(vertical = 8.dp)) {
                Fact(s.factChannel, "${frequencyToChannel(network.frequency)} · ${bandLabel(network.frequency)}")
                Fact(s.factWidth, when {
                    network.channelWidth <= 0 -> s.notAvailable
                    network.is80Plus80 -> "80 + 80 MHz"
                    else -> "${network.channelWidth} MHz"
                })
                Fact(s.factSecurity, when (security) { "" -> sharedCatalog.common.unknown; "Open" -> s.noPassword; else -> security })
                getVendorName(network.bssid)?.let { Fact(sharedCatalog.common.maker, it) }
                Fact(s.factDistance, formatDistance(estimateDistanceMeters(network.rssi, network.frequency)))
                Fact(sharedCatalog.common.address, network.bssid.uppercase())
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (onTrack != null) Button(
                onClick = onTrack,
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier.weight(1f).heightIn(min = ButtonDefaults.MediumContainerHeight)
            ) {
                Icon(Icons.AutoMirrored.Rounded.ShowChart, null)
                Spacer(Modifier.width(8.dp))
                Text(s.watch, style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight))
            } else Spacer(Modifier.weight(1f))
            FilledTonalIconButton(
                onClick = onToggleFavorite,
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.size(ButtonDefaults.MediumContainerHeight)
            ) { Icon(if (isFavorite) Icons.Rounded.Star else Icons.Rounded.StarBorder, if (isFavorite) s.removeFromFavorites else s.addToFavorites) }
            FilledTonalIconButton(
                onClick = onShare,
                shapes = IconButtonDefaults.shapes(),
                modifier = Modifier.size(ButtonDefaults.MediumContainerHeight)
            ) { Icon(Icons.Rounded.Share, common.share) }
        }
    }
}

@Composable
private fun Fact(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}
