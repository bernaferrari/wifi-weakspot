@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.pulsewave.app.feature.walk

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.rooms.RoomReading
import com.pulsewave.app.core.rooms.spotComparison
import com.pulsewave.app.core.util.relativeTime
import com.pulsewave.app.core.wifi.bandLabel

/** Persistent before/after readings for one named spot on one network. */
@Composable
fun SpotHistorySheet(readings: List<RoomReading>, saving: Boolean, canRecheck: Boolean, onRecheck: () -> Unit, onDismiss: () -> Unit) {
    val current = readings.first()
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden, enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded))) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(current.name, style = MaterialTheme.typography.headlineSmall)
                Text(current.ssid.ifBlank { current.bssid }, style = MaterialTheme.typography.bodyMedium)
            }
            item {
                Text(spotComparison(current, readings.getOrNull(1)), style = MaterialTheme.typography.titleMedium)
                Text("Stand in the same place, hold the phone the same way, and recheck after trying one change. Different bands, access points, and traffic can affect the comparison.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item {
                Button(onClick = onRecheck, enabled = !saving && canRecheck, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(if (saving) "Hold still · taking fresh readings…" else "Recheck this spot")
                }
            }
            if (!canRecheck) item {
                Text("Connect to this network and allow signal readings before rechecking.",
                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            item { Text("Reading history", style = MaterialTheme.typography.titleSmall) }
            items(readings, key = { it.id }) { reading ->
                ListItem(
                    supportingContent = {
                        Text("${relativeTime(reading.timestamp)} · ${bandLabel(reading.frequencyMhz)} · ${reading.samples} samples\nAccess point ${reading.bssid}")
                    }
                ) { Text("${reading.averageRssi} dBm · ${reading.quality.label}") }
            }
        }
    }
}
