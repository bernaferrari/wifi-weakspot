package com.bernaferrari.wifiweakspot.core.i18n.de

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object DeSurveyStrings : SurveyStrings {
    override val horizontalPosition = "Waagerecht, von links"
    override val verticalPosition = "Senkrecht, von oben"
    override val measureHere = "Hier messen"
    override val recheckReading = "Hier erneut messen"
    override val moveLeft = "Nach links"
    override val moveRight = "Nach rechts"
    override val moveUp = "Nach oben"
    override val moveDown = "Nach unten"
    override fun mapPosition(x: Int, y: Int) = "Position: $x% von links, $y% von oben"
    override fun pointName(id: Long) = "Messung $id"

    override val measuringHoldStill = "Messen… stillhalten"
    override val tapWhereYouAreStanding = "Tippen Sie dort auf die Karte, wo Sie sich befinden"
    override val chooseWifi = "Wählen Sie Wi-Fi"
    override val undoLastReading = "Letzte Messung rückgängig machen"

    override val allReadings = "Alle Messwerte"

    override val floorPlanTitle = "Grundriss"
    override fun readingCount(count: Int) = if (count == 1) "1 Messung" else "${count} Messungen"

    override val mapOptions = "Kartenoptionen"
    override val usePlanPhoto = "Foto des Grundrisses verwenden"

    override val changePhoto = "Ändern Sie das Foto"
    override val removePhoto = "Entfernen Sie das Foto"
    override val exportReadingsCsv = "Messwerte exportieren (CSV)"

    override fun floorName(floor: Int) = if (floor == 0) "Keller" else "Etage ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "Etage ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "Fügen Sie einen Keller hinzu" else "Etage hinzufügen ${floor}"
    override val deleteFloorReadingsAction = "Löschen Sie die Messwerte dieser Etage"
    override fun deleteFloorBody(count: Int) = "Die Messwerte dieser Etage werden gelöscht (${count})."

    override val oneRouterAtATime = "Eine Karte erfasst jeweils einen Zugangspunkt. Bei Mesh-Systemen wird jede Einheit einzeln angezeigt."

    override val noNetworksFound = "Noch keine Netzwerke gefunden. Überprüfen Sie, ob Wi-Fi und Standort aktiviert sind."
    override val scanning = "Scan läuft…"
    override val refresh = "Aktualisieren"
    override val switchWifiTitle = "Zu einer anderen Wi-Fi wechseln?"
    override val switchWifiBody = "Dadurch werden die Messwerte auf jeder Etage dieser Karte gelöscht. Erstellen Sie eine neue Karte, um beides beizubehalten."
    override val clearAndSwitch = "Löschen und wechseln"

    override val deleteMapBody = "Alle Messwerte auf jeder Etage werden gelöscht."

    override val noReadingsYet = "Noch keine Messwerte vorhanden. Tippen Sie im Grundriss auf Ihren Standort."

    override val weakestFirst = "Der Schwächste zuerst, daher liegen die Problemstellen oben."
    override val mapYourHome = "Zuhause kartieren"
    override val mapYourHomeBody = "Sehen Sie die WLAN-Abdeckung im Grundriss Ihres Zuhauses. Nutzen Sie ein Foto des Grundrisses oder ein leeres Raster."

    override val createMap = "Erstellen Sie eine Karte"

    override val namePlaceholder = "Obergeschoss, Arbeitszimmer…"

    override val floorplanDescription = "Projektgrundriss"
    override fun coverageMapDescription(count: Int) =
        "Abdeckungskarte mit ${readingCount(count)}. Tippen Sie zum Messen auf die Stelle, an der Sie stehen."

    override val networkNotSeenHere = "Dieses WLAN wurde hier nicht gefunden. Es wurde keine Messung gespeichert."

    override val projectChangedWhileScanning = "Die Karte wurde während des Scans gewechselt. Es wurde kein Messpunkt gespeichert."

    override val couldNotSaveReading = "Dieser Messwert konnte nicht gespeichert werden. Ihre anderen Messwerte bleiben erhalten."

    override fun defaultMapName(index: Int) = if (index == 1) "Zuhause" else "Startseite ${index}"
    override val stillSavingMap = "Die Karte wird noch gespeichert. Bitte warten Sie kurz."

    override val noFreshScan = "Android hat keine neuen Scanergebnisse bereitgestellt. Warten Sie kurz und versuchen Sie es erneut."

    override val couldNotScan = "Konnte nicht scannen. Überprüfen Sie die Wi-Fi- und Standorteinstellungen."
    override val chooseAccessPointFirst = "Wählen Sie einen Zugangspunkt, bevor Sie dessen Abdeckung kartieren."
    override val saveMeasurementBeforeExport = "Speichern Sie vor dem Exportieren mindestens eine Messung."
    override val couldNotExportProject = "Dieses Projekt konnte nicht exportiert werden. Versuchen Sie es erneut."

    override val connectToYourWifi = "Verbinden Sie sich mit Ihrem Wi-Fi"
    override fun checkingRoom(room: String) = "Überprüfen Sie ${room}… ruhig bleiben"
    override val connectedWifi = "Verbundenes WLAN"
    override val connectedNoReading = "Ihr Gerät ist verbunden, aber es liegt noch kein Messwert für das Signal vor."

    override val connectToCheck = "Verbinden Sie sich mit der Wi-Fi, die Sie überprüfen möchten."
    override fun removedRoom(room: String) = "Entfernt ${room}"
    override val undo = "Rückgängig machen"
    override val roomsEmptyHint = "Bleiben Sie in einem Raum stehen und speichern Sie eine Messung. Messen Sie auch in anderen Räumen, um schwache Stellen zu finden."

    override val noWifiSignal = "Kein Wi-Fi-Signal"
    override val saveRoom = "Raum speichern"
    override fun dbStronger(delta: Int) = "${delta} dB stärker"
    override fun dbWeaker(delta: Int) = "${delta} dB schwächer"
    override val savedRooms = "Gespeicherte Räume"
    override fun deleteRoom(room: String) = "Löschen ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) = "${room}, Signal: ${quality}, ${reading}. Messwerte ansehen und diesen Ort erneut messen."

    override val noSignalToSave = "Kein Wi-Fi-Signal zum Speichern. Stellen Sie zunächst eine Verbindung zu Ihrem Wi-Fi her."
    override val allowScansToLink = "Erlauben Sie unter Netzwerke die WLAN-Scans, um diese Messung Ihrem Netzwerk zuzuordnen."

    override val connectToSpotNetwork = "Stellen Sie eine Verbindung zum Netzwerk dieses Spots her, bevor Sie ihn erneut überprüfen."
    override fun savedRoom(room: String) = "Gespeichert ${room}"
    override val couldNotSaveStableReading = "Es konnte kein stabiler Messwert gespeichert werden. Bleiben Sie am selben Ort und mit demselben WLAN verbunden und versuchen Sie es erneut."

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
    override val stepDeveloperOptionsBody = "Öffnen Sie in den Einstellungen **Über das Telefon** und tippen Sie 7-mal auf **Build-Nummer**. Möglicherweise müssen Sie die Displaysperre bestätigen."

    override val openAboutPhone = "Öffnen Sie „Info zum Telefon“."
    override val stepThrottlingTitle = "Deaktivieren Sie die Wi-Fi Scan-Drosselung"
    override val stepThrottlingBody = "Deaktivieren Sie in **Entwickleroptionen** unter **Netzwerke** die Option **Drosselung der WLAN-Suche**. Die Namen können je nach Gerät abweichen."

    override val openDeveloperOptions = "Öffnen Sie die Entwickleroptionen"
    override val stepComeBackTitle = "Komm hierher zurück"
    override val stepComeBackBody = "Wenn Sie zurückkehren, prüft WiFi Weakspot die Einstellung automatisch. Sie können die Begrenzung jederzeit wieder aktivieren."

    override val hideComputerMethod = "Anleitung für den Computer ausblenden"

    override val useAdbInstead = "Mit ADB am Computer einrichten"

    override val adbCommandClipLabel = "ADB Befehl"
    override val hideRootOption = "Root-Option ausblenden"

    override val useRootInstead = "Mit Root-Zugriff einrichten"

    override val scanLimitFootnote = "Android begrenzt Scans, um Batterie zu sparen. Sie können das Limit jederzeit wieder aktivieren."
    override val unlimitedWithRootTitle = "Unbegrenzte Scans mit root"
    override val unlimitedOnTitle = "Unbegrenzte Scans sind aktiviert"
    override val unlimitedOnBody = "Netzwerke und Kanaldiagramme in der Nähe werden alle paar Sekunden aktualisiert."
    override val scansLimitedTitle = "Scans sind begrenzt"
    override val scansLimitedAndroid9Body = "Android 9 erlaubt höchstens 4 Scans in 2 Minuten. Diese Begrenzung lässt sich nicht abschalten. Während der Wartezeit zeigt WiFi Weakspot die letzten Ergebnisse an."

    override val usingRoot = "Mit root"
    override val deviceRooted = "Ihr Gerät ist gerootet"
    override val rootScanningBody = "Scans laufen über die System-Shell, daher gibt es an den Entwickleroptionen nichts zu ändern."
    override val rootDeniedBody = "Der Root-Zugriff wurde verweigert. Erlauben Sie WiFi Weakspot im Root-Manager und versuchen Sie es erneut, oder nutzen Sie die Entwickleroptionen unten."

    override val rootNotNeededBody = "Scans sind bereits unbegrenzt. Root wird nicht benötigt."
    override val rootOfferBody = "Mit Root-Zugriff können Sie ohne Änderungen an den Entwickleroptionen scannen. Ihr Root-Manager fragt einmal nach der Berechtigung."

    override val stopUsingRoot = "Hören Sie auf, root zu verwenden"
    override val waitingForRootManager = "Warten auf den Root-Manager…"

    override val tryRootAgain = "Versuchen Sie es erneut mit root"
    override val useRoot = "Verwenden Sie root"
    override val copied = "Kopiert"
    override val copyCommand = "Befehl kopieren"

}
