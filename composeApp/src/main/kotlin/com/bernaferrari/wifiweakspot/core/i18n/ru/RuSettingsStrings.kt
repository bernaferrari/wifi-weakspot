package com.bernaferrari.wifiweakspot.core.i18n.ru

import com.bernaferrari.wifiweakspot.core.i18n.SettingsStrings

object RuSettingsStrings : SettingsStrings {
    override val sectionBackup = "Резервная копия"
    override val backupScope = "Сохраните карты, изображения планов и замеры комнат в выбранный файл. Настройки и история диагностики не включены."
    override val exportBackup = "Сохранить копию"
    override val restoreBackup = "Восстановить копию"
    override val restoreBackupExplanation = "Выберите резервную копию WiFi Weakspot. Карты будут добавлены как копии, а существующие замеры сохранятся. Текущие данные не будут заменены."
    override val backupSaved = "Копия сохранена."
    override val backupRestored = "Копия восстановлена."
    override val backupAlreadyRestored = "Эта копия уже восстановлена."
    override val backupExportFailed = "Не удалось сохранить копию. Проверьте свободное место и повторите попытку."
    override val backupRestoreFailed = "Не удалось завершить восстановление. Проверьте файл и свободное место, затем повторите попытку с тем же файлом."

    override val sectionScanning = "Сканирование"
    override val fasterScansThrottled = "Android позволяет 4 сканирования каждые 2 минуты."
    override val sectionAppearance = "Оформление"
    override val themeAuto = "Авто"
    override val themeLight = "Светлая"
    override val themeDark = "Тёмная"
    override val colorStyle = "Стиль цвета"
    override val styleExpressive = "Выразительный"
    override val styleVibrant = "Яркий"
    override val styleTonal = "Тональный"
    override val styleCmf = "CMF"
    override val showSignalAs = "Отображение сигнала"
    override val sectionComfort = "Комфорт"
    override val reduceMotionTitle = "Уменьшить анимацию"

    override val hapticsSubtitle = "Вибрация при нажатии и результатах"
    override val sectionAlerts = "Оповещения"
    override val backgroundAlertsTitle = "Фоновые оповещения"
    override val backgroundAlertsSubtitle = "При изменениях в сетях Wi-Fi поблизости"

    override val notificationsDenied = "Разрешить уведомления, чтобы видеть статус мониторинга и оповещения."
    override val checkEvery = "Интервал проверки"

    override fun intervalSeconds(seconds: Int) = "${seconds} сек."
    override fun intervalMinutes(minutes: Int) = "${minutes} мин."
    override val newNetworkAlertsTitle = "Новые сетевые оповещения"
    override val signalChangeAlertsTitle = "Оповещения об изменении сигнала"
    override val signalChangeAlertsSubtitle = "Когда ваш сигнал сильно возрастает или падает"
    override val sectionAbout = "О нас"
    override fun aboutApp(appName: String) = "Около ${appName}"
    override fun version(version: String) = "Версия ${version}"
    override val replayIntroTitle = "Еще раз покажи введение."
    override val wallpaperColors = "Цвета обоев"
    override val colorIndigo = "Индиго"
    override val colorOrange = "Оранжевый"
    override val colorAmber = "Янтарь"
    override val colorGreen = "Зеленый"
    override val colorTeal = "Бирюзовый"
    override val colorCyan = "Голубой"
    override val colorBlue = "Синий"
    override val colorPurple = "Фиолетовый"
    override val colorPink = "Розовый"

    override val tagline = "Найдите зоны слабого Wi-Fi дома"
    override val openSourceLicense = "Открытый исходный код · Apache 2.0"
    override val feedbackTitle = "Обратная связь"
    override val feedbackBody = "Идеи и отчеты об ошибках"
    override val elsewhere = "Другие ссылки"

    override val privacySummary = "Результаты остаются на вашем устройстве, если вы сами ими не поделитесь."
    override val privacyPolicy = "Политика конфиденциальности"

    override val notNow = "Не сейчас"
    override val getStarted = "Начать"
    override val welcomeHeadline = "Где дома\nслабый Wi-Fi?"

    override val welcomeBody = "Узнайте, где сигнал слабеет и как это исправить."
    override val featureRoomsTitle = "Проверьте каждую комнату."
    override val featureRoomsBody = "Найдите комнаты со слабым сигналом."

    override val featureRouterTitle = "Проверьте свой роутер"
    override val featureRouterBody = "Посмотрите, как быстро он отвечает."
    override val featureNearbyTitle = "Посмотреть ближайшие сети"
    override val featureNearbyBody = "Найдите канал с меньшим перекрытием."

}
