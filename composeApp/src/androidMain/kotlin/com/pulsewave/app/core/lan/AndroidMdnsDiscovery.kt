package com.pulsewave.app.core.lan

import android.content.Context
import android.net.wifi.WifiManager
import java.net.DatagramPacket
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.MulticastSocket
import java.net.SocketTimeoutException

/**
 * Listens for devices describing themselves over mDNS (Bonjour). Blocking; call from an IO
 * thread. Android needs a multicast lock or the Wi-Fi chip drops multicast replies to save power.
 */
class AndroidMdnsDiscovery(private val context: Context) {

    /**
     * Listens for at least [minMs], then until [keepListening] turns false (the address sweep
     * finishing), but never longer than [maxMs]. Slow sleepers get several chances to answer.
     */
    fun discover(minMs: Long = 3_000, maxMs: Long = 25_000, keepListening: () -> Boolean = { false }): List<Announcement> {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val lock = wifi.createMulticastLock("pulsewave-mdns").apply { setReferenceCounted(false) }
        val records = Mdns.Records()
        runCatching {
            lock.acquire()
            // Port 5353 hears multicast answers too; if the system holds it exclusively, a random
            // port still gets replies to "unicast-response" questions.
            val (socket, shared) = openSocket()
            socket.use {
                val group = InetAddress.getByName(Mdns.Group)
                if (shared) runCatching { socket.joinGroup(group) }
                val query = Mdns.query(unicast = !shared)
                val target = InetSocketAddress(group, Mdns.Port)
                socket.soTimeout = 250
                val buffer = ByteArray(9_000)
                val started = System.currentTimeMillis()
                var sent = 0
                val askAt = listOf(0L, 1_000L, 3_000L, 8_000L)
                while (true) {
                    val elapsed = System.currentTimeMillis() - started
                    if (elapsed >= maxMs || (elapsed >= minMs && !keepListening())) break
                    // Ask again over time: later rounds catch devices that were asleep for the first.
                    if (sent < askAt.size && elapsed >= askAt[sent]) {
                        runCatching { socket.send(DatagramPacket(query, query.size, target)) }
                        sent++
                    }
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)
                        records.addAll(Mdns.parse(packet.data.copyOf(packet.length)))
                    } catch (_: SocketTimeoutException) {
                    }
                }
                if (shared) runCatching { socket.leaveGroup(group) }
            }
        }
        if (lock.isHeld) lock.release()
        return records.announcements()
    }

    private fun openSocket(): Pair<MulticastSocket, Boolean> = runCatching {
        MulticastSocket(null).apply {
            reuseAddress = true
            bind(InetSocketAddress(Mdns.Port))
            timeToLive = 255
        } to true
    }.getOrElse { MulticastSocket(0) to false }
}
