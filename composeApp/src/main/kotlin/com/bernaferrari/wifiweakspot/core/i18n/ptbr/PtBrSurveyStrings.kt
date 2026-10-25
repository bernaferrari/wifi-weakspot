package com.bernaferrari.wifiweakspot.core.i18n.ptbr

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object PtBrSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "Medindo... fique quieto"
    override val tapWhereYouAreStanding = "Toque no mapa onde você está"
    override val chooseWifi = "Escolha Wi-Fi"
    override val undoLastReading = "Desfazer a última medição"

    override val allReadings = "Todas as medições"

    override val floorPlanTitle = "Planta baixa"
    override fun readingCount(count: Int) = if (count == 1) "1 medição" else "${count} medições"

    override val mapOptions = "Opções de mapa"
    override val usePlanPhoto = "Usar uma foto da planta"

    override val changePhoto = "Mude a foto"
    override val removePhoto = "Remova a foto"
    override val exportReadingsCsv = "Exportar medições (CSV)"

    override fun floorName(floor: Int) = if (floor == 0) "Porão" else "Piso ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "Piso ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "Adicione um porão" else "Adicionar piso ${floor}"
    override val deleteFloorReadingsAction = "Excluir as leituras deste andar"
    override fun deleteFloorBody(count: Int) = "As medições deste andar serão excluídas (${count})."

    override val oneRouterAtATime = "Cada mapa mede um ponto de acesso por vez. Em sistemas mesh, cada unidade aparece separadamente."

    override val noNetworksFound = "Nenhuma rede encontrada ainda. Verifique se Wi-Fi e localização estão ativados."
    override val scanning = "Buscando…"
    override val refresh = "Atualizar"
    override val switchWifiTitle = "Mudar para outro Wi-Fi?"
    override val switchWifiBody = "Isso limpa as leituras em todos os andares deste mapa. Crie um novo mapa para manter ambos."
    override val clearAndSwitch = "Limpar e mudar"

    override val deleteMapBody = "Todas as leituras em cada andar serão excluídas."

    override val noReadingsYet = "Ainda não há medições aqui. Toque na planta onde você está."

    override val weakestFirst = "Os mais fracos primeiro, então os pontos problemáticos ficam no topo."
    override val mapYourHome = "Mapeie sua casa"
    override val mapYourHomeBody = "Veja a cobertura do Wi-Fi na planta da sua casa. Use uma foto da planta ou uma grade em branco."

    override val createMap = "Crie um mapa"

    override val namePlaceholder = "Andar de cima, escritório…"

    override val floorplanDescription = "Planta baixa do projeto"
    override fun coverageMapDescription(count: Int) =
        "Mapa de cobertura com ${readingCount(count)}. Toque onde você está para medir."

    override val networkNotSeenHere = "Esse Wi-Fi não foi encontrado aqui. Nenhuma medição foi salva."

    override val projectChangedWhileScanning = "O mapa mudou durante a busca. Nenhum ponto foi salvo."

    override val couldNotSaveReading = "Não foi possível salvar esta medição. As outras medições foram mantidas."

    override fun defaultMapName(index: Int) = if (index == 1) "Lar" else "Página inicial ${index}"
    override val stillSavingMap = "O mapa está sendo salvo. Aguarde um momento."

    override val noFreshScan = "O Android não forneceu novos resultados. Aguarde um pouco e tente novamente."

    override val couldNotScan = "Não foi possível digitalizar. Verifique Wi-Fi e configurações de localização."
    override val chooseAccessPointFirst = "Escolha um ponto de acesso antes de mapear sua cobertura."
    override val saveMeasurementBeforeExport = "Salve pelo menos uma medição antes de exportar."
    override val couldNotExportProject = "Não foi possível exportar este projeto. Tente novamente."

    override val connectToYourWifi = "Conecte-se ao seu Wi-Fi"
    override fun checkingRoom(room: String) = "Verificando ${room}… fique quieto"
    override val connectedWifi = "Wi-Fi conectado"
    override val connectedNoReading = "O dispositivo está conectado, mas ainda não há uma medição de sinal disponível."

    override val connectToCheck = "Conecte-se ao Wi-Fi que deseja verificar."
    override fun removedRoom(room: String) = "Removido ${room}"
    override val undo = "Desfazer"
    override val roomsEmptyHint = "Fique parado em um cômodo e salve uma medição. Meça em outro cômodo para descobrir onde o sinal fica mais fraco."

    override val noWifiSignal = "Sem sinal Wi-Fi"
    override val saveRoom = "Salvar cômodo"
    override fun dbStronger(delta: Int) = "${delta} dB mais forte"
    override fun dbWeaker(delta: Int) = "${delta} dB mais fraco"
    override val savedRooms = "Salas salvas"
    override fun deleteRoom(room: String) = "Excluir ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) = "${room}, sinal ${quality}, ${reading}. Veja as medições e meça este local novamente."

    override val noSignalToSave = "Nenhum sinal Wi-Fi para salvar. Conecte-se ao seu Wi-Fi primeiro."
    override val allowScansToLink = "Permita as buscas de Wi-Fi em Redes para vincular esta medição à sua rede."

    override val connectToSpotNetwork = "Conecte-se à rede deste local antes de verificá-lo novamente."
    override fun savedRoom(room: String) = "Salvo ${room}"
    override val couldNotSaveStableReading = "Não foi possível salvar uma medição estável. Fique parado, mantenha a conexão com o mesmo Wi-Fi e tente novamente."

    override val whereAreYou = "Onde você está?"
    override val somewhereElse = "Em outro lugar"

    override fun savedAt(time: String) = "Salvo ${time}"
    override val moveRouterTip = "Tente mover o roteador para um ponto mais alto, mais central ou mais próximo desta sala."
    override val betterThanLastTime = "Melhor que da última vez"
    override val worseThanLastTime = "Pior que da última vez"
    override val sameAsLastTime = "Igual à última vez"
    override val earlierChecks = "Verificações anteriores"
    override val remove = "Remover"
    override fun connectToRecheck(ssid: String?) =
        "Conecte-se a ${ssid?.ifBlank { null } ?: "o mesmo Wi-Fi"} para verificar novamente."

    override val stepDeveloperOptionsTitle = "Ative as opções do desenvolvedor"
    override val stepDeveloperOptionsBody = "Em Configurações, abra **Sobre o telefone** e toque em **Número da versão** 7 vezes. Pode ser necessário confirmar o bloqueio de tela."

    override val openAboutPhone = "Abra Sobre o telefone"
    override val stepThrottlingTitle = "Desative a limitação de varredura Wi-Fi"
    override val stepThrottlingBody = "Em **Opções do desenvolvedor**, na seção **Redes**, desative **Limitar busca por Wi-Fi**. Os nomes podem variar conforme o dispositivo."

    override val openDeveloperOptions = "Abra as opções do desenvolvedor"
    override val stepComeBackTitle = "Volte aqui"
    override val stepComeBackBody = "Ao voltar, o WiFi Weakspot verifica a configuração automaticamente. Você pode reativar o limite a qualquer momento."

    override val hideComputerMethod = "Ocultar passos para computador"

    override val useAdbInstead = "Configurar pelo computador com ADB"

    override val adbCommandClipLabel = "ADB comando"
    override val hideRootOption = "Ocultar opção de root"

    override val useRootInstead = "Configurar com acesso root"

    override val scanLimitFootnote = "Android limita as varreduras para economizar bateria. Você pode reativar o limite a qualquer momento."
    override val unlimitedWithRootTitle = "Varreduras ilimitadas com root"
    override val unlimitedOnTitle = "As verificações ilimitadas estão ativadas"
    override val unlimitedOnBody = "As redes próximas e os gráficos de canais são atualizados a cada poucos segundos."
    override val scansLimitedTitle = "As verificações são limitadas"
    override val scansLimitedAndroid9Body = "O Android 9 permite no máximo 4 buscas a cada 2 minutos e não permite remover esse limite. Durante a espera, o WiFi Weakspot exibe os resultados mais recentes."

    override val usingRoot = "Usando root"
    override val deviceRooted = "Seu dispositivo está enraizado"
    override val rootScanningBody = "As verificações passam pelo shell do sistema, portanto não há nada a alterar nas opções do desenvolvedor."
    override val rootDeniedBody = "O acesso root foi negado. Autorize o WiFi Weakspot no gerenciador de root e tente novamente, ou use as opções do desenvolvedor abaixo."

    override val rootNotNeededBody = "As varreduras já são ilimitadas. Root não é necessário."
    override val rootOfferBody = "Use root para buscar redes sem alterar as opções do desenvolvedor. Seu gerenciador de root pedirá permissão uma vez."

    override val stopUsingRoot = "Pare de usar root"
    override val waitingForRootManager = "Aguardando o gerenciador de root…"

    override val tryRootAgain = "Tente root novamente"
    override val useRoot = "Use root"
    override val copied = "Copiado"
    override val copyCommand = "Copiar comando"

}
