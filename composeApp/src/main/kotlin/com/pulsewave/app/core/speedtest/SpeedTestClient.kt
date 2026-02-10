package com.pulsewave.app.core.speedtest

import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class SpeedTestServer(
    val id: String,
    val name: String,
    val host: String,
    val location: String,
    val distance: Float?
)

@Serializable
data class SpeedTestResult(
    val id: Long,
    val timestamp: Long,
    val server: SpeedTestServer,
    val downloadMbps: Float,
    val uploadMbps: Float,
    val pingMs: Float,
    val jitterMs: Float,
    val packetLoss: Float
)

sealed interface SpeedTestProgress {
    data object Idle : SpeedTestProgress
    data class Ping(val latencyMs: Float) : SpeedTestProgress
    data class Download(val mbps: Float, val progress: Float) : SpeedTestProgress
    data class Upload(val mbps: Float, val progress: Float) : SpeedTestProgress
    data class Complete(val result: SpeedTestResult) : SpeedTestProgress
    data class Error(val message: String) : SpeedTestProgress
}

interface SpeedTestClient {
    val progress: Flow<SpeedTestProgress>
    
    suspend fun getServers(): List<SpeedTestServer>
    suspend fun runTest(server: SpeedTestServer? = null): SpeedTestResult
    fun cancel()
}
