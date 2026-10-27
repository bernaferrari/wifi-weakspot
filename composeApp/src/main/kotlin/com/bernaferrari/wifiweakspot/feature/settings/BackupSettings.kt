package com.bernaferrari.wifiweakspot.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.bernaferrari.wifiweakspot.core.backup.LocalBackupDocumentPicker
import com.bernaferrari.wifiweakspot.core.i18n.strings
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BackupSettings(viewModel: BackupViewModel = koinViewModel()) {
    if (!viewModel.supported) return
    val busy by viewModel.busy.collectAsState()
    val message by viewModel.message.collectAsState()
    val picker = LocalBackupDocumentPicker.current(viewModel::exportTo, viewModel::restoreFrom)
    var confirmRestore by rememberSaveable { mutableStateOf(false) }
    val s = strings.settings
    SettingsSection(s.sectionBackup) {
        Text(s.backupScope, Modifier.padding(16.dp), style = MaterialTheme.typography.bodyMedium)
        SettingsRow(Icons.Default.SaveAlt, s.exportBackup, enabled = !busy, onClick = picker.export)
        SettingsRow(Icons.Default.Restore, s.restoreBackup, enabled = !busy, onClick = { confirmRestore = true })
        if (busy) LinearProgressIndicator(Modifier.fillMaxWidth().padding(16.dp))
        message?.let { result ->
            Text(when (result) {
                BackupMessage.Exported -> s.backupSaved
                BackupMessage.Restored -> s.backupRestored
                BackupMessage.AlreadyRestored -> s.backupAlreadyRestored
                BackupMessage.ExportFailed -> s.backupExportFailed
                BackupMessage.RestoreFailed -> s.backupRestoreFailed
            }, Modifier.padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite },
                style = MaterialTheme.typography.bodyMedium,
                color = if (result == BackupMessage.ExportFailed || result == BackupMessage.RestoreFailed)
                    MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
        }
    }
    if (confirmRestore) AlertDialog(
        onDismissRequest = { confirmRestore = false },
        title = { Text(s.restoreBackup) },
        text = { Text(s.restoreBackupExplanation) },
        confirmButton = { TextButton(onClick = { confirmRestore = false; picker.restore() }) { Text(s.restoreBackup) } },
        dismissButton = { TextButton(onClick = { confirmRestore = false }) { Text(strings.common.cancel) } }
    )
}
