package com.bernaferrari.wifiweakspot.core.i18n.ptbr

import com.bernaferrari.wifiweakspot.core.i18n.DomainStrings
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind
import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel

object PtBrDomainStrings : DomainStrings {

    override val signalDisplayBars = "Barras"
    override val qualityExcellentLabel = "Excelente"
    override val qualityExcellentHeadline = "Sinal excelente"
    override val qualityExcellentExplanation = "A conexão com o roteador é forte. O sinal por si só não mede a velocidade da internet."
    override val qualityGoodLabel = "Bom"
    override val qualityGoodHeadline = "Sinal bom"
    override val qualityGoodExplanation = "Um sinal sólido do roteador."
    override val qualityFairLabel = "Razoável"
    override val qualityFairHeadline = "Sinal razoável"
    override val qualityFairExplanation = "O sinal é utilizável, mas tolera menos interferência."
    override val qualityWeakLabel = "Fraco"
    override val qualityWeakHeadline = "Ponto fraco"
    override val qualityWeakExplanation = "Um sinal fraco. Compare este local com outro mais próximo do roteador."
    override val qualityDeadZoneLabel = "Sem cobertura"
    override val qualityDeadZoneExplanation = "Muito pouco sinal chega a este ponto."

    override val deviceKindRouter = "Roteador"
    override val deviceKindThisDevice = "Este dispositivo"
    override val deviceKindPhone = "Celular ou tablet"
    override val deviceKindComputer = "Computador"
    override val deviceKindTv = "TV ou streamer"
    override val deviceKindSpeaker = "Alto-falante"
    override val deviceKindPrinter = "Impressora"
    override val deviceKindConsole = "Consola de jogos"
    override val deviceKindCamera = "Câmera"
    override val deviceKindSmartHome = "Dispositivo doméstico inteligente"
    override val deviceKindUnknown = "Dispositivo"
    override val unnamedDevice = "Dispositivo sem nome"

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

    override val verdictGreatHeadline = "O roteador responde rapidamente"
    override val verdictGreatExplanation = "As respostas foram rápidas e constantes."
    override val verdictGoodHeadline = "A resposta do roteador é utilizável"
    override val verdictGoodExplanation = "Algumas respostas foram um pouco lentas ou irregulares."
    override val verdictSlowHeadline = "As respostas do roteador estão atrasadas"
    override val verdictSlowExplanation = "As respostas foram lentas ou irregulares. Tente novamente perto do roteador para comparar."
    override val verdictUnreliableHeadline = "Verificação do roteador inconclusiva"
    override val verdictUnreliableExplanation = "Muitas respostas desapareceram. Alguns roteadores ignoram essas verificações, então tente novamente antes de alterar qualquer coisa."

    override val roomSuggestions = listOf("Sala de estar", "Cozinha", "Quarto", "Escritório", "Quarto das crianças", "Banheiro", "Sala de jantar", "Corredor", "Garagem", "Porão", "Varanda", "Jardim")

    override val coverageNoRooms = "Nenhum quarto verificado ainda"
    override fun coverageWeakInRoom(room: String) = "Fraco Wi-Fi em ${room}"
    override fun coverageWeakInRooms(count: Int) = "Fraco Wi-Fi em ${count} quartos"
    override fun coverageRoomsCheckedNoneWeak(count: Int) =
        if (count == 1) "1 quarto verificado, nenhum fraco" else "${count} quartos verificados, nenhum fraco"

    override fun tipHighBandWeak(room: String, band: String) =
        "Em ${room} seu dispositivo usou ${band}, que atravessa paredes menos longe. Se o seu roteador puder dividir bandas, 2,4 GHz poderá funcionar melhor lá."
    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) =
        "O sinal cai ${dropDb} dB de ${bestRoom} para ${worstRoom}. Experimente uma posição mais central e elevada do roteador e verifique novamente os mesmos pontos para ver se isso ajuda."
    override fun tipMultipleAccessPoints(count: Int) =
        "Seu dispositivo usou ${count} pontos de acesso nessas salas, o que pode refletir diferentes bandas ou pontos de acesso. Isto por si só não confirma um roaming confiável."
    override val tipRouterPlacement = "Mantenha o roteador ao ar livre, longe do chão e longe de micro-ondas, espelhos e aquários."

    override val spotFirstReading = "Primeira leitura · verifique novamente após fazer uma alteração"
    override val spotDifferentNetwork = "Rede ou local diferente · iniciar uma nova comparação"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "Sinal ${deltaDb} dB mais forte"
            deltaDb <= -4 -> "Sinal ${-deltaDb} dB mais fraco"
            else -> "Sinal sobre o mesmo"
        }
        val changed = buildList {
            if (accessPointChanged) add("ponto de acesso alterado")
            if (bandChanged) add("banda mudou")
        }
        return signal + if (changed.isEmpty()) " desde a última verificação" else " · ${changed.joinToString("; ")}"
    }

    override val monitorChannelName = "Monitoramento em segundo plano"
    override val monitorChannelDescription = "Mostra quando WiFi Weakspot está monitorando redes WiFi"
    override val alertsChannelName = "Alertas de rede"
    override val alertsChannelDescription = "Alertas para novas redes e mudanças de sinal"
    override val monitorStartFailed = "O monitoramento não pôde ser iniciado. Permita verificações Wi-Fi e ative os serviços de localização e tente novamente."
    override val monitorWaitingForScanLimited = "Aguardando uma nova varredura Wi-Fi. Android pode limitar solicitações de varredura."
    override val monitorWaitingForScan = "Esperando por uma nova varredura Wi-Fi"
    override fun monitorStatus(networkCount: Int, lastScanTime: String) = "${networkCount} ${if (networkCount == 1) "rede" else "redes"} · Última busca: ${lastScanTime}"

    override fun newNetworksTitle(count: Int) =
        if (count == 1) "Novo WiFi Rede detectada" else "Novas WiFi Redes Detectadas"

    override fun newNetworksText(names: List<String>): String = when {
        names.size == 1 -> "\"${names.first()}\" agora está ao alcance"
        else -> "${names[0]}, ${names[1]}" + if (names.size > 2) " e mais" else ""
    }

    override val signalChangeTitle = "Mudança de sinal"
    override fun signalChangeText(networkName: String, level: MonitorSignalLevel, rssi: Int): String {
        val quality = when (level) {
            MonitorSignalLevel.Excellent -> "excelente"
            MonitorSignalLevel.Good -> "bom"
            MonitorSignalLevel.Fair -> "justo"
            MonitorSignalLevel.Weak -> "fraco"
        }
        return "O sinal \"${networkName}\" agora é ${quality} (${rssi} dBm)"
    }

    override val exportFileChooserTitle = "Exportar arquivo"
    override fun chartDescription(name: String) = "Gráfico: ${name}"

    override val lanVpnUnavailable = "A descoberta de dispositivos não está disponível por meio desta conexão VPN. Verifique se a sua VPN permite acesso à rede local ou tente novamente quando estiver desconectada."
    override val lanConnectFirst = "Conecte-se a Wi-Fi ou Ethernet antes de digitalizar dispositivos locais."
    override val lanNotConnected = "Não conectado"

}
