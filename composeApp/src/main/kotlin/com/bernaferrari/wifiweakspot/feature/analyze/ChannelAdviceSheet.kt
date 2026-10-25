@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.analyze

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.designsystem.WifiWeakspotShapes
import com.bernaferrari.wifiweakspot.core.i18n.strings

/** A visual comparison of the opened scan, followed by the router steps. */
@Composable
internal fun ChannelAdviceSheet(snapshot: ChannelAdviceSnapshot, onDismiss: () -> Unit) {
    val (hint, band, scores) = snapshot
    val s = strings.analyze
    val scheme = MaterialTheme.colorScheme
    val comparisons = scores.filter { it.channelNumber in setOf(1, 6, 11, hint.channel) }
    val largest = comparisons.maxOfOrNull { it.score }?.coerceAtLeast(1.0) ?: 1.0
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    Modifier.size(80.dp).clip(MaterialShapes.Cookie9Sided.toShape()).background(scheme.tertiaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(hint.channel.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.SemiBold, color = scheme.onTertiaryContainer)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(hint.label, style = MaterialTheme.typography.titleLarge, modifier = Modifier.semantics { heading() })
                    Text(band.label, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                }
            }
            Surface(shape = WifiWeakspotShapes.large, color = scheme.surfaceContainerHigh) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(s.channelOverlapTitle, style = MaterialTheme.typography.titleSmall)
                    comparisons.forEach { score ->
                        val suggested = score.channelNumber == hint.channel
                        val description = s.channelAdviceDescription(score.rating.label, score.channelNumber)
                        Row(
                            Modifier.fillMaxWidth().clearAndSetSemantics {
                                contentDescription = description
                                if (suggested) stateDescription = hint.label
                            },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(s.channelShort(score.channelNumber), Modifier.widthIn(min = 56.dp), style = MaterialTheme.typography.labelLarge, fontWeight = if (suggested) FontWeight.Bold else FontWeight.Normal)
                            LinearProgressIndicator(
                                progress = { (score.score / largest).toFloat() },
                                modifier = Modifier.weight(1f).height(8.dp),
                                color = if (suggested) scheme.tertiary else scheme.outline,
                                trackColor = scheme.surfaceContainerHighest,
                                gapSize = 0.dp,
                                drawStopIndicator = {}
                            )
                            Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                if (suggested) Icon(Icons.Rounded.Check, null, tint = scheme.tertiary)
                            }
                        }
                    }
                    Text(s.channelAdviceBasis, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                ChannelStep(1, Icons.Rounded.Router, strings.tools.openRouterSettings)
                ChannelStep(2, Icons.Rounded.Wifi, s.channelChangeInstructions(hint.channel, band.label))
                ChannelStep(3, Icons.Rounded.Save, s.channelApplyChange)
            }
            Text(s.channelChangeNote, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            FilledTonalButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                Text(strings.common.done)
            }
        }
    }
}

@Composable
private fun ChannelStep(number: Int, icon: ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.secondaryContainer), contentAlignment = Alignment.Center) {
            Text(number.toString(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        }
        Icon(icon, null, Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
    }
}
