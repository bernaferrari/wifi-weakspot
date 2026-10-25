package com.bernaferrari.wifiweakspot.core.i18n.zhhans

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object ZhHansAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = "${count} 个网络"

    override fun channelShort(channel: Int) = "信道 ${channel}"

    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · 第 ${channel} · ${band}"
    override val noPassword = "无密码"
    override val wifiOffTitle = "Wi-Fi 已关闭"
    override val locationOffBody = "Android 需要开启位置信息才能列出附近的 Wi-Fi。WiFi Weakspot 不使用 GPS。"

    override val scanPermissionBody = "Android 仅与具有位置访问权限的应用程序共享附近的网络名称。 WiFi Weakspot 永远不会记录您所在的位置。"
    override val wifiOffBody = "打开Wi-Fi 查看附近的网络。"
    override val locationOffTitle = "位置已关闭"
    override val turnOnLocation = "打开位置"
    override val scanPausedTitle = "Android 暂停扫描"
    override fun scanPausedBody(seconds: Long) = "允许在${seconds} 秒内进行下一次扫描。关闭扫描限制以跳过等待。"
    override val turnOffThrottling = "关闭节流"
    override val readyToScanBody = "点击扫描即可查看附近的每个网络。"
    override val scan = "扫描"
    override val channels = "信道"
    override val signalOverTime = "信号变化"
    override val howToReadThisChart = "如何阅读此图表"
    override val expandSignalHistory = "扩展信号历史记录"
    override val expandChannels = "拓展渠道"
    override fun noBandNetworksNearby(band: String) = "附近没有 ${band} 网络。"
    override fun networksOnBand(count: Int, band: String) = if (count == 0) "没有 ${band} 网络" else "${band} 网络：${count} 个"

    override fun showVeryWeakNetworks(count: Int) =
        if (count == 1) "显示 1 个非常弱的网络" else "显示${count} 非常弱的网络"
    override fun hideVeryWeakNetworks(count: Int) =
        if (count == 1) "隐藏 1 个非常弱的网络" else "隐藏 ${count} 非常弱的网络"
    override fun bandNetworkCount(count: Int) = if (count == 0) "无" else networks(count)
    override val highlightedOnChart = "在图表上突出显示"
    override val notHighlightedOnChart = "图表上未突出显示"
    override fun detailsForNetwork(ssid: String) = "${ssid.ifEmpty { "隐藏网络" }} 的详细信息"
    override fun alsoYourRouter(quality: String) = "${quality} · 也是你的路由器"
    override val connected = "已连接"

    override val scanReportHeading = "WiFi Weakspot 扫描报告"
    override fun scanReportTime(time: String) = "时间：${time}"
    override fun scanReportTotal(count: Int) = "网络总数：${count}"
    override val scanReportNetworksHeading = "网络："
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) =
        "• ${ssid} - ${rssi}dBm，第 ${channel}，${security}"
    override val channelLooksClear = "您的频道看起来很清晰"
    override val noChannelImprovement = "此次扫描未发现任何改进"
    override fun lessOverlapThanChannel(currentChannel: Int) = "比通道重叠少${currentChannel}"
    override val leastOverlapInScan = "本次扫描中重叠最少"
    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}：频道${channel}"
    override val lowOverlap = "低重叠"
    override val moderateOverlap = "适度重叠"
    override val highOverlap = "高重叠"
    override val veryHighOverlap = "非常高的重叠"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) =
        "通道图，${networks(networkCount)}。" +
            (if (busiestChannel != null) " 最繁忙的频道 ${busiestChannel} 和 ${busiestCount}。" else "")
    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) =
        name + (if (isYou) " （你）" else "") + (if (extraNames > 0) " +${extraNames}" else "")
    override val busyCaption = "拥挤"
    override val axisStrong = "强"
    override val axisOkay = "一般"
    override val axisWeak = "弱"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading}·${quality}·第${channel}"
    override val clearSelectedNetwork = "清除所选网络"
    override val tapWaveForDetails = "挥手了解网络详情"

    override val chartGuideTitle = "阅读图表"
    override val guideTallerBody = "高度是信号强度。"
    override val guideOverlapBody = "重叠网络共享频谱并可能竞争通话时间。"
    override val guideWiderBody = "更宽的信道占用更多的频谱并且可以重叠更多的邻居。"
    override val guideSameRouterBody = "+1: 似乎来自同一个电台的名字共享一座山。"
    override val guideCrowdBody = "绿色表示重叠较少，红色表示重叠较多。2.4 GHz 下面的条带显示重叠。点击它进行计数。"
    override val guideSteadyBody = "一条跳线显示信号强度的变化。点击一条线即可暂停，然后在时间中横向滑动或在网络之间垂直滑动。继续直播观看新读物。"
    override val guideHighlightBody = "点击一座山或一排以突出显示它。在图表上水平拖动进行探索。"

    override fun channelOverlapHeading(count: Int, channel: Int) = when (count) {
        0 -> "频道${channel}畅通"
        1 -> "1 个网络重叠频道${channel}"
        else -> "${count} 网络重叠频道 ${channel}"
    }
    override fun fromChannel(homeChannel: Int, reading: String) = "来自频道${homeChannel}·${reading}"

    override val estimatedChannelOverlap = "估计频道重叠"
    override fun tryChannel(channel: Int) = "尝试频道${channel}"
    override val lowestOverlapFor20Mhz = "20 个MHz 通道的最低估计重叠"
    override val overlapDisclaimer = "基于附近的接入点和信号强度，而不是测量的流量。 " +
        "在更改设置之前，请检查路由器支持的频道和本地规则。 " +
        "5 和 6 GHz 建议需要已知的路由器支持；缺少宽通道中心 " +
        "仅显示主要 20 个MHz 频道。"
    override val overlapScore = "重叠分数"

    override val collectingReadings = "收集读数...扫描几次后就会出现线条。"
    override fun signalHistoryDescription(networkCount: Int) =
        "${networks(networkCount)} 的信号历史记录。点击一条线进行检查。箭头键在时间和网络中移动。"
    override val historyLive = "直播"
    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) =
        "${networkName}、${reading ?: "没有阅读"}、暂停、${readingsAgo(readingsAgo)}"
    override val strongerNetwork = "更强大的网络"
    override val weakerNetwork = "网络较弱"
    override val axisOlder = "较早"
    override val axisLatest = "最新"
    override val exploreSignalHistory = "探索信号历史"
    override val tapLineToCompare = "点击一条线，然后拖动进行比较"
    override val noReadingAtPoint = "目前没有阅读"
    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) =
        "${reading} · ${quality} · " + if (readingsAgo == 0) "最新阅读" else readingsAgo(readingsAgo)
    private fun readingsAgo(count: Int) = if (count == 1) "1 次阅读前" else "${count} 阅读前"
    override val resumeLive = "继续直播"

    override fun seenReading(rssi: Int, seen: String) = "${rssi}dBm·见过${seen}"
    override val factChannel = "频道"
    override val factWidth = "宽度"
    override val factSecurity = "安全"
    override val factDistance = "距离估计"
    override val notAvailable = "不可用"
    override val watch = "观看"
    override val addToFavorites = "添加到收藏夹"
    override val removeFromFavorites = "从收藏夹中删除"

    override fun routerCheckLastTime(pingMs: String) = "${pingMs} 毫秒"
    override fun devicesFoundLastScan(count: Int) = "${count} 找到上次扫描"
    override val devicesSubtitle = "谁已连接"
    override val testWebsiteSubtitle = "Ping、DNS、端口"
    override fun savedChecks(count: Int) = when (count) {
        0 -> "过去的结果"
        1 -> "1 张支票"
        else -> "${count} 检查"
    }
    override val notOnWifi = "不在 Wi-Fi"
    override val online = "在线"
    override val internetNotConfirmed = "互联网未确认"
    override fun channelChip(channel: Int) = "频道${channel}"
    override fun linkRateChip(mbps: Int) = "链接率 ${mbps} Mbps"
    override val chooseNetwork = "选择网络"
    override val turnOnWifi = "打开Wi-Fi"
    override val dismiss = "解雇"
    override val findWeakSpotsTitle = "寻找弱点"
    override val findWeakSpotsBody = "查看哪些房间较弱。"
    override val findWeakSpotsAction = "检查你的房间"
    override fun networksNearby(count: Int) = "${networks(count)} 附近"
    override val nearbyStale = "来自您上次的扫描。点击刷新。"
    override val nearbyNoConnection = "查看每个网络的频道和信号。"
    override fun nearbyOverlap(count: Int, channel: Int) = when (count) {
        0 -> "在通道 ${channel} 上未检测到附近重叠。"
        1 -> "1 个附近信号与您的频道${channel} 重叠。"
        else -> "${count} 附近的信号与您的频道重叠${channel}。"
    }

    override val adviceNoWifiBody = "连接到您要检查的网络。您仍然可以使用当前的连接运行互联网测试和网络工具。"
    override val adviceNoInternetTitle = "首先检查互联网连接"
    override val adviceNoInternetBody = "Android 尚未确认此 Wi-Fi 连接上的互联网访问。检查登录页面，然后测试与路由器和互联网主机的连接。"
    override val adviceNoSignalTitle = "读取您的Wi-Fi信号"
    override val adviceNoSignalBody = "允许在网络中进行Wi-Fi 扫描以查看信号测量结果。比较您发现问题的房间内的信号。"
    override val adviceWeakSignalTitle = "比较路由器附近的覆盖范围"
    override val adviceWeakSignalBody = "当前Wi-Fi信号较弱。在此运行路由器检查，然后在路由器附近重复此操作。对您自己的网络进行调查来查找薄弱环节。"
    override val adviceGoodSignalTitle = "比较路由器响应能力"
    override val adviceGoodSignalBody = "信号强度看起来可用，并且Android 报告互联网访问情况。如果感觉连接速度很慢，请比较这里和路由器附近的路由器响应；仅仅强大的信号并不能保证性能。"
}
