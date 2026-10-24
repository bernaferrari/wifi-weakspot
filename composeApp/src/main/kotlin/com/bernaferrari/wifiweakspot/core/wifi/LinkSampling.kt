package com.bernaferrari.wifiweakspot.core.wifi

import com.bernaferrari.wifiweakspot.core.model.NetworkStatus
import com.bernaferrari.wifiweakspot.core.util.nowMillis
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform

/** Collects spaced platform reads, never repeated values from a stalled status cache.
 * Caller supplies a timeout and owns cancellation. A network/AP change aborts the batch. */
suspend fun collectFreshLinkSamples(
    source: Flow<NetworkStatus>,
    targetBssid: String,
    requestedAt: Long,
    count: Int,
    minimumIntervalMs: Long,
    expectedSsid: String? = null,
    clock: () -> Long = ::nowMillis
): List<NetworkStatus> {
    require(count > 0 && minimumIntervalMs > 0)
    var previousTime = requestedAt
    var selectedSsid = expectedSsid
    return source.transform { status ->
        check(status.isConnected) { "The connection was lost while measuring" }
        val knownBssid = status.bssid.takeUnless { it.isBlank() || it == "02:00:00:00:00:00" }
        check(knownBssid == null || knownBssid.equals(targetBssid, ignoreCase = true)) {
            "The access point changed while measuring"
        }
        // Capability/address callbacks may contain redacted or incomplete metadata.
        // They are not direct samples; a confirmed disconnect or different AP still aborts.
        if (status.sampledAtMillis <= 0) return@transform
        check(status.hasSignalMeasurement && status.bssid.equals(targetBssid, ignoreCase = true)) {
            "The access point changed while measuring"
        }
        if (selectedSsid == null) selectedSsid = status.ssid
        check(status.ssid == selectedSsid) { "The network changed while measuring" }
        val time = status.sampledAtMillis
        if (time > requestedAt && time <= clock() + 1_000 &&
            time - previousTime >= minimumIntervalMs && clock() - time <= minimumIntervalMs * 2) {
            previousTime = time
            emit(status)
        }
    }.take(count).toList().also {
        check(it.size == count) { "Fresh platform readings stopped before the batch completed" }
    }
}
