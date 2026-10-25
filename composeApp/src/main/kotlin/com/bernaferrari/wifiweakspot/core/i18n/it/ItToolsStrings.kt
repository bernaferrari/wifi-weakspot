package com.bernaferrari.wifiweakspot.core.i18n.it

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object ItToolsStrings : ToolsStrings {
    override val tryAgain = "Riprova"

    override val targetPlaceholder = "Sito web o indirizzo IP"
    override val clearTarget = "Cancella"
    override val checkSectionTitle = "Controlla"
    override val recentSectionTitle = "Recenti"

    override val toolPortsDescription = "Cosa è aperto"

    override val pingQuestion = "Risponde?"
    override val dnsQuestion = "Qual è il suo indirizzo?"
    override val dnsDetail = "DNS ricerca"
    override val portsQuestion = "Cosa offre?"

    override val runningPortScanTitle = "Verifica delle porte"

    override val diagnosticFailed = "Diagnostica fallita"
    override val invalidTarget = "Inserisci un nome host o un indirizzo IP senza percorso URL o spazi."
    override val diagnosticCouldNotComplete = "Impossibile completare la diagnostica."
    override val connectionChangedRunAgain = "La connessione è cambiata. Esegui nuovamente questo controllo sulla nuova rete."

    override val openItsWebPage = "Apri la sua pagina web"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ${answered} risposte su ${total}"

    override val slowest = "Il più lento"
    override fun repliesContentDescription(replies: String) = "Risposte: ${replies}"

    override val pingDidntAnswer = "Non ho risposto"
    override val pingResponsive = "Reattivo"
    override val pingResponsiveButUneven = "Reattivo, ma irregolare"
    override val pingDroppingReplies = "Eliminazione delle risposte"
    override val pingSlowToAnswer = "Lento a rispondere"

    override val pingNoAnswerLocal = "Potrebbe essere spento, inattivo o impostato per ignorare i ping."
    override fun pingRepliesLost(lost: Int, total: Int) =
        "${lost} di ${total} risposte sono andate perse. Confronta un controllo del router con un host Internet per individuare il problema; questo test da solo non può identificare la causa."
    override val pingUneven = "I tempi di risposta sono stati variabili. Confronta un controllo del router qui e vicino al router, quindi testa un host Internet."
    override val pingLocalExplanation = "È ora di raggiungere questo dispositivo e ritorno. Il dispositivo stesso può influenzare il risultato."
    override val pingInternetExplanation = "È ora di raggiungere questo host e ritorno. La tua connessione, il percorso e l'host influiscono tutti sul risultato."

    override fun dnsAddressUnit(count: Int) = if (count == 1) "indirizzo" else "indirizzi"
    override fun dnsForQuery(query: String) = "Per ${query}"
    override val dnsNotFound = "Impossibile trovarlo"
    override val dnsFound = "Trovato"
    override val dnsNotFoundExplanation = "Controlla l'ortografia o riprova; il nome potrebbe non esistere."
    override fun dnsFoundExplanation(query: String) = "Il tuo dispositivo può trovare ${query}."
    override val dnsAddressesTitle = "Indirizzi"

    override val portsOpenUnit = "aperto"
    override fun portsCommonServicesOn(host: String) = "Servizi comuni su ${host}"
    override val portsNothingOpen = "Niente di aperto"
    override fun portsOffersServices(count: Int) = if (count == 1) "Offre 1 servizio" else "Offre ${count} servizi"
    override val portsNoneAnswered = "Nessun servizio comune ha risposto."
    override val portsOffersExplanation = "Cose come una pagina delle impostazioni o la condivisione di file."

    override val portWebPage = "Pagina web"
    override val portSecureWebPage = "Pagina web sicura"
    override val portRemoteLoginSsh = "Accesso remoto (SSH)"
    override val portOldRemoteLoginTelnet = "Vecchio login remoto (Telnet)"
    override val portFileTransferFtp = "Trasferimento file (FTP)"
    override val portAddressLookupsDns = "Ricerche di indirizzi (DNS)"
    override val portFileSharing = "Condivisione file"
    override val portMacFileSharing = "Condivisione file Mac"
    override val portPrinting = "Stampa"
    override val portRemoteDesktop = "Desktop remoto"
    override val portScreenSharing = "Condivisione dello schermo"
    override val portDeviceDiscoveryUpnp = "Rilevamento dispositivo (UPnP)"
    override val portStorageOrAdminPage = "Pagina di archiviazione o amministrazione"
    override val portDatabase = "Banca dati"
    override val portService = "Servizio"
    override val devicesLooking = "Alla ricerca di dispositivi…"
    override val devicesFindOnWifi = "Trova dispositivi sul tuo Wi-Fi"
    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${count} su ${ssid}"
    override fun devicesOnYourNetwork(count: Int) = "${count} sulla tua rete"
    override val couldntLookForDevices = "Impossibile cercare i dispositivi"
    override val scanFailed = "Scansione fallita"
    override val connectionChangedLookAgain = "La connessione è cambiata. Guarda di nuovo su questa rete."
    override val stoppedLooking = "Ho smesso di cercare"
    override val noDevicesFound = "Nessun dispositivo trovato"
    override val noDevicesFoundSubtitle =
        "Alcuni dispositivi rimangono silenziosi quando richiesto. Riprova tra un attimo o verifica che questo dispositivo sia presente nella tua casa Wi-Fi."
    override fun otherDevicesGroup(count: Int) = "Altri dispositivi · ${count}"
    override fun quietDevices(count: Int) = if (count == 1) "1 dispositivo silenzioso" else "${count} dispositivi silenziosi"
    override val quietDevicesSubtitle = "Non hanno detto cosa sono"
    override val hide = "Nascondi"
    override val show = "Mostra"
    override fun checkingLocalAddresses(percent: Int) = "Controllo indirizzi locali · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · non ha risposto"

    override val deviceNotScannedYet = "Non ancora trovato nella scansione"
    override val deviceAnsweredLastScan = "Ho risposto all'ultima scansione"
    override val deviceSeenButNoAnswer = "Visto in rete, ma non ho risposto"
    override val actionCheckPorts = "Controlla le porte"
    override val openRouterSettings = "Apri le impostazioni del router"
    override val detailsTitle = "Dettagli"
    override val factNetworkName = "Nome della rete"
    override val factHardwareAddress = "Indirizzo hardware"
    override val macHiddenByAndroid = "Nascosto da Android"
    override val factYourName = "Il tuo nome"
    override val portsNotCheckedYet = "Non ancora selezionato."
    override fun portsNoneOpenWhenChecked(moment: String) =
        "Nessuna delle porte comuni era aperta quando è stato controllato ${moment.replaceFirstChar { it.lowercase() }}."
    override fun portsCheckedAt(moment: String) = "Selezionato ${moment.replaceFirstChar { it.lowercase() }}."
    override val renameDeviceTitle = "Rinomina dispositivo"
    override val renameSupporting = "Lascia vuoto per utilizzare il nome WiFi Weakspot trovato."
    override val connectionChangedOpenAgain = "La connessione è cambiata. Apri di nuovo questo dispositivo sulla nuova rete."
    override val couldntLoadDetails = "Impossibile caricare i dettagli salvati. Prova ad aprire di nuovo questo dispositivo."
    override val scanBeforeNaming = "Scansiona questo dispositivo prima di salvare un nome."

}
