package com.bernaferrari.wifiweakspot.core.tools

import com.bernaferrari.wifiweakspot.core.model.tools.PingResult
import com.bernaferrari.wifiweakspot.core.model.tools.PingSummary
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.toList
import kotlin.math.abs

class AndroidPingClient : PingClient {
    override fun ping(host: String, count: Int, timeoutMs: Long): Flow<PingResult> = flow {
        val timeoutSec = (timeoutMs / 1000).coerceAtLeast(1)
        val command = listOf("ping", "-c", "1", "-W", "$timeoutSec", host)

        require(count in 1..100 && timeoutMs > 0)
        require(host.isNotBlank() && !host.startsWith("-"))
        for (sequence in 1..count) {
            currentCoroutineContext().ensureActive()
            var process: Process? = null
            try {
                process = ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start()
                val finished = process.waitFor(timeoutMs + 1_000L, TimeUnit.MILLISECONDS)
                currentCoroutineContext().ensureActive()
                if (!finished) {
                    process.destroy()
                    emit(failure(host, sequence, "Request timeout"))
                    continue
                }
                val output = process.inputStream.bufferedReader().use { it.readText() }
                val exitCode = process.exitValue()
                if (exitCode == 0) {
                    val rtt = Regex("time=([\\d.]+)").find(output)?.groupValues?.get(1)?.toFloatOrNull()
                        ?: 0f
                    val ttl = Regex("ttl=(\\d+)").find(output)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                    val ip = Regex("from ([\\d.]+)").find(output)?.groupValues?.get(1)
                    emit(
                        PingResult(
                            host = host,
                            ip = ip,
                            sequence = sequence,
                            ttl = ttl,
                            rttMs = rtt,
                            isSuccess = true,
                            error = null
                        )
                    )
                } else {
                    emit(failure(host, sequence, "Request timeout"))
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                emit(failure(host, sequence, "Ping failed"))
            } finally {
                process?.destroy()
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun pingSummary(host: String, count: Int, timeoutMs: Long): PingSummary {
        val results = ping(host, count, timeoutMs).toList()
        val success = results.filter { it.isSuccess }
        val rtts = success.map { it.rttMs }
        val sent = results.size
        val received = success.size
        val loss = if (sent == 0) 100f else (sent - received) * 100f / sent
        val jitter = if (rtts.size >= 2) {
            rtts.zipWithNext { a, b -> abs(a - b) }.average().toFloat()
        } else {
            0f
        }
        return PingSummary(
            host = host,
            packetsTransmitted = sent,
            packetsReceived = received,
            packetLoss = loss,
            minRtt = rtts.minOrNull() ?: 0f,
            avgRtt = if (rtts.isEmpty()) 0f else rtts.average().toFloat(),
            maxRtt = rtts.maxOrNull() ?: 0f,
            jitter = jitter
        )
    }

    private fun failure(host: String, sequence: Int, error: String) = PingResult(
        host = host,
        ip = null,
        sequence = sequence,
        ttl = 0,
        rttMs = 0f,
        isSuccess = false,
        error = error
    )
}
