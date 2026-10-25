package com.bernaferrari.wifiweakspot.core.i18n.ko

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object KoSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "측정… 가만히 있어"
    override val tapWhereYouAreStanding = "당신이 서있는 지도를 탭하세요"
    override val chooseWifi = "Wi-Fi 선택"
    override val undoLastReading = "마지막 읽기 취소"
    override val allReadings = "전체 읽기"
    override val floorPlanTitle = "평면도"
    override fun readingCount(count: Int) = "${count} 개 측정값"

    override val mapOptions = "지도 옵션"
    override val usePlanPhoto = "계획 사진을 사용하세요"
    override val changePhoto = "사진 변경"
    override val removePhoto = "사진 삭제"
    override val exportReadingsCsv = "판독값 내보내기(CSV)"
    override fun floorName(floor: Int) = if (floor == 0) "지하" else "층 ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "ㄴ" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "층 ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "지하실 추가" else "층 추가 ${floor}"
    override val deleteFloorReadingsAction = "이 층의 수치 삭제"
    override fun deleteFloorBody(count: Int) =
        if (count == 1) "1개의 읽기가 삭제됩니다." else "${count} 판독값이 삭제됩니다."

    override val oneRouterAtATime = "지도는 한 번에 하나의 라우터를 따릅니다. 메시 시스템은 각 유닛을 개별적으로 표시합니다."
    override val noNetworksFound = "아직 네트워크를 찾을 수 없습니다. Wi-Fi 및 위치가 켜져 있는지 확인하세요."
    override val scanning = "스캔 중…"
    override val refresh = "새로고침"
    override val switchWifiTitle = "다른 Wi-Fi로 전환하시겠습니까?"
    override val switchWifiBody = "이 맵의 모든 층에 대한 판독값이 삭제됩니다. 두 가지를 모두 유지하려면 새 지도를 만드세요."
    override val clearAndSwitch = "지우고 전환하기"

    override val deleteMapBody = "모든 층의 측정값이 모두 삭제됩니다."

    override val noReadingsYet = "여기에는 아직 판독값이 없습니다. 당신이 서있는 지도를 탭하세요."
    override val weakestFirst = "가장 약한 것이 먼저이므로 문제가 있는 부분이 위에 있습니다."
    override val mapYourHome = "집 지도를 그려보세요"
    override val mapYourHomeBody = "집 지도에서 Wi-Fi을 확인하세요. 계획 사진이나 빈 그리드를 사용하세요."
    override val createMap = "지도 만들기"

    override val namePlaceholder = "위층 사무실…"

    override val floorplanDescription = "프로젝트 평면도"
    override fun coverageMapDescription(count: Int) =
        "${readingCount(count)}이 포함된 적용 범위 지도. 측정할 위치를 탭하세요."

    override val networkNotSeenHere = "그 Wi-Fi은 여기에 나타나지 않았습니다. 아무것도 저장되지 않았습니다."
    override val projectChangedWhileScanning = "스캔하는 동안 프로젝트가 변경되었습니다. 저장된 포인트가 없습니다."
    override val couldNotSaveReading = "읽은 내용을 저장할 수 없습니다. 다른 판독값은 안전합니다."
    override fun defaultMapName(index: Int) = if (index == 1) "홈" else "홈 ${index}"
    override val stillSavingMap = "잠시만 기다려 주세요. 아직 지도를 저장하고 있습니다."
    override val noFreshScan = "Android이 새로운 스캔을 제공하지 않았습니다. 잠시 후 다시 시도하세요."
    override val couldNotScan = "스캔할 수 없습니다. Wi-Fi 및 위치 설정을 확인하세요."
    override val chooseAccessPointFirst = "범위를 매핑하기 전에 액세스 포인트를 선택하세요."
    override val saveMeasurementBeforeExport = "내보내기 전에 측정값을 하나 이상 저장하세요."
    override val couldNotExportProject = "이 프로젝트를 내보낼 수 없습니다. 다시 시도해 보세요."

    override val connectToYourWifi = "Wi-Fi에 연결하세요"
    override fun checkingRoom(room: String) = "확인 중 ${room}… 가만히 계세요"
    override val connectedWifi = "연결된 Wi-Fi"
    override val connectedNoReading = "기기가 연결되었지만 아직 신호 판독을 사용할 수 없습니다."
    override val connectToCheck = "확인하고 싶은 Wi-Fi에 접속하세요."
    override fun removedRoom(room: String) = "삭제됨 ${room}"
    override val undo = "실행 취소"
    override val roomsEmptyHint =
        "방에 서서 읽은 내용을 저장하세요. 신호가 약해지는 곳을 찾으려면 다른 방을 확인하세요."
    override val noWifiSignal = "신호 없음 Wi-Fi"
    override val saveRoom = "방 저장"
    override fun dbStronger(delta: Int) = "${delta}dB 더 강함"
    override fun dbWeaker(delta: Int) = "${delta}dB 약함"
    override val savedRooms = "저장된 방"
    override fun deleteRoom(room: String) = "삭제 ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) =
        "${room}, ${quality} 신호, ${reading}. 판독값을 검토하고 이 지점을 다시 확인하십시오."

    override val noSignalToSave = "저장할 Wi-Fi 신호가 없습니다. 먼저 Wi-Fi에 연결하세요."
    override val allowScansToLink = "이 판독값이 네트워크에 연결될 수 있도록 네트워크에서 Wi-Fi 스캔을 허용합니다."
    override val connectToSpotNetwork = "다시 확인하기 전에 이 스팟의 네트워크에 연결하세요."
    override fun savedRoom(room: String) = "저장됨 ${room}"
    override val couldNotSaveStableReading = "안정적인 측정값을 저장할 수 없습니다. 계속 같은 Wi-Fi을 유지하고 다시 시도하세요."
    override val whereAreYou = "어디에 있나요?"
    override val somewhereElse = "다른 곳"

    override fun savedAt(time: String) = "저장됨 ${time}"
    override val moveRouterTip = "라우터를 더 높이, 중앙에 위치시키거나 이 방에 더 가까이 옮겨보세요."
    override val betterThanLastTime = "지난번보다 나아요"
    override val worseThanLastTime = "지난번보다 더 심해요"
    override val sameAsLastTime = "지난번과 동일"
    override val earlierChecks = "사전 확인"
    override val remove = "삭제"
    override fun connectToRecheck(ssid: String?) =
        "${ssid?.ifBlank { null } ?: "똑같습니다 Wi-Fi"}에 접속하여 다시 확인해 보세요."

    override val stepDeveloperOptionsTitle = "개발자 옵션을 켜세요"
    override val stepDeveloperOptionsBody =
        "설정에서 **휴대전화 정보**를 열고 **빌드 번호**를 7번 탭하세요. 화면 잠금을 입력해야 할 수도 있습니다."
    override val openAboutPhone = "열기 휴대폰 정보"
    override val stepThrottlingTitle = "Wi-Fi 스캔 제한 끄기"
    override val stepThrottlingBody =
        "**개발자 옵션**에서 **네트워킹**까지 아래로 스크롤하고 **Wi-Fi 검사 조절**을 끕니다."
    override val openDeveloperOptions = "개발자 옵션 열기"
    override val stepComeBackTitle = "여기로 돌아와"
    override val stepComeBackBody =
        "WiFi Weakspot 복귀시 자동으로 확인됩니다. 언제든지 제한을 다시 설정할 수 있습니다."
    override val hideComputerMethod = "컴퓨터 숨기기 방법"
    override val useAdbInstead = "컴퓨터를 선호하시나요? ADB 사용"
    override val adbCommandClipLabel = "ADB 명령"
    override val hideRootOption = "숨기기 root 옵션"
    override val useRootInstead = "루팅된 기기인가요? 대신 root을 사용하세요"
    override val scanLimitFootnote = "Android 배터리 절약을 위해 스캔을 제한합니다. 언제든지 한도를 다시 설정할 수 있습니다."
    override val unlimitedWithRootTitle = "root로 무제한 스캔"
    override val unlimitedOnTitle = "무제한 스캔이 켜져 있습니다."
    override val unlimitedOnBody = "주변 네트워크 및 채널 그래프가 몇 초마다 새로 고쳐집니다."
    override val scansLimitedTitle = "스캔이 제한되어 있습니다."
    override val scansLimitedAndroid9Body =
        "Android 9는 2분마다 4번의 스캔을 허용하며 이 기능을 끌 수 없습니다. WiFi Weakspot 중간에 최근 결과를 재사용합니다."
    override val usingRoot = "root 사용"
    override val deviceRooted = "기기가 루팅되었습니다"
    override val rootScanningBody = "스캔은 시스템 셸을 통과하므로 개발자 옵션에서 변경할 것이 없습니다."
    override val rootDeniedBody =
        "루트 액세스가 거부되었습니다. root 관리자에서 WiFi Weakspot을 허용하고 다시 시도하거나 아래 개발자 옵션을 사용하세요."
    override val rootNotNeededBody = "스캔은 이미 무제한입니다. 루트는 필요하지 않습니다."
    override val rootOfferBody =
        "개발자 옵션을 건너뛰고 WiFi Weakspot가 root을 스캔하도록 합니다. root 관리자가 한 번 물어볼 것입니다."
    override val stopUsingRoot = "root 사용 중지"
    override val waitingForRootManager = "root 매니저를 기다리는 중…"
    override val tryRootAgain = "root를 다시 시도해 보세요."
    override val useRoot = "root 사용"
    override val copied = "복사됨"
    override val copyCommand = "복사 명령"
}
