@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class, androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.bernaferrari.wifiweakspot.feature.walk

import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bernaferrari.wifiweakspot.core.designsystem.component.WifiBadge
import com.bernaferrari.wifiweakspot.core.designsystem.signalColors
import com.bernaferrari.wifiweakspot.core.rooms.RoomReading
import com.bernaferrari.wifiweakspot.core.util.relativeTime
import com.bernaferrari.wifiweakspot.core.wifi.SignalQuality
import com.bernaferrari.wifiweakspot.core.wifi.bandLabel

/** One saved room: how it is now, how it changed, and the checks before. Newest reading first. */
@Composable
fun SpotHistorySheet(
    readings: List<RoomReading>,
    saving: Boolean,
    canRecheck: Boolean,
    onRecheck: () -> Unit,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    val current = readings.first()
    val previous = readings.getOrNull(1)
    val scheme = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                WifiBadge(current.averageRssi, size = 72.dp)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(current.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
                    Text(
                        strings.survey.savedAt(relativeTime(current.timestamp)),
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            // What the signal there means in practice, plus how it moved since the last check.
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("${current.quality.headline} · ${current.averageRssi} dBm", style = MaterialTheme.typography.titleMedium, color = signalColors(current.quality).accent)
                Text(current.quality.explanation, style = MaterialTheme.typography.bodyLarge)
                // One practical idea when it's weak, nothing when it's fine.
                if (current.quality.isProblem) Text(
                    strings.survey.moveRouterTip,
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant
                )
                if (previous != null) {
                    val delta = current.averageRssi - previous.averageRssi
                    Text(
                        when {
                            delta >= 4 -> strings.survey.betterThanLastTime
                            delta <= -4 -> strings.survey.worseThanLastTime
                            else -> strings.survey.sameAsLastTime
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = scheme.onSurfaceVariant
                    )
                }
            }

            if (readings.size >= 2) {
                Column(verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap)) {
                    Text(strings.survey.earlierChecks, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
                    val earlier = readings.drop(1)
                    earlier.forEachIndexed { index, reading ->
                        SegmentedListItem(
                            onClick = {},
                            enabled = false,
                            shapes = ListItemDefaults.segmentedShapes(index = index, count = earlier.size),
                            colors = ListItemDefaults.colors(containerColor = scheme.surfaceContainerHigh, disabledContainerColor = scheme.surfaceContainerHigh, disabledContentColor = scheme.onSurface),
                            leadingContent = { WifiBadge(reading.averageRssi, size = 36.dp) },
                            supportingContent = { Text("${relativeTime(reading.timestamp)} · ${bandLabel(reading.frequencyMhz)}") }
                        ) { Text(reading.quality.headline) }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onRemove,
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.weight(1f).heightIn(min = ButtonDefaults.MediumContainerHeight)
                    ) {
                        Icon(Icons.Rounded.DeleteOutline, null)
                        Spacer(Modifier.width(8.dp))
                        Text(strings.survey.remove, style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight), maxLines = 1,
                            autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight).fontSize))
                    }
                    Button(
                        onClick = onRecheck,
                        enabled = !saving && canRecheck,
                        shapes = ButtonDefaults.shapes(),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                        modifier = Modifier.weight(1f).heightIn(min = ButtonDefaults.MediumContainerHeight)
                    ) {
                        Icon(Icons.Rounded.Refresh, null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (saving) strings.common.checking else strings.common.checkAgain, style = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight), maxLines = 1,
                            autoSize = androidx.compose.foundation.text.TextAutoSize.StepBased(minFontSize = 12.sp, maxFontSize = ButtonDefaults.textStyleFor(ButtonDefaults.MediumContainerHeight).fontSize))
                    }
                }
                if (!canRecheck) Text(
                    strings.survey.connectToRecheck(current.ssid),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

