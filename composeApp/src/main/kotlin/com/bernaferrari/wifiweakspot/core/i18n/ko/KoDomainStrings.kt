package com.bernaferrari.wifiweakspot.core.i18n.ko

import com.bernaferrari.wifiweakspot.core.i18n.DomainStrings
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind
import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel

object KoDomainStrings : DomainStrings {

    override val signalDisplayBars = "막대"
    override val qualityExcellentLabel = "매우 좋음"
    override val qualityExcellentHeadline = "신호 매우 좋음"
    override val qualityExcellentExplanation = "공유기와의 연결 신호가 강합니다. 신호 세기만으로 인터넷 속도를 알 수는 없습니다."
    override val qualityGoodLabel = "좋음"
    override val qualityGoodHeadline = "신호 좋음"
    override val qualityGoodExplanation = "라우터에서 확실한 신호가 수신됩니다."
    override val qualityFairLabel = "보통"
    override val qualityFairHeadline = "신호 보통"
    override val qualityFairExplanation = "사용 가능한 신호이지만 간섭에 더 민감합니다."
    override val qualityWeakLabel = "약함"
    override val qualityWeakHeadline = "신호가 약한 곳"
    override val qualityWeakExplanation = "신호가 약해요. 이 지점을 라우터에 더 가까운 지점과 비교하십시오."
    override val qualityDeadZoneLabel = "신호 없음"
    override val qualityDeadZoneExplanation = "이 지점에는 신호가 거의 도달하지 않습니다."

    override val deviceKindRouter = "공유기"
    override val deviceKindThisDevice = "이 기기"
    override val deviceKindPhone = "휴대전화 또는 태블릿"
    override val deviceKindComputer = "컴퓨터"
    override val deviceKindTv = "TV 또는 스트리머"
    override val deviceKindSpeaker = "스피커"
    override val deviceKindPrinter = "프린터"
    override val deviceKindConsole = "게임 콘솔"
    override val deviceKindCamera = "카메라"
    override val deviceKindSmartHome = "스마트 홈 기기"
    override val deviceKindUnknown = "기기"
    override val unnamedDevice = "이름이 지정되지 않은 기기"

    override fun makerDevice(maker: String, kind: DeviceKind): String = "${maker} ${kindLabel(kind)}"

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

    override val verdictGreatHeadline = "라우터가 빠르게 응답합니다."
    override val verdictGreatExplanation = "답변은 빠르고 꾸준했습니다."
    override val verdictGoodHeadline = "라우터 응답을 사용할 수 있습니다."
    override val verdictGoodExplanation = "일부 답변이 약간 느리거나 고르지 못했습니다."
    override val verdictSlowHeadline = "라우터 응답이 지연됩니다."
    override val verdictSlowExplanation = "답변이 느리거나 고르지 못했습니다. 라우터 근처에서 다시 시도하여 비교해 보세요."
    override val verdictUnreliableHeadline = "라우터 확인 결과가 나오지 않음"
    override val verdictUnreliableExplanation = "많은 답글이 누락되었습니다. 일부 라우터는 이러한 검사를 무시하므로 변경하기 전에 다시 시도하십시오."

    override val roomSuggestions = listOf("거실", "주방", "침실", "서재", "아이 방", "욕실", "식당", "복도", "차고", "지하실", "발코니", "정원")

    override val coverageNoRooms = "아직 확인된 객실이 없습니다."
    override fun coverageWeakInRoom(room: String) = "약함 Wi-Fi ${room}"
    override fun coverageWeakInRooms(count: Int) = "${count}개 방에서 약 Wi-Fi"
    override fun coverageRoomsCheckedNoneWeak(count: Int) =
        if (count == 1) "방 1개 확인, 약함 없음" else "${count} 객실 확인, 약한 객실 없음"

    override fun tipHighBandWeak(room: String, band: String) =
        "${room}에서 기기는 ${band}를 사용했는데, 이는 벽을 통과하는 거리가 더 짧습니다. 라우터가 대역을 분할할 수 있다면 2.4 GHz가 더 잘 유지될 수 있습니다."
    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) =
        "신호가 ${bestRoom}에서 ${worstRoom}까지 ${dropDb} dB 감소합니다. 좀 더 중앙에 올려진 라우터 위치를 시도한 다음 동일한 지점을 다시 확인하여 도움이 되는지 확인하세요."
    override fun tipMultipleAccessPoints(count: Int) =
        "귀하의 기기는 이 방 전체에서 ${count} 액세스 포인트를 사용했는데, 이는 다양한 대역이나 액세스 포인트를 반영할 수 있습니다. 이것만으로는 안정적인 로밍을 보장할 수 없습니다."
    override val tipRouterPlacement = "라우터를 바닥에서 떨어진 개방된 곳에 보관하고 전자레인지, 거울, 수조에서 멀리 두세요."

    override val spotFirstReading = "첫 번째 읽기 · 변경 후 다시 확인"
    override val spotDifferentNetwork = "다른 네트워크 또는 지점 · 새로운 비교 시작"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "신호 ${deltaDb}dB 더 강함"
            deltaDb <= -4 -> "신호 ${-deltaDb} dB 약함"
            else -> "같은 신호"
        }
        val changed = buildList {
            if (accessPointChanged) add("액세스 포인트가 변경되었습니다.")
            if (bandChanged) add("밴드 변경됨")
        }
        return signal + if (changed.isEmpty()) " 마지막 확인 이후" else " · ${changed.joinToString("; ")}"
    }

    override val monitorChannelName = "백그라운드 모니터링"
    override val monitorChannelDescription = "WiFi Weakspot이 WiFi 네트워크를 모니터링하는 경우 표시됩니다."
    override val alertsChannelName = "네트워크 경고"
    override val alertsChannelDescription = "새로운 네트워크 및 신호 변경에 대한 알림"
    override val monitorStartFailed = "모니터링을 시작할 수 없습니다. Wi-Fi 검색을 허용하고 위치 서비스를 활성화한 후 다시 시도하세요."
    override val monitorWaitingForScanLimited = "새로운 Wi-Fi 스캔을 기다리는 중입니다. Android 스캔 요청이 제한될 수 있습니다."
    override val monitorWaitingForScan = "새로운 Wi-Fi 스캔을 기다리는 중"
    override fun monitorStatus(networkCount: Int, lastScanTime: String) = "네트워크 ${networkCount}개 · 마지막 스캔: ${lastScanTime}"

    override fun newNetworksTitle(count: Int) =
        if (count == 1) "신규 WiFi 네트워크 감지됨" else "새로운 WiFi 네트워크 감지됨"

    override fun newNetworksText(names: List<String>): String = when {
        names.size == 1 -> "\"${names.first()}\"이(가) 범위 내에 있습니다."
        else -> "${names[0]}, ${names[1]}" + if (names.size > 2) " 등" else ""
    }

    override val signalChangeTitle = "신호변경"
    override fun signalChangeText(networkName: String, level: MonitorSignalLevel, rssi: Int): String {
        val quality = when (level) {
            MonitorSignalLevel.Excellent -> "훌륭해요"
            MonitorSignalLevel.Good -> "좋아요"
            MonitorSignalLevel.Fair -> "박람회"
            MonitorSignalLevel.Weak -> "약함"
        }
        return "\"${networkName}\" 신호는 이제 ${quality} (${rssi} dBm)입니다."
    }

    override val exportFileChooserTitle = "파일 내보내기"
    override fun chartDescription(name: String) = "차트: ${name}"

    override val lanVpnUnavailable = "이 VPN 연결을 통해서는 장치 검색을 사용할 수 없습니다. VPN이 로컬 네트워크 액세스를 허용하는지 확인하거나 연결이 끊어지면 다시 시도하세요."
    override val lanConnectFirst = "로컬 장치를 검색하기 전에 Wi-Fi 또는 이더넷에 연결하세요."
    override val lanNotConnected = "연결되지 않음"

}
