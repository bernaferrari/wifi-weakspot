package com.bernaferrari.wifiweakspot.core.i18n.ru

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object RuAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = "${count} ${plural(count, "сеть", "сети", "сетей")}"

    override fun channelShort(channel: Int) = "Кан. ${channel}"

    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · ${channelShort(channel)} · ${band}"

    override val noPassword = "Без пароля"
    override val wifiOffTitle = "Wi-Fi выключено"
    override val locationOffBody = "Для отображения ближайших сетей Wi-Fi Android требует включить геолокацию. WiFi Weakspot не использует GPS."

    override val scanPermissionBody = "Android передает имена ближайших сетей только приложениям, у которых есть доступ к местоположению. WiFi Weakspot никогда не записывает, где вы находитесь."
    override val wifiOffBody = "Включите Wi-Fi, чтобы увидеть ближайшие сети."
    override val locationOffTitle = "Местоположение отключено."
    override val turnOnLocation = "Включить местоположение"
    override val scanPausedTitle = "Android сканирование приостановлено"
    override fun scanPausedBody(seconds: Long) = "Следующее сканирование разрешено через ${seconds} с. Отключите регулирование сканирования, чтобы пропустить ожидание."
    override val turnOffThrottling = "Отключить регулирование"
    override val readyToScanBody = "Нажмите «Сканировать», чтобы увидеть все сети поблизости."
    override val scan = "Сканировать"
    override val channels = "Каналы"
    override val signalOverTime = "Изменение сигнала"
    override val howToReadThisChart = "Как читать эту диаграмму"
    override val expandSignalHistory = "Развернуть историю сигналов"
    override val expandChannels = "Развернуть каналы"
    override fun noBandNetworksNearby(band: String) = "Нет сетей ${band} поблизости."
    override fun networksOnBand(count: Int, band: String) =
        if (count == 0) "Нет сетей на ${band}" else "${networks(count)} на ${band}"
    override fun showVeryWeakNetworks(count: Int) = "Показать ${count} ${plural(count, "очень слабую сеть", "очень слабые сети", "очень слабых сетей")}"

    override fun hideVeryWeakNetworks(count: Int) = "Скрыть ${count} ${plural(count, "очень слабую сеть", "очень слабые сети", "очень слабых сетей")}"

    override fun bandNetworkCount(count: Int) = if (count == 0) "нет" else networks(count)
    override val highlightedOnChart = "Выделено на графике."
    override val notHighlightedOnChart = "Не выделено на графике"
    override fun detailsForNetwork(ssid: String) = "Подробности для ${ssid.ifEmpty { "скрытая сеть" }}"
    override fun alsoYourRouter(quality: String) = "${quality} · Также ваш роутер"
    override val connected = "Подключено"

    override val scanReportHeading = "WiFi Weakspot Отчет о сканировании"
    override fun scanReportTime(time: String) = "Время: ${time}"
    override fun scanReportTotal(count: Int) = "Всего сетей: ${count}"
    override val scanReportNetworksHeading = "Сети:"
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) = "• ${ssid} - ${rssi} dBm, ${channelShort(channel)}, ${security}"

    override val channelLooksClear = "Ваш канал мало загружен"
    override val noChannelImprovement = "Более подходящий канал не найден"
    override fun lessOverlapThanChannel(currentChannel: Int) = "Менее загружен, чем канал ${currentChannel}"
    override val suggestedChannel = "Рекомендуемый канал"
    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}: канал ${channel}"
    override val lowOverlap = "Низкое перекрытие"
    override val moderateOverlap = "Умеренное перекрытие"
    override val highOverlap = "Высокое перекрытие"
    override val veryHighOverlap = "Очень сильное перекрытие"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) =
        "График канала, ${networks(networkCount)}." +
            (if (busiestChannel != null) " Самый загруженный канал ${busiestChannel} с ${busiestCount}." else "")
    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) = name + (if (isYou) " (${connected})" else "") + (if (extraNames > 0) " +${extraNames}" else "")

    override val busyCaption = "Загрузка"
    override val axisStrong = "Сильный"
    override val axisOkay = "Средний"
    override val axisWeak = "Слабый"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · ${channelShort(channel)}"

    override val clearSelectedNetwork = "Очистить выбранную сеть."
    override val tapWaveForDetails = "Нажмите волну, чтобы узнать подробности о сети."

    override val chartGuideTitle = "Измерение графиков"
    override val guideTallerBody = "Высота — это уровень сигнала."
    override val guideOverlapBody = "Перекрывающиеся сети используют общий спектр и могут конкурировать за эфирное время."
    override val guideWiderBody = "Более широкий канал занимает больший спектр и может перекрывать большее количество соседей."
    override val guideSameRouterBody = "+1: Имена сетей, которые, вероятно, используют один радиомодуль Wi-Fi, объединены на одной кривой."

    override val guideCrowdBody = "Зелёный означает меньше перекрытий, красный — больше. Полоса под 2.4 GHz показывает перекрытие. Нажмите на него для подсчета."
    override val guideSteadyBody = "Колебания линии показывают изменения силы сигнала. Нажмите на линию для паузы, затем перемещайтесь по горизонтали между моментами времени или по вертикали между сетями. Возобновите показ, чтобы видеть новые измерения."

    override val guideHighlightBody = "Коснитесь холма или ряда, чтобы выделить его. Перетащите горизонтально по диаграмме, чтобы изучить ее."

    override fun channelOverlapHeading(count: Int, channel: Int) = if (count == 0) "Канал ${channel} свободен" else "Канал ${channel} перекрывают: ${networks(count)}"

    override fun fromChannel(homeChannel: Int, reading: String) = "С канала ${homeChannel} · ${reading}"

    override val estimatedChannelOverlap = "Предполагаемое перекрытие каналов"
    override fun tryChannel(channel: Int) = "Попробуйте канал ${channel}"
    override val lowestOverlapFor20Mhz = "Наименьшее расчетное перекрытие для 20 MHz каналов"
    override val overlapDisclaimer = "Оценка основана на ближайших точках доступа и силе сигнала, а не на измеренном трафике. Перед изменением настроек проверьте поддерживаемые роутером каналы и местные правила. Каналы в диапазонах 5 и 6 GHz предлагаются только при известной поддержке роутера. Если центральная частота широкого канала неизвестна, отображается только его основной канал шириной 20 MHz."

    override val overlapScore = "Оценка перекрытия"

    override val collectingReadings = "Сбор измерений… Линии появятся после нескольких сканирований."

    override fun signalHistoryDescription(networkCount: Int) = "История сигнала сетей: ${networkCount}. Нажмите на линию для просмотра. Стрелки перемещают выбор по времени и между сетями."

    override val historyLive = "Прямой эфир"
    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) = "${networkName}, ${reading ?: "нет измерения"}, пауза, ${readingsAgo(readingsAgo)}"

    override val strongerNetwork = "Более мощная сеть"
    override val weakerNetwork = "Более слабая сеть"
    override val axisOlder = "раньше"
    override val axisLatest = "сейчас"
    override val exploreSignalHistory = "Изучите историю сигналов"
    override val tapLineToCompare = "Коснитесь линии, затем перетащите ее, чтобы сравнить."
    override val noReadingAtPoint = "Для этого момента нет измерения"

    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) = "${reading} · ${quality} · " + if (readingsAgo == 0) "последнее измерение" else readingsAgo(readingsAgo)

    private fun readingsAgo(count: Int) = "${count} ${plural(count, "измерение", "измерения", "измерений")} назад"

    override val resumeLive = "Возобновить трансляцию"

    override fun seenReading(rssi: Int, seen: String) = "${rssi} dBm · видел ${seen}"
    override val factChannel = "Канал"
    override val factWidth = "Ширина"
    override val factSecurity = "Безопасность"
    override val factDistance = "Оценка расстояния"
    override val notAvailable = "Недоступно"
    override val watch = "Смотреть"
    override val addToFavorites = "Добавить в избранное"
    override val removeFromFavorites = "Удалить из избранного"

    override fun routerCheckLastTime(pingMs: String) = "${pingMs} мс в прошлый раз"
    override fun devicesFoundLastScan(count: Int) = "${count} найдено при последнем сканировании"
    override val devicesSubtitle = "Кто на связи"
    override val testWebsiteSubtitle = "Пинг, DNS, порты"
    override fun savedChecks(count: Int) = if (count == 0) "Прошлые результаты" else "${count} ${plural(count, "проверка", "проверки", "проверок")}"

    override val notOnWifi = "Не на Wi-Fi"
    override val online = "Онлайн"
    override val internetNotConfirmed = "Интернет не подтвержден"
    override fun channelChip(channel: Int) = "Канал ${channel}"
    override fun linkRateChip(mbps: Int) = "Скорость соединения ${mbps} Mbps"
    override val chooseNetwork = "Выберите сеть"
    override val turnOnWifi = "Включи Wi-Fi"
    override val dismiss = "Пропустить"
    override val findWeakSpotsTitle = "Найдите слабые места"
    override val findWeakSpotsBody = "Найдите комнаты со слабым сигналом."

    override val findWeakSpotsAction = "Проверьте свои комнаты."
    override fun networksNearby(count: Int) = "${networks(count)} рядом"
    override val nearbyStale = "Из вашего последнего сканирования. Нажмите, чтобы обновить."
    override val nearbyNoConnection = "См. каналы и сигнал для каждой сети."
    override fun nearbyOverlap(count: Int, channel: Int) = if (count == 0) "На канале ${channel} перекрытий не обнаружено." else "Соседних сигналов, перекрывающих канал ${channel}: ${count}."

    override val adviceNoWifiBody = "Подключитесь к сети, которую хотите проверить. Вы по-прежнему можете запускать интернет-тесты и сетевые инструменты, используя текущее соединение."
    override val adviceNoInternetTitle = "Сначала проверьте доступ в Интернет."
    override val adviceNoInternetBody = "Android не подтвердил доступ в Интернет для этого соединения Wi-Fi. Найдите страницу входа, затем проверьте подключение к маршрутизатору и интернет-хосту."
    override val adviceNoSignalTitle = "Прочитайте свой сигнал Wi-Fi."
    override val adviceNoSignalBody = "Разрешить сканирование Wi-Fi в сетях для просмотра измерений сигнала. Сравните сигнал в комнате, где вы заметили проблемы."
    override val adviceWeakSignalTitle = "Сравните зону покрытия рядом с вашим маршрутизатором"
    override val adviceWeakSignalBody = "Сигнал Wi-Fi слабый. Проверьте отклик роутера здесь и повторите тест рядом с ним. Сравните измерения своей сети, чтобы найти слабые места."

    override val adviceGoodSignalTitle = "Сравните скорость реагирования маршрутизатора"
    override val adviceGoodSignalBody = "Уровень сигнала выглядит приемлемым, а Android указывает на доступ к Интернету. Если соединение кажется медленным, сравните реакцию маршрутизатора здесь и рядом с маршрутизатором; сильный сигнал сам по себе не гарантирует производительность."
    override val channelAdviceBasis = "Чем короче столбик, тем меньше перекрытие в этом сканировании. Скорость может меняться."
    override fun channelChangeInstructions(channel: Int, band: String) = "Выберите канал ${channel} для диапазона ${band}"

    override val channelOverlapTitle = "Перекрытие с соседними сетями"
    override val channelApplyChange = "Сохраните и при необходимости подключитесь заново"
    override val channelChangeNote = "Измените настройку в приложении или настройках роутера, а не в WiFi Weakspot."

}
