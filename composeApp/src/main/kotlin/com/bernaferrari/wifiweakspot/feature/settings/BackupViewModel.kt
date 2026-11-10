package com.bernaferrari.wifiweakspot.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bernaferrari.wifiweakspot.core.backup.BackupService
import com.bernaferrari.wifiweakspot.core.backup.PreparedBackup
import com.bernaferrari.wifiweakspot.core.backup.RestoreResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
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

    private val _chooseDestination = MutableStateFlow(false)
    val chooseDestination = _chooseDestination.asStateFlow()
    private var prepared: PreparedBackup? = null

    fun prepareExport() {
        if (_busy.value) return
        _busy.value = true
        _message.value = null
        viewModelScope.launch {
            try {
                prepared = service.prepareExport()
                _chooseDestination.value = true
            } catch (cancelled: CancellationException) {
                _busy.value = false
                throw cancelled
            } catch (_: Exception) {
                _message.value = BackupMessage.ExportFailed
                _busy.value = false
            }
        }
    }

    fun launchExportPicker(launch: () -> Unit) {
        if (!_chooseDestination.compareAndSet(expect = true, update = false)) return
        try {
            launch()
        } catch (_: Exception) {
            prepared?.discard()
            prepared = null
            _message.value = BackupMessage.ExportFailed
            _busy.value = false
        }
    }

    fun exportTo(uri: String?) {
        val backup = prepared
        _chooseDestination.value = false
        if (backup == null) {
            if (uri != null && !_busy.value) viewModelScope.launch {
                service.discardExportDestination(uri)
                _message.value = BackupMessage.ExportFailed
            }
            return
        }
        if (uri == null) {
            prepared = null
            backup.discard()
            _busy.value = false
            return
        }
        prepared = null
        viewModelScope.launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                backup.writeTo(uri)
                _message.value = BackupMessage.Exported
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _message.value = BackupMessage.ExportFailed
            } finally {
                backup.discard()
                _busy.value = false
            }
        }
    }

    override fun onCleared() {
        prepared?.discard()
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
