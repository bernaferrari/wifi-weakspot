@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.pulsewave.app.feature.overview

import com.pulsewave.app.core.wifi.competitorsOf

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import com.pulsewave.app.core.designsystem.PulseWaveShapes
import com.pulsewave.app.core.designsystem.component.ShapeIcon
import com.pulsewave.app.core.wifi.WifiNetwork
import com.pulsewave.app.core.wifi.frequencyToChannel
import com.pulsewave.app.feature.analyze.CongestionAnalyzer
import com.pulsewave.app.feature.analyze.FrequencyBand

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
        shape = PulseWaveShapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ShapeIcon(tile.icon, tile.shape, tile.container, tile.content, size = 48.dp, iconSize = 24.dp)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(tile.title, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
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
 * What's around you, in one sentence: how many networks, how crowded your channel is,
 * and whether a clearer one exists. Hidden until there is something to say.
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
    val band = FrequencyBand.entries.firstOrNull { connectedFrequency > 0 && it.contains(connectedFrequency) }
    val channel = frequencyToChannel(connectedFrequency)
    // Your router's guest or hidden names on the same radio aren't neighbors.
    val competitors = remember(networks, connectedBssid, connectedFrequency) {
        networks.competitorsOf(connectedBssid, connectedFrequency)
    }
    val sharing = if (band == null) 0 else competitors.count {
        frequencyToChannel(it.frequency) == channel && band.contains(it.frequency)
    }
    val best = remember(competitors, band) {
        band?.let { b ->
            CongestionAnalyzer.analyze(competitors.filter { b.contains(it.frequency) }, b)
                .firstOrNull { it.recommendation }
        }
    }
    val detail = when {
        band == null -> "See channels and signal for each one."
        best != null && best.channelNumber != channel -> "Channel ${best.channelNumber} looks clearer than yours."
        sharing == 0 -> "Your channel $channel has it to itself."
        else -> "Your channel $channel is still the clearest."
    }
    Card(
        onClick = onClick,
        shape = PulseWaveShapes.large,
        colors = CardDefaults.cardColors(containerColor = scheme.secondaryContainer, contentColor = scheme.onSecondaryContainer),
        modifier = modifier
    ) {
        Row(Modifier.fillMaxWidth().padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            ShapeIcon(com.pulsewave.app.core.designsystem.component.NetworksIcons.Filled, MaterialShapes.Cookie7Sided, scheme.secondary, scheme.onSecondary, size = 52.dp, iconSize = 26.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    if (networks.size == 1) "1 network nearby" else "${networks.size} networks nearby",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(detail, style = MaterialTheme.typography.bodyMedium)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, Modifier.size(24.dp))
        }
    }
}
