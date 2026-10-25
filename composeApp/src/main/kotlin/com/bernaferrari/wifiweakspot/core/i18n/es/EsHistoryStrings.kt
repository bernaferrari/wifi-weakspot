package com.bernaferrari.wifiweakspot.core.i18n.es

import com.bernaferrari.wifiweakspot.core.i18n.HistoryStrings

object EsHistoryStrings : HistoryStrings {
    override val today = "Hoy"
    override val yesterday = "Ayer"
    override val weekdayNames = listOf("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo")
    override val weekdayShortNames = listOf("lun", "mar", "mié", "jue", "vie", "sáb", "dom")
    override val monthShortNames = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    override fun dateThisYear(weekday: String, day: Int, month: String) = "${weekday}, ${day} de ${month}"

    override fun dateOtherYear(weekday: String, day: Int, month: String, year: Int) = "${weekday}, ${day} de ${month} de ${year}"

    override fun momentAt(day: String, time: String) = "${day} en ${time}"

    override val filterAll = "Todo"
    override val kindInternetTests = "pruebas de internet"
    override val kindScans = "Escaneos"
    override val emptyTitle = "Nada guardado aún"
    override val emptySubtitle = "Aquí se guardan las comprobaciones del router, las búsquedas de redes y los resultados de las herramientas para comparar cómo cambia tu Wi-Fi."

    override fun trendBarsDescription(count: Int, readings: String) = "Últimas ${count} pruebas de enrutador: ${readings}"
    override val trendNoReply = "No hay respuesta del router. Algunos enrutadores ignoran estas comprobaciones."
    override val trendRecheckToCompare = "Vuelva a verificar después de un cambio para comparar."
    override fun trendQuicker(usualMs: String) = "Más rápido de lo habitual (${usualMs} ms)"
    override fun trendSlower(usualMs: String) = "Más lento de lo habitual (${usualMs} ms)"
    override fun trendAboutSame(usualMs: String) = "Casi lo mismo de siempre (${usualMs} ms)"

    override fun internetTitle(mbps: String) = "${mbps} Mbps descargar"
    override fun routerTitle(ms: String) = "${ms} ms al enrutador"
    override fun networksNearby(count: Int) = if (count == 1) "1 red cercana" else "${count} redes cercanas"
    override fun strongestNetwork(network: String) = "Más fuerte: ${network}"
    override fun accessPoints(count: Int) = if (count == 1) "1 punto de acceso" else "${count} puntos de acceso"
    override fun devicesFound(count: Int) = if (count == 1) "1 dispositivo encontrado" else "${count} dispositivos encontrados"
    override fun listWithMore(list: String, more: Int) = if (more > 0) "${list} y ${more} más" else list
    override fun pingSummary(averageMs: String, replied: Int, sent: Int) = "${averageMs} ms · ${replied} de ${sent} respondieron"

    override fun lookupTitle(target: String) = "Consulta DNS: ${target}"

    override val noRecords = "Sin registros"
    override val lookupFailed = "La búsqueda falló"
    override fun openPortsTitle(target: String) = "Abrir puertos en ${target}"
    override val noCommonPortsOpen = "Ninguno de los puertos comunes estaba abierto."
    override val completed = "Terminado"
    override val failed = "Fallido"

    override val metricDownload = "Descargar"
    override val metricUpload = "Subir"
    override val metricLatency = "Estado latente"
    override val metricVariation = "Variación"
    override val metricLost = "Perdido"
    override fun metricOnBand(band: String) = "en ${band}"
    override val metricReplied = "Respondió"
    override val metricReplies = "Respuestas"
    override val metricAverage = "Promedio"
    override val factServer = "Servidor"
    override val factRoom = "Habitación"
    override val factBand = "Banda"
    override val factSignal = "Señal"
    override val factAccessPoints = "Puntos de acceso"
    override fun signalFact(quality: String, rssi: Int) = "${quality} señal · ${rssi} dBm"

    override val linesNetworksStrongestFirst = "Redes, las más fuertes primero"
    override val linesRecords = "Archivos"
    override fun replyNumber(index: Int) = "Responder ${index}"
    override val scanNote = "Las redes cercanas en el mismo canal que el tuyo compiten por el tiempo aire."

    override val go = "IR"
    override val testInternetSpeedInstead = "Prueba la velocidad de Internet en su lugar"
    override val keepDeviceStill = "Mantenga tu dispositivo quieto durante unos segundos."
    override val connectToStart = "Conéctate a tu Wi-Fi para comenzar."
    override val lastTime = "Último tiempo"
    override val couldNotSaveSpot = "No se pudo guardar este lugar. Intentar otra vez."
    override val connectFirst = "Conéctate a tu Wi-Fi primero. Esta verificación mide las respuestas de tu router."
    override val historyNotSaved = "Se midió el resultado, pero no se pudo guardar el historial."
    override val checkStopped = "La conexión cambió o se detuvo la verificación del router. Mantén este Wi-Fi conectado y vuelve a intentarlo. Algunos enrutadores bloquean el ping."

    override val internetSpeedTitle = "Velocidad de Internet · fast.com"
}
