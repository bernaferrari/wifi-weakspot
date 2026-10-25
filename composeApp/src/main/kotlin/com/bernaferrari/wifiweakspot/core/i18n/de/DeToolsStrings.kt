package com.bernaferrari.wifiweakspot.core.i18n.de

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object DeToolsStrings : ToolsStrings {
    override val tryAgain = "Versuchen Sie es erneut"

    override val targetPlaceholder = "Website oder IP-Adresse"
    override val clearTarget = "Löschen"

    override val checkSectionTitle = "Prüfen"
    override val recentSectionTitle = "Neu"

    override val toolPortsDescription = "Was ist offen"

    override val pingQuestion = "Antwortet es?"
    override val dnsQuestion = "Wie lautet die IP-Adresse?"

    override val dnsDetail = "DNS Suche"
    override val portsQuestion = "Welche Dienste sind verfügbar?"

    override val runningPortScanTitle = "Ports werden geprüft"

    override val diagnosticFailed = "Diagnose fehlgeschlagen"
    override val invalidTarget = "Geben Sie einen Hostnamen oder eine IP-Adresse ohne URL-Pfad oder Leerzeichen ein."
    override val diagnosticCouldNotComplete = "Die Diagnose konnte nicht abgeschlossen werden."
    override val connectionChangedRunAgain = "Die Verbindung hat sich geändert. Führen Sie diese Prüfung im neuen Netzwerk erneut durch."

    override val openItsWebPage = "Öffnen Sie die Webseite"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ${answered} von ${total} Antworten"

    override val slowest = "Am langsamsten"
    override fun repliesContentDescription(replies: String) = "Antworten: ${replies}"

    override val pingDidntAnswer = "Nicht geantwortet"
    override val pingResponsive = "Gute Reaktionszeit"

    override val pingResponsiveButUneven = "Schwankende Reaktionszeit"

    override val pingDroppingReplies = "Einige Antworten fehlen"

    override val pingSlowToAnswer = "Langsame Antwort"

    override val pingNoAnswerLocal = "Möglicherweise ist es ausgeschaltet, schläft oder ist so eingestellt, dass Pings ignoriert werden."
    override fun pingRepliesLost(lost: Int, total: Int) =
        "${lost} von ${total} Antworten gingen verloren. Vergleichen Sie einen Router-Check mit einem Internet-Host, um das Problem zu lokalisieren. Dieser Test allein kann die Ursache nicht ermitteln."
    override val pingUneven = "Antwortzeiten variieren. Vergleichen Sie einen Router-Check hier und in der Nähe des Routers und testen Sie dann einen Internet-Host."
    override val pingLocalExplanation = "Zeit, dieses Gerät und zurück zu erreichen. Das Gerät selbst kann das Ergebnis beeinflussen."
    override val pingInternetExplanation = "Zeit, diesen Host zu erreichen und zurück. Ihre Verbindung, die Route und der Host beeinflussen das Ergebnis."

    override fun dnsAddressUnit(count: Int) = if (count == 1) "Adresse" else "Adressen"
    override fun dnsForQuery(query: String) = "Für ${query}"
    override val dnsNotFound = "Konnte es nicht finden"
    override val dnsFound = "Gefunden"
    override val dnsNotFoundExplanation = "Überprüfen Sie die Rechtschreibung oder versuchen Sie es erneut; Der Name existiert möglicherweise nicht."
    override fun dnsFoundExplanation(query: String) = "Ihr Gerät kann ${query} finden."
    override val dnsAddressesTitle = "Adressen"

    override fun portsOpenUnit(count: Int) = "offen"

    override fun portsCommonServicesOn(host: String) = "Gemeinsame Dienste auf ${host}"
    override val portsNothingOpen = "Nichts offen"
    override fun portsOffersServices(count: Int) = if (count == 1) "Es bietet 1 Dienst" else "Es bietet ${count} Dienste an"
    override val portsNoneAnswered = "Keiner der üblichen Dienste hat geantwortet."

    override val portsOffersExplanation = "Dinge wie eine Einstellungsseite oder Dateifreigabe."

    override val portWebPage = "Webseite"
    override val portSecureWebPage = "Sichere Webseite"
    override val portRemoteLoginSsh = "Remote-Anmeldung (SSH)"
    override val portOldRemoteLoginTelnet = "Alte Remote-Anmeldung (Telnet)"
    override val portFileTransferFtp = "Dateiübertragung (FTP)"
    override val portAddressLookupsDns = "Adresssuche (DNS)"
    override val portFileSharing = "Dateifreigabe"
    override val portMacFileSharing = "Mac-Dateifreigabe"
    override val portPrinting = "Drucken"
    override val portRemoteDesktop = "Remotedesktop"
    override val portScreenSharing = "Bildschirmfreigabe"
    override val portDeviceDiscoveryUpnp = "Geräteerkennung (UPnP)"
    override val portStorageOrAdminPage = "Speicher- oder Admin-Seite"
    override val portDatabase = "Datenbank"
    override val portService = "Service"
    override val devicesLooking = "Auf der Suche nach Geräten…"
    override val devicesFindOnWifi = "Finden Sie Geräte auf Ihrem Wi-Fi"
    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${count} auf ${ssid}"
    override fun devicesOnYourNetwork(count: Int) = "${count} in Ihrem Netzwerk"
    override val couldntLookForDevices = "Konnte nicht nach Geräten suchen"
    override val scanFailed = "Scan fehlgeschlagen"
    override val connectionChangedLookAgain = "Die Verbindung hat sich geändert. Schauen Sie noch einmal in diesem Netzwerk vorbei."
    override val stoppedLooking = "Suche angehalten"

    override val noDevicesFound = "Keine Geräte gefunden"
    override val noDevicesFoundSubtitle = "Manche Geräte antworten nicht auf Scans. Versuchen Sie es gleich erneut oder prüfen Sie, ob dieses Gerät mit Ihrem Heim-WLAN verbunden ist."

    override fun otherDevicesGroup(count: Int) = "Andere Geräte · ${count}"
    override fun quietDevices(count: Int) = if (count == 1) "1 unbekanntes Gerät" else "${count} unbekannte Geräte"

    override val quietDevicesSubtitle = "Der Gerätetyp konnte nicht erkannt werden"

    override val hide = "Ausblenden"
    override val show = "Anzeigen"
    override fun checkingLocalAddresses(percent: Int) = "Überprüfung lokaler Adressen · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · hat nicht geantwortet"

    override val deviceNotScannedYet = "Noch nicht in einem Scan gefunden"
    override val deviceAnsweredLastScan = "Habe den letzten Scan beantwortet"
    override val deviceSeenButNoAnswer = "Im Netzwerk gesehen, aber nicht geantwortet"
    override val actionCheckPorts = "Überprüfen Sie die Ports"
    override val openRouterSettings = "Öffnen Sie die Router-Einstellungen"
    override val detailsTitle = "Details"
    override val factNetworkName = "Netzwerkname"
    override val factHardwareAddress = "Hardware-Adresse"
    override val macHiddenByAndroid = "Versteckt von Android"
    override val factYourName = "Ihr gewählter Name"

    override val portsNotCheckedYet = "Noch nicht geprüft."
    override fun portsNoneOpenWhenChecked(moment: String) = "Keiner der üblichen Ports war offen. Geprüft: ${moment}."

    override fun portsCheckedAt(moment: String) = "Geprüft: ${moment}."

    override val renameDeviceTitle = "Gerät umbenennen"
    override val renameSupporting = "Leer lassen, um den von WiFi Weakspot erkannten Namen zu verwenden."

    override val connectionChangedOpenAgain = "Die Verbindung hat sich geändert. Öffnen Sie dieses Gerät erneut im neuen Netzwerk."
    override val couldntLoadDetails = "Gespeicherte Details konnten nicht geladen werden. Versuchen Sie erneut, dieses Gerät zu öffnen."
    override val scanBeforeNaming = "Scannen Sie dieses Gerät, bevor Sie einen Namen speichern."

}
