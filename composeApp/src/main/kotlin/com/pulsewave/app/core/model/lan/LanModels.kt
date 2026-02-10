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
    val openPorts: List<Int>
) {
    val displayName: String
        get() = customName ?: hostname ?: macAddress ?: ipAddress
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
