package com.bernaferrari.wifiweakspot.core.router

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

}
