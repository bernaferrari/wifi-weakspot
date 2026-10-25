package com.bernaferrari.wifiweakspot.core.i18n.es

import com.bernaferrari.wifiweakspot.core.i18n.CommonStrings

object EsCommonStrings : CommonStrings {
    override fun deleteItemTitle(name: String) = "¿Eliminar ${name}?"

    override val couldNotSaveChanges = "No se pudieron guardar los cambios. Inténtalo de nuevo."
    override val couldNotDeleteItem = "No se pudo eliminar este elemento. Inténtalo de nuevo."

    override val hiddenNetworkShort = "Oculto"
    override val network = "Red"
    override val tools = "Herramientas"
    override val maker = "Fabricante"
    override val noReply = "Ninguna respuesta"
    override val fastest = "Lo más rápido"
    override val address = "DIRECCIÓN"
    override val responseTime = "Tiempo de respuesta"
    override val checkAgain = "Volver a comprobar"

    override val rename = "Rebautizar"
    override val stop = "Detener"
    override val unknown = "Desconocido"

    override val hiddenNetwork = "Red oculta"
    override val fasterScans = "Búsquedas más rápidas"
    override val openWifiSettings = "Abrir ajustes de Wi-Fi"
    override val signalUnavailable = "Señal no disponible"
    override val devices = "Dispositivos"
    override val openPorts = "Puertos abiertos"
    override val nameLabel = "Nombre"
    override val historyTitle = "Historial"
    override val routerCheck = "Comprobar router"
    override val testWebsite = "Probar un sitio web"
    override val yourRouter = "Tu router"
    override val yourNetwork = "Tu red"

    override val openInNewTab = "Abrir en una nueva pestaña"
    override val mapSharingAndroidOnly = "Compartir el mapa funciona en la aplicación Android."
    override val back = "Atrás"
    override val close = "Cerrar"
    override val cancel = "Cancelar"
    override val save = "Guardar"
    override val done = "Listo"
    override val share = "Compartir"
    override val delete = "Eliminar"

    override val tabHome = "Inicio"
    override val tabNetworks = "Redes"
    override val tabRooms = "Habitaciones"
    override val tabSettings = "Ajustes"

    override val permissionScreenTitle = "Permitir acceso a redes Wi-Fi cercanas"

    override val permissionAllowAccess = "Permitir acceso"
    override val openAppSettings = "Abrir configuración de la aplicación"

    override fun signalBadgeDescription(quality: String, reading: String) = "${quality} señal, ${reading}"

    override val scanReady = "LISTO"
    override fun scansCount(count: Int) = if (count == 1) "1 escaneo" else "${count} escaneos"
    override val scansLeftCaption = "restantes"
    override val scanWaitingCaption = "espera"
    override val scanDevMode = "MODO DESARROLLADOR"
    override val readyToScan = "Listo para escanear"
    override fun scansRemaining(count: Int) = if (count == 1) "Queda 1 escaneo" else "${count} escaneos restantes"
    override fun nextScanIn(time: String) = "Siguiente escaneo en ${time}"
    override val checking = "Comprobando…"

    override val justNow = "En este momento"
    override fun minutesAgo(minutes: Long) = "${minutes} min hace"
    override fun hoursAgo(hours: Long) = "${hours} hace"
    override fun daysAgo(days: Long) = "Hace ${days} días"
}
