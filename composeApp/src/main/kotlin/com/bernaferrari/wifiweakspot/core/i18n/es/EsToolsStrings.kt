package com.bernaferrari.wifiweakspot.core.i18n.es

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object EsToolsStrings : ToolsStrings {
    override val tryAgain = "Intentar otra vez"

    override val targetPlaceholder = "Sitio web o dirección IP"
    override val clearTarget = "Claro"
    override val checkSectionTitle = "Controlar"
    override val recentSectionTitle = "Reciente"

    override val toolPortsDescription = "que esta abierto"

    override val pingQuestion = "¿Responde?"
    override val dnsQuestion = "¿Cuál es su dirección?"
    override val dnsDetail = "DNS búsqueda"
    override val portsQuestion = "¿Qué ofrece?"

    override val runningPortScanTitle = "Comprobando puertos"

    override val diagnosticFailed = "Error de diagnóstico"
    override val invalidTarget = "Ingrese un nombre de host o una dirección IP sin una ruta URL ni espacios."
    override val diagnosticCouldNotComplete = "No se pudo completar el diagnóstico."
    override val connectionChangedRunAgain = "La conexión cambió. Ejecute esta verificación nuevamente en la nueva red."

    override val openItsWebPage = "Abre su página web"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ${answered} de ${total} respondieron"

    override val slowest = "El más lento"
    override fun repliesContentDescription(replies: String) = "Respuestas: ${replies}"

    override val pingDidntAnswer = "no respondi"
    override val pingResponsive = "Sensible"
    override val pingResponsiveButUneven = "Responsivo, pero desigual"
    override val pingDroppingReplies = "Dejar respuestas"
    override val pingSlowToAnswer = "lento para responder"

    override val pingNoAnswerLocal = "Puede que esté apagado, dormido o configurado para ignorar los pings."
    override fun pingRepliesLost(lost: Int, total: Int) =
        "${lost} de ${total} respuestas desaparecieron. Compare la verificación del enrutador con la de un servidor de Internet para ayudar a localizar el problema; esta prueba por sí sola no puede identificar la causa."
    override val pingUneven = "Los tiempos de respuesta variaron. Compare una verificación de enrutador aquí y cerca del enrutador, luego pruebe un host de Internet."
    override val pingLocalExplanation = "Es hora de llegar a este dispositivo y regresar. El propio dispositivo puede afectar el resultado."
    override val pingInternetExplanation = "Es hora de llegar a este anfitrión y regresar. Su conexión, la ruta y el host afectan el resultado."

    override fun dnsAddressUnit(count: Int) = if (count == 1) "DIRECCIÓN" else "direcciones"
    override fun dnsForQuery(query: String) = "Para ${query}"
    override val dnsNotFound = "No pude encontrarlo"
    override val dnsFound = "lo encontré"
    override val dnsNotFoundExplanation = "Revisa la ortografía o inténtalo de nuevo; Es posible que el nombre no exista."
    override fun dnsFoundExplanation(query: String) = "Su dispositivo puede encontrar ${query}."
    override val dnsAddressesTitle = "Direcciones"

    override val portsOpenUnit = "abierto"
    override fun portsCommonServicesOn(host: String) = "Servicios comunes en ${host}"
    override val portsNothingOpen = "Nada abierto"
    override fun portsOffersServices(count: Int) = if (count == 1) "Ofrece 1 servicio" else "Ofrece ${count} servicios"
    override val portsNoneAnswered = "Ningún servicio común respondió."
    override val portsOffersExplanation = "Cosas como una página de configuración o compartir archivos."

    override val portWebPage = "Página web"
    override val portSecureWebPage = "pagina web segura"
    override val portRemoteLoginSsh = "Inicio de sesión remoto (SSH)"
    override val portOldRemoteLoginTelnet = "Inicio de sesión remoto antiguo (Telnet)"
    override val portFileTransferFtp = "Transferencia de archivos (FTP)"
    override val portAddressLookupsDns = "Búsquedas de direcciones (DNS)"
    override val portFileSharing = "compartir archivos"
    override val portMacFileSharing = "compartir archivos mac"
    override val portPrinting = "Impresión"
    override val portRemoteDesktop = "Escritorio remoto"
    override val portScreenSharing = "Compartir pantalla"
    override val portDeviceDiscoveryUpnp = "Descubrimiento de dispositivos (UPnP)"
    override val portStorageOrAdminPage = "Página de almacenamiento o administración"
    override val portDatabase = "Base de datos"
    override val portService = "Servicio"
    override val devicesLooking = "Buscando dispositivos..."
    override val devicesFindOnWifi = "Encuentra dispositivos en tu Wi-Fi"
    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${count} en ${ssid}"
    override fun devicesOnYourNetwork(count: Int) = "${count} en tu red"
    override val couldntLookForDevices = "No se pudieron buscar dispositivos"
    override val scanFailed = "Error de escaneo"
    override val connectionChangedLookAgain = "La conexión cambió. Mire nuevamente en esta red."
    override val stoppedLooking = "dejó de mirar"
    override val noDevicesFound = "No se encontraron dispositivos"
    override val noDevicesFoundSubtitle =
        "Algunos dispositivos permanecen en silencio cuando se les solicita. Inténtalo de nuevo en un momento o comprueba que este dispositivo esté en tu casa Wi-Fi."
    override fun otherDevicesGroup(count: Int) = "Otros dispositivos · ${count}"
    override fun quietDevices(count: Int) = if (count == 1) "1 dispositivo silencioso" else "${count} dispositivos silenciosos"
    override val quietDevicesSubtitle = "No dijeron cuales son"
    override val hide = "Esconder"
    override val show = "Espectáculo"
    override fun checkingLocalAddresses(percent: Int) = "Comprobando direcciones locales · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · no respondió"

    override val deviceNotScannedYet = "Aún no encontrado en un escaneo"
    override val deviceAnsweredLastScan = "Respondió el último escaneo."
    override val deviceSeenButNoAnswer = "Visto en la red, pero no respondió."
    override val actionCheckPorts = "comprobar puertos"
    override val openRouterSettings = "Abrir la configuración del enrutador"
    override val detailsTitle = "Detalles"
    override val factNetworkName = "Nombre de la red"
    override val factHardwareAddress = "Dirección de hardware"
    override val macHiddenByAndroid = "Oculto por Android"
    override val factYourName = "Tu nombre para ello"
    override val portsNotCheckedYet = "Aún no probado."
    override fun portsNoneOpenWhenChecked(moment: String) =
        "Ninguno de los puertos comunes estaba abierto cuando se marcó ${moment.replaceFirstChar { it.lowercase() }}."
    override fun portsCheckedAt(moment: String) = "Marcado ${moment.replaceFirstChar { it.lowercase() }}."
    override val renameDeviceTitle = "Cambiar nombre del dispositivo"
    override val renameSupporting = "Déjelo vacío para usar el nombre WiFi Weakspot encontrado."
    override val connectionChangedOpenAgain = "La conexión cambió. Abra este dispositivo nuevamente en la nueva red."
    override val couldntLoadDetails = "No se pudieron cargar los detalles guardados. Intente abrir este dispositivo nuevamente."
    override val scanBeforeNaming = "Escanee este dispositivo antes de guardar un nombre."

}
