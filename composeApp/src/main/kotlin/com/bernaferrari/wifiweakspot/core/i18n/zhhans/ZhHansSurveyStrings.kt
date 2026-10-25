package com.bernaferrari.wifiweakspot.core.i18n.zhhans

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object ZhHansSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "正在测量，请保持不动"

    override val tapWhereYouAreStanding = "请在平面图上点击你所在的位置"

    override val chooseWifi = "选择Wi-Fi"
    override val undoLastReading = "撤销上一次测量"

    override val allReadings = "所有读物"
    override val floorPlanTitle = "平面图"
    override fun readingCount(count: Int) = "${count} 条读数"

    override val mapOptions = "地图选项"
    override val usePlanPhoto = "使用平面图照片"

    override val changePhoto = "更换照片"

    override val removePhoto = "移除照片"

    override val exportReadingsCsv = "导出读数（CSV）"

    override fun floorName(floor: Int) = if (floor == 0) "地下层" else "${floor} 楼"

    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "楼层${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "添加地下层" else "添加 ${floor} 楼"

    override val deleteFloorReadingsAction = "删除此楼层的读数"

    override fun deleteFloorBody(count: Int) = "将删除此楼层的 ${count} 条读数。"

    override val oneRouterAtATime = "一张地图只测量一个接入点。Mesh 系统中的各个设备会分别显示。"

    override val noNetworksFound = "尚未找到网络，请确认 Wi-Fi 和位置信息已开启。"

    override val scanning = "正在扫描…"
    override val refresh = "刷新"

    override val switchWifiTitle = "切换到另一个Wi-Fi？"
    override val switchWifiBody = "此地图所有楼层的读数都会被清除。如需保留原来的数据，请新建地图。"

    override val clearAndSwitch = "清除并切换"

    override val deleteMapBody = "每层楼的所有读数都将被删除。"

    override val noReadingsYet = "这里还没有读数，请在平面图上点击你所在的位置。"

    override val weakestFirst = "按信号从弱到强排序，方便先查看信号较差的位置。"

    override val mapYourHome = "绘制您家的地图"
    override val mapYourHomeBody = "在家中的平面图上查看 Wi-Fi 覆盖情况，可使用平面图照片或空白网格。"

    override val createMap = "创建地图"

    override val namePlaceholder = "例如：楼上、书房"

    override val floorplanDescription = "测量用平面图"

    override fun coverageMapDescription(count: Int) = "Wi-Fi 覆盖图，共 ${count} 条读数。点击你所在的位置进行测量。"

    override val networkNotSeenHere = "在此位置未发现所选 Wi-Fi，未保存任何读数。"

    override val projectChangedWhileScanning = "扫描期间地图发生变化，未保存测量点。"

    override val couldNotSaveReading = "无法保存此读数，其他读数不受影响。"

    override fun defaultMapName(index: Int) = if (index == 1) "家" else "家 ${index}"

    override val stillSavingMap = "正在保存地图，请稍候。"

    override val noFreshScan = "未能从 Android 获取新的扫描结果，请稍后重试。"

    override val couldNotScan = "扫描失败，请检查 Wi-Fi 和位置信息设置。"

    override val chooseAccessPointFirst = "请先选择要测量覆盖范围的接入点。"

    override val saveMeasurementBeforeExport = "导出前至少保存一项测量结果。"
    override val couldNotExportProject = "无法导出地图，请重试。"

    override val connectToYourWifi = "连接到您的Wi-Fi"
    override fun checkingRoom(room: String) = "正在测量${room}，请保持不动"

    override val connectedWifi = "已连接的 Wi-Fi"
    override val connectedNoReading = "设备已连接 Wi-Fi，但尚未获取到信号读数。"

    override val connectToCheck = "请连接要检查的 Wi-Fi。"

    override fun removedRoom(room: String) = "已删除 ${room}"
    override val undo = "撤消"
    override val roomsEmptyHint = "在一个房间内保持不动并保存读数，再到其他房间测量，找出信号变弱的位置。"

    override val noWifiSignal = "无Wi-Fi信号"
    override val saveRoom = "保存房间测量"

    override fun dbStronger(delta: Int) = "增强了 ${delta} dB"

    override fun dbWeaker(delta: Int) = "减弱了 ${delta} dB"

    override val savedRooms = "已测量的房间"

    override fun deleteRoom(room: String) = "删除${room}"
    override fun roomDescription(room: String, quality: String, reading: String) = "${room}，信号：${quality}，${reading}。可查看读数并重新测量此位置。"

    override val noSignalToSave = "没有可保存的 Wi-Fi 信号，请先连接 Wi-Fi。"

    override val allowScansToLink = "请在“网络”页面允许 Wi-Fi 扫描，以便将读数关联到对应网络。"

    override val connectToSpotNetwork = "重新测量前，请连接上次测量此位置时使用的网络。"

    override fun savedRoom(room: String) = "已保存${room}"

    override val couldNotSaveStableReading = "无法保存稳定的读数。请保持连接同一 Wi-Fi 并静止不动，然后重试。"

    override val whereAreYou = "你在哪里？"
    override val somewhereElse = "其他地方"

    override fun savedAt(time: String) = "保存于${time}"

    override val moveRouterTip = "尝试将路由器移至更高、更中心或更靠近此房间的位置。"
    override val betterThanLastTime = "比上次更好"

    override val worseThanLastTime = "比上次更差"

    override val sameAsLastTime = "和上次一样"
    override val earlierChecks = "此前的测量"

    override val remove = "删除"
    override fun connectToRecheck(ssid: String?) =
        "连接${ssid?.ifBlank { null } ?: "相同Wi-Fi"}再次检查。"

    override val stepDeveloperOptionsTitle = "打开开发者选项"
    override val stepDeveloperOptionsBody = "在设置中打开**关于手机**，连续点击**版本号** 7 次，可能需要验证锁屏密码。"

    override val openAboutPhone = "打开“关于手机”"

    override val stepThrottlingTitle = "关闭 Wi-Fi 扫描限制"

    override val stepThrottlingBody = "在**开发者选项**的**网络**部分，关闭 **Wi-Fi 扫描限制**。"

    override val openDeveloperOptions = "打开开发者选项"
    override val stepComeBackTitle = "返回应用"

    override val stepComeBackBody = "返回应用后，WiFi Weakspot 会自动检查设置。你可以随时恢复扫描限制。"

    override val hideComputerMethod = "收起电脑操作步骤"

    override val useAdbInstead = "在电脑上通过 ADB 设置"

    override val adbCommandClipLabel = "ADB命令"
    override val hideRootOption = "收起 root 操作选项"

    override val useRootInstead = "通过 root 权限设置"

    override val scanLimitFootnote = "Android 会限制扫描次数以节省电量，你可以随时恢复限制。"

    override val unlimitedWithRootTitle = "通过 root 解除扫描次数限制"

    override val unlimitedOnTitle = "扫描次数不受限制"

    override val unlimitedOnBody = "附近网络和信道图会每隔几秒刷新一次。"

    override val scansLimitedTitle = "扫描次数受限"

    override val scansLimitedAndroid9Body = "Android 9 每 2 分钟最多允许扫描 4 次，且无法解除此限制。等待期间，WiFi Weakspot 会继续显示最近的结果。"

    override val usingRoot = "正在使用 root"

    override val deviceRooted = "此设备可使用 root 权限"

    override val rootScanningBody = "扫描通过系统 Shell 执行，无需修改开发者选项。"

    override val rootDeniedBody = "root 授权被拒绝。请在 root 管理应用中允许 WiFi Weakspot 后重试，或使用下方的开发者选项方法。"

    override val rootNotNeededBody = "扫描次数已不受限制，无需使用 root 权限。"

    override val rootOfferBody = "使用 root 权限扫描即可跳过开发者选项设置，root 管理应用会请求一次授权。"

    override val stopUsingRoot = "停止使用 root"

    override val waitingForRootManager = "正在等待 root 管理应用响应…"

    override val tryRootAgain = "重试 root 授权"

    override val useRoot = "使用 root"

    override val copied = "已复制"
    override val copyCommand = "复制命令"

}
