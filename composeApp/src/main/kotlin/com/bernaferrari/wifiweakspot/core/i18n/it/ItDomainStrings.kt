package com.bernaferrari.wifiweakspot.core.i18n.it

import com.bernaferrari.wifiweakspot.core.i18n.DomainStrings
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind
import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel

object ItDomainStrings : DomainStrings {

    override val signalDisplayBars = "Barre"
    override val qualityExcellentLabel = "Eccellente"
    override val qualityExcellentHeadline = "Segnale eccellente"
    override val qualityExcellentExplanation = "La connessione al router è forte. Il solo segnale non misura la velocità di internet."
    override val qualityGoodLabel = "Buono"
    override val qualityGoodHeadline = "Segnale buono"
    override val qualityGoodExplanation = "Un segnale solido dal router."
    override val qualityFairLabel = "Discreto"
    override val qualityFairHeadline = "Segnale discreto"
    override val qualityFairExplanation = "Il segnale è utilizzabile, ma tollera meno le interferenze."
    override val qualityWeakLabel = "Debole"
    override val qualityWeakHeadline = "Punto debole"
    override val qualityWeakExplanation = "Un segnale debole. Confronta questo punto con uno più vicino al router."
    override val qualityDeadZoneLabel = "Zona senza segnale"
    override val qualityDeadZoneExplanation = "In questo punto arriva pochissimo segnale."

    override val deviceKindRouter = "Router"
    override val deviceKindThisDevice = "Questo dispositivo"
    override val deviceKindPhone = "Telefono o tablet"
    override val deviceKindComputer = "Computer"
    override val deviceKindTv = "TV o streamer"
    override val deviceKindSpeaker = "Altoparlante"
    override val deviceKindPrinter = "Stampante"
    override val deviceKindConsole = "Console di gioco"
    override val deviceKindCamera = "Fotocamera"
    override val deviceKindSmartHome = "Dispositivo domestico intelligente"
    override val deviceKindUnknown = "Dispositivo"
    override val unnamedDevice = "Dispositivo senza nome"

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

    override val verdictGreatHeadline = "Il router risponde rapidamente"
    override val verdictGreatExplanation = "Le risposte sono state rapide e costanti."
    override val verdictGoodHeadline = "La risposta del router è utilizzabile"
    override val verdictGoodExplanation = "Alcune risposte sono state un po' lente o irregolari."
    override val verdictSlowHeadline = "Le risposte del router sono ritardate"
    override val verdictSlowExplanation = "Le risposte sono state lente o irregolari. Riprovare vicino al router per effettuare il confronto."
    override val verdictUnreliableHeadline = "Controllo del router inconcludente"
    override val verdictUnreliableExplanation = "Molte risposte sono andate perdute. Alcuni router ignorano questi controlli, quindi riprova prima di modificare qualsiasi cosa."

    override val roomSuggestions = listOf("Soggiorno", "Cucina", "Camera da letto", "Studio", "Cameretta", "Bagno", "Sala da pranzo", "Corridoio", "Garage", "Seminterrato", "Balcone", "Giardino")

    override val coverageNoRooms = "Nessuna stanza ancora misurata"

    override fun coverageWeakInRoom(room: String) = "Segnale Wi-Fi debole: ${room}"

    override fun coverageWeakInRooms(count: Int) = "Segnale Wi-Fi debole in ${count} stanze"

    override fun coverageRoomsCheckedNoneWeak(count: Int) =
        if (count == 1) "1 stanza controllata, nessuna debole" else "${count} stanze controllate, nessuna debole"

    override fun tipHighBandWeak(room: String, band: String) = "In ${room} il dispositivo ha usato ${band}, una banda che attraversa meno bene i muri. Se il router permette di separare le bande, la rete a 2,4 GHz potrebbe essere più stabile."

    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) = "Il segnale cala di ${dropDb} dB tra ${bestRoom} e ${worstRoom}. Prova a mettere il router in una posizione più centrale e alta, poi ripeti le misurazioni negli stessi punti."

    override fun tipMultipleAccessPoints(count: Int) =
        "Il tuo dispositivo ha utilizzato ${count} punti di accesso in queste stanze, che potrebbero riflettere bande o punti di accesso diversi. Questo da solo non conferma il roaming affidabile."
    override val tipRouterPlacement = "Posiziona il router in casa, in alto e senza ostacoli intorno, lontano da microonde, specchi e acquari."

    override val spotFirstReading = "Prima misurazione · ripeti dopo una modifica"

    override val spotDifferentNetwork = "Rete o luogo diverso · avvia un nuovo confronto"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "Segnale ${deltaDb} dB più forte"
            deltaDb <= -4 -> "Segnale ${-deltaDb} dB più debole"
            else -> "Segnale più o meno lo stesso"
        }
        val changed = buildList {
            if (accessPointChanged) add("punto di accesso cambiato")
            if (bandChanged) add("banda cambiata")
        }
        return signal + if (changed.isEmpty()) " dall'ultimo controllo" else " · ${changed.joinToString("; ")}"
    }

    override val monitorChannelName = "Monitoraggio in background"

    override val monitorChannelDescription = "Mostra quando WiFi Weakspot sta monitorando le reti WiFi"
    override val alertsChannelName = "Avvisi di rete"
    override val alertsChannelDescription = "Avvisi per nuove reti e cambiamenti di segnale"
    override val monitorStartFailed = "Impossibile avviare il monitoraggio. Consenti le scansioni Wi-Fi, attiva la posizione e riprova."

    override val monitorWaitingForScanLimited = "In attesa di una nuova scansione Wi-Fi. Android potrebbe limitare le richieste di scansione."
    override val monitorWaitingForScan = "In attesa di una nuova scansione Wi-Fi"
    override fun monitorStatus(networkCount: Int, lastScanTime: String) = "${networkCount} ${if (networkCount == 1) "rete" else "reti"} · Ultima scansione: ${lastScanTime}"

    override fun newNetworksTitle(count: Int) =
        if (count == 1) "Nuova WiFi Rete rilevata" else "Nuove WiFi Reti rilevate"

    override fun newNetworksText(names: List<String>): String = when {
        names.size == 1 -> "\"${names.first()}\" è ora nel raggio d'azione"
        else -> "${names[0]}, ${names[1]}" + if (names.size > 2) " e altro ancora" else ""
    }

    override val signalChangeTitle = "Cambio di segnale"
    override fun signalChangeText(networkName: String, level: MonitorSignalLevel, rssi: Int): String {
        val quality = when (level) {
            MonitorSignalLevel.Excellent -> qualityExcellentLabel
            MonitorSignalLevel.Good -> qualityGoodLabel
            MonitorSignalLevel.Fair -> qualityFairLabel
            MonitorSignalLevel.Weak -> qualityWeakLabel
        }
        return "Segnale di «$networkName»: $quality ($rssi dBm)"
    }

    override val exportFileChooserTitle = "Esporta file"
    override fun chartDescription(name: String) = "Grafico: ${name}"

    override val lanVpnUnavailable = "Il rilevamento del dispositivo non è disponibile tramite questa connessione VPN. Controlla se la tua VPN consente l'accesso alla rete locale o riprova quando è disconnessa."
    override val lanConnectFirst = "Connettersi a Wi-Fi o Ethernet prima di scansionare i dispositivi locali."
    override val lanNotConnected = "Non connesso"

}
