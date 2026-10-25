package com.bernaferrari.wifiweakspot.core.i18n.ja

import com.bernaferrari.wifiweakspot.core.i18n.CommonStrings

object JaCommonStrings : CommonStrings {
    override fun deleteItemTitle(name: String) = "${name} を削除しますか?"

    override val couldNotSaveChanges = "変更を保存できませんでした。もう一度お試しください。"
    override val couldNotDeleteItem = "この項目を削除できませんでした。もう一度お試しください。"

    override val hiddenNetworkShort = "非表示"
    override val network = "ネットワーク"
    override val tools = "ツール"
    override val maker = "メーカー"
    override val noReply = "応答なし"

    override val fastest = "最速"
    override val address = "アドレス"

    override val responseTime = "応答時間"
    override val checkAgain = "再測定"

    override val rename = "名前を変更"
    override val stop = "停止"
    override val unknown = "不明"

    override val hiddenNetwork = "非公開ネットワーク"
    override val fasterScans = "スキャンを高速化"
    override val openWifiSettings = "Wi-Fi設定を開く"
    override val signalUnavailable = "信号を取得できません"
    override val devices = "デバイス"
    override val openPorts = "開いているポート"
    override val nameLabel = "名前"
    override val historyTitle = "履歴"
    override val routerCheck = "ルーターをテスト"
    override val testWebsite = "ウェブサイトをテスト"
    override val yourRouter = "使用中のルーター"
    override val yourNetwork = "使用中のネットワーク"

    override val openInNewTab = "新しいタブで開く"
    override val mapSharingAndroidOnly = "マップの共有はAndroidアプリで利用できます。"

    override val back = "戻る"
    override val close = "閉じる"
    override val cancel = "キャンセル"
    override val save = "保存"
    override val done = "完了"
    override val share = "共有"
    override val delete = "削除"

    override val tabHome = "ホーム"
    override val tabNetworks = "ネットワーク"
    override val tabRooms = "部屋"
    override val tabSettings = "設定"

    override val permissionScreenTitle = "近くのWi-Fiへのアクセス"

    override val permissionAllowAccess = "アクセスを許可"

    override val openAppSettings = "アプリの設定を開く"

    override fun signalBadgeDescription(quality: String, reading: String) = "${quality} 信号、${reading}"

    override val scanReady = "準備完了"
    override fun scansCount(count: Int) = "スキャン${count}回"

    override val scansLeftCaption = "残り"
    override val scanWaitingCaption = "待機中"

    override val scanDevMode = "開発モード"
    override val readyToScan = "スキャンの準備ができました"
    override fun scansRemaining(count: Int) = "残り${count}回スキャン可能"

    override fun nextScanIn(time: String) = "次のスキャンまで${time}"

    override val checking = "確認中…"

    override val justNow = "ちょうど今"
    override fun minutesAgo(minutes: Long) = "${minutes}分前"

    override fun hoursAgo(hours: Long) = "${hours}時間前"

    override fun daysAgo(days: Long) = "${days}日前"

}
