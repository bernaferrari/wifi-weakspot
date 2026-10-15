package com.pulsewave.app.core.data

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.LinkProperties
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.pulsewave.app.core.domain.NetworkStatusRepository
import com.pulsewave.app.core.model.NetworkStatus
import com.pulsewave.app.core.wifi.frequencyToChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.net.Inet4Address

@SuppressLint("MissingPermission")
class AndroidNetworkStatusRepository(
    private val context: Context
) : NetworkStatusRepository {

    private val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    override fun getCurrentNetworkStatus(refreshIntervalMs: Long): Flow<NetworkStatus> = callbackFlow {
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        val tracker = WifiConnectionTracker<Network, WifiInfo, LinkProperties>()
        fun publish() {
            val state = tracker.current()?.second
            trySend(mapWifiInfo(state?.info, state?.link, state?.validated ?: false))
        }
        val handleAvailable: (Network) -> Unit = { network ->
            tracker.available(network)
        }
        val handleCapabilities: (Network, NetworkCapabilities) -> Unit = { network, capabilities ->
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                capabilities.transportInfo as? WifiInfo
            } else {
                @Suppress("DEPRECATION")
                runCatching { wifiManager.connectionInfo }.getOrNull()
            }
            tracker.capabilities(network, info,
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
            publish()
        }
        val handleLink: (Network, LinkProperties) -> Unit = { network, link ->
            tracker.link(network, link)
            publish()
        }
        val handleLost: (Network) -> Unit = { network ->
            tracker.lost(network)
            publish()
        }
        val callback = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            object : ConnectivityManager.NetworkCallback(ConnectivityManager.NetworkCallback.FLAG_INCLUDE_LOCATION_INFO) {
                override fun onAvailable(network: Network) = handleAvailable(network)
                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = handleCapabilities(network, capabilities)
                override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) = handleLink(network, linkProperties)
                override fun onLost(network: Network) = handleLost(network)
            }
        } else {
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) = handleAvailable(network)
                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) = handleCapabilities(network, capabilities)
                override fun onLinkPropertiesChanged(network: Network, linkProperties: LinkProperties) = handleLink(network, linkProperties)
                override fun onLost(network: Network) = handleLost(network)
            }
        }

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        fun refreshSelectedWifi() {
            val selection = tracker.selection()
            val selected = selection.network?.let { network ->
                connectivityManager.getNetworkCapabilities(network)?.takeIf(::isPhysicalWifi)
                    ?.let { network to it }
            } ?: findPhysicalWifi()
            val network = selected?.first
            val capabilities = selected?.second
            val link = network?.let(connectivityManager::getLinkProperties)
            val previousInfo = tracker.current()?.takeIf { it.first == network }?.second?.info
            val info = capabilities?.let { readWifiInfo(it, link, previousInfo) }
            // A callback may select a new connection while the platform reads above run.
            if (tracker.refresh(selection, network, info, link,
                    capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false)) {
                publish()
            }
        }
        refreshSelectedWifi()
        // Refresh the selected physical Wi-Fi link even when a VPN is the default network.
        val refresh = launch {
            while (isActive) {
                delay(refreshIntervalMs)
                refreshSelectedWifi()
            }
        }

        awaitClose {
            refresh.cancel()
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }.onStart {
        emit(readCurrentStatus())
    }.conflate()

    override fun getAirHealth(): Flow<Int> = flow {
        getCurrentNetworkStatus().collect { status ->
            if (!status.isConnected) {
                emit(0)
            } else {
                emit(calculateHealth(status.rssi))
            }
        }
    }

    private fun isPhysicalWifi(capabilities: NetworkCapabilities): Boolean =
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            !capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)

    private fun findPhysicalWifi(): Pair<Network, NetworkCapabilities>? {
        val defaultNetwork = connectivityManager.activeNetwork
        val candidates = buildList {
            if (defaultNetwork != null) add(defaultNetwork)
            @Suppress("DEPRECATION")
            addAll(connectivityManager.allNetworks.filter { it != defaultNetwork })
        }
        return candidates.firstNotNullOfOrNull { network ->
            connectivityManager.getNetworkCapabilities(network)?.takeIf(::isPhysicalWifi)
                ?.let { network to it }
        }
    }

    private fun readCurrentStatus(): NetworkStatus {
        val (network, capabilities) = findPhysicalWifi() ?: return NetworkStatus.Disconnected
        val link = connectivityManager.getLinkProperties(network)
        return mapWifiInfo(readWifiInfo(capabilities, link), link,
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED))
    }

    private fun readWifiInfo(capabilities: NetworkCapabilities, link: LinkProperties?, previous: WifiInfo? = null): WifiInfo? {
        val perNetwork = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            capabilities.transportInfo as? WifiInfo
        } else null
        @Suppress("DEPRECATION")
        val primary = runCatching { wifiManager.connectionInfo }.getOrNull()
        if (primary == null) return perNetwork
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return primary
        val bssid = (perNetwork?.bssid?.takeUnless { it == "02:00:00:00:00:00" || it.isBlank() }
            ?: previous?.bssid?.takeUnless { it == "02:00:00:00:00:00" || it.isBlank() })
        val primaryBssid = primary.bssid?.takeUnless { it == "02:00:00:00:00:00" || it.isBlank() }
        val matchesBssid = bssid != null && primaryBssid != null && bssid == primaryBssid
        // After a permission change, transport info may be redacted. Match the IPv4
        // address before using the legacy primary info so it cannot label another link.
        @Suppress("DEPRECATION")
        val primaryIp = primary.ipAddress
        val matchesIp = if (bssid != null && primaryBssid != null) false else {
            link?.linkAddresses?.any { address ->
                val bytes = address.address.address
                address.address is Inet4Address && primaryIp != 0 &&
                    bytes.indices.all { index -> ((primaryIp ushr (index * 8)) and 255) == (bytes[index].toInt() and 255) }
            } == true
        }
        return if (matchesBssid || matchesIp) primary else perNetwork
    }

    private fun mapWifiInfo(info: WifiInfo?, link: LinkProperties?, validated: Boolean = false): NetworkStatus {
        if (info == null) return NetworkStatus.Disconnected

        val ssidRaw = info.ssid?.trim('"')
        val hasValidSsid = ssidRaw != null && ssidRaw != "<unknown ssid>" && ssidRaw.isNotBlank()

        val ipv4 = link?.linkAddresses
            ?.firstOrNull { it.address is Inet4Address }
            ?.address
            ?.hostAddress
            .orEmpty()

        return NetworkStatus(
            ssid = if (hasValidSsid) ssidRaw!! else "Your Wi-Fi",
            bssid = info.bssid ?: "",
            frequency = info.frequency,
            channel = frequencyToChannel(info.frequency),
            rssi = info.rssi,
            linkSpeed = info.linkSpeed,
            ipAddress = ipv4,
            isConnected = true,
            security = securityType(info),
            internetValidated = validated,
            gateway = link?.routes
                ?.firstOrNull { it.isDefaultRoute && it.gateway is Inet4Address }
                ?.gateway?.hostAddress.orEmpty(),
            // Per-direction rates can be unknown (-1); fall back to the shared link rate.
            txLinkSpeed = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.txLinkSpeedMbps else 0)
                .takeIf { it > 0 } ?: info.linkSpeed.coerceAtLeast(0),
            rxLinkSpeed = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.rxLinkSpeedMbps else 0)
                .takeIf { it > 0 } ?: info.linkSpeed.coerceAtLeast(0)
        )
    }

    private fun securityType(info: WifiInfo): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return ""
        return when (info.currentSecurityType) {
            WifiInfo.SECURITY_TYPE_OPEN -> "Open"
            WifiInfo.SECURITY_TYPE_WEP -> "WEP"
            WifiInfo.SECURITY_TYPE_PSK -> "WPA2"
            WifiInfo.SECURITY_TYPE_SAE -> "WPA3"
            WifiInfo.SECURITY_TYPE_EAP -> "Enterprise"
            WifiInfo.SECURITY_TYPE_EAP_WPA3_ENTERPRISE -> "WPA3-Enterprise"
            WifiInfo.SECURITY_TYPE_OWE -> "OWE"
            else -> ""
        }
    }

    private fun calculateHealth(rssi: Int): Int {
        return when {
            rssi >= -50 -> 100
            rssi <= -100 -> 0
            else -> ((rssi + 100) * 2).coerceIn(0, 100)
        }
    }
}

/** Keeps capabilities and addresses attached to the same connection, independent of Android. */
internal class WifiConnectionTracker<K : Any, I : Any, L : Any> {
    data class Entry<I, L>(val info: I? = null, val link: L? = null, val validated: Boolean = false)
    data class Selection<K>(val network: K?, val revision: Long)
    private val entries = linkedMapOf<K, Entry<I, L>>()
    private var selected: K? = null
    private var revision = 0L

    @Synchronized fun available(network: K) {
        entries.putIfAbsent(network, Entry())
        selected = network
        revision++
    }

    @Synchronized fun capabilities(network: K, info: I?, validated: Boolean) {
        val entry = entries[network] ?: return
        entries[network] = entry.copy(info = info, validated = validated)
        revision++
    }

    @Synchronized fun link(network: K, link: L) {
        val entry = entries[network] ?: return
        entries[network] = entry.copy(link = link)
        revision++
    }

    @Synchronized fun lost(network: K) {
        entries.remove(network)
        if (selected == network) selected = entries.keys.lastOrNull()
        revision++
    }

    @Synchronized fun selection(): Selection<K> = Selection(selected, revision)
    @Synchronized fun current(): Pair<K, Entry<I, L>>? = selected?.let { key -> entries[key]?.let { key to it } }

    @Synchronized fun refresh(
        expected: Selection<K>, network: K?, info: I?, link: L?, validated: Boolean
    ): Boolean {
        if (expected != selection()) return false
        if (network == null) {
            entries.clear()
            selected = null
        } else {
            selected?.takeIf { it != network }?.let { entries.remove(it) }
            selected = network
            entries[network] = Entry(info, link, validated)
        }
        revision++
        return true
    }
}
