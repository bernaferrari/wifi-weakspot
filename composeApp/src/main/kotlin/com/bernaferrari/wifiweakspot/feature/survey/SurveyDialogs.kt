package com.bernaferrari.wifiweakspot.feature.survey

import com.bernaferrari.wifiweakspot.core.i18n.strings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyProjectEntity

@Composable
internal fun SurveyNameDialog(current: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf(current) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(strings.common.rename) },
        text = { OutlinedTextField(value = name, onValueChange = { name = it.take(60) }, label = { Text(strings.common.nameLabel) }, placeholder = { Text(strings.survey.namePlaceholder) }, singleLine = true) },
        confirmButton = { TextButton(onClick = { onSave(name.trim()) }, enabled = name.isNotBlank()) { Text(strings.common.save) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.common.cancel) } })
}

