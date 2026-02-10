package com.pulsewave.app.core.tools

import com.pulsewave.app.core.model.tools.*
import kotlinx.coroutines.flow.Flow

interface PingClient {
    fun ping(host: String, count: Int = 4, timeoutMs: Long = 1000): Flow<PingResult>
    suspend fun pingSummary(host: String, count: Int = 4, timeoutMs: Long = 1000): PingSummary
}

interface TracerouteClient {
    fun trace(host: String, maxHops: Int = 30, timeoutMs: Long = 2000): Flow<TraceHop>
    suspend fun traceComplete(host: String, maxHops: Int = 30): TraceResult
}

interface DnsClient {
    suspend fun lookup(
        query: String,
        recordType: DnsRecordType = DnsRecordType.A,
        server: String? = null
    ): DnsResult
}

interface WhoisClient {
    suspend fun query(target: String): WhoisResult
}

interface PortScanner {
    val progress: Flow<Float>
    
    suspend fun scanCommonPorts(host: String): PortScanResult
    suspend fun scanRange(host: String, startPort: Int, endPort: Int): PortScanResult
    suspend fun scanPorts(host: String, ports: List<Int>): PortScanResult
    fun cancel()
}
