package com.pulsewave.app.core.tools

import com.pulsewave.app.core.model.tools.PortResult
import com.pulsewave.app.core.model.tools.PortScanResult
import com.pulsewave.app.core.model.tools.PortStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
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

    @Volatile private var isCancelled = false
    @Volatile private var activeSocket: Socket? = null

    override suspend fun scanCommonPorts(host: String): PortScanResult {
        val commonPorts = listOf(20, 21, 22, 23, 25, 53, 80, 110, 143, 443, 445, 993, 995, 3306, 3389, 5900, 8080)
        return scanPorts(host, commonPorts)
    }

    override suspend fun scanRange(host: String, startPort: Int, endPort: Int): PortScanResult {
        require(startPort in 1..65535 && endPort in startPort..65535) { "Choose a valid port range between 1 and 65535." }
        return scanPorts(host, (startPort..endPort).toList())
    }

    override suspend fun scanPorts(host: String, ports: List<Int>): PortScanResult = withContext(Dispatchers.IO) {
        require(ports.isNotEmpty() && ports.all { it in 1..65535 }) { "Ports must be between 1 and 65535." }
        val address = java.net.InetAddress.getByName(host)
        val openPorts = mutableListOf<PortResult>()
        val start = System.nanoTime()
        isCancelled = false

        ports.forEachIndexed { index, port ->
            currentCoroutineContext().ensureActive()
            if (isCancelled) throw CancellationException("Port scan cancelled")
            _progress.value = (index + 1) / ports.size.toFloat()

            val socket = Socket()
            activeSocket = socket
            try {
                socket.connect(InetSocketAddress(address, port), 350)
                openPorts.add(PortResult(port, PortStatus.OPEN, getServiceName(port), null))
            } catch (_: Exception) {
                // Closed or filtered
            } finally {
                activeSocket = null
                try {
                    socket.close()
                } catch (_: Exception) {
                }
            }
        }

        currentCoroutineContext().ensureActive()
        if (isCancelled) throw CancellationException("Port scan cancelled")
        PortScanResult(
            host = host,
            ports = openPorts,
            scanType = "TCP Connect",
            durationMs = (System.nanoTime() - start) / 1_000_000
        )
    }

    override fun cancel() {
        isCancelled = true
        runCatching { activeSocket?.close() }
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
