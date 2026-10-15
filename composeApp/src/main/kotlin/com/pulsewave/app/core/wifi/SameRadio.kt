package com.pulsewave.app.core.wifi

/**
 * Routers broadcast guest, IoT and hidden networks from the same radio, each with a BSSID
 * derived from the main one: either the first byte changes (locally administered bit plus an
 * index, b0:95:… → b6:95:…) or the last byte steps (…:a8:0a → …:a8:0b). These are one radio,
 * not neighbors; counting them as congestion would blame your own router for crowding itself.
 */
fun sameRadio(a: String, b: String): Boolean {
    val x = macBytes(a) ?: return false
    val y = macBytes(b) ?: return false
    if (x == y) return true
    val restMatches = (1..5).all { x[it] == y[it] }
    if (restMatches) return true
    val headMatches = (0..4).all { x[it] == y[it] }
    return headMatches && (x[5] and 0xF0) == (y[5] and 0xF0)
}

/** Same radio and same channel: another name for the very same transmitter. */
fun WifiNetwork.isSiblingOf(bssid: String?, frequency: Int): Boolean =
    bssid != null && !this.bssid.equals(bssid, ignoreCase = true) && this.frequency == frequency && sameRadio(this.bssid, bssid)

/** Networks that really compete with yours: drops your own BSSID and its sibling names. */
fun List<WifiNetwork>.competitorsOf(bssid: String?, frequency: Int): List<WifiNetwork> =
    filterNot { bssid != null && (it.bssid.equals(bssid, ignoreCase = true) || it.isSiblingOf(bssid, frequency)) }

private fun macBytes(mac: String): List<Int>? {
    val parts = mac.split(':')
    if (parts.size != 6) return null
    return parts.map { it.toIntOrNull(16) ?: return null }
}

/** One physical radio and every network name it broadcasts. */
data class RadioGroup(val lead: WifiNetwork, val members: List<WifiNetwork>) {
    val extraNames: Int get() = members.size - 1
}

/**
 * Collapses guest and hidden names onto the radio that sends them, so a chart draws one hill
 * per transmitter. The lead is the network you're connected to, else the strongest name.
 */
fun groupByRadio(networks: List<WifiNetwork>, connectedBssid: String? = null): List<RadioGroup> {
    val groups = mutableListOf<MutableList<WifiNetwork>>()
    networks.sortedByDescending { it.rssi }.forEach { network ->
        val home = groups.firstOrNull { group ->
            group.any { it.frequency == network.frequency && sameRadio(it.bssid, network.bssid) }
        }
        if (home != null) home += network else groups += mutableListOf(network)
    }
    return groups.map { members ->
        val lead = members.firstOrNull { connectedBssid != null && it.bssid.equals(connectedBssid, ignoreCase = true) }
            ?: members.firstOrNull { it.ssid.isNotEmpty() }
            ?: members.first()
        RadioGroup(lead, members)
    }
}
