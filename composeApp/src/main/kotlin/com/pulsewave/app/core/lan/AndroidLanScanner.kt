package com.pulsewave.app.core.lan

import com.pulsewave.app.core.model.lan.LanDevice
import com.pulsewave.app.core.model.lan.LanScanResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.net.InetAddress

class AndroidLanScanner : LanScanner {
    private val _scanProgress = MutableStateFlow(0f)
    override val scanProgress: Flow<Float> = _scanProgress.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: Flow<Boolean> = _isScanning.asStateFlow()

    private var isCancelled = false

    override suspend fun scanSubnet(subnet: String?): LanScanResult = withContext(Dispatchers.IO) {
        // Simple implementation: Read ARP table
        // Real implementation would involve sending ICMP packets (ping sweep)
        // to populate the ARP table first.
        _isScanning.value = true
        _scanProgress.value = 0f
        isCancelled = false
        
        val start = System.currentTimeMillis()
        val devices = mutableListOf<LanDevice>()

        try {
            // Trigger ARP population (simplified: ping gateway)
            // In a full app, we'd ping the whole subnet x.x.x.1-254
            try {
                // Determine own IP and subnet from WifiManager (injected in a real scenario)
                // For now, assume simplified scan by reading existing ARP entries
            } catch (e: Exception) {
                e.printStackTrace()
            }

            _scanProgress.value = 0.5f

            // Read ARP table
            val arpDevices = readArpTable()
            devices.addAll(arpDevices)
            
            _scanProgress.value = 1.0f
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isScanning.value = false
        }

        LanScanResult(
            id = System.currentTimeMillis(),
            timestamp = System.currentTimeMillis(),
            subnet = subnet ?: "Unknown",
            devices = devices,
            durationMs = System.currentTimeMillis() - start
        )
    }

    override suspend fun scanRange(startIp: String, endIp: String): LanScanResult {
        return scanSubnet()
    }

    override fun cancel() {
        isCancelled = true
        _isScanning.value = false
    }

    private fun readArpTable(): List<LanDevice> {
        val result = mutableListOf<LanDevice>()
        try {
            val reader = BufferedReader(FileReader("/proc/net/arp"))
            reader.readLine() // Skip header
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val tokens = line!!.split(Regex("\\s+"))
                if (tokens.size >= 4) {
                    val ip = tokens[0]
                    val mac = tokens[3]
                    // flag 0x2 means completed/valid
                    val flags = tokens[2] 
                    
                    if (mac != "00:00:00:00:00:00" && flags != "0x0") {
                         result.add(
                            LanDevice(
                                ipAddress = ip,
                                macAddress = mac,
                                hostname = null, // Resolve later
                                vendor = null, // Resolve later
                                lastSeen = System.currentTimeMillis(),
                                isOnline = true,
                                isTrusted = false,
                                customName = null,
                                openPorts = emptyList()
                            )
                        )
                    }
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }
}
