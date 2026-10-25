package com.bernaferrari.wifiweakspot.core.model.wifi

import kotlinx.serialization.Serializable

@Serializable
enum class WifiBand {
    BAND_2_4_GHZ,
    BAND_5_GHZ,
    BAND_6_GHZ,
    UNKNOWN
}

@Serializable
enum class ChannelWidth(val mhz: Int) {
    WIDTH_20(20),
    WIDTH_40(40),
    WIDTH_80(80),
    WIDTH_160(160),
    WIDTH_320(320),
    UNKNOWN(0)
}

@Serializable
enum class Security {
    OPEN,
    WEP,
    WPA,
    WPA2,
    WPA3,
    WPA2_WPA3,
    UNKNOWN
}

@Serializable
data class AccessPoint(
    val ssid: String,
    val bssid: String,
    val rssi: Int,
    val frequency: Int,
    val channel: Int,
    val channelWidth: ChannelWidth,
    val band: WifiBand,
    val security: Security,
    val capabilities: String,
    val vendor: String,
    val timestamp: Long
) {
    val signalLevel: Int
        get() = when {
            rssi >= -50 -> 4
            rssi >= -60 -> 3
            rssi >= -70 -> 2
            rssi >= -80 -> 1
            else -> 0
        }

}

@Serializable
data class WifiConnectionInfo(
    val ssid: String,
    val bssid: String,
    val ipAddress: String,
    val gateway: String,
    val dns: List<String>,
    val linkSpeed: Int,
    val txLinkSpeed: Int,
    val rxLinkSpeed: Int,
    val frequency: Int,
    val channel: Int,
    val rssi: Int,
    val security: Security
)

@Serializable
data class SignalSample(
    val bssid: String,
    val rssi: Int,
    val timestamp: Long
)

@Serializable
data class WifiScanSnapshot(
    val id: Long,
    val timestamp: Long,
    val accessPoints: List<AccessPoint>,
    val isPassive: Boolean
)
