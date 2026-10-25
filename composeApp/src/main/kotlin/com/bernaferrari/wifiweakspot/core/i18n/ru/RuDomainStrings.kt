package com.bernaferrari.wifiweakspot.core.i18n.ru

import com.bernaferrari.wifiweakspot.core.i18n.DomainStrings
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind
import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel

object RuDomainStrings : DomainStrings {

    override val signalDisplayBars = "Полоски"
    override val qualityExcellentLabel = "Отличный"
    override val qualityExcellentHeadline = "Отличный сигнал"
    override val qualityExcellentExplanation = "Сильный сигнал от роутера. Сам по себе уровень сигнала не определяет скорость интернета."
    override val qualityGoodLabel = "Хороший"
    override val qualityGoodHeadline = "Хороший сигнал"
    override val qualityGoodExplanation = "Устойчивый сигнал от роутера."
    override val qualityFairLabel = "Средний"
    override val qualityFairHeadline = "Средний сигнал"
    override val qualityFairExplanation = "Сигнал пригоден для работы, но более чувствителен к помехам."
    override val qualityWeakLabel = "Слабый"
    override val qualityWeakHeadline = "Слабый сигнал"
    override val qualityWeakExplanation = "Слабый сигнал. Сравните это место с местом, расположенным ближе к маршрутизатору."
    override val qualityDeadZoneLabel = "Нет сигнала"
    override val qualityDeadZoneExplanation = "Очень слабый сигнал достигает этой точки."

    override val deviceKindRouter = "Роутер"
    override val deviceKindThisDevice = "Это устройство"
    override val deviceKindPhone = "Телефон или планшет"
    override val deviceKindComputer = "Компьютер"
    override val deviceKindTv = "ТВ или стример"
    override val deviceKindSpeaker = "Колонка"
    override val deviceKindPrinter = "Принтер"
    override val deviceKindConsole = "Игровая консоль"
    override val deviceKindCamera = "Камера"
    override val deviceKindSmartHome = "Устройство умного дома"
    override val deviceKindUnknown = "Устройство"
    override val unnamedDevice = "Безымянное устройство"

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

    override val verdictGreatHeadline = "Маршрутизатор отвечает быстро"
    override val verdictGreatExplanation = "Ответы были быстрыми и постоянными."
    override val verdictGoodHeadline = "Ответ маршрутизатора можно использовать."
    override val verdictGoodExplanation = "Некоторые ответы были немного медленными или неровными."
    override val verdictSlowHeadline = "Ответы маршрутизатора задерживаются."
    override val verdictSlowExplanation = "Ответы были медленными или неравномерными. Попробуйте еще раз возле роутера для сравнения."
    override val verdictUnreliableHeadline = "Проверка маршрутизатора безрезультатна"
    override val verdictUnreliableExplanation = "Многие ответы пропали. Некоторые маршрутизаторы игнорируют эти проверки, поэтому попробуйте еще раз, прежде чем что-либо менять."

    override val roomSuggestions = listOf("Гостиная", "Кухня", "Спальня", "Кабинет", "Детская", "Ванная", "Столовая", "Коридор", "Гараж", "Подвал", "Балкон", "Сад")

    override val coverageNoRooms = "Еще не проверено ни одного номера."
    override fun coverageWeakInRoom(room: String) = "Слабый Wi-Fi в ${room}"
    override fun coverageWeakInRooms(count: Int) = "Слабый Wi-Fi в ${count} ${plural(count, "комнате", "комнатах", "комнатах")}"

    override fun coverageRoomsCheckedNoneWeak(count: Int) = "${plural(count, "Проверена", "Проверены", "Проверено")} ${count} ${plural(count, "комната", "комнаты", "комнат")}, слабый сигнал не обнаружен"

    override fun tipHighBandWeak(room: String, band: String) =
        "В ${room} ваше устройство использовало ${band}, которое пропускает сквозь стены меньшее расстояние. Если ваш маршрутизатор может разделять диапазоны, 2,4 GHz может там держаться лучше."
    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) =
        "Сигнал падает на ${dropDb} дБ с ${bestRoom} до ${worstRoom}. Попробуйте более центральное, приподнятое положение фрезы, затем еще раз проверьте те же места, чтобы посмотреть, поможет ли это."
    override fun tipMultipleAccessPoints(count: Int) = "В этих комнатах устройство использовало ${count} ${plural(count, "точку доступа", "точки доступа", "точек доступа")}. Это могут быть разные диапазоны или точки доступа. Само по себе это не подтверждает надёжность роуминга."

    override val tipRouterPlacement = "Держите маршрутизатор на открытом воздухе, на полу, вдали от микроволновых печей, зеркал и аквариумов."

    override val spotFirstReading = "Первое чтение · повторная проверка после внесения изменений"
    override val spotDifferentNetwork = "Другая сеть или спот · начать новое сравнение"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "Сигнал ${deltaDb} дБ сильнее"
            deltaDb <= -4 -> "Сигнал ${-deltaDb} дБ слабее"
            else -> "Сигнал примерно такой же"
        }
        val changed = buildList {
            if (accessPointChanged) add("точка доступа изменена")
            if (bandChanged) add("изменена")
        }
        return signal + if (changed.isEmpty()) " с момента последней проверки" else " · ${changed.joinToString("; ")}"
    }

    override val monitorChannelName = "Фоновый мониторинг"
    override val monitorChannelDescription = "Показывает, когда WiFi Weakspot контролирует сети WiFi."
    override val alertsChannelName = "Сетевые оповещения"
    override val alertsChannelDescription = "Оповещения о новых сетях и изменениях сигнала."
    override val monitorStartFailed = "Не удалось запустить мониторинг. Разрешите сканирование Wi-Fi и включите службы определения местоположения, затем повторите попытку."
    override val monitorWaitingForScanLimited = "Ожидание нового сканирования Wi-Fi. Android может ограничивать запросы на сканирование."
    override val monitorWaitingForScan = "Ожидание нового сканирования Wi-Fi"
    override fun monitorStatus(networkCount: Int, lastScanTime: String) = "${networkCount} ${plural(networkCount, "сеть", "сети", "сетей")} · Последнее сканирование: ${lastScanTime}"

    override fun newNetworksTitle(count: Int) =
        if (count == 1) "Обнаружена новая сеть WiFi" else "Обнаружены новые WiFi сети"

    override fun newNetworksText(names: List<String>): String = when {
        names.size == 1 -> "«${names.first()}» теперь в пределах досягаемости."
        else -> "${names[0]}, ${names[1]}" + if (names.size > 2) " и многое другое" else ""
    }

    override val signalChangeTitle = "Изменение сигнала"
    override fun signalChangeText(networkName: String, level: MonitorSignalLevel, rssi: Int): String {
        val quality = when (level) {
            MonitorSignalLevel.Excellent -> "отлично"
            MonitorSignalLevel.Good -> "хорошо"
            MonitorSignalLevel.Fair -> "ярмарка"
            MonitorSignalLevel.Weak -> "слабый"
        }
        return "Сигнал «${networkName}» теперь ${quality} (${rssi} dBm)"
    }

    override val exportFileChooserTitle = "Экспортировать файл"
    override fun chartDescription(name: String) = "Диаграмма: ${name}"

    override val lanVpnUnavailable = "Обнаружение устройств через это VPN-соединение недоступно. Проверьте, разрешает ли ваша VPN доступ к локальной сети, или повторите попытку, когда она отключена."
    override val lanConnectFirst = "Подключитесь к Wi-Fi или Ethernet перед сканированием локальных устройств."
    override val lanNotConnected = "Не подключено"

}
