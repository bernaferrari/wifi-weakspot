package com.bernaferrari.wifiweakspot.core.i18n.it

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object ItAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = if (count == 1) "1 rete" else "${count} reti"

    override fun channelShort(channel: Int) = "Can. ${channel}"

    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · ${channelShort(channel)} · ${band}"

    override val noPassword = "Senza password"
    override val wifiOffTitle = "Wi-Fi è spento"
    override val locationOffBody = "Android richiede la posizione attiva per elencare le reti Wi-Fi vicine. WiFi Weakspot non usa il GPS."

    override val scanPermissionBody = "Android condivide solo i nomi di rete nelle vicinanze con le app che hanno accesso alla posizione. WiFi Weakspot non registra mai dove ti trovi."
    override val wifiOffBody = "Attiva Wi-Fi per vedere le reti vicine."
    override val locationOffTitle = "La posizione è disattivata"
    override val turnOnLocation = "Attiva Posizione"
    override val scanPausedTitle = "Android scansione in pausa"
    override fun scanPausedBody(seconds: Long) = "La scansione successiva è consentita tra ${seconds} s. Disattiva la limitazione della scansione per saltare l'attesa."
    override val turnOffThrottling = "Disattiva la limitazione"
    override val readyToScanBody = "Tocca Scansione per vedere tutte le reti nelle vicinanze."
    override val scan = "Scansiona"
    override val channels = "Canali"
    override val signalOverTime = "Segnale nel tempo"
    override val howToReadThisChart = "Come leggere questo grafico"
    override val expandSignalHistory = "Espandi la cronologia dei segnali"
    override val expandChannels = "Espandi canali"
    override fun noBandNetworksNearby(band: String) = "Nessuna rete ${band} nelle vicinanze."
    override fun networksOnBand(count: Int, band: String) =
        if (count == 0) "Nessuna rete su ${band}" else "${networks(count)} su ${band}"
    override fun showVeryWeakNetworks(count: Int) =
        if (count == 1) "Mostra 1 rete molto debole" else "Mostra ${count} reti molto deboli"
    override fun hideVeryWeakNetworks(count: Int) =
        if (count == 1) "Nascondi 1 rete molto debole" else "Nascondi ${count} reti molto deboli"
    override fun bandNetworkCount(count: Int) = if (count == 0) "nessuno" else networks(count)
    override val highlightedOnChart = "Evidenziato sul grafico"
    override val notHighlightedOnChart = "Non evidenziato sul grafico"
    override fun detailsForNetwork(ssid: String) = "Dettagli per ${ssid.ifEmpty { "rete nascosta" }}"
    override fun alsoYourRouter(quality: String) = "${quality} · Anche il tuo router"
    override val connected = "Connesso"

    override val scanReportHeading = "WiFi Weakspot Rapporto scansione"
    override fun scanReportTime(time: String) = "Ora: ${time}"
    override fun scanReportTotal(count: Int) = "Reti totali: ${count}"
    override val scanReportNetworksHeading = "Reti:"
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) = "• ${ssid} - ${rssi} dBm, ${channelShort(channel)}, ${security}"

    override val channelLooksClear = "Il tuo canale è poco affollato"
    override val noChannelImprovement = "Nessun canale migliore trovato"
    override fun lessOverlapThanChannel(currentChannel: Int) = "Meno affollato del canale ${currentChannel}"
    override val suggestedChannel = "Canale consigliato"
    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}: canale ${channel}"
    override val lowOverlap = "Bassa sovrapposizione"
    override val moderateOverlap = "Sovrapposizione moderata"
    override val highOverlap = "Elevata sovrapposizione"
    override val veryHighOverlap = "Sovrapposizione molto elevata"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) =
        "Grafico del canale, ${networks(networkCount)}." +
            (if (busiestChannel != null) " Canale più occupato ${busiestChannel} con ${busiestCount}." else "")
    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) = name + (if (isYou) " (${connected})" else "") + (if (extraNames > 0) " +${extraNames}" else "")

    override val busyCaption = "Affollato"
    override val axisStrong = "Forte"
    override val axisOkay = "Medio"

    override val axisWeak = "Debole"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · ${channelShort(channel)}"

    override val clearSelectedNetwork = "Cancella la rete selezionata"
    override val tapWaveForDetails = "Tocca un'onda per i dettagli della rete"

    override val chartGuideTitle = "Lettura delle classifiche"
    override val guideTallerBody = "L'altezza indica la potenza del segnale."
    override val guideOverlapBody = "Le reti sovrapposte condividono lo spettro e possono competere per il tempo di trasmissione."
    override val guideWiderBody = "Un canale più ampio occupa più spettro e può sovrapporsi a più vicini."
    override val guideSameRouterBody = "+1: I nomi di rete che sembrano usare lo stesso modulo Wi-Fi condividono una curva."

    override val guideCrowdBody = "Il verde indica meno sovrapposizioni, il rosso di più. La striscia sotto 2.4 GHz mostra una sovrapposizione. Toccalo per i conteggi."
    override val guideSteadyBody = "Una linea che oscilla indica variazioni del segnale. Toccala per mettere in pausa, poi trascina orizzontalmente per cambiare istante o verticalmente per cambiare rete. Riprendi la visualizzazione live per vedere nuove misurazioni."

    override val guideHighlightBody = "Tocca una collina o una riga per evidenziarla. Trascina orizzontalmente sul grafico per esplorare."

    override fun channelOverlapHeading(count: Int, channel: Int) = when (count) {
        0 -> "Il canale ${channel} è libero"
        1 -> "1 rete si sovrappone al canale ${channel}"
        else -> "${count} le reti si sovrappongono al canale ${channel}"
    }
    override fun fromChannel(homeChannel: Int, reading: String) = "Dal canale ${homeChannel} · ${reading}"

    override val estimatedChannelOverlap = "Sovrapposizione stimata dei canali"
    override fun tryChannel(channel: Int) = "Prova il canale ${channel}"
    override val lowestOverlapFor20Mhz = "Sovrapposizione stimata più bassa per 20 MHz canali"
    override val overlapDisclaimer = "La stima si basa sui punti di accesso vicini e sulla potenza del segnale, non sul traffico misurato. Prima di cambiare le impostazioni, verifica i canali supportati dal router e le norme locali. I canali a 5 e 6 GHz vengono suggeriti solo quando il supporto del router è noto. Se manca la frequenza centrale di un canale largo, viene mostrato solo il canale primario da 20 MHz."

    override val overlapScore = "Punteggio di sovrapposizione"

    override val collectingReadings = "Raccolta delle misurazioni… Le linee appariranno dopo alcune scansioni."

    override fun signalHistoryDescription(networkCount: Int) =
        "Cronologia del segnale per ${networks(networkCount)}. Tocca una linea per ispezionarla. I tasti freccia si muovono attraverso il tempo e le reti."
    override val historyLive = "In diretta"
    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) = "${networkName}, ${reading ?: "nessuna misurazione"}, in pausa, ${readingsAgo(readingsAgo)}"

    override val strongerNetwork = "Rete più forte"
    override val weakerNetwork = "Rete più debole"
    override val axisOlder = "precedente"
    override val axisLatest = "recente"
    override val exploreSignalHistory = "Esplora la cronologia dei segnali"
    override val tapLineToCompare = "Tocca una linea, quindi trascina per confrontare"
    override val noReadingAtPoint = "Nessuna misurazione in questo punto"

    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) = "${reading} · ${quality} · " + if (readingsAgo == 0) "ultima misurazione" else readingsAgo(readingsAgo)

    private fun readingsAgo(count: Int) = if (count == 1) "1 misurazione fa" else "$count misurazioni fa"

    override val resumeLive = "Riprendi la diretta"

    override fun seenReading(rssi: Int, seen: String) = "${rssi} dBm · visto ${seen}"
    override val factChannel = "Canale"
    override val factWidth = "Larghezza"
    override val factSecurity = "Sicurezza"
    override val factDistance = "Stima della distanza"
    override val notAvailable = "Non disponibile"
    override val watch = "Guarda"
    override val addToFavorites = "Aggiungi ai preferiti"
    override val removeFromFavorites = "Rimuovi dai preferiti"

    override fun routerCheckLastTime(pingMs: String) = "${pingMs} ms l'ultima volta"
    override fun devicesFoundLastScan(count: Int) = "${count} trovata l'ultima scansione"
    override val devicesSubtitle = "Chi è connesso"
    override val testWebsiteSubtitle = "Ping, DNS, porte"
    override fun savedChecks(count: Int) = when (count) {
        0 -> "Risultati passati"
        1 -> "1 assegno"
        else -> "${count} assegni"
    }
    override val notOnWifi = "Non attivo Wi-Fi"
    override val online = "In linea"
    override val internetNotConfirmed = "Internet non confermato"
    override fun channelChip(channel: Int) = "Canale ${channel}"
    override fun linkRateChip(mbps: Int) = "Tasso di collegamento ${mbps} Mbps"
    override val chooseNetwork = "Scegli una rete"
    override val turnOnWifi = "Accendi Wi-Fi"
    override val dismiss = "Ignora"
    override val findWeakSpotsTitle = "Trova i punti deboli"
    override val findWeakSpotsBody = "Trova le stanze con segnale debole."

    override val findWeakSpotsAction = "Controlla le tue stanze"
    override fun networksNearby(count: Int) = "${networks(count)} nelle vicinanze"
    override val nearbyStale = "Dalla tua ultima scansione. Tocca per aggiornare."
    override val nearbyNoConnection = "Visualizza canali e segnale per ciascuna rete."
    override fun nearbyOverlap(count: Int, channel: Int) = when (count) {
        0 -> "Nessuna sovrapposizione nelle vicinanze rilevata sul canale ${channel}."
        1 -> "1 segnale vicino si sovrappone al tuo canale ${channel}."
        else -> "${count} i segnali vicini si sovrappongono al tuo canale ${channel}."
    }

    override val adviceNoWifiBody = "Connettiti alla rete che desideri controllare. Puoi comunque eseguire test Internet e strumenti di rete utilizzando la tua connessione attuale."
    override val adviceNoInternetTitle = "Controllare prima l'accesso a Internet"
    override val adviceNoInternetBody = "Android non ha confermato l'accesso a Internet su questa connessione Wi-Fi. Verifica la presenza di una pagina di accesso, quindi verifica la connettività al router e a un host Internet."
    override val adviceNoSignalTitle = "Leggi il tuo segnale Wi-Fi"
    override val adviceNoSignalBody = "Consenti scansioni Wi-Fi nelle reti per vedere le misurazioni del segnale. Confronta il segnale nella stanza in cui noti problemi."
    override val adviceWeakSignalTitle = "Confronta la copertura vicino al tuo router"
    override val adviceWeakSignalBody = "Il segnale Wi-Fi è debole. Verifica la risposta del router qui e ripeti il test vicino al router. Confronta le misurazioni della tua rete per trovare i punti deboli."

    override val adviceGoodSignalTitle = "Confronta la reattività del router"
    override val adviceGoodSignalBody = "La potenza del segnale sembra utilizzabile e Android segnala l'accesso a Internet. Se la connessione sembra lenta, confronta la risposta del router qui e vicino al router; il segnale forte da solo non garantisce le prestazioni."
    override val channelAdviceBasis = "Le barre più corte indicano meno sovrapposizione in questa scansione. La velocità può variare."
    override fun channelChangeInstructions(channel: Int, band: String) = "Scegli il canale ${channel} per ${band}"

    override val channelOverlapTitle = "Sovrapposizione con le reti vicine"
    override val channelApplyChange = "Salva e riconnettiti se necessario"
    override val channelChangeNote = "Effettua la modifica nell’app o nelle impostazioni del router, non in WiFi Weakspot."

}
