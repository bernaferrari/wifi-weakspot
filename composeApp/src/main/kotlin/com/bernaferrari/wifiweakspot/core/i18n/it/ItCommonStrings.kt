package com.bernaferrari.wifiweakspot.core.i18n.it

import com.bernaferrari.wifiweakspot.core.i18n.CommonStrings

object ItCommonStrings : CommonStrings {
    override fun deleteItemTitle(name: String) = "Eliminare ${name}?"

    override val couldNotSaveChanges = "Impossibile salvare le modifiche. Riprova."
    override val couldNotDeleteItem = "Impossibile eliminare questo elemento. Riprova."

    override val hiddenNetworkShort = "Nascosto"
    override val network = "Rete"
    override val tools = "Strumenti"
    override val maker = "Creatore"
    override val noReply = "Nessuna risposta"
    override val fastest = "Il più veloce"
    override val address = "Indirizzo"
    override val responseTime = "Tempo di risposta"
    override val checkAgain = "Ricontrolla"

    override val rename = "Rinomina"
    override val stop = "Fermati"
    override val unknown = "Sconosciuto"

    override val hiddenNetwork = "Rete nascosta"
    override val fasterScans = "Scansioni più rapide"
    override val openWifiSettings = "Apri impostazioni Wi-Fi"
    override val signalUnavailable = "Segnale non disponibile"
    override val devices = "Dispositivi"
    override val openPorts = "Porte aperte"
    override val nameLabel = "Nome"
    override val historyTitle = "Cronologia"
    override val routerCheck = "Verifica router"
    override val testWebsite = "Prova un sito web"
    override val yourRouter = "Il tuo router"
    override val yourNetwork = "La tua rete"

    override val openInNewTab = "Apri in una nuova scheda"
    override val mapSharingAndroidOnly = "La condivisione della mappa funziona nell'app Android."
    override val back = "Indietro"
    override val close = "Chiudi"
    override val cancel = "Annulla"
    override val save = "Salva"
    override val done = "Fine"
    override val share = "Condividi"
    override val delete = "Elimina"

    override val tabHome = "Home"
    override val tabNetworks = "Reti"
    override val tabRooms = "Stanze"
    override val tabSettings = "Impostazioni"

    override val permissionScreenTitle = "Consenti l’accesso alle reti Wi-Fi vicine"

    override val permissionAllowAccess = "Consenti l'accesso"
    override val openAppSettings = "Apri le impostazioni dell'app"

    override fun signalBadgeDescription(quality: String, reading: String) = "${quality} segnale, ${reading}"

    override val scanReady = "PRONTO"
    override fun scansCount(count: Int) = if (count == 1) "1 scansione" else "${count} scansiona"
    override val scansLeftCaption = "rimanenti"
    override val scanWaitingCaption = "in attesa"
    override val scanDevMode = "MODALITÀ SVILUPPO"
    override val readyToScan = "Pronto per la scansione"
    override fun scansRemaining(count: Int) = if (count == 1) "1 scansione rimanente" else "${count} scansioni rimanenti"
    override fun nextScanIn(time: String) = "Prossima scansione tra ${time}"
    override val checking = "Verifica…"

    override val justNow = "proprio ora"
    override fun minutesAgo(minutes: Long) = "${minutes} min fa"
    override fun hoursAgo(hours: Long) = "${hours} h fa"
    override fun daysAgo(days: Long) = "${days} d fa"
}
