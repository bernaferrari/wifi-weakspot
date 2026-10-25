package com.bernaferrari.wifiweakspot.core.i18n.fr

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object FrAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = if (count == 1) "1 réseau" else "${count} réseaux"

    override fun channelShort(channel: Int) = "Can. ${channel}"

    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · ${channelShort(channel)} · ${band}"

    override val noPassword = "Sans mot de passe"
    override val wifiOffTitle = "Wi-Fi est éteint"
    override val locationOffBody = "Android exige que la localisation soit activée pour afficher les réseaux Wi-Fi à proximité. WiFi Weakspot n'utilise pas le GPS."

    override val scanPermissionBody = "Android ne partage les noms de réseaux à proximité qu'avec les applications ayant accès à la localisation. WiFi Weakspot n'enregistre jamais où vous êtes."
    override val wifiOffBody = "Activez Wi-Fi pour voir les réseaux à proximité."
    override val locationOffTitle = "L'emplacement est désactivé"
    override val turnOnLocation = "Activer la localisation"
    override val scanPausedTitle = "Android analyse en pause"
    override fun scanPausedBody(seconds: Long) = "Le prochain scan est autorisé dans ${seconds} s. Désactivez la limitation de l'analyse pour éviter l'attente."
    override val turnOffThrottling = "Désactiver la limitation"
    override val readyToScanBody = "Appuyez sur Scan pour voir tous les réseaux à proximité."
    override val scan = "Analyser"
    override val channels = "Canaux"
    override val signalOverTime = "Évolution du signal"
    override val howToReadThisChart = "Comment lire ce graphique"
    override val expandSignalHistory = "Développer l'historique des signaux"
    override val expandChannels = "Développer les chaînes"
    override fun noBandNetworksNearby(band: String) = "Aucun réseau ${band} à proximité."
    override fun networksOnBand(count: Int, band: String) =
        if (count == 0) "Aucun réseau sur ${band}" else "${networks(count)} sur ${band}"
    override fun showVeryWeakNetworks(count: Int) =
        if (count == 1) "Afficher 1 réseau très faible" else "Afficher ${count} réseaux très faibles"
    override fun hideVeryWeakNetworks(count: Int) =
        if (count == 1) "Cacher 1 réseau très faible" else "Masquer ${count} réseaux très faibles"
    override fun bandNetworkCount(count: Int) = if (count == 0) "aucun" else networks(count)
    override val highlightedOnChart = "Mis en évidence sur le graphique"
    override val notHighlightedOnChart = "Non mis en évidence sur le graphique"
    override fun detailsForNetwork(ssid: String) = "Détails pour ${ssid.ifEmpty { "réseau caché" }}"
    override fun alsoYourRouter(quality: String) = "${quality} · Également votre routeur"
    override val connected = "Connecté"

    override val scanReportHeading = "WiFi Weakspot Rapport d'analyse"
    override fun scanReportTime(time: String) = "Heure : ${time}"
    override fun scanReportTotal(count: Int) = "Réseaux totaux : ${count}"
    override val scanReportNetworksHeading = "Réseaux :"
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) = "• ${ssid} - ${rssi} dBm, ${channelShort(channel)}, ${security}"

    override val channelLooksClear = "Votre canal est peu encombré"
    override val noChannelImprovement = "Aucun meilleur canal trouvé"
    override fun lessOverlapThanChannel(currentChannel: Int) = "Moins encombré que le canal ${currentChannel}"
    override val suggestedChannel = "Canal conseillé"
    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice} : canal ${channel}"
    override val lowOverlap = "Faible chevauchement"
    override val moderateOverlap = "Chevauchement modéré"
    override val highOverlap = "Chevauchement élevé"
    override val veryHighOverlap = "Chevauchement très élevé"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) =
        "Graphique de canal, ${networks(networkCount)}." +
            (if (busiestChannel != null) " Chaîne la plus fréquentée ${busiestChannel} avec ${busiestCount}." else "")
    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) = name + (if (isYou) " (${connected})" else "") + (if (extraNames > 0) " +${extraNames}" else "")

    override val busyCaption = "Chargé"
    override val axisStrong = "Fort"
    override val axisOkay = "Moyen"

    override val axisWeak = "Faible"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · ${channelShort(channel)}"

    override val clearSelectedNetwork = "Effacer le réseau sélectionné"
    override val tapWaveForDetails = "Appuyez sur une vague pour les détails du réseau"

    override val chartGuideTitle = "Lecture des cartes"
    override val guideTallerBody = "La hauteur est la force du signal."
    override val guideOverlapBody = "Les réseaux qui se chevauchent partagent le spectre et peuvent rivaliser pour le temps d'antenne."
    override val guideWiderBody = "Un canal plus large occupe plus de spectre et peut chevaucher davantage de voisins."
    override val guideSameRouterBody = "+1 : Les noms de réseaux qui semblent utiliser le même module Wi-Fi partagent une courbe."

    override val guideCrowdBody = "Le vert indique moins de chevauchement ; le rouge, davantage. La bande sous 2.4 GHz montre un chevauchement. Appuyez dessus pour compter."
    override val guideSteadyBody = "Une courbe qui monte et descend indique des variations du signal. Touchez-la pour mettre en pause, puis glissez horizontalement pour changer d’instant ou verticalement pour changer de réseau. Reprenez le direct pour voir les nouvelles mesures."

    override val guideHighlightBody = "Appuyez sur une colline ou une ligne pour la mettre en surbrillance. Faites glisser horizontalement sur le graphique pour l'explorer."

    override fun channelOverlapHeading(count: Int, channel: Int) = when (count) {
        0 -> "Le canal ${channel} est clair"
        1 -> "1 réseau chevauche le canal ${channel}"
        else -> "${count} les réseaux chevauchent le canal ${channel}"
    }
    override fun fromChannel(homeChannel: Int, reading: String) = "Depuis la chaîne ${homeChannel} · ${reading}"

    override val estimatedChannelOverlap = "Chevauchement estimé des canaux"
    override fun tryChannel(channel: Int) = "Essayez la chaîne ${channel}"
    override val lowestOverlapFor20Mhz = "Chevauchement estimé le plus bas pour 20 MHz chaînes"
    override val overlapDisclaimer = "L’estimation repose sur les points d’accès proches et la puissance du signal, pas sur le trafic mesuré. Avant de changer les réglages, vérifiez les canaux pris en charge par votre routeur et les règles locales. Les canaux 5 et 6 GHz ne sont suggérés que si la compatibilité du routeur est connue. Si la fréquence centrale d’un canal large est inconnue, seul son canal principal de 20 MHz est affiché."

    override val overlapScore = "Score de chevauchement"

    override val collectingReadings = "Collecte des mesures… Les courbes apparaîtront après quelques analyses."

    override fun signalHistoryDescription(networkCount: Int) =
        "Historique des signaux pour ${networks(networkCount)}. Appuyez sur une ligne à inspecter. Les touches fléchées se déplacent dans le temps et les réseaux."
    override val historyLive = "En direct"
    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) = "${networkName}, ${reading ?: "aucune mesure"}, en pause, ${readingsAgo(readingsAgo)}"

    override val strongerNetwork = "Réseau plus fort"
    override val weakerNetwork = "Réseau plus faible"
    override val axisOlder = "ancien"
    override val axisLatest = "récent"
    override val exploreSignalHistory = "Explorez l'historique des signaux"
    override val tapLineToCompare = "Appuyez sur une ligne, puis faites-la glisser pour comparer"
    override val noReadingAtPoint = "Aucune mesure à cet instant"

    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) = "${reading} · ${quality} · " + if (readingsAgo == 0) "dernière mesure" else readingsAgo(readingsAgo)

    private fun readingsAgo(count: Int) = if (count == 1) "il y a 1 mesure" else "il y a $count mesures"

    override val resumeLive = "Reprendre le direct"

    override fun seenReading(rssi: Int, seen: String) = "${rssi} dBm · vu ${seen}"
    override val factChannel = "Canal"
    override val factWidth = "Largeur"
    override val factSecurity = "Sécurité"
    override val factDistance = "Estimation de distance"
    override val notAvailable = "Non disponible"
    override val watch = "Regarder"
    override val addToFavorites = "Ajouter aux favoris"
    override val removeFromFavorites = "Supprimer des favoris"

    override fun routerCheckLastTime(pingMs: String) = "${pingMs} ms la dernière fois"
    override fun devicesFoundLastScan(count: Int) = "${count} trouvé lors de la dernière analyse"
    override val devicesSubtitle = "Qui est connecté"
    override val testWebsiteSubtitle = "Ping, DNS, ports"
    override fun savedChecks(count: Int) = when (count) {
        0 -> "Résultats passés"
        1 -> "1 chèque"
        else -> "${count} chèques"
    }
    override val notOnWifi = "Pas sur Wi-Fi"
    override val online = "En ligne"
    override val internetNotConfirmed = "Internet non confirmé"
    override fun channelChip(channel: Int) = "Chaîne ${channel}"
    override fun linkRateChip(mbps: Int) = "Taux de liaison ${mbps} Mbps"
    override val chooseNetwork = "Choisissez un réseau"
    override val turnOnWifi = "Allumez Wi-Fi"
    override val dismiss = "Rejeter"
    override val findWeakSpotsTitle = "Trouver les points faibles"
    override val findWeakSpotsBody = "Repérez les pièces où le signal faiblit."

    override val findWeakSpotsAction = "Vérifiez vos chambres"
    override fun networksNearby(count: Int) = "${networks(count)} à proximité"
    override val nearbyStale = "De votre dernière analyse. Appuyez pour actualiser."
    override val nearbyNoConnection = "Voir les canaux et le signal pour chaque réseau."
    override fun nearbyOverlap(count: Int, channel: Int) = when (count) {
        0 -> "Aucun chevauchement à proximité détecté sur le canal ${channel}."
        1 -> "1 signal à proximité chevauche votre chaîne ${channel}."
        else -> "${count} les signaux à proximité chevauchent votre chaîne ${channel}."
    }

    override val adviceNoWifiBody = "Connectez-vous au réseau que vous souhaitez vérifier. Vous pouvez toujours exécuter des tests Internet et des outils réseau en utilisant votre connexion actuelle."
    override val adviceNoInternetTitle = "Vérifiez d'abord l'accès à Internet"
    override val adviceNoInternetBody = "Android n'a pas confirmé l'accès à Internet sur cette connexion Wi-Fi. Recherchez une page de connexion, puis testez la connectivité à votre routeur et à un hébergeur Internet."
    override val adviceNoSignalTitle = "Lisez votre signal Wi-Fi"
    override val adviceNoSignalBody = "Autorisez les analyses Wi-Fi dans les réseaux pour voir les mesures du signal. Comparez le signal dans la pièce où vous remarquez des problèmes."
    override val adviceWeakSignalTitle = "Comparez la couverture à proximité de votre routeur"
    override val adviceWeakSignalBody = "Le signal Wi-Fi est faible. Testez la réponse du routeur ici, puis à proximité de celui-ci. Comparez les mesures de votre propre réseau pour repérer les zones faibles."

    override val adviceGoodSignalTitle = "Comparez la réactivité du routeur"
    override val adviceGoodSignalBody = "La force du signal semble utilisable et Android signale l'accès à Internet. Si la connexion semble lente, comparez la réponse du routeur ici et à proximité du routeur ; un signal fort à lui seul ne garantit pas les performances."
    override val channelAdviceBasis = "Les barres plus courtes indiquent moins de chevauchement dans cette analyse. La vitesse peut varier."
    override fun channelChangeInstructions(channel: Int, band: String) = "Choisissez le canal ${channel} pour ${band}"

    override val channelOverlapTitle = "Chevauchement des réseaux voisins"
    override val channelApplyChange = "Enregistrez et reconnectez-vous si nécessaire"
    override val channelChangeNote = "Effectuez ce changement dans l’application ou les paramètres du routeur, pas dans WiFi Weakspot."

}
