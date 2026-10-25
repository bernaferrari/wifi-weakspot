package com.bernaferrari.wifiweakspot.core.i18n.es

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object EsSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "Midiendo… quédate quieto"
    override val tapWhereYouAreStanding = "Toca el mapa donde estás parado"
    override val chooseWifi = "Elige Wi-Fi"
    override val undoLastReading = "Deshacer la última medición"

    override val allReadings = "Todas las mediciones"

    override val floorPlanTitle = "Plano"
    override fun readingCount(count: Int) = if (count == 1) "1 medición" else "${count} mediciones"

    override val mapOptions = "Opciones de mapa"
    override val usePlanPhoto = "Usar una foto del plano"

    override val changePhoto = "cambiar la foto"
    override val removePhoto = "quitar la foto"
    override val exportReadingsCsv = "Exportar mediciones (CSV)"

    override fun floorName(floor: Int) = if (floor == 0) "Sótano" else "Piso ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "Piso ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "Añadir un sótano" else "Agregar piso ${floor}"
    override val deleteFloorReadingsAction = "Eliminar las lecturas de este piso"
    override fun deleteFloorBody(count: Int) = "Se eliminarán las mediciones de esta planta (${count})."

    override val oneRouterAtATime = "Cada mapa mide un solo punto de acceso. En los sistemas mesh, cada unidad aparece por separado."

    override val noNetworksFound = "Aún no se han encontrado redes. Comprueba que Wi-Fi y la ubicación estén activados."
    override val scanning = "Buscando…"
    override val refresh = "Refrescar"
    override val switchWifiTitle = "¿Cambiar a otro Wi-Fi?"
    override val switchWifiBody = "Esto borra las lecturas en cada piso de este mapa. Crea un nuevo mapa para conservar ambos."
    override val clearAndSwitch = "Borrar y cambiar"

    override val deleteMapBody = "Se eliminarán todas las lecturas de cada piso."

    override val noReadingsYet = "Aún no hay mediciones aquí. Toca en el plano el lugar donde estás."

    override val weakestFirst = "Los más débiles primero, por lo que los puntos problemáticos están arriba."
    override val mapYourHome = "Mapea tu casa"
    override val mapYourHomeBody = "Vea su Wi-Fi en un mapa de su hogar. Utilice una foto de su plan o una cuadrícula en blanco."
    override val createMap = "Crear un mapa"

    override val namePlaceholder = "Planta de arriba, despacho…"

    override val floorplanDescription = "Plano del proyecto"
    override fun coverageMapDescription(count: Int) =
        "Mapa de cobertura con ${readingCount(count)}. Toca donde estás parado para medir."

    override val networkNotSeenHere = "No se encontró esa red Wi-Fi aquí. No se guardó ninguna medición."

    override val projectChangedWhileScanning = "El mapa cambió durante la búsqueda. No se guardó ningún punto."

    override val couldNotSaveReading = "No se pudo guardar esta medición. Las demás mediciones se conservan."

    override fun defaultMapName(index: Int) = if (index == 1) "Hogar" else "Inicio ${index}"
    override val stillSavingMap = "Se está guardando el mapa. Espera un momento."

    override val noFreshScan = "Android no proporcionó nuevos resultados. Espera un momento e inténtalo de nuevo."

    override val couldNotScan = "No se pudo escanear. Marque Wi-Fi y la configuración de ubicación."
    override val chooseAccessPointFirst = "Elige un punto de acceso antes de mapear su cobertura."
    override val saveMeasurementBeforeExport = "Guarde al menos una medición antes de exportar."
    override val couldNotExportProject = "No se pudo exportar este proyecto. Intentar otra vez."

    override val connectToYourWifi = "Conéctate a tu Wi-Fi"
    override fun checkingRoom(room: String) = "Comprobando ${room}… quédate quieto"
    override val connectedWifi = "Wi-Fi conectado"
    override val connectedNoReading = "Tu dispositivo está conectado, pero aún no hay una medición de señal disponible."

    override val connectToCheck = "Conéctate al Wi-Fi que deseas verificar."
    override fun removedRoom(room: String) = "Eliminado ${room}"
    override val undo = "Deshacer"
    override val roomsEmptyHint = "Quédate quieto en una habitación y guarda una medición. Mide en otra habitación para encontrar dónde se debilita la señal."

    override val noWifiSignal = "Sin señal Wi-Fi"
    override val saveRoom = "Guardar habitación"
    override fun dbStronger(delta: Int) = "${delta} dB más fuerte"
    override fun dbWeaker(delta: Int) = "${delta} dB más débil"
    override val savedRooms = "Habitaciones guardadas"
    override fun deleteRoom(room: String) = "Eliminar ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) = "${room}, señal ${quality}, ${reading}. Revisa las mediciones y vuelve a medir este lugar."

    override val noSignalToSave = "No hay señal Wi-Fi para guardar. Conéctate a tu Wi-Fi primero."
    override val allowScansToLink = "Permite Wi-Fi escaneos en Redes para que esta lectura pueda vincularse a su red."
    override val connectToSpotNetwork = "Conéctate a la red de este lugar antes de volver a verificarlo."
    override fun savedRoom(room: String) = "Guardado ${room}"
    override val couldNotSaveStableReading = "No se pudo guardar una medición estable. Mantén el dispositivo quieto y conectado al mismo Wi-Fi, e inténtalo de nuevo."

    override val whereAreYou = "¿Dónde estás?"
    override val somewhereElse = "En algún otro lugar"

    override fun savedAt(time: String) = "Guardado ${time}"
    override val moveRouterTip = "Intenta mover el router más arriba, más central o más cerca de esta habitación."
    override val betterThanLastTime = "Mejor que la última vez"
    override val worseThanLastTime = "Peor que la última vez"
    override val sameAsLastTime = "Igual que la última vez"
    override val earlierChecks = "Controles anteriores"
    override val remove = "Eliminar"
    override fun connectToRecheck(ssid: String?) =
        "Conéctate a ${ssid?.ifBlank { null } ?: "lo mismo Wi-Fi"} para verificar nuevamente."

    override val stepDeveloperOptionsTitle = "Activar opciones de desarrollador"
    override val stepDeveloperOptionsBody =
        "En Configuración, abre **Acerca del teléfono** y toca **Número de compilación** 7 veces. Es posible que tengas que ingresar tu bloqueo de pantalla."
    override val openAboutPhone = "Abrir Acerca del teléfono"
    override val stepThrottlingTitle = "Desactivar Wi-Fi aceleración de escaneo"
    override val stepThrottlingBody =
        "En **Opciones de desarrollador**, desplácese hacia abajo hasta **Redes** y desactive **Wi-Fi aceleración de escaneo**."
    override val openDeveloperOptions = "Abrir opciones de desarrollador"
    override val stepComeBackTitle = "Vuelve aquí"
    override val stepComeBackBody = "Al volver, WiFi Weakspot comprobará el ajuste automáticamente. Puedes volver a activar el límite cuando quieras."

    override val hideComputerMethod = "Ocultar los pasos para ordenador"

    override val useAdbInstead = "Configurar con ADB desde un ordenador"

    override val adbCommandClipLabel = "ADB comando"
    override val hideRootOption = "Ocultar la opción de root"

    override val useRootInstead = "Configurar con permisos de root"

    override val scanLimitFootnote = "Android limita los escaneos para ahorrar batería. Puedes volver a activar el límite en cualquier momento."
    override val unlimitedWithRootTitle = "Escaneos ilimitados con root"
    override val unlimitedOnTitle = "Los escaneos ilimitados están activados"
    override val unlimitedOnBody = "Las redes cercanas y los gráficos de canales se actualizan cada pocos segundos."
    override val scansLimitedTitle = "Los escaneos son limitados"
    override val scansLimitedAndroid9Body =
        "Android 9 permite 4 escaneos cada 2 minutos y no puede desactivarlo. WiFi Weakspot reutiliza resultados recientes en el medio."
    override val usingRoot = "Usando root"
    override val deviceRooted = "Tu dispositivo está rooteado"
    override val rootScanningBody = "Los análisis pasan por el shell del sistema, por lo que no hay nada que cambiar en las opciones de desarrollador."
    override val rootDeniedBody =
        "Se denegó el acceso raíz. Permite WiFi Weakspot en su administrador root e inténtelo nuevamente, o use las opciones de desarrollador a continuación."
    override val rootNotNeededBody = "Los escaneos ya son ilimitados. No se necesita raíz."
    override val rootOfferBody = "Usa root para buscar redes sin cambiar las opciones de desarrollador. Tu gestor de root te pedirá permiso una vez."

    override val stopUsingRoot = "Deja de usar root"
    override val waitingForRootManager = "Esperando al gestor de root…"

    override val tryRootAgain = "Intenta root nuevamente"
    override val useRoot = "Utilice root"
    override val copied = "Copiado"
    override val copyCommand = "Copiar comando"

}
