package com.bernaferrari.wifiweakspot.core.i18n.es

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object EsAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = if (count == 1) "1 red" else "${count} redes"

    override fun channelShort(channel: Int) = "Ch ${channel}"
    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · Ch ${channel} · ${band}"
    override val noPassword = "Sin contraseña"
    override val wifiOffTitle = "Wi-Fi está apagado"
    override val locationOffBody = "Android necesita la ubicación activada para mostrar las redes Wi-Fi cercanas. WiFi Weakspot no usa GPS."

    override val scanPermissionBody = "Android solo comparte nombres de redes cercanas con aplicaciones que tienen acceso a la ubicación. WiFi Weakspot nunca registra dónde estás."
    override val wifiOffBody = "Activa Wi-Fi para ver redes cercanas."
    override val locationOffTitle = "La ubicación está apagada"
    override val turnOnLocation = "Activar ubicación"
    override val scanPausedTitle = "Android escaneo en pausa"
    override fun scanPausedBody(seconds: Long) = "El siguiente escaneo está permitido en ${seconds} s. Desactive la aceleración del escaneo para evitar la espera."
    override val turnOffThrottling = "Desactivar la aceleración"
    override val readyToScanBody = "Toque escanear para ver todas las redes cercanas."
    override val scan = "Buscar"
    override val channels = "Canales"
    override val signalOverTime = "Señal a lo largo del tiempo"
    override val howToReadThisChart = "Cómo leer este gráfico"
    override val expandSignalHistory = "Ampliar el historial de señales"
    override val expandChannels = "Ampliar canales"
    override fun noBandNetworksNearby(band: String) = "No hay redes ${band} cercanas."
    override fun networksOnBand(count: Int, band: String) =
        if (count == 0) "No hay redes en ${band}" else "${networks(count)} en ${band}"
    override fun showVeryWeakNetworks(count: Int) =
        if (count == 1) "Mostrar 1 red muy débil" else "Mostrar ${count} redes muy débiles"
    override fun hideVeryWeakNetworks(count: Int) =
        if (count == 1) "Ocultar 1 red muy débil" else "Ocultar ${count} redes muy débiles"
    override fun bandNetworkCount(count: Int) = if (count == 0) "ninguno" else networks(count)
    override val highlightedOnChart = "Destacado en el gráfico"
    override val notHighlightedOnChart = "No resaltado en el gráfico"
    override fun detailsForNetwork(ssid: String) = "Detalles para ${ssid.ifEmpty { "red oculta" }}"
    override fun alsoYourRouter(quality: String) = "${quality} · También tu enrutador"
    override val connected = "Conectado"

    override val scanReportHeading = "WiFi Weakspot Informe de escaneo"
    override fun scanReportTime(time: String) = "Hora: ${time}"
    override fun scanReportTotal(count: Int) = "Redes totales: ${count}"
    override val scanReportNetworksHeading = "Redes:"
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) =
        "• ${ssid} - ${rssi}dBm, Ch ${channel}, ${security}"
    override val channelLooksClear = "Tu canal se ve claro."
    override val noChannelImprovement = "No se encontró ninguna mejora en este escaneo."
    override fun lessOverlapThanChannel(currentChannel: Int) = "Menos superposición que el canal ${currentChannel}"
    override val leastOverlapInScan = "Menor superposición en este escaneo"
    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}: canal ${channel}"
    override val lowOverlap = "Superposición baja"
    override val moderateOverlap = "Superposición moderada"
    override val highOverlap = "Alta superposición"
    override val veryHighOverlap = "Superposición muy alta"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) =
        "Gráfico de canales, ${networks(networkCount)}." +
            (if (busiestChannel != null) " Canal más ocupado ${busiestChannel} con ${busiestCount}." else "")
    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) =
        name + (if (isYou) " (tú)" else "") + (if (extraNames > 0) " +${extraNames}" else "")
    override val busyCaption = "Ocupado"
    override val axisStrong = "Fuerte"
    override val axisOkay = "Aceptable"
    override val axisWeak = "Débil"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · Ch ${channel}"
    override val clearSelectedNetwork = "Borrar red seleccionada"
    override val tapWaveForDetails = "Toca un saludo para obtener detalles de la red"

    override val chartGuideTitle = "Leyendo los gráficos"
    override val guideTallerBody = "La altura es la intensidad de la señal."
    override val guideOverlapBody = "Las redes superpuestas comparten espectro y pueden competir por el tiempo aire."
    override val guideWiderBody = "Un canal más amplio ocupa más espectro y puede superponerse a más vecinos."
    override val guideSameRouterBody = "+1: Los nombres de red que parecen usar el mismo módulo Wi-Fi comparten una curva."

    override val guideCrowdBody = "El verde indica menos superposición; el rojo, más. La franja debajo de 2.4 GHz muestra superposición. Tócalo para contar."
    override val guideSteadyBody = "Una línea con saltos muestra los cambios en la intensidad de la señal. Toque una línea para hacer una pausa y luego deslícese hacia los lados en el tiempo o verticalmente entre redes. Reanude en vivo para ver nuevas lecturas."
    override val guideHighlightBody = "Toque una colina o fila para resaltarla. Arrastre horizontalmente por el gráfico para explorar."

    override fun channelOverlapHeading(count: Int, channel: Int) = when (count) {
        0 -> "El canal ${channel} está limpio"
        1 -> "1 red se superpone al canal ${channel}"
        else -> "${count} redes se superponen al canal ${channel}"
    }
    override fun fromChannel(homeChannel: Int, reading: String) = "Del canal ${homeChannel} · ${reading}"

    override val estimatedChannelOverlap = "Superposición de canales estimada"
    override fun tryChannel(channel: Int) = "Prueba el canal ${channel}"
    override val lowestOverlapFor20Mhz = "Superposición estimada más baja para 20 MHz canales"
    override val overlapDisclaimer = "Basado en puntos de acceso cercanos y en la intensidad de la señal, no en el tráfico medido. " +
        "Verifique los canales admitidos y las reglas locales de su enrutador antes de cambiar la configuración. " +
        "Las sugerencias 5 y 6 GHz requieren soporte de enrutador conocido; faltan centros de canal ancho " +
        "muestra solo el canal principal 20 MHz."
    override val overlapScore = "Puntuación de superposición"

    override val collectingReadings = "Recopilando lecturas... Las líneas aparecen después de un par de escaneos."
    override fun signalHistoryDescription(networkCount: Int) =
        "Historial de señales para ${networks(networkCount)}. Toque una línea para inspeccionar. Las teclas de flecha se mueven a través del tiempo y las redes."
    override val historyLive = "Vivir"
    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) = "${networkName}, ${reading ?: "sin medición"}, en pausa, ${readingsAgo(readingsAgo)}"

    override val strongerNetwork = "Red más fuerte"
    override val weakerNetwork = "Red más débil"
    override val axisOlder = "anterior"
    override val axisLatest = "reciente"
    override val exploreSignalHistory = "Explorar el historial de señales"
    override val tapLineToCompare = "Toca una línea y luego arrástrala para comparar."
    override val noReadingAtPoint = "No hay medición en este punto"

    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) = "${reading} · ${quality} · " + if (readingsAgo == 0) "última medición" else readingsAgo(readingsAgo)

    private fun readingsAgo(count: Int) = if (count == 1) "hace 1 medición" else "hace $count mediciones"

    override val resumeLive = "Reanudar en vivo"

    override fun seenReading(rssi: Int, seen: String) = "${rssi} dBm · visto ${seen}"
    override val factChannel = "Canal"
    override val factWidth = "Ancho"
    override val factSecurity = "Seguridad"
    override val factDistance = "Estimación de distancia"
    override val notAvailable = "No disponible"
    override val watch = "Mirar"
    override val addToFavorites = "Añadir a favoritos"
    override val removeFromFavorites = "Quitar de favoritos"

    override fun routerCheckLastTime(pingMs: String) = "${pingMs} ms la última vez"
    override fun devicesFoundLastScan(count: Int) = "${count} encontró el último escaneo"
    override val devicesSubtitle = "¿Quién está conectado?"
    override val testWebsiteSubtitle = "Ping, DNS, puertos"
    override fun savedChecks(count: Int) = when (count) {
        0 -> "Resultados pasados"
        1 -> "1 cheque"
        else -> "${count} cheques"
    }
    override val notOnWifi = "No en Wi-Fi"
    override val online = "En línea"
    override val internetNotConfirmed = "Internet no confirmado"
    override fun channelChip(channel: Int) = "Canal ${channel}"
    override fun linkRateChip(mbps: Int) = "Tasa de enlace ${mbps} Mbps"
    override val chooseNetwork = "Elige una red"
    override val turnOnWifi = "Enciende Wi-Fi"
    override val dismiss = "Despedir"
    override val findWeakSpotsTitle = "Encuentra puntos débiles"
    override val findWeakSpotsBody = "Vea qué habitaciones son débiles."
    override val findWeakSpotsAction = "Revisa tus habitaciones"
    override fun networksNearby(count: Int) = "${networks(count)} cerca"
    override val nearbyStale = "De tu último escaneo. Toque para actualizar."
    override val nearbyNoConnection = "Ver canales y señal de cada red."
    override fun nearbyOverlap(count: Int, channel: Int) = when (count) {
        0 -> "No se detectó ninguna superposición cercana en el canal ${channel}."
        1 -> "1 señal cercana se superpone a tu canal ${channel}."
        else -> "${count} las señales cercanas se superponen a tu canal ${channel}."
    }

    override val adviceNoWifiBody = "Conéctese a la red que desea verificar. Aún puedes ejecutar pruebas de Internet y herramientas de red usando tu conexión actual."
    override val adviceNoInternetTitle = "Primero verifique el acceso a Internet"
    override val adviceNoInternetBody = "Android no ha confirmado el acceso a Internet en esta conexión Wi-Fi. Busque una página de inicio de sesión y luego pruebe la conectividad con su enrutador y un servidor de Internet."
    override val adviceNoSignalTitle = "Lee tu señal Wi-Fi"
    override val adviceNoSignalBody = "Permita escaneos Wi-Fi en Redes para ver mediciones de señal. Compara la señal en la habitación donde notes problemas."
    override val adviceWeakSignalTitle = "Compara la cobertura cerca de tu router"
    override val adviceWeakSignalBody = "La señal Wi-Fi actual es débil. Ejecute una verificación del enrutador aquí, luego repita cerca del enrutador. Utilice una encuesta de su propia red para encontrar puntos débiles."
    override val adviceGoodSignalTitle = "Comparar la capacidad de respuesta del enrutador"
    override val adviceGoodSignalBody = "La intensidad de la señal parece utilizable y Android informa acceso a Internet. Si la conexión se siente lenta, compare la respuesta del enrutador aquí y cerca del enrutador; Una señal fuerte por sí sola no garantiza el rendimiento."
}
