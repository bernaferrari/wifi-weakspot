package com.bernaferrari.wifiweakspot.core.i18n.ko

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object KoSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "측정 중입니다. 기기를 움직이지 마세요"

    override val tapWhereYouAreStanding = "평면도에서 현재 위치를 누르세요"

    override val chooseWifi = "Wi-Fi 선택"
    override val undoLastReading = "마지막 측정 취소"

    override val allReadings = "전체 읽기"
    override val floorPlanTitle = "평면도"
    override fun readingCount(count: Int) = "측정값 ${count}개"

    override val mapOptions = "지도 옵션"
    override val usePlanPhoto = "평면도 사진 사용"

    override val changePhoto = "사진 변경"

    override val removePhoto = "사진 삭제"

    override val exportReadingsCsv = "측정값 CSV로 내보내기"

    override fun floorName(floor: Int) = if (floor == 0) "지하" else "${floor}층"

    override fun floorShortLabel(floor: Int) = if (floor == 0) "ㄴ" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "층 ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "지하층 추가" else "${floor}층 추가"

    override val deleteFloorReadingsAction = "이 층의 측정값 삭제"

    override fun deleteFloorBody(count: Int) = "이 층의 측정값 ${count}개가 삭제됩니다."

    override val oneRouterAtATime = "지도 하나에서는 액세스 포인트 하나를 측정합니다. 메시 Wi-Fi는 각 기기가 따로 표시됩니다."

    override val noNetworksFound = "네트워크를 찾지 못했습니다. Wi-Fi와 위치 기능이 켜져 있는지 확인하세요."

    override val scanning = "스캔 중…"
    override val refresh = "새로고침"

    override val switchWifiTitle = "다른 Wi-Fi로 전환하시겠습니까?"
    override val switchWifiBody = "이 지도의 모든 층에서 측정값이 삭제됩니다. 기존 측정값도 보관하려면 새 지도를 만드세요."

    override val clearAndSwitch = "지우고 전환하기"

    override val deleteMapBody = "모든 층의 측정값이 모두 삭제됩니다."

    override val noReadingsYet = "아직 측정값이 없습니다. 평면도에서 현재 위치를 누르세요."

    override val weakestFirst = "신호가 약한 순서로 표시하여 문제가 있는 곳을 먼저 볼 수 있습니다."

    override val mapYourHome = "집 지도를 그려보세요"
    override val mapYourHomeBody = "집 평면도에 Wi-Fi 신호를 표시해 보세요. 평면도 사진이나 빈 격자를 사용할 수 있습니다."

    override val createMap = "지도 만들기"

    override val namePlaceholder = "위층, 서재 등"

    override val floorplanDescription = "측정용 평면도"

    override fun coverageMapDescription(count: Int) = "측정값 ${count}개가 있는 Wi-Fi 커버리지 지도입니다. 현재 위치를 눌러 측정하세요."

    override val networkNotSeenHere = "이 위치에서 선택한 Wi-Fi를 찾지 못했습니다. 측정값은 저장되지 않았습니다."

    override val projectChangedWhileScanning = "스캔 중 지도가 변경되어 측정값을 저장하지 않았습니다."

    override val couldNotSaveReading = "측정값을 저장하지 못했습니다. 기존 측정값은 그대로 유지됩니다."

    override fun defaultMapName(index: Int) = if (index == 1) "우리 집" else "우리 집 ${index}"

    override val stillSavingMap = "지도를 저장하는 중입니다. 잠시 기다려 주세요."

    override val noFreshScan = "Android에서 새 스캔 결과를 받지 못했습니다. 잠시 후 다시 시도하세요."

    override val couldNotScan = "스캔하지 못했습니다. Wi-Fi와 위치 설정을 확인하세요."

    override val chooseAccessPointFirst = "커버리지를 측정할 액세스 포인트를 먼저 선택하세요."

    override val saveMeasurementBeforeExport = "내보내기 전에 측정값을 하나 이상 저장하세요."
    override val couldNotExportProject = "지도를 내보내지 못했습니다. 다시 시도하세요."

    override val connectToYourWifi = "Wi-Fi에 연결하세요"
    override fun checkingRoom(room: String) = "${room} 측정 중… 기기를 움직이지 마세요"

    override val connectedWifi = "연결된 Wi-Fi"
    override val connectedNoReading = "Wi-Fi에 연결되어 있지만 아직 신호 측정값을 확인할 수 없습니다."

    override val connectToCheck = "확인할 Wi-Fi에 연결하세요."

    override fun removedRoom(room: String) = "삭제됨 ${room}"
    override val undo = "실행 취소"
    override val roomsEmptyHint = "방 안에서 움직이지 않고 측정값을 저장하세요. 다른 방에서도 측정해 신호가 약해지는 곳을 찾아보세요."

    override val noWifiSignal = "신호 없음 Wi-Fi"
    override val saveRoom = "방 측정값 저장"

    override fun dbStronger(delta: Int) = "${delta} dB 강해짐"

    override fun dbWeaker(delta: Int) = "${delta} dB 약해짐"

    override val savedRooms = "측정한 방"

    override fun deleteRoom(room: String) = "삭제 ${room}"
    override fun roomDescription(room: String, quality: String, reading: String) = "${room}, 신호: ${quality}, ${reading}. 측정값을 확인하고 이 장소를 다시 측정할 수 있습니다."

    override val noSignalToSave = "저장할 Wi-Fi 신호가 없습니다. 먼저 Wi-Fi에 연결하세요."

    override val allowScansToLink = "네트워크 화면에서 Wi-Fi 스캔을 허용하면 측정값을 해당 네트워크에 연결할 수 있습니다."

    override val connectToSpotNetwork = "다시 측정하려면 이 장소를 측정할 때 사용한 네트워크에 연결하세요."

    override fun savedRoom(room: String) = "${room} 저장됨"

    override val couldNotSaveStableReading = "안정적인 측정값을 저장하지 못했습니다. 같은 Wi-Fi에 연결한 채 기기를 움직이지 않고 다시 시도하세요."

    override val whereAreYou = "어디에 있나요?"
    override val somewhereElse = "다른 곳"

    override fun savedAt(time: String) = "${time}에 저장됨"

    override val moveRouterTip = "공유기를 더 높이, 중앙에 위치시키거나 이 방에 더 가까이 옮겨보세요."
    override val betterThanLastTime = "지난번보다 나아짐"

    override val worseThanLastTime = "지난번보다 나빠짐"

    override val sameAsLastTime = "지난번과 동일"
    override val earlierChecks = "이전 측정"

    override val remove = "삭제"
    override fun connectToRecheck(ssid: String?) =
        "${ssid?.ifBlank { null } ?: "똑같습니다 Wi-Fi"}에 접속하여 다시 확인해 보세요."

    override val stepDeveloperOptionsTitle = "개발자 옵션을 켜세요"
    override val stepDeveloperOptionsBody = "설정에서 **휴대전화 정보**를 열고 **빌드 번호**를 7번 누르세요. 화면 잠금 인증이 필요할 수 있습니다."

    override val openAboutPhone = "휴대전화 정보 열기"

    override val stepThrottlingTitle = "Wi-Fi 스캔 횟수 제한 해제"

    override val stepThrottlingBody = "**개발자 옵션**의 **네트워크**에서 **Wi‑Fi 검색 제한**을 끄세요. 항목 이름은 기기마다 다를 수 있습니다."

    override val openDeveloperOptions = "개발자 옵션 열기"
    override val stepComeBackTitle = "앱으로 돌아오기"

    override val stepComeBackBody = "앱으로 돌아오면 WiFi Weakspot이 설정을 자동으로 확인합니다. 언제든지 스캔 제한을 다시 켤 수 있습니다."

    override val hideComputerMethod = "컴퓨터로 설정하는 방법 접기"

    override val useAdbInstead = "컴퓨터에서 ADB로 설정"

    override val adbCommandClipLabel = "ADB 명령"
    override val hideRootOption = "root 사용 방법 접기"

    override val useRootInstead = "root 권한으로 설정"

    override val scanLimitFootnote = "Android는 배터리를 절약하기 위해 스캔 횟수를 제한합니다. 언제든지 제한을 다시 켤 수 있습니다."

    override val unlimitedWithRootTitle = "root로 스캔 횟수 제한 해제"

    override val unlimitedOnTitle = "스캔 횟수 제한이 해제됨"

    override val unlimitedOnBody = "주변 네트워크와 채널 그래프가 몇 초마다 갱신됩니다."

    override val scansLimitedTitle = "스캔 횟수가 제한됨"

    override val scansLimitedAndroid9Body = "Android 9에서는 2분마다 4회만 스캔할 수 있으며 이 제한을 해제할 수 없습니다. WiFi Weakspot은 대기 중에 최근 결과를 다시 표시합니다."

    override val usingRoot = "root 사용 중"

    override val deviceRooted = "root 권한을 사용할 수 있습니다"

    override val rootScanningBody = "시스템 셸에서 스캔하므로 개발자 옵션을 변경할 필요가 없습니다."

    override val rootDeniedBody = "root 권한이 거부되었습니다. root 관리 앱에서 WiFi Weakspot을 허용하고 다시 시도하거나 아래의 개발자 옵션 방법을 사용하세요."

    override val rootNotNeededBody = "이미 스캔 횟수 제한이 해제되어 있습니다. root 권한은 필요하지 않습니다."

    override val rootOfferBody = "root 권한으로 스캔하면 개발자 옵션을 변경하지 않아도 됩니다. root 관리 앱에서 한 번 권한을 요청합니다."

    override val stopUsingRoot = "root 사용 중지"

    override val waitingForRootManager = "root 관리 앱의 응답을 기다리는 중…"

    override val tryRootAgain = "root 권한으로 다시 시도"

    override val useRoot = "root 사용"

    override val copied = "복사됨"
    override val copyCommand = "명령 복사"

}
