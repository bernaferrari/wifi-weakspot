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
    override val qualityGoodExplanation = "공유기에서 충분한 세기의 신호가 수신됩니다."

    override val qualityFairLabel = "보통"
    override val qualityFairHeadline = "신호 보통"
    override val qualityFairExplanation = "사용 가능한 신호이지만 간섭에 더 민감합니다."
    override val qualityWeakLabel = "약함"
    override val qualityWeakHeadline = "신호가 약한 곳"
    override val qualityWeakExplanation = "신호가 약합니다. 공유기에 더 가까운 곳에서 측정해 비교해 보세요."

    override val qualityDeadZoneLabel = "신호 없음"
    override val qualityDeadZoneExplanation = "이 지점에는 신호가 거의 도달하지 않습니다."

    override val deviceKindRouter = "공유기"
    override val deviceKindThisDevice = "이 기기"
    override val deviceKindPhone = "휴대전화 또는 태블릿"
    override val deviceKindComputer = "컴퓨터"
    override val deviceKindTv = "TV 또는 스트리밍 기기"

    override val deviceKindSpeaker = "스피커"
    override val deviceKindPrinter = "프린터"
    override val deviceKindConsole = "게임 콘솔"
    override val deviceKindCamera = "카메라"
    override val deviceKindSmartHome = "스마트 홈 기기"
    override val deviceKindUnknown = "기기"
    override val unnamedDevice = "이름 없는 기기"

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

    override val verdictGreatHeadline = "공유기 응답이 빠릅니다"

    override val verdictGreatExplanation = "응답이 빠르고 안정적이었습니다."

    override val verdictGoodHeadline = "공유기 응답은 사용 가능한 수준입니다"

    override val verdictGoodExplanation = "일부 응답이 조금 느리거나 일정하지 않았습니다."

    override val verdictSlowHeadline = "공유기 응답이 지연됩니다"

    override val verdictSlowExplanation = "응답이 느리거나 일정하지 않았습니다. 공유기 근처에서 다시 테스트해 비교해 보세요."

    override val verdictUnreliableHeadline = "공유기 응답을 판단하기 어렵습니다"

    override val verdictUnreliableExplanation = "응답이 없는 요청이 많았습니다. 일부 공유기는 이 테스트에 응답하지 않으므로 설정을 바꾸기 전에 다시 시도하세요."

    override val roomSuggestions = listOf("거실", "주방", "침실", "서재", "아이 방", "욕실", "식당", "복도", "차고", "지하실", "발코니", "정원")

    override val coverageNoRooms = "아직 측정한 방이 없습니다"

    override fun coverageWeakInRoom(room: String) = "${room}의 Wi-Fi 신호가 약합니다"

    override fun coverageWeakInRooms(count: Int) = "방 ${count}곳에서 Wi-Fi 신호가 약합니다"

    override fun coverageRoomsCheckedNoneWeak(count: Int) = "방 ${count}곳을 측정했으며 신호가 약한 방은 없습니다"

    override fun tipHighBandWeak(room: String, band: String) = "${room}에서는 ${band} 대역을 사용했습니다. 이 대역은 벽을 통과할 때 신호가 약해지기 쉽습니다. 공유기에서 대역을 분리할 수 있다면 2.4 GHz가 더 안정적일 수 있습니다."

    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) = "${bestRoom}에서 ${worstRoom}으로 이동하면 신호가 ${dropDb} dB 약해집니다. 공유기를 집의 중앙에 더 가깝고 높은 곳으로 옮긴 뒤 같은 장소에서 다시 측정해 보세요."

    override fun tipMultipleAccessPoints(count: Int) = "이 방들에서 액세스 포인트 ${count}개를 사용했습니다. 서로 다른 대역이나 접속 지점 때문일 수 있으며, 이것만으로 로밍의 안정성을 판단할 수는 없습니다."

    override val tipRouterPlacement = "공유기는 실내의 높고 장애물이 없는 곳에 두고 전자레인지, 거울, 수조에서 멀리 떨어뜨려 놓으세요."

    override val spotFirstReading = "첫 측정입니다. 환경을 바꾼 뒤 다시 측정해 비교하세요"

    override val spotDifferentNetwork = "네트워크 또는 장소가 다릅니다. 새로 비교를 시작하세요"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "신호가 ${deltaDb} dB 강해졌습니다"
            deltaDb <= -4 -> "신호가 ${-deltaDb} dB 약해졌습니다"
            else -> "신호는 거의 같습니다"
        }
        val changed = buildList {
            if (accessPointChanged) add("액세스 포인트 변경")
            if (bandChanged) add("주파수 대역 변경")
        }
        return signal + if (changed.isEmpty()) " (지난 측정과 비교)" else " · ${changed.joinToString("; ")}"
    }

    override val monitorChannelName = "백그라운드 모니터링"
    override val monitorChannelDescription = "WiFi Weakspot의 Wi-Fi 모니터링 상태를 표시합니다"

    override val alertsChannelName = "네트워크 경고"
    override val alertsChannelDescription = "새 네트워크와 신호 변화를 알립니다"

    override val monitorStartFailed = "모니터링을 시작하지 못했습니다. Wi-Fi 스캔 권한을 허용하고 위치 기능을 켠 뒤 다시 시도하세요."

    override val monitorWaitingForScanLimited = "새 Wi-Fi 스캔 결과를 기다리는 중입니다. Android에서 스캔 횟수를 제한할 수 있습니다."

    override val monitorWaitingForScan = "새 Wi-Fi 스캔 결과를 기다리는 중"

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
            MonitorSignalLevel.Excellent -> qualityExcellentLabel
            MonitorSignalLevel.Good -> qualityGoodLabel
            MonitorSignalLevel.Fair -> qualityFairLabel
            MonitorSignalLevel.Weak -> qualityWeakLabel
        }
        return "“${networkName}” 신호: ${quality} (${rssi} dBm)"
    }

    override val exportFileChooserTitle = "파일 내보내기"
    override fun chartDescription(name: String) = "차트: ${name}"

    override val lanVpnUnavailable = "이 VPN 연결에서는 기기를 검색할 수 없습니다. VPN의 로컬 네트워크 접근 허용 여부를 확인하거나 VPN을 끈 뒤 다시 시도하세요."

    override val lanConnectFirst = "로컬 기기를 검색하려면 Wi-Fi 또는 이더넷에 연결하세요."

    override val lanNotConnected = "연결되지 않음"

}
