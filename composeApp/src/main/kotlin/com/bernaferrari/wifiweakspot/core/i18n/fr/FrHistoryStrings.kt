package com.bernaferrari.wifiweakspot.core.i18n.fr

import com.bernaferrari.wifiweakspot.core.i18n.HistoryStrings

object FrHistoryStrings : HistoryStrings {
    override val today = "Aujourd'hui"
    override val yesterday = "Hier"
    override val weekdayNames = listOf("lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche")
    override val weekdayShortNames = listOf("lun.", "mar.", "mer.", "jeu.", "ven.", "sam.", "dim.")
    override val monthShortNames = listOf("janv.", "févr.", "mars", "avr.", "mai", "juin", "juil.", "août", "sept.", "oct.", "nov.", "déc.")
    override fun dateThisYear(weekday: String, day: Int, month: String) = "${weekday} ${day} ${month}"

    override fun dateOtherYear(weekday: String, day: Int, month: String, year: Int) = "${weekday} ${day} ${month} ${year}"

    override fun momentAt(day: String, time: String) = "${day} à ${time}"

    override val filterAll = "Tous"
    override val kindInternetTests = "Tests Internet"
    override val kindScans = "Numérisations"
    override val emptyTitle = "Rien n'a encore été enregistré"
    override val emptySubtitle = "Les vérifications du routeur, les analyses du réseau et les résultats des outils s'affichent ici, afin que vous puissiez voir comment votre Wi-Fi évolue au fil du temps."
    override fun trendBarsDescription(count: Int, readings: String) = "Derniers ${count} tests de routeur : ${readings}"
    override val trendNoReply = "Aucune réponse du routeur. Certains routeurs ignorent ces vérifications."
    override val trendRecheckToCompare = "Revérifiez après un changement pour comparer."
    override fun trendQuicker(usualMs: String) = "Plus rapide que d'habitude (${usualMs} ms)"
    override fun trendSlower(usualMs: String) = "Plus lent que d'habitude (${usualMs} ms)"
    override fun trendAboutSame(usualMs: String) = "À peu près la même chose que d'habitude (${usualMs} ms)"

    override fun internetTitle(mbps: String) = "${mbps} Mbps télécharger"
    override fun routerTitle(ms: String) = "${ms} ms au routeur"
    override fun networksNearby(count: Int) = if (count == 1) "1 réseau à proximité" else "${count} réseaux à proximité"
    override fun strongestNetwork(network: String) = "Le plus fort : ${network}"
    override fun accessPoints(count: Int) = if (count == 1) "1 point d'accès" else "${count} points d'accès"
    override fun devicesFound(count: Int) = if (count == 1) "1 appareil trouvé" else "${count} appareils trouvés"
    override fun listWithMore(list: String, more: Int) = if (more > 0) "${list} et ${more} plus" else list
    override fun pingSummary(averageMs: String, replied: Int, sent: Int) = "${averageMs} ms · ${replied} réponses sur ${sent}"

    override fun lookupTitle(target: String) = "Recherche DNS : ${target}"

    override val noRecords = "Aucun enregistrement"
    override val lookupFailed = "La recherche a échoué"
    override fun openPortsTitle(target: String) = "Ouvrir les ports sur ${target}"
    override val noCommonPortsOpen = "Aucun des ports communs n'était ouvert"
    override val completed = "Complété"
    override val failed = "Échoué"

    override val metricDownload = "Télécharger"
    override val metricUpload = "Télécharger"
    override val metricLatency = "Latence"
    override val metricVariation = "Variante"
    override val metricLost = "Perdu"
    override fun metricOnBand(band: String) = "sur ${band}"
    override val metricReplied = "a répondu"
    override val metricReplies = "Réponses"
    override val metricAverage = "Moyenne"
    override val factServer = "Serveur"
    override val factRoom = "Chambre"
    override val factBand = "Groupe"
    override val factSignal = "Signal"
    override val factAccessPoints = "Points d'accès"
    override fun signalFact(quality: String, rssi: Int) = "${quality} signal · ${rssi} dBm"

    override val linesNetworksStrongestFirst = "Les réseaux, les plus forts en premier"
    override val linesRecords = "Enregistrements"
    override fun replyNumber(index: Int) = "Répondre ${index}"
    override val scanNote = "Les réseaux à proximité sur la même chaîne que la vôtre se disputent le temps d'antenne."

    override val go = "ALLER"
    override val testInternetSpeedInstead = "Testez plutôt la vitesse Internet"
    override val keepDeviceStill = "Gardez votre appareil immobile pendant quelques secondes."
    override val connectToStart = "Connectez-vous à votre Wi-Fi pour commencer."
    override val lastTime = "Dernière fois"
    override val couldNotSaveSpot = "Impossible de sauvegarder cet emplacement. Essayer à nouveau."
    override val connectFirst = "Connectez-vous d'abord à votre Wi-Fi. Cette vérification mesure les réponses de votre routeur."
    override val historyNotSaved = "Résultat mesuré, mais l'historique n'a pas pu être enregistré."
    override val checkStopped = "La connexion a changé ou la vérification du routeur s'est arrêtée. Gardez ce Wi-Fi connecté et réessayez. Certains routeurs bloquent le ping."

    override val internetSpeedTitle = "Vitesse Internet · fast.com"
}
