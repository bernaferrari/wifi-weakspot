package com.bernaferrari.wifiweakspot.core.i18n.it

import com.bernaferrari.wifiweakspot.core.i18n.SettingsStrings

object ItSettingsStrings : SettingsStrings {
    override val sectionBackup = "Backup"
    override val backupScope = "Salva mappe, immagini delle planimetrie e misurazioni delle stanze in un file a tua scelta. Impostazioni e cronologia diagnostica non sono incluse."
    override val exportBackup = "Salva backup"
    override val restoreBackup = "Ripristina backup"
    override val restoreBackupExplanation = "Scegli un backup di WiFi Weakspot. Le mappe vengono aggiunte come copie e le misurazioni esistenti vengono conservate. Il lavoro attuale non verrà sostituito."
    override val backupSaved = "Backup salvato."
    override val backupRestored = "Backup ripristinato."
    override val backupAlreadyRestored = "Questo backup è già stato ripristinato."
    override val backupExportFailed = "Impossibile salvare il backup. Controlla lo spazio libero e riprova."
    override val backupRestoreFailed = "Impossibile completare il ripristino. Controlla il file e lo spazio libero, poi riprova con lo stesso file."

    override val sectionScanning = "Scansione"
    override val fasterScansThrottled = "Android consente 4 scansioni ogni 2 minuti"
    override val sectionAppearance = "Aspetto"
    override val themeAuto = "Automatico"
    override val themeLight = "Chiaro"
    override val themeDark = "Scuro"
    override val colorStyle = "Stile colore"
    override val styleExpressive = "Espressivo"
    override val styleVibrant = "Vivace"
    override val styleTonal = "Tonale"
    override val styleCmf = "Tenue"
    override val showSignalAs = "Mostra segnale come"
    override val sectionComfort = "Comodità"
    override val reduceMotionTitle = "Riduci le animazioni"

    override val hapticsSubtitle = "Vibrazione al tocco e ai risultati"

    override val sectionAlerts = "Avvisi"
    override val backgroundAlertsTitle = "Avvisi in background"
    override val backgroundAlertsSubtitle = "Quando cambiano le reti Wi-Fi vicine"

    override val notificationsDenied = "Consenti notifiche per vedere lo stato del monitoraggio e gli avvisi."
    override val checkEvery = "Intervallo di controllo"

    override fun intervalSeconds(seconds: Int) = "${seconds} sec"
    override fun intervalMinutes(minutes: Int) = "${minutes} min"
    override val newNetworkAlertsTitle = "Nuovi avvisi di rete"
    override val signalChangeAlertsTitle = "Avvisi di cambio segnale"
    override val signalChangeAlertsSubtitle = "Quando il segnale aumenta o diminuisce molto"
    override val sectionAbout = "Informazioni"
    override fun aboutApp(appName: String) = "Informazioni su ${appName}"
    override fun version(version: String) = "Versione ${version}"
    override val replayIntroTitle = "Mostra di nuovo l'introduzione"
    override val wallpaperColors = "Colori della carta da parati"
    override val colorIndigo = "Indaco"
    override val colorOrange = "Arancione"
    override val colorAmber = "Ambra"
    override val colorGreen = "Verde"
    override val colorTeal = "Verde acqua"
    override val colorCyan = "Ciano"
    override val colorBlue = "Blu"
    override val colorPurple = "Viola"
    override val colorPink = "Rosa"

    override val tagline = "Trova i punti deboli del Wi-Fi"
    override val openSourceLicense = "Open source · Apache 2.0"
    override val feedbackTitle = "Feedback"
    override val feedbackBody = "Idee e segnalazioni di bug"
    override val elsewhere = "Altri link"

    override val privacySummary = "I risultati restano sul tuo dispositivo, a meno che tu non li condivida."
    override val privacyPolicy = "Informativa sulla privacy"

    override val notNow = "Non ora"
    override val getStarted = "Inizia"
    override val welcomeHeadline = "Trova dove\nil Wi-Fi è debole"

    override val welcomeBody = "Scopri dove il segnale cala e come migliorarlo."
    override val featureRoomsTitle = "Controlla ogni stanza"
    override val featureRoomsBody = "Trova le stanze in cui il segnale è debole."

    override val featureRouterTitle = "Controlla il tuo router"
    override val featureRouterBody = "Guarda quanto velocemente risponde."
    override val featureNearbyTitle = "Visualizza le reti vicine"
    override val featureNearbyBody = "Trova un canale con meno sovrapposizioni."

}
