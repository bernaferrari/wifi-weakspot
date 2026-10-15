package com.pulsewave.app.core.router

import kotlinx.coroutines.flow.Flow



sealed interface PingEvent {
    data class Reply(val sequence: Int, val rttMs: Float) : PingEvent
    data class Finished(val sent: Int, val received: Int) : PingEvent
}

/**
 * Measures the hop between this phone and its router only. Nothing leaves the home network.
 */
interface RouterProbe {
    /** Streams ICMP replies from one `ping` run, then a [PingEvent.Finished] summary. */
    fun ping(host: String, count: Int, intervalSeconds: Double = 0.2): Flow<PingEvent>

    /**
     * Sends UDP to the router's discard port as fast as the Wi-Fi link drains it and
     * reports the achieved rate. Android blocks the sender while the radio queue is full,
     * so this follows the real uplink airtime. [onProgress] receives the running Mbps.
     */
    suspend fun uploadMbps(
        host: String,
        durationMs: Long,
        capMbps: Double,
        onProgress: (elapsedFraction: Float, mbps: Double) -> Unit
    ): Double
}
