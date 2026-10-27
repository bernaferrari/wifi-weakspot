package com.bernaferrari.wifiweakspot.core.backup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

class BackupDocumentPicker(val export: () -> Unit, val restore: () -> Unit)

/** Registered at the settings screen, so activity recreation reconnects results to its view model. */
val LocalBackupDocumentPicker = staticCompositionLocalOf<@Composable (
    onExport: (String?) -> Unit, onRestore: (String?) -> Unit
) -> BackupDocumentPicker> { { _, _ -> BackupDocumentPicker({}, {}) } }
