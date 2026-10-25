package com.bernaferrari.wifiweakspot.core.i18n.ru

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object RuSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "Измеряю… стой неподвижно"
    override val tapWhereYouAreStanding = "Коснитесь карты, где вы находитесь."
    override val chooseWifi = "Выберите Wi-Fi"
    override val undoLastReading = "Отменить последнее чтение"
    override val allReadings = "Все показания"
    override val floorPlanTitle = "План помещения"
    override fun readingCount(count: Int) = "${count} ${plural(count, "измерение", "измерения", "измерений")}"

    override val mapOptions = "Варианты карты"
    override val usePlanPhoto = "Используйте фотографию своего плана."
    override val changePhoto = "Сменить фото"
    override val removePhoto = "Удалить фото"
    override val exportReadingsCsv = "Экспортировать показания (CSV)"
    override fun floorName(floor: Int) = if (floor == 0) "Подвал" else "Этаж ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "Б" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "Этаж ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "Добавить подвал." else "Добавить этаж ${floor}"
    override val deleteFloorReadingsAction = "Удалить показания этого этажа."
    override fun deleteFloorBody(count: Int) = "Будут удалены все измерения на этом этаже (${count})."

    override val oneRouterAtATime = "Карта следует за одним маршрутизатором за раз. Mesh-системы показывают каждую единицу отдельно."
    override val noNetworksFound = "Сети пока не найдены. Убедитесь, что Wi-Fi и местоположение включены."
    override val scanning = "Сканирование…"
    override val refresh = "Обновить"
    override val switchWifiTitle = "Переключиться на другой Wi-Fi?"
    override val switchWifiBody = "Это очистит показания на каждом этаже этой карты. Создайте новую карту, чтобы сохранить обе."
    override val clearAndSwitch = "Очистить и переключить"

    override val deleteMapBody = "Все показания на каждом этаже будут удалены."

    override val noReadingsYet = "Здесь пока нет показаний. Коснитесь карты, где вы находитесь."
    override val weakestFirst = "Сначала самые слабые, поэтому проблемные места сверху."
    override val mapYourHome = "Нанесите на карту свой дом"
    override val mapYourHomeBody = "Посмотрите свой Wi-Fi на карте вашего дома. Используйте фотографию вашего плана или пустую сетку."
    override val createMap = "Создать карту"

    override val namePlaceholder = "Наверху, офис…"

    override val floorplanDescription = "План проекта"
    override fun coverageMapDescription(count: Int) =
        "Карта покрытия с ${readingCount(count)}. Нажмите, где вы стоите, чтобы измерить."

    override val networkNotSeenHere = "Этого Wi-Fi здесь не было. Ничего не сохранилось."
    override val projectChangedWhileScanning = "Проект изменился во время сканирования. Ни одна точка не сохранилась."
    override val couldNotSaveReading = "Не удалось сохранить показания. Остальные ваши показания в безопасности."
    override fun defaultMapName(index: Int) = if (index == 1) "Домой" else "Дом ${index}"
    override val stillSavingMap = "Минутку, все еще сохраняю карту."
    override val noFreshScan = "Android не предоставил свежего сканирования. Повторите попытку через некоторое время."
    override val couldNotScan = "Не удалось выполнить сканирование. Проверьте Wi-Fi и настройки местоположения."
    override val chooseAccessPointFirst = "Выберите точку доступа перед отображением ее зоны покрытия."
    override val saveMeasurementBeforeExport = "Перед экспортом сохраните хотя бы одно измерение."
    override val couldNotExportProject = "Не удалось экспортировать этот проект. Попробуйте еще раз."

    override val connectToYourWifi = "Подключитесь к своему Wi-Fi"
    override fun checkingRoom(room: String) = "Проверяю ${room}… сохраняйте спокойствие."
    override val connectedWifi = "Подключённая сеть Wi-Fi"
    override val connectedNoReading = "Ваше устройство подключено, но считывание сигнала пока недоступно."
    override val connectToCheck = "Подключитесь к Wi-Fi, который хотите проверить."
    override fun removedRoom(room: String) = "Удален ${room}"
    override val undo = "Отменить"
    override val roomsEmptyHint =
        "Встаньте в комнате и сохраните показания. Проверьте другую комнату и найдите место, где сигнал становится слабее."
    override val noWifiSignal = "Нет сигнала Wi-Fi"
    override val saveRoom = "Комната сохранения."
    override fun dbStronger(delta: Int) = "${delta} дБ сильнее"
    override fun dbWeaker(delta: Int) = "${delta} дБ слабее"
    override val savedRooms = "Сохраненные комнаты"
    override fun deleteRoom(room: String) = "Удалить ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) =
        "${room}, ${quality} сигнал, ${reading}. Просмотрите показания и еще раз проверьте это место."

    override val noSignalToSave = "Нет сигнала Wi-Fi для сохранения. Сначала подключитесь к своему Wi-Fi."
    override val allowScansToLink = "Разрешить сканирование Wi-Fi в сетях, чтобы это показание можно было связать с вашей сетью."
    override val connectToSpotNetwork = "Подключитесь к сети этого места, прежде чем перепроверять его."
    override fun savedRoom(room: String) = "Сохранено ${room}"
    override val couldNotSaveStableReading = "Не удалось сохранить стабильные показания. Продолжайте на том же уровне Wi-Fi и попробуйте еще раз."
    override val whereAreYou = "Где вы находитесь?"
    override val somewhereElse = "Где-то ещё"

    override fun savedAt(time: String) = "Сохранено ${time}"
    override val moveRouterTip = "Попробуйте переместить маршрутизатор выше, ближе к центру или ближе к этой комнате."
    override val betterThanLastTime = "Лучше, чем в прошлый раз."
    override val worseThanLastTime = "Хуже, чем в прошлый раз."
    override val sameAsLastTime = "То же, что и в прошлый раз."
    override val earlierChecks = "Предыдущие проверки"
    override val remove = "Удалить"
    override fun connectToRecheck(ssid: String?) =
        "Подключитесь к ${ssid?.ifBlank { null } ?: "то же самое Wi-Fi"}, чтобы проверить еще раз."

    override val stepDeveloperOptionsTitle = "Включите параметры разработчика."
    override val stepDeveloperOptionsBody =
        "В настройках откройте **О телефоне** и нажмите **Номер сборки** 7 раз. Возможно, вам придется ввести блокировку экрана."
    override val openAboutPhone = "Открыть О телефоне"
    override val stepThrottlingTitle = "Отключить Wi-Fi регулирование сканирования"
    override val stepThrottlingBody =
        "В **Параметры разработчика** прокрутите вниз до **Сеть** и отключите **Wi-Fi регулирование сканирования**."
    override val openDeveloperOptions = "Открыть параметры разработчика"
    override val stepComeBackTitle = "Вернись сюда."
    override val stepComeBackBody =
        "WiFi Weakspot проверяется автоматически, когда вы возвращаетесь. Вы можете снова включить дросселирование в любое время."
    override val hideComputerMethod = "Скрыть компьютерный метод"
    override val useAdbInstead = "Предпочитаешь компьютер? Используйте ADB"
    override val adbCommandClipLabel = "ADB команда"
    override val hideRootOption = "Скрыть опцию root"
    override val useRootInstead = "Рутированное устройство? Вместо этого используйте root"
    override val scanLimitFootnote = "Android ограничивает сканирование для экономии заряда батареи. Вы можете в любой момент снова включить лимит."
    override val unlimitedWithRootTitle = "Неограниченное количество сканирований с помощью root"
    override val unlimitedOnTitle = "Включено неограниченное сканирование."
    override val unlimitedOnBody = "Соседние сети и графики каналов обновляются каждые несколько секунд."
    override val scansLimitedTitle = "Количество сканирований ограничено."
    override val scansLimitedAndroid9Body =
        "Android 9 разрешает 4 сканирования каждые 2 минуты и не может отключить это. WiFi Weakspot между ними повторно использует последние результаты."
    override val usingRoot = "Использование root"
    override val deviceRooted = "Ваше устройство рутировано."
    override val rootScanningBody = "Сканирование проходит через системную оболочку, поэтому в настройках разработчика ничего менять не нужно."
    override val rootDeniedBody =
        "Корневой доступ запрещен. Разрешите WiFi Weakspot в вашем менеджере root и повторите попытку или воспользуйтесь параметрами разработчика ниже."
    override val rootNotNeededBody = "Количество сканирований уже неограничено. Рут не нужен."
    override val rootOfferBody =
        "Пропустите параметры разработчика и позвольте WiFi Weakspot просканировать root. Ваш менеджер root спросит один раз."
    override val stopUsingRoot = "Прекратите использовать root"
    override val waitingForRootManager = "Жду вашего root менеджера…"
    override val tryRootAgain = "Попробуйте root еще раз."
    override val useRoot = "Используйте root"
    override val copied = "Скопировано"
    override val copyCommand = "Команда копирования."
}
