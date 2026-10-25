package com.bernaferrari.wifiweakspot.core.i18n.zhhans

import com.bernaferrari.wifiweakspot.core.i18n.HistoryStrings

object ZhHansHistoryStrings : HistoryStrings {
    override val today = "今天"
    override val yesterday = "昨天"
    override val weekdayNames = listOf("星期一", "星期二", "星期三", "星期四", "星期五", "星期六", "星期日")
    override val weekdayShortNames = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    override val monthShortNames = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")
    override fun dateThisYear(weekday: String, day: Int, month: String) = "${month}${day}日（${weekday}）"

    override fun dateOtherYear(weekday: String, day: Int, month: String, year: Int) = "${year}年${month}${day}日（${weekday}）"

    override fun momentAt(day: String, time: String) = "${day} ${time}"

    override val filterAll = "全部"
    override val kindInternetTests = "互联网测试"
    override val kindScans = "扫描"
    override val emptyTitle = "尚未保存任何内容"
    override val emptySubtitle = "这里会保存路由器检查、网络扫描和工具结果，方便查看 Wi-Fi 随时间的变化。"

    override fun trendBarsDescription(count: Int, readings: String) = "最近 ${count} 次路由器测试：${readings}"

    override val trendNoReply = "路由器没有回复。有些路由器会忽略这些检查。"
    override val trendRecheckToCompare = "更改后重新检查比较。"
    override fun trendQuicker(usualMs: String) = "比平常快 (${usualMs} 毫秒)"
    override fun trendSlower(usualMs: String) = "比平常慢 (${usualMs} 毫秒)"
    override fun trendAboutSame(usualMs: String) = "与平常大致相同 (${usualMs} 毫秒)"

    override fun internetTitle(mbps: String) = "${mbps}Mbps下载"
    override fun routerTitle(ms: String) = "${ms} 毫秒到路由器"
    override fun networksNearby(count: Int) = "附近有 ${count} 个网络"

    override fun strongestNetwork(network: String) = "最强：${network}"
    override fun accessPoints(count: Int) = "${count} 个接入点"

    override fun devicesFound(count: Int) = "发现 ${count} 台设备"

    override fun listWithMore(list: String, more: Int) = if (more > 0) "${list}，以及另外 ${more} 项" else list

    override fun pingSummary(averageMs: String, replied: Int, sent: Int) = "${averageMs} ms · ${sent} 次中收到 ${replied} 次响应"

    override fun lookupTitle(target: String) = "查询 ${target}"

    override val noRecords = "无记录"

    override val lookupFailed = "查找失败"
    override fun openPortsTitle(target: String) = "${target} 的开放端口"

    override val noCommonPortsOpen = "公共端口均未开放"
    override val completed = "已完成"

    override val failed = "失败"

    override val metricDownload = "下载"
    override val metricUpload = "上传"
    override val metricLatency = "延迟"
    override val metricVariation = "波动"

    override val metricLost = "丢失"

    override fun metricOnBand(band: String) = "于 ${band}"
    override val metricReplied = "有响应"

    override val metricReplies = "响应"

    override val metricAverage = "平均"
    override val factServer = "服务器"
    override val factRoom = "房间"
    override val factBand = "频段"

    override val factSignal = "信号"
    override val factAccessPoints = "接入点"
    override fun signalFact(quality: String, rssi: Int) = "${quality}信号·${rssi}dBm"

    override val linesNetworksStrongestFirst = "网络，最强第一"
    override val linesRecords = "DNS 记录"

    override fun replyNumber(index: Int) = "回复${index}"
    override val scanNote = "与你处于同一信道的附近网络会争夺通话时间。"

    override val go = "开始"

    override val testInternetSpeedInstead = "测试网速"
    override val keepDeviceStill = "让你的设备保持静止几秒钟。"
    override val connectToStart = "请先连接 Wi-Fi 以开始测试。"

    override val lastTime = "最后一次"
    override val couldNotSaveSpot = "无法保存这个位置。再试一次。"
    override val connectFirst = "请先连接 Wi-Fi，此测试会测量路由器的响应时间。"

    override val historyNotSaved = "测量结果，但无法保存历史记录。"
    override val checkStopped = "网络连接发生变化或测试已停止。请保持连接此 Wi-Fi 后重试，部分路由器不会响应 Ping。"

    override val internetSpeedTitle = "网速·fast.com"
}
