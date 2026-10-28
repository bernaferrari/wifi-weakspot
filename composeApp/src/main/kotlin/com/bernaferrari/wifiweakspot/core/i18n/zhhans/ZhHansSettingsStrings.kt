package com.bernaferrari.wifiweakspot.core.i18n.zhhans

import com.bernaferrari.wifiweakspot.core.i18n.SettingsStrings

object ZhHansSettingsStrings : SettingsStrings {
    override val sectionBackup = "备份"
    override val backupScope = "将地图、平面图图片和房间测量数据保存到您选择的文件。备份不包含设置和诊断历史记录。"
    override val exportBackup = "保存备份"
    override val restoreBackup = "恢复备份"
    override val restoreBackupExplanation = "请选择 WiFi Weakspot 备份。地图将作为副本添加，现有房间测量数据将保留。当前数据不会被替换。"
    override val backupSaved = "备份已保存。"
    override val backupRestored = "备份已恢复。"
    override val backupAlreadyRestored = "此备份已恢复过。"
    override val backupExportFailed = "无法保存备份。请检查可用空间后重试。"
    override val backupRestoreFailed = "无法完成恢复。请检查备份文件和可用空间，然后使用同一文件重试。"

    override val sectionScanning = "扫描"

    override val fasterScansThrottled = "Android 允许每 2 分钟扫描 4 次"
    override val sectionAppearance = "外观"
    override val themeAuto = "自动"
    override val themeLight = "浅色"
    override val themeDark = "深色"
    override val colorStyle = "配色风格"
    override val styleExpressive = "表现力"
    override val styleVibrant = "鲜艳"
    override val styleTonal = "柔和色调"
    override val styleCmf = "柔和"
    override val showSignalAs = "信号显示方式"
    override val sectionComfort = "使用体验"

    override val reduceMotionTitle = "减少动画效果"

    override val hapticsSubtitle = "点击和显示结果时振动"

    override val sectionAlerts = "警报"
    override val backgroundAlertsTitle = "后台提醒"

    override val backgroundAlertsSubtitle = "附近 Wi-Fi 发生变化时"

    override val notificationsDenied = "请允许通知，以查看监测状态和提醒。"

    override val checkEvery = "检查间隔"

    override fun intervalSeconds(seconds: Int) = "${seconds}秒"
    override fun intervalMinutes(minutes: Int) = "${minutes}分钟\n上次"
    override val newNetworkAlertsTitle = "发现新网络时提醒"

    override val signalChangeAlertsTitle = "信号变化时提醒"

    override val signalChangeAlertsSubtitle = "信号强度大幅变化时"

    override val sectionAbout = "关于"

    override fun aboutApp(appName: String) = "关于${appName}"
    override fun version(version: String) = "版本${version}"
    override val replayIntroTitle = "重新查看入门介绍"

    override val wallpaperColors = "壁纸颜色"
    override val colorIndigo = "靛蓝"
    override val colorOrange = "橙色"
    override val colorAmber = "琥珀色"
    override val colorGreen = "绿色"
    override val colorTeal = "蓝绿色"

    override val colorCyan = "青色"
    override val colorBlue = "蓝色"
    override val colorPurple = "紫色"
    override val colorPink = "粉色"

    override val tagline = "找出家中 Wi-Fi 信号弱的地方"
    override val openSourceLicense = "开源·Apache 2.0"
    override val feedbackTitle = "反馈"
    override val feedbackBody = "想法和错误报告"
    override val elsewhere = "相关链接"

    override val privacySummary = "除非你主动分享，否则结果只保存在你的设备上。"
    override val privacyPolicy = "隐私政策"

    override val notNow = "暂时不用"

    override val getStarted = "开始使用"

    override val welcomeHeadline = "找出家中的\nWi-Fi 信号弱点"

    override val welcomeBody = "看看哪里信号弱，以及如何改善。"
    override val featureRoomsTitle = "测量每个房间"

    override val featureRoomsBody = "找出信号较弱的房间。"

    override val featureRouterTitle = "检查路由器"

    override val featureRouterBody = "查看路由器的响应速度。"

    override val featureNearbyTitle = "查看附近网络"

    override val featureNearbyBody = "寻找重叠较少的信道。"

}
