package com.bernaferrari.wifiweakspot.core.i18n.fr

import com.bernaferrari.wifiweakspot.core.i18n.SettingsStrings

object FrSettingsStrings : SettingsStrings {
    override val sectionBackup = "Sauvegarde"
    override val backupScope = "Enregistrez les cartes, les images des plans et les mesures des pièces dans le fichier de votre choix. Les réglages et l’historique des diagnostics sont exclus."
    override val exportBackup = "Enregistrer une sauvegarde"
    override val restoreBackup = "Restaurer une sauvegarde"
    override val restoreBackupExplanation = "Choisissez une sauvegarde WiFi Weakspot. Les cartes sont ajoutées sous forme de copies et les mesures existantes sont conservées. Votre travail actuel ne sera pas remplacé."
    override val backupSaved = "Sauvegarde enregistrée."
    override val backupRestored = "Sauvegarde restaurée."
    override val backupAlreadyRestored = "Cette sauvegarde a déjà été restaurée."
    override val backupExportFailed = "Impossible d’enregistrer la sauvegarde. Vérifiez l’espace libre et réessayez."
    override val backupRestoreFailed = "La restauration n’a pas pu se terminer. Vérifiez le fichier et l’espace libre, puis réessayez avec le même fichier."

    override val sectionScanning = "Numérisation"
    override val fasterScansThrottled = "Android permet 4 scans toutes les 2 minutes"
    override val sectionAppearance = "Apparence"
    override val themeAuto = "Auto"
    override val themeLight = "Clair"
    override val themeDark = "Sombre"
    override val colorStyle = "Style de couleur"
    override val styleExpressive = "Expressif"
    override val styleVibrant = "Vif"
    override val styleTonal = "Tonal"
    override val styleCmf = "CMF"
    override val showSignalAs = "Afficher le signal en"
    override val sectionComfort = "Confort"
    override val reduceMotionTitle = "Réduire les animations"

    override val hapticsSubtitle = "Vibrer au toucher et à l’affichage des résultats"

    override val sectionAlerts = "Alertes"
    override val backgroundAlertsTitle = "Alertes en arrière-plan"
    override val backgroundAlertsSubtitle = "Quand les réseaux Wi-Fi à proximité changent"

    override val notificationsDenied = "Autorisez les notifications à voir l’état de surveillance et les alertes."
    override val checkEvery = "Intervalle de vérification"

    override fun intervalSeconds(seconds: Int) = "${seconds} seconde"
    override fun intervalMinutes(minutes: Int) = "${minutes} minute"
    override val newNetworkAlertsTitle = "Nouvelles alertes réseau"
    override val signalChangeAlertsTitle = "Alertes de changement de signal"
    override val signalChangeAlertsSubtitle = "Lorsque votre signal monte ou descend beaucoup"
    override val sectionAbout = "À propos"
    override fun aboutApp(appName: String) = "À propos de ${appName}"
    override fun version(version: String) = "Version ${version}"
    override val replayIntroTitle = "Afficher à nouveau l'introduction"
    override val wallpaperColors = "Couleurs du papier peint"
    override val colorIndigo = "Indigo"
    override val colorOrange = "Orange"
    override val colorAmber = "Ambre"
    override val colorGreen = "Vert"
    override val colorTeal = "Bleu canard"

    override val colorCyan = "Cyan"
    override val colorBlue = "Bleu"
    override val colorPurple = "Violet"
    override val colorPink = "Rose"

    override val tagline = "Repérez les zones de faible signal Wi-Fi"
    override val openSourceLicense = "Open source · Apache 2.0"
    override val feedbackTitle = "Retour"
    override val feedbackBody = "Idées et rapports de bugs"
    override val elsewhere = "Autres liens"

    override val privacySummary = "Vos résultats restent sur votre appareil, sauf si vous les partagez."
    override val privacyPolicy = "Politique de confidentialité"

    override val notNow = "Pas maintenant"
    override val getStarted = "Commencer"
    override val welcomeHeadline = "Où le Wi-Fi\npasse mal ?"

    override val welcomeBody = "Voyez où le signal faiblit et comment l'améliorer."
    override val featureRoomsTitle = "Vérifiez chaque pièce"
    override val featureRoomsBody = "Repérez les pièces où le signal est faible."

    override val featureRouterTitle = "Vérifiez votre routeur"
    override val featureRouterBody = "Voyez à quelle vitesse il répond."
    override val featureNearbyTitle = "Voir les réseaux à proximité"
    override val featureNearbyBody = "Trouvez un canal avec moins de chevauchement."

}
