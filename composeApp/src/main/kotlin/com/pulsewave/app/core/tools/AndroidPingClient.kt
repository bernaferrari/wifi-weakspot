package com.pulsewave.app.core.tools

import com.pulsewave.app.core.model.tools.PingResult
import com.pulsewave.app.core.model.tools.PingSummary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class AndroidPingClient : PingClient {
    override fun ping(host: String, count: Int, timeoutMs: Long): Flow<PingResult> = flow {
        // -c count, -W timeout_sec
        val timeoutSec = (timeoutMs / 1000).coerceAtLeast(1)
        val command = listOf("ping", "-c", "1", "-W", "$timeoutSec", host)

        for (i in 1..count) {
            val start = System.currentTimeMillis()
            try {
                val process = ProcessBuilder(command).start()
                val reader = BufferedReader(InputStreamReader(process.inputStream))
                val output = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    output.append(line).append("\n")
                }
                process.waitFor()
                val exitCode = process.exitValue()
                val duration = System.currentTimeMillis() - start

                if (exitCode == 0) {
                    // Parse output for time (e.g., time=12.3 ms)
                    val timeMatch = Regex("time=([\\d.]+)").find(output)
                    val rtt = timeMatch?.groupValues?.get(1)?.toFloatOrNull() ?: duration.toFloat()
                    
                    emit(PingResult(
                        host = host,
                        ip = null, // Parsing IP implementation omitted for brevity
                        sequence = i,
                        ttl = 64, // Placeholder, would need regex parsing
                        rttMs = rtt,
                        isSuccess = true,
                        error = null
                    ))
                } else {
                    emit(PingResult(
                        host = host,
                        ip = null,
                        sequence = i,
                        ttl = 0,
                        rttMs = 0f,
                        isSuccess = false,
                        error = "Request timeout"
                    ))
                }
            } catch (e: Exception) {
                emit(PingResult(
                    host = host,
                    ip = null,
                    sequence = i,
                    ttl = 0,
                    rttMs = 0f,
                    isSuccess = false,
                    error = e.message
                ))
            }
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun pingSummary(host: String, count: Int, timeoutMs: Long): PingSummary {
        // Implementation simplified for this step
        return PingSummary(host, count, 0, 0f, 0f, 0f, 0f, 0f)
    }
}
