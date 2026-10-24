@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.designsystem.component.ShapeIcon
import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import com.bernaferrari.wifiweakspot.core.util.nowMillis
import kotlinx.coroutines.delay

data class QuickTileSpec(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val shape: RoundedPolygon,
    val container: Color,
    val content: Color,
    val onClick: () -> Unit
)

/** Two-column grid where tiles in a row always share one height, whatever their text. */
@Composable
fun QuickTileGrid(tiles: List<QuickTileSpec>, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        tiles.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                row.forEach { tile -> QuickTile(tile, Modifier.weight(1f).fillMaxHeight()) }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickTile(tile: QuickTileSpec, modifier: Modifier) {
    Card(
        onClick = tile.onClick,
        shape = WifiWeakspotShapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ShapeIcon(tile.icon, tile.shape, tile.container, tile.content, size = 48.dp, iconSize = 24.dp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(tile.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(
                    tile.subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * What's around you, in one sentence. Age the advice even when no new scan arrives.
 */
@Composable
fun NearbySummaryCard(
    networks: List<WifiNetwork>,
    connectedBssid: String?,
    connectedFrequency: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (networks.isEmpty()) return
    val scheme = MaterialTheme.colorScheme
    val now by produceState(nowMillis()) {
        while (true) {
            value = nowMillis()
            delay(5_000)
        }
    }
    val advice = nearbyAdvice(networks, connectedBssid, connectedFrequency, now)
    Card(
        onClick = onClick,
        shape = WifiWeakspotShapes.large,
        colors = CardDefaults.cardColors(containerColor = scheme.secondaryContainer, contentColor = scheme.onSecondaryContainer),
        modifier = modifier
    ) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            ShapeIcon(com.bernaferrari.wifiweakspot.core.designsystem.component.NetworksIcons.Filled, MaterialShapes.Cookie7Sided, scheme.secondary, scheme.onSecondary, size = 52.dp, iconSize = 26.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    // Stale scans say so in the line below, so the title stays short either way.
                    if (advice.networkCount == 1) "1 network nearby" else "${advice.networkCount} networks nearby",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(advice.detail, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, Modifier.size(24.dp))
        }
    }
}
