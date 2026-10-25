package com.bernaferrari.wifiweakspot.core.i18n.zhhans

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object ZhHansSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "测量...保持不动"
    override val tapWhereYouAreStanding = "点击您所在位置的地图"
    override val chooseWifi = "选择Wi-Fi"
    override val undoLastReading = "撤消上次阅读"
    override val allReadings = "所有读物"
    override val floorPlanTitle = "平面图"
    override fun readingCount(count: Int) = "${count} 条读数"

    override val mapOptions = "地图选项"
    override val usePlanPhoto = "使用您的计划的照片"
    override val changePhoto = "更改照片"
    override val removePhoto = "删除照片"
    override val exportReadingsCsv = "导出读数 (CSV)"
    override fun floorName(floor: Int) = if (floor == 0) "地下室" else "楼层${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "楼层${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "添加地下室" else "添加楼层 ${floor}"
    override val deleteFloorReadingsAction = "删除该楼层的读数"
    override fun deleteFloorBody(count: Int) =
        if (count == 1) "其1个读数将被删除。" else "其${count}读数将被删除。"

    override val oneRouterAtATime = "一张地图一次跟随一个路由器。网格系统单独显示每个单元。"
    override val noNetworksFound = "尚未找到网络。检查Wi-Fi和位置是否已打开。"
    override val scanning = "正在扫描…"
    override val refresh = "刷新"
    override val switchWifiTitle = "切换到另一个Wi-Fi？"
    override val switchWifiBody = "这会清除该地图每一层的读数。创建一个新地图来保留两者。"
    override val clearAndSwitch = "清除并切换"

    override val deleteMapBody = "每层楼的所有读数都将被删除。"

    override val noReadingsYet = "这里还没有读数。点击您所在位置的地图。"
    override val weakestFirst = "最弱的在前，所以问题点在最上面。"
    override val mapYourHome = "绘制您家的地图"
    override val mapYourHomeBody = "在您家的地图上查看您的Wi-Fi。使用您的计划的照片或空白网格。"
    override val createMap = "创建地图"

    override val namePlaceholder = "楼上，办公室……"

    override val floorplanDescription = "项目平面图"
    override fun coverageMapDescription(count: Int) =
        "带有${readingCount(count)}的覆盖图。点击您站立的位置进行测量。"

    override val networkNotSeenHere = "那个Wi-Fi没有出现在这里。什么都没有保存。"
    override val projectChangedWhileScanning = "扫描时项目发生变化。毫无意义。"
    override val couldNotSaveReading = "无法保存该读数。您的其他读数是安全的。"
    override fun defaultMapName(index: Int) = if (index == 1) "首页" else "首页${index}"
    override val stillSavingMap = "一会儿，还在保存地图。"
    override val noFreshScan = "Android 未提供新扫描。稍后再试。"
    override val couldNotScan = "无法扫描。检查Wi-Fi和位置设置。"
    override val chooseAccessPointFirst = "在绘制其覆盖范围之前选择一个接入点。"
    override val saveMeasurementBeforeExport = "导出前至少保存一项测量结果。"
    override val couldNotExportProject = "无法导出该项目。再试一次。"

    override val connectToYourWifi = "连接到您的Wi-Fi"
    override fun checkingRoom(room: String) = "检查${room}…保持静止"
    override val connectedWifi = "已连接的 Wi-Fi"
    override val connectedNoReading = "您的设备已连接，但尚未读取信号。"
    override val connectToCheck = "连接到您要检查的Wi-Fi。"
    override fun removedRoom(room: String) = "已删除 ${room}"
    override val undo = "撤消"
    override val roomsEmptyHint =
        "站在房间里保存读数。检查另一个房间，找出信号变弱的地方。"
    override val noWifiSignal = "无Wi-Fi信号"
    override val saveRoom = "节省空间"
    override fun dbStronger(delta: Int) = "${delta} dB 更强"
    override fun dbWeaker(delta: Int) = "${delta} dB 较弱"
    override val savedRooms = "已保存房间"
    override fun deleteRoom(room: String) = "删除${room}"
    override fun roomDescription(room: String, quality: String, reading: String) =
        "${room}，${quality}信号，${reading}。检查读数并重新检查该位置。"

    override val noSignalToSave = "无Wi-Fi 保存信号。首先连接到您的Wi-Fi。"
    override val allowScansToLink = "允许在网络中进行 Wi-Fi 扫描，以便此读数可以链接到您的网络。"
    override val connectToSpotNetwork = "在重新检查之前连接到该地点的网络。"
    override fun savedRoom(room: String) = "已保存 ${room}"
    override val couldNotSaveStableReading = "无法保存稳定的读数。保持原样Wi-Fi不动，然后重试。"
    override val whereAreYou = "你在哪里？"
    override val somewhereElse = "其他地方"

    override fun savedAt(time: String) = "已保存 ${time}"
    override val moveRouterTip = "尝试将路由器移至更高、更中心或更靠近此房间的位置。"
    override val betterThanLastTime = "比上次好"
    override val worseThanLastTime = "比上次更糟糕"
    override val sameAsLastTime = "和上次一样"
    override val earlierChecks = "早期检查"
    override val remove = "删除"
    override fun connectToRecheck(ssid: String?) =
        "连接${ssid?.ifBlank { null } ?: "相同Wi-Fi"}再次检查。"

    override val stepDeveloperOptionsTitle = "打开开发者选项"
    override val stepDeveloperOptionsBody =
        "在“设置”中，打开“**关于手机**”，然后点击“**内部版本号**”7 次。您可能需要输入屏幕锁定。"
    override val openAboutPhone = "打开关于手机"
    override val stepThrottlingTitle = "关闭 Wi-Fi 扫描限制"
    override val stepThrottlingBody =
        "在**开发者选项**中，向下滚动到**网络**并关闭**Wi-Fi扫描限制**。"
    override val openDeveloperOptions = "打开开发者选项"
    override val stepComeBackTitle = "回到这里"
    override val stepComeBackBody =
        "WiFi Weakspot 返回时自动检查。您可以随时重新打开限制。"
    override val hideComputerMethod = "隐藏电脑方法"
    override val useAdbInstead = "喜欢电脑吗？使用ADB"
    override val adbCommandClipLabel = "ADB命令"
    override val hideRootOption = "隐藏 root 选项"
    override val useRootInstead = "已 Root 的设备？使用 root 代替"
    override val scanLimitFootnote = "Android 限制扫描以节省电池。您可以随时重新开启限制。"
    override val unlimitedWithRootTitle = "无限次扫描root"
    override val unlimitedOnTitle = "无限扫描已开启"
    override val unlimitedOnBody = "附近的网络和频道图表每隔几秒刷新一次。"
    override val scansLimitedTitle = "扫描次数有限"
    override val scansLimitedAndroid9Body =
        "Android 9 允许每 2 分钟扫描 4 次，并且无法将其关闭。 WiFi Weakspot 重复使用中间的最近结果。"
    override val usingRoot = "使用root"
    override val deviceRooted = "您的设备已root"
    override val rootScanningBody = "扫描通过系统 shell，因此开发人员选项中无需更改。"
    override val rootDeniedBody =
        "Root 访问被拒绝。在您的 root 管理器中允许 WiFi Weakspot 并重试，或使用下面的开发者选项。"
    override val rootNotNeededBody = "扫描次数已经是无限的。不需要root。"
    override val rootOfferBody =
        "跳过开发者选项并让WiFi Weakspot扫描root。您的root经理会询问一次。"
    override val stopUsingRoot = "停止使用root"
    override val waitingForRootManager = "等待您的root经理…"
    override val tryRootAgain = "再试一次root"
    override val useRoot = "使用root"
    override val copied = "已复制"
    override val copyCommand = "复制命令"
}
