package com.bernaferrari.wifiweakspot.core.i18n.de

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object DeAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = if (count == 1) "1 Netzwerk" else "${count} Netzwerke"

    override fun channelShort(channel: Int) = "Ch ${channel}"
    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · Ch ${channel} · ${band}"
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
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) =
        "• ${ssid} - ${rssi}dBm, Ch ${channel}, ${security}"
    override val channelLooksClear = "Dein Kanal sieht klar aus"
    override val noChannelImprovement = "Bei diesem Scan wurde keine Verbesserung festgestellt"
    override fun lessOverlapThanChannel(currentChannel: Int) = "Weniger Überlappung als Kanal ${currentChannel}"
    override val leastOverlapInScan = "Geringste Überlappung in diesem Scan"
    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}: Kanal ${channel}"
    override val lowOverlap = "Geringe Überlappung"
    override val moderateOverlap = "Mäßige Überlappung"
    override val highOverlap = "Hohe Überlappung"
    override val veryHighOverlap = "Sehr hohe Überlappung"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) =
        "Kanaldiagramm, ${networks(networkCount)}." +
            (if (busiestChannel != null) " Belebtester Kanal ${busiestChannel} mit ${busiestCount}." else "")
    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) =
        name + (if (isYou) " (Sie)" else "") + (if (extraNames > 0) " +${extraNames}" else "")
    override val busyCaption = "Belegt"
    override val axisStrong = "Stark"
    override val axisOkay = "Mittel"
    override val axisWeak = "Schwach"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · Ch ${channel}"
    override val clearSelectedNetwork = "Ausgewähltes Netzwerk löschen"
    override val tapWaveForDetails = "Tippen Sie auf eine Welle, um Netzwerkdetails anzuzeigen"

    override val chartGuideTitle = "Lesen der Charts"
    override val guideTallerBody = "Die Höhe ist die Signalstärke."
    override val guideOverlapBody = "Überlappende Netzwerke teilen sich das Spektrum und können um Sendezeit konkurrieren."
    override val guideWiderBody = "Ein breiterer Kanal belegt mehr Spektrum und kann mehr Nachbarn überlappen."
    override val guideSameRouterBody = "+1: Namen, die scheinbar aus demselben Radio stammen, teilen sich einen Hügel."
    override val guideCrowdBody = "Grün bedeutet weniger Überlappung, Rot mehr. Der Streifen unter 2.4 GHz zeigt Überlappung. Tippen Sie darauf, um zu zählen."
    override val guideSteadyBody = "Eine sprunghafte Linie zeigt die sich ändernde Signalstärke an. Tippen Sie auf eine Zeile, um anzuhalten, und gleiten Sie dann seitwärts durch die Zeit oder vertikal zwischen den Netzwerken. Gehen Sie live weiter, um neue Lesungen anzusehen."
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
    override val overlapDisclaimer = "Basierend auf nahegelegenen Zugangspunkten und Signalstärke, nicht gemessenem Verkehr. " +
        "Überprüfen Sie die von Ihrem Router unterstützten Kanäle und lokalen Regeln, bevor Sie Einstellungen ändern. " +
        "5 und 6 GHz Vorschläge erfordern bekannte Router-Unterstützung; fehlende Breitkanalzentren " +
        "zeigt nur die primären 20 MHz Kanäle an."
    override val overlapScore = "Überlappungspunktzahl"

    override val collectingReadings = "Messwerte sammeln… Die Linien erscheinen nach ein paar Scans."
    override fun signalHistoryDescription(networkCount: Int) =
        "Signalverlauf für ${networks(networkCount)}. Tippen Sie auf eine Leitung, um sie zu überprüfen. Mit den Pfeiltasten bewegen Sie sich durch Zeit und Netzwerke."
    override val historyLive = "Live"
    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) =
        "${networkName}, ${reading ?: "keine Lesung"}, pausiert, ${readingsAgo(readingsAgo)}"
    override val strongerNetwork = "Stärkeres Netzwerk"
    override val weakerNetwork = "Schwächeres Netzwerk"
    override val axisOlder = "älter"
    override val axisLatest = "aktuell"
    override val exploreSignalHistory = "Entdecken Sie den Signalverlauf"
    override val tapLineToCompare = "Tippen Sie auf eine Linie und ziehen Sie sie zum Vergleichen"
    override val noReadingAtPoint = "Derzeit keine Lektüre"
    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) =
        "${reading} · ${quality} · " + if (readingsAgo == 0) "neueste Lesung" else readingsAgo(readingsAgo)
    private fun readingsAgo(count: Int) = if (count == 1) "Vor 1 Lesung" else "${count} Lesungen"
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
    override val findWeakSpotsBody = "Sehen Sie, welche Räume schwach sind."
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
    override val adviceWeakSignalBody = "Das aktuelle Wi-Fi-Signal ist schwach. Führen Sie hier eine Routerprüfung durch und wiederholen Sie den Vorgang dann in der Nähe des Routers. Nutzen Sie eine Umfrage in Ihrem eigenen Netzwerk, um Schwachstellen zu finden."
    override val adviceGoodSignalTitle = "Vergleichen Sie die Reaktionsfähigkeit des Routers"
    override val adviceGoodSignalBody = "Die Signalstärke sieht brauchbar aus und Android meldet Internetzugang. Wenn sich die Verbindung langsam anfühlt, vergleichen Sie die Reaktion des Routers hier und in der Nähe des Routers. Ein starkes Signal allein garantiert keine Leistung."
}
