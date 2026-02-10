package com.pulsewave.app.core.tools

import com.pulsewave.app.core.model.tools.PortResult
import com.pulsewave.app.core.model.tools.PortScanResult
import com.pulsewave.app.core.model.tools.PortStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

class AndroidPortScanner : PortScanner {
    private val _progress = MutableStateFlow(0f)
    override val progress: Flow<Float> = _progress.asStateFlow()

    private var isCancelled = false

    override suspend fun scanCommonPorts(host: String): PortScanResult {
        val commonPorts = listOf(20, 21, 22, 23, 25, 53, 80, 110, 143, 443, 445, 993, 995, 3306, 3389, 5900, 8080)
        return scanPorts(host, commonPorts)
    }

    override suspend fun scanRange(host: String, startPort: Int, endPort: Int): PortScanResult {
        return scanPorts(host, (startPort..endPort).toList())
    }

    override suspend fun scanPorts(host: String, ports: List<Int>): PortScanResult = withContext(Dispatchers.IO) {
        val openPorts = mutableListOf<PortResult>()
        val start = System.currentTimeMillis()
        isCancelled = false

        ports.forEachIndexed { index, port ->
            if (isCancelled) return@forEachIndexed
            _progress.value = (index + 1) / ports.size.toFloat()

            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(host, port), 200) // 200ms timeout for speed
                socket.close()
                openPorts.add(PortResult(port, PortStatus.OPEN, getServiceName(port), null))
            } catch (e: Exception) {
                // Closed or filtered
            }
        }

        PortScanResult(
            host = host,
            ports = openPorts,
            scanType = "TCP Connect",
            durationMs = System.currentTimeMillis() - start
        )
    }

    override fun cancel() {
        isCancelled = true
    }

    private fun getServiceName(port: Int): String {
        return when (port) {
            21 -> "FTP"
            22 -> "SSH"
            23 -> "Telnet"
            25 -> "SMTP"
            53 -> "DNS"
            80 -> "HTTP"
            443 -> "HTTPS"
            445 -> "SMB"
            3306 -> "MySQL"
            3389 -> "RDP"
            8080 -> "HTTP-Proxy"
            else -> "Unknown"
        }
    }
}
