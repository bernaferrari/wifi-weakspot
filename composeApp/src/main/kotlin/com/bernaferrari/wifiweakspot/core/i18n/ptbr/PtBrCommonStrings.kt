package com.bernaferrari.wifiweakspot.core.i18n.ptbr

import com.bernaferrari.wifiweakspot.core.i18n.CommonStrings

object PtBrCommonStrings : CommonStrings {
    override fun deleteItemTitle(name: String) = "Excluir ${name}?"

    override val couldNotSaveChanges = "Não foi possível salvar as alterações. Tente novamente."
    override val couldNotDeleteItem = "Não foi possível excluir este item. Tente novamente."

    override val hiddenNetworkShort = "Escondido"
    override val network = "Rede"
    override val tools = "Ferramentas"
    override val maker = "Fabricante"
    override val noReply = "Sem resposta"
    override val fastest = "Mais rápido"
    override val address = "Endereço"
    override val responseTime = "Tempo de resposta"
    override val checkAgain = "Verifique novamente"
    override val rename = "Renomear"
    override val stop = "Parar"
    override val unknown = "Desconhecido"

    override val hiddenNetwork = "Rede oculta"
    override val fasterScans = "Buscas mais rápidas"
    override val openWifiSettings = "Abrir configurações de Wi-Fi"
    override val signalUnavailable = "Sinal indisponível"
    override val devices = "Dispositivos"
    override val openPorts = "Portas abertas"
    override val nameLabel = "Nome"
    override val historyTitle = "Histórico"
    override val routerCheck = "Verificar roteador"
    override val testWebsite = "Testar um site"
    override val yourRouter = "Seu roteador"
    override val yourNetwork = "Sua rede"

    override val openInNewTab = "Abrir em uma nova aba"
    override val mapSharingAndroidOnly = "Compartilhar o mapa funciona no aplicativo Android."
    override val back = "Voltar"
    override val close = "Fechar"
    override val cancel = "Cancelar"
    override val save = "Salvar"
    override val done = "Concluído"
    override val share = "Compartilhar"
    override val delete = "Excluir"

    override val tabHome = "Início"
    override val tabNetworks = "Redes"
    override val tabRooms = "Cômodos"
    override val tabSettings = "Configurações"

    override val permissionScreenTitle = "Permitir acesso a pessoas próximas Wi-Fi"
    override val permissionAllowAccess = "Permitir acesso"
    override val openAppSettings = "Abra as configurações do aplicativo"

    override fun signalBadgeDescription(quality: String, reading: String) = "${quality} sinal, ${reading}"

    override val scanReady = "PREPARAR"
    override fun scansCount(count: Int) = if (count == 1) "1 varredura" else "${count} varreduras"
    override val scansLeftCaption = "restantes"
    override val scanWaitingCaption = "esperando"
    override val scanDevMode = "MODO DESENVOLVIMENTO"
    override val readyToScan = "Pronto para digitalizar"
    override fun scansRemaining(count: Int) = if (count == 1) "1 busca restante" else "${count} varreduras restantes"
    override fun nextScanIn(time: String) = "Próxima varredura em ${time}"
    override val checking = "Verificando..."
    override val justNow = "agora mesmo"
    override fun minutesAgo(minutes: Long) = "${minutes} min atrás"
    override fun hoursAgo(hours: Long) = "${hours} h atrás"
    override fun daysAgo(days: Long) = "${days} d atrás"
}
