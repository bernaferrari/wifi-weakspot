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
    override val emptySubtitle = "ルーターのチェック、ネットワーク スキャン、ツールの結果がここに表示されるため、時間の経過とともに Wi-Fi がどのように変化するかを確認できます。"
    override fun trendBarsDescription(count: Int, readings: String) = "最後の ${count} ルーター テスト: ${readings}"
    override val trendNoReply = "ルーターから応答がありません。一部のルーターはこれらのチェックを無視します。"
    override val trendRecheckToCompare = "変更後に再確認して比較してください。"
    override fun trendQuicker(usualMs: String) = "通常より速い (${usualMs} ミリ秒)"
    override fun trendSlower(usualMs: String) = "通常より遅い (${usualMs} ミリ秒)"
    override fun trendAboutSame(usualMs: String) = "いつもと同じくらい (${usualMs} ミリ秒)"

    override fun internetTitle(mbps: String) = "${mbps} Mbps ダウンロード"
    override fun routerTitle(ms: String) = "${ms} ミリ秒 (ルーターまで)"
    override fun networksNearby(count: Int) = if (count == 1) "近くのネットワークが 1 つあります" else "${count} ネットワーク"
    override fun strongestNetwork(network: String) = "最強: ${network}"
    override fun accessPoints(count: Int) = if (count == 1) "アクセスポイント 1 台" else "${count} アクセスポイント"
    override fun devicesFound(count: Int) = if (count == 1) "1 台のデバイスが見つかりました" else "${count} デバイスが見つかりました"
    override fun listWithMore(list: String, more: Int) = if (more > 0) "${list} と ${more} 詳細" else list
    override fun pingSummary(averageMs: String, replied: Int, sent: Int) = "${averageMs} ms · ${sent} 回中 ${replied} 回応答"

    override fun lookupTitle(target: String) = "上を向いて ${target}"
    override val noRecords = "記録がありません"
    override val lookupFailed = "検索に失敗しました"
    override fun openPortsTitle(target: String) = "${target} のポートを開く"
    override val noCommonPortsOpen = "開いている共通ポートはありませんでした"
    override val completed = "完成しました"
    override val failed = "失敗しました"

    override val metricDownload = "ダウンロード"
    override val metricUpload = "アップロード"
    override val metricLatency = "レイテンシ"
    override val metricVariation = "変化"
    override val metricLost = "紛失しました"
    override fun metricOnBand(band: String) = "${band}に"
    override val metricReplied = "返信しました"
    override val metricReplies = "返信"
    override val metricAverage = "平均"
    override val factServer = "サーバー"
    override val factRoom = "部屋"
    override val factBand = "バンド"
    override val factSignal = "信号"
    override val factAccessPoints = "アクセスポイント"
    override fun signalFact(quality: String, rssi: Int) = "${quality} シグナル · ${rssi} dBm"

    override val linesNetworksStrongestFirst = "ネットワーク、最も強いものから"
    override val linesRecords = "記録"
    override fun replyNumber(index: Int) = "返信 ${index}"
    override val scanNote = "あなたと同じチャンネル上の近くのネットワークが通信時間を競います。"

    override val go = "行く"
    override val testInternetSpeedInstead = "代わりにインターネット速度をテストしてください"
    override val keepDeviceStill = "デバイスを数秒間動かさないでください。"
    override val connectToStart = "Wi-Fi に接続して開始します。"
    override val lastTime = "前回"
    override val couldNotSaveSpot = "このスポットを保存できませんでした。もう一度やり直してください。"
    override val connectFirst = "まず、Wi-Fi に接続します。このチェックでは、ルーターからの応答を測定します。"
    override val historyNotSaved = "結果を測定しましたが、履歴を保存できませんでした。"
    override val checkStopped = "接続が変更されたか、ルーターのチェックが停止しました。この Wi-Fi を接続したままにして、もう一度お試しください。一部のルーターは ping をブロックします。"

    override val internetSpeedTitle = "インターネット速度 · fast.com"
}
