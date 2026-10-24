package com.bernaferrari.wifiweakspot.core.lan

import kotlinx.coroutines.ensureActive

/** ARP proves a cached neighbor exists, not that it replied to this scan. */
internal fun mergeDiscoveredHosts(reachable: Set<String>, cached: Set<String>): Set<String> = reachable + cached

/** ICMP first, then at most three short TCP handshakes; only replies confirm reachability. */
internal suspend fun reachableWithFallback(
    ip: String,
    ping: suspend (String) -> Boolean,
    tcp: suspend (String, Int) -> Boolean
): Boolean {
    kotlinx.coroutines.currentCoroutineContext().ensureActive()
    if (ping(ip)) return true
    for (port in listOf(80, 443, 445)) {
        kotlinx.coroutines.currentCoroutineContext().ensureActive()
        if (tcp(ip, port)) return true
    }
    return false
}
