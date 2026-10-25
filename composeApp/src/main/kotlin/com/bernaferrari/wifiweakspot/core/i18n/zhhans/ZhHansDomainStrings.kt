package com.bernaferrari.wifiweakspot.core.i18n.zhhans

import com.bernaferrari.wifiweakspot.core.i18n.DomainStrings
import com.bernaferrari.wifiweakspot.core.lan.DeviceKind
import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel

object ZhHansDomainStrings : DomainStrings {

    override val signalDisplayBars = "信号格"
    override val qualityExcellentLabel = "极佳"
    override val qualityExcellentHeadline = "信号极佳"
    override val qualityExcellentExplanation = "与路由器的连接信号很强。信号强度本身不能衡量网速。"
    override val qualityGoodLabel = "良好"
    override val qualityGoodHeadline = "信号良好"
    override val qualityGoodExplanation = "路由器信号稳定。"

    override val qualityFairLabel = "一般"
    override val qualityFairHeadline = "信号一般"
    override val qualityFairExplanation = "信号可用，但更容易受到干扰。"
    override val qualityWeakLabel = "较弱"
    override val qualityWeakHeadline = "信号薄弱处"
    override val qualityWeakExplanation = "信号较弱，请在更靠近路由器的位置测量并比较。"

    override val qualityDeadZoneLabel = "无信号"
    override val qualityDeadZoneExplanation = "到达此点的信号非常少。"

    override val deviceKindRouter = "路由器"
    override val deviceKindThisDevice = "此设备"
    override val deviceKindPhone = "手机或平板电脑"
    override val deviceKindComputer = "电脑"
    override val deviceKindTv = "电视或流媒体设备"

    override val deviceKindSpeaker = "音箱"
    override val deviceKindPrinter = "打印机"
    override val deviceKindConsole = "游戏机"
    override val deviceKindCamera = "相机"
    override val deviceKindSmartHome = "智能家居设备"
    override val deviceKindUnknown = "设备"
    override val unnamedDevice = "未命名设备"

    override fun makerDevice(maker: String, kind: DeviceKind): String = "${maker}${kindLabel(kind)}"

    private fun kindLabel(kind: DeviceKind): String = when (kind) {
        DeviceKind.Router -> deviceKindRouter
        DeviceKind.ThisPhone -> deviceKindThisDevice
        DeviceKind.Phone -> deviceKindPhone
        DeviceKind.Computer -> deviceKindComputer
        DeviceKind.Tv -> deviceKindTv
        DeviceKind.Speaker -> deviceKindSpeaker
        DeviceKind.Printer -> deviceKindPrinter
        DeviceKind.Console -> deviceKindConsole
        DeviceKind.Camera -> deviceKindCamera
        DeviceKind.SmartHome -> deviceKindSmartHome
        DeviceKind.Unknown -> deviceKindUnknown
    }

    override val verdictGreatHeadline = "路由器响应很快"

    override val verdictGreatExplanation = "响应快速且稳定。"

    override val verdictGoodHeadline = "路由器响应尚可"

    override val verdictGoodExplanation = "部分响应略慢或不够稳定。"

    override val verdictSlowHeadline = "路由器响应延迟"

    override val verdictSlowExplanation = "响应较慢或不稳定，请在路由器附近重新测试并比较。"

    override val verdictUnreliableHeadline = "无法判断路由器响应情况"

    override val verdictUnreliableExplanation = "许多请求未收到响应。部分路由器会忽略此类测试，请先重试，再考虑更改设置。"

    override val roomSuggestions = listOf("客厅", "厨房", "卧室", "书房", "儿童房", "浴室", "餐厅", "走廊", "车库", "地下室", "阳台", "花园")

    override val coverageNoRooms = "尚未测量任何房间"

    override fun coverageWeakInRoom(room: String) = "${room}的 Wi-Fi 信号较弱"

    override fun coverageWeakInRooms(count: Int) = "${count} 个房间的 Wi-Fi 信号较弱"

    override fun coverageRoomsCheckedNoneWeak(count: Int) = "已测量 ${count} 个房间，均未发现弱信号"

    override fun tipHighBandWeak(room: String, band: String) = "在${room}使用的是 ${band} 频段，穿墙后的覆盖距离较短。如果路由器支持分开设置频段，2.4 GHz 在这里可能更稳定。"

    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) = "从${bestRoom}到${worstRoom}，信号减弱了 ${dropDb} dB。请尝试将路由器移到更居中、更高的位置，再到相同位置重新测量并比较。"

    override fun tipMultipleAccessPoints(count: Int) = "设备在这些房间中使用了 ${count} 个接入点，可能涉及不同频段或接入点，仅凭这一点无法判断漫游是否可靠。"

    override val tipRouterPlacement = "请将路由器放在室内较高、周围无遮挡的位置，并远离微波炉、镜子和鱼缸。"

    override val spotFirstReading = "首次测量；调整环境后可重新测量并比较"

    override val spotDifferentNetwork = "网络或位置不同，请开始新的比较"

    override fun spotComparison(deltaDb: Int, accessPointChanged: Boolean, bandChanged: Boolean): String {
        val signal = when {
            deltaDb >= 4 -> "信号${deltaDb} dB 更强"
            deltaDb <= -4 -> "信号${-deltaDb} dB 较弱"
            else -> "信号大致相同"
        }
        val changed = buildList {
            if (accessPointChanged) add("接入点已更改")
            if (bandChanged) add("频段已更改")
        }
        return signal + if (changed.isEmpty()) "（与上次检查相比）" else " · ${changed.joinToString("; ")}"
    }

    override val monitorChannelName = "后台监控"
    override val monitorChannelDescription = "显示 WiFi Weakspot 的 Wi-Fi 监测状态"

    override val alertsChannelName = "网络警报"
    override val alertsChannelDescription = "提示新发现的网络和信号变化"

    override val monitorStartFailed = "无法启动监测。请允许 Wi-Fi 扫描并开启位置信息，然后重试。"

    override val monitorWaitingForScanLimited = "正在等待新的 Wi-Fi 扫描结果。Android 可能会限制扫描次数。"

    override val monitorWaitingForScan = "正在等待新的 Wi-Fi 扫描结果"

    override fun monitorStatus(networkCount: Int, lastScanTime: String) = "${networkCount} 个网络 · 上次扫描：${lastScanTime}"

    override fun newNetworksTitle(count: Int) =
        if (count == 1) "检测到新WiFi网络" else "检测到新的 WiFi 网络"

    override fun newNetworksText(names: List<String>): String = when {
        names.size == 1 -> "“${names.first()}”现已在范围内"
        else -> "${names[0]}, ${names[1]}" + if (names.size > 2) " 及更多" else ""
    }

    override val signalChangeTitle = "信号变化"
    override fun signalChangeText(networkName: String, level: MonitorSignalLevel, rssi: Int): String {
        val quality = when (level) {
            MonitorSignalLevel.Excellent -> qualityExcellentLabel
            MonitorSignalLevel.Good -> qualityGoodLabel
            MonitorSignalLevel.Fair -> qualityFairLabel
            MonitorSignalLevel.Weak -> qualityWeakLabel
        }
        return "“${networkName}”的信号：${quality}（${rssi} dBm）"
    }

    override val exportFileChooserTitle = "导出文件"
    override fun chartDescription(name: String) = "图表：${name}"

    override val lanVpnUnavailable = "此 VPN 连接不支持设备发现。请检查 VPN 是否允许访问局域网，或断开 VPN 后重试。"

    override val lanConnectFirst = "请先连接 Wi-Fi 或以太网，再扫描局域网设备。"

    override val lanNotConnected = "未连接"

}
