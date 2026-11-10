package com.bernaferrari.wifiweakspot.core.i18n.fr

import com.bernaferrari.wifiweakspot.core.i18n.DomainStrings
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind

object FrDomainStrings : DomainStrings {

    override val signalDisplayBars = "Barres"
    override val qualityExcellentLabel = "Excellent"
    override val qualityExcellentHeadline = "Excellent signal"
    override val qualityExcellentExplanation = "La connexion au routeur est forte. Le signal seul ne mesure pas le débit internet."
    override val qualityGoodLabel = "Bon"
    override val qualityGoodHeadline = "Bon signal"
    override val qualityGoodExplanation = "Le signal reçu du routeur est bon."

    override val qualityFairLabel = "Correct"
    override val qualityFairHeadline = "Signal correct"
    override val qualityFairExplanation = "Le signal est utilisable, mais tolère moins les interférences."
    override val qualityWeakLabel = "Faible"
    override val qualityWeakHeadline = "Zone de faible signal"
    override val qualityWeakExplanation = "Un signal faible. Comparez cet endroit avec un autre plus proche du routeur."
    override val qualityDeadZoneLabel = "Zone sans signal"
    override val qualityDeadZoneExplanation = "Très peu de signal atteint cet endroit."

    override val deviceKindRouter = "Routeur"
    override val deviceKindThisDevice = "Cet appareil"
    override val deviceKindPhone = "Téléphone ou tablette"
    override val deviceKindComputer = "Ordinateur"
    override val deviceKindTv = "TV ou streamer"
    override val deviceKindSpeaker = "Enceinte"
    override val deviceKindPrinter = "Imprimante"
    override val deviceKindConsole = "Console de jeu"
    override val deviceKindCamera = "Caméra"
    override val deviceKindSmartHome = "Appareil domestique intelligent"
    override val deviceKindUnknown = "Appareil"
    override val unnamedDevice = "Appareil sans nom"

    override fun makerDevice(maker: String, kind: DeviceKind): String = "${kindLabel(kind)} ${maker}"

    private fun kindLabel(kind: DeviceKind): String = when (kind) {
        DeviceKind.Router -> deviceKindRouter
        DeviceKind.ThisPhone -> deviceKindThisDevice
        DeviceKind.Phone -> deviceKindPhone
        DeviceKind.Computer -> deviceKindComputer
        DeviceKind.Tv -> deviceKindTv
        DeviceKind.Speaker -> deviceKindSpeaker
        DeviceKind.Printer -> deviceKindPrinter
        DeviceKind.Console -> deviceKindConsole
        DeviceKind.Camera -> deviceKindCamera
        DeviceKind.SmartHome -> deviceKindSmartHome
        DeviceKind.Unknown -> deviceKindUnknown
    }

    override val verdictGreatHeadline = "Le routeur répond rapidement"
    override val verdictGreatExplanation = "Les réponses ont été rapides et régulières."
    override val verdictGoodHeadline = "La réponse du routeur est utilisable"
    override val verdictGoodExplanation = "Certaines réponses étaient un peu lentes ou inégales."
    override val verdictSlowHeadline = "Les réponses du routeur sont retardées"
    override val verdictSlowExplanation = "Les réponses étaient lentes ou inégales. Réessayez près du routeur pour comparer."
    override val verdictUnreliableHeadline = "Vérification du routeur non concluante"
    override val verdictUnreliableExplanation = "De nombreuses réponses ont disparu. Certains routeurs ignorent ces vérifications, alors réessayez avant de modifier quoi que ce soit."

    override val roomSuggestions = listOf("Salon", "Cuisine", "Chambre", "Bureau", "Chambre des enfants", "Salle de bains", "Salle à manger", "Couloir", "Garage", "Sous-sol", "Balcon", "Jardin")

    override val coverageNoRooms = "Aucune pièce mesurée pour l’instant"

    override fun coverageWeakInRoom(room: String) = "Signal Wi-Fi faible : ${room}"

    override fun coverageWeakInRooms(count: Int) = "Signal Wi-Fi faible dans ${count} pièces"

    override fun coverageRoomsCheckedNoneWeak(count: Int) =
        if (count == 1) "1 pièce vérifiée, aucune faible" else "${count} pièces vérifiées, aucune faible"

    override fun tipHighBandWeak(room: String, band: String) = "Pour ${room}, l’appareil a utilisé ${band}, une bande qui traverse moins bien les murs. Si votre routeur permet de séparer les bandes, le 2,4 GHz peut y être plus stable."

    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) = "Le signal baisse de ${dropDb} dB entre ${bestRoom} et ${worstRoom}. Placez le routeur plus au centre et en hauteur, puis mesurez à nouveau aux mêmes endroits."

    override fun tipMultipleAccessPoints(count: Int) =
        "Votre appareil a utilisé ${count} points d'accès dans ces pièces, ce qui peut refléter différentes bandes ou points d'accès. Cela ne suffit pas à confirmer une itinérance fiable."
    override val tipRouterPlacement = "Placez le routeur à l’intérieur, en hauteur et dans un espace dégagé, loin des micro-ondes, des miroirs et des aquariums."

    override val spotFirstReading = "Première mesure · mesurez à nouveau après une modification"

    override val spotDifferentNetwork = "Réseau ou lieu différent · commencez une nouvelle comparaison"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "Signal ${deltaDb} dB plus fort"
            deltaDb <= -4 -> "Signal ${-deltaDb} dB plus faible"
            else -> "Signal à peu près pareil"
        }
        val changed = buildList {
            if (accessPointChanged) add("point d’accès modifié")
            if (bandChanged) add("bande changée")
        }
        return signal + if (changed.isEmpty()) " depuis le dernier contrôle" else " · ${changed.joinToString("; ")}"
    }

    override val exportFileChooserTitle = "Exporter le fichier"

    override val lanVpnUnavailable = "La découverte d'appareils n'est pas disponible via cette connexion VPN. Vérifiez si votre VPN autorise l'accès au réseau local ou réessayez lorsqu'il est déconnecté."
    override val lanConnectFirst = "Connectez-vous à Wi-Fi ou Ethernet avant de scanner les appareils locaux."
    override val lanNotConnected = "Non connecté"

}
