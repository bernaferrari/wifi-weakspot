@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.analyze

import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.snap
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
    palette: NetworkPalette,
    showRecommendation: Boolean,
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
    val reduceMotion = com.bernaferrari.wifiweakspot.core.designsystem.LocalReduceMotion.current
    val common = strings.common
    val s = strings.analyze
    // Match the band buttons, while allowing larger accessibility text to grow.
    // Keep the recommendation and selection in the same stable-height slot.
    val channelBadgeSize = with(LocalDensity.current) {
        maxOf(32.dp, typography.labelLarge.lineHeight.toDp() + 8.dp)
    }
    val rowHeight = channelBadgeSize + 16.dp
    Box(
        Modifier.fillMaxWidth().padding(top = 6.dp).heightIn(min = rowHeight),
        contentAlignment = Alignment.CenterStart
    ) {
        AnimatedContent(
            targetState = selectedNetwork,
            contentKey = { it?.bssid },
            contentAlignment = Alignment.CenterStart,
            label = "chartSelection",
            transitionSpec = {
                val enter = if (reduceMotion) androidx.compose.animation.EnterTransition.None else fadeIn(tween(150))
                val exit = if (reduceMotion) androidx.compose.animation.ExitTransition.None else fadeOut(tween(100))
                (enter togetherWith exit).using(SizeTransform(clip = false) { _, _ -> if (reduceMotion) snap() else tween(180) })
            }
        ) { selection ->
            if (selection != null) {
                InputChip(
                    selected = true,
                    onClick = onClearSelection,
                    modifier = Modifier.heightIn(min = rowHeight).semantics {
                        contentDescription = "${s.clearSelectedNetwork}: ${selection.ssid.ifBlank { common.hiddenNetwork }}"
                    },
                    label = { Text(selection.ssid.ifBlank { common.hiddenNetwork }, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = {
                        Box(Modifier.size(10.dp).clip(androidx.compose.foundation.shape.CircleShape).background(palette.color(selection.bssid)))
                    },
                    trailingIcon = { Icon(Icons.Rounded.Close, null, Modifier.size(18.dp)) },
                    colors = InputChipDefaults.inputChipColors(
                        selectedContainerColor = scheme.surfaceContainerHigh,
                        selectedLabelColor = scheme.onSurface,
                        selectedTrailingIconColor = scheme.onSurfaceVariant
                    )
                )
            } else if (hint != null && showRecommendation) {
                Surface(
                    onClick = { explaining = ChannelAdviceSnapshot(hint, band, scores) },
                    shape = WifiWeakspotShapes.full,
                    color = scheme.tertiaryContainer,
                    contentColor = scheme.onTertiaryContainer,
                    modifier = Modifier.semantics { contentDescription = s.channelAdviceDescription(hint.label, hint.channel) }
                ) {
                    Row(Modifier.clearAndSetSemantics { }.heightIn(min = rowHeight).padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(channelBadgeSize).clip(MaterialShapes.Cookie9Sided.toShape()).background(scheme.onTertiaryContainer), contentAlignment = Alignment.Center) {
                            Text("${hint.channel}", style = typography.labelLarge, fontWeight = FontWeight.Bold, color = scheme.tertiaryContainer)
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(hint.label, style = typography.labelLarge, modifier = Modifier.weight(1f, fill = false))
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, Modifier.size(20.dp))
                    }
                }
            } else if (showRecommendation) {
                Text(s.tapWaveForDetails, modifier = Modifier.padding(start = 4.dp), style = typography.bodySmall, color = scheme.onSurfaceVariant)
            }
        }
    }

    explaining?.let { advice ->
        ChannelAdviceSheet(advice, onDismiss = { explaining = null })
    }

}
