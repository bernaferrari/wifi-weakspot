package com.pulsewave.app.feature.speedtest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pulsewave.app.core.speedtest.SpeedTestClient
import com.pulsewave.app.core.speedtest.SpeedTestProgress
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SpeedTestViewModel(
    private val client: SpeedTestClient
) : ViewModel() {

    val uiState: StateFlow<SpeedTestState> = client.progress.map { progress ->
        when (progress) {
            is SpeedTestProgress.Idle -> SpeedTestState.Idle
            is SpeedTestProgress.Ping -> SpeedTestState.Running("Ping", 0f, 0f)
            is SpeedTestProgress.Download -> SpeedTestState.Running("Download", progress.progress, progress.mbps)
            is SpeedTestProgress.Upload -> SpeedTestState.Running("Upload", progress.progress, progress.mbps)
            is SpeedTestProgress.Complete -> SpeedTestState.Complete(
                download = progress.result.downloadMbps,
                upload = progress.result.uploadMbps,
                ping = progress.result.pingMs
            )
            is SpeedTestProgress.Error -> SpeedTestState.Idle // Reset on error for now
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SpeedTestState.Idle)

    fun startTest() {
        viewModelScope.launch {
            client.runTest()
        }
    }

    fun cancelTest() {
        client.cancel()
    }
}
