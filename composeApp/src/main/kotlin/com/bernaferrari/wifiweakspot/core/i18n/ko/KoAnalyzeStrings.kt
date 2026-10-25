package com.bernaferrari.wifiweakspot.core.i18n.ko

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object KoAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = "${count} 개 네트워크"

    override fun channelShort(channel: Int) = "채널 ${channel}"
    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · 채널 ${channel} · ${band}"
    override val noPassword = "비밀번호 없음"
    override val wifiOffTitle = "Wi-Fi 꺼짐"
    override val locationOffBody = "Android에서 주변 Wi-Fi 목록을 보려면 위치 기능을 켜야 합니다. WiFi Weakspot은 GPS를 사용하지 않습니다."

    override val scanPermissionBody = "Android 위치 액세스 권한이 있는 앱과만 근처 네트워크 이름을 공유합니다. WiFi Weakspot 절대 자신이 있는 곳을 기록하지 마세요."
    override val wifiOffBody = "근처 네트워크를 보려면 Wi-Fi을 켜세요."
    override val locationOffTitle = "위치가 꺼져있습니다"
    override val turnOnLocation = "위치 켜기"
    override val scanPausedTitle = "Android 스캔 일시중지됨"
    override fun scanPausedBody(seconds: Long) = "다음 스캔은 ${seconds}초 후에 허용됩니다. 대기 시간을 건너뛰려면 검사 조절을 끄십시오."
    override val turnOffThrottling = "제한 끄기"
    override val readyToScanBody = "스캔을 탭하면 근처의 모든 네트워크를 볼 수 있습니다."
    override val scan = "스캔"
    override val channels = "채널"
    override val signalOverTime = "시간에 따른 신호"
    override val howToReadThisChart = "이 차트를 읽는 방법"
    override val expandSignalHistory = "신호 기록 확장"
    override val expandChannels = "채널 확장"
    override fun noBandNetworksNearby(band: String) = "근처에 ${band} 네트워크가 없습니다."
    override fun networksOnBand(count: Int, band: String) = if (count == 0) "${band} 네트워크 없음" else "${band} 네트워크 ${count}개"

    override fun showVeryWeakNetworks(count: Int) =
        if (count == 1) "매우 약한 네트워크 1개 표시" else "표시 ${count} 매우 약한 네트워크 표시"
    override fun hideVeryWeakNetworks(count: Int) =
        if (count == 1) "매우 약한 네트워크 1개 숨기기" else "숨기기 ${count} 매우 약한 네트워크"
    override fun bandNetworkCount(count: Int) = if (count == 0) "없음" else networks(count)
    override val highlightedOnChart = "차트에 하이라이트 표시됨"
    override val notHighlightedOnChart = "차트에 강조표시되지 않음"
    override fun detailsForNetwork(ssid: String) = "${ssid.ifEmpty { "숨겨진 네트워크" }}에 대한 세부정보"
    override fun alsoYourRouter(quality: String) = "${quality} · 또한 귀하의 라우터"
    override val connected = "연결됨"

    override val scanReportHeading = "WiFi Weakspot 보고서 스캔"
    override fun scanReportTime(time: String) = "시간: ${time}"
    override fun scanReportTotal(count: Int) = "총 네트워크 수: ${count}"
    override val scanReportNetworksHeading = "네트워크:"
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) =
        "• ${ssid} - ${rssi}dBm, 채널 ${channel}, ${security}"
    override val channelLooksClear = "채널이 깔끔해 보이네요"
    override val noChannelImprovement = "이 스캔에서는 개선 사항을 찾을 수 없습니다."
    override fun lessOverlapThanChannel(currentChannel: Int) = "채널보다 중복이 적음 ${currentChannel}"
    override val leastOverlapInScan = "이 스캔에서 최소 중복"
    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}: 채널 ${channel}"
    override val lowOverlap = "낮은 중복"
    override val moderateOverlap = "중간 정도의 중복"
    override val highOverlap = "높은 중복"
    override val veryHighOverlap = "매우 높은 중복률"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) =
        "채널 그래프, ${networks(networkCount)}." +
            (if (busiestChannel != null) " 가장 바쁜 채널 ${busiestChannel}, ${busiestCount}." else "")
    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) =
        name + (if (isYou) " (당신)" else "") + (if (extraNames > 0) " +${extraNames}" else "")
    override val busyCaption = "혼잡"
    override val axisStrong = "강함"
    override val axisOkay = "보통"
    override val axisWeak = "약함"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · 채널 ${channel}"
    override val clearSelectedNetwork = "선택한 네트워크 지우기"
    override val tapWaveForDetails = "네트워크 세부정보를 보려면 웨이브를 탭하세요."

    override val chartGuideTitle = "차트 읽기"
    override val guideTallerBody = "키는 신호세기입니다."
    override val guideOverlapBody = "중복되는 네트워크는 스펙트럼을 공유하며 방송 시간을 놓고 경쟁할 수 있습니다."
    override val guideWiderBody = "더 넓은 채널은 더 많은 스펙트럼을 차지하고 더 많은 이웃과 겹칠 수 있습니다."
    override val guideSameRouterBody = "+1: 같은 라디오에서 나온 것처럼 보이는 이름은 언덕을 공유합니다."
    override val guideCrowdBody = "녹색은 겹침이 적고 빨간색은 많다는 뜻입니다. 2.4 GHz 아래의 스트립은 겹치는 부분을 보여줍니다. 탭하여 계산해 보세요."
    override val guideSteadyBody = "불안한 선은 신호 강도 변화를 나타냅니다. 선을 탭하여 일시 중지한 다음 시간을 따라 옆으로 슬라이드하거나 네트워크 사이에서 수직으로 슬라이드하세요. 새로운 내용을 보려면 라이브를 재개하세요."
    override val guideHighlightBody = "언덕이나 행을 탭하여 강조표시하세요. 탐색하려면 차트를 가로로 가로로 드래그하세요."

    override fun channelOverlapHeading(count: Int, channel: Int) = when (count) {
        0 -> "채널 ${channel}이 선명합니다"
        1 -> "1개의 네트워크 중복 채널 ${channel}"
        else -> "${count} 네트워크 중복 채널 ${channel}"
    }
    override fun fromChannel(homeChannel: Int, reading: String) = "채널 ${homeChannel} · ${reading}에서"

    override val estimatedChannelOverlap = "예상 채널 중복"
    override fun tryChannel(channel: Int) = "채널 ${channel}을 이용해 보세요"
    override val lowestOverlapFor20Mhz = "20 MHz 채널에 대한 가장 낮은 추정 중복"
    override val overlapDisclaimer = "측정된 트래픽이 아닌 주변 액세스 포인트 및 신호 강도를 기반으로 합니다. " +
        "설정을 변경하기 전에 라우터가 지원하는 채널과 로컬 규칙을 확인하세요. " +
        "5 및 6 GHz 제안에는 알려진 라우터 지원이 필요합니다. 광채널 센터 누락 " +
        "기본 20 MHz 채널만 표시합니다."
    override val overlapScore = "중복 점수"

    override val collectingReadings = "판독값 수집 중... 몇 번 스캔하면 선이 나타납니다."
    override fun signalHistoryDescription(networkCount: Int) =
        "${networks(networkCount)}의 신호 기록. 검사하려면 라인을 탭하세요. 화살표 키는 시간과 네트워크를 통해 이동합니다."
    override val historyLive = "라이브"
    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) =
        "${networkName}, ${reading ?: "읽지 마세요"}, 일시중지됨, ${readingsAgo(readingsAgo)}"
    override val strongerNetwork = "더욱 강력해진 네트워크"
    override val weakerNetwork = "약한 네트워크"
    override val axisOlder = "이전"
    override val axisLatest = "최신"
    override val exploreSignalHistory = "신호 기록 살펴보기"
    override val tapLineToCompare = "선을 탭한 후 드래그하여 비교하세요."
    override val noReadingAtPoint = "지금은 읽을 수 없습니다."
    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) =
        "${reading} · ${quality} · " + if (readingsAgo == 0) "최근 읽은 내용" else readingsAgo(readingsAgo)
    private fun readingsAgo(count: Int) = if (count == 1) "1회 읽기 전" else "${count} 읽은 시간 전"
    override val resumeLive = "라이브 재개"

    override fun seenReading(rssi: Int, seen: String) = "${rssi} dBm · 본 ${seen}"
    override val factChannel = "채널"
    override val factWidth = "너비"
    override val factSecurity = "보안"
    override val factDistance = "거리 추정"
    override val notAvailable = "사용할 수 없음"
    override val watch = "보기"
    override val addToFavorites = "즐겨찾기에 추가"
    override val removeFromFavorites = "즐겨찾기에서 삭제"

    override fun routerCheckLastTime(pingMs: String) = "${pingMs}ms 지난번"
    override fun devicesFoundLastScan(count: Int) = "${count} 마지막 스캔에서 발견됨"
    override val devicesSubtitle = "누가 연결되어 있나요?"
    override val testWebsiteSubtitle = "핑, DNS, 포트"
    override fun savedChecks(count: Int) = when (count) {
        0 -> "지난 결과"
        1 -> "1번 확인"
        else -> "${count} 확인"
    }
    override val notOnWifi = "켜져 있지 않음 Wi-Fi"
    override val online = "온라인"
    override val internetNotConfirmed = "인터넷 미확인"
    override fun channelChip(channel: Int) = "채널 ${channel}"
    override fun linkRateChip(mbps: Int) = "링크 속도 ${mbps} Mbps"
    override val chooseNetwork = "네트워크 선택"
    override val turnOnWifi = "Wi-Fi 켜기"
    override val dismiss = "닫기"
    override val findWeakSpotsTitle = "약점 찾기"
    override val findWeakSpotsBody = "어떤 방이 약한지 확인해보세요."
    override val findWeakSpotsAction = "방을 확인하세요"
    override fun networksNearby(count: Int) = "${networks(count)}"
    override val nearbyStale = "마지막 스캔에서. 새로고침하려면 탭하세요."
    override val nearbyNoConnection = "각 네트워크의 채널과 신호를 확인하세요."
    override fun nearbyOverlap(count: Int, channel: Int) = when (count) {
        0 -> "채널 ${channel}에서 근처 중복이 감지되지 않았습니다."
        1 -> "근처의 신호 1개가 채널 ${channel}과 중복됩니다."
        else -> "${count} 근처 신호가 채널 ${channel}과 겹칩니다."
    }

    override val adviceNoWifiBody = "확인하려는 네트워크에 연결하세요. 현재 연결을 사용하여 인터넷 테스트 및 네트워크 도구를 계속 실행할 수 있습니다."
    override val adviceNoInternetTitle = "먼저 인터넷 연결을 확인하세요."
    override val adviceNoInternetBody = "Android님은 이 Wi-Fi 연결에서 인터넷 액세스를 확인하지 않았습니다. 로그인 페이지를 확인한 다음 라우터 및 인터넷 호스트에 대한 연결을 테스트하십시오."
    override val adviceNoSignalTitle = "Wi-Fi 신호를 읽어보세요"
    override val adviceNoSignalBody = "네트워크에서 Wi-Fi 스캔을 허용하여 신호 측정값을 확인합니다. 문제가 발견된 방의 신호를 비교하십시오."
    override val adviceWeakSignalTitle = "라우터 근처의 서비스 범위를 비교하세요"
    override val adviceWeakSignalBody = "현재 Wi-Fi 신호가 약합니다. 여기서 라우터 검사를 실행한 다음 라우터 근처에서 반복하십시오. 자신의 네트워크에 대한 설문 조사를 사용하여 약점을 찾으십시오."
    override val adviceGoodSignalTitle = "라우터 응답성 비교"
    override val adviceGoodSignalBody = "신호 강도는 사용할 수 있는 것으로 보이며 Android는 인터넷 액세스를 보고합니다. 연결이 느리게 느껴지면 여기와 라우터 근처의 라우터 응답을 비교하십시오. 강한 신호만으로는 성능을 보장하지 않습니다."
}
