package com.bernaferrari.wifiweakspot.core.i18n.ja

import com.bernaferrari.wifiweakspot.core.i18n.SettingsStrings

object JaSettingsStrings : SettingsStrings {
    override val sectionBackup = "バックアップ"
    override val backupScope = "地図、間取り画像、部屋の測定値を選択したファイルに保存します。設定と診断履歴は含まれません。"
    override val exportBackup = "バックアップを保存"
    override val restoreBackup = "バックアップを復元"
    override val restoreBackupExplanation = "WiFi Weakspotのバックアップを選択してください。地図はコピーとして追加され、既存の部屋の測定値は保持されます。現在のデータは置き換えられません。"
    override val backupSaved = "バックアップを保存しました。"
    override val backupRestored = "バックアップを復元しました。"
    override val backupAlreadyRestored = "このバックアップはすでに復元されています。"
    override val backupExportFailed = "バックアップを保存できませんでした。空き容量を確認して、もう一度お試しください。"
    override val backupRestoreFailed = "復元を完了できませんでした。ファイルと空き容量を確認し、同じファイルでもう一度お試しください。"

    override val sectionScanning = "スキャン"

    override val fasterScansThrottled = "Android では 2 分ごとに 4 回のスキャンが可能"
    override val sectionAppearance = "外観"
    override val themeAuto = "自動"
    override val themeLight = "ライト"
    override val themeDark = "ダーク"
    override val colorStyle = "配色スタイル"
    override val styleExpressive = "表情豊か"
    override val styleVibrant = "鮮やか"
    override val styleTonal = "トーナル"
    override val styleCmf = "落ち着き"
    override val showSignalAs = "信号の表示方法"
    override val sectionComfort = "使いやすさ"

    override val reduceMotionTitle = "アニメーションを減らす"

    override val hapticsSubtitle = "タップ時や結果の表示時に振動"

    override val sectionAlerts = "アラート"
    override val backgroundAlertsTitle = "バックグラウンド通知"

    override val backgroundAlertsSubtitle = "近くのWi-Fiに変化があったとき"

    override val notificationsDenied = "監視状況や通知を表示するには、通知を許可してください。"

    override val checkEvery = "確認する間隔"

    override fun intervalSeconds(seconds: Int) = "${seconds} 秒"
    override fun intervalMinutes(minutes: Int) = "${minutes} 分"
    override val newNetworkAlertsTitle = "新しいネットワークを通知"

    override val signalChangeAlertsTitle = "信号の変化を通知"

    override val signalChangeAlertsSubtitle = "信号が大きく強まったり弱まったりしたとき"

    override val sectionAbout = "このアプリについて"

    override fun aboutApp(appName: String) = "${appName}について"
    override fun version(version: String) = "バージョン ${version}"
    override val replayIntroTitle = "使い方の紹介をもう一度見る"

    override val wallpaperColors = "壁紙の色"
    override val colorIndigo = "インディゴ"
    override val colorOrange = "オレンジ"
    override val colorAmber = "アンバー"
    override val colorGreen = "グリーン"
    override val colorTeal = "青緑"

    override val colorCyan = "シアン"
    override val colorBlue = "ブルー"
    override val colorPurple = "パープル"
    override val colorPink = "ピンク"

    override val tagline = "自宅のWi-Fiの弱い場所を見つける"
    override val openSourceLicense = "オープンソース · Apache 2.0"
    override val feedbackTitle = "フィードバック"
    override val feedbackBody = "アイデアとバグレポート"
    override val elsewhere = "関連リンク"

    override val privacySummary = "測定結果は、共有しない限りこのデバイスに保存されます。"
    override val privacyPolicy = "プライバシーポリシー"

    override val notNow = "後で"

    override val getStarted = "始める"

    override val welcomeHeadline = "家のWi-Fiの\n弱い場所を探す"

    override val welcomeBody = "信号が弱くなる場所と改善方法を確認できます。"
    override val featureRoomsTitle = "各部屋で測定"

    override val featureRoomsBody = "信号が弱い部屋を見つけられます。"

    override val featureRouterTitle = "ルーターをテスト"

    override val featureRouterBody = "応答にかかる時間を確認できます。"

    override val featureNearbyTitle = "近くのネットワークを確認"

    override val featureNearbyBody = "重なりの少ないチャンネルを探せます。"

}
