package com.bernaferrari.wifiweakspot.core.i18n.ru

import com.bernaferrari.wifiweakspot.core.i18n.CommonStrings

object RuCommonStrings : CommonStrings {
    override fun deleteItemTitle(name: String) = "Удалить ${name}?"

    override val couldNotSaveChanges = "Не удалось сохранить изменения. Попробуйте ещё раз."
    override val couldNotDeleteItem = "Не удалось удалить этот элемент. Попробуйте ещё раз."

    override val hiddenNetworkShort = "Скрытый"
    override val network = "Сеть"
    override val tools = "Инструменты"
    override val maker = "Создатель"
    override val noReply = "Нет ответа"
    override val fastest = "Самый быстрый"
    override val address = "Адрес"
    override val responseTime = "Время отклика"
    override val checkAgain = "Перепроверить"
    override val rename = "Переименовать"
    override val stop = "Стоп"
    override val unknown = "Неизвестно"

    override val hiddenNetwork = "Скрытая сеть"
    override val fasterScans = "Сканировать быстрее"
    override val openWifiSettings = "Открыть настройки Wi-Fi"
    override val signalUnavailable = "Сигнал недоступен"
    override val devices = "Устройства"
    override val openPorts = "Открытые порты"
    override val nameLabel = "Название"
    override val historyTitle = "История"
    override val routerCheck = "Проверка роутера"
    override val testWebsite = "Проверка сайта"
    override val yourRouter = "Ваш роутер"
    override val yourNetwork = "Ваша сеть"

    override val openInNewTab = "Открыть в новой вкладке"
    override val mapSharingAndroidOnly = "Делиться картой можно в приложении Android."
    override val back = "Назад"
    override val close = "Закрыть"
    override val cancel = "Отмена"
    override val save = "Сохранить"
    override val done = "Готово"
    override val share = "Поделиться"
    override val delete = "Удалить"

    override val tabHome = "Главная"
    override val tabNetworks = "Сети"
    override val tabRooms = "Комнаты"
    override val tabSettings = "Настройки"

    override val permissionScreenTitle = "Разрешить доступ к ближайшему Wi-Fi"
    override val permissionAllowAccess = "Разрешить доступ"
    override val openAppSettings = "Открыть настройки приложения."

    override fun signalBadgeDescription(quality: String, reading: String) = "${quality} сигнал, ${reading}"

    override val scanReady = "ГОТОВО"
    override fun scansCount(count: Int) = "${count} ${plural(count, "сканирование", "сканирования", "сканирований")}"

    override val scansLeftCaption = "осталось"
    override val scanWaitingCaption = "ожидание"
    override val scanDevMode = "РЕЖИМ РАЗРАБОТКИ"
    override val readyToScan = "Готово к сканированию"
    override fun scansRemaining(count: Int) = "Осталось ${count} ${plural(count, "сканирование", "сканирования", "сканирований")}"

    override fun nextScanIn(time: String) = "Следующее сканирование через ${time}"
    override val checking = "Проверяю..."
    override val justNow = "только что"
    override fun minutesAgo(minutes: Long) = "${minutes} минуту назад"
    override fun hoursAgo(hours: Long) = "${hours} час назад"
    override fun daysAgo(days: Long) = "${days} д назад"
}
