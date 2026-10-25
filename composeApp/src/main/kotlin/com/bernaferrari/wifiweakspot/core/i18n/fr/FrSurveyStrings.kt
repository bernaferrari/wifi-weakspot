package com.bernaferrari.wifiweakspot.core.i18n.fr

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object FrSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "Mesurer… rester immobile"
    override val tapWhereYouAreStanding = "Appuyez sur la carte à l'endroit où vous vous trouvez"
    override val chooseWifi = "Choisissez Wi-Fi"
    override val undoLastReading = "Annuler la dernière lecture"
    override val allReadings = "Toutes les lectures"
    override val floorPlanTitle = "Plan du logement"
    override fun readingCount(count: Int) = if (count == 1) "1 lecture" else "${count} lectures"
    override val mapOptions = "Options de la carte"
    override val usePlanPhoto = "Utilisez une photo de votre forfait"
    override val changePhoto = "Changer la photo"
    override val removePhoto = "Supprimer la photo"
    override val exportReadingsCsv = "Exporter les lectures (CSV)"
    override fun floorName(floor: Int) = if (floor == 0) "Sous-sol" else "Étage ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "Étage ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "Ajouter un sous-sol" else "Ajouter un étage ${floor}"
    override val deleteFloorReadingsAction = "Supprimer les relevés de cet étage"
    override fun deleteFloorBody(count: Int) =
        if (count == 1) "Sa 1re lecture sera supprimée." else "Ses lectures ${count} seront supprimées."

    override val oneRouterAtATime = "Une carte suit un routeur à la fois. Les systèmes de maillage montrent chaque unité séparément."
    override val noNetworksFound = "Aucun réseau trouvé pour l'instant. Vérifiez que Wi-Fi et l'emplacement sont activés."
    override val scanning = "Analyse…"
    override val refresh = "Actualiser"
    override val switchWifiTitle = "Passer à un autre Wi-Fi ?"
    override val switchWifiBody = "Cela efface les lectures à chaque étage de cette carte. Créez une nouvelle carte pour conserver les deux."
    override val clearAndSwitch = "Effacer et changer"

    override val deleteMapBody = "Toutes les lectures à chaque étage seront supprimées."

    override val noReadingsYet = "Aucune lecture ici pour l'instant. Appuyez sur la carte à l'endroit où vous vous trouvez."
    override val weakestFirst = "Le plus faible en premier, donc les points problématiques sont en haut."
    override val mapYourHome = "Cartographiez votre maison"
    override val mapYourHomeBody = "Visualisez votre Wi-Fi sur une carte de votre maison. Utilisez une photo de votre plan ou une grille vierge."
    override val createMap = "Créer une carte"

    override val namePlaceholder = "A l'étage, Bureau…"

    override val floorplanDescription = "Plan du projet"
    override fun coverageMapDescription(count: Int) =
        "Carte de couverture avec ${readingCount(count)}. Appuyez sur l'endroit où vous vous trouvez pour mesurer."

    override val networkNotSeenHere = "Ce Wi-Fi n'est pas apparu ici. Rien n'a été sauvegardé."
    override val projectChangedWhileScanning = "Le projet a changé lors de la numérisation. Aucun point n'a été économisé."
    override val couldNotSaveReading = "Impossible de sauvegarder cette lecture. Vos autres lectures sont en sécurité."
    override fun defaultMapName(index: Int) = if (index == 1) "Maison" else "Accueil ${index}"
    override val stillSavingMap = "Un instant, toujours en train de sauvegarder la carte."
    override val noFreshScan = "Android n'a pas fourni de nouvelle analyse. Réessayez sous peu."
    override val couldNotScan = "Impossible de numériser. Vérifiez Wi-Fi et les paramètres de localisation."
    override val chooseAccessPointFirst = "Choisissez un point d'accès avant de cartographier sa couverture."
    override val saveMeasurementBeforeExport = "Enregistrez au moins une mesure avant d'exporter."
    override val couldNotExportProject = "Impossible d'exporter ce projet. Essayer à nouveau."

    override val connectToYourWifi = "Connectez-vous à votre Wi-Fi"
    override fun checkingRoom(room: String) = "Je vérifie ${room}… reste tranquille"
    override val connectedWifi = "Wi-Fi connecté"
    override val connectedNoReading = "Votre appareil est connecté, mais aucune lecture de signal n'est encore disponible."
    override val connectToCheck = "Connectez-vous au Wi-Fi que vous souhaitez vérifier."
    override fun removedRoom(room: String) = "Supprimé ${room}"
    override val undo = "Annuler"
    override val roomsEmptyHint =
        "Placez-vous dans une pièce et enregistrez une lecture. Vérifiez une autre pièce pour trouver où le signal devient plus faible."
    override val noWifiSignal = "Pas de signal Wi-Fi"
    override val saveRoom = "Enregistrer la pièce"
    override fun dbStronger(delta: Int) = "${delta} dB plus fort"
    override fun dbWeaker(delta: Int) = "${delta} dB plus faible"
    override val savedRooms = "Chambres enregistrées"
    override fun deleteRoom(room: String) = "Supprimer ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) =
        "${room}, signal ${quality}, ${reading}. Passez en revue les lectures et revérifiez cet endroit."

    override val noSignalToSave = "Aucun signal Wi-Fi à enregistrer. Connectez-vous d'abord à votre Wi-Fi."
    override val allowScansToLink = "Autorisez les analyses Wi-Fi dans les réseaux afin que cette lecture puisse être liée à votre réseau."
    override val connectToSpotNetwork = "Connectez-vous au réseau de ce spot avant de le revérifier."
    override fun savedRoom(room: String) = "Enregistré ${room}"
    override val couldNotSaveStableReading = "Impossible d'enregistrer une lecture stable. Restez toujours sur le même Wi-Fi et réessayez."
    override val whereAreYou = "Où êtes-vous ?"
    override val somewhereElse = "Ailleurs"

    override fun savedAt(time: String) = "Enregistré ${time}"
    override val moveRouterTip = "Essayez de déplacer le routeur plus haut, plus central ou plus près de cette pièce."
    override val betterThanLastTime = "Mieux que la dernière fois"
    override val worseThanLastTime = "Pire que la dernière fois"
    override val sameAsLastTime = "Comme la dernière fois"
    override val earlierChecks = "Contrôles antérieurs"
    override val remove = "Supprimer"
    override fun connectToRecheck(ssid: String?) =
        "Connectez-vous à ${ssid?.ifBlank { null } ?: "pareil Wi-Fi"} pour vérifier à nouveau."

    override val stepDeveloperOptionsTitle = "Activez les options du développeur"
    override val stepDeveloperOptionsBody =
        "Dans Paramètres, ouvrez **À propos du téléphone** et appuyez sept fois sur **Numéro de build**. Vous devrez peut-être saisir votre verrouillage d'écran."
    override val openAboutPhone = "Ouvrir À propos du téléphone"
    override val stepThrottlingTitle = "Désactivez la limitation de l'analyse Wi-Fi"
    override val stepThrottlingBody =
        "Dans **Options pour les développeurs**, faites défiler jusqu'à **Réseau** et désactivez **Wi-Fi la limitation de l'analyse**."
    override val openDeveloperOptions = "Ouvrir les options du développeur"
    override val stepComeBackTitle = "Reviens ici"
    override val stepComeBackBody =
        "WiFi Weakspot vérifie automatiquement à votre retour. Vous pouvez réactiver la limitation à tout moment."
    override val hideComputerMethod = "Masquer la méthode informatique"
    override val useAdbInstead = "Vous préférez un ordinateur ? Utilisez ADB"
    override val adbCommandClipLabel = "ADB"
    override val hideRootOption = "Masquer l'option root"
    override val useRootInstead = "Appareil rooté ? Utilisez plutôt root"
    override val scanLimitFootnote = "Android limite les analyses pour économiser la batterie. Vous pouvez réactiver la limite à tout moment."
    override val unlimitedWithRootTitle = "Scans illimités avec root"
    override val unlimitedOnTitle = "Les analyses illimitées sont activées"
    override val unlimitedOnBody = "Les réseaux à proximité et les graphiques des canaux sont actualisés toutes les quelques secondes."
    override val scansLimitedTitle = "Les analyses sont limitées"
    override val scansLimitedAndroid9Body =
        "Android 9 autorise 4 analyses toutes les 2 minutes et ne peut pas la désactiver. WiFi Weakspot réutilise les résultats récents entre les deux."
    override val usingRoot = "Utilisation de root"
    override val deviceRooted = "Votre appareil est rooté"
    override val rootScanningBody = "Les analyses passent par le shell du système, il n'y a donc rien à changer dans les options du développeur."
    override val rootDeniedBody =
        "L'accès root a été refusé. Autorisez WiFi Weakspot dans votre gestionnaire root et réessayez, ou utilisez les options de développement ci-dessous."
    override val rootNotNeededBody = "Les scans sont déjà illimités. La racine n'est pas nécessaire."
    override val rootOfferBody =
        "Ignorez les options du développeur et laissez WiFi Weakspot parcourir root. Votre manager root vous le demandera une fois."
    override val stopUsingRoot = "Arrêtez d'utiliser root"
    override val waitingForRootManager = "En attente de votre manager root…"
    override val tryRootAgain = "Essayez à nouveau root"
    override val useRoot = "Utilisez root"
    override val copied = "Copié"
    override val copyCommand = "Commande Copier"
}
