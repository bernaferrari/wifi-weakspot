package com.bernaferrari.wifiweakspot.core.i18n.ko

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object KoToolsStrings : ToolsStrings {
    override val tryAgain = "다시 시도해 보세요"

    override val targetPlaceholder = "웹사이트 또는 IP 주소"
    override val clearTarget = "클리어"
    override val checkSectionTitle = "확인"
    override val recentSectionTitle = "최근"

    override val toolPortsDescription = "열려 있는 내용"

    override val pingQuestion = "답변이 되나요?"
    override val dnsQuestion = "주소가 어떻게 되나요?"
    override val dnsDetail = "DNS 조회"
    override val portsQuestion = "무엇을 제공하나요?"

    override val runningPortScanTitle = "포트 확인 중"

    override val diagnosticFailed = "진단 실패"
    override val invalidTarget = "URL 경로나 공백 없이 호스트 이름이나 IP 주소를 입력하세요."
    override val diagnosticCouldNotComplete = "진단을 완료할 수 없습니다."
    override val connectionChangedRunAgain = "연결이 변경되었습니다. 새 네트워크에서 이 검사를 다시 실행하세요."

    override val openItsWebPage = "웹페이지 열기"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ${total}회 중 ${answered}회 응답"

    override val slowest = "가장 느림"
    override fun repliesContentDescription(replies: String) = "답글: ${replies}"

    override val pingDidntAnswer = "답변이 없습니다"
    override val pingResponsive = "반응형"
    override val pingResponsiveButUneven = "반응이 좋지만 고르지 않음"
    override val pingDroppingReplies = "답글 삭제 중"
    override val pingSlowToAnswer = "답변이 느림"

    override val pingNoAnswerLocal = "꺼져 있거나, 잠자기 상태이거나, 핑을 무시하도록 설정되어 있을 수 있습니다."
    override fun pingRepliesLost(lost: Int, total: Int) = "${total}회 중 ${lost}회 응답이 없었습니다. 공유기 확인 결과와 인터넷 호스트 테스트를 비교해 문제 범위를 좁혀 보세요. 이 테스트만으로는 원인을 확인할 수 없습니다."

    override val pingUneven = "응답 시간은 다양했습니다. 여기와 라우터 근처의 라우터 점검을 비교한 다음 인터넷 호스트를 테스트하십시오."
    override val pingLocalExplanation = "이제 이 장치에 접근했다가 돌아올 시간입니다. 장치 자체가 결과에 영향을 미칠 수 있습니다."
    override val pingInternetExplanation = "이제 이 호스트에 도달하고 돌아올 시간입니다. 귀하의 연결, 경로 및 호스트가 모두 결과에 영향을 미칩니다."

    override fun dnsAddressUnit(count: Int) = "개 주소"

    override fun dnsForQuery(query: String) = "${query}의 경우"
    override val dnsNotFound = "찾을 수 없습니다"
    override val dnsFound = "찾았어요"
    override val dnsNotFoundExplanation = "철자를 확인하거나 다시 시도하세요. 이름이 존재하지 않을 수 있습니다."
    override fun dnsFoundExplanation(query: String) = "귀하의 기기는 ${query}을 찾을 수 있습니다."
    override val dnsAddressesTitle = "주소"

    override val portsOpenUnit = "열려 있음"
    override fun portsCommonServicesOn(host: String) = "${host} 공통 서비스"
    override val portsNothingOpen = "열려 있는 항목이 없습니다."
    override fun portsOffersServices(count: Int) = if (count == 1) "1개의 서비스를 제공합니다" else "${count} 서비스를 제공합니다"
    override val portsNoneAnswered = "공통 서비스에 대한 답변이 없습니다."
    override val portsOffersExplanation = "설정 페이지나 파일 공유 같은 것."

    override val portWebPage = "웹페이지"
    override val portSecureWebPage = "보안 웹페이지"
    override val portRemoteLoginSsh = "원격 로그인 (SSH)"
    override val portOldRemoteLoginTelnet = "기존 원격 로그인(Telnet)"
    override val portFileTransferFtp = "파일 전송(FTP)"
    override val portAddressLookupsDns = "주소 조회(DNS)"
    override val portFileSharing = "파일 공유"
    override val portMacFileSharing = "Mac 파일 공유"
    override val portPrinting = "인쇄 중"
    override val portRemoteDesktop = "원격 데스크톱"
    override val portScreenSharing = "화면 공유"
    override val portDeviceDiscoveryUpnp = "장치 검색(UPnP)"
    override val portStorageOrAdminPage = "저장소 또는 관리 페이지"
    override val portDatabase = "데이터베이스"
    override val portService = "서비스"
    override val devicesLooking = "기기를 찾는 중…"
    override val devicesFindOnWifi = "Wi-Fi에서 기기를 찾으세요."
    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${count} ${ssid}에"
    override fun devicesOnYourNetwork(count: Int) = "${count}"
    override val couldntLookForDevices = "기기를 찾을 수 없습니다."
    override val scanFailed = "스캔 실패"
    override val connectionChangedLookAgain = "연결이 변경되었습니다. 이 네트워크를 다시 살펴보세요."
    override val stoppedLooking = "찾는 걸 멈췄어요"
    override val noDevicesFound = "기기를 찾을 수 없습니다."
    override val noDevicesFoundSubtitle =
        "일부 장치는 요청 시 조용하게 유지됩니다. 잠시 후에 다시 시도하거나 이 기기가 집에 있는지 확인하세요 Wi-Fi."
    override fun otherDevicesGroup(count: Int) = "기타 기기 · ${count}"
    override fun quietDevices(count: Int) = if (count == 1) "조용한 장치 1개" else "${count} 조용한 장치"
    override val quietDevicesSubtitle = "그들은 자신이 무엇인지 말하지 않았습니다."
    override val hide = "숨기기"
    override val show = "쇼"
    override fun checkingLocalAddresses(percent: Int) = "로컬 주소 확인 중 · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · 답변하지 않음"

    override val deviceNotScannedYet = "아직 스캔에서 찾을 수 없습니다."
    override val deviceAnsweredLastScan = "마지막 스캔에 답변함"
    override val deviceSeenButNoAnswer = "네트워크에서 보았지만 응답하지 않았습니다."
    override val actionCheckPorts = "포트 확인"
    override val openRouterSettings = "라우터 설정 열기"
    override val detailsTitle = "세부정보"
    override val factNetworkName = "네트워크 이름"
    override val factHardwareAddress = "하드웨어 주소"
    override val macHiddenByAndroid = "Android에 의해 숨겨짐"
    override val factYourName = "당신의 이름은"
    override val portsNotCheckedYet = "아직 확인되지 않았습니다."
    override fun portsNoneOpenWhenChecked(moment: String) =
        "확인 시 ${moment.replaceFirstChar { it.lowercase() }} 공통 포트가 열려 있지 않았습니다."
    override fun portsCheckedAt(moment: String) = "확인됨 ${moment.replaceFirstChar { it.lowercase() }}."
    override val renameDeviceTitle = "기기 이름 바꾸기"
    override val renameSupporting = "발견된 이름 WiFi Weakspot을 사용하려면 비워 두세요."
    override val connectionChangedOpenAgain = "연결이 변경되었습니다. 새 네트워크에서 이 장치를 다시 엽니다."
    override val couldntLoadDetails = "저장된 세부정보를 로드할 수 없습니다. 이 장치를 다시 열어보세요."
    override val scanBeforeNaming = "이름을 저장하기 전에 이 장치를 스캔하세요."

}
