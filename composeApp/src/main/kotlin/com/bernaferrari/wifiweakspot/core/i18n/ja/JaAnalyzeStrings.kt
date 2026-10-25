package com.bernaferrari.wifiweakspot.core.i18n.ja

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object JaAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = "${count} 件のネットワーク"

    override fun channelShort(channel: Int) = "ch ${channel}"

    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · ch ${channel} · ${band}"
    override val noPassword = "パスワードなし"
    override val wifiOffTitle = "Wi-Fi がオフです"
    override val locationOffBody = "Androidでは、近くのWi-Fiを表示するために位置情報をオンにする必要があります。WiFi WeakspotはGPSを使用しません。"

    override val scanPermissionBody = "Androidでは、近くのWi-Fiネットワーク名を取得するために位置情報へのアクセス許可が必要です。WiFi Weakspotは居場所を記録しません。"

    override val wifiOffBody = "近くのネットワークを確認するには、Wi-Fiをオンにしてください。"

    override val locationOffTitle = "位置情報がオフです"

    override val turnOnLocation = "位置情報をオンにする"
    override val scanPausedTitle = "Androidによるスキャン制限中"

    override fun scanPausedBody(seconds: Long) = "次のスキャンまで${seconds}秒です。スキャン回数の制限を解除すると、待機時間を短縮できます。"

    override val turnOffThrottling = "スキャン回数の制限を解除"

    override val readyToScanBody = "「スキャン」をタップすると、近くのネットワークを検索します。"

    override val scan = "スキャン"
    override val channels = "チャンネル"
    override val signalOverTime = "信号の推移"
    override val howToReadThisChart = "グラフの見方"

    override val expandSignalHistory = "信号履歴を拡大"

    override val expandChannels = "チャンネルグラフを拡大"

    override fun noBandNetworksNearby(band: String) = "近くに ${band} ネットワークがありません。"
    override fun networksOnBand(count: Int, band: String) = if (count == 0) "近くに${band}のネットワークはありません" else "${band}：${networks(count)}"

    override fun showVeryWeakNetworks(count: Int) = "信号が非常に弱いネットワーク${count}件を表示"

    override fun hideVeryWeakNetworks(count: Int) = "信号が非常に弱いネットワーク${count}件を隠す"

    override fun bandNetworkCount(count: Int) = if (count == 0) "なし" else networks(count)
    override val highlightedOnChart = "グラフ上で強調表示中"

    override val notHighlightedOnChart = "グラフ上で未選択"

    override fun detailsForNetwork(ssid: String) = "${ssid.ifEmpty { "隠されたネットワーク" }}の詳細"
    override fun alsoYourRouter(quality: String) = "${quality} · 同じルーターの別ネットワーク"

    override val connected = "接続中"

    override val scanReportHeading = "WiFi Weakspot スキャンレポート"

    override fun scanReportTime(time: String) = "時間: ${time}"
    override fun scanReportTotal(count: Int) = "ネットワーク総数: ${count}"
    override val scanReportNetworksHeading = "ネットワーク:"
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) =
        "• ${ssid} - ${rssi}dBm、Ch ${channel}、${security}"
    override val channelLooksClear = "使用中のチャンネルは混雑が少なそうです"

    override val noChannelImprovement = "今回のスキャンでは、より良いチャンネルは見つかりませんでした"

    override fun lessOverlapThanChannel(currentChannel: Int) = "チャンネル${currentChannel}より重なりが少ない"

    override val leastOverlapInScan = "今回のスキャンで重なりが最小"

    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}: チャンネル ${channel}"
    override val lowOverlap = "重なりが少ない"

    override val moderateOverlap = "重なりが中程度"

    override val highOverlap = "重なりが多い"

    override val veryHighOverlap = "重なりが非常に多い"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) = "チャンネルグラフ。ネットワーク${networkCount}件。" + if (busiestChannel != null) " 最も混雑しているチャンネルは${busiestChannel}で、${busiestCount}件のネットワークがあります。" else ""

    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) =
        name + (if (isYou) " (あなた)" else "") + (if (extraNames > 0) " +${extraNames}" else "")
    override val busyCaption = "混雑"
    override val axisStrong = "強い"
    override val axisOkay = "普通"
    override val axisWeak = "弱い"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · ch ${channel}"
    override val clearSelectedNetwork = "ネットワークの選択を解除"

    override val tapWaveForDetails = "山の形をタップすると、ネットワークの詳細が表示されます"

    override val chartGuideTitle = "グラフの見方"

    override val guideTallerBody = "山の高さは信号の強さを表します。"

    override val guideOverlapBody = "重なっているネットワークは周波数帯を共有しており、通信時間を取り合うことがあります。"

    override val guideWiderBody = "チャンネル幅が広いほど多くの周波数を使うため、近くのネットワークと重なりやすくなります。"

    override val guideSameRouterBody = "+1：同じWi-Fi無線モジュールを使っていると推定されるネットワーク名を、1つの山にまとめて表示します。"

    override val guideCrowdBody = "緑は重なりが少なく、赤は多いことを表します。2.4 GHz の下のストリップはオーバーラップを示しています。タップするとカウントされます。"
    override val guideSteadyBody = "線が大きく上下するほど、信号の強さが変動しています。線をタップして一時停止し、左右に動かすと時点を、上下に動かすとネットワークを切り替えられます。再開すると最新の測定値が表示されます。"

    override val guideHighlightBody = "山の形や一覧の行をタップすると強調表示されます。グラフ上を左右にドラッグして確認できます。"

    override fun channelOverlapHeading(count: Int, channel: Int) = if (count == 0) "チャンネル${channel}に重なりはありません" else "チャンネル${channel}と重なるネットワーク：${count}件"

    override fun fromChannel(homeChannel: Int, reading: String) = "チャンネル${homeChannel}から · ${reading}"

    override val estimatedChannelOverlap = "チャンネルの重なりの推定"

    override fun tryChannel(channel: Int) = "チャンネル ${channel} をお試しください"
    override val lowestOverlapFor20Mhz = "20 MHz幅のチャンネルで、重なりが最も少ないと推定されます"

    override val overlapDisclaimer = "近くのアクセスポイントと信号の強さに基づく推定で、実際の通信量を測定したものではありません。設定を変更する前に、ルーターの対応チャンネルと地域の規則を確認してください。5 GHz・6 GHzの提案はルーターの対応が確認できる場合のみです。広いチャンネルの中心周波数が不明な場合は、主チャンネルの20 MHz分だけを表示します。"

    override val overlapScore = "重なりのスコア"

    override val collectingReadings = "測定値を収集中です。数回スキャンすると線が表示されます。"

    override fun signalHistoryDescription(networkCount: Int) = "${networkCount}件のネットワークの信号履歴です。線をタップして確認できます。矢印キーで時点やネットワークを切り替えられます。"

    override val historyLive = "リアルタイム"

    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) = "${networkName}, ${reading ?: "測定値なし"}, 一時停止中, ${readingsAgo(readingsAgo)}"

    override val strongerNetwork = "信号がより強いネットワーク"

    override val weakerNetwork = "信号がより弱いネットワーク"

    override val axisOlder = "過去"
    override val axisLatest = "最新"
    override val exploreSignalHistory = "信号履歴を確認"

    override val tapLineToCompare = "線をタップし、ドラッグして比較できます"

    override val noReadingAtPoint = "この時点の測定値はありません"

    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) = "${reading} · ${quality} · " + if (readingsAgo == 0) "最新の測定値" else readingsAgo(readingsAgo)

    private fun readingsAgo(count: Int) = "${count}回前の測定"

    override val resumeLive = "リアルタイム表示を再開"

    override fun seenReading(rssi: Int, seen: String) = "${rssi} dBm · 検出：${seen}"

    override val factChannel = "チャンネル"
    override val factWidth = "チャンネル幅"

    override val factSecurity = "セキュリティ"
    override val factDistance = "推定距離"

    override val notAvailable = "取得できません"

    override val watch = "信号を追跡"

    override val addToFavorites = "お気に入りに追加"
    override val removeFromFavorites = "お気に入りから削除"

    override fun routerCheckLastTime(pingMs: String) = "前回：${pingMs} ms"

    override fun devicesFoundLastScan(count: Int) = "前回のスキャンで${count}台を検出"

    override val devicesSubtitle = "接続中のデバイス"

    override val testWebsiteSubtitle = "Ping・DNS・ポート"

    override fun savedChecks(count: Int) = if (count == 0) "過去の結果" else "テスト${count}件"

    override val notOnWifi = "Wi-Fiに未接続"

    override val online = "オンライン"
    override val internetNotConfirmed = "インターネット接続は未確認"

    override fun channelChip(channel: Int) = "チャンネル ${channel}"
    override fun linkRateChip(mbps: Int) = "リンクレート ${mbps} Mbps"
    override val chooseNetwork = "ネットワークを選択してください"
    override val turnOnWifi = "Wi-Fiをオンにする"
    override val dismiss = "却下"
    override val findWeakSpotsTitle = "信号が弱い場所を探す"

    override val findWeakSpotsBody = "信号が届きにくい部屋を確認できます。"

    override val findWeakSpotsAction = "部屋ごとに測定する"

    override fun networksNearby(count: Int) = "近くに${count}件のネットワーク"

    override val nearbyStale = "前回のスキャン結果です。タップして更新できます。"

    override val nearbyNoConnection = "各ネットワークのチャンネルと信号を参照してください。"
    override fun nearbyOverlap(count: Int, channel: Int) = when (count) {
        0 -> "チャネル ${channel} で近くのオーバーラップが検出されませんでした。"
        1 -> "1 つの近くの信号があなたのチャンネル ${channel} と重なっています。"
        else -> "${count} 近くの信号がチャンネル ${channel} と重なっています。"
    }

    override val adviceNoWifiBody = "確認したいネットワークに接続します。現在の接続を使用して、インターネット テストやネットワーク ツールを引き続き実行できます。"
    override val adviceNoInternetTitle = "まずインターネット接続を確認"

    override val adviceNoInternetBody = "AndroidでこのWi-Fiのインターネット接続を確認できていません。ログイン用のページがないか確認し、ルーターとインターネット上のホストへの接続をテストしてください。"

    override val adviceNoSignalTitle = "Wi-Fiの信号を測定"

    override val adviceNoSignalBody = "「ネットワーク」でWi-Fiスキャンを許可すると、信号を測定できます。問題が起きる部屋で測定し、ほかの場所と比較してください。"

    override val adviceWeakSignalTitle = "ルーターの近くでも測定して比較"

    override val adviceWeakSignalBody = "現在のWi-Fi信号は弱くなっています。ここでルーターをテストし、近くに移動してもう一度テストしてください。自分のネットワークをマッピングすると、信号が弱い場所を探せます。"

    override val adviceGoodSignalTitle = "ルーターの応答を比較"

    override val adviceGoodSignalBody = "信号の強さは実用的で、Androidでもインターネット接続が確認されています。遅いと感じる場合は、ここでの応答時間とルーターの近くでの応答時間を比べてください。信号が強くても、通信が速いとは限りません。"

}
