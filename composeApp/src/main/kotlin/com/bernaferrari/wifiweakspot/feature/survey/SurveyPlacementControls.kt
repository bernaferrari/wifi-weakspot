package com.bernaferrari.wifiweakspot.feature.survey

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.OpenWith
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyPointEntity
import com.bernaferrari.wifiweakspot.core.i18n.strings
import kotlin.math.roundToInt

/**
 * The action row under the map: one clear primary action. A selected reading shows its name and
 * value; otherwise the measure button fills the row. Exact coordinates stay out of sight, behind a
 * small button that opens keyboard- and screen-reader-operable position sliders.
 */
@Composable
internal fun SurveyPlacementControls(
    cursor: SurveyPosition,
    selectedPoint: SurveyPointEntity?,
    enabled: Boolean,
    canRename: Boolean,
    onPositionChanged: (SurveyPosition) -> Unit,
    onMeasure: () -> Unit,
    onRename: () -> Unit
) {
    val s = strings.survey
    val display = com.bernaferrari.wifiweakspot.core.designsystem.LocalSignalDisplay.current
    var choosePosition by remember { mutableStateOf(false) }
    val position = s.mapPosition((cursor.x * 100).roundToInt(), (cursor.y * 100).roundToInt())
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        FilledTonalIconButton(onClick = { choosePosition = true }, modifier = Modifier.size(56.dp)) {
            Icon(Icons.Rounded.OpenWith, contentDescription = position)
        }
        if (selectedPoint != null) {
            Column(Modifier.weight(1f)) {
                Text(selectedPoint.label ?: s.pointName(selectedPoint.id), style = MaterialTheme.typography.titleMedium,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(display.reading(selectedPoint.rssi), style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onRename, enabled = canRename) { Icon(Icons.Rounded.Edit, strings.common.rename) }
            FilledTonalButton(onClick = onMeasure, enabled = enabled, modifier = Modifier.height(56.dp)) {
                Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.recheckReading)
            }
        } else {
            Button(onClick = onMeasure, enabled = enabled, modifier = Modifier.weight(1f).height(56.dp)) {
                Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(s.measureHere, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
    if (choosePosition) AlertDialog(
        onDismissRequest = { choosePosition = false },
        title = { Text(position) },
        text = {
            Column {
                Text(s.horizontalPosition)
                Slider(cursor.x, onValueChange = { onPositionChanged(cursor.copy(x = it)) },
                    modifier = Modifier.semantics { contentDescription = s.horizontalPosition })
                Text(s.verticalPosition)
                Slider(cursor.y, onValueChange = { onPositionChanged(cursor.copy(y = it)) },
                    modifier = Modifier.semantics { contentDescription = s.verticalPosition })
            }
        },
        confirmButton = { TextButton(onClick = { choosePosition = false }) { Text(strings.common.close) } }
    )
}
