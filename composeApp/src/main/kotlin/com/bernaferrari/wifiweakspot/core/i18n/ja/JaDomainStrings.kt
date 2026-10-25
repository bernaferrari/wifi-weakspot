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
    override val qualityGoodExplanation = "ルーターから安定した信号が届いています。"

    override val qualityFairLabel = "普通"
    override val qualityFairHeadline = "普通の信号"
    override val qualityFairExplanation = "接続に使える信号ですが、干渉の影響を受けやすくなっています。"
    override val qualityWeakLabel = "弱い"
    override val qualityWeakHeadline = "信号が弱い場所"
    override val qualityWeakExplanation = "信号が弱くなっています。ルーターに近い場所でも測定して比較してください。"

    override val qualityDeadZoneLabel = "圏外"
    override val qualityDeadZoneExplanation = "このスポットに届く信号はほとんどありません。"

    override val deviceKindRouter = "ルーター"
    override val deviceKindThisDevice = "このデバイス"
    override val deviceKindPhone = "スマートフォン・タブレット"
    override val deviceKindComputer = "コンピューター"
    override val deviceKindTv = "テレビ・ストリーミング端末"

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

    override val verdictGreatHeadline = "ルーターの応答は速いです"

    override val verdictGreatExplanation = "応答は速く、安定していました。"

    override val verdictGoodHeadline = "ルーターの応答は実用的です"

    override val verdictGoodExplanation = "一部の応答に遅れやばらつきがありました。"

    override val verdictSlowHeadline = "ルーターの応答が遅れています"

    override val verdictSlowExplanation = "応答に遅れやばらつきがありました。ルーターの近くでもう一度テストして比較してください。"

    override val verdictUnreliableHeadline = "ルーターの応答を判定できません"

    override val verdictUnreliableExplanation = "多くの要求に応答がありませんでした。テストに応答しないルーターもあるため、設定を変える前にもう一度お試しください。"

    override val roomSuggestions = listOf("リビング", "キッチン", "寝室", "書斎", "子ども部屋", "浴室", "ダイニング", "廊下", "ガレージ", "地下室", "バルコニー", "庭")

    override val coverageNoRooms = "まだ部屋を測定していません"

    override fun coverageWeakInRoom(room: String) = "${room}のWi-Fi信号が弱いです"

    override fun coverageWeakInRooms(count: Int) = "${count}部屋でWi-Fi信号が弱くなっています"

    override fun coverageRoomsCheckedNoneWeak(count: Int) = "${count}部屋を測定済み。信号が弱い部屋はありません"

    override fun tipHighBandWeak(room: String, band: String) = "${room}では${band}を使いました。この周波数帯は壁越しに届きにくいため、ルーターで周波数帯を分けられる場合は、2.4 GHzのほうが安定することがあります。"

    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) = "${bestRoom}から${worstRoom}に移動すると、信号が${dropDb} dB弱くなっています。ルーターを家の中央寄りの高い場所に移して、同じ場所で再測定してください。"

    override fun tipMultipleAccessPoints(count: Int) = "これらの部屋では${count}個のアクセスポイントを使用しました。周波数帯や接続先の違いによる可能性があり、これだけではローミングの安定性は判断できません。"

    override val tipRouterPlacement = "ルーターは屋内の高く開けた場所に置き、電子レンジ、鏡、水槽から離してください。"

    override val spotFirstReading = "最初の測定です。環境を変えた後に再測定して比較できます"

    override val spotDifferentNetwork = "ネットワークまたは場所が異なります。新しく比較を始めてください"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "信号が${deltaDb} dB強くなりました"
            deltaDb <= -4 -> "信号が${-deltaDb} dB弱くなりました"
            else -> "信号はほぼ同じです"
        }
        val changed = buildList {
            if (accessPointChanged) add("接続先のアクセスポイントが変わりました")
            if (bandChanged) add("周波数帯が変わりました")
        }
        return signal + if (changed.isEmpty()) "（前回の測定と比較）" else " · ${changed.joinToString("; ")}"
    }

    override val monitorChannelName = "バックグラウンド監視"
    override val monitorChannelDescription = "WiFi WeakspotによるWi-Fi監視の実行状態を表示します"

    override val alertsChannelName = "ネットワークアラート"
    override val alertsChannelDescription = "新しいネットワークや信号の変化を通知します"

    override val monitorStartFailed = "監視を開始できませんでした。Wi-Fiスキャンを許可し、位置情報をオンにしてから、もう一度お試しください。"

    override val monitorWaitingForScanLimited = "新しいWi-Fiスキャンの結果を待っています。Androidによってスキャン回数が制限される場合があります。"

    override val monitorWaitingForScan = "新しいWi-Fiスキャンの結果を待っています"

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
            MonitorSignalLevel.Excellent -> qualityExcellentLabel
            MonitorSignalLevel.Good -> qualityGoodLabel
            MonitorSignalLevel.Fair -> qualityFairLabel
            MonitorSignalLevel.Weak -> qualityWeakLabel
        }
        return "「${networkName}」の信号：${quality}（${rssi} dBm）"
    }

    override val exportFileChooserTitle = "ファイルのエクスポート"
    override fun chartDescription(name: String) = "チャート: ${name}"

    override val lanVpnUnavailable = "このVPN接続ではデバイスを検出できません。VPNがローカルネットワークへのアクセスを許可しているか確認するか、VPNを切断して再試行してください。"

    override val lanConnectFirst = "デバイスを検索するには、Wi-Fiまたはイーサネットに接続してください。"

    override val lanNotConnected = "接続されていません"

}
