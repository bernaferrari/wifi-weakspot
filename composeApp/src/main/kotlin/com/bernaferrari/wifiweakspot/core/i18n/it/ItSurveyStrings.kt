package com.bernaferrari.wifiweakspot.core.i18n.it

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object ItSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "Misurare… stai fermo"
    override val tapWhereYouAreStanding = "Tocca la mappa nel punto in cui ti trovi"
    override val chooseWifi = "Scegli Wi-Fi"
    override val undoLastReading = "Annulla l’ultima misurazione"

    override val allReadings = "Tutte le misurazioni"

    override val floorPlanTitle = "Planimetria"
    override fun readingCount(count: Int) = if (count == 1) "1 misurazione" else "${count} misurazioni"

    override val mapOptions = "Opzioni della mappa"
    override val usePlanPhoto = "Usa una foto della planimetria"

    override val changePhoto = "Cambia la foto"
    override val removePhoto = "Rimuovi la foto"
    override val exportReadingsCsv = "Esporta misurazioni (CSV)"

    override fun floorName(floor: Int) = if (floor == 0) "Seminterrato" else "Piano ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "Piano ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "Aggiungi un seminterrato" else "Aggiungi piano ${floor}"
    override val deleteFloorReadingsAction = "Cancella le misurazioni di questo piano"
    override fun deleteFloorBody(count: Int) = "Le misurazioni di questo piano verranno eliminate (${count})."

    override val oneRouterAtATime = "Ogni mappa misura un solo punto di accesso. Nei sistemi mesh, ogni unità appare separatamente."

    override val noNetworksFound = "Nessuna rete ancora trovata. Controlla che Wi-Fi e la posizione siano attivi."
    override val scanning = "Scansione…"
    override val refresh = "Aggiorna"
    override val switchWifiTitle = "Passare a un altro Wi-Fi?"
    override val switchWifiBody = "Questo cancella le misurazioni su ogni piano di questa mappa. Crea una nuova mappa per mantenerli entrambi."
    override val clearAndSwitch = "Cancella e cambia"

    override val deleteMapBody = "Tutte le misurazioni di ogni piano verranno cancellate."

    override val noReadingsYet = "Non ci sono ancora misurazioni. Tocca sulla planimetria il punto in cui ti trovi."

    override val weakestFirst = "Prima i più deboli, quindi i punti problematici sono in cima."
    override val mapYourHome = "Mappa la tua casa"
    override val mapYourHomeBody = "Visualizza la copertura Wi-Fi sulla planimetria di casa. Usa una foto della planimetria o una griglia vuota."

    override val createMap = "Crea una mappa"

    override val namePlaceholder = "Piano superiore, studio…"

    override val floorplanDescription = "Pianta del progetto"
    override fun coverageMapDescription(count: Int) =
        "Mappa di copertura con ${readingCount(count)}. Tocca il punto in cui ti trovi per misurare."

    override val networkNotSeenHere = "Questa rete Wi-Fi non è stata rilevata qui. Nessuna misurazione è stata salvata."

    override val projectChangedWhileScanning = "La mappa è cambiata durante la scansione. Nessun punto è stato salvato."

    override val couldNotSaveReading = "Impossibile salvare questa misurazione. Le altre misurazioni sono conservate."

    override fun defaultMapName(index: Int) = if (index == 1) "Casa" else "Casa ${index}"
    override val stillSavingMap = "Salvataggio della mappa in corso. Attendi un momento."

    override val noFreshScan = "Android non ha fornito nuovi risultati. Attendi un momento e riprova."

    override val couldNotScan = "Impossibile eseguire la scansione. Controlla Wi-Fi e le impostazioni di posizione."
    override val chooseAccessPointFirst = "Scegli un punto di accesso prima di mapparne la copertura."
    override val saveMeasurementBeforeExport = "Salvare almeno una misurazione prima di esportarla."
    override val couldNotExportProject = "Impossibile esportare questo progetto. Riprova."

    override val connectToYourWifi = "Connettiti al tuo Wi-Fi"
    override fun checkingRoom(room: String) = "Controllo ${room}… stai fermo"
    override val connectedWifi = "Wi-Fi connesso"
    override val connectedNoReading = "Il dispositivo è connesso, ma non è ancora disponibile una misurazione del segnale."

    override val connectToCheck = "Connettiti allo Wi-Fi che vuoi controllare."
    override fun removedRoom(room: String) = "Rimosso ${room}"
    override val undo = "Annulla"
    override val roomsEmptyHint = "Resta fermo in una stanza e salva una misurazione. Misura in un’altra stanza per trovare dove il segnale si indebolisce."

    override val noWifiSignal = "Nessun segnale Wi-Fi"
    override val saveRoom = "Risparmia spazio"
    override fun dbStronger(delta: Int) = "${delta} dB più forte"
    override fun dbWeaker(delta: Int) = "${delta} dB più debole"
    override val savedRooms = "Stanze salvate"
    override fun deleteRoom(room: String) = "Elimina ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) = "${room}, segnale ${quality}, ${reading}. Rivedi le misurazioni e misura di nuovo questo punto."

    override val noSignalToSave = "Nessun segnale Wi-Fi da salvare. Connettiti prima al tuo Wi-Fi."
    override val allowScansToLink = "Consenti le scansioni Wi-Fi in Reti per associare questa misurazione alla tua rete."

    override val connectToSpotNetwork = "Connettiti alla rete di questo spot prima di ricontrollarlo."
    override fun savedRoom(room: String) = "Salvato ${room}"
    override val couldNotSaveStableReading = "Impossibile salvare una misurazione stabile. Resta fermo e connesso allo stesso Wi-Fi, poi riprova."

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
    override val stepDeveloperOptionsBody = "In Impostazioni, apri **Informazioni sul telefono** e tocca **Numero build** 7 volte. Potrebbe essere necessario confermare il blocco schermo."

    override val openAboutPhone = "Apri Informazioni sul telefono"
    override val stepThrottlingTitle = "Disattiva Wi-Fi limitazione della scansione"
    override val stepThrottlingBody = "In **Opzioni sviluppatore**, nella sezione **Reti**, disattiva **Limita ricerca di reti Wi‑Fi**. I nomi possono variare in base al dispositivo."

    override val openDeveloperOptions = "Apri Opzioni sviluppatore"
    override val stepComeBackTitle = "Torna qui"
    override val stepComeBackBody = "Al ritorno, WiFi Weakspot verifica automaticamente l’impostazione. Puoi riattivare il limite in qualsiasi momento."

    override val hideComputerMethod = "Nascondi i passaggi per computer"

    override val useAdbInstead = "Configura con ADB da un computer"

    override val adbCommandClipLabel = "ADB"
    override val hideRootOption = "Nascondi l’opzione root"

    override val useRootInstead = "Configura con accesso root"

    override val scanLimitFootnote = "Android limita le scansioni per risparmiare batteria. Puoi riattivare il limite in qualsiasi momento."
    override val unlimitedWithRootTitle = "Scansioni illimitate con root"
    override val unlimitedOnTitle = "Sono attive scansioni illimitate"
    override val unlimitedOnBody = "Le reti vicine e i grafici dei canali si aggiornano ogni pochi secondi."
    override val scansLimitedTitle = "Le scansioni sono limitate"
    override val scansLimitedAndroid9Body = "Android 9 consente al massimo 4 scansioni ogni 2 minuti e non permette di rimuovere il limite. Nel frattempo, WiFi Weakspot mostra i risultati più recenti."

    override val usingRoot = "Utilizzando root"
    override val deviceRooted = "Il tuo dispositivo è rootato"
    override val rootScanningBody = "Le scansioni passano attraverso la shell del sistema, quindi non c'è nulla da cambiare nelle opzioni sviluppatore."
    override val rootDeniedBody = "L’accesso root è stato negato. Autorizza WiFi Weakspot nel gestore root e riprova, oppure usa le opzioni sviluppatore qui sotto."

    override val rootNotNeededBody = "Le scansioni sono già illimitate. Non è necessario il root."
    override val rootOfferBody = "Usa root per scansionare le reti senza modificare le opzioni sviluppatore. Il gestore root chiederà l’autorizzazione una volta."

    override val stopUsingRoot = "Smetti di usare root"
    override val waitingForRootManager = "In attesa del gestore root…"

    override val tryRootAgain = "Prova di nuovo root"
    override val useRoot = "Usa root"
    override val copied = "Copiato"
    override val copyCommand = "Copia comando"

}
