package com.bernaferrari.wifiweakspot.core.i18n.it

import com.bernaferrari.wifiweakspot.core.i18n.HistoryStrings

object ItHistoryStrings : HistoryStrings {
    override val today = "Oggi"
    override val yesterday = "Ieri"
    override val weekdayNames = listOf("lunedì", "martedì", "mercoledì", "giovedì", "venerdì", "sabato", "domenica")
    override val weekdayShortNames = listOf("lun", "mar", "mer", "gio", "ven", "sab", "dom")
    override val monthShortNames = listOf("gen", "feb", "mar", "apr", "mag", "giu", "lug", "ago", "set", "ott", "nov", "dic")
    override fun dateThisYear(weekday: String, day: Int, month: String) = "${weekday} ${day} ${month}"

    override fun dateOtherYear(weekday: String, day: Int, month: String, year: Int) = "${weekday} ${day} ${month} ${year}"

    override fun momentAt(day: String, time: String) = "${day} alle ${time}"

    override val filterAll = "Tutti"
    override val kindInternetTests = "Test su Internet"
    override val kindScans = "Scansioni"
    override val emptyTitle = "Ancora niente salvato"
    override val emptySubtitle = "I controlli del router, le scansioni della rete e i risultati degli strumenti vengono visualizzati qui, così puoi vedere come cambia il tuo Wi-Fi nel tempo."
    override fun trendBarsDescription(count: Int, readings: String) = "Ultimi test del router ${count}: ${readings}"
    override val trendNoReply = "Nessuna risposta dal router. Alcuni router ignorano questi controlli."
    override val trendRecheckToCompare = "Ricontrolla dopo una modifica per confrontare."
    override fun trendQuicker(usualMs: String) = "Più veloce del solito (${usualMs} ms)"
    override fun trendSlower(usualMs: String) = "Più lento del solito (${usualMs} ms)"
    override fun trendAboutSame(usualMs: String) = "Più o meno come al solito (${usualMs} ms)"

    override fun internetTitle(mbps: String) = "${mbps} Mbps scarica"
    override fun routerTitle(ms: String) = "${ms} ms al router"
    override fun networksNearby(count: Int) = if (count == 1) "1 rete nelle vicinanze" else "${count} reti nelle vicinanze"
    override fun strongestNetwork(network: String) = "Più forte: ${network}"
    override fun accessPoints(count: Int) = if (count == 1) "1 punto di accesso" else "${count} punti di accesso"
    override fun devicesFound(count: Int) = if (count == 1) "1 dispositivo trovato" else "${count} dispositivi trovati"
    override fun listWithMore(list: String, more: Int) = if (more > 0) "${list} e ${more} altro" else list
    override fun pingSummary(averageMs: String, replied: Int, sent: Int) = "${averageMs} ms · ${replied} risposte su ${sent}"

    override fun lookupTitle(target: String) = "Cerca ${target}"
    override val noRecords = "Nessun record"
    override val lookupFailed = "Ricerca fallita"
    override fun openPortsTitle(target: String) = "Porte aperte su ${target}"
    override val noCommonPortsOpen = "Nessuno dei porti comuni era aperto"
    override val completed = "Completato"
    override val failed = "Fallito"

    override val metricDownload = "Scarica"
    override val metricUpload = "Carica"
    override val metricLatency = "Latenza"
    override val metricVariation = "Variazione"
    override val metricLost = "Smarrito"
    override fun metricOnBand(band: String) = "il ${band}"
    override val metricReplied = "Rispose"
    override val metricReplies = "Risposte"
    override val metricAverage = "Nella media"
    override val factServer = "Server"
    override val factRoom = "Camera"
    override val factBand = "Banda"
    override val factSignal = "Segnale"
    override val factAccessPoints = "Punti di accesso"
    override fun signalFact(quality: String, rssi: Int) = "${quality} segnale · ${rssi} dBm"

    override val linesNetworksStrongestFirst = "Reti, prima le più forti"
    override val linesRecords = "Record"
    override fun replyNumber(index: Int) = "Rispondi ${index}"
    override val scanNote = "Le reti vicine sullo stesso canale del tuo competono per il tempo di trasmissione."

    override val go = "VAI"
    override val testInternetSpeedInstead = "Prova invece la velocità di Internet"
    override val keepDeviceStill = "Mantieni fermo il tuo dispositivo per qualche secondo."
    override val connectToStart = "Connettiti al tuo Wi-Fi per iniziare."
    override val lastTime = "L'ultima volta"
    override val couldNotSaveSpot = "Impossibile salvare questo punto. Riprova."
    override val connectFirst = "Connettiti prima al tuo Wi-Fi. Questo controllo misura le risposte dal router."
    override val historyNotSaved = "Risultato misurato, ma non è stato possibile salvare la cronologia."
    override val checkStopped = "La connessione è cambiata o il controllo del router è stato interrotto. Mantieni questo Wi-Fi connesso e riprova. Alcuni router bloccano il ping."

    override val internetSpeedTitle = "Velocità Internet · fast.com"
}
