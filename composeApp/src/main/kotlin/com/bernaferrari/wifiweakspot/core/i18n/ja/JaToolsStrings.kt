package com.bernaferrari.wifiweakspot.core.i18n.ja

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object JaToolsStrings : ToolsStrings {
    override val tryAgain = "もう一度試してください"

    override val targetPlaceholder = "ウェブサイトまたはIPアドレス"
    override val clearTarget = "クリア"
    override val checkSectionTitle = "チェックしてください"
    override val recentSectionTitle = "最近"

    override val toolPortsDescription = "何が開いているのか"

    override val pingQuestion = "答えますか？"
    override val dnsQuestion = "その住所は何ですか?"
    override val dnsDetail = "DNS ルックアップ"
    override val portsQuestion = "それは何を提供するのでしょうか?"

    override val runningPortScanTitle = "ポートを確認中"

    override val diagnosticFailed = "診断に失敗しました"
    override val invalidTarget = "URL パスやスペースを含めずにホスト名または IP アドレスを入力します。"
    override val diagnosticCouldNotComplete = "診断を完了できませんでした。"
    override val connectionChangedRunAgain = "接続が変更されました。新しいネットワークでこのチェックを再度実行します。"

    override val openItsWebPage = "Web ページを開く"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ${total} 回中 ${answered} 回応答"

    override val slowest = "最も遅い"
    override fun repliesContentDescription(replies: String) = "返信: ${replies}"

    override val pingDidntAnswer = "応答しませんでした"
    override val pingResponsive = "レスポンシブ"
    override val pingResponsiveButUneven = "反応は良いが不均一"
    override val pingDroppingReplies = "返信の削除"
    override val pingSlowToAnswer = "返事が遅い"

    override val pingNoAnswerLocal = "オフ、スリープ状態、または ping を無視するように設定されている可能性があります。"
    override fun pingRepliesLost(lost: Int, total: Int) = "${total} 回中 ${lost} 回、応答がありませんでした。ルーターへのテストとインターネット上のホストへのテストを比較すると、問題の場所を絞り込めます。このテストだけでは原因を特定できません。"

    override val pingUneven = "返信時間はさまざまでした。こことルーターの近くのルーター チェックを比較し、インターネット ホストをテストします。"
    override val pingLocalExplanation = "このデバイスに到達して戻ってくるまでの時間です。デバイス自体が結果に影響を与える可能性があります。"
    override val pingInternetExplanation = "このホストに到達して戻ってくるまでの時間です。接続、ルート、ホストはすべて結果に影響します。"

    override fun dnsAddressUnit(count: Int) = "件のアドレス"

    override fun dnsForQuery(query: String) = "${query}用"
    override val dnsNotFound = "見つかりませんでした"
    override val dnsFound = "見つけました"
    override val dnsNotFoundExplanation = "スペルを確認するか、再試行してください。名前は存在しないかもしれません。"
    override fun dnsFoundExplanation(query: String) = "お使いのデバイスは ${query} を見つけることができます。"
    override val dnsAddressesTitle = "住所"

    override val portsOpenUnit = "開ける"
    override fun portsCommonServicesOn(host: String) = "${host} の共通サービス"
    override val portsNothingOpen = "何も開いていません"
    override fun portsOffersServices(count: Int) = if (count == 1) "1 つのサービスを提供します" else "${count} のサービスを提供します"
    override val portsNoneAnswered = "共通サービスが応答しませんでした。"
    override val portsOffersExplanation = "設定ページやファイル共有など。"

    override val portWebPage = "ウェブページ"
    override val portSecureWebPage = "安全なウェブページ"
    override val portRemoteLoginSsh = "リモートログイン (SSH)"
    override val portOldRemoteLoginTelnet = "古いリモート ログイン (Telnet)"
    override val portFileTransferFtp = "ファイル転送 (FTP)"
    override val portAddressLookupsDns = "住所検索 (DNS)"
    override val portFileSharing = "ファイル共有"
    override val portMacFileSharing = "Mac ファイル共有"
    override val portPrinting = "印刷"
    override val portRemoteDesktop = "リモートデスクトップ"
    override val portScreenSharing = "画面共有"
    override val portDeviceDiscoveryUpnp = "デバイス検出 (UPnP)"
    override val portStorageOrAdminPage = "ストレージまたは管理ページ"
    override val portDatabase = "データベース"
    override val portService = "サービス"
    override val devicesLooking = "デバイスを探しています…"
    override val devicesFindOnWifi = "Wi-Fi でデバイスを検索"
    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${count} オン ${ssid}"
    override fun devicesOnYourNetwork(count: Int) = "${count}"
    override val couldntLookForDevices = "デバイスが見つかりませんでした"
    override val scanFailed = "スキャンに失敗しました"
    override val connectionChangedLookAgain = "接続が変更されました。このネットワークをもう一度見てください。"
    override val stoppedLooking = "見るのをやめた"
    override val noDevicesFound = "デバイスが見つかりませんでした"
    override val noDevicesFoundSubtitle =
        "一部のデバイスは、尋ねられても沈黙を保ちます。しばらくしてからもう一度試すか、このデバイスが自宅 Wi-Fi にあることを確認してください。"
    override fun otherDevicesGroup(count: Int) = "その他のデバイス · ${count}"
    override fun quietDevices(count: Int) = if (count == 1) "静音デバイス 1 台" else "${count} 静かなデバイス"
    override val quietDevicesSubtitle = "彼らは自分たちが何者であるかを語らなかった"
    override val hide = "隠す"
    override val show = "表示する"
    override fun checkingLocalAddresses(percent: Int) = "ローカルアドレスを確認しています · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · 応答しませんでした"

    override val deviceNotScannedYet = "スキャンではまだ見つかりませんでした"
    override val deviceAnsweredLastScan = "最後のスキャンに応答しました"
    override val deviceSeenButNoAnswer = "ネットワーク上で見られましたが、応答しませんでした"
    override val actionCheckPorts = "ポートを確認してください"
    override val openRouterSettings = "ルーター設定を開く"
    override val detailsTitle = "詳細"
    override val factNetworkName = "ネットワーク名"
    override val factHardwareAddress = "ハードウェアアドレス"
    override val macHiddenByAndroid = "Android によって隠されました"
    override val factYourName = "あなたの名前です"
    override val portsNotCheckedYet = "まだチェックされていません。"
    override fun portsNoneOpenWhenChecked(moment: String) =
        "${moment.replaceFirstChar { it.lowercase() }} をチェックしたとき、開いている共通ポートはありませんでした。"
    override fun portsCheckedAt(moment: String) = "${moment.replaceFirstChar { it.lowercase() }} にチェックを入れました。"
    override val renameDeviceTitle = "デバイスの名前を変更"
    override val renameSupporting = "見つかった名前 WiFi Weakspot を使用するには、空のままにしておきます。"
    override val connectionChangedOpenAgain = "接続が変更されました。新しいネットワークでこのデバイスを再度開きます。"
    override val couldntLoadDetails = "保存された詳細をロードできませんでした。このデバイスをもう一度開いてみてください。"
    override val scanBeforeNaming = "名前を保存する前に、このデバイスをスキャンしてください。"

}
