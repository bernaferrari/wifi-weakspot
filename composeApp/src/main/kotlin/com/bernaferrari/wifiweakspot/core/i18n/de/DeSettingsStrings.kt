package com.bernaferrari.wifiweakspot.core.i18n.de

import com.bernaferrari.wifiweakspot.core.i18n.SettingsStrings

object DeSettingsStrings : SettingsStrings {
    override val sectionBackup = "Sicherung"
    override val backupScope = "Speichere Karten, Grundrissbilder und Raummessungen in einer Datei deiner Wahl. Einstellungen und Diagnoseverlauf sind nicht enthalten."
    override val exportBackup = "Sicherung speichern"
    override val restoreBackup = "Sicherung wiederherstellen"
    override val restoreBackupExplanation = "Wähle eine WiFi-Weakspot-Sicherung. Karten werden als Kopien hinzugefügt, vorhandene Raummessungen bleiben erhalten. Deine bisherige Arbeit wird nicht ersetzt."
    override val backupSaved = "Sicherung gespeichert."
    override val backupRestored = "Sicherung wiederhergestellt."
    override val backupAlreadyRestored = "Diese Sicherung wurde bereits wiederhergestellt."
    override val backupExportFailed = "Sicherung konnte nicht gespeichert werden. Prüfe den freien Speicherplatz und versuche es erneut."
    override val backupRestoreFailed = "Wiederherstellung konnte nicht abgeschlossen werden. Prüfe die Datei und den freien Speicherplatz und versuche es mit derselben Datei erneut."

    override val sectionScanning = "Scannen"
    override val fasterScansThrottled = "Android ermöglicht 4 Scans alle 2 Minuten"
    override val sectionAppearance = "Darstellung"
    override val themeAuto = "Automatisch"
    override val themeLight = "Hell"
    override val themeDark = "Dunkel"
    override val colorStyle = "Farbstil"
    override val styleExpressive = "Ausdrucksvoll"
    override val styleVibrant = "Lebhaft"
    override val styleTonal = "Tonal"
    override val styleCmf = "CMF"
    override val showSignalAs = "Signalanzeige"
    override val sectionComfort = "Komfort"
    override val reduceMotionTitle = "Animationen reduzieren"

    override val hapticsSubtitle = "Vibration bei Berührungen und Ergebnissen"

    override val sectionAlerts = "Warnungen"
    override val backgroundAlertsTitle = "Hintergrundwarnungen"
    override val backgroundAlertsSubtitle = "Bei Änderungen an WLANs in der Nähe"

    override val notificationsDenied = "Erlauben Sie Benachrichtigungen, um den Überwachungsstatus und Warnungen anzuzeigen."
    override val checkEvery = "Prüfintervall"

    override fun intervalSeconds(seconds: Int) = "${seconds} Sek"
    override fun intervalMinutes(minutes: Int) = "${minutes} Min"
    override val newNetworkAlertsTitle = "Neue Netzwerkwarnungen"
    override val signalChangeAlertsTitle = "Signaländerungswarnungen"
    override val signalChangeAlertsSubtitle = "Wenn Ihr Signal stark ansteigt oder abfällt"
    override val sectionAbout = "Über die App"

    override fun aboutApp(appName: String) = "Über ${appName}"

    override fun version(version: String) = "Version ${version}"
    override val replayIntroTitle = "Einführung erneut anzeigen"

    override val wallpaperColors = "Hintergrundfarben"
    override val colorIndigo = "Indigo"
    override val colorOrange = "Orange"
    override val colorAmber = "Bernstein"
    override val colorGreen = "Grün"
    override val colorTeal = "Blaugrün"
    override val colorCyan = "Cyan"
    override val colorBlue = "Blau"
    override val colorPurple = "Lila"
    override val colorPink = "Rosa"

    override val tagline = "Schwache WLAN-Stellen zu Hause finden"

    override val openSourceLicense = "Open Source · Apache 2.0"
    override val feedbackTitle = "Feedback"
    override val feedbackBody = "Ideen und Fehlerberichte"
    override val elsewhere = "Weitere Links"

    override val privacySummary = "Ihre Ergebnisse bleiben auf Ihrem Gerät, solange Sie sie nicht teilen."

    override val privacyPolicy = "Datenschutzerklärung"

    override val notNow = "Nicht jetzt"
    override val getStarted = "Loslegen"

    override val welcomeHeadline = "Wo ist Ihr\nWLAN schwach?"

    override val welcomeBody = "Finden Sie heraus, wo das Signal schwächer wird und wie Sie es verbessern können."

    override val featureRoomsTitle = "Überprüfen Sie jedes Zimmer"
    override val featureRoomsBody = "Finden Sie die Räume mit schwachem Signal."

    override val featureRouterTitle = "Überprüfen Sie Ihren Router"
    override val featureRouterBody = "Sehen Sie, wie schnell die Antwort erfolgt."
    override val featureNearbyTitle = "Netzwerke in der Nähe anzeigen"
    override val featureNearbyBody = "Finden Sie einen Kanal mit weniger Überlappung."

}
