package com.bernaferrari.wifiweakspot.core.i18n.de

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object DeSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "Messen… stillhalten"
    override val tapWhereYouAreStanding = "Tippen Sie dort auf die Karte, wo Sie sich befinden"
    override val chooseWifi = "Wählen Sie Wi-Fi"
    override val undoLastReading = "Letzte Lesung rückgängig machen"
    override val allReadings = "Alle Messwerte"
    override val floorPlanTitle = "Grundriss"
    override fun readingCount(count: Int) = if (count == 1) "1 Lesung" else "${count} Messwerte"
    override val mapOptions = "Kartenoptionen"
    override val usePlanPhoto = "Verwenden Sie ein Foto Ihres Plans"
    override val changePhoto = "Ändern Sie das Foto"
    override val removePhoto = "Entfernen Sie das Foto"
    override val exportReadingsCsv = "Messwerte exportieren (CSV)"
    override fun floorName(floor: Int) = if (floor == 0) "Keller" else "Etage ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "Etage ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "Fügen Sie einen Keller hinzu" else "Etage hinzufügen ${floor}"
    override val deleteFloorReadingsAction = "Löschen Sie die Messwerte dieser Etage"
    override fun deleteFloorBody(count: Int) =
        if (count == 1) "Sein 1 Messwert wird gelöscht." else "Seine ${count} Messwerte werden gelöscht."

    override val oneRouterAtATime = "Eine Karte folgt jeweils einem Router. Mesh-Systeme zeigen jede Einheit separat an."
    override val noNetworksFound = "Noch keine Netzwerke gefunden. Überprüfen Sie, ob Wi-Fi und Standort aktiviert sind."
    override val scanning = "Scan läuft…"
    override val refresh = "Aktualisieren"
    override val switchWifiTitle = "Zu einer anderen Wi-Fi wechseln?"
    override val switchWifiBody = "Dadurch werden die Messwerte auf jeder Etage dieser Karte gelöscht. Erstellen Sie eine neue Karte, um beides beizubehalten."
    override val clearAndSwitch = "Löschen und wechseln"

    override val deleteMapBody = "Alle Messwerte auf jeder Etage werden gelöscht."

    override val noReadingsYet = "Hier gibt es noch keine Lesungen. Tippen Sie auf die Karte, auf der Sie sich befinden."
    override val weakestFirst = "Der Schwächste zuerst, daher liegen die Problemstellen oben."
    override val mapYourHome = "Kartieren Sie Ihr Zuhause"
    override val mapYourHomeBody = "Sehen Sie sich Ihr Wi-Fi auf einer Karte Ihres Zuhauses an. Verwenden Sie ein Foto Ihres Plans oder ein leeres Raster."
    override val createMap = "Erstellen Sie eine Karte"

    override val namePlaceholder = "Oben, Büro…"

    override val floorplanDescription = "Projektgrundriss"
    override fun coverageMapDescription(count: Int) =
        "Abdeckungskarte mit ${readingCount(count)}. Tippen Sie zum Messen auf die Stelle, an der Sie stehen."

    override val networkNotSeenHere = "Das Wi-Fi ist hier nicht aufgetaucht. Es wurde nichts gerettet."
    override val projectChangedWhileScanning = "Das Projekt hat sich während des Scannens geändert. Es wurde kein Punkt gespeichert."
    override val couldNotSaveReading = "Der Messwert konnte nicht gespeichert werden. Ihre anderen Messwerte sind sicher."
    override fun defaultMapName(index: Int) = if (index == 1) "Zuhause" else "Startseite ${index}"
    override val stillSavingMap = "Einen Moment, ich speichere immer noch die Karte."
    override val noFreshScan = "Android hat keinen neuen Scan bereitgestellt. Versuchen Sie es in Kürze noch einmal."
    override val couldNotScan = "Konnte nicht scannen. Überprüfen Sie die Wi-Fi- und Standorteinstellungen."
    override val chooseAccessPointFirst = "Wählen Sie einen Zugangspunkt, bevor Sie dessen Abdeckung kartieren."
    override val saveMeasurementBeforeExport = "Speichern Sie vor dem Exportieren mindestens eine Messung."
    override val couldNotExportProject = "Dieses Projekt konnte nicht exportiert werden. Versuchen Sie es erneut."

    override val connectToYourWifi = "Verbinden Sie sich mit Ihrem Wi-Fi"
    override fun checkingRoom(room: String) = "Überprüfen Sie ${room}… ruhig bleiben"
    override val connectedWifi = "Verbundenes WLAN"
    override val connectedNoReading = "Ihr Gerät ist verbunden, aber es ist noch kein Signalwert verfügbar."
    override val connectToCheck = "Verbinden Sie sich mit der Wi-Fi, die Sie überprüfen möchten."
    override fun removedRoom(room: String) = "Entfernt ${room}"
    override val undo = "Rückgängig machen"
    override val roomsEmptyHint =
        "Stellen Sie sich in einen Raum und speichern Sie einen Messwert. Schauen Sie in einem anderen Raum nach, um herauszufinden, wo das Signal schwächer wird."
    override val noWifiSignal = "Kein Wi-Fi-Signal"
    override val saveRoom = "Platz sparen"
    override fun dbStronger(delta: Int) = "${delta} dB stärker"
    override fun dbWeaker(delta: Int) = "${delta} dB schwächer"
    override val savedRooms = "Gespeicherte Räume"
    override fun deleteRoom(room: String) = "Löschen ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) =
        "${room}, ${quality} Signal, ${reading}. Überprüfen Sie die Messwerte und überprüfen Sie diese Stelle noch einmal."

    override val noSignalToSave = "Kein Wi-Fi-Signal zum Speichern. Stellen Sie zunächst eine Verbindung zu Ihrem Wi-Fi her."
    override val allowScansToLink = "Erlauben Sie Wi-Fi Scans in Netzwerken, damit dieser Messwert mit Ihrem Netzwerk verknüpft werden kann."
    override val connectToSpotNetwork = "Stellen Sie eine Verbindung zum Netzwerk dieses Spots her, bevor Sie ihn erneut überprüfen."
    override fun savedRoom(room: String) = "Gespeichert ${room}"
    override val couldNotSaveStableReading = "Der stabile Messwert konnte nicht gespeichert werden. Bleiben Sie weiterhin bei der gleichen Wi-Fi und versuchen Sie es erneut."
    override val whereAreYou = "Wo bist du?"
    override val somewhereElse = "Woanders"

    override fun savedAt(time: String) = "Gespeichert ${time}"
    override val moveRouterTip = "Versuchen Sie, den Router höher, zentraler oder näher an diesem Raum aufzustellen."
    override val betterThanLastTime = "Besser als letztes Mal"
    override val worseThanLastTime = "Schlimmer als beim letzten Mal"
    override val sameAsLastTime = "Das Gleiche wie beim letzten Mal"
    override val earlierChecks = "Frühere Kontrollen"
    override val remove = "Entfernen"
    override fun connectToRecheck(ssid: String?) =
        "Stellen Sie eine Verbindung zu ${ssid?.ifBlank { null } ?: "das gleiche Wi-Fi"} her, um die Überprüfung erneut durchzuführen."

    override val stepDeveloperOptionsTitle = "Aktivieren Sie die Entwickleroptionen"
    override val stepDeveloperOptionsBody =
        "Öffnen Sie in den Einstellungen **Über das Telefon** und tippen Sie siebenmal auf **Build-Nummer**. Möglicherweise müssen Sie Ihre Bildschirmsperre eingeben."
    override val openAboutPhone = "Öffnen Sie „Info zum Telefon“."
    override val stepThrottlingTitle = "Deaktivieren Sie die Wi-Fi Scan-Drosselung"
    override val stepThrottlingBody =
        "Scrollen Sie in den **Entwickleroptionen** nach unten zu **Netzwerk** und schalten Sie die **Wi-Fi Scan-Drosselung** aus."
    override val openDeveloperOptions = "Öffnen Sie die Entwickleroptionen"
    override val stepComeBackTitle = "Komm hierher zurück"
    override val stepComeBackBody =
        "WiFi Weakspot prüft automatisch, wenn Sie zurückkommen. Sie können die Drosselung jederzeit wieder einschalten."
    override val hideComputerMethod = "Computermethode ausblenden"
    override val useAdbInstead = "Bevorzugen Sie einen Computer? Verwenden Sie ADB"
    override val adbCommandClipLabel = "ADB Befehl"
    override val hideRootOption = "Option root ausblenden"
    override val useRootInstead = "Gerootetes Gerät? Verwenden Sie stattdessen root"
    override val scanLimitFootnote = "Android begrenzt Scans, um Batterie zu sparen. Sie können das Limit jederzeit wieder aktivieren."
    override val unlimitedWithRootTitle = "Unbegrenzte Scans mit root"
    override val unlimitedOnTitle = "Unbegrenzte Scans sind aktiviert"
    override val unlimitedOnBody = "Netzwerke und Kanaldiagramme in der Nähe werden alle paar Sekunden aktualisiert."
    override val scansLimitedTitle = "Scans sind begrenzt"
    override val scansLimitedAndroid9Body =
        "Android 9 ermöglicht 4 Scans alle 2 Minuten und kann nicht ausgeschaltet werden. WiFi Weakspot verwendet aktuelle Ergebnisse zwischendurch wieder."
    override val usingRoot = "Mit root"
    override val deviceRooted = "Ihr Gerät ist gerootet"
    override val rootScanningBody = "Scans laufen über die System-Shell, daher gibt es an den Entwickleroptionen nichts zu ändern."
    override val rootDeniedBody =
        "Root-Zugriff wurde verweigert. Erlauben Sie WiFi Weakspot in Ihrem root-Manager und versuchen Sie es erneut, oder verwenden Sie die Entwickleroptionen unten."
    override val rootNotNeededBody = "Scans sind bereits unbegrenzt. Root wird nicht benötigt."
    override val rootOfferBody =
        "Überspringen Sie die Entwickleroptionen und lassen Sie WiFi Weakspot bis root scannen. Ihr root Manager wird einmal nachfragen."
    override val stopUsingRoot = "Hören Sie auf, root zu verwenden"
    override val waitingForRootManager = "Warten auf Ihren root Manager…"
    override val tryRootAgain = "Versuchen Sie es erneut mit root"
    override val useRoot = "Verwenden Sie root"
    override val copied = "Kopiert"
    override val copyCommand = "Befehl kopieren"
}
