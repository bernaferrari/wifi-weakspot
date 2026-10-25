package com.bernaferrari.wifiweakspot.core.i18n.ko

import com.bernaferrari.wifiweakspot.core.i18n.CommonStrings

object KoCommonStrings : CommonStrings {
    override fun deleteItemTitle(name: String) = "삭제 ${name}?"

    override val couldNotSaveChanges = "변경사항을 저장하지 못했습니다. 다시 시도하세요."
    override val couldNotDeleteItem = "이 항목을 삭제하지 못했습니다. 다시 시도하세요."

    override val hiddenNetworkShort = "숨김"
    override val network = "네트워크"
    override val tools = "도구"
    override val maker = "메이커"
    override val noReply = "응답 없음"

    override val fastest = "가장 빠름"
    override val address = "주소"

    override val responseTime = "응답 시간"
    override val checkAgain = "다시 측정"

    override val rename = "이름 바꾸기"
    override val stop = "중지"
    override val unknown = "알 수 없음"

    override val hiddenNetwork = "숨겨진 네트워크"
    override val fasterScans = "더 빠른 스캔"
    override val openWifiSettings = "Wi-Fi 설정 열기"
    override val signalUnavailable = "신호를 확인할 수 없음"
    override val devices = "기기"
    override val openPorts = "열린 포트"
    override val nameLabel = "이름"
    override val historyTitle = "기록"
    override val routerCheck = "공유기 확인"
    override val testWebsite = "웹사이트 테스트"
    override val yourRouter = "내 공유기"
    override val yourNetwork = "내 네트워크"

    override val openInNewTab = "새 탭에서 열기"
    override val mapSharingAndroidOnly = "지도 공유는 Android 앱에서 사용할 수 있습니다."

    override val back = "뒤로"
    override val close = "닫기"
    override val cancel = "취소"
    override val save = "저장"
    override val done = "완료"
    override val share = "공유"
    override val delete = "삭제"

    override val tabHome = "홈"
    override val tabNetworks = "네트워크"
    override val tabRooms = "방"
    override val tabSettings = "설정"

    override val permissionScreenTitle = "주변 Wi-Fi 접근 허용"

    override val permissionAllowAccess = "접근 허용"

    override val openAppSettings = "앱 설정 열기"

    override fun signalBadgeDescription(quality: String, reading: String) = "${quality} 신호, ${reading}"

    override val scanReady = "준비됨"
    override fun scansCount(count: Int) = "스캔 ${count}회"

    override val scansLeftCaption = "남음"
    override val scanWaitingCaption = "대기 중"

    override val scanDevMode = "개발 모드"
    override val readyToScan = "스캔 준비 완료"
    override fun scansRemaining(count: Int) = "스캔 ${count}회 남음"

    override fun nextScanIn(time: String) = "다음 스캔까지 ${time}"

    override val checking = "확인 중…"

    override val justNow = "지금 막"
    override fun minutesAgo(minutes: Long) = "${minutes}분 전"

    override fun hoursAgo(hours: Long) = "${hours}시간 전"

    override fun daysAgo(days: Long) = "${days}일 전"

}
