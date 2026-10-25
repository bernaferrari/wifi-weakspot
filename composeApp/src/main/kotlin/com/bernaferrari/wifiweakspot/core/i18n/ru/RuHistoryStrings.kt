package com.bernaferrari.wifiweakspot.core.i18n.ru

import com.bernaferrari.wifiweakspot.core.i18n.HistoryStrings

object RuHistoryStrings : HistoryStrings {
    override val today = "Сегодня"
    override val yesterday = "Вчера"
    override val weekdayNames = listOf("Понедельник", "Вторник", "Среда", "Четверг", "Пятница", "Суббота", "Воскресенье")
    override val weekdayShortNames = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")
    override val monthShortNames = listOf("янв.", "февр.", "мар.", "апр.", "мая", "июн.", "июл.", "авг.", "сент.", "окт.", "нояб.", "дек.")
    override fun dateThisYear(weekday: String, day: Int, month: String) = "${weekday}, ${day} ${month}"

    override fun dateOtherYear(weekday: String, day: Int, month: String, year: Int) = "${weekday}, ${day} ${month} ${year} г."

    override fun momentAt(day: String, time: String) = "${day} в ${time}"

    override val filterAll = "Все"
    override val kindInternetTests = "Интернет-тесты"
    override val kindScans = "Сканы"
    override val emptyTitle = "Пока ничего не сохранено."
    override val emptySubtitle = "Здесь отображаются проверки маршрутизатора, сканирования сети и результаты инструментов, поэтому вы можете увидеть, как ваш Wi-Fi меняется с течением времени."
    override fun trendBarsDescription(count: Int, readings: String) = "Последние ${count} тесты маршрутизатора: ${readings}"
    override val trendNoReply = "Нет ответа от маршрутизатора. Некоторые маршрутизаторы игнорируют эти проверки."
    override val trendRecheckToCompare = "После изменения перепроверьте для сравнения."
    override fun trendQuicker(usualMs: String) = "Быстрее, чем обычно (${usualMs} мс)"
    override fun trendSlower(usualMs: String) = "Медленнее, чем обычно (${usualMs} мс)"
    override fun trendAboutSame(usualMs: String) = "Примерно так же, как обычно (${usualMs} мс)"

    override fun internetTitle(mbps: String) = "${mbps} Mbps скачать"
    override fun routerTitle(ms: String) = "${ms} мс к маршрутизатору"
    override fun networksNearby(count: Int) = "Рядом: ${count} ${plural(count, "сеть", "сети", "сетей")}"

    override fun strongestNetwork(network: String) = "Сильнейший: ${network}"
    override fun accessPoints(count: Int) = "${count} ${plural(count, "точка доступа", "точки доступа", "точек доступа")}"

    override fun devicesFound(count: Int) = "Найдено устройств: ${count}"

    override fun listWithMore(list: String, more: Int) = if (more > 0) "${list} и ${more} ещё" else list
    override fun pingSummary(averageMs: String, replied: Int, sent: Int) = "${averageMs} мс · ответов: ${replied} из ${sent}"

    override fun lookupTitle(target: String) = "Посмотрите вверх ${target}"
    override val noRecords = "Нет записей"
    override val lookupFailed = "Поиск не удался."
    override fun openPortsTitle(target: String) = "Открыть порты на ${target}"
    override val noCommonPortsOpen = "Ни один из общих портов не был открыт."
    override val completed = "Завершено"
    override val failed = "Не удалось"

    override val metricDownload = "Скачать"
    override val metricUpload = "Загрузить"
    override val metricLatency = "Задержка"
    override val metricVariation = "Вариант"
    override val metricLost = "Потерян"
    override fun metricOnBand(band: String) = "на ${band}"
    override val metricReplied = "Ответил"
    override val metricReplies = "Ответы"
    override val metricAverage = "Среднее"
    override val factServer = "Сервер"
    override val factRoom = "Комната"
    override val factBand = "Группа"
    override val factSignal = "Сигнал"
    override val factAccessPoints = "Точки доступа"
    override fun signalFact(quality: String, rssi: Int) = "${quality} сигнал · ${rssi} dBm"

    override val linesNetworksStrongestFirst = "Сети, самые сильные в первую очередь"
    override val linesRecords = "Записи"
    override fun replyNumber(index: Int) = "Ответить ${index}"
    override val scanNote = "Соседние сети на том же канале, что и ваша, конкурируют за эфирное время."

    override val go = "ВПЕРЕД"
    override val testInternetSpeedInstead = "Вместо этого проверьте скорость интернета."
    override val keepDeviceStill = "Держите устройство неподвижно в течение нескольких секунд."
    override val connectToStart = "Чтобы начать, подключитесь к своему Wi-Fi."
    override val lastTime = "Последний раз"
    override val couldNotSaveSpot = "Не удалось сохранить это место. Попробуйте еще раз."
    override val connectFirst = "Сначала подключитесь к своему Wi-Fi. Эта проверка измеряет ответы вашего маршрутизатора."
    override val historyNotSaved = "Результат измерен, но историю сохранить не удалось."
    override val checkStopped = "Изменилось соединение или проверка роутера остановлена. Подключите этот Wi-Fi и повторите попытку. Некоторые маршрутизаторы блокируют пинг."

    override val internetSpeedTitle = "Скорость интернета · fast.com"
}
