package com.bernaferrari.wifiweakspot.core.i18n.es

import com.bernaferrari.wifiweakspot.core.i18n.SettingsStrings

object EsSettingsStrings : SettingsStrings {
    override val sectionBackup = "Copia de seguridad"
    override val backupScope = "Guarda mapas, imágenes de planos y mediciones de habitaciones en un archivo que elijas. No incluye ajustes ni historial de diagnósticos."
    override val exportBackup = "Guardar copia"
    override val restoreBackup = "Restaurar copia"
    override val restoreBackupExplanation = "Elige una copia de WiFi Weakspot. Los mapas se añaden como copias y se conservan las mediciones existentes. No se reemplazará tu trabajo actual."
    override val backupSaved = "Copia guardada."
    override val backupRestored = "Copia restaurada."
    override val backupAlreadyRestored = "Esta copia ya se restauró."
    override val backupExportFailed = "No se pudo guardar la copia. Comprueba el espacio libre e inténtalo de nuevo."
    override val backupRestoreFailed = "No se pudo completar la restauración. Comprueba el archivo y el espacio libre y vuelve a intentarlo con el mismo archivo."

    override val sectionScanning = "Exploración"
    override val fasterScansThrottled = "Android permite 4 escaneos cada 2 minutos"
    override val sectionAppearance = "Apariencia"
    override val themeAuto = "Auto"
    override val themeLight = "Claro"
    override val themeDark = "Oscuro"
    override val showSignalAs = "Mostrar señal como"
    override val sectionComfort = "Comodidad"
    override val reduceMotionTitle = "Reducir animaciones"

    override val hapticsSubtitle = "Vibrar al tocar y al mostrar resultados"

    override val sectionAlerts = "Alertas"
    override val backgroundAlertsTitle = "Alertas de fondo"
    override val backgroundAlertsSubtitle = "Cuando cambian las redes Wi-Fi cercanas"

    override val notificationsDenied = "Permitir notificaciones para ver el estado de monitoreo y alertas."
    override val checkEvery = "Intervalo de comprobación"

    override fun intervalSeconds(seconds: Int) = "${seconds} seg"
    override fun intervalMinutes(minutes: Int) = "${minutes} minutos"
    override val newNetworkAlertsTitle = "Nuevas alertas de red"
    override val signalChangeAlertsTitle = "Alertas de cambio de señal"
    override val signalChangeAlertsSubtitle = "Cuando tu señal sube o baja mucho"
    override val sectionAbout = "Acerca de"
    override fun aboutApp(appName: String) = "Acerca de ${appName}"
    override fun version(version: String) = "Versión ${version}"
    override val replayIntroTitle = "Mostrar la introducción nuevamente."
    override val wallpaperColors = "Colores del papel tapiz"
    override val colorIndigo = "Índigo"
    override val colorOrange = "Naranja"
    override val colorAmber = "Ámbar"
    override val colorGreen = "Verde"
    override val colorTeal = "verde azulado"
    override val colorCyan = "cian"
    override val colorBlue = "Azul"
    override val colorPurple = "Púrpura"
    override val colorPink = "Rosa"

    override val tagline = "Encuentra los puntos débiles de tu Wi-Fi"
    override val openSourceLicense = "Código abierto · Apache 2.0"
    override val feedbackTitle = "Comentario"
    override val feedbackBody = "Ideas e informes de errores."
    override val elsewhere = "Otros enlaces"

    override val privacySummary = "Tus resultados se quedan en tu dispositivo, salvo que los compartas."
    override val privacyPolicy = "Política de privacidad"

    override val notNow = "Ahora no"
    override val getStarted = "empezar"
    override val welcomeHeadline = "Encuentra dónde\nfalla tu Wi-Fi"

    override val welcomeBody = "Descubre dónde falla y cómo mejorarlo."
    override val featureRoomsTitle = "Revisa cada habitación"
    override val featureRoomsBody = "Encuentra las habitaciones donde la señal es débil."

    override val featureRouterTitle = "Comprueba tu enrutador"
    override val featureRouterBody = "Mira lo rápido que responde."
    override val featureNearbyTitle = "Ver redes cercanas"
    override val featureNearbyBody = "Encuentra un canal con menos interferencias."

}
