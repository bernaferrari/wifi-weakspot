package com.bernaferrari.wifiweakspot.core.i18n.zhhans

import com.bernaferrari.wifiweakspot.core.i18n.AnalyzeStrings

object ZhHansAnalyzeStrings : AnalyzeStrings {

    private fun networks(count: Int) = "${count} 个网络"

    override fun channelShort(channel: Int) = "信道 ${channel}"

    override fun shareNetworkLine(rssi: Int, channel: Int, band: String) = "${rssi} dBm · ${channelShort(channel)} · ${band}"

    override val noPassword = "无密码"
    override val wifiOffTitle = "Wi-Fi 已关闭"
    override val locationOffBody = "Android 需要开启位置信息才能列出附近的 Wi-Fi。WiFi Weakspot 不使用 GPS。"

    override val scanPermissionBody = "Android 要求应用获得位置信息访问权限，才能读取附近的 Wi-Fi 名称。WiFi Weakspot 不会记录你的位置。"

    override val wifiOffBody = "请开启 Wi-Fi 以查看附近网络。"

    override val locationOffTitle = "位置信息已关闭"

    override val turnOnLocation = "打开位置"
    override val scanPausedTitle = "Android 暂停了扫描"

    override fun scanPausedBody(seconds: Long) = "${seconds} 秒后可再次扫描。关闭扫描限制即可跳过等待。"

    override val turnOffThrottling = "关闭扫描限制"

    override val readyToScanBody = "点击“扫描”以查找附近的网络。"

    override val scan = "扫描"
    override val channels = "信道"
    override val signalOverTime = "信号变化"
    override val howToReadThisChart = "图表怎么看"

    override val expandSignalHistory = "展开信号历史"

    override val expandChannels = "展开信道图"

    override fun noBandNetworksNearby(band: String) = "附近没有 ${band} 网络。"
    override fun networksOnBand(count: Int, band: String) = if (count == 0) "没有 ${band} 网络" else "${band} 网络：${count} 个"

    override fun showVeryWeakNetworks(count: Int) = "显示 ${count} 个信号很弱的网络"

    override fun hideVeryWeakNetworks(count: Int) = "隐藏 ${count} 个信号很弱的网络"

    override fun bandNetworkCount(count: Int) = if (count == 0) "无" else "${count}个"

    override val highlightedOnChart = "已在图表中高亮"

    override val notHighlightedOnChart = "未在图表中高亮"

    override fun detailsForNetwork(ssid: String) = "${ssid.ifEmpty { "隐藏网络" }} 的详细信息"
    override fun alsoYourRouter(quality: String) = "${quality} · 同一路由器的其他网络"

    override val connected = "已连接"

    override val scanReportHeading = "WiFi Weakspot 扫描报告"

    override fun scanReportTime(time: String) = "时间：${time}"
    override fun scanReportTotal(count: Int) = "网络总数：${count}"
    override val scanReportNetworksHeading = "网络："
    override fun scanReportLine(ssid: String, rssi: Int, channel: Int, security: String) = "• ${ssid} - ${rssi} dBm, ${channelShort(channel)}, ${security}"

    override val channelLooksClear = "当前信道较空闲"

    override val noChannelImprovement = "未找到更好的信道"

    override fun lessOverlapThanChannel(currentChannel: Int) = "比信道 ${currentChannel} 更空闲"

    override val suggestedChannel = "建议信道"

    override fun channelAdviceDescription(advice: String, channel: Int) = "${advice}：信道${channel}"
    override val lowOverlap = "重叠较少"

    override val moderateOverlap = "重叠中等"

    override val highOverlap = "重叠较多"

    override val veryHighOverlap = "重叠很多"

    override fun channelGraphDescription(networkCount: Int, busiestChannel: Int?, busiestCount: Int) = "信道图，共 ${networkCount} 个网络。" + if (busiestChannel != null) " 最拥挤的信道为 ${busiestChannel}，有 ${busiestCount} 个网络。" else ""

    override fun chartNetworkLabel(name: String, isYou: Boolean, extraNames: Int) = name + (if (isYou) " (${connected})" else "") + (if (extraNames > 0) " +${extraNames}" else "")

    override val busyCaption = "拥挤"
    override val axisStrong = "强"
    override val axisOkay = "一般"
    override val axisWeak = "弱"
    override fun selectedNetworkSummary(reading: String, quality: String, channel: Int) = "${reading} · ${quality} · ${channelShort(channel)}"

    override val clearSelectedNetwork = "清除网络选择"

    override val tapWaveForDetails = "点击曲线查看网络详情"

    override val chartGuideTitle = "图表说明"

    override val guideTallerBody = "曲线越高，信号越强。"

    override val guideOverlapBody = "相互重叠的网络会共享频谱，并可能争用传输时间。"

    override val guideWiderBody = "信道越宽，占用的频谱越多，也越容易与附近网络重叠。"

    override val guideSameRouterBody = "+1：可能使用同一 Wi-Fi 无线模块的网络名称会合并显示在同一条曲线上。"

    override val guideCrowdBody = "绿色表示重叠较少，红色表示重叠较多。2.4 GHz 下面的条带显示重叠。点击它进行计数。"
    override val guideSteadyBody = "曲线上下波动表示信号强度在变化。点击曲线暂停，左右拖动切换时间点，上下拖动切换网络。恢复实时显示后可继续查看新读数。"

    override val guideHighlightBody = "点击曲线或列表项即可高亮显示，也可在图表上左右拖动查看。"

    override fun channelOverlapHeading(count: Int, channel: Int) = if (count == 0) "信道 ${channel} 无重叠" else "${count} 个网络与信道 ${channel} 重叠"

    override fun fromChannel(homeChannel: Int, reading: String) = "来自信道 ${homeChannel} · ${reading}"

    override val estimatedChannelOverlap = "估算的信道重叠"

    override fun tryChannel(channel: Int) = "尝试信道${channel}"
    override val lowestOverlapFor20Mhz = "按 20 MHz 信道估算，重叠最少"

    override val overlapDisclaimer = "此估算基于附近的接入点和信号强度，并非实测流量。更改设置前，请确认路由器支持的信道及当地规定。仅在已知路由器支持时才会建议 5 GHz 和 6 GHz 信道。若无法获取宽信道的中心频率，则只显示主信道的 20 MHz 范围。"

    override val overlapScore = "重叠评分"

    override val collectingReadings = "正在收集读数，完成几次扫描后会显示曲线。"

    override fun signalHistoryDescription(networkCount: Int) = "共 ${networkCount} 个网络的信号历史。点击曲线查看，可用方向键切换时间点和网络。"

    override val historyLive = "实时"

    override fun historyPaused(networkName: String, reading: String?, readingsAgo: Int) = "${networkName}, ${reading ?: "无读数"}, 已暂停, ${readingsAgo(readingsAgo)}"

    override val strongerNetwork = "信号更强的网络"

    override val weakerNetwork = "信号更弱的网络"

    override val axisOlder = "较早"
    override val axisLatest = "最新"
    override val exploreSignalHistory = "查看信号历史"

    override val tapLineToCompare = "点击曲线并拖动以进行比较"

    override val noReadingAtPoint = "此时间点没有读数"

    override fun inspectedReading(reading: String, quality: String, readingsAgo: Int) = "${reading} · ${quality} · " + if (readingsAgo == 0) "最新读数" else readingsAgo(readingsAgo)

    private fun readingsAgo(count: Int) = "${count} 次测量前"

    override val resumeLive = "恢复实时显示"

    override fun seenReading(rssi: Int, seen: String) = "${rssi} dBm · 发现于${seen}"

    override val factChannel = "信道"
    override val factWidth = "信道宽度"

    override val factSecurity = "安全"
    override val factDistance = "估算距离"

    override val notAvailable = "无法获取"

    override val watch = "跟踪信号"

    override val addToFavorites = "添加到收藏夹"
    override val removeFromFavorites = "从收藏夹中删除"

    override fun routerCheckLastTime(pingMs: String) = "上次：${pingMs} ms"

    override fun devicesFoundLastScan(count: Int) = "上次扫描发现 ${count} 台"

    override val devicesSubtitle = "谁在使用网络"

    override val testWebsiteSubtitle = "Ping、DNS、端口"

    override fun savedChecks(count: Int) = if (count == 0) "历史结果" else "${count} 次检查"

    override val notOnWifi = "未连接 Wi-Fi"

    override val online = "在线"
    override val internetNotConfirmed = "尚未确认互联网连接"

    override fun channelChip(channel: Int) = "信道${channel}"
    override fun linkRateChip(mbps: Int) = "链接率 ${mbps} Mbps"
    override val chooseNetwork = "选择网络"
    override val turnOnWifi = "打开Wi-Fi"
    override val dismiss = "解雇"
    override val findWeakSpotsTitle = "寻找信号薄弱处"

    override val findWeakSpotsBody = "查看哪些房间的信号较弱。"

    override val findWeakSpotsAction = "测量各个房间"

    override fun networksNearby(count: Int) = "附近有 ${count} 个网络"

    override val nearbyStale = "这是上次扫描的结果，点击即可刷新。"

    override val nearbyNoConnection = "查看每个网络的信道和信号。"
    override fun nearbyOverlap(count: Int, channel: Int) = if (count == 0) "未发现与当前信道 ${channel} 重叠的附近信号。" else "附近有 ${count} 个信号与当前信道 ${channel} 重叠。"

    override val adviceNoWifiBody = "连接到你要检查的网络。你仍然可以使用当前的连接运行互联网测试和网络工具。"
    override val adviceNoInternetTitle = "先检查互联网连接"

    override val adviceNoInternetBody = "Android 尚未确认此 Wi-Fi 能否访问互联网。请检查是否需要在网页上登录，再测试与路由器和互联网主机的连接。"

    override val adviceNoSignalTitle = "测量 Wi-Fi 信号"

    override val adviceNoSignalBody = "在“网络”页面允许 Wi-Fi 扫描，即可查看信号测量结果。请在出现问题的房间测量并比较。"

    override val adviceWeakSignalTitle = "在路由器附近测量并比较"

    override val adviceWeakSignalBody = "当前 Wi-Fi 信号较弱。请先在此处检查路由器，再靠近路由器重复测试。测量自己网络的覆盖范围，可以找出信号薄弱的位置。"

    override val adviceGoodSignalTitle = "比较路由器响应"

    override val adviceGoodSignalBody = "信号强度可用，Android 也已确认互联网连接。如果感觉速度慢，请比较当前位置和路由器附近的响应时间；信号强并不一定代表通信性能好。"

    override val channelAdviceBasis = "条形越短，本次扫描中的重叠越少。速度可能有所变化。"
    override fun channelChangeInstructions(channel: Int, band: String) = "为 ${band} 选择信道 ${channel}"

    override val channelOverlapTitle = "与附近 Wi-Fi 的重叠"
    override val channelApplyChange = "保存，必要时重新连接"
    override val channelChangeNote = "请在路由器的应用或设置中更改，而非在 WiFi Weakspot 中更改。"

}
