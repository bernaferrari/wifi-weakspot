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
internal fun SurveyNameDialog(current: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(current) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Rename map") },
        text = { OutlinedTextField(value = name, onValueChange = { name = it.take(60) }, label = { Text("Name") }, placeholder = { Text("Upstairs, Office…") }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onSave(name.trim()) }, enabled = name.isNotBlank()) { Text("Save") } },
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
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Size and direction") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Rough numbers are fine. They help the walking dot move the right distance; you can always drag it.", style = MaterialTheme.typography.bodySmall)
            SurveyNumberField(width, { width = it }, "Width, left to right (m)", "1–500 m")
            SurveyNumberField(height, { height = it }, "Depth, top to bottom (m)", "1–500 m")
            SurveyNumberField(step, { step = it }, "Your step length (m)", "Most people: 0.6–0.8 m")
            SurveyNumberField(bearing, { bearing = it }, "Compass direction of the map's top", "0 north · 90 east · 180 south · 270 west")
            Text("These apply to every floor of this map.", style = MaterialTheme.typography.bodySmall)
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
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add a reading by distance") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("How far you are from the map's top-left corner. Stay put while it measures.", style = MaterialTheme.typography.bodySmall)
            SurveyNumberField(x, { x = it }, "From the left edge (m)", "0–${project.widthMeters} m")
            SurveyNumberField(y, { y = it }, "From the top edge (m)", "0–${project.heightMeters} m")
            if (project.targetBssid == null) Text("Pick the Wi-Fi to measure first.", color = MaterialTheme.colorScheme.error)
        }
    }, confirmButton = { TextButton(onClick = { onSample(xMeters!! / project.widthMeters, yMeters!! / project.heightMeters) }, enabled = valid) { Text("Measure") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}

@Composable
private fun SurveyNumberField(value: String, onChange: (String) -> Unit, label: String, range: String) {
    OutlinedTextField(value = value, onValueChange = onChange, label = { Text(label) }, supportingText = { Text(range) },
        modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal))
}
