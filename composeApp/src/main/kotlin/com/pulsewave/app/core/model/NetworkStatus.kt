package com.pulsewave.app.core.model

data class NetworkStatus(
    val ssid: String,
    val bssid: String,
    val frequency: Int,
    val channel: Int,
    val rssi: Int,
    val linkSpeed: Int, // Mbps
    val ipAddress: String,
    val isConnected: Boolean,
    val security: String
) {
    companion object {
        val Disconnected = NetworkStatus(
            ssid = "",
            bssid = "",
            frequency = 0,
            channel = 0,
            rssi = 0,
            linkSpeed = 0,
            ipAddress = "",
            isConnected = false,
            security = ""
        )
    }
}
