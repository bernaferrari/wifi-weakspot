package com.bernaferrari.wifiweakspot.core.router

import android.content.Context
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class AndroidRouterProbe(private val context: Context) : RouterProbe {

    override fun ping(host: String, count: Int, intervalSeconds: Double): Flow<PingEvent> = flow {
        require(host.isNotBlank() && !host.startsWith("-"))
        val process = ProcessBuilder(
            "ping", "-n", "-c", "$count", "-i", "%.1f".format(java.util.Locale.US, intervalSeconds.coerceAtLeast(0.2)), "-W", "1", host
        ).redirectErrorStream(true).start()
        var sent = count
        var received = 0
        coroutineScope {
            // Destroy ping immediately on cancellation, even when readLine is blocked awaiting output.
            val cancellationGuard = launch(start = CoroutineStart.UNDISPATCHED) {
                try { awaitCancellation() } finally { process.destroy() }
            }
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
                cancellationGuard.cancel()
                process.destroy()
            }
        }
    }.flowOn(Dispatchers.IO)

    private companion object {
        val TimeRegex = Regex("time=([\\d.]+)")
        val SeqRegex = Regex("icmp_seq=(\\d+)")
        val SummaryRegex = Regex("(\\d+) packets transmitted, (\\d+) (?:packets )?received")
    }
}
