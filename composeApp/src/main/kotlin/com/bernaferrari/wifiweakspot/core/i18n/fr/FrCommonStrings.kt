package com.bernaferrari.wifiweakspot.core.i18n.fr

import com.bernaferrari.wifiweakspot.core.i18n.CommonStrings

object FrCommonStrings : CommonStrings {
    override fun deleteItemTitle(name: String) = "Supprimer ${name} ?"

    override val couldNotSaveChanges = "Impossible d’enregistrer les modifications. Réessayez."
    override val couldNotDeleteItem = "Impossible de supprimer cet élément. Réessayez."

    override val hiddenNetworkShort = "Caché"
    override val network = "Réseau"
    override val tools = "Outils"
    override val maker = "Fabricant"
    override val noReply = "Pas de réponse"
    override val fastest = "Le plus rapide"
    override val address = "Adresse"
    override val responseTime = "Temps de réponse"
    override val checkAgain = "Vérifier à nouveau"

    override val rename = "Renommer"
    override val stop = "Arrêter"
    override val unknown = "Inconnu"

    override val hiddenNetwork = "Réseau masqué"
    override val fasterScans = "Analyses plus rapides"
    override val openWifiSettings = "Ouvrir les paramètres Wi-Fi"
    override val signalUnavailable = "Signal indisponible"
    override val devices = "Appareils"
    override val openPorts = "Ports ouverts"
    override val nameLabel = "Nom"
    override val historyTitle = "Historique"
    override val routerCheck = "Tester le routeur"
    override val testWebsite = "Tester un site web"
    override val yourRouter = "Votre routeur"
    override val yourNetwork = "Votre réseau"

    override val openInNewTab = "Ouvrir dans un nouvel onglet"
    override val mapSharingAndroidOnly = "Le partage de la carte fonctionne dans l'application Android."
    override val back = "Retour"
    override val close = "Fermer"
    override val cancel = "Annuler"
    override val save = "Enregistrer"
    override val done = "Terminé"
    override val share = "Partager"
    override val delete = "Supprimer"

    override val tabHome = "Accueil"
    override val tabNetworks = "Réseaux"
    override val tabRooms = "Pièces"
    override val tabSettings = "Paramètres"

    override val permissionScreenTitle = "Autoriser l’accès aux réseaux Wi-Fi à proximité"

    override val permissionAllowAccess = "Autoriser l'accès"
    override val openAppSettings = "Ouvrir les paramètres de l'application"

    override fun signalBadgeDescription(quality: String, reading: String) = "Signal ${quality}, ${reading}"

    override val scanReady = "PRÊT"
    override fun scansCount(count: Int) = if (count == 1) "1 analyse" else "${count} scans"
    override val scansLeftCaption = "restants"
    override val scanWaitingCaption = "en attente"
    override val scanDevMode = "MODE DÉVELOPPEMENT"
    override val readyToScan = "Prêt à numériser"
    override fun scansRemaining(count: Int) = if (count == 1) "1 analyse restante" else "${count} scans restants"
    override fun nextScanIn(time: String) = "Prochaine analyse dans ${time}"
    override val checking = "Vérification…"

    override val justNow = "tout à l'heure"
    override fun minutesAgo(minutes: Long) = "${minutes} il y a min"
    override fun hoursAgo(hours: Long) = "${hours} il y a h"
    override fun daysAgo(days: Long) = "${days} j"
}
