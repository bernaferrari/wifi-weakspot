package com.bernaferrari.wifiweakspot.core.i18n.ja

import com.bernaferrari.wifiweakspot.core.i18n.HistoryStrings

object JaHistoryStrings : HistoryStrings {
    override val today = "今日"
    override val yesterday = "昨日"
    override val weekdayNames = listOf("月曜日", "火曜日", "水曜日", "木曜日", "金曜日", "土曜日", "日曜日")
    override val weekdayShortNames = listOf("月", "火", "水", "木", "金", "土", "日")
    override val monthShortNames = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")
    override fun dateThisYear(weekday: String, day: Int, month: String) = "${month}${day}日（${weekday}）"

    override fun dateOtherYear(weekday: String, day: Int, month: String, year: Int) = "${year}年${month}${day}日（${weekday}）"

    override fun momentAt(day: String, time: String) = "${day} ${time}"

    override val filterAll = "すべて"
    override val kindInternetTests = "インターネットテスト"
    override val kindScans = "スキャン"
    override val emptyTitle = "まだ何も保存されていません"
    override val emptySubtitle = "ルーターのテスト、ネットワークのスキャン、各ツールの結果を保存し、Wi-Fiの変化を振り返ることができます。"

    override fun trendBarsDescription(count: Int, readings: String) = "直近${count}回のルーターテスト：${readings}"

    override val trendNoReply = "ルーターから応答がありません。一部のルーターはこれらのチェックを無視します。"
    override val trendRecheckToCompare = "変更後に再確認して比較してください。"
    override fun trendQuicker(usualMs: String) = "通常より速い (${usualMs} ミリ秒)"
    override fun trendSlower(usualMs: String) = "通常より遅い (${usualMs} ミリ秒)"
    override fun trendAboutSame(usualMs: String) = "いつもと同じくらい (${usualMs} ミリ秒)"

    override fun internetTitle(mbps: String) = "${mbps} Mbps ダウンロード"
    override fun routerTitle(ms: String) = "${ms} ミリ秒 (ルーターまで)"
    override fun networksNearby(count: Int) = "近くのネットワーク${count}件"

    override fun strongestNetwork(network: String) = "最強: ${network}"
    override fun accessPoints(count: Int) = "アクセスポイント${count}個"

    override fun devicesFound(count: Int) = "デバイス${count}台を検出"

    override fun listWithMore(list: String, more: Int) = if (more > 0) "${list}、ほか${more}件" else list

    override fun pingSummary(averageMs: String, replied: Int, sent: Int) = "${averageMs} ms · ${sent} 回中 ${replied} 回応答"

    override fun lookupTitle(target: String) = "${target}の名前解決"

    override val noRecords = "レコードなし"

    override val lookupFailed = "検索に失敗しました"
    override fun openPortsTitle(target: String) = "${target}の開いているポート"

    override val noCommonPortsOpen = "開いている共通ポートはありませんでした"
    override val completed = "完了"

    override val failed = "失敗しました"

    override val metricDownload = "ダウンロード"
    override val metricUpload = "アップロード"
    override val metricLatency = "レイテンシ"
    override val metricVariation = "ばらつき"

    override val metricLost = "未応答"

    override fun metricOnBand(band: String) = "${band}に"
    override val metricReplied = "応答あり"

    override val metricReplies = "応答"

    override val metricAverage = "平均"
    override val factServer = "サーバー"
    override val factRoom = "部屋"
    override val factBand = "周波数帯"

    override val factSignal = "信号"
    override val factAccessPoints = "アクセスポイント"
    override fun signalFact(quality: String, rssi: Int) = "${quality} シグナル · ${rssi} dBm"

    override val linesNetworksStrongestFirst = "ネットワーク、最も強いものから"
    override val linesRecords = "DNSレコード"

    override fun replyNumber(index: Int) = "返信 ${index}"
    override val scanNote = "あなたと同じチャンネル上の近くのネットワークが通信時間を競います。"

    override val go = "開始"

    override val testInternetSpeedInstead = "代わりにインターネット速度をテストしてください"
    override val keepDeviceStill = "デバイスを数秒間動かさないでください。"
    override val connectToStart = "開始するにはWi-Fiに接続してください。"

    override val lastTime = "前回"
    override val couldNotSaveSpot = "このスポットを保存できませんでした。もう一度やり直してください。"
    override val connectFirst = "先にWi-Fiに接続してください。このテストでは、ルーターからの応答時間を測定します。"

    override val historyNotSaved = "結果を測定しましたが、履歴を保存できませんでした。"
    override val checkStopped = "接続先が変わったか、テストが停止しました。同じWi-Fiに接続したまま再試行してください。Pingに応答しないルーターもあります。"

    override val internetSpeedTitle = "インターネット速度 · fast.com"
}
