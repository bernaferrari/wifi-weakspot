package com.bernaferrari.wifiweakspot.core.i18n.ja

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object JaToolsStrings : ToolsStrings {
    override val tryAgain = "再試行"

    override val targetPlaceholder = "ウェブサイトまたはIPアドレス"
    override val clearTarget = "クリア"
    override val checkSectionTitle = "テスト"

    override val recentSectionTitle = "最近のテスト"

    override val toolPortsDescription = "開いているポート"

    override val pingQuestion = "応答するか？"

    override val dnsQuestion = "IPアドレスは？"

    override val dnsDetail = "DNS ルックアップ"
    override val portsQuestion = "利用できるサービスは？"

    override val runningPortScanTitle = "ポートを確認中"

    override val diagnosticFailed = "診断に失敗しました"
    override val invalidTarget = "URLのパスや空白を含めずに、ホスト名またはIPアドレスを入力してください。"

    override val diagnosticCouldNotComplete = "診断を完了できませんでした。"
    override val connectionChangedRunAgain = "接続先が変わりました。新しいネットワークで再度テストしてください。"

    override val openItsWebPage = "Web ページを開く"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ${total} 回中 ${answered} 回応答"

    override val slowest = "最も遅い"
    override fun repliesContentDescription(replies: String) = "返信: ${replies}"

    override val pingDidntAnswer = "応答なし"

    override val pingResponsive = "応答は良好"

    override val pingResponsiveButUneven = "応答にばらつきあり"

    override val pingDroppingReplies = "一部の要求に応答なし"

    override val pingSlowToAnswer = "応答が遅い"

    override val pingNoAnswerLocal = "電源がオフ、スリープ中、またはPingに応答しない設定の可能性があります。"

    override fun pingRepliesLost(lost: Int, total: Int) = "${total} 回中 ${lost} 回、応答がありませんでした。ルーターへのテストとインターネット上のホストへのテストを比較すると、問題の場所を絞り込めます。このテストだけでは原因を特定できません。"

    override val pingUneven = "応答時間にばらつきがありました。この場所とルーターの近くで結果を比較し、インターネット上のホストでもテストしてください。"

    override val pingLocalExplanation = "このデバイスまで往復する時間です。接続先のデバイス自体も結果に影響します。"

    override val pingInternetExplanation = "このホストまで往復する時間です。接続状況、通信経路、ホストの状態が結果に影響します。"

    override fun dnsAddressUnit(count: Int) = "件のアドレス"

    override fun dnsForQuery(query: String) = "${query}の名前解決"

    override val dnsNotFound = "アドレスが見つかりません"

    override val dnsFound = "アドレスが見つかりました"

    override val dnsNotFoundExplanation = "入力した名前を確認し、もう一度お試しください。その名前が登録されていない可能性もあります。"

    override fun dnsFoundExplanation(query: String) = "このデバイスで${query}のアドレスを取得できました。"

    override val dnsAddressesTitle = "IPアドレス"

    override val portsOpenUnit = "件のポートが開放"

    override fun portsCommonServicesOn(host: String) = "${host} の共通サービス"
    override val portsNothingOpen = "開いているポートなし"

    override fun portsOffersServices(count: Int) = "${count}件のサービスを利用できます"

    override val portsNoneAnswered = "よく使われるサービスからの応答はありませんでした。"

    override val portsOffersExplanation = "設定ページやファイル共有などのサービスです。"

    override val portWebPage = "ウェブページ"
    override val portSecureWebPage = "安全なウェブページ"
    override val portRemoteLoginSsh = "リモートログイン (SSH)"
    override val portOldRemoteLoginTelnet = "古いリモート ログイン (Telnet)"
    override val portFileTransferFtp = "ファイル転送 (FTP)"
    override val portAddressLookupsDns = "名前解決（DNS）"

    override val portFileSharing = "ファイル共有"
    override val portMacFileSharing = "Mac ファイル共有"
    override val portPrinting = "印刷"
    override val portRemoteDesktop = "リモートデスクトップ"
    override val portScreenSharing = "画面共有"
    override val portDeviceDiscoveryUpnp = "デバイス検出 (UPnP)"
    override val portStorageOrAdminPage = "ストレージまたは管理ページ"
    override val portDatabase = "データベース"
    override val portService = "サービス"
    override val devicesLooking = "デバイスを検索中…"

    override val devicesFindOnWifi = "Wi-Fiに接続中のデバイスを検索"

    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${ssid}で${count}台を検出"

    override fun devicesOnYourNetwork(count: Int) = "使用中のネットワークで${count}台を検出"

    override val couldntLookForDevices = "デバイスを検索できませんでした"

    override val scanFailed = "スキャンに失敗しました"
    override val connectionChangedLookAgain = "接続先が変わりました。このネットワークで再検索してください。"

    override val stoppedLooking = "検索を停止しました"

    override val noDevicesFound = "デバイスが見つかりませんでした"
    override val noDevicesFoundSubtitle = "スキャンに応答しないデバイスもあります。少し待って再試行するか、このデバイスが自宅のWi-Fiに接続されているか確認してください。"

    override fun otherDevicesGroup(count: Int) = "その他のデバイス · ${count}"
    override fun quietDevices(count: Int) = "種類が不明なデバイス${count}台"

    override val quietDevicesSubtitle = "デバイスの種類を取得できませんでした"

    override val hide = "閉じる"

    override val show = "表示"

    override fun checkingLocalAddresses(percent: Int) = "ローカルアドレスを確認しています · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · 応答しませんでした"

    override val deviceNotScannedYet = "まだスキャンで見つかっていません"

    override val deviceAnsweredLastScan = "前回のスキャンで応答あり"

    override val deviceSeenButNoAnswer = "ネットワーク上で検出されましたが、応答はありませんでした"

    override val actionCheckPorts = "ポートを確認"

    override val openRouterSettings = "ルーター設定を開く"
    override val detailsTitle = "詳細"
    override val factNetworkName = "ネットワーク名"
    override val factHardwareAddress = "ハードウェアアドレス"
    override val macHiddenByAndroid = "Androidによって非公開"

    override val factYourName = "設定した名前"

    override val portsNotCheckedYet = "まだ確認していません"

    override fun portsNoneOpenWhenChecked(moment: String) = "${moment}に確認した時点では、よく使われるポートは開いていませんでした。"

    override fun portsCheckedAt(moment: String) = "確認日時：${moment}"

    override val renameDeviceTitle = "デバイスの名前を変更"
    override val renameSupporting = "空欄にすると、WiFi Weakspotが検出した名前を使います。"

    override val connectionChangedOpenAgain = "接続先が変わりました。新しいネットワークでこのデバイスの詳細を開き直してください。"

    override val couldntLoadDetails = "保存された詳細をロードできませんでした。このデバイスをもう一度開いてみてください。"
    override val scanBeforeNaming = "名前を保存する前に、このデバイスをスキャンしてください。"

}
