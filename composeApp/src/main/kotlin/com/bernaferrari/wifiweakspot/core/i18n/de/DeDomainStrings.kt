package com.bernaferrari.wifiweakspot.core.i18n.de

import com.bernaferrari.wifiweakspot.core.i18n.DomainStrings
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind
import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel

object DeDomainStrings : DomainStrings {

    override val signalDisplayBars = "Balken"
    override val qualityExcellentLabel = "Hervorragend"
    override val qualityExcellentHeadline = "Hervorragendes Signal"
    override val qualityExcellentExplanation = "Eine starke Verbindung zum Router. Die Signalstärke allein misst nicht die Internetgeschwindigkeit."
    override val qualityGoodLabel = "Gut"
    override val qualityGoodHeadline = "Gutes Signal"
    override val qualityGoodExplanation = "Ein stabiles Signal vom Router."
    override val qualityFairLabel = "Ausreichend"
    override val qualityFairHeadline = "Ausreichendes Signal"
    override val qualityFairExplanation = "Das Signal ist nutzbar, aber anfälliger für Störungen."
    override val qualityWeakLabel = "Schwach"
    override val qualityWeakHeadline = "Schwacher Empfang"
    override val qualityWeakExplanation = "Ein schwaches Signal. Vergleichen Sie diese Stelle mit einer Stelle, die näher am Router liegt."
    override val qualityDeadZoneLabel = "Funkloch"
    override val qualityDeadZoneExplanation = "An dieser Stelle erreicht nur sehr wenig Signal."

    override val deviceKindRouter = "Router"
    override val deviceKindThisDevice = "Dieses Gerät"
    override val deviceKindPhone = "Handy oder Tablet"
    override val deviceKindComputer = "Computer"
    override val deviceKindTv = "TV oder Streamer"
    override val deviceKindSpeaker = "Lautsprecher"
    override val deviceKindPrinter = "Drucker"
    override val deviceKindConsole = "Spielekonsole"
    override val deviceKindCamera = "Kamera"
    override val deviceKindSmartHome = "Smart-Home-Gerät"
    override val deviceKindUnknown = "Gerät"
    override val unnamedDevice = "Unbenanntes Gerät"

    override fun makerDevice(maker: String, kind: DeviceKind): String = "${maker} ${kindLabel(kind)}"

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

    override val verdictGreatHeadline = "Router reagiert schnell"
    override val verdictGreatExplanation = "Die Antworten waren schnell und zuverlässig."
    override val verdictGoodHeadline = "Router-Antwort ist verwendbar"
    override val verdictGoodExplanation = "Einige Antworten waren etwas langsam oder ungleichmäßig."
    override val verdictSlowHeadline = "Router-Antworten sind verzögert"
    override val verdictSlowExplanation = "Die Antworten waren langsam oder ungleichmäßig. Versuchen Sie es zum Vergleich erneut in der Nähe des Routers."
    override val verdictUnreliableHeadline = "Routerprüfung nicht schlüssig"
    override val verdictUnreliableExplanation = "Viele Antworten gingen verloren. Einige Router ignorieren diese Prüfungen. Versuchen Sie es daher noch einmal, bevor Sie etwas ändern."

    override val roomSuggestions = listOf("Wohnzimmer", "Küche", "Schlafzimmer", "Arbeitszimmer", "Kinderzimmer", "Badezimmer", "Esszimmer", "Flur", "Garage", "Keller", "Balkon", "Garten")

    override val coverageNoRooms = "Noch keine Zimmer überprüft"
    override fun coverageWeakInRoom(room: String) = "Schwach Wi-Fi in ${room}"
    override fun coverageWeakInRooms(count: Int) = "Schwach Wi-Fi in ${count} Räumen"
    override fun coverageRoomsCheckedNoneWeak(count: Int) =
        if (count == 1) "1 Zimmer überprüft, keines schwach" else "${count} Zimmer überprüft, keines schwach"

    override fun tipHighBandWeak(room: String, band: String) =
        "In ${room} verwendete Ihr Gerät ${band}, das weniger weit durch Wände reicht. Wenn Ihr Router Bänder aufteilen kann, hält 2.4 GHz dort möglicherweise besser."
    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) =
        "Signal fällt um ${dropDb} dB von ${bestRoom} auf ${worstRoom}. Versuchen Sie es mit einer zentraleren, erhöhten Position des Routers und überprüfen Sie dann die gleichen Stellen noch einmal, um zu sehen, ob es hilft."
    override fun tipMultipleAccessPoints(count: Int) =
        "Ihr Gerät verwendet ${count} Zugangspunkte in diesen Räumen, was möglicherweise unterschiedliche Bänder oder Zugangspunkte widerspiegelt. Dies allein bestätigt kein zuverlässiges Roaming."
    override val tipRouterPlacement = "Stelle den Router im Innenraum erhöht und frei von Hindernissen auf, mit Abstand zu Mikrowellen, Spiegeln und Aquarien."

    override val spotFirstReading = "Erste Lesung · Nach einer Änderung erneut prüfen"
    override val spotDifferentNetwork = "Anderes Netzwerk oder anderer Spot · Starten Sie einen neuen Vergleich"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "Signal ${deltaDb} dB stärker"
            deltaDb <= -4 -> "Signal ${-deltaDb} dB schwächer"
            else -> "Signal ungefähr gleich"
        }
        val changed = buildList {
            if (accessPointChanged) add("Zugangspunkt geändert")
            if (bandChanged) add("Band geändert")
        }
        return signal + if (changed.isEmpty()) " seit der letzten Überprüfung" else " · ${changed.joinToString("; ")}"
    }

    override val monitorChannelName = "Hintergrundüberwachung"
    override val monitorChannelDescription = "Zeigt an, wenn WiFi Weakspot WiFi Netzwerke überwacht"
    override val alertsChannelName = "Netzwerkwarnungen"
    override val alertsChannelDescription = "Warnungen für neue Netzwerke und Signaländerungen"
    override val monitorStartFailed = "Die Überwachung konnte nicht gestartet werden. Erlauben Sie Wi-Fi Scans und aktivieren Sie die Ortungsdienste. Versuchen Sie es dann erneut."
    override val monitorWaitingForScanLimited = "Warten auf einen neuen Wi-Fi Scan. Android kann Scananfragen einschränken."
    override val monitorWaitingForScan = "Warten auf einen neuen Wi-Fi Scan"
    override fun monitorStatus(networkCount: Int, lastScanTime: String) = "${networkCount} ${if (networkCount == 1) "Netzwerk" else "Netzwerke"} · Letzter Scan: ${lastScanTime}"

    override fun newNetworksTitle(count: Int) =
        if (count == 1) "Neues WiFi Netzwerk erkannt" else "Neue WiFi Netzwerke erkannt"

    override fun newNetworksText(names: List<String>): String = when {
        names.size == 1 -> "„${names.first()}“ ist jetzt in Reichweite"
        else -> "${names[0]}, ${names[1]}" + if (names.size > 2) " und mehr" else ""
    }

    override val signalChangeTitle = "Signalwechsel"
    override fun signalChangeText(networkName: String, level: MonitorSignalLevel, rssi: Int): String {
        val quality = when (level) {
            MonitorSignalLevel.Excellent -> qualityExcellentLabel
            MonitorSignalLevel.Good -> qualityGoodLabel
            MonitorSignalLevel.Fair -> qualityFairLabel
            MonitorSignalLevel.Weak -> qualityWeakLabel
        }
        return "Signal von „$networkName“: $quality ($rssi dBm)"
    }

    override val exportFileChooserTitle = "Datei exportieren"
    override fun chartDescription(name: String) = "Diagramm: ${name}"

    override val lanVpnUnavailable = "Die Geräteerkennung ist über diese VPN-Verbindung nicht verfügbar. Überprüfen Sie, ob Ihr VPN den Zugriff auf das lokale Netzwerk zulässt, oder versuchen Sie es erneut, wenn die Verbindung getrennt ist."
    override val lanConnectFirst = "Stellen Sie eine Verbindung zu Wi-Fi oder Ethernet her, bevor Sie lokale Geräte scannen."
    override val lanNotConnected = "Nicht verbunden"

}
