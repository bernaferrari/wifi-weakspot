package com.bernaferrari.wifiweakspot.core.i18n.ru

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object RuToolsStrings : ToolsStrings {
    override val tryAgain = "Попробуй еще раз."

    override val targetPlaceholder = "Веб-сайт или IP-адрес."
    override val clearTarget = "Ясно"
    override val checkSectionTitle = "Проверить"
    override val recentSectionTitle = "Недавние"

    override val toolPortsDescription = "Что открыто"

    override val pingQuestion = "Он отвечает?"
    override val dnsQuestion = "Какой у него адрес?"
    override val dnsDetail = "DNS поиск"
    override val portsQuestion = "Что он предлагает?"

    override val runningPortScanTitle = "Проверка портов"

    override val diagnosticFailed = "Диагностика не удалась."
    override val invalidTarget = "Введите имя хоста или IP-адрес без URL-пути и пробелов."
    override val diagnosticCouldNotComplete = "Не удалось завершить диагностику."
    override val connectionChangedRunAgain = "Соединение изменилось. Запустите эту проверку еще раз в новой сети."

    override val openItsWebPage = "Откройте его веб-страницу."

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ответов: ${answered} из ${total}"

    override val slowest = "Самый медленный"
    override fun repliesContentDescription(replies: String) = "Ответы: ${replies}"

    override val pingDidntAnswer = "Не ответил"
    override val pingResponsive = "Адаптивный"
    override val pingResponsiveButUneven = "Отзывчивый, но неравномерный"
    override val pingDroppingReplies = "Удаление ответов"
    override val pingSlowToAnswer = "Медленно отвечаю"

    override val pingNoAnswerLocal = "Возможно, он выключен, находится в спящем режиме или настроен на игнорирование пингов."
    override fun pingRepliesLost(lost: Int, total: Int) =
        "${lost} из ${total} ответов пропали. Сравните проверку маршрутизатора с данными интернет-хоста, чтобы определить проблему; сам по себе этот тест не может выявить причину."
    override val pingUneven = "Время ответов различалось. Сравните результаты проверки маршрутизатора здесь и рядом с ним, а затем протестируйте интернет-хост."
    override val pingLocalExplanation = "Время добраться до этого устройства и обратно. Само устройство может повлиять на результат."
    override val pingInternetExplanation = "Время добраться до этого хоста и обратно. Ваше соединение, маршрут и хост влияют на результат."

    override fun dnsAddressUnit(count: Int) = plural(count, "адрес", "адреса", "адресов")

    override fun dnsForQuery(query: String) = "Для ${query}"
    override val dnsNotFound = "Не удалось найти."
    override val dnsFound = "Нашёл"
    override val dnsNotFoundExplanation = "Проверьте правильность написания или повторите попытку; имя может не существовать."
    override fun dnsFoundExplanation(query: String) = "Ваше устройство может найти ${query}."
    override val dnsAddressesTitle = "Адреса"

    override val portsOpenUnit = "открыто"
    override fun portsCommonServicesOn(host: String) = "Общие службы на ${host}"
    override val portsNothingOpen = "Ничего не открыто"
    override fun portsOffersServices(count: Int) = "${plural(count, "Доступна", "Доступны", "Доступно")} ${count} ${plural(count, "служба", "службы", "служб")}"

    override val portsNoneAnswered = "Ни одна общая служба не ответила."
    override val portsOffersExplanation = "Такие вещи, как страница настроек или обмен файлами."

    override val portWebPage = "Веб-страница"
    override val portSecureWebPage = "Защищенная веб-страница."
    override val portRemoteLoginSsh = "Удаленный вход (SSH)"
    override val portOldRemoteLoginTelnet = "Старый удаленный вход (Telnet)"
    override val portFileTransferFtp = "Передача файлов (FTP)"
    override val portAddressLookupsDns = "Поиск адреса (DNS)"
    override val portFileSharing = "Обмен файлами"
    override val portMacFileSharing = "Общий доступ к файлам Mac"
    override val portPrinting = "Печать"
    override val portRemoteDesktop = "Удаленный рабочий стол"
    override val portScreenSharing = "Демонстрация экрана"
    override val portDeviceDiscoveryUpnp = "Обнаружение устройства (UPnP)"
    override val portStorageOrAdminPage = "Страница хранилища или администратора."
    override val portDatabase = "База данных"
    override val portService = "Сервис"
    override val devicesLooking = "Ищем устройства…"
    override val devicesFindOnWifi = "Найдите устройства на вашем Wi-Fi"
    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${count} на ${ssid}"
    override fun devicesOnYourNetwork(count: Int) = "${count} в вашей сети"
    override val couldntLookForDevices = "Не удалось найти устройства."
    override val scanFailed = "Сканирование не удалось."
    override val connectionChangedLookAgain = "Соединение изменилось. Посмотрите еще раз в этой сети."
    override val stoppedLooking = "Перестал искать"
    override val noDevicesFound = "Устройства не найдены."
    override val noDevicesFoundSubtitle =
        "Некоторые устройства молчат, когда об этом спрашивают. Повторите попытку через минуту или проверьте, есть ли это устройство у вас дома Wi-Fi."
    override fun otherDevicesGroup(count: Int) = "Другие устройства · ${count}"
    override fun quietDevices(count: Int) = "${count} ${plural(count, "неопознанное устройство", "неопознанных устройства", "неопознанных устройств")}"

    override val quietDevicesSubtitle = "Они не сказали, кто они такие"
    override val hide = "Скрыть"
    override val show = "Показать"
    override fun checkingLocalAddresses(percent: Int) = "Проверка локальных адресов · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · не ответил"

    override val deviceNotScannedYet = "Пока не найдено при сканировании"
    override val deviceAnsweredLastScan = "Ответил на последнее сканирование."
    override val deviceSeenButNoAnswer = "Видел в сети, но не ответил"
    override val actionCheckPorts = "Проверьте порты"
    override val openRouterSettings = "Открыть настройки роутера."
    override val detailsTitle = "Подробности"
    override val factNetworkName = "Имя сети"
    override val factHardwareAddress = "Аппаратный адрес"
    override val macHiddenByAndroid = "Скрыт Android"
    override val factYourName = "Ваше имя"
    override val portsNotCheckedYet = "Пока не проверено."
    override fun portsNoneOpenWhenChecked(moment: String) =
        "Ни один из общих портов не был открыт, когда установлен флажок ${moment.replaceFirstChar { it.lowercase() }}."
    override fun portsCheckedAt(moment: String) = "Проверено ${moment.replaceFirstChar { it.lowercase() }}."
    override val renameDeviceTitle = "Переименование устройства"
    override val renameSupporting = "Оставьте пустым, чтобы использовать найденное имя WiFi Weakspot."
    override val connectionChangedOpenAgain = "Соединение изменилось. Откройте это устройство еще раз в новой сети."
    override val couldntLoadDetails = "Не удалось загрузить сохраненные данные. Попробуйте открыть это устройство еще раз."
    override val scanBeforeNaming = "Просканируйте это устройство перед сохранением имени."

}
