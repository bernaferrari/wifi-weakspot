package com.bernaferrari.wifiweakspot.core.i18n.ko

import com.bernaferrari.wifiweakspot.core.i18n.HistoryStrings

object KoHistoryStrings : HistoryStrings {
    override val today = "오늘"
    override val yesterday = "어제"
    override val weekdayNames = listOf("월요일", "화요일", "수요일", "목요일", "금요일", "토요일", "일요일")
    override val weekdayShortNames = listOf("월", "화", "수", "목", "금", "토", "일")
    override val monthShortNames = listOf("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")
    override fun dateThisYear(weekday: String, day: Int, month: String) = "${month} ${day}일 (${weekday})"

    override fun dateOtherYear(weekday: String, day: Int, month: String, year: Int) = "${year}년 ${month} ${day}일 (${weekday})"

    override fun momentAt(day: String, time: String) = "${day} ${time}"

    override val filterAll = "전체"
    override val kindInternetTests = "인터넷 테스트"
    override val kindScans = "스캔"
    override val emptyTitle = "아직 저장된 것이 없습니다."
    override val emptySubtitle = "라우터 확인, 네트워크 스캔 및 도구 결과가 여기에 표시되므로 시간이 지남에 따라 Wi-Fi이 어떻게 변하는지 확인할 수 있습니다."
    override fun trendBarsDescription(count: Int, readings: String) = "마지막 ${count} 라우터 테스트: ${readings}"
    override val trendNoReply = "라우터에서 응답이 없습니다. 일부 라우터는 이러한 검사를 무시합니다."
    override val trendRecheckToCompare = "변경 후 다시 확인하여 비교해 보세요."
    override fun trendQuicker(usualMs: String) = "평소보다 빠릅니다(${usualMs}ms)"
    override fun trendSlower(usualMs: String) = "평소보다 느림(${usualMs}ms)"
    override fun trendAboutSame(usualMs: String) = "평소와 거의 같습니다(${usualMs} ms)"

    override fun internetTitle(mbps: String) = "${mbps} Mbps 다운로드"
    override fun routerTitle(ms: String) = "${ms}ms 라우터까지"
    override fun networksNearby(count: Int) = if (count == 1) "근처 네트워크 1개" else "${count} 주변 네트워크"
    override fun strongestNetwork(network: String) = "최강: ${network}"
    override fun accessPoints(count: Int) = if (count == 1) "액세스 포인트 1개" else "${count} 액세스 포인트"
    override fun devicesFound(count: Int) = if (count == 1) "기기 1개 발견됨" else "${count} 기기 발견됨"
    override fun listWithMore(list: String, more: Int) = if (more > 0) "${list} 외 ${more}" else list
    override fun pingSummary(averageMs: String, replied: Int, sent: Int) = "${averageMs} ms · ${sent}회 중 ${replied}회 응답"

    override fun lookupTitle(target: String) = "찾아보세요 ${target}"
    override val noRecords = "기록 없음"
    override val lookupFailed = "조회 실패"
    override fun openPortsTitle(target: String) = "${target}에서 포트 열기"
    override val noCommonPortsOpen = "공용 포트가 하나도 열려 있지 않았습니다."
    override val completed = "완료"
    override val failed = "실패"

    override val metricDownload = "다운로드"
    override val metricUpload = "업로드"
    override val metricLatency = "지연 시간"
    override val metricVariation = "변형"
    override val metricLost = "분실"
    override fun metricOnBand(band: String) = "${band}"
    override val metricReplied = "답변됨"
    override val metricReplies = "답글"
    override val metricAverage = "평균"
    override val factServer = "서버"
    override val factRoom = "방"
    override val factBand = "밴드"
    override val factSignal = "시그널"
    override val factAccessPoints = "액세스 포인트"
    override fun signalFact(quality: String, rssi: Int) = "${quality} 신호 · ${rssi} dBm"

    override val linesNetworksStrongestFirst = "네트워크, 가장 강한 것부터"
    override val linesRecords = "기록"
    override fun replyNumber(index: Int) = "답글 ${index}"
    override val scanNote = "귀하와 동일한 채널에 있는 인근 네트워크가 방송 시간을 놓고 경쟁합니다."

    override val go = "바로가기"
    override val testInternetSpeedInstead = "대신 인터넷 속도를 테스트해 보세요."
    override val keepDeviceStill = "몇 초 동안 기기를 가만히 두세요."
    override val connectToStart = "시작하려면 Wi-Fi에 연결하세요."
    override val lastTime = "지난번"
    override val couldNotSaveSpot = "이 장소를 저장할 수 없습니다. 다시 시도해 보세요."
    override val connectFirst = "먼저 Wi-Fi에 연결하세요. 이 검사는 라우터의 응답을 측정합니다."
    override val historyNotSaved = "결과가 측정되었으나 기록을 저장할 수 없습니다."
    override val checkStopped = "연결이 변경되었거나 라우터 확인이 중지되었습니다. Wi-Fi 연결을 유지하고 다시 시도하세요. 일부 라우터는 핑을 차단합니다."

    override val internetSpeedTitle = "인터넷 속도 · fast.com"
}
