package com.pulsewave.app.feature.overview

import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.wifi.WifiNetwork

/** Add scan-only metadata only when it belongs to the current radio and channel. */
internal fun connectionDetails(status: NetworkStatus, nearby: List<WifiNetwork>, nowMillis: Long): WifiNetwork {
    val observedAt = status.sampledAtMillis.takeIf { it > 0 } ?: nowMillis
    return nearby.firstOrNull {
        it.bssid.equals(status.bssid, ignoreCase = true) && it.frequency == status.frequency &&
            it.timestamp > 0 && nowMillis - it.timestamp in 0..45_000
    }?.copy(rssi = status.rssi, timestamp = observedAt)
        ?: WifiNetwork(status.ssid, status.bssid, status.rssi, status.frequency,
            channelWidth = 0, security = status.security, capabilities = "", timestamp = observedAt)
}
