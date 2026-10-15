package com.pulsewave.app.core.router

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class AndroidRouterProbe(private val context: Context) : RouterProbe {

    override fun ping(host: String, count: Int, intervalSeconds: Double): Flow<PingEvent> = flow {
        require(host.isNotBlank() && !host.startsWith("-"))
        val process = ProcessBuilder(
            "ping", "-n", "-c", "$count", "-i", "%.1f".format(java.util.Locale.US, intervalSeconds.coerceAtLeast(0.2)), "-W", "1", host
        ).redirectErrorStream(true).start()
        var sent = count
        var received = 0
        try {
            process.inputStream.bufferedReader().useLines { lines ->
                for (line in lines) {
                    currentCoroutineContext().ensureActive()
                    val rtt = TimeRegex.find(line)?.groupValues?.get(1)?.toFloatOrNull()
                    val seq = SeqRegex.find(line)?.groupValues?.get(1)?.toIntOrNull()
                    if (rtt != null && seq != null) {
                        received++
                        emit(PingEvent.Reply(seq, rtt))
                    }
                    SummaryRegex.find(line)?.let { match ->
                        sent = match.groupValues[1].toInt()
                        received = match.groupValues[2].toInt()
                    }
                }
            }
            emit(PingEvent.Finished(sent, received))
        } catch (cancelled: CancellationException) {
            throw cancelled
        } finally {
            process.destroy()
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun uploadMbps(
        host: String,
        durationMs: Long,
        capMbps: Double,
        onProgress: (Float, Double) -> Unit
    ): Double = withContext(Dispatchers.IO) {
        // Paced just above the negotiated rate: the radio still sets the real pace, but the
        // burst never floods the home network or counts packets the phone dropped itself.
        val nanosPerPacket = ((PAYLOAD_BYTES + UDP_IP_OVERHEAD) * 8.0 / (capMbps.coerceAtLeast(1.0) * 1_000_000) * 1_000_000_000).toLong()
        val address = InetAddress.getByName(host)
        DatagramSocket().use { socket ->
            wifiNetwork()?.bindSocket(socket)
            socket.sendBufferSize = 256 * 1024
            val payload = ByteArray(PAYLOAD_BYTES)
            val packet = DatagramPacket(payload, payload.size, address, DISCARD_PORT)
            val start = System.nanoTime()
            val warmupEnd = start + WARMUP_MS * 1_000_000
            val end = start + durationMs * 1_000_000
            var measuredFrom = 0L
            var bytes = 0L
            var lastReport = start
            var nextSend = start
            while (isActive) {
                var now = System.nanoTime()
                if (now >= end) break
                if (now < nextSend) {
                    val wait = nextSend - now
                    if (wait > 1_000_000) Thread.sleep(wait / 1_000_000, (wait % 1_000_000).toInt())
                    now = System.nanoTime()
                }
                nextSend = maxOf(nextSend + nanosPerPacket, now - 10 * nanosPerPacket)
                try {
                    socket.send(packet)
                } catch (_: java.io.IOException) {
                    // A full queue can briefly refuse buffers; keep pacing on the link.
                    continue
                }
                if (now >= warmupEnd) {
                    if (measuredFrom == 0L) measuredFrom = now
                    bytes += PAYLOAD_BYTES + UDP_IP_OVERHEAD
                }
                if (now - lastReport > 200_000_000 && measuredFrom > 0) {
                    lastReport = now
                    onProgress(((now - start).toFloat() / (end - start)).coerceIn(0f, 1f), mbps(bytes, now - measuredFrom))
                }
            }
            if (measuredFrom == 0L) 0.0 else mbps(bytes, System.nanoTime() - measuredFrom)
        }
    }

    private fun mbps(bytes: Long, nanos: Long): Double =
        if (nanos <= 0) 0.0 else bytes * 8.0 / (nanos / 1_000_000_000.0) / 1_000_000.0

    private fun wifiNetwork(): Network? {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        @Suppress("DEPRECATION")
        return manager.allNetworks.firstOrNull { network ->
            manager.getNetworkCapabilities(network)?.let {
                it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) && !it.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
            } == true
        }
    }

    private companion object {
        const val DISCARD_PORT = 9
        const val PAYLOAD_BYTES = 1_400
        const val UDP_IP_OVERHEAD = 28
        const val WARMUP_MS = 500L
        val TimeRegex = Regex("time=([\\d.]+)")
        val SeqRegex = Regex("icmp_seq=(\\d+)")
        val SummaryRegex = Regex("(\\d+) packets transmitted, (\\d+) (?:packets )?received")
    }
}
