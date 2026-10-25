package com.bernaferrari.wifiweakspot.core.i18n.ptbr

import com.bernaferrari.wifiweakspot.core.i18n.SettingsStrings

object PtBrSettingsStrings : SettingsStrings {
    override val sectionScanning = "Buscando"
    override val fasterScansThrottled = "Android permite 4 varreduras a cada 2 minutos"
    override val sectionAppearance = "Aparência"
    override val themeAuto = "Automático"
    override val themeLight = "Claro"
    override val themeDark = "Escuro"
    override val showSignalAs = "Mostrar sinal como"
    override val sectionComfort = "Conforto"
    override val reduceMotionTitle = "Reduzir animações"

    override val hapticsSubtitle = "Vibrar ao tocar e ao mostrar resultados"

    override val sectionAlerts = "Alertas"
    override val backgroundAlertsTitle = "Alertas em segundo plano"
    override val backgroundAlertsSubtitle = "Quando as redes Wi-Fi próximas mudam"

    override val notificationsDenied = "Permitir notificações para ver o status de monitoramento e alertas."
    override val checkEvery = "Intervalo de verificação"

    override fun intervalSeconds(seconds: Int) = "${seconds} seg"
    override fun intervalMinutes(minutes: Int) = "${minutes} minutos"
    override val newNetworkAlertsTitle = "Novos alertas de rede"
    override val signalChangeAlertsTitle = "Alertas de mudança de sinal"
    override val signalChangeAlertsSubtitle = "Quando seu sinal sobe ou cai muito"
    override val sectionAbout = "Sobre"
    override fun aboutApp(appName: String) = "Sobre ${appName}"
    override fun version(version: String) = "Versão ${version}"
    override val replayIntroTitle = "Mostrar a introdução novamente"
    override val wallpaperColors = "Cores do papel de parede"
    override val colorIndigo = "Índigo"
    override val colorOrange = "Laranja"
    override val colorAmber = "Âmbar"
    override val colorGreen = "Verde"
    override val colorTeal = "Azul-petróleo"

    override val colorCyan = "Ciano"
    override val colorBlue = "Azul"
    override val colorPurple = "Roxo"
    override val colorPink = "Rosa"

    override val tagline = "Encontre os pontos fracos do Wi-Fi"
    override val openSourceLicense = "Código aberto · Apache 2.0"
    override val feedbackTitle = "Opinião"
    override val feedbackBody = "Ideias e relatórios de bugs"
    override val elsewhere = "Outros links"

    override val privacySummary = "Seus resultados ficam no seu dispositivo, a menos que você os compartilhe."
    override val privacyPolicy = "Política de Privacidade"

    override val notNow = "Agora não"
    override val getStarted = "Comece"
    override val welcomeHeadline = "Encontre onde\no Wi-Fi fica fraco"

    override val welcomeBody = "Veja onde o sinal cai e como melhorar."
    override val featureRoomsTitle = "Verifique todos os cômodos"
    override val featureRoomsBody = "Encontre os cômodos onde o sinal fica fraco."

    override val featureRouterTitle = "Verifique seu roteador"
    override val featureRouterBody = "Veja com que rapidez ele responde."
    override val featureNearbyTitle = "Ver redes próximas"
    override val featureNearbyBody = "Encontre um canal com menos sobreposição."

}
