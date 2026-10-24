package com.bernaferrari.wifiweakspot.core.model.tools

import kotlinx.serialization.Serializable

@Serializable
data class PingResult(
    val host: String,
    val ip: String?,
    val sequence: Int,
    val ttl: Int,
    val rttMs: Float,
    val isSuccess: Boolean,
    val error: String?
)

@Serializable
data class PingSummary(
    val host: String,
    val packetsTransmitted: Int,
    val packetsReceived: Int,
    val packetLoss: Float,
    val minRtt: Float,
    val avgRtt: Float,
    val maxRtt: Float,
    val jitter: Float
)

@Serializable
data class TraceHop(
    val hopNumber: Int,
    val ip: String?,
    val hostname: String?,
    val rttMs: List<Float>,
    val isTimeout: Boolean,
    val asn: String?,
    val geo: String?
)

@Serializable
data class TraceResult(
    val host: String,
    val hops: List<TraceHop>,
    val isComplete: Boolean,
    val durationMs: Long
)

@Serializable
enum class DnsRecordType {
    A, AAAA, CNAME, MX, TXT, NS, SOA, SRV, PTR
}

@Serializable
data class DnsRecord(
    val type: DnsRecordType,
    val name: String,
    val value: String,
    val ttl: Int
)

@Serializable
data class DnsResult(
    val query: String,
    val server: String,
    val records: List<DnsRecord>,
    val queryTimeMs: Long,
    val isSuccess: Boolean,
    val error: String?
)

@Serializable
data class WhoisResult(
    val query: String,
    val registrar: String?,
    val organization: String?,
    val createdDate: String?,
    val expiresDate: String?,
    val nameServers: List<String>,
    val rawText: String,
    val isSuccess: Boolean
)

@Serializable
enum class PortStatus {
    OPEN, CLOSED, FILTERED, UNKNOWN
}

@Serializable
data class PortResult(
    val port: Int,
    val status: PortStatus,
    val service: String?,
    val banner: String?
)

@Serializable
data class PortScanResult(
    val host: String,
    val ports: List<PortResult>,
    val scanType: String,
    val durationMs: Long
)
