package com.bernaferrari.wifiweakspot.core.i18n.ptbr

import com.bernaferrari.wifiweakspot.core.i18n.HistoryStrings

object PtBrHistoryStrings : HistoryStrings {
    override val today = "Hoje"
    override val yesterday = "Ontem"
    override val weekdayNames = listOf("segunda-feira", "terça-feira", "quarta-feira", "quinta-feira", "sexta-feira", "sábado", "domingo")
    override val weekdayShortNames = listOf("seg", "ter", "qua", "qui", "sex", "sáb", "dom")
    override val monthShortNames = listOf("jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez")
    override fun dateThisYear(weekday: String, day: Int, month: String) = "${weekday}, ${day} de ${month}"

    override fun dateOtherYear(weekday: String, day: Int, month: String, year: Int) = "${weekday}, ${day} de ${month} de ${year}"

    override fun momentAt(day: String, time: String) = "${day} em ${time}"

    override val filterAll = "Todos"
    override val kindInternetTests = "Testes de Internet"
    override val kindScans = "Verificações"
    override val emptyTitle = "Nada salvo ainda"
    override val emptySubtitle = "Verificações de roteador, varreduras de rede e resultados de ferramentas aparecem aqui, para que você possa ver como seu Wi-Fi muda ao longo do tempo."
    override fun trendBarsDescription(count: Int, readings: String) = "Últimos ${count} testes de roteador: ${readings}"
    override val trendNoReply = "Nenhuma resposta do roteador. Alguns roteadores ignoram essas verificações."
    override val trendRecheckToCompare = "Verifique novamente após uma alteração para comparar."
    override fun trendQuicker(usualMs: String) = "Mais rápido que o normal (${usualMs} ms)"
    override fun trendSlower(usualMs: String) = "Mais lento que o normal (${usualMs} ms)"
    override fun trendAboutSame(usualMs: String) = "Quase o mesmo de sempre (${usualMs} ms)"

    override fun internetTitle(mbps: String) = "${mbps} Mbps baixar"
    override fun routerTitle(ms: String) = "${ms} ms para roteador"
    override fun networksNearby(count: Int) = if (count == 1) "1 rede próxima" else "${count} redes próximas"
    override fun strongestNetwork(network: String) = "Mais forte: ${network}"
    override fun accessPoints(count: Int) = if (count == 1) "1 ponto de acesso" else "${count} pontos de acesso"
    override fun devicesFound(count: Int) = if (count == 1) "1 dispositivo encontrado" else "${count} dispositivos encontrados"
    override fun listWithMore(list: String, more: Int) = if (more > 0) "${list} e ${more} mais" else list
    override fun pingSummary(averageMs: String, replied: Int, sent: Int) = "${averageMs} ms · ${replied} de ${sent} responderam"

    override fun lookupTitle(target: String) = "Consulta DNS: ${target}"

    override val noRecords = "Sem registros"
    override val lookupFailed = "Falha na pesquisa"
    override fun openPortsTitle(target: String) = "Portas abertas em ${target}"
    override val noCommonPortsOpen = "Nenhuma das portas comuns estava aberta"
    override val completed = "Concluído"
    override val failed = "Fracassado"

    override val metricDownload = "Download"
    override val metricUpload = "Carregar"
    override val metricLatency = "Latência"
    override val metricVariation = "Variação"
    override val metricLost = "Perdido"
    override fun metricOnBand(band: String) = "em ${band}"
    override val metricReplied = "Respondeu"
    override val metricReplies = "Respostas"
    override val metricAverage = "Média"
    override val factServer = "Servidor"
    override val factRoom = "Sala"
    override val factBand = "Banda"
    override val factSignal = "Sinal"
    override val factAccessPoints = "Pontos de acesso"
    override fun signalFact(quality: String, rssi: Int) = "${quality} sinal · ${rssi} dBm"

    override val linesNetworksStrongestFirst = "Redes, mais fortes primeiro"
    override val linesRecords = "Registros"
    override fun replyNumber(index: Int) = "Responder ${index}"
    override val scanNote = "Redes próximas no mesmo canal que o seu competem por tempo de transmissão."

    override val go = "IR"
    override val testInternetSpeedInstead = "Em vez disso, teste a velocidade da Internet"
    override val keepDeviceStill = "Mantenha seu dispositivo imóvel por alguns segundos."
    override val connectToStart = "Conecte-se ao seu Wi-Fi para começar."
    override val lastTime = "Última vez"
    override val couldNotSaveSpot = "Não foi possível salvar este local. Tente novamente."
    override val connectFirst = "Conecte-se ao seu Wi-Fi primeiro. Esta verificação mede as respostas do seu roteador."
    override val historyNotSaved = "Resultado medido, mas não foi possível salvar o histórico."
    override val checkStopped = "A conexão foi alterada ou a verificação do roteador foi interrompida. Mantenha este Wi-Fi conectado e tente novamente. Alguns roteadores bloqueiam o ping."

    override val internetSpeedTitle = "Velocidade da Internet · fast.com"
}
