package com.bernaferrari.wifiweakspot.core.i18n.ptbr

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object PtBrToolsStrings : ToolsStrings {
    override val tryAgain = "Tente novamente"

    override val targetPlaceholder = "Site ou endereço IP"
    override val clearTarget = "Claro"
    override val checkSectionTitle = "Verificar"
    override val recentSectionTitle = "Recente"

    override val toolPortsDescription = "O que está aberto"

    override val pingQuestion = "Isso responde?"
    override val dnsQuestion = "Qual é o endereço?"
    override val dnsDetail = "DNS pesquisa"
    override val portsQuestion = "O que isso oferece?"

    override val runningPortScanTitle = "Verificando portas"

    override val diagnosticFailed = "Falha no diagnóstico"
    override val invalidTarget = "Insira um nome de host ou endereço IP sem caminho de URL ou espaços."
    override val diagnosticCouldNotComplete = "O diagnóstico não pôde ser concluído."
    override val connectionChangedRunAgain = "A conexão mudou. Execute esta verificação novamente na nova rede."

    override val openItsWebPage = "Abra sua página da web"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ${answered} de ${total} responderam"

    override val slowest = "Mais lento"
    override fun repliesContentDescription(replies: String) = "Respostas: ${replies}"

    override val pingDidntAnswer = "Não respondi"
    override val pingResponsive = "Responsivo"
    override val pingResponsiveButUneven = "Responsivo, mas desigual"
    override val pingDroppingReplies = "Descartando respostas"
    override val pingSlowToAnswer = "Lento para responder"

    override val pingNoAnswerLocal = "Ele pode estar desligado, adormecido ou configurado para ignorar pings."
    override fun pingRepliesLost(lost: Int, total: Int) =
        "${lost} de ${total} respostas desapareceram. Compare uma verificação do roteador com um host da Internet para ajudar a localizar o problema; este teste por si só não pode identificar a causa."
    override val pingUneven = "Os tempos de resposta variaram. Compare uma verificação de roteador aqui e perto do roteador e, em seguida, teste um host da Internet."
    override val pingLocalExplanation = "É hora de chegar a este dispositivo e voltar. O próprio dispositivo pode afetar o resultado."
    override val pingInternetExplanation = "É hora de chegar a este host e voltar. Sua conexão, a rota e o host afetam o resultado."

    override fun dnsAddressUnit(count: Int) = if (count == 1) "endereço" else "endereços"
    override fun dnsForQuery(query: String) = "Para ${query}"
    override val dnsNotFound = "Não foi possível encontrar"
    override val dnsFound = "Encontrei"
    override val dnsNotFoundExplanation = "Verifique a ortografia ou tente novamente; o nome pode não existir."
    override fun dnsFoundExplanation(query: String) = "Seu dispositivo pode encontrar ${query}."
    override val dnsAddressesTitle = "Endereços"

    override val portsOpenUnit = "abrir"
    override fun portsCommonServicesOn(host: String) = "Serviços comuns em ${host}"
    override val portsNothingOpen = "Nada aberto"
    override fun portsOffersServices(count: Int) = if (count == 1) "Oferece 1 serviço" else "Oferece ${count} serviços"
    override val portsNoneAnswered = "Nenhum serviço comum respondeu."
    override val portsOffersExplanation = "Coisas como uma página de configurações ou compartilhamento de arquivos."

    override val portWebPage = "página da Internet"
    override val portSecureWebPage = "Página da web segura"
    override val portRemoteLoginSsh = "Login remoto (SSH)"
    override val portOldRemoteLoginTelnet = "Login remoto antigo (Telnet)"
    override val portFileTransferFtp = "Transferência de arquivo (FTP)"
    override val portAddressLookupsDns = "Pesquisas de endereço (DNS)"
    override val portFileSharing = "Compartilhamento de arquivos"
    override val portMacFileSharing = "Compartilhamento de arquivos Mac"
    override val portPrinting = "Impressão"
    override val portRemoteDesktop = "Área de trabalho remota"
    override val portScreenSharing = "Compartilhamento de tela"
    override val portDeviceDiscoveryUpnp = "Descoberta de dispositivos (UPnP)"
    override val portStorageOrAdminPage = "Página de armazenamento ou administração"
    override val portDatabase = "Banco de dados"
    override val portService = "Serviço"
    override val devicesLooking = "Procurando dispositivos…"
    override val devicesFindOnWifi = "Encontre dispositivos no seu Wi-Fi"
    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${count} em ${ssid}"
    override fun devicesOnYourNetwork(count: Int) = "${count} na sua rede"
    override val couldntLookForDevices = "Não foi possível procurar dispositivos"
    override val scanFailed = "Falha na verificação"
    override val connectionChangedLookAgain = "A conexão mudou. Olhe novamente nesta rede."
    override val stoppedLooking = "Parou de procurar"
    override val noDevicesFound = "Nenhum dispositivo encontrado"
    override val noDevicesFoundSubtitle =
        "Alguns dispositivos ficam silenciosos quando solicitados. Tente novamente em alguns instantes ou verifique se este dispositivo está em sua casa Wi-Fi."
    override fun otherDevicesGroup(count: Int) = "Outros dispositivos · ${count}"
    override fun quietDevices(count: Int) = if (count == 1) "1 dispositivo silencioso" else "${count} dispositivos silenciosos"
    override val quietDevicesSubtitle = "Eles não disseram o que são"
    override val hide = "Esconder"
    override val show = "Mostrar"
    override fun checkingLocalAddresses(percent: Int) = "Verificando endereços locais · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · não respondeu"

    override val deviceNotScannedYet = "Ainda não encontrado em uma verificação"
    override val deviceAnsweredLastScan = "Respondeu a última varredura"
    override val deviceSeenButNoAnswer = "Visto na rede, mas não respondi"
    override val actionCheckPorts = "Verifique as portas"
    override val openRouterSettings = "Abra as configurações do roteador"
    override val detailsTitle = "Detalhes"
    override val factNetworkName = "Nome da rede"
    override val factHardwareAddress = "Endereço de hardware"
    override val macHiddenByAndroid = "Escondido por Android"
    override val factYourName = "Seu nome para isso"
    override val portsNotCheckedYet = "Ainda não verificado."
    override fun portsNoneOpenWhenChecked(moment: String) =
        "Nenhuma das portas comuns estava aberta quando marcada ${moment.replaceFirstChar { it.lowercase() }}."
    override fun portsCheckedAt(moment: String) = "Verificado ${moment.replaceFirstChar { it.lowercase() }}."
    override val renameDeviceTitle = "Renomear dispositivo"
    override val renameSupporting = "Deixe em branco para usar o nome WiFi Weakspot encontrado."
    override val connectionChangedOpenAgain = "A conexão mudou. Abra este dispositivo novamente na nova rede."
    override val couldntLoadDetails = "Não foi possível carregar os detalhes salvos. Tente abrir este dispositivo novamente."
    override val scanBeforeNaming = "Digitalize este dispositivo antes de salvar um nome."

}
