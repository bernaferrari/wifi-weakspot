package com.bernaferrari.wifiweakspot.core.i18n.ja

import com.bernaferrari.wifiweakspot.core.i18n.DomainStrings
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind
import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel

object JaDomainStrings : DomainStrings {

    override val signalDisplayBars = "バー"
    override val qualityExcellentLabel = "非常に良い"
    override val qualityExcellentHeadline = "非常に良い信号"
    override val qualityExcellentExplanation = "ルーターとの接続は良好です。信号の強さだけではインターネット速度はわかりません。"
    override val qualityGoodLabel = "良い"
    override val qualityGoodHeadline = "良い信号"
    override val qualityGoodExplanation = "ルーターからのしっかりした信号。"
    override val qualityFairLabel = "普通"
    override val qualityFairHeadline = "普通の信号"
    override val qualityFairExplanation = "接続に使える信号ですが、干渉の影響を受けやすくなっています。"
    override val qualityWeakLabel = "弱い"
    override val qualityWeakHeadline = "信号が弱い場所"
    override val qualityWeakExplanation = "信号が弱いです。このスポットをルーターに近い場所と比較してください。"
    override val qualityDeadZoneLabel = "圏外"
    override val qualityDeadZoneExplanation = "このスポットに届く信号はほとんどありません。"

    override val deviceKindRouter = "ルーター"
    override val deviceKindThisDevice = "このデバイス"
    override val deviceKindPhone = "スマートフォン・タブレット"
    override val deviceKindComputer = "コンピューター"
    override val deviceKindTv = "テレビまたはストリーマー"
    override val deviceKindSpeaker = "スピーカー"
    override val deviceKindPrinter = "プリンター"
    override val deviceKindConsole = "ゲーム機"
    override val deviceKindCamera = "カメラ"
    override val deviceKindSmartHome = "スマートホームデバイス"
    override val deviceKindUnknown = "デバイス"
    override val unnamedDevice = "名前のないデバイス"

    override fun makerDevice(maker: String, kind: DeviceKind): String = "${maker}の${kindLabel(kind)}"

    private fun kindLabel(kind: DeviceKind): String = when (kind) {
        DeviceKind.Router -> deviceKindRouter
        DeviceKind.ThisPhone -> deviceKindThisDevice
        DeviceKind.Phone -> deviceKindPhone
        DeviceKind.Computer -> deviceKindComputer
        DeviceKind.Tv -> deviceKindTv
        DeviceKind.Speaker -> deviceKindSpeaker
        DeviceKind.Printer -> deviceKindPrinter
        DeviceKind.Console -> deviceKindConsole
        DeviceKind.Camera -> deviceKindCamera
        DeviceKind.SmartHome -> deviceKindSmartHome
        DeviceKind.Unknown -> deviceKindUnknown
    }

    override val verdictGreatHeadline = "ルーターの応答が速い"
    override val verdictGreatExplanation = "返信は迅速かつ安定していました。"
    override val verdictGoodHeadline = "ルーター応答は使用可能です"
    override val verdictGoodExplanation = "一部の返信は少し遅かったり、不均等でした。"
    override val verdictSlowHeadline = "ルーターの応答が遅れています"
    override val verdictSlowExplanation = "返信が遅かったり、不均一でした。ルーターの近くでもう一度試して比較してください。"
    override val verdictUnreliableHeadline = "ルーターのチェックが不確定"
    override val verdictUnreliableExplanation = "多くの返信が行方不明になりました。一部のルーターはこれらのチェックを無視するため、何かを変更する前にもう一度試してください。"

    override val roomSuggestions = listOf("リビング", "キッチン", "寝室", "書斎", "子ども部屋", "浴室", "ダイニング", "廊下", "ガレージ", "地下室", "バルコニー", "庭")

    override val coverageNoRooms = "チェックされた部屋はまだありません"
    override fun coverageWeakInRoom(room: String) = "${room} の弱い Wi-Fi"
    override fun coverageWeakInRooms(count: Int) = "${count} 部屋では弱い Wi-Fi"
    override fun coverageRoomsCheckedNoneWeak(count: Int) =
        if (count == 1) "1 部屋チェック済み、弱いものはありません" else "${count} 部屋をチェックしましたが、弱いものはありませんでした"

    override fun tipHighBandWeak(room: String, band: String) =
        "${room} では、デバイスは壁を通過する距離が少ない ${band} を使用しました。ルーターが帯域を分割できる場合は、2.4 GHz の方がよく耐えられる可能性があります。"
    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) =
        "信号が ${bestRoom} から ${worstRoom} に ${dropDb} dB 低下します。より中央の盛り上がったルーターの位置を試し、同じ場所を再チェックして効果があるかどうかを確認します。"
    override fun tipMultipleAccessPoints(count: Int) =
        "お使いのデバイスは、これらの部屋全体で ${count} アクセス ポイントを使用しました。これは、異なる帯域またはアクセス ポイントを反映している可能性があります。これだけでは、信頼できるローミングを確認できません。"
    override val tipRouterPlacement = "ルーターは屋外に置き、床から離し、電子レンジ、鏡、水槽から離してください。"

    override val spotFirstReading = "最初の読み込み・変更後の再確認"
    override val spotDifferentNetwork = "異なるネットワークまたはスポット · 新しい比較を開始します"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "信号 ${deltaDb} dB 強くなります"
            deltaDb <= -4 -> "信号 ${-deltaDb} dB 弱くなっています"
            else -> "ほぼ同じ信号"
        }
        val changed = buildList {
            if (accessPointChanged) add("アクセスポイントが変更されました")
            if (bandChanged) add("バンドが変わりました")
        }
        return signal + if (changed.isEmpty()) " 前回のチェック以降" else " · ${changed.joinToString("; ")}"
    }

    override val monitorChannelName = "バックグラウンド監視"
    override val monitorChannelDescription = "WiFi Weakspot が WiFi ネットワークを監視していることを示します"
    override val alertsChannelName = "ネットワークアラート"
    override val alertsChannelDescription = "新しいネットワークと信号の変化に関するアラート"
    override val monitorStartFailed = "監視を開始できませんでした。 Wi-Fi スキャンを許可し、位置情報サービスを有効にしてから、もう一度お試しください。"
    override val monitorWaitingForScanLimited = "新しい Wi-Fi スキャンを待っています。 Android によりスキャン リクエストが制限される場合があります。"
    override val monitorWaitingForScan = "新しい Wi-Fi スキャンを待っています"
    override fun monitorStatus(networkCount: Int, lastScanTime: String) = "ネットワーク${networkCount}件 · 最終スキャン：${lastScanTime}"

    override fun newNetworksTitle(count: Int) =
        if (count == 1) "新しい WiFi ネットワークが検出されました" else "新しい WiFi ネットワークが検出されました"

    override fun newNetworksText(names: List<String>): String = when {
        names.size == 1 -> "「${names.first()}」が範囲内になりました"
        else -> "${names[0]}, ${names[1]}" + if (names.size > 2) " など" else ""
    }

    override val signalChangeTitle = "信号変化"
    override fun signalChangeText(networkName: String, level: MonitorSignalLevel, rssi: Int): String {
        val quality = when (level) {
            MonitorSignalLevel.Excellent -> "素晴らしい"
            MonitorSignalLevel.Good -> "良い"
            MonitorSignalLevel.Fair -> "公平"
            MonitorSignalLevel.Weak -> "弱い"
        }
        return "「${networkName}」信号は ${quality} (${rssi} dBm) になりました"
    }

    override val exportFileChooserTitle = "ファイルのエクスポート"
    override fun chartDescription(name: String) = "チャート: ${name}"

    override val lanVpnUnavailable = "この VPN 接続ではデバイス検出を利用できません。 VPN でローカル ネットワーク アクセスが許可されているかどうかを確認するか、切断されたときにもう一度試してください。"
    override val lanConnectFirst = "ローカルデバイスをスキャンする前に、Wi-Fi またはイーサネットに接続してください。"
    override val lanNotConnected = "接続されていません"

}
