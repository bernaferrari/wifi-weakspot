package com.bernaferrari.wifiweakspot.core.i18n.en

import com.bernaferrari.wifiweakspot.core.i18n.DomainStrings
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind
import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel

object EnDomainStrings : DomainStrings {

    override val signalDisplayBars = "Bars"
    override val qualityExcellentLabel = "Excellent"
    override val qualityExcellentHeadline = "Excellent signal"
    override val qualityExcellentExplanation = "A strong connection to the router. Signal alone doesn’t measure internet speed."
    override val qualityGoodLabel = "Good"
    override val qualityGoodHeadline = "Good signal"
    override val qualityGoodExplanation = "A solid signal from the router."
    override val qualityFairLabel = "Okay"
    override val qualityFairHeadline = "Okay signal"
    override val qualityFairExplanation = "A usable signal, with less room for interference."
    override val qualityWeakLabel = "Weak"
    override val qualityWeakHeadline = "Weak spot"
    override val qualityWeakExplanation = "A weak signal. Compare this spot with one closer to the router."
    override val qualityDeadZoneLabel = "Dead zone"
    override val qualityDeadZoneExplanation = "Very little signal reaches this spot."

    override val deviceKindRouter = "Router"
    override val deviceKindThisDevice = "This device"
    override val deviceKindPhone = "Phone or tablet"
    override val deviceKindComputer = "Computer"
    override val deviceKindTv = "TV or streamer"
    override val deviceKindSpeaker = "Speaker"
    override val deviceKindPrinter = "Printer"
    override val deviceKindConsole = "Game console"
    override val deviceKindCamera = "Camera"
    override val deviceKindSmartHome = "Smart home device"
    override val deviceKindUnknown = "Device"
    override val unnamedDevice = "Unnamed device"

    override fun makerDevice(maker: String, kind: DeviceKind): String = when (kind) {
        DeviceKind.Unknown -> "$maker device"
        else -> "$maker ${kindLabel(kind).lowercase()}"
    }

    private fun kindLabel(kind: DeviceKind): String = when (kind) {
        DeviceKind.Router -> deviceKindRouter
        DeviceKind.ThisPhone -> deviceKindThisDevice
        DeviceKind.Phone -> deviceKindPhone
        DeviceKind.Computer -> deviceKindComputer
        DeviceKind.Tv -> deviceKindTv
        DeviceKind.Speaker -> deviceKindSpeaker
        DeviceKind.Printer -> deviceKindPrinter
        DeviceKind.Console -> deviceKindConsole
        DeviceKind.Camera -> deviceKindCamera
        DeviceKind.SmartHome -> deviceKindSmartHome
        DeviceKind.Unknown -> deviceKindUnknown
    }

    override val verdictGreatHeadline = "Router responds quickly"
    override val verdictGreatExplanation = "Replies were quick and steady."
    override val verdictGoodHeadline = "Router response is usable"
    override val verdictGoodExplanation = "Some replies were a little slow or uneven."
    override val verdictSlowHeadline = "Router replies are delayed"
    override val verdictSlowExplanation = "Replies were slow or uneven. Try again near the router to compare."
    override val verdictUnreliableHeadline = "Router check inconclusive"
    override val verdictUnreliableExplanation = "Many replies went missing. Some routers ignore these checks, so try again before changing anything."

    override val roomSuggestions = listOf(
        "Living room", "Kitchen", "Bedroom", "Office", "Kids' room", "Bathroom",
        "Dining room", "Hallway", "Garage", "Basement", "Balcony", "Garden"
    )

    override val coverageNoRooms = "No rooms checked yet"
    override fun coverageWeakInRoom(room: String) = "Weak Wi-Fi in $room"
    override fun coverageWeakInRooms(count: Int) = "Weak Wi-Fi in $count rooms"
    override fun coverageRoomsCheckedNoneWeak(count: Int) =
        if (count == 1) "1 room checked, none weak" else "$count rooms checked, none weak"

    override fun tipHighBandWeak(room: String, band: String) =
        "In $room your device used $band, which carries less far through walls. If your router can split bands, 2.4 GHz may hold on better there."
    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) =
        "Signal drops $dropDb dB from $bestRoom to $worstRoom. Try a more central, raised router position, then recheck the same spots to see whether it helps."
    override fun tipMultipleAccessPoints(count: Int) =
        "Your device used $count access points across these rooms, which may reflect different bands or access points. This alone does not confirm reliable roaming."
    override val tipRouterPlacement = "Keep the router in the open, off the floor, and away from microwaves, mirrors, and fish tanks."

    override val spotFirstReading = "First reading · recheck after making a change"
    override val spotDifferentNetwork = "Different network or spot · start a new comparison"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "Signal $deltaDb dB stronger"
            deltaDb <= -4 -> "Signal ${-deltaDb} dB weaker"
            else -> "Signal about the same"
        }
        val changed = buildList {
            if (accessPointChanged) add("access point changed")
            if (bandChanged) add("band changed")
        }
        return signal + if (changed.isEmpty()) " since last check" else " · ${changed.joinToString("; ")}"
    }

    override val monitorChannelName = "Background Monitoring"
    override val monitorChannelDescription = "Shows when WiFi\u00A0Weakspot is monitoring WiFi networks"
    override val alertsChannelName = "Network Alerts"
    override val alertsChannelDescription = "Alerts for new networks and signal changes"
    override val monitorStartFailed = "Monitoring could not start. Allow Wi-Fi scans and enable Location services, then try again."
    override val monitorWaitingForScanLimited = "Waiting for a fresh Wi-Fi scan. Android may limit scan requests."
    override val monitorWaitingForScan = "Waiting for a fresh Wi-Fi scan"
    override fun monitorStatus(networkCount: Int, lastScanTime: String) =
        "$networkCount ${if (networkCount == 1) "network" else "networks"} · Last scan: $lastScanTime"

    override fun newNetworksTitle(count: Int) =
        if (count == 1) "New WiFi Network Detected" else "New WiFi Networks Detected"

    override fun newNetworksText(names: List<String>): String = when {
        names.size == 1 -> "\"${names.first()}\" is now in range"
        else -> "${names[0]}, ${names[1]}" + if (names.size > 2) " and more" else ""
    }

    override val signalChangeTitle = "Signal Change"
    override fun signalChangeText(networkName: String, level: MonitorSignalLevel, rssi: Int): String {
        val quality = when (level) {
            MonitorSignalLevel.Excellent -> "excellent"
            MonitorSignalLevel.Good -> "good"
            MonitorSignalLevel.Fair -> "fair"
            MonitorSignalLevel.Weak -> "weak"
        }
        return "\"$networkName\" signal is now $quality ($rssi dBm)"
    }

    override val exportFileChooserTitle = "Export File"
    override fun chartDescription(name: String) = "Chart: $name"

    override val lanVpnUnavailable = "Device discovery isn't available through this VPN connection. Check whether your VPN allows local-network access, or try again when it is disconnected."
    override val lanConnectFirst = "Connect to Wi-Fi or Ethernet before scanning local devices."
    override val lanNotConnected = "Not connected"

}
