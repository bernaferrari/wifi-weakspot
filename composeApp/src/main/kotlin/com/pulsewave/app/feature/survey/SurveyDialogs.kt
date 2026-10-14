package com.pulsewave.app.feature.survey

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pulsewave.app.core.database.entities.SurveyProjectEntity

@Composable
internal fun SurveyNameDialog(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New survey project") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Project name") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onCreate(name.trim()) }, enabled = name.isNotBlank()) { Text("Create") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
internal fun SurveyMapSettingsDialog(project: SurveyProjectEntity, onDismiss: () -> Unit, onSave: (Float, Float, Float, Float) -> Unit) {
    var width by remember { mutableStateOf(project.widthMeters.toString()) }
    var height by remember { mutableStateOf(project.heightMeters.toString()) }
    var step by remember { mutableStateOf(project.stepLengthMeters.toString()) }
    var bearing by remember { mutableStateOf(project.mapTopBearingDegrees.toString()) }
    val b = bearing.replace(',', '.').toFloatOrNull()
    val w = width.replace(',', '.').toFloatOrNull()
    val h = height.replace(',', '.').toFloatOrNull()
    val s = step.replace(',', '.').toFloatOrNull()
    val valid = w != null && w in 1f..500f && h != null && h in 1f..500f && s != null && s in 0.2f..1.5f && b != null && b in 0f..359.99f
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Calibrate map") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Width runs left to right; height runs top to bottom. Set the compass direction toward the map's top edge. Walking is an estimate; correct your position on the map.", style = MaterialTheme.typography.bodySmall)
            SurveyNumberField(width, { width = it }, "Map width (m)", "1–500 m")
            SurveyNumberField(height, { height = it }, "Map height (m)", "1–500 m")
            SurveyNumberField(step, { step = it }, "Step length (m)", "0.2–1.5 m")
            SurveyNumberField(bearing, { bearing = it }, "Map top bearing (degrees)", "North 0 · East 90 · South 180 · West 270")
            Text("Dimensions and floorplan apply to all floors. Measure your average step length for better walking estimates.", style = MaterialTheme.typography.bodySmall)
        }
    }, confirmButton = { TextButton(onClick = { onSave(w!!, h!!, s!!, b!!) }, enabled = valid) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
internal fun SurveyCoordinateDialog(project: SurveyProjectEntity, position: SurveyPosition?, onDismiss: () -> Unit, onSample: (Float, Float) -> Unit) {
    var x by remember { mutableStateOf(((position?.x ?: 0.5f) * project.widthMeters).toString()) }
    var y by remember { mutableStateOf(((position?.y ?: 0.5f) * project.heightMeters).toString()) }
    val xMeters = x.replace(',', '.').toFloatOrNull()
    val yMeters = y.replace(',', '.').toFloatOrNull()
    val valid = xMeters != null && xMeters in 0f..project.widthMeters && yMeters != null && yMeters in 0f..project.heightMeters && project.targetBssid != null
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Measure at coordinates") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Enter your position in meters from the map's top-left corner. Stay there while the fresh scan runs.", style = MaterialTheme.typography.bodySmall)
            SurveyNumberField(x, { x = it }, "X from left (m)", "0–${project.widthMeters} m")
            SurveyNumberField(y, { y = it }, "Y from top (m)", "0–${project.heightMeters} m")
            if (project.targetBssid == null) Text("Choose an access point first.", color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(onClick = { onSample(xMeters!! / project.widthMeters, yMeters!! / project.heightMeters) }, enabled = valid) { Text("Measure") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun SurveyNumberField(value: String, onChange: (String) -> Unit, label: String, range: String) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, supportingText = { Text(range) },
        modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}
