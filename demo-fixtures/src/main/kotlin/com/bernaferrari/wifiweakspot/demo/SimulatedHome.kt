package com.bernaferrari.wifiweakspot.demo

import com.bernaferrari.wifiweakspot.core.util.nowMillis
import com.bernaferrari.wifiweakspot.core.wifi.WifiNetwork
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

/**
 * A believable apartment block for the demo builds: your router plus ~30 neighbors,
 * with signal that drifts like the real thing. Every simulated service reads from this.
 */
object SimulatedHome {
    const val Ssid = "Home Wi-Fi"
    const val Bssid = "02:15:b2:00:00:01"
    const val Gateway = "192.168.1.1"
    const val PhoneIp = "192.168.1.23"
    const val Frequency = 5180

    private class Ap(val ssid: String, val bssid: String, var rssi: Int, val freq: Int, val width: Int, val center: Int, val caps: String)

    private val random = Random(42)
    private val startedAt = nowMillis()

    private val aps: List<Ap> = buildList {
        add(Ap(Ssid, Bssid, -58, Frequency, 80, 5210, Wpa3))
        add(Ap(Ssid, "02:15:b2:00:00:02", -61, 2437, 20, 0, Wpa2))
        val names = listOf("Vivo-Fibra", "NET_2G", "CLARO", "Apto 302", "Casa da Ana", "TP-Link_7C21", "Xiaomi_5A", "ASUS", "Linksys00412",
            "DIRECT-HP Printer", "Escritorio", "Familia Silva", "Galaxy A54", "iPhone de Joao", "Starlink")
        val channels24 = listOf(1, 1, 6, 6, 6, 11, 11, 11, 3, 9, 1, 6, 11, 4, 11, 1, 6)
        channels24.forEachIndexed { i, ch ->
            val width = if (i % 6 == 0) 40 else 20
            val freq = 2407 + ch * 5
            add(Ap(names[i % names.size], "a0:00:${(i + 10)}:00:00:01", -60 - random.nextInt(32), freq, width,
                if (width == 40) freq + 10 else 0, if (i == 2) Open else Wpa2))
        }
        val channels5 = listOf(36, 44, 52, 100, 116, 149, 157, 161, 40, 149, 36)
        channels5.forEachIndexed { i, ch ->
            val freq = 5000 + ch * 5
            val width = listOf(20, 40, 80)[i % 3]
            add(Ap("${names[(i + 3) % names.size]} 5G", "b0:00:${(i + 10)}:00:00:05", -64 - random.nextInt(28), freq, width,
                if (width > 20) freq + width / 2 - 10 else 0, Wpa2))
        }
        add(Ap("$Ssid 6E", "02:15:b2:00:00:06", -66, 6115, 160, 6185, Wpa3))
        add(Ap("Neighbor 6E", "c0:00:00:00:00:06", -84, 6435, 80, 6465, Wpa3))
    }

    /** One scan: every neighbor drifts a little, as real readings do. */
    fun scan(): List<WifiNetwork> {
        val now = nowMillis()
        aps.forEach { it.rssi = (it.rssi + random.nextInt(-3, 4)).coerceIn(-93, -58) }
        aps[0].rssi = connectedRssi()
        aps[1].rssi = (connectedRssi() - 3).coerceAtLeast(-93)
        return aps.map {
            WifiNetwork(it.ssid, it.bssid, it.rssi, it.freq, it.width, com.bernaferrari.wifiweakspot.core.wifi.securityLabel(it.caps), it.caps, now, centerFreq0 = it.center)
        }
    }

    /** Hold the connected sample steady for still Android marketing captures. */
    var stationary: Boolean = false

    /**
     * Signal on the connected link. It mostly sits near the router, then fades on a slow
     * loop as if someone carried the phone to the far bedroom and back, so Walk has a story.
     */
    fun connectedRssi(): Int {
        if (stationary) return -58
        val seconds = (nowMillis() - startedAt) / 1000.0
        val phase = (seconds % LoopSeconds) / LoopSeconds
        val dip = max(0.0, sin(phase * 2 * PI)).pow(3) * 32
        return (-58 - dip + random.nextInt(-2, 3)).toInt().coerceIn(-92, -58)
    }

    /** Link rate the radio would negotiate at [rssi]. */
    fun linkSpeedMbps(rssi: Int): Int = when {
        rssi >= -55 -> 866
        rssi >= -62 -> 650
        rssi >= -68 -> 433
        rssi >= -74 -> 200
        rssi >= -80 -> 87
        else -> 29
    }

    /** Router round trip: a few ms close by, slower and spikier when the signal is weak. */
    fun routerRttMs(rssi: Int): Float {
        val base = 2.5f + random.nextFloat() * 2.5f
        val weak = ((-rssi - 65).coerceAtLeast(0)) * (0.8f + random.nextFloat() * 1.6f)
        return base + weak
    }

    private const val LoopSeconds = 75.0
    private const val Wpa2 = "[WPA2-PSK-CCMP][ESS]"
    private const val Wpa3 = "[RSN-SAE-CCMP][ESS]"
    private const val Open = "[ESS]"
}
