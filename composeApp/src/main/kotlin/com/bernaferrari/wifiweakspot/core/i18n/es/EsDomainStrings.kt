package com.bernaferrari.wifiweakspot.core.i18n.es

import com.bernaferrari.wifiweakspot.core.i18n.DomainStrings
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind
import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel

object EsDomainStrings : DomainStrings {

    override val signalDisplayBars = "Barras"
    override val qualityExcellentLabel = "Excelente"
    override val qualityExcellentHeadline = "Señal excelente"
    override val qualityExcellentExplanation = "La conexión con el router es fuerte. La señal por sí sola no mide la velocidad de internet."
    override val qualityGoodLabel = "Buena"
    override val qualityGoodHeadline = "Buena señal"
    override val qualityGoodExplanation = "Una señal sólida del enrutador."
    override val qualityFairLabel = "Aceptable"
    override val qualityFairHeadline = "Señal aceptable"
    override val qualityFairExplanation = "La señal es utilizable, pero tolera menos interferencias."
    override val qualityWeakLabel = "Débil"
    override val qualityWeakHeadline = "Punto débil"
    override val qualityWeakExplanation = "Una señal débil. Compare este lugar con uno más cercano al enrutador."
    override val qualityDeadZoneLabel = "Sin cobertura"
    override val qualityDeadZoneExplanation = "A este lugar llega muy poca señal."

    override val deviceKindRouter = "Router"
    override val deviceKindThisDevice = "Este dispositivo"
    override val deviceKindPhone = "Teléfono o tablet"
    override val deviceKindComputer = "Computadora"
    override val deviceKindTv = "TV o transmisor"
    override val deviceKindSpeaker = "Altavoz"
    override val deviceKindPrinter = "Impresora"
    override val deviceKindConsole = "consola de juegos"
    override val deviceKindCamera = "Cámara"
    override val deviceKindSmartHome = "Dispositivo doméstico inteligente"
    override val deviceKindUnknown = "Dispositivo"
    override val unnamedDevice = "Dispositivo sin nombre"

    override fun makerDevice(maker: String, kind: DeviceKind): String = "${kindLabel(kind)} ${maker}"

    private fun kindLabel(kind: DeviceKind): String = when (kind) {
        DeviceKind.Router -> deviceKindRouter
        DeviceKind.ThisPhone -> deviceKindThisDevice
        DeviceKind.Phone -> deviceKindPhone
        DeviceKind.Computer -> deviceKindComputer
        DeviceKind.Tv -> deviceKindTv
        DeviceKind.Speaker -> deviceKindSpeaker
        DeviceKind.Printer -> deviceKindPrinter
        DeviceKind.Console -> deviceKindConsole
        DeviceKind.Camera -> deviceKindCamera
        DeviceKind.SmartHome -> deviceKindSmartHome
        DeviceKind.Unknown -> deviceKindUnknown
    }

    override val verdictGreatHeadline = "El enrutador responde rápidamente"
    override val verdictGreatExplanation = "Las respuestas fueron rápidas y constantes."
    override val verdictGoodHeadline = "La respuesta del enrutador es utilizable"
    override val verdictGoodExplanation = "Algunas respuestas fueron un poco lentas o desiguales."
    override val verdictSlowHeadline = "Las respuestas del enrutador se retrasan"
    override val verdictSlowExplanation = "Las respuestas fueron lentas o desiguales. Intente nuevamente cerca del enrutador para comparar."
    override val verdictUnreliableHeadline = "La comprobación del enrutador no es concluyente"
    override val verdictUnreliableExplanation = "Muchas respuestas desaparecieron. Algunos enrutadores ignoran estas comprobaciones, así que inténtelo de nuevo antes de cambiar algo."

    override val roomSuggestions = listOf("Sala de estar", "Cocina", "Dormitorio", "Despacho", "Habitación infantil", "Baño", "Comedor", "Pasillo", "Garaje", "Sótano", "Balcón", "Jardín")

    override val coverageNoRooms = "Ninguna habitación revisada todavía"
    override fun coverageWeakInRoom(room: String) = "Débil Wi-Fi en ${room}"
    override fun coverageWeakInRooms(count: Int) = "Débil Wi-Fi en ${count} habitaciones"
    override fun coverageRoomsCheckedNoneWeak(count: Int) =
        if (count == 1) "1 habitación revisada, ninguna débil" else "${count} habitaciones revisadas, ninguna débil"

    override fun tipHighBandWeak(room: String, band: String) =
        "En ${room} su dispositivo usó ${band}, que atraviesa menos las paredes. Si su enrutador puede dividir bandas, 2.4 GHz puede funcionar mejor allí."
    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) =
        "La señal cae ${dropDb} dB de ${bestRoom} a ${worstRoom}. Pruebe con una posición más central y elevada del enrutador, luego vuelva a verificar los mismos puntos para ver si ayuda."
    override fun tipMultipleAccessPoints(count: Int) =
        "Su dispositivo utilizó ${count} puntos de acceso en estas habitaciones, lo que puede reflejar diferentes bandas o puntos de acceso. Esto por sí solo no confirma un roaming confiable."
    override val tipRouterPlacement = "Coloca el router dentro de casa, en un lugar elevado y sin obstáculos, lejos de microondas, espejos y peceras."

    override val spotFirstReading = "Primera lectura · volver a comprobar después de realizar un cambio"
    override val spotDifferentNetwork = "Diferente red o spot · iniciar una nueva comparación"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "Señal ${deltaDb} dB más fuerte"
            deltaDb <= -4 -> "Señal ${-deltaDb} dB más débil"
            else -> "Señal sobre lo mismo."
        }
        val changed = buildList {
            if (accessPointChanged) add("punto de acceso cambiado")
            if (bandChanged) add("banda cambiada")
        }
        return signal + if (changed.isEmpty()) " desde el último control" else " · ${changed.joinToString("; ")}"
    }

    override val monitorChannelName = "Monitoreo de fondo"
    override val monitorChannelDescription = "Muestra cuando WiFi Weakspot está monitoreando las redes WiFi"
    override val alertsChannelName = "Alertas de red"
    override val alertsChannelDescription = "Alertas de nuevas redes y cambios de señal."
    override val monitorStartFailed = "No se pudo iniciar el monitoreo. Permita Wi-Fi escaneos y habilite los servicios de ubicación, luego intente nuevamente."
    override val monitorWaitingForScanLimited = "Esperando un nuevo escaneo Wi-Fi. Android puede limitar las solicitudes de escaneo."
    override val monitorWaitingForScan = "Esperando un nuevo escaneo Wi-Fi"
    override fun monitorStatus(networkCount: Int, lastScanTime: String) = "${networkCount} ${if (networkCount == 1) "red" else "redes"} · Última búsqueda: ${lastScanTime}"

    override fun newNetworksTitle(count: Int) =
        if (count == 1) "Nueva WiFi Red detectada" else "Nuevas WiFi Redes detectadas"

    override fun newNetworksText(names: List<String>): String = when {
        names.size == 1 -> "\"${names.first()}\" ahora está dentro del alcance"
        else -> "${names[0]}, ${names[1]}" + if (names.size > 2) " y más" else ""
    }

    override val signalChangeTitle = "Cambio de señal"
    override fun signalChangeText(networkName: String, level: MonitorSignalLevel, rssi: Int): String {
        val quality = when (level) {
            MonitorSignalLevel.Excellent -> qualityExcellentLabel
            MonitorSignalLevel.Good -> qualityGoodLabel
            MonitorSignalLevel.Fair -> qualityFairLabel
            MonitorSignalLevel.Weak -> qualityWeakLabel
        }
        return "La señal de «$networkName» ahora es $quality ($rssi dBm)"
    }

    override val exportFileChooserTitle = "Exportar archivo"
    override fun chartDescription(name: String) = "Gráfico: ${name}"

    override val lanVpnUnavailable = "La detección de dispositivos no está disponible a través de esta conexión VPN. Verifique si su VPN permite el acceso a la red local o inténtelo nuevamente cuando esté desconectada."
    override val lanConnectFirst = "Conéctese a Wi-Fi o Ethernet antes de escanear dispositivos locales."
    override val lanNotConnected = "No conectado"

}
