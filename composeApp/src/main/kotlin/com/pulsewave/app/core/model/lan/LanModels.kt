package com.pulsewave.app.core.model.lan

import kotlinx.serialization.Serializable

@Serializable
data class LanDevice(
    val ipAddress: String,
    val macAddress: String?,
    val hostname: String?,
    val vendor: String?,
    val lastSeen: Long,
    val isOnline: Boolean,
    val isTrusted: Boolean,
    val customName: String?,
    val openPorts: List<Int>,
    /** Name the device announces over mDNS, such as "Sala de Estar". */
    val announcedName: String? = null,
    /** Model hint from mDNS, such as "AppleTV14,1". */
    val model: String? = null,
    /** mDNS service types it offers, such as "_airplay._tcp". */
    val services: List<String> = emptyList()
) {
    val displayName: String
        get() = customName ?: announcedName ?: hostname ?: macAddress ?: ipAddress
}

@Serializable
data class ServiceInfo(
    val name: String,
    val type: String,
    val host: String,
    val port: Int,
    val txtRecords: Map<String, String>
)

@Serializable
data class LanScanResult(
    val id: Long,
    val timestamp: Long,
    val subnet: String,
    val devices: List<LanDevice>,
    val durationMs: Long
)
