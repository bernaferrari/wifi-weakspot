package com.bernaferrari.wifiweakspot.core.i18n.ja

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object JaAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = "${count} 件のネットワーク"

    override fun channelShort(channel: Int) = "ch ${channel}"

    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · ch ${channel} · ${band}"
    override val noPassword = "パスワードなし"
    override val wifiOffTitle = "Wi-Fi がオフです"
    override val locationOffBody = "Androidでは、近くのWi-Fiを表示するために位置情報をオンにする必要があります。WiFi WeakspotはGPSを使用しません。"

    override val scanPermissionBody = "Android は、位置情報にアクセスできるアプリと近くのネットワーク名のみを共有します。 WiFi Weakspot はあなたの居場所を決して記録しません。"
    override val wifiOffBody = "Wi-Fi をオンにすると、近くのネットワークが表示されます。"
    override val locationOffTitle = "位置情報がオフです"
    override val turnOnLocation = "位置情報をオンにする"
    override val scanPausedTitle = "Android スキャンを一時停止しました"
    override fun scanPausedBody(seconds: Long) = "次のスキャンは ${seconds} 秒以内に許可されます。待機をスキップするには、スキャン スロットルをオフにします。"
    override val turnOffThrottling = "スロットリングをオフにする"
    override val readyToScanBody = "「スキャン」をタップすると、近くにあるすべてのネットワークが表示されます。"
    override val scan = "スキャン"
    override val channels = "チャンネル"
    override val signalOverTime = "信号の推移"
    override val howToReadThisChart = "このチャートの見方"
    override val expandSignalHistory = "信号履歴を展開する"
    override val expandChannels = "チャネルを拡張する"
    override fun noBandNetworksNearby(band: String) = "近くに ${band} ネットワークがありません。"
    override fun networksOnBand(count: Int, band: String) = if (count == 0) "近くに${band}のネットワークはありません" else "${band}：${networks(count)}"

    override fun showVeryWeakNetworks(count: Int) =
        if (count == 1) "非常に弱いネットワークを 1 件表示" else "${count} 非常に弱いネットワークを表示します"
    override fun hideVeryWeakNetworks(count: Int) =
        if (count == 1) "非常に弱いネットワークを 1 つ非表示にする" else "${count} 非常に弱いネットワークを非表示にします"
    override fun bandNetworkCount(count: Int) = if (count == 0) "なし" else networks(count)
    override val highlightedOnChart = "チャート上でハイライト表示"
    override val notHighlightedOnChart = "チャートではハイライトされていません"
    override fun detailsForNetwork(ssid: String) = "${ssid.ifEmpty { "隠されたネットワーク" }}の詳細"
    override fun alsoYourRouter(quality: String) = "${quality} · ルーターも"
    override val connected = "接続中"

    override val scanReportHeading = "WiFi Weakspot スキャンレポート"
    override fun scanReportTime(time: String) = "時間: ${time}"
    override fun scanReportTotal(count: Int) = "ネットワーク総数: ${count}"
    override val scanReportNetworksHeading = "ネットワーク:"
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) =
        "• ${ssid} - ${rssi}dBm、Ch ${channel}、${security}"
    override val channelLooksClear = "チャンネルがクリアに見えます"
    override val noChannelImprovement = "このスキャンでは改善は見つかりませんでした"
    override fun lessOverlapThanChannel(currentChannel: Int) = "チャンネル ${currentChannel} よりもオーバーラップが少ない"
    override val leastOverlapInScan = "このスキャンで重複が最も少ない"
    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}: チャンネル ${channel}"
    override val lowOverlap = "低オーバーラップ"
    override val moderateOverlap = "中程度の重複"
    override val highOverlap = "重複が多い"
    override val veryHighOverlap = "非常に高い重複"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) =
        "チャネルグラフ、${networks(networkCount)}。" +
            (if (busiestChannel != null) " 最もビジーなチャンネル ${busiestChannel} と ${busiestCount}。" else "")
    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) =
        name + (if (isYou) " (あなた)" else "") + (if (extraNames > 0) " +${extraNames}" else "")
    override val busyCaption = "混雑"
    override val axisStrong = "強い"
    override val axisOkay = "普通"
    override val axisWeak = "弱い"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · ch ${channel}"
    override val clearSelectedNetwork = "選択したネットワークをクリアします"
    override val tapWaveForDetails = "Wave をタップするとネットワークの詳細が表示されます"

    override val chartGuideTitle = "チャートを読む"
    override val guideTallerBody = "高さは信号強度です。"
    override val guideOverlapBody = "重複するネットワークはスペクトルを共有し、通信時間を競合する可能性があります。"
    override val guideWiderBody = "チャネルが広いほど、より多くのスペクトルを占有し、より多くの隣接チャネルと重複する可能性があります。"
    override val guideSameRouterBody = "+1: 同じラジオから来たと思われる名前が同じ丘を共有しています。"
    override val guideCrowdBody = "緑は重なりが少なく、赤は多いことを表します。2.4 GHz の下のストリップはオーバーラップを示しています。タップするとカウントされます。"
    override val guideSteadyBody = "飛び跳ねた線は、信号強度の変化を示しています。線をタップして一時停止し、時間を横にスライドしたり、ネットワーク間を縦にスライドしたりできます。ライブを再開して新しい測定値を確認します。"
    override val guideHighlightBody = "丘または列をタップして強調表示します。チャート上で水平方向にドラッグして探索します。"

    override fun channelOverlapHeading(count: Int, channel: Int) = when (count) {
        0 -> "チャンネル ${channel} はクリアです"
        1 -> "1 つのネットワークがチャンネル ${channel} と重複しています"
        else -> "${count} ネットワークがチャネル ${channel} と重複しています"
    }
    override fun fromChannel(homeChannel: Int, reading: String) = "チャンネル ${homeChannel} · ${reading} から"

    override val estimatedChannelOverlap = "推定チャンネル重複"
    override fun tryChannel(channel: Int) = "チャンネル ${channel} をお試しください"
    override val lowestOverlapFor20Mhz = "20 MHz チャネルの最小推定オーバーラップ"
    override val overlapDisclaimer = "測定されたトラフィックではなく、近くのアクセス ポイントと信号強度に基づいています。 " +
        "設定を変更する前に、ルーターのサポートされているチャンネルとローカル ルールを確認してください。 " +
        "5 および 6 GHz の提案には、既知のルーターのサポートが必要です。ワイドチャンネルセンターが欠けている " +
        "プライマリ 20 MHz チャンネルのみを表示します。"
    override val overlapScore = "オーバーラップスコア"

    override val collectingReadings = "測定値を収集中… 数回のスキャン後に線が表示されます。"
    override fun signalHistoryDescription(networkCount: Int) =
        "${networks(networkCount)} の信号履歴。検査する行をタップします。矢印キーは時間とネットワークを移動します。"
    override val historyLive = "ライブ"
    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) =
        "${networkName}、${reading ?: "読んでいません"}、一時停止、${readingsAgo(readingsAgo)}"
    override val strongerNetwork = "より強力なネットワーク"
    override val weakerNetwork = "弱いネットワーク"
    override val axisOlder = "過去"
    override val axisLatest = "最新"
    override val exploreSignalHistory = "信号履歴を探索する"
    override val tapLineToCompare = "線をタップし、ドラッグして比較します"
    override val noReadingAtPoint = "現時点では読み取り値はありません"
    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) =
        "${reading} · ${quality} · " + if (readingsAgo == 0) "最近の読書" else readingsAgo(readingsAgo)
    private fun readingsAgo(count: Int) = if (count == 1) "1 件読む前" else "${count} 閲覧前"
    override val resumeLive = "ライブを再開"

    override fun seenReading(rssi: Int, seen: String) = "${rssi} dBm · ${seen} を見た"
    override val factChannel = "チャンネル"
    override val factWidth = "幅"
    override val factSecurity = "セキュリティ"
    override val factDistance = "推定距離"
    override val notAvailable = "利用できません"
    override val watch = "時計"
    override val addToFavorites = "お気に入りに追加"
    override val removeFromFavorites = "お気に入りから削除"

    override fun routerCheckLastTime(pingMs: String) = "${pingMs} ミリ秒"
    override fun devicesFoundLastScan(count: Int) = "${count} 最後のスキャンが見つかりました"
    override val devicesSubtitle = "誰がつながっているのか"
    override val testWebsiteSubtitle = "Ping、DNS、ポート"
    override fun savedChecks(count: Int) = when (count) {
        0 -> "過去の実績"
        1 -> "1 チェック"
        else -> "${count} 小切手"
    }
    override val notOnWifi = "Wi-Fi にはありません"
    override val online = "オンライン"
    override val internetNotConfirmed = "インターネット未確認"
    override fun channelChip(channel: Int) = "チャンネル ${channel}"
    override fun linkRateChip(mbps: Int) = "リンクレート ${mbps} Mbps"
    override val chooseNetwork = "ネットワークを選択してください"
    override val turnOnWifi = "Wi-Fiをオンにする"
    override val dismiss = "却下"
    override val findWeakSpotsTitle = "弱点を見つける"
    override val findWeakSpotsBody = "どの部屋が弱いかを確認します。"
    override val findWeakSpotsAction = "お部屋をチェックしてください"
    override fun networksNearby(count: Int) = "${networks(count)} 近く"
    override val nearbyStale = "最後のスキャンから。タップして更新します。"
    override val nearbyNoConnection = "各ネットワークのチャンネルと信号を参照してください。"
    override fun nearbyOverlap(count: Int, channel: Int) = when (count) {
        0 -> "チャネル ${channel} で近くのオーバーラップが検出されませんでした。"
        1 -> "1 つの近くの信号があなたのチャンネル ${channel} と重なっています。"
        else -> "${count} 近くの信号がチャンネル ${channel} と重なっています。"
    }

    override val adviceNoWifiBody = "確認したいネットワークに接続します。現在の接続を使用して、インターネット テストやネットワーク ツールを引き続き実行できます。"
    override val adviceNoInternetTitle = "まずインターネットアクセスを確認してください"
    override val adviceNoInternetBody = "Android は、この Wi-Fi 接続でのインターネット アクセスを確認していません。サインイン ページを確認し、ルーターとインターネット ホストへの接続をテストします。"
    override val adviceNoSignalTitle = "Wi-Fi シグナルを読み取る"
    override val adviceNoSignalBody = "ネットワークの Wi-Fi スキャンに信号測定値の表示を許可します。問題が発生した部屋の信号を比較してください。"
    override val adviceWeakSignalTitle = "ルーター付近のカバレッジを比較します"
    override val adviceWeakSignalBody = "現在のWi-Fi 信号は弱いです。ここでルーターチェックを実行し、ルーターの近くで繰り返します。独自のネットワークを調査して弱点を見つけます。"
    override val adviceGoodSignalTitle = "ルーターの応答性を比較する"
    override val adviceGoodSignalBody = "信号強度は使用可能なようで、Android はインターネット アクセスを報告します。接続が遅いと感じる場合は、ルーターの応答をこことルーターの近くで比較してください。信号が強いだけではパフォーマンスは保証されません。"
}
