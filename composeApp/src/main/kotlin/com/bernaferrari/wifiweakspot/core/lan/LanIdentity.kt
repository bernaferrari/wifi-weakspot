package com.bernaferrari.wifiweakspot.core.lan

import com.bernaferrari.wifiweakspot.core.model.NetworkStatus

/** A radio identity is conservative: equal SSIDs and private IPs do not prove equal networks. */
fun lanNetworkKey(status: NetworkStatus): String? {
    if (!status.isConnected) return null
    val bssid = status.bssid.trim().lowercase()
    if (!Bssid.matches(bssid) || bssid == "02:00:00:00:00:00" || bssid == "00:00:00:00:00:00") return null
    return "wifi:$bssid"
}

private val Bssid = Regex("(?:[0-9a-f]{2}:){5}[0-9a-f]{2}")

internal fun sameLanConnection(first: NetworkStatus, second: NetworkStatus): Boolean =
    first.isConnected == second.isConnected && first.bssid.equals(second.bssid, ignoreCase = true) &&
        first.ssid == second.ssid && first.ipAddress == second.ipAddress && first.gateway == second.gateway

/** Use the actual answering port, including alternate web interfaces. */
internal fun deviceWebUrl(host: String, ports: List<Int>, router: Boolean = false): String? {
    val http = listOf(80, 8080, 8000).firstOrNull { it in ports }
    val https = listOf(443, 8443).firstOrNull { it in ports }
    val port = http ?: https ?: return if (router) "http://$host" else null
    val secure = http == null
    return "${if (secure) "https" else "http"}://$host${if (port == 80 || port == 443) "" else ":$port"}"
}
