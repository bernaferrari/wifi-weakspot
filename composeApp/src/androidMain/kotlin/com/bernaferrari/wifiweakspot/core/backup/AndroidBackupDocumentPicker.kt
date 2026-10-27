package com.bernaferrari.wifiweakspot.core.backup

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
fun rememberAndroidBackupDocumentPicker(onExport: (String?) -> Unit, onRestore: (String?) -> Unit): BackupDocumentPicker {
    val create = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { onExport(it?.toString()) }
    val open = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { onRestore(it?.toString()) }
    return remember(create, open) {
        BackupDocumentPicker(
            export = { create.launch("WiFi-Weakspot-${System.currentTimeMillis()}.zip") },
            restore = { open.launch(arrayOf("application/zip", "application/x-zip-compressed", "application/octet-stream")) }
        )
    }
}
