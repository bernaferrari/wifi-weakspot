package com.bernaferrari.wifiweakspot.core.i18n.it

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object ItSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "Misurare… stai fermo"
    override val tapWhereYouAreStanding = "Tocca la mappa nel punto in cui ti trovi"
    override val chooseWifi = "Scegli Wi-Fi"
    override val undoLastReading = "Annulla l'ultima lettura"
    override val allReadings = "Tutte le letture"
    override val floorPlanTitle = "Planimetria"
    override fun readingCount(count: Int) = if (count == 1) "1 lettura" else "${count} letture"
    override val mapOptions = "Opzioni della mappa"
    override val usePlanPhoto = "Usa una foto della tua planimetria"
    override val changePhoto = "Cambia la foto"
    override val removePhoto = "Rimuovi la foto"
    override val exportReadingsCsv = "Esporta letture (CSV)"
    override fun floorName(floor: Int) = if (floor == 0) "Seminterrato" else "Piano ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "Piano ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "Aggiungi un seminterrato" else "Aggiungi piano ${floor}"
    override val deleteFloorReadingsAction = "Cancella le letture di questo piano"
    override fun deleteFloorBody(count: Int) =
        if (count == 1) "La sua 1 lettura verrà cancellata." else "Le sue letture ${count} verranno cancellate."

    override val oneRouterAtATime = "Una mappa segue un router alla volta. I sistemi mesh mostrano ciascuna unità separatamente."
    override val noNetworksFound = "Nessuna rete ancora trovata. Controlla che Wi-Fi e la posizione siano attivi."
    override val scanning = "Scansione…"
    override val refresh = "Aggiorna"
    override val switchWifiTitle = "Passare a un altro Wi-Fi?"
    override val switchWifiBody = "Questo cancella le letture su ogni piano di questa mappa. Crea una nuova mappa per mantenerli entrambi."
    override val clearAndSwitch = "Cancella e cambia"

    override val deleteMapBody = "Tutte le letture di ogni piano verranno cancellate."

    override val noReadingsYet = "Ancora nessuna lettura qui. Tocca la mappa nel punto in cui ti trovi."
    override val weakestFirst = "Prima i più deboli, quindi i punti problematici sono in cima."
    override val mapYourHome = "Mappa la tua casa"
    override val mapYourHomeBody = "Visualizza il tuo Wi-Fi sulla mappa di casa tua. Utilizza una foto della tua planimetria o una griglia vuota."
    override val createMap = "Crea una mappa"

    override val namePlaceholder = "Al piano superiore, ufficio…"

    override val floorplanDescription = "Pianta del progetto"
    override fun coverageMapDescription(count: Int) =
        "Mappa di copertura con ${readingCount(count)}. Tocca il punto in cui ti trovi per misurare."

    override val networkNotSeenHere = "Quel Wi-Fi non è apparso qui. Niente è stato salvato."
    override val projectChangedWhileScanning = "Il progetto è cambiato durante la scansione. Nessun punto è stato salvato."
    override val couldNotSaveReading = "Impossibile salvare la lettura. Le altre tue letture sono al sicuro."
    override fun defaultMapName(index: Int) = if (index == 1) "Casa" else "Casa ${index}"
    override val stillSavingMap = "Un attimo, sto ancora salvando la mappa."
    override val noFreshScan = "Android non ha fornito una nuova scansione. Riprova a breve."
    override val couldNotScan = "Impossibile eseguire la scansione. Controlla Wi-Fi e le impostazioni di posizione."
    override val chooseAccessPointFirst = "Scegli un punto di accesso prima di mapparne la copertura."
    override val saveMeasurementBeforeExport = "Salvare almeno una misurazione prima di esportarla."
    override val couldNotExportProject = "Impossibile esportare questo progetto. Riprova."

    override val connectToYourWifi = "Connettiti al tuo Wi-Fi"
    override fun checkingRoom(room: String) = "Controllo ${room}… stai fermo"
    override val connectedWifi = "Wi-Fi connesso"
    override val connectedNoReading = "Il tuo dispositivo è connesso, ma non è ancora disponibile la lettura del segnale."
    override val connectToCheck = "Connettiti allo Wi-Fi che vuoi controllare."
    override fun removedRoom(room: String) = "Rimosso ${room}"
    override val undo = "Annulla"
    override val roomsEmptyHint =
        "Mettiti in una stanza e salva una lettura. Controlla un'altra stanza per scoprire dove il segnale diventa più debole."
    override val noWifiSignal = "Nessun segnale Wi-Fi"
    override val saveRoom = "Risparmia spazio"
    override fun dbStronger(delta: Int) = "${delta} dB più forte"
    override fun dbWeaker(delta: Int) = "${delta} dB più debole"
    override val savedRooms = "Stanze salvate"
    override fun deleteRoom(room: String) = "Elimina ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) =
        "${room}, segnale ${quality}, ${reading}. Rivedi le letture e ricontrolla questo punto."

    override val noSignalToSave = "Nessun segnale Wi-Fi da salvare. Connettiti prima al tuo Wi-Fi."
    override val allowScansToLink = "Consenti scansioni Wi-Fi nelle reti in modo che questa lettura possa essere collegata alla tua rete."
    override val connectToSpotNetwork = "Connettiti alla rete di questo spot prima di ricontrollarlo."
    override fun savedRoom(room: String) = "Salvato ${room}"
    override val couldNotSaveStableReading = "Impossibile salvare una lettura stabile. Continua sullo stesso Wi-Fi e riprova."
    override val whereAreYou = "Dove sei?"
    override val somewhereElse = "Da qualche altra parte"

    override fun savedAt(time: String) = "Salvato ${time}"
    override val moveRouterTip = "Prova a spostare il router più in alto, più al centro o più vicino a questa stanza."
    override val betterThanLastTime = "Meglio dell'ultima volta"
    override val worseThanLastTime = "Peggio dell'ultima volta"
    override val sameAsLastTime = "Come l'ultima volta"
    override val earlierChecks = "Controlli precedenti"
    override val remove = "Rimuovi"
    override fun connectToRecheck(ssid: String?) =
        "Connettiti a ${ssid?.ifBlank { null } ?: "lo stesso Wi-Fi"} per verificare di nuovo."

    override val stepDeveloperOptionsTitle = "Attiva le opzioni sviluppatore"
    override val stepDeveloperOptionsBody =
        "In Impostazioni, apri **Informazioni sul telefono** e tocca **Numero build** 7 volte. Potrebbe essere necessario inserire il blocco schermo."
    override val openAboutPhone = "Apri Informazioni sul telefono"
    override val stepThrottlingTitle = "Disattiva Wi-Fi limitazione della scansione"
    override val stepThrottlingBody =
        "In **Opzioni sviluppatore**, scorri verso il basso fino a **Rete** e disattiva **Wi-Fi limitazione della scansione**."
    override val openDeveloperOptions = "Apri Opzioni sviluppatore"
    override val stepComeBackTitle = "Torna qui"
    override val stepComeBackBody =
        "WiFi Weakspot controlla automaticamente al tuo ritorno. Puoi riattivare la limitazione in qualsiasi momento."
    override val hideComputerMethod = "Nascondi metodo computer"
    override val useAdbInstead = "Preferisci un computer? Usa ADB"
    override val adbCommandClipLabel = "ADB"
    override val hideRootOption = "Nascondi l'opzione root"
    override val useRootInstead = "Dispositivo rootato? Utilizza invece root"
    override val scanLimitFootnote = "Android limita le scansioni per risparmiare batteria. Puoi riattivare il limite in qualsiasi momento."
    override val unlimitedWithRootTitle = "Scansioni illimitate con root"
    override val unlimitedOnTitle = "Sono attive scansioni illimitate"
    override val unlimitedOnBody = "Le reti vicine e i grafici dei canali si aggiornano ogni pochi secondi."
    override val scansLimitedTitle = "Le scansioni sono limitate"
    override val scansLimitedAndroid9Body =
        "Android 9 consente 4 scansioni ogni 2 minuti e non è possibile disattivarlo. WiFi Weakspot riutilizza i risultati recenti nel mezzo."
    override val usingRoot = "Utilizzando root"
    override val deviceRooted = "Il tuo dispositivo è rootato"
    override val rootScanningBody = "Le scansioni passano attraverso la shell del sistema, quindi non c'è nulla da cambiare nelle opzioni sviluppatore."
    override val rootDeniedBody =
        "L'accesso root è stato negato. Consenti WiFi Weakspot nel tuo gestore root e riprova oppure utilizza le opzioni sviluppatore di seguito."
    override val rootNotNeededBody = "Le scansioni sono già illimitate. Non è necessario il root."
    override val rootOfferBody =
        "Salta le opzioni sviluppatore e lascia che WiFi Weakspot esegua la scansione di root. Il tuo manager root te lo chiederà una volta."
    override val stopUsingRoot = "Smetti di usare root"
    override val waitingForRootManager = "Aspettando il tuo root manager…"
    override val tryRootAgain = "Prova di nuovo root"
    override val useRoot = "Usa root"
    override val copied = "Copiato"
    override val copyCommand = "Comando di copia"
}
