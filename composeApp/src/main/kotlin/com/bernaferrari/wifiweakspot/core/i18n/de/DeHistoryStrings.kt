package com.bernaferrari.wifiweakspot.core.i18n.de

import com.bernaferrari.wifiweakspot.core.i18n.HistoryStrings

object DeHistoryStrings : HistoryStrings {
    override val today = "Heute"
    override val yesterday = "Gestern"
    override val weekdayNames = listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag")
    override val weekdayShortNames = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")
    override val monthShortNames = listOf("Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez")
    override fun dateThisYear(weekday: String, day: Int, month: String) = "${weekday}, ${day}. ${month}"

    override fun dateOtherYear(weekday: String, day: Int, month: String, year: Int) = "${weekday}, ${day}. ${month} ${year}"

    override fun momentAt(day: String, time: String) = "${day} um ${time}"

    override val filterAll = "Alle"
    override val kindInternetTests = "Internettests"
    override val kindScans = "Scans"
    override val emptyTitle = "Noch nichts gespeichert"
    override val emptySubtitle = "Router-Prüfungen, Netzwerk-Scans und Tool-Ergebnisse werden hier angezeigt, sodass Sie sehen können, wie sich Ihr Wi-Fi im Laufe der Zeit verändert."
    override fun trendBarsDescription(count: Int, readings: String) = "Letzte ${count} Routertests: ${readings}"
    override val trendNoReply = "Keine Antwort vom Router. Einige Router ignorieren diese Prüfungen."
    override val trendRecheckToCompare = "Nach einer Änderung zum Vergleich erneut prüfen."
    override fun trendQuicker(usualMs: String) = "Schneller als gewöhnlich (${usualMs} ms)"
    override fun trendSlower(usualMs: String) = "Langsamer als gewöhnlich (${usualMs} ms)"
    override fun trendAboutSame(usualMs: String) = "Ungefähr das Gleiche wie üblich (${usualMs} ms)"

    override fun internetTitle(mbps: String) = "${mbps} Mbps herunterladen"
    override fun routerTitle(ms: String) = "${ms} ms zum Router"
    override fun networksNearby(count: Int) = if (count == 1) "1 Netzwerk in der Nähe" else "${count} Netzwerke in der Nähe"
    override fun strongestNetwork(network: String) = "Am stärksten: ${network}"
    override fun accessPoints(count: Int) = if (count == 1) "1 Zugangspunkt" else "${count} Zugangspunkte"
    override fun devicesFound(count: Int) = if (count == 1) "1 Gerät gefunden" else "${count} Geräte gefunden"
    override fun listWithMore(list: String, more: Int) = if (more > 0) "${list} und ${more} mehr" else list
    override fun pingSummary(averageMs: String, replied: Int, sent: Int) = "${averageMs} ms · ${replied} von ${sent} Antworten"

    override fun lookupTitle(target: String) = "Suchen Sie nach ${target}"
    override val noRecords = "Keine Datensätze"
    override val lookupFailed = "Suche fehlgeschlagen"
    override fun openPortsTitle(target: String) = "Offene Ports auf ${target}"
    override val noCommonPortsOpen = "Keiner der gemeinsamen Ports war geöffnet"
    override val completed = "Abgeschlossen"
    override val failed = "Fehlgeschlagen"

    override val metricDownload = "Herunterladen"
    override val metricUpload = "Hochladen"
    override val metricLatency = "Latenz"
    override val metricVariation = "Variation"
    override val metricLost = "Verloren"
    override fun metricOnBand(band: String) = "auf ${band}"
    override val metricReplied = "Geantwortet"
    override val metricReplies = "Antworten"
    override val metricAverage = "Durchschnitt"
    override val factServer = "Server"
    override val factRoom = "Raum"
    override val factBand = "Band"
    override val factSignal = "Signal"
    override val factAccessPoints = "Zugangspunkte"
    override fun signalFact(quality: String, rssi: Int) = "${quality} Signal · ${rssi} dBm"

    override val linesNetworksStrongestFirst = "Netzwerke, die Stärksten zuerst"
    override val linesRecords = "Aufzeichnungen"
    override fun replyNumber(index: Int) = "Antwort ${index}"
    override val scanNote = "Nahe gelegene Netzwerke auf demselben Kanal wie Ihres konkurrieren um Sendezeit."

    override val go = "LOS"
    override val testInternetSpeedInstead = "Testen Sie stattdessen die Internetgeschwindigkeit"
    override val keepDeviceStill = "Halten Sie Ihr Gerät einige Sekunden lang ruhig."
    override val connectToStart = "Verbinden Sie sich zum Starten mit Ihrem Wi-Fi."
    override val lastTime = "Letztes Mal"
    override val couldNotSaveSpot = "Diese Stelle konnte nicht gespeichert werden. Versuchen Sie es erneut."
    override val connectFirst = "Verbinden Sie sich zuerst mit Ihrem Wi-Fi. Diese Prüfung misst die Antworten Ihres Routers."
    override val historyNotSaved = "Ergebnis gemessen, aber Verlauf konnte nicht gespeichert werden."
    override val checkStopped = "Die Verbindung hat sich geändert oder die Routerprüfung wurde gestoppt. Lassen Sie diese Wi-Fi verbunden und versuchen Sie es erneut. Einige Router blockieren Ping."

    override val internetSpeedTitle = "Internetgeschwindigkeit · fast.com"
}
