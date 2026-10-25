package com.bernaferrari.wifiweakspot.core.i18n.fr

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object FrToolsStrings : ToolsStrings {
    override val tryAgain = "Réessayez"

    override val targetPlaceholder = "Site Web ou adresse IP"
    override val clearTarget = "Clair"
    override val checkSectionTitle = "Vérifier"
    override val recentSectionTitle = "Récent"

    override val toolPortsDescription = "Ce qui est ouvert"

    override val pingQuestion = "Est-ce que ça répond ?"
    override val dnsQuestion = "Quelle est son adresse ?"
    override val dnsDetail = "DNS recherche"
    override val portsQuestion = "Que propose-t-il ?"

    override val runningPortScanTitle = "Vérification des ports"

    override val diagnosticFailed = "Échec du diagnostic"
    override val invalidTarget = "Saisissez un nom d'hôte ou une adresse IP sans chemin d'URL ni espaces."
    override val diagnosticCouldNotComplete = "Le diagnostic n'a pas pu être effectué."
    override val connectionChangedRunAgain = "La connexion a changé. Exécutez à nouveau cette vérification sur le nouveau réseau."

    override val openItsWebPage = "Ouvrir sa page web"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ${answered} réponses sur ${total}"

    override val slowest = "Le plus lent"
    override fun repliesContentDescription(replies: String) = "Réponses : ${replies}"

    override val pingDidntAnswer = "Je n'ai pas répondu"
    override val pingResponsive = "Réactif"
    override val pingResponsiveButUneven = "Réactif, mais inégal"
    override val pingDroppingReplies = "Supprimer les réponses"
    override val pingSlowToAnswer = "Lent à répondre"

    override val pingNoAnswerLocal = "Il peut être éteint, endormi ou configuré pour ignorer les pings."
    override fun pingRepliesLost(lost: Int, total: Int) =
        "${lost} des ${total} réponses ont disparu. Comparez une vérification du routeur avec un hébergeur Internet pour vous aider à localiser le problème ; ce test à lui seul ne peut pas identifier la cause."
    override val pingUneven = "Les délais de réponse variaient. Comparez une vérification de routeur ici et à proximité du routeur, puis testez un hébergeur Internet."
    override val pingLocalExplanation = "Il est temps d'atteindre cet appareil et de revenir. L'appareil lui-même peut affecter le résultat."
    override val pingInternetExplanation = "Il est temps de joindre cet hôte et de revenir. Votre connexion, l'itinéraire et l'hôte affectent tous le résultat."

    override fun dnsAddressUnit(count: Int) = if (count == 1) "adresse" else "adresses"

    override fun dnsForQuery(query: String) = "Pour ${query}"
    override val dnsNotFound = "Je n'ai pas pu le trouver"
    override val dnsFound = "Je l'ai trouvé"
    override val dnsNotFoundExplanation = "Vérifiez l'orthographe ou réessayez ; le nom n'existe peut-être pas."
    override fun dnsFoundExplanation(query: String) = "Votre appareil peut trouver ${query}."
    override val dnsAddressesTitle = "Adresses"

    override val portsOpenUnit = "ouvert"
    override fun portsCommonServicesOn(host: String) = "Services communs sur ${host}"
    override val portsNothingOpen = "Rien d'ouvert"
    override fun portsOffersServices(count: Int) = if (count == 1) "Il propose 1 service" else "Il propose ${count} services"
    override val portsNoneAnswered = "Aucun service commun n'a répondu."
    override val portsOffersExplanation = "Des éléments comme une page de paramètres ou le partage de fichiers."

    override val portWebPage = "Page Web"
    override val portSecureWebPage = "Page web sécurisée"
    override val portRemoteLoginSsh = "Connexion à distance (SSH)"
    override val portOldRemoteLoginTelnet = "Ancienne connexion à distance (Telnet)"
    override val portFileTransferFtp = "Transfert de fichiers (FTP)"
    override val portAddressLookupsDns = "Recherches d'adresses (DNS)"
    override val portFileSharing = "Partage de fichiers"
    override val portMacFileSharing = "Partage de fichiers Mac"
    override val portPrinting = "Impression"
    override val portRemoteDesktop = "Bureau à distance"
    override val portScreenSharing = "Partage d'écran"
    override val portDeviceDiscoveryUpnp = "Découverte de l'appareil (UPnP)"
    override val portStorageOrAdminPage = "Page de stockage ou d'administration"
    override val portDatabase = "Base de données"
    override val portService = "Service"
    override val devicesLooking = "À la recherche d'appareils…"
    override val devicesFindOnWifi = "Rechercher des appareils sur votre Wi-Fi"
    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${count} sur ${ssid}"
    override fun devicesOnYourNetwork(count: Int) = "${count} sur votre réseau"
    override val couldntLookForDevices = "Impossible de rechercher des appareils"
    override val scanFailed = "Échec de l'analyse"
    override val connectionChangedLookAgain = "La connexion a changé. Regardez à nouveau sur ce réseau."
    override val stoppedLooking = "J'ai arrêté de chercher"
    override val noDevicesFound = "Aucun appareil trouvé"
    override val noDevicesFoundSubtitle =
        "Certains appareils restent silencieux lorsqu'on leur demande. Réessayez dans un instant ou vérifiez que cet appareil est chez vous Wi-Fi."
    override fun otherDevicesGroup(count: Int) = "Autres appareils · ${count}"
    override fun quietDevices(count: Int) = if (count == 1) "1 appareil silencieux" else "${count} appareils silencieux"
    override val quietDevicesSubtitle = "Ils n'ont pas dit ce qu'ils étaient"
    override val hide = "Cacher"
    override val show = "Afficher"
    override fun checkingLocalAddresses(percent: Int) = "Vérification des adresses locales · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · n'a pas répondu"

    override val deviceNotScannedYet = "Pas encore trouvé dans une analyse"
    override val deviceAnsweredLastScan = "J'ai répondu au dernier scan"
    override val deviceSeenButNoAnswer = "Vu sur le réseau, mais n'a pas répondu"
    override val actionCheckPorts = "Vérifier les ports"
    override val openRouterSettings = "Ouvrir les paramètres du routeur"
    override val detailsTitle = "Détails"
    override val factNetworkName = "Nom du réseau"
    override val factHardwareAddress = "Adresse du matériel"
    override val macHiddenByAndroid = "Caché par Android"
    override val factYourName = "Votre nom pour cela"
    override val portsNotCheckedYet = "Pas encore vérifié."
    override fun portsNoneOpenWhenChecked(moment: String) =
        "Aucun des ports communs n'était ouvert lors de la vérification ${moment.replaceFirstChar { it.lowercase() }}."
    override fun portsCheckedAt(moment: String) = "Vérifié ${moment.replaceFirstChar { it.lowercase() }}."
    override val renameDeviceTitle = "Renommer l'appareil"
    override val renameSupporting = "Laissez vide pour utiliser le nom WiFi Weakspot trouvé."
    override val connectionChangedOpenAgain = "La connexion a changé. Ouvrez à nouveau cet appareil sur le nouveau réseau."
    override val couldntLoadDetails = "Impossible de charger les détails enregistrés. Essayez d'ouvrir à nouveau cet appareil."
    override val scanBeforeNaming = "Scannez cet appareil avant d'enregistrer un nom."

}
