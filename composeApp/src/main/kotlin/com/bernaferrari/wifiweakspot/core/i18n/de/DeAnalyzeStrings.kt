package com.bernaferrari.wifiweakspot.core.i18n.de

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object DeAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = if (count == 1) "1 Netzwerk" else "${count} Netzwerke"

    override fun channelShort(channel: Int) = "K. ${channel}"

    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · ${channelShort(channel)} · ${band}"

    override val noPassword = "Ohne Passwort"
    override val wifiOffTitle = "Wi-Fi ist ausgeschaltet"
    override val locationOffBody = "Android benötigt aktivierte Standortdienste, um WLANs in der Nähe aufzulisten. WiFi Weakspot verwendet kein GPS."

    override val scanPermissionBody = "Android teilt nur Netzwerknamen in der Nähe mit Apps, die über Standortzugriff verfügen. WiFi Weakspot zeichnet niemals auf, wo Sie sich befinden."
    override val wifiOffBody = "Schalten Sie Wi-Fi ein, um Netzwerke in der Nähe anzuzeigen."
    override val locationOffTitle = "Standort ist deaktiviert"
    override val turnOnLocation = "Aktivieren Sie den Standort"
    override val scanPausedTitle = "Android hat den Scanvorgang angehalten"
    override fun scanPausedBody(seconds: Long) = "Der nächste Scan ist in ${seconds} s zulässig. Deaktivieren Sie die Scan-Drosselung, um die Wartezeit zu überspringen."
    override val turnOffThrottling = "Schalten Sie die Drosselung aus"
    override val readyToScanBody = "Tippen Sie auf „Scannen“, um alle Netzwerke in der Nähe anzuzeigen."
    override val scan = "Scannen"
    override val channels = "Kanäle"
    override val signalOverTime = "Signalverlauf"
    override val howToReadThisChart = "So lesen Sie dieses Diagramm"
    override val expandSignalHistory = "Signalverlauf erweitern"
    override val expandChannels = "Kanäle erweitern"
    override fun noBandNetworksNearby(band: String) = "Keine ${band} Netzwerke in der Nähe."
    override fun networksOnBand(count: Int, band: String) =
        if (count == 0) "Keine Netzwerke auf ${band}" else "${networks(count)} auf ${band}"
    override fun showVeryWeakNetworks(count: Int) =
        if (count == 1) "1 sehr schwaches Netzwerk anzeigen" else "Zeige ${count} sehr schwache Netzwerke"
    override fun hideVeryWeakNetworks(count: Int) =
        if (count == 1) "1 sehr schwaches Netzwerk ausblenden" else "${count} sehr schwache Netzwerke ausblenden"
    override fun bandNetworkCount(count: Int) = if (count == 0) "keine" else networks(count)
    override val highlightedOnChart = "Im Diagramm hervorgehoben"
    override val notHighlightedOnChart = "Im Diagramm nicht hervorgehoben"
    override fun detailsForNetwork(ssid: String) = "Details für ${ssid.ifEmpty { "verstecktes Netzwerk" }}"
    override fun alsoYourRouter(quality: String) = "${quality} · Auch Ihr Router"
    override val connected = "Verbunden"

    override val scanReportHeading = "WiFi Weakspot Scanbericht"
    override fun scanReportTime(time: String) = "Zeit: ${time}"
    override fun scanReportTotal(count: Int) = "Netzwerke insgesamt: ${count}"
    override val scanReportNetworksHeading = "Netzwerke:"
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) = "• ${ssid} - ${rssi} dBm, ${channelShort(channel)}, ${security}"

    override val channelLooksClear = "Ihr Kanal ist wenig belegt"

    override val noChannelImprovement = "Kein besserer Kanal gefunden"
    override fun lessOverlapThanChannel(currentChannel: Int) = "Weniger belegt als Kanal ${currentChannel}"
    override val suggestedChannel = "Empfohlener Kanal"
    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}: Kanal ${channel}"
    override val lowOverlap = "Geringe Überlappung"
    override val moderateOverlap = "Mäßige Überlappung"
    override val highOverlap = "Hohe Überlappung"
    override val veryHighOverlap = "Sehr hohe Überlappung"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) =
        "Kanaldiagramm, ${networks(networkCount)}." +
            (if (busiestChannel != null) " Belebtester Kanal ${busiestChannel} mit ${busiestCount}." else "")
    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) = name + (if (isYou) " (${connected})" else "") + (if (extraNames > 0) " +${extraNames}" else "")

    override val busyCaption = "Belegt"
    override val axisStrong = "Stark"
    override val axisOkay = "Mittel"
    override val axisWeak = "Schwach"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · ${channelShort(channel)}"

    override val clearSelectedNetwork = "Ausgewähltes Netzwerk löschen"
    override val tapWaveForDetails = "Tippen Sie auf eine Welle, um Netzwerkdetails anzuzeigen"

    override val chartGuideTitle = "Lesen der Charts"
    override val guideTallerBody = "Die Höhe ist die Signalstärke."
    override val guideOverlapBody = "Überlappende Netzwerke teilen sich das Spektrum und können um Sendezeit konkurrieren."
    override val guideWiderBody = "Ein breiterer Kanal belegt mehr Spektrum und kann mehr Nachbarn überlappen."
    override val guideSameRouterBody = "+1: Netzwerknamen, die offenbar dasselbe WLAN-Funkmodul nutzen, teilen sich eine Kurve."

    override val guideCrowdBody = "Grün bedeutet weniger Überlappung, Rot mehr. Der Streifen unter 2.4 GHz zeigt Überlappung. Tippen Sie darauf, um zu zählen."
    override val guideSteadyBody = "Eine stark schwankende Linie zeigt Änderungen der Signalstärke. Tippen Sie zum Pausieren auf eine Linie. Wischen Sie horizontal durch die Zeit oder vertikal zwischen Netzwerken. Setzen Sie die Live-Anzeige für neue Messwerte fort."

    override val guideHighlightBody = "Tippen Sie auf einen Hügel oder eine Reihe, um ihn hervorzuheben. Ziehen Sie zum Erkunden horizontal über das Diagramm."

    override fun channelOverlapHeading(count: Int, channel: Int) = when (count) {
        0 -> "Kanal ${channel} ist frei"
        1 -> "1 Netzwerk überlappt Kanal ${channel}"
        else -> "${count} Netzwerke überlappen Kanal ${channel}"
    }
    override fun fromChannel(homeChannel: Int, reading: String) = "Von Kanal ${homeChannel} · ${reading}"

    override val estimatedChannelOverlap = "Geschätzte Kanalüberlappung"
    override fun tryChannel(channel: Int) = "Probieren Sie Kanal ${channel} aus"
    override val lowestOverlapFor20Mhz = "Geringste geschätzte Überlappung für 20 MHz Kanäle"
    override val overlapDisclaimer = "Die Schätzung basiert auf Zugangspunkten in der Nähe und der Signalstärke, nicht auf gemessenem Datenverkehr. Prüfen Sie vor Änderungen die unterstützten Kanäle Ihres Routers und die örtlichen Vorschriften. Kanäle im 5- und 6-GHz-Band werden nur vorgeschlagen, wenn die Routerunterstützung bekannt ist. Fehlt die Mittenfrequenz eines breiten Kanals, wird nur sein primärer 20-MHz-Kanal angezeigt."

    override val overlapScore = "Überlappungspunktzahl"

    override val collectingReadings = "Messwerte werden gesammelt… Nach einigen Scans erscheinen die Linien."

    override fun signalHistoryDescription(networkCount: Int) =
        "Signalverlauf für ${networks(networkCount)}. Tippen Sie auf eine Leitung, um sie zu überprüfen. Mit den Pfeiltasten bewegen Sie sich durch Zeit und Netzwerke."
    override val historyLive = "Live"
    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) = "${networkName}, ${reading ?: "kein Messwert"}, pausiert, ${readingsAgo(readingsAgo)}"

    override val strongerNetwork = "Stärkeres Netzwerk"
    override val weakerNetwork = "Schwächeres Netzwerk"
    override val axisOlder = "älter"
    override val axisLatest = "aktuell"
    override val exploreSignalHistory = "Entdecken Sie den Signalverlauf"
    override val tapLineToCompare = "Tippen Sie auf eine Linie und ziehen Sie sie zum Vergleichen"
    override val noReadingAtPoint = "Für diesen Zeitpunkt liegt kein Messwert vor"

    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) = "${reading} · ${quality} · " + if (readingsAgo == 0) "neuester Messwert" else readingsAgo(readingsAgo)

    private fun readingsAgo(count: Int) = if (count == 1) "vor 1 Messung" else "vor $count Messungen"

    override val resumeLive = "Live fortsetzen"

    override fun seenReading(rssi: Int, seen: String) = "${rssi} dBm · gesehen ${seen}"
    override val factChannel = "Kanal"
    override val factWidth = "Breite"
    override val factSecurity = "Sicherheit"
    override val factDistance = "Entfernungsschätzung"
    override val notAvailable = "Nicht verfügbar"
    override val watch = "Anschauen"
    override val addToFavorites = "Zu Favoriten hinzufügen"
    override val removeFromFavorites = "Aus Favoriten entfernen"

    override fun routerCheckLastTime(pingMs: String) = "${pingMs} ms letztes Mal"
    override fun devicesFoundLastScan(count: Int) = "${count} letzten Scan gefunden"
    override val devicesSubtitle = "Wer ist verbunden?"
    override val testWebsiteSubtitle = "Ping, DNS, Ports"
    override fun savedChecks(count: Int) = when (count) {
        0 -> "Vergangene Ergebnisse"
        1 -> "1 Scheck"
        else -> "${count} prüft"
    }
    override val notOnWifi = "Nicht auf Wi-Fi"
    override val online = "Online"
    override val internetNotConfirmed = "Internet nicht bestätigt"
    override fun channelChip(channel: Int) = "Kanal ${channel}"
    override fun linkRateChip(mbps: Int) = "Verbindungsrate ${mbps} Mbps"
    override val chooseNetwork = "Wählen Sie ein Netzwerk"
    override val turnOnWifi = "Wi-Fi einschalten"
    override val dismiss = "Ablehnen"
    override val findWeakSpotsTitle = "Finden Sie Schwachstellen"
    override val findWeakSpotsBody = "Finden Sie Räume mit schwachem Signal."

    override val findWeakSpotsAction = "Überprüfen Sie Ihre Räume"
    override fun networksNearby(count: Int) = "${networks(count)} in der Nähe"
    override val nearbyStale = "Von Ihrem letzten Scan. Zum Aktualisieren tippen."
    override val nearbyNoConnection = "Sehen Sie sich Kanäle und Signale für jedes Netzwerk an."
    override fun nearbyOverlap(count: Int, channel: Int) = when (count) {
        0 -> "Keine Überlappung in der Nähe auf Kanal ${channel} erkannt."
        1 -> "1 Signal in der Nähe überlappt Ihren Kanal ${channel}."
        else -> "${count} Signale in der Nähe überlappen Ihren Kanal ${channel}."
    }

    override val adviceNoWifiBody = "Stellen Sie eine Verbindung zu dem Netzwerk her, das Sie überprüfen möchten. Sie können weiterhin Internettests und Netzwerktools über Ihre aktuelle Verbindung ausführen."
    override val adviceNoInternetTitle = "Überprüfen Sie zunächst den Internetzugang"
    override val adviceNoInternetBody = "Android hat den Internetzugang für diese Wi-Fi-Verbindung nicht bestätigt. Suchen Sie nach einer Anmeldeseite und testen Sie dann die Verbindung zu Ihrem Router und einem Internethost."
    override val adviceNoSignalTitle = "Lesen Sie Ihr Wi-Fi-Signal"
    override val adviceNoSignalBody = "Erlauben Sie Wi-Fi Scans in Netzwerken, um Signalmessungen anzuzeigen. Vergleichen Sie das Signal in dem Raum, in dem Sie Probleme bemerken."
    override val adviceWeakSignalTitle = "Vergleichen Sie die Abdeckung in der Nähe Ihres Routers"
    override val adviceWeakSignalBody = "Das WLAN-Signal ist schwach. Prüfen Sie die Routerantwort hier und erneut in der Nähe des Routers. Vergleichen Sie Messungen Ihres eigenen Netzwerks, um schwache Stellen zu finden."

    override val adviceGoodSignalTitle = "Vergleichen Sie die Reaktionsfähigkeit des Routers"
    override val adviceGoodSignalBody = "Die Signalstärke sieht brauchbar aus und Android meldet Internetzugang. Wenn sich die Verbindung langsam anfühlt, vergleichen Sie die Reaktion des Routers hier und in der Nähe des Routers. Ein starkes Signal allein garantiert keine Leistung."
    override val channelAdviceBasis = "Kürzere Balken bedeuten weniger Überlappung in diesem Scan. Die Geschwindigkeit kann variieren."
    override fun channelChangeInstructions(channel: Int, band: String) = "Wählen Sie Kanal ${channel} für ${band}"

    override val channelOverlapTitle = "Überlappung mit benachbarten WLANs"
    override val channelApplyChange = "Speichern und bei Bedarf erneut verbinden"
    override val channelChangeNote = "Ändern Sie dies in der Router-App oder den Router-Einstellungen, nicht in WiFi Weakspot."

}
