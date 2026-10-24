package com.bernaferrari.wifiweakspot.feature.overview

import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork

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
