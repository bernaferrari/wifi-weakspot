package com.bernaferrari.wifiweakspot.core.i18n.ptbr

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object PtBrSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "Medindo... fique quieto"
    override val tapWhereYouAreStanding = "Toque no mapa onde você está"
    override val chooseWifi = "Escolha Wi-Fi"
    override val undoLastReading = "Desfazer a última leitura"
    override val allReadings = "Todas as leituras"
    override val floorPlanTitle = "Planta baixa"
    override fun readingCount(count: Int) = if (count == 1) "1 leitura" else "${count} leituras"
    override val mapOptions = "Opções de mapa"
    override val usePlanPhoto = "Use uma foto do seu plano"
    override val changePhoto = "Mude a foto"
    override val removePhoto = "Remova a foto"
    override val exportReadingsCsv = "Exportar leituras (CSV)"
    override fun floorName(floor: Int) = if (floor == 0) "Porão" else "Piso ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "Piso ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "Adicione um porão" else "Adicionar piso ${floor}"
    override val deleteFloorReadingsAction = "Excluir as leituras deste andar"
    override fun deleteFloorBody(count: Int) =
        if (count == 1) "Sua 1ª leitura será excluída." else "Suas leituras ${count} serão excluídas."

    override val oneRouterAtATime = "Um mapa segue um roteador por vez. Os sistemas de malha mostram cada unidade separadamente."
    override val noNetworksFound = "Nenhuma rede encontrada ainda. Verifique se Wi-Fi e localização estão ativados."
    override val scanning = "Buscando…"
    override val refresh = "Atualizar"
    override val switchWifiTitle = "Mudar para outro Wi-Fi?"
    override val switchWifiBody = "Isso limpa as leituras em todos os andares deste mapa. Crie um novo mapa para manter ambos."
    override val clearAndSwitch = "Limpar e mudar"

    override val deleteMapBody = "Todas as leituras em cada andar serão excluídas."

    override val noReadingsYet = "Nenhuma leitura aqui ainda. Toque no mapa onde você está."
    override val weakestFirst = "Os mais fracos primeiro, então os pontos problemáticos ficam no topo."
    override val mapYourHome = "Mapeie sua casa"
    override val mapYourHomeBody = "Veja o seu Wi-Fi no mapa da sua casa. Use uma foto do seu plano ou uma grade em branco."
    override val createMap = "Crie um mapa"

    override val namePlaceholder = "No andar de cima, escritório…"

    override val floorplanDescription = "Planta baixa do projeto"
    override fun coverageMapDescription(count: Int) =
        "Mapa de cobertura com ${readingCount(count)}. Toque onde você está para medir."

    override val networkNotSeenHere = "Esse Wi-Fi não apareceu aqui. Nada foi salvo."
    override val projectChangedWhileScanning = "O projeto mudou durante a busca. Nenhum ponto foi salvo."
    override val couldNotSaveReading = "Não foi possível salvar essa leitura. Suas outras leituras estão seguras."
    override fun defaultMapName(index: Int) = if (index == 1) "Lar" else "Página inicial ${index}"
    override val stillSavingMap = "Um momento, ainda salvando o mapa."
    override val noFreshScan = "Android não forneceu uma nova varredura. Tente novamente em breve."
    override val couldNotScan = "Não foi possível digitalizar. Verifique Wi-Fi e configurações de localização."
    override val chooseAccessPointFirst = "Escolha um ponto de acesso antes de mapear sua cobertura."
    override val saveMeasurementBeforeExport = "Salve pelo menos uma medição antes de exportar."
    override val couldNotExportProject = "Não foi possível exportar este projeto. Tente novamente."

    override val connectToYourWifi = "Conecte-se ao seu Wi-Fi"
    override fun checkingRoom(room: String) = "Verificando ${room}… fique quieto"
    override val connectedWifi = "Wi-Fi conectado"
    override val connectedNoReading = "Seu dispositivo está conectado, mas nenhuma leitura de sinal ainda está disponível."
    override val connectToCheck = "Conecte-se ao Wi-Fi que deseja verificar."
    override fun removedRoom(room: String) = "Removido ${room}"
    override val undo = "Desfazer"
    override val roomsEmptyHint =
        "Fique em uma sala e salve uma leitura. Verifique outra sala para descobrir onde o sinal fica mais fraco."
    override val noWifiSignal = "Sem sinal Wi-Fi"
    override val saveRoom = "Salvar quarto"
    override fun dbStronger(delta: Int) = "${delta} dB mais forte"
    override fun dbWeaker(delta: Int) = "${delta} dB mais fraco"
    override val savedRooms = "Salas salvas"
    override fun deleteRoom(room: String) = "Excluir ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) =
        "${room}, ${quality} sinal, ${reading}. Revise as leituras e verifique novamente este local."

    override val noSignalToSave = "Nenhum sinal Wi-Fi para salvar. Conecte-se ao seu Wi-Fi primeiro."
    override val allowScansToLink = "Permita varreduras Wi-Fi em Redes para que esta leitura possa ser vinculada à sua rede."
    override val connectToSpotNetwork = "Conecte-se à rede deste local antes de verificá-lo novamente."
    override fun savedRoom(room: String) = "Salvo ${room}"
    override val couldNotSaveStableReading = "Não foi possível salvar uma leitura estável. Continue no mesmo Wi-Fi e tente novamente."
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
    override val stepDeveloperOptionsBody =
        "Em Configurações, abra **Sobre o telefone** e toque em **Número da versão** 7 vezes. Pode ser necessário inserir o bloqueio de tela."
    override val openAboutPhone = "Abra Sobre o telefone"
    override val stepThrottlingTitle = "Desative a limitação de varredura Wi-Fi"
    override val stepThrottlingBody =
        "Em **Opções do desenvolvedor**, role para baixo até **Rede** e desative **Wi-Fi otimização de varredura**."
    override val openDeveloperOptions = "Abra as opções do desenvolvedor"
    override val stepComeBackTitle = "Volte aqui"
    override val stepComeBackBody =
        "WiFi Weakspot verifica automaticamente quando você retorna. Você pode reativar a aceleração a qualquer momento."
    override val hideComputerMethod = "Ocultar método de computador"
    override val useAdbInstead = "Prefere um computador? Use ADB"
    override val adbCommandClipLabel = "ADB comando"
    override val hideRootOption = "Ocultar opção root"
    override val useRootInstead = "Dispositivo enraizado? Use root em vez disso"
    override val scanLimitFootnote = "Android limita as varreduras para economizar bateria. Você pode reativar o limite a qualquer momento."
    override val unlimitedWithRootTitle = "Varreduras ilimitadas com root"
    override val unlimitedOnTitle = "As verificações ilimitadas estão ativadas"
    override val unlimitedOnBody = "As redes próximas e os gráficos de canais são atualizados a cada poucos segundos."
    override val scansLimitedTitle = "As verificações são limitadas"
    override val scansLimitedAndroid9Body =
        "Android 9 permite 4 varreduras a cada 2 minutos e não pode desligar isso. WiFi Weakspot reutiliza resultados recentes no meio."
    override val usingRoot = "Usando root"
    override val deviceRooted = "Seu dispositivo está enraizado"
    override val rootScanningBody = "As verificações passam pelo shell do sistema, portanto não há nada a alterar nas opções do desenvolvedor."
    override val rootDeniedBody =
        "O acesso root foi negado. Permita WiFi Weakspot em seu gerenciador root e tente novamente ou use as opções do desenvolvedor abaixo."
    override val rootNotNeededBody = "As varreduras já são ilimitadas. Root não é necessário."
    override val rootOfferBody =
        "Ignore as opções do desenvolvedor e deixe WiFi Weakspot examinar root. Seu gerente root perguntará uma vez."
    override val stopUsingRoot = "Pare de usar root"
    override val waitingForRootManager = "Esperando pelo seu gerente root…"
    override val tryRootAgain = "Tente root novamente"
    override val useRoot = "Use root"
    override val copied = "Copiado"
    override val copyCommand = "Comando copiar"
}
