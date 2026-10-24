package com.bernaferrari.wifiweakspot.core.wifi

import kotlinx.coroutines.flow.StateFlow

interface WifiScanner {
    val scanResults: StateFlow<List<WifiNetwork>>
    val isScanning: StateFlow<Boolean>

    /** @return true when the platform accepted the scan request. */
    fun startScan(): Boolean

    fun stopScan()

    /**
     * Starts a scan and waits for successful observations made after the request.
     * Returns null for a rejected, failed, stopped, timed-out or already-running scan.
     * Cached observations are never returned as a new measurement.
     */
    suspend fun awaitNextScan(timeoutMs: Long = 8_000): List<WifiNetwork>?
}
