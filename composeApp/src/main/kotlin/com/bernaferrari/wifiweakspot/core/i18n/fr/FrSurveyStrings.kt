package com.bernaferrari.wifiweakspot.core.i18n.fr

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object FrSurveyStrings : SurveyStrings {
    override val horizontalPosition = "Horizontalement, depuis la gauche"
    override val verticalPosition = "Verticalement, depuis le haut"
    override val measureHere = "Mesurer ici"
    override val recheckReading = "Mesurer à nouveau ici"
    override val moveLeft = "Déplacer à gauche"
    override val moveRight = "Déplacer à droite"
    override val moveUp = "Déplacer vers le haut"
    override val moveDown = "Déplacer vers le bas"
    override fun mapPosition(x: Int, y: Int) = "Position : $x% depuis la gauche, $y% depuis le haut"
    override fun pointName(id: Long) = "Mesure $id"

    override val measuringHoldStill = "Mesurer… rester immobile"
    override val tapWhereYouAreStanding = "Appuyez sur la carte à l'endroit où vous vous trouvez"
    override val chooseWifi = "Choisissez Wi-Fi"
    override val undoLastReading = "Annuler la dernière mesure"

    override val allReadings = "Toutes les mesures"

    override val floorPlanTitle = "Plan du logement"
    override fun readingCount(count: Int) = if (count in 0..1) "${count} mesure" else "${count} mesures"

    override val mapOptions = "Options de la carte"
    override val usePlanPhoto = "Utiliser une photo du plan"

    override val changePhoto = "Changer la photo"
    override val removePhoto = "Supprimer la photo"
    override val exportReadingsCsv = "Exporter les mesures (CSV)"

    override fun floorName(floor: Int) = if (floor == 0) "Sous-sol" else "Étage ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "Étage ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "Ajouter un sous-sol" else "Ajouter un étage ${floor}"
    override val deleteFloorReadingsAction = "Supprimer les relevés de cet étage"
    override fun deleteFloorBody(count: Int) = "Les mesures de cet étage seront supprimées (${count})."

    override val oneRouterAtATime = "Chaque carte mesure un seul point d’accès. Les unités d’un système mesh apparaissent séparément."

    override val noNetworksFound = "Aucun réseau trouvé pour l'instant. Vérifiez que Wi-Fi et l'emplacement sont activés."
    override val scanning = "Analyse…"
    override val refresh = "Actualiser"
    override val switchWifiTitle = "Passer à un autre Wi-Fi ?"
    override val switchWifiBody = "Cela efface les mesures à chaque étage de cette carte. Créez une nouvelle carte pour conserver les deux."
    override val clearAndSwitch = "Effacer et changer"

    override val deleteMapBody = "Toutes les mesures à chaque étage seront supprimées."

    override val noReadingsYet = "Aucune mesure ici pour l’instant. Touchez le plan à l’endroit où vous vous trouvez."

    override val weakestFirst = "Le plus faible en premier, donc les points problématiques sont en haut."
    override val mapYourHome = "Cartographiez votre maison"
    override val mapYourHomeBody = "Visualisez la couverture Wi-Fi sur le plan de votre logement. Utilisez une photo du plan ou une grille vierge."

    override val createMap = "Créer une carte"

    override val namePlaceholder = "Étage, bureau…"

    override val floorplanDescription = "Plan du projet"
    override fun coverageMapDescription(count: Int) =
        "Carte de couverture avec ${readingCount(count)}. Appuyez sur l'endroit où vous vous trouvez pour mesurer."

    override val networkNotSeenHere = "Ce réseau Wi-Fi n’a pas été détecté ici. Aucune mesure n’a été enregistrée."

    override val projectChangedWhileScanning = "La carte a changé pendant l’analyse. Aucun point n’a été enregistré."

    override val couldNotSaveReading = "Impossible d’enregistrer cette mesure. Les autres mesures sont conservées."

    override fun defaultMapName(index: Int) = if (index == 1) "Maison" else "Accueil ${index}"
    override val stillSavingMap = "Enregistrement de la carte en cours. Patientez un instant."

    override val noFreshScan = "Android n’a pas fourni de nouveaux résultats. Patientez un instant, puis réessayez."

    override val couldNotScan = "Impossible de numériser. Vérifiez Wi-Fi et les paramètres de localisation."
    override val chooseAccessPointFirst = "Choisissez un point d'accès avant de cartographier sa couverture."
    override val saveMeasurementBeforeExport = "Enregistrez au moins une mesure avant d'exporter."
    override val couldNotExportProject = "Impossible d'exporter ce projet. Essayer à nouveau."

    override val connectToYourWifi = "Connectez-vous à votre Wi-Fi"
    override fun checkingRoom(room: String) = "Je vérifie ${room}… reste tranquille"
    override val connectedWifi = "Wi-Fi connecté"
    override val connectedNoReading = "Votre appareil est connecté, mais aucune mesure du signal n’est encore disponible."

    override val connectToCheck = "Connectez-vous au Wi-Fi que vous souhaitez vérifier."
    override fun removedRoom(room: String) = "Supprimé ${room}"
    override val undo = "Annuler"
    override val roomsEmptyHint = "Restez immobile dans une pièce et enregistrez une mesure. Mesurez dans une autre pièce pour repérer où le signal faiblit."

    override val noWifiSignal = "Pas de signal Wi-Fi"
    override val saveRoom = "Enregistrer la pièce"
    override fun dbStronger(delta: Int) = "${delta} dB plus fort"
    override fun dbWeaker(delta: Int) = "${delta} dB plus faible"
    override val savedRooms = "Chambres enregistrées"
    override fun deleteRoom(room: String) = "Supprimer ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) = "${room}, signal ${quality}, ${reading}. Consultez les mesures et mesurez à nouveau cet endroit."

    override val noSignalToSave = "Aucun signal Wi-Fi à enregistrer. Connectez-vous d'abord à votre Wi-Fi."
    override val allowScansToLink = "Autorisez les analyses Wi-Fi dans Réseaux pour associer cette mesure à votre réseau."

    override val connectToSpotNetwork = "Connectez-vous au réseau de ce spot avant de le revérifier."
    override fun savedRoom(room: String) = "Enregistré ${room}"
    override val couldNotSaveStableReading = "Impossible d’enregistrer une mesure stable. Restez immobile et connecté au même Wi-Fi, puis réessayez."

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
    override val stepDeveloperOptionsBody = "Dans Paramètres, ouvrez **À propos du téléphone** et touchez **Numéro de build** 7 fois. Une authentification de l’écran de verrouillage peut être demandée."

    override val openAboutPhone = "Ouvrir À propos du téléphone"
    override val stepThrottlingTitle = "Désactivez la limitation de l'analyse Wi-Fi"
    override val stepThrottlingBody = "Dans **Options pour les développeurs**, sous **Mise en réseau**, désactivez **Limiter la recherche Wi‑Fi**. Les noms peuvent varier selon l’appareil."

    override val openDeveloperOptions = "Ouvrir les options du développeur"
    override val stepComeBackTitle = "Reviens ici"
    override val stepComeBackBody = "À votre retour, WiFi Weakspot vérifie automatiquement le réglage. Vous pouvez réactiver la limite à tout moment."

    override val hideComputerMethod = "Masquer la méthode par ordinateur"

    override val useAdbInstead = "Configurer avec ADB sur un ordinateur"

    override val adbCommandClipLabel = "ADB"
    override val hideRootOption = "Masquer l’option root"

    override val useRootInstead = "Configurer avec l’accès root"

    override val scanLimitFootnote = "Android limite les analyses pour économiser la batterie. Vous pouvez réactiver la limite à tout moment."
    override val unlimitedWithRootTitle = "Scans illimités avec root"
    override val unlimitedOnTitle = "Les analyses illimitées sont activées"
    override val unlimitedOnBody = "Les réseaux à proximité et les graphiques des canaux sont actualisés toutes les quelques secondes."
    override val scansLimitedTitle = "Les analyses sont limitées"
    override val scansLimitedAndroid9Body = "Android 9 autorise au maximum 4 analyses toutes les 2 minutes, sans possibilité de désactiver cette limite. En attendant, WiFi Weakspot affiche les résultats les plus récents."

    override val usingRoot = "Utilisation de root"
    override val deviceRooted = "Votre appareil est rooté"
    override val rootScanningBody = "Les analyses passent par le shell du système, il n'y a donc rien à changer dans les options du développeur."
    override val rootDeniedBody = "L’accès root a été refusé. Autorisez WiFi Weakspot dans votre gestionnaire root et réessayez, ou utilisez les options pour les développeurs ci-dessous."

    override val rootNotNeededBody = "Les scans sont déjà illimités. La racine n'est pas nécessaire."
    override val rootOfferBody = "Utilisez root pour analyser les réseaux sans modifier les options pour les développeurs. Votre gestionnaire root demandera une autorisation une seule fois."

    override val stopUsingRoot = "Arrêtez d'utiliser root"
    override val waitingForRootManager = "En attente du gestionnaire root…"

    override val tryRootAgain = "Essayez à nouveau root"
    override val useRoot = "Utilisez root"
    override val copied = "Copié"
    override val copyCommand = "Copier la commande"

}
