package com.bernaferrari.wifiweakspot.core.i18n.ko

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object KoAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = "네트워크 ${count}개"

    override fun channelShort(channel: Int) = "채널 ${channel}"

    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · ${channelShort(channel)} · ${band}"

    override val noPassword = "비밀번호 없음"
    override val wifiOffTitle = "Wi-Fi 꺼짐"
    override val locationOffBody = "Android에서 주변 Wi-Fi 목록을 보려면 위치 기능을 켜야 합니다. WiFi Weakspot은 GPS를 사용하지 않습니다."

    override val scanPermissionBody = "Android에서 주변 Wi-Fi 이름을 확인하려면 위치 접근 권한이 필요합니다. WiFi Weakspot은 사용자의 위치를 기록하지 않습니다."

    override val wifiOffBody = "주변 네트워크를 보려면 Wi-Fi를 켜세요."

    override val locationOffTitle = "위치 기능 꺼짐"

    override val turnOnLocation = "위치 켜기"
    override val scanPausedTitle = "Android에서 스캔을 제한함"

    override fun scanPausedBody(seconds: Long) = "${seconds}초 후에 다시 스캔할 수 있습니다. 스캔 횟수 제한을 해제하면 기다리지 않아도 됩니다."

    override val turnOffThrottling = "스캔 횟수 제한 해제"

    override val readyToScanBody = "스캔을 눌러 주변 네트워크를 검색하세요."

    override val scan = "스캔"
    override val channels = "채널"
    override val signalOverTime = "시간에 따른 신호"
    override val howToReadThisChart = "그래프 보는 방법"

    override val expandSignalHistory = "신호 기록 확대"

    override val expandChannels = "채널 그래프 확대"

    override fun noBandNetworksNearby(band: String) = "근처에 ${band} 네트워크가 없습니다."
    override fun networksOnBand(count: Int, band: String) = if (count == 0) "${band} 네트워크 없음" else "${band} 네트워크 ${count}개"

    override fun showVeryWeakNetworks(count: Int) = "신호가 매우 약한 네트워크 ${count}개 표시"

    override fun hideVeryWeakNetworks(count: Int) = "신호가 매우 약한 네트워크 ${count}개 숨기기"

    override fun bandNetworkCount(count: Int) = if (count == 0) "없음" else "${count}개"

    override val highlightedOnChart = "그래프에서 강조됨"

    override val notHighlightedOnChart = "그래프에서 강조되지 않음"

    override fun detailsForNetwork(ssid: String) = "${ssid.ifEmpty { "숨겨진 네트워크" }}에 대한 세부정보"
    override fun alsoYourRouter(quality: String) = "${quality} · 같은 공유기의 다른 네트워크"

    override val connected = "연결됨"

    override val scanReportHeading = "WiFi Weakspot 스캔 보고서"

    override fun scanReportTime(time: String) = "시간: ${time}"
    override fun scanReportTotal(count: Int) = "총 네트워크 수: ${count}"
    override val scanReportNetworksHeading = "네트워크:"
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) = "• ${ssid} - ${rssi} dBm, ${channelShort(channel)}, ${security}"

    override val channelLooksClear = "현재 채널은 혼잡하지 않음"

    override val noChannelImprovement = "더 나은 채널을 찾지 못함"

    override fun lessOverlapThanChannel(currentChannel: Int) = "채널 ${currentChannel}보다 덜 혼잡함"

    override val suggestedChannel = "추천 채널"

    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}: 채널 ${channel}"
    override val lowOverlap = "겹침 적음"

    override val moderateOverlap = "겹침 보통"

    override val highOverlap = "겹침 많음"

    override val veryHighOverlap = "겹침 매우 많음"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) = "채널 그래프. 네트워크 ${networkCount}개." + if (busiestChannel != null) " 가장 혼잡한 채널은 ${busiestChannel}이며 네트워크 ${busiestCount}개가 있습니다." else ""

    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) = name + (if (isYou) " (${connected})" else "") + (if (extraNames > 0) " +${extraNames}" else "")

    override val axisStrong = "강함"
    override val axisOkay = "보통"
    override val axisWeak = "약함"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · ${channelShort(channel)}"

    override val clearSelectedNetwork = "네트워크 선택 해제"

    override val tapWaveForDetails = "곡선을 눌러 네트워크 상세 정보를 확인하세요"

    override val chartGuideTitle = "그래프 보는 방법"

    override val guideTallerBody = "곡선의 높이는 신호 세기를 나타냅니다."

    override val guideOverlapBody = "겹치는 네트워크는 주파수 대역을 공유하므로 전송 시간을 두고 경쟁할 수 있습니다."

    override val guideWiderBody = "채널 폭이 넓을수록 더 많은 주파수를 사용하므로 주변 네트워크와 겹치기 쉽습니다."

    override val guideSameRouterBody = "+1: 같은 Wi-Fi 무선 모듈을 사용하는 것으로 보이는 네트워크 이름을 하나의 곡선으로 묶어 표시합니다."

    override val guideSteadyBody = "선이 크게 오르내리면 신호 세기가 변하고 있다는 뜻입니다. 선을 눌러 일시정지한 뒤 좌우로 움직여 시점을, 위아래로 움직여 네트워크를 바꿔 보세요. 다시 시작하면 새 측정값이 표시됩니다."

    override val guideHighlightBody = "곡선이나 목록의 항목을 누르면 강조됩니다. 그래프를 좌우로 드래그해 살펴보세요."

    override fun channelOverlapHeading(count: Int, channel: Int) = if (count == 0) "채널 ${channel}에 겹침이 없습니다" else "채널 ${channel}와 겹치는 네트워크 ${count}개"

    override fun fromChannel(homeChannel: Int, reading: String) = "채널 ${homeChannel}에서 · ${reading}"

    override val estimatedChannelOverlap = "예상 채널 겹침"

    override fun tryChannel(channel: Int) = "채널 ${channel}을 이용해 보세요"
    override val lowestOverlapFor20Mhz = "20 MHz 채널 기준으로 예상 겹침이 가장 적습니다"

    override val overlapDisclaimer = "주변 액세스 포인트와 신호 세기에 따른 추정이며 실제 트래픽을 측정한 값은 아닙니다. 설정을 바꾸기 전에 공유기의 지원 채널과 해당 지역의 규정을 확인하세요. 5 GHz와 6 GHz 제안은 공유기 지원 여부가 확인된 경우에만 제공됩니다. 넓은 채널의 중심 주파수를 알 수 없으면 기본 20 MHz 채널만 표시합니다."

    override val overlapScore = "겹침 점수"

    override val collectingReadings = "측정값을 모으는 중입니다. 몇 차례 스캔하면 선이 표시됩니다."

    override fun signalHistoryDescription(networkCount: Int) = "네트워크 ${networkCount}개의 신호 기록입니다. 선을 눌러 확인하세요. 방향키로 시점과 네트워크를 바꿀 수 있습니다."

    override val historyLive = "실시간"

    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) = "${networkName}, ${reading ?: "측정값 없음"}, 일시정지, ${readingsAgo(readingsAgo)}"

    override val strongerNetwork = "신호가 더 강한 네트워크"

    override val weakerNetwork = "신호가 더 약한 네트워크"

    override val axisOlder = "이전"
    override val axisLatest = "최신"
    override val exploreSignalHistory = "신호 기록 살펴보기"

    override val tapLineToCompare = "선을 누르고 드래그해 비교하세요"

    override val noReadingAtPoint = "이 시점의 측정값이 없습니다"

    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) = "${reading} · ${quality} · " + if (readingsAgo == 0) "최신 측정값" else readingsAgo(readingsAgo)

    private fun readingsAgo(count: Int) = "${count}회 전 측정"

    override val resumeLive = "실시간 표시 재개"

    override fun seenReading(rssi: Int, seen: String) = "${rssi} dBm · 발견: ${seen}"

    override val factChannel = "채널"
    override val factWidth = "채널 폭"

    override val factSecurity = "보안"
    override val factDistance = "예상 거리"

    override val notAvailable = "확인할 수 없음"

    override val watch = "신호 추적"

    override val addToFavorites = "즐겨찾기에 추가"
    override val removeFromFavorites = "즐겨찾기에서 삭제"

    override fun routerCheckLastTime(pingMs: String) = "지난번: ${pingMs} ms"

    override fun devicesFoundLastScan(count: Int) = "지난 스캔에서 ${count}개 발견"

    override val devicesSubtitle = "연결된 기기"

    override val testWebsiteSubtitle = "Ping, DNS, 포트"

    override fun savedChecks(count: Int) = if (count == 0) "지난 결과" else "테스트 ${count}회"

    override val notOnWifi = "Wi-Fi에 연결되지 않음"

    override val online = "온라인"
    override val internetNotConfirmed = "인터넷 연결 확인 안 됨"

    override fun channelChip(channel: Int) = "채널 ${channel}"
    override fun linkRateChip(mbps: Int) = "링크 속도 ${mbps} Mbps"
    override val chooseNetwork = "네트워크 선택"
    override val turnOnWifi = "Wi-Fi 켜기"
    override val dismiss = "닫기"
    override val findWeakSpotsTitle = "신호가 약한 곳 찾기"

    override val findWeakSpotsBody = "어느 방에서 신호가 약한지 확인하세요."

    override val findWeakSpotsAction = "방마다 측정하기"

    override fun networksNearby(count: Int) = "주변 네트워크 ${count}개"

    override val nearbyStale = "지난 스캔 결과입니다. 눌러서 새로고침하세요."

    override val nearbyNoConnection = "각 네트워크의 채널과 신호를 확인하세요."
    override fun nearbyOverlap(count: Int, channel: Int) = if (count == 0) "현재 채널 ${channel}와 겹치는 주변 신호가 없습니다." else "주변 신호 ${count}개가 현재 채널 ${channel}와 겹칩니다."

    override val adviceNoWifiBody = "확인하려는 네트워크에 연결하세요. 현재 연결을 사용하여 인터넷 테스트 및 네트워크 도구를 계속 실행할 수 있습니다."
    override val adviceNoInternetTitle = "먼저 인터넷 연결 확인"

    override val adviceNoInternetBody = "Android에서 이 Wi-Fi의 인터넷 연결을 확인하지 못했습니다. 로그인이 필요한 페이지가 있는지 확인한 뒤 공유기와 인터넷 호스트로 연결을 테스트하세요."

    override val adviceNoSignalTitle = "Wi-Fi 신호 측정"

    override val adviceNoSignalBody = "네트워크 화면에서 Wi-Fi 스캔을 허용하면 신호를 측정할 수 있습니다. 문제가 있는 방에서 측정해 다른 곳과 비교하세요."

    override val adviceWeakSignalTitle = "공유기 근처에서 측정해 비교"

    override val adviceWeakSignalBody = "현재 Wi-Fi 신호가 약합니다. 여기서 공유기를 테스트한 뒤 공유기 근처에서 다시 테스트하세요. 자신의 네트워크를 지도에 측정하면 신호가 약한 곳을 찾을 수 있습니다."

    override val adviceGoodSignalTitle = "공유기 응답 비교"

    override val adviceGoodSignalBody = "신호 세기는 사용 가능한 수준이며 Android에서 인터넷 연결도 확인했습니다. 느리게 느껴진다면 현재 위치와 공유기 근처의 응답 시간을 비교하세요. 신호가 강하다고 통신 성능이 보장되는 것은 아닙니다."

    override val channelAdviceBasis = "막대가 짧을수록 이번 스캔에서 겹침이 적습니다. 속도는 달라질 수 있습니다."
    override fun channelChangeInstructions(channel: Int, band: String) = "${band}에서 채널 ${channel} 선택"

    override val channelOverlapTitle = "주변 Wi-Fi와의 겹침"
    override val channelApplyChange = "저장하고 필요하면 다시 연결"
    override val channelChangeNote = "WiFi Weakspot이 아닌 공유기 앱이나 설정에서 변경하세요."

}
