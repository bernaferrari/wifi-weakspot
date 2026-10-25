package com.bernaferrari.wifiweakspot.core.i18n.ko

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object KoToolsStrings : ToolsStrings {
    override val tryAgain = "다시 시도"

    override val targetPlaceholder = "웹사이트 또는 IP 주소"
    override val clearTarget = "클리어"
    override val checkSectionTitle = "테스트"

    override val recentSectionTitle = "최근 테스트"

    override val toolPortsDescription = "열린 포트 확인"

    override val pingQuestion = "응답하나요?"

    override val dnsQuestion = "IP 주소는 무엇인가요?"

    override val dnsDetail = "DNS 조회"
    override val portsQuestion = "어떤 서비스를 제공하나요?"

    override val runningPortScanTitle = "포트 확인 중"

    override val diagnosticFailed = "진단 실패"
    override val invalidTarget = "URL 경로나 공백 없이 호스트 이름 또는 IP 주소를 입력하세요."

    override val diagnosticCouldNotComplete = "진단을 완료할 수 없습니다."
    override val connectionChangedRunAgain = "연결된 네트워크가 변경되었습니다. 새 네트워크에서 다시 테스트하세요."

    override val openItsWebPage = "웹페이지 열기"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ${total}회 중 ${answered}회 응답"

    override val slowest = "가장 느림"
    override fun repliesContentDescription(replies: String) = "답글: ${replies}"

    override val pingDidntAnswer = "응답 없음"

    override val pingResponsive = "응답 양호"

    override val pingResponsiveButUneven = "응답 시간이 일정하지 않음"

    override val pingDroppingReplies = "일부 요청에 응답 없음"

    override val pingSlowToAnswer = "응답이 느림"

    override val pingNoAnswerLocal = "전원이 꺼져 있거나 절전 상태이거나 Ping에 응답하지 않도록 설정되어 있을 수 있습니다."

    override fun pingRepliesLost(lost: Int, total: Int) = "${total}회 중 ${lost}회 응답이 없었습니다. 공유기 확인 결과와 인터넷 호스트 테스트를 비교해 문제 범위를 좁혀 보세요. 이 테스트만으로는 원인을 확인할 수 없습니다."

    override val pingUneven = "응답 시간이 일정하지 않았습니다. 현재 위치와 공유기 근처에서 결과를 비교한 뒤 인터넷 호스트도 테스트하세요."

    override val pingLocalExplanation = "이 기기까지 왕복하는 데 걸리는 시간입니다. 대상 기기의 상태도 결과에 영향을 줍니다."

    override val pingInternetExplanation = "이 호스트까지 왕복하는 데 걸리는 시간입니다. 연결 상태, 통신 경로, 호스트 상태가 모두 결과에 영향을 줍니다."

    override fun dnsAddressUnit(count: Int) = "개 주소"

    override fun dnsForQuery(query: String) = "${query} 이름 조회"

    override val dnsNotFound = "주소를 찾지 못함"

    override val dnsFound = "주소를 찾음"

    override val dnsNotFoundExplanation = "입력한 이름을 확인하고 다시 시도하세요. 등록되지 않은 이름일 수도 있습니다."

    override fun dnsFoundExplanation(query: String) = "이 기기에서 ${query}의 주소를 확인했습니다."

    override val dnsAddressesTitle = "IP 주소"

    override val portsOpenUnit = "개 열림"

    override fun portsCommonServicesOn(host: String) = "${host} 공통 서비스"
    override val portsNothingOpen = "열린 포트 없음"

    override fun portsOffersServices(count: Int) = "서비스 ${count}개를 제공합니다"

    override val portsNoneAnswered = "일반적인 서비스에서 응답을 받지 못했습니다."

    override val portsOffersExplanation = "설정 페이지나 파일 공유 같은 서비스입니다."

    override val portWebPage = "웹페이지"
    override val portSecureWebPage = "보안 웹페이지"
    override val portRemoteLoginSsh = "원격 로그인 (SSH)"
    override val portOldRemoteLoginTelnet = "기존 원격 로그인(Telnet)"
    override val portFileTransferFtp = "파일 전송(FTP)"
    override val portAddressLookupsDns = "이름 조회 (DNS)"

    override val portFileSharing = "파일 공유"
    override val portMacFileSharing = "Mac 파일 공유"
    override val portPrinting = "인쇄 중"
    override val portRemoteDesktop = "원격 데스크톱"
    override val portScreenSharing = "화면 공유"
    override val portDeviceDiscoveryUpnp = "장치 검색(UPnP)"
    override val portStorageOrAdminPage = "저장소 또는 관리 페이지"
    override val portDatabase = "데이터베이스"
    override val portService = "서비스"
    override val devicesLooking = "기기 검색 중…"

    override val devicesFindOnWifi = "Wi-Fi에 연결된 기기 찾기"

    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${ssid}에서 ${count}개 발견"

    override fun devicesOnYourNetwork(count: Int) = "내 네트워크에서 ${count}개 발견"

    override val couldntLookForDevices = "기기를 검색하지 못했습니다"

    override val scanFailed = "스캔 실패"
    override val connectionChangedLookAgain = "연결된 네트워크가 변경되었습니다. 이 네트워크에서 다시 검색하세요."

    override val stoppedLooking = "검색 중지됨"

    override val noDevicesFound = "기기를 찾을 수 없습니다."
    override val noDevicesFoundSubtitle = "스캔에 응답하지 않는 기기도 있습니다. 잠시 후 다시 시도하거나 이 기기가 집 Wi-Fi에 연결되어 있는지 확인하세요."

    override fun otherDevicesGroup(count: Int) = "기타 기기 · ${count}"
    override fun quietDevices(count: Int) = "종류를 알 수 없는 기기 ${count}개"

    override val quietDevicesSubtitle = "기기 종류를 확인할 수 없습니다"

    override val hide = "접기"

    override val show = "펼치기"

    override fun checkingLocalAddresses(percent: Int) = "로컬 주소 확인 중 · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · 답변하지 않음"

    override val deviceNotScannedYet = "아직 스캔에서 발견되지 않음"

    override val deviceAnsweredLastScan = "지난 스캔에서 응답함"

    override val deviceSeenButNoAnswer = "네트워크에서 발견되었지만 응답하지 않음"

    override val actionCheckPorts = "포트 확인"

    override val openRouterSettings = "라우터 설정 열기"
    override val detailsTitle = "세부정보"
    override val factNetworkName = "네트워크 이름"
    override val factHardwareAddress = "하드웨어 주소"
    override val macHiddenByAndroid = "Android에서 숨김 처리됨"

    override val factYourName = "직접 지정한 이름"

    override val portsNotCheckedYet = "아직 확인하지 않음"

    override fun portsNoneOpenWhenChecked(moment: String) = "${moment}에 확인했을 때 일반적으로 사용하는 포트는 열려 있지 않았습니다."

    override fun portsCheckedAt(moment: String) = "확인 시점: ${moment}"

    override val renameDeviceTitle = "기기 이름 바꾸기"
    override val renameSupporting = "비워 두면 WiFi Weakspot이 찾은 이름을 사용합니다."

    override val connectionChangedOpenAgain = "연결된 네트워크가 변경되었습니다. 새 네트워크에서 이 기기 상세 정보를 다시 여세요."

    override val couldntLoadDetails = "저장된 세부정보를 로드할 수 없습니다. 이 장치를 다시 열어보세요."
    override val scanBeforeNaming = "이름을 저장하기 전에 이 장치를 스캔하세요."

}
