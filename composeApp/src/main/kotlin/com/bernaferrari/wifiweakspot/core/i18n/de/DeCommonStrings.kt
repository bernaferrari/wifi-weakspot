package com.bernaferrari.wifiweakspot.core.i18n.de

import com.bernaferrari.wifiweakspot.core.i18n.CommonStrings

object DeCommonStrings : CommonStrings {
    override fun deleteItemTitle(name: String) = "${name} löschen?"

    override val couldNotSaveChanges = "Die Änderungen konnten nicht gespeichert werden. Versuchen Sie es erneut."
    override val couldNotDeleteItem = "Dieses Element konnte nicht gelöscht werden. Versuchen Sie es erneut."

    override val hiddenNetworkShort = "Versteckt"
    override val network = "Netzwerk"
    override val tools = "Werkzeuge"
    override val maker = "Hersteller"
    override val noReply = "Keine Antwort"
    override val fastest = "Am schnellsten"
    override val address = "Adresse"
    override val responseTime = "Reaktionszeit"
    override val checkAgain = "Erneut prüfen"

    override val rename = "Umbenennen"
    override val stop = "Stopp"
    override val unknown = "Unbekannt"

    override val hiddenNetwork = "Verstecktes Netzwerk"
    override val fasterScans = "Schneller scannen"
    override val openWifiSettings = "WLAN-Einstellungen öffnen"
    override val signalUnavailable = "Signal nicht verfügbar"
    override val devices = "Geräte"
    override val openPorts = "Offene Ports"
    override val nameLabel = "Name"
    override val historyTitle = "Verlauf"
    override val routerCheck = "Router prüfen"
    override val testWebsite = "Website testen"
    override val yourRouter = "Ihr Router"
    override val yourNetwork = "Ihr Netzwerk"

    override val openInNewTab = "In einem neuen Tab öffnen"
    override val mapSharingAndroidOnly = "Das Teilen der Karte funktioniert in der Android-App."
    override val back = "Zurück"
    override val close = "Schließen"
    override val cancel = "Abbrechen"
    override val save = "Speichern"
    override val done = "Fertig"
    override val share = "Teilen"
    override val delete = "Löschen"

    override val tabHome = "Start"
    override val tabNetworks = "Netzwerke"
    override val tabRooms = "Räume"
    override val tabSettings = "Einstellungen"

    override val permissionScreenTitle = "Zugriff auf WLANs in der Nähe erlauben"

    override val permissionAllowAccess = "Zugriff erlauben"
    override val openAppSettings = "Öffnen Sie die App-Einstellungen"

    override fun signalBadgeDescription(quality: String, reading: String) = "${quality} Signal, ${reading}"

    override val scanReady = "BEREIT"
    override fun scansCount(count: Int) = if (count == 1) "1 Scan" else "${count} Scans"
    override val scansLeftCaption = "übrig"
    override val scanWaitingCaption = "wartet"
    override val scanDevMode = "ENTWICKLERMODUS"
    override val readyToScan = "Bereit zum Scannen"
    override fun scansRemaining(count: Int) = if (count == 1) "1 Scan übrig" else "${count} verbleibende Scans"
    override fun nextScanIn(time: String) = "Nächster Scan in ${time}"
    override val checking = "Wird geprüft…"

    override val justNow = "gerade eben"
    override fun minutesAgo(minutes: Long) = "vor ${minutes} Min."

    override fun hoursAgo(hours: Long) = "vor ${hours} Std."

    override fun daysAgo(days: Long) = "vor ${days} Tagen"

}
