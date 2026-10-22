package com.pulsewave.app.core.lan

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.pulsewave.app.core.model.lan.LanDevice
import com.pulsewave.app.core.model.lan.LanScanResult
import com.pulsewave.app.core.wifi.getVendorName
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class AndroidLanScanner(
    private val context: Context
) : LanScanner {
    private val mdns = AndroidMdnsDiscovery(context)

    private val _scanProgress = MutableStateFlow(0f)
    override val scanProgress: Flow<Float> = _scanProgress.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: Flow<Boolean> = _isScanning.asStateFlow()

    @Volatile
    private var isCancelled = false
    private val activeProcesses = ConcurrentHashMap.newKeySet<Process>()
    private val activeSockets = ConcurrentHashMap.newKeySet<Socket>()

    override suspend fun scanSubnet(subnet: String?): LanScanResult = withContext(Dispatchers.IO) {
        val local = localIpv4()
        check(local != null) {
            val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = connectivity.activeNetwork?.let(connectivity::getNetworkCapabilities)
            if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) {
                "Device discovery isn't available through this VPN connection. Check whether your VPN allows local-network access, or try again when it is disconnected."
            } else "Connect to Wi-Fi or Ethernet before scanning local devices."
        }
        val hosts = if (local != null) {
            hostsOnLink(local.first, local.second).filter { it != local.first }
        } else {
            emptyList()
        }
        val label = local?.let { "${networkAddress(it.first, it.second)}/${it.second.coerceAtLeast(24).coerceAtMost(32)}" }
            ?: subnet
            ?: "Not connected"
        scanHosts(hosts, label, ownIp = local?.first)
    }

    override suspend fun scanRange(startIp: String, endIp: String): LanScanResult = withContext(Dispatchers.IO) {
        val start = ipv4ToInt(startIp)?.toLong()?.and(0xffffffffL)
        val end = ipv4ToInt(endIp)?.toLong()?.and(0xffffffffL)
        val hosts = if (start != null && end != null && end >= start) {
            val cappedEnd = minOf(end, start + 1023L)
            (start..cappedEnd).map { intToIpv4(it.toInt()) }
        } else {
            emptyList()
        }
        scanHosts(hosts, "$startIp-$endIp")
    }

    override fun cancel() {
        isCancelled = true
        activeProcesses.forEach { it.destroy() }
        activeSockets.forEach { runCatching { it.close() } }
        _isScanning.value = false
    }

    private suspend fun scanHosts(hosts: List<String>, subnetLabel: String, ownIp: String? = null): LanScanResult {
        val started = System.currentTimeMillis()
        _isScanning.value = true
        _scanProgress.value = 0f
        isCancelled = false
        val hostSet = hosts.toSet()
        val reached = mutableListOf<String>()
        try {
            // Devices name themselves over mDNS while the sweep runs; it costs no extra time.
            val announced = coroutineScope {
                val sweeping = java.util.concurrent.atomic.AtomicBoolean(true)
                val listening = async(Dispatchers.IO) {
                    runCatching { mdns.discover(keepListening = { sweeping.get() && !isCancelled }) }.getOrDefault(emptyList())
                }
                try { sweep(hosts, reached) } finally { sweeping.set(false) }
                listening.await()
            }.filter { it.ip in hostSet || it.ip == ownIp }.associateBy { it.ip }
            val arp = readArpTable()
            currentCoroutineContext().ensureActive()
            if (isCancelled) throw CancellationException("LAN scan cancelled")
            // Anything that announced itself is on the network, even if it ignored the sweep.
            val ips = mergeDiscoveredHosts(reached.toSet() + announced.keys.filter { it != ownIp }, arp.keys.filter { it in hostSet }.toSet())
            val names = reverseNames(ips)
            val devices = ips.map { ip ->
                val mac = arp[ip]
                val said = announced[ip]
                LanDevice(
                    ipAddress = ip,
                    macAddress = mac,
                    hostname = names[ip],
                    vendor = mac?.let { getVendorName(it) },
                    lastSeen = System.currentTimeMillis(),
                    isOnline = ip in reached || said != null,
                    isTrusted = false,
                    customName = null,
                    openPorts = emptyList(),
                    announcedName = said?.name,
                    model = said?.model,
                    services = said?.services.orEmpty().sorted()
                )
            } + listOfNotNull(ownIp?.let { ip ->
                // The sweep skips this phone; list it anyway so people can find themselves.
                LanDevice(
                    ipAddress = ip, macAddress = null, hostname = null, vendor = null,
                    lastSeen = System.currentTimeMillis(), isOnline = true, isTrusted = false, customName = null,
                    openPorts = emptyList(), announcedName = ownDeviceName(), model = android.os.Build.MODEL
                )
            })
            _scanProgress.value = 1f
            return LanScanResult(
                id = started,
                timestamp = started,
                subnet = subnetLabel,
                devices = devices.sortedBy { ipv4ToInt(it.ipAddress) ?: 0 },
                durationMs = System.currentTimeMillis() - started
            )
        } finally {
            _isScanning.value = false
        }
    }

    private fun ownDeviceName(): String? =
        runCatching { android.provider.Settings.Global.getString(context.contentResolver, "device_name") }.getOrNull()
            ?.takeIf { it.isNotBlank() } ?: android.os.Build.MODEL

    /** Pings every address, 32 at a time, collecting the ones that answer. */
    private suspend fun sweep(hosts: List<String>, reached: MutableList<String>) {
        if (hosts.isEmpty()) return
        val gate = Semaphore(32)
        val finished = AtomicInteger(0)
        coroutineScope {
            hosts.map { ip ->
                async {
                    if (isCancelled) return@async null
                    val online = gate.withPermit {
                        if (isCancelled) false else reachableWithFallback(ip, ::ping, ::tcpReachable)
                    }
                    _scanProgress.value = finished.incrementAndGet() / hosts.size.toFloat()
                    if (online) ip else null
                }
            }.awaitAll().filterNotNullTo(reached)
        }
    }

    /**
     * Asks the router's DNS for each device's name ("living-room-tv.lan"). The lookups block and
     * can't be cancelled, so they run on a small pool and the scan waits at most [NameBudgetMs].
     */
    private fun reverseNames(ips: Collection<String>): Map<String, String> {
        if (ips.isEmpty()) return emptyMap()
        val pool = Executors.newFixedThreadPool(minOf(16, ips.size)) { task -> Thread(task, "lan-names").apply { isDaemon = true } }
        try {
            val lookups = ips.associateWith { ip ->
                pool.submit(Callable { InetAddress.getByName(ip).canonicalHostName.takeIf { it != ip && it.isNotBlank() } })
            }
            val deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(NameBudgetMs)
            return lookups.mapNotNull { (ip, lookup) ->
                val left = (deadline - System.nanoTime()).coerceAtLeast(0L)
                runCatching { lookup.get(left, TimeUnit.NANOSECONDS) }.getOrNull()?.let { ip to it }
            }.toMap()
        } finally {
            pool.shutdownNow()
        }
    }

    private suspend fun ping(ip: String): Boolean {
        currentCoroutineContext().ensureActive()
        var process: Process? = null
        return try {
            process = ProcessBuilder("ping", "-c", "1", "-W", "1", ip)
                .redirectErrorStream(true)
                .start()
            activeProcesses.add(process)
            val finished = process.waitFor(2, TimeUnit.SECONDS)
            currentCoroutineContext().ensureActive()
            finished && process.exitValue() == 0
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            false
        } finally {
            process?.let { activeProcesses.remove(it) }
            process?.destroy()
        }
    }

    private suspend fun tcpReachable(ip: String, port: Int): Boolean {
        currentCoroutineContext().ensureActive()
        if (isCancelled) throw CancellationException("LAN scan cancelled")
        val socket = Socket()
        activeSockets.add(socket)
        return try {
            socket.connect(InetSocketAddress(ip, port), 150)
            currentCoroutineContext().ensureActive()
            true
        } catch (e: CancellationException) { throw e } catch (_: Exception) { false }
        finally { activeSockets.remove(socket); runCatching { socket.close() } }
    }

    private fun localIpv4(): Pair<String, Int>? {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivity.activeNetwork ?: return null
        val capabilities = connectivity.getNetworkCapabilities(network) ?: return null
        if (!capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        ) {
            return null
        }
        val link = connectivity.getLinkProperties(network) ?: return null
        val address = link.linkAddresses.firstOrNull {
            val ip = it.address
            ip is Inet4Address && !ip.isLoopbackAddress
        } ?: return null
        val host = address.address.hostAddress ?: return null
        return host to address.prefixLength
    }

    private fun hostsOnLink(ip: String, prefixLength: Int): List<String> {
        val address = ipv4ToInt(ip) ?: return emptyList()
        val prefix = if (prefixLength < 24) 24 else prefixLength.coerceAtMost(32)
        val hostBits = 32 - prefix
        val mask = if (hostBits == 0) -1 else (-1 shl hostBits)
        val base = if (prefixLength < 24) address and (-1 shl 8) else address and mask
        val size = 1 shl hostBits
        val hostOffsets = if (prefix >= 31) 0 until size else 1 until size - 1
        return hostOffsets.map { intToIpv4(base + it) }
    }

    private fun networkAddress(ip: String, prefixLength: Int): String {
        val address = ipv4ToInt(ip) ?: return ip
        val prefix = if (prefixLength < 24) 24 else prefixLength.coerceAtMost(32)
        val hostBits = 32 - prefix
        val mask = if (hostBits == 0) -1 else (-1 shl hostBits)
        val base = if (prefixLength < 24) address and (-1 shl 8) else address and mask
        return intToIpv4(base)
    }

    private fun readArpTable(): Map<String, String> {
        val result = mutableMapOf<String, String>()
        try {
            BufferedReader(FileReader("/proc/net/arp")).use { reader ->
                reader.readLine()
                while (true) {
                    if (isCancelled) break
                    val line = reader.readLine() ?: break
                    val tokens = line.split(Regex("\\s+"))
                    if (tokens.size < 4) continue
                    val ip = tokens[0]
                    val flags = tokens[2]
                    val mac = tokens[3]
                    if (mac != "00:00:00:00:00:00" && flags != "0x0") {
                        result[ip] = mac
                    }
                }
            }
        } catch (_: Exception) {
        }
        return result
    }

    private fun ipv4ToInt(ip: String): Int? {
        val parts = ip.split('.')
        if (parts.size != 4) return null
        var value = 0
        for (part in parts) {
            val octet = part.toIntOrNull() ?: return null
            if (octet !in 0..255) return null
            value = (value shl 8) or octet
        }
        return value
    }

    private fun intToIpv4(value: Int): String {
        return "${(value ushr 24) and 255}.${(value ushr 16) and 255}.${(value ushr 8) and 255}.${value and 255}"
    }
}

private const val NameBudgetMs = 2_000L
