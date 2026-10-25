@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.analyze

import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.LocalSignalDisplay
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import com.bernaferrari.wifiweakspot.core.wifi.competitorsOf
import com.bernaferrari.wifiweakspot.core.wifi.frequencyToChannel

/** One stable place for the scan's recommendation or the selected radio's current reading. */
@Composable
internal fun ChannelContext(
    networks: List<WifiNetwork>,
    band: FrequencyBand,
    connected: NetworkStatus,
    selectedNetwork: WifiNetwork?,
    onClearSelection: () -> Unit
) {
    val competitors = remember(networks, connected.bssid, connected.frequency) {
        networks.competitorsOf(connected.bssid.takeIf { connected.isConnected }, connected.frequency)
    }
    val scores = remember(competitors, band) {
        CongestionAnalyzer.analyze(competitors.filter { band.contains(it.frequency) }, band)
    }
    val connectedHere = connected.isConnected && band.contains(connected.frequency)
    var explaining by remember(band) { mutableStateOf<ChannelAdviceSnapshot?>(null) }
    val hint = channelHint(scores, connected.channel.takeIf { connectedHere })
    val scheme = MaterialTheme.colorScheme
    val typography = MaterialTheme.typography
    val display = LocalSignalDisplay.current
    val common = strings.common
    val s = strings.analyze
    // Reserve the two-line selection's height even at large font sizes. The recommendation
    // can swap with it without displacing the list or the finger's next target.
    val rowHeight = with(LocalDensity.current) {
        maxOf(56.dp, typography.labelLarge.lineHeight.toDp() + typography.bodySmall.lineHeight.toDp() + 10.dp)
    }
    Box(
        Modifier.fillMaxWidth().padding(top = 6.dp).heightIn(min = rowHeight),
        contentAlignment = Alignment.CenterStart
    ) {
        if (selectedNetwork != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = WifiWeakspotShapes.full,
                color = scheme.secondaryContainer,
                contentColor = scheme.onSecondaryContainer
            ) {
                Row(Modifier.heightIn(min = rowHeight).padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(selectedNetwork.ssid.ifBlank { common.hiddenNetwork }, style = typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            s.selectedNetworkSummary(display.reading(selectedNetwork.rssi), SignalQuality.of(selectedNetwork.rssi).label, frequencyToChannel(selectedNetwork.frequency)),
                            style = typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis
                        )
                    }
                    IconButton(onClick = onClearSelection, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Rounded.Close, s.clearSelectedNetwork)
                    }
                }
            }
        } else if (hint != null) {
            Surface(
                onClick = { explaining = ChannelAdviceSnapshot(hint, band, scores) },
                shape = WifiWeakspotShapes.full,
                color = scheme.tertiaryContainer,
                contentColor = scheme.onTertiaryContainer,
                modifier = Modifier.semantics { contentDescription = s.channelAdviceDescription(hint.label, hint.channel) }
            ) {
                Row(Modifier.clearAndSetSemantics { }.heightIn(min = rowHeight).padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(MaterialShapes.Cookie9Sided.toShape()).background(scheme.onTertiaryContainer), contentAlignment = Alignment.Center) {
                        Text("${hint.channel}", style = typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.tertiaryContainer)
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(hint.label, style = typography.labelLarge, modifier = Modifier.weight(1f, fill = false))
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, Modifier.size(20.dp))
                }
            }
        } else {
            Text(s.tapWaveForDetails, modifier = Modifier.padding(start = 4.dp), style = typography.bodySmall, color = scheme.onSurfaceVariant)
        }
    }
    explaining?.let { advice ->
        ChannelAdviceSheet(advice, onDismiss = { explaining = null })
    }

}
