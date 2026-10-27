package com.bernaferrari.wifiweakspot.core.i18n.ptbr

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object PtBrAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = if (count == 1) "1 rede" else "${count} redes"

    override fun channelShort(channel: Int) = "Can. ${channel}"

    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · ${channelShort(channel)} · ${band}"

    override val noPassword = "Sem senha"
    override val wifiOffTitle = "Wi-Fi está desligado"
    override val locationOffBody = "O Android precisa da localização ativada para listar redes Wi-Fi próximas. O WiFi Weakspot não usa GPS."

    override val scanPermissionBody = "Android compartilha apenas nomes de redes próximas com aplicativos que tenham acesso à localização. WiFi Weakspot nunca registra onde você está."
    override val wifiOffBody = "Ative Wi-Fi para ver redes próximas."
    override val locationOffTitle = "A localização está desativada"
    override val turnOnLocation = "Ativar localização"
    override val scanPausedTitle = "Android verificação pausada"
    override fun scanPausedBody(seconds: Long) = "A próxima varredura é permitida em ${seconds} s. Desative a otimização da verificação para evitar a espera."
    override val turnOffThrottling = "Desativar a limitação"
    override val readyToScanBody = "Toque em digitalizar para ver todas as redes próximas."
    override val scan = "Buscar"
    override val channels = "Canais"
    override val signalOverTime = "Sinal ao longo do tempo"
    override val howToReadThisChart = "Como ler este gráfico"
    override val expandSignalHistory = "Expandir histórico de sinal"
    override val expandChannels = "Expandir canais"
    override fun noBandNetworksNearby(band: String) = "Não há redes ${band} por perto."
    override fun networksOnBand(count: Int, band: String) =
        if (count == 0) "Nenhuma rede em ${band}" else "${networks(count)} em ${band}"
    override fun showVeryWeakNetworks(count: Int) =
        if (count == 1) "Mostrar 1 rede muito fraca" else "Mostrar ${count} redes muito fracas"
    override fun hideVeryWeakNetworks(count: Int) =
        if (count == 1) "Ocultar 1 rede muito fraca" else "Ocultar ${count} redes muito fracas"
    override fun bandNetworkCount(count: Int) = if (count == 0) "nenhum" else networks(count)
    override val highlightedOnChart = "Destacado no gráfico"
    override val notHighlightedOnChart = "Não destacado no gráfico"
    override fun detailsForNetwork(ssid: String) = "Detalhes para ${ssid.ifEmpty { "rede oculta" }}"
    override fun alsoYourRouter(quality: String) = "${quality} · Também seu roteador"
    override val connected = "Conectado"

    override val scanReportHeading = "WiFi Weakspot Relatório de verificação"
    override fun scanReportTime(time: String) = "Horário: ${time}"
    override fun scanReportTotal(count: Int) = "Total de redes: ${count}"
    override val scanReportNetworksHeading = "Redes:"
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) = "• ${ssid} - ${rssi} dBm, ${channelShort(channel)}, ${security}"

    override val channelLooksClear = "Seu canal está pouco congestionado"
    override val noChannelImprovement = "Nenhum canal melhor encontrado"
    override fun lessOverlapThanChannel(currentChannel: Int) = "Menos congestionado que o canal ${currentChannel}"
    override val suggestedChannel = "Canal sugerido"
    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}: canal ${channel}"
    override val lowOverlap = "Baixa sobreposição"
    override val moderateOverlap = "Sobreposição moderada"
    override val highOverlap = "Alta sobreposição"
    override val veryHighOverlap = "Sobreposição muito alta"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) =
        "Gráfico do canal, ${networks(networkCount)}." +
            (if (busiestChannel != null) " Canal mais movimentado ${busiestChannel} com ${busiestCount}." else "")
    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) = name + (if (isYou) " (${connected})" else "") + (if (extraNames > 0) " +${extraNames}" else "")

    override val axisStrong = "Forte"
    override val axisOkay = "Médio"

    override val axisWeak = "Fraco"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · ${channelShort(channel)}"

    override val clearSelectedNetwork = "Limpar rede selecionada"
    override val tapWaveForDetails = "Toque em uma onda para ver detalhes da rede"

    override val chartGuideTitle = "Lendo os gráficos"
    override val guideTallerBody = "Altura é a intensidade do sinal."
    override val guideOverlapBody = "Redes sobrepostas partilham espectro e podem competir pelo tempo de antena."
    override val guideWiderBody = "Um canal mais amplo ocupa mais espectro e pode sobrepor mais vizinhos."
    override val guideSameRouterBody = "+1: Nomes de redes que parecem usar o mesmo rádio Wi-Fi compartilham uma curva."

    override val guideSteadyBody = "Uma linha que sobe e desce indica variações no sinal. Toque para pausar; arraste para os lados para mudar o instante ou para cima e para baixo para mudar de rede. Retome para ver novas medições."

    override val guideHighlightBody = "Toque em uma colina ou linha para destacá-la. Arraste horizontalmente pelo gráfico para explorar."

    override fun channelOverlapHeading(count: Int, channel: Int) = when (count) {
        0 -> "O canal ${channel} está limpo"
        1 -> "1 canal de sobreposição de rede ${channel}"
        else -> "${count} redes sobrepõem canal ${channel}"
    }
    override fun fromChannel(homeChannel: Int, reading: String) = "Do canal ${homeChannel} · ${reading}"

    override val estimatedChannelOverlap = "Sobreposição estimada de canais"
    override fun tryChannel(channel: Int) = "Experimente o canal ${channel}"
    override val lowestOverlapFor20Mhz = "Sobreposição estimada mais baixa para 20 MHz canais"
    override val overlapDisclaimer = "A estimativa usa os pontos de acesso próximos e a intensidade do sinal, não o tráfego medido. Antes de alterar as configurações, verifique os canais compatíveis com o roteador e as regras da sua região. Canais de 5 e 6 GHz só são sugeridos quando a compatibilidade do roteador é conhecida. Se a frequência central de um canal largo não estiver disponível, apenas o canal primário de 20 MHz será mostrado."

    override val overlapScore = "Pontuação de sobreposição"

    override val collectingReadings = "Coletando medições… As linhas aparecem após algumas buscas."

    override fun signalHistoryDescription(networkCount: Int) =
        "Histórico de sinais para ${networks(networkCount)}. Toque em uma linha para inspecionar. As teclas de seta movem-se através do tempo e das redes."
    override val historyLive = "Ao vivo"
    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) = "${networkName}, ${reading ?: "sem medição"}, pausado, ${readingsAgo(readingsAgo)}"

    override val strongerNetwork = "Rede mais forte"
    override val weakerNetwork = "Rede mais fraca"
    override val axisOlder = "anterior"
    override val axisLatest = "recente"
    override val exploreSignalHistory = "Explore o histórico do sinal"
    override val tapLineToCompare = "Toque em uma linha e arraste para comparar"
    override val noReadingAtPoint = "Sem medição neste ponto"

    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) = "${reading} · ${quality} · " + if (readingsAgo == 0) "última medição" else readingsAgo(readingsAgo)

    private fun readingsAgo(count: Int) = if (count == 1) "há 1 medição" else "há $count medições"

    override val resumeLive = "Retomar ao vivo"

    override fun seenReading(rssi: Int, seen: String) = "${rssi} dBm · visto ${seen}"
    override val factChannel = "Canal"
    override val factWidth = "Largura"
    override val factSecurity = "Segurança"
    override val factDistance = "Estimativa de distância"
    override val notAvailable = "Não disponível"
    override val watch = "Assistir"
    override val addToFavorites = "Adicionar aos favoritos"
    override val removeFromFavorites = "Remover dos favoritos"

    override fun routerCheckLastTime(pingMs: String) = "${pingMs} ms da última vez"
    override fun devicesFoundLastScan(count: Int) = "${count} encontrado na última varredura"
    override val devicesSubtitle = "Quem está conectado"
    override val testWebsiteSubtitle = "Ping, DNS, portas"
    override fun savedChecks(count: Int) = when (count) {
        0 -> "Resultados anteriores"
        1 -> "1 cheque"
        else -> "${count} verificações"
    }
    override val notOnWifi = "Não em Wi-Fi"
    override val online = "On-line"
    override val internetNotConfirmed = "Internet não confirmada"
    override fun channelChip(channel: Int) = "Canal ${channel}"
    override fun linkRateChip(mbps: Int) = "Taxa de link ${mbps} Mbps"
    override val chooseNetwork = "Escolha uma rede"
    override val turnOnWifi = "Ative Wi-Fi"
    override val dismiss = "Liberar"
    override val findWeakSpotsTitle = "Encontre pontos fracos"
    override val findWeakSpotsBody = "Veja onde o sinal fica fraco."

    override val findWeakSpotsAction = "Verifique seus cômodos"
    override fun networksNearby(count: Int) = "${networks(count)} nas proximidades"
    override val nearbyStale = "Da sua última varredura. Toque para atualizar."
    override val nearbyNoConnection = "Veja canais e sinal de cada rede."
    override fun nearbyOverlap(count: Int, channel: Int) = when (count) {
        0 -> "Nenhuma sobreposição próxima detectada no canal ${channel}."
        1 -> "1 sinal próximo se sobrepõe ao seu canal ${channel}."
        else -> "${count} sinais próximos se sobrepõem ao seu canal ${channel}."
    }

    override val adviceNoWifiBody = "Conecte-se à rede que deseja verificar. Você ainda pode executar testes de Internet e ferramentas de rede usando sua conexão atual."
    override val adviceNoInternetTitle = "Verifique primeiro o acesso à Internet"
    override val adviceNoInternetBody = "Android não confirmou acesso à Internet nesta conexão Wi-Fi. Verifique se há uma página de login e teste a conectividade com seu roteador e um host da Internet."
    override val adviceNoSignalTitle = "Leia seu sinal Wi-Fi"
    override val adviceNoSignalBody = "Permitir varreduras Wi-Fi em redes para ver medições de sinal. Compare o sinal na sala onde você percebe problemas."
    override val adviceWeakSignalTitle = "Compare a cobertura perto do seu roteador"
    override val adviceWeakSignalBody = "O sinal Wi-Fi está fraco. Teste a resposta do roteador aqui e repita perto dele. Compare medições da sua própria rede para encontrar os pontos fracos."

    override val adviceGoodSignalTitle = "Compare a capacidade de resposta do roteador"
    override val adviceGoodSignalBody = "A intensidade do sinal parece utilizável e Android relata o acesso à Internet. Se a conexão parecer lenta, compare a resposta do roteador aqui e perto do roteador; sinal forte por si só não garante desempenho."
    override val channelAdviceBasis = "Barras menores indicam menos sobreposição nesta varredura. A velocidade pode variar."
    override fun channelChangeInstructions(channel: Int, band: String) = "Escolha o canal ${channel} para ${band}"

    override val channelOverlapTitle = "Sobreposição com redes próximas"
    override val channelApplyChange = "Salve e reconecte, se necessário"
    override val channelChangeNote = "Faça a alteração no aplicativo ou nas configurações do roteador, não no WiFi Weakspot."

}
