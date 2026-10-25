package com.bernaferrari.wifiweakspot.core.i18n.ko

import com.bernaferrari.wifiweakspot.core.i18n.SettingsStrings

object KoSettingsStrings : SettingsStrings {
    override val sectionScanning = "스캔"

    override val fasterScansThrottled = "Android 2분마다 4번 스캔 가능"
    override val sectionAppearance = "화면"
    override val themeAuto = "자동"
    override val themeLight = "라이트"
    override val themeDark = "다크"
    override val showSignalAs = "신호 표시 방식"
    override val sectionComfort = "사용 편의"

    override val reduceMotionTitle = "동작 줄이기"
    override val hapticsSubtitle = "탭하거나 결과가 나올 때 진동"

    override val sectionAlerts = "알림"
    override val backgroundAlertsTitle = "백그라운드 알림"

    override val backgroundAlertsSubtitle = "주변 Wi-Fi에 변화가 있을 때"

    override val notificationsDenied = "모니터링 상태와 알림을 보려면 알림 권한을 허용하세요."

    override val checkEvery = "확인 간격"

    override fun intervalSeconds(seconds: Int) = "${seconds}초"
    override fun intervalMinutes(minutes: Int) = "${minutes}분"
    override val newNetworkAlertsTitle = "새 네트워크 알림"

    override val signalChangeAlertsTitle = "신호 변화 알림"

    override val signalChangeAlertsSubtitle = "신호 세기가 크게 변할 때"

    override val sectionAbout = "앱 정보"

    override fun aboutApp(appName: String) = "${appName} 소개"
    override fun version(version: String) = "버전 ${version}"
    override val replayIntroTitle = "시작 안내 다시 보기"

    override val wallpaperColors = "배경화면 색상"
    override val colorIndigo = "인디고"
    override val colorOrange = "오렌지"
    override val colorAmber = "호박색"
    override val colorGreen = "녹색"
    override val colorTeal = "청록색"
    override val colorCyan = "청록색"
    override val colorBlue = "파란색"
    override val colorPurple = "보라색"
    override val colorPink = "핑크"

    override val tagline = "집 안의 약한 Wi-Fi 신호 찾기"
    override val openSourceLicense = "오픈 소스 · Apache 2.0"
    override val feedbackTitle = "피드백"
    override val feedbackBody = "아이디어 및 버그 보고서"
    override val elsewhere = "관련 링크"

    override val privacySummary = "직접 공유하지 않는 한 결과는 이 기기에만 보관됩니다."
    override val privacyPolicy = "개인정보 보호정책"

    override val notNow = "나중에"

    override val getStarted = "시작하기"

    override val welcomeHeadline = "집에서 Wi-Fi 신호가\n약한 곳을 찾아보세요"
    override val welcomeBody = "신호가 약해지는 곳과 개선 방법을 확인하세요."
    override val featureRoomsTitle = "방마다 측정"

    override val featureRoomsBody = "신호가 약한 방을 찾아보세요."

    override val featureRouterTitle = "공유기 확인"

    override val featureRouterBody = "응답하는 데 걸리는 시간을 확인하세요."

    override val featureNearbyTitle = "주변 네트워크 확인"

    override val featureNearbyBody = "겹침이 적은 채널을 찾아보세요."

}
