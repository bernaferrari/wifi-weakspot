package com.bernaferrari.wifiweakspot.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.wifiweakspot.core.backup.BackupService
import com.bernaferrari.wifiweakspot.core.backup.RestoreResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class BackupMessage { Exported, Restored, AlreadyRestored, ExportFailed, RestoreFailed }

class BackupViewModel(private val service: BackupService) : ViewModel() {
    val supported = service.supported
    private val _busy = MutableStateFlow(false)
    val busy = _busy.asStateFlow()
    private val _message = MutableStateFlow<BackupMessage?>(null)
    val message = _message.asStateFlow()

    fun exportTo(uri: String?) = perform(uri, BackupMessage.ExportFailed) {
        service.exportTo(it)
        BackupMessage.Exported
    }

    fun restoreFrom(uri: String?) = perform(uri, BackupMessage.RestoreFailed) {
        when (service.restoreFrom(it)) {
            RestoreResult.Restored -> BackupMessage.Restored
            RestoreResult.AlreadyRestored -> BackupMessage.AlreadyRestored
        }
    }

    private fun perform(uri: String?, failure: BackupMessage, action: suspend (String) -> BackupMessage) {
        if (uri == null || _busy.value) return
        _busy.value = true
        _message.value = null
        viewModelScope.launch {
            try { _message.value = action(uri) }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { _message.value = failure }
            finally { _busy.value = false }
        }
    }
}
