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
    override val qualityGoodExplanation = "来自路由器的稳定信号。"
    override val qualityFairLabel = "一般"
    override val qualityFairHeadline = "信号一般"
    override val qualityFairExplanation = "信号可用，但更容易受到干扰。"
    override val qualityWeakLabel = "较弱"
    override val qualityWeakHeadline = "信号薄弱处"
    override val qualityWeakExplanation = "信号微弱。将此位置与距离路由器较近的位置进行比较。"
    override val qualityDeadZoneLabel = "无信号"
    override val qualityDeadZoneExplanation = "到达此点的信号非常少。"

    override val deviceKindRouter = "路由器"
    override val deviceKindThisDevice = "此设备"
    override val deviceKindPhone = "手机或平板电脑"
    override val deviceKindComputer = "电脑"
    override val deviceKindTv = "电视或流媒体"
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

    override val verdictGreatHeadline = "路由器响应速度快"
    override val verdictGreatExplanation = "回复快速而稳定。"
    override val verdictGoodHeadline = "路由器响应可用"
    override val verdictGoodExplanation = "有些回复有点慢或参差不齐。"
    override val verdictSlowHeadline = "路由器回复延迟"
    override val verdictSlowExplanation = "回复缓慢或参差不齐。在路由器附近再试一下进行比较。"
    override val verdictUnreliableHeadline = "路由器检查无结果"
    override val verdictUnreliableExplanation = "许多回复丢失了。有些路由器会忽略这些检查，因此在更改任何内容之前请重试。"

    override val roomSuggestions = listOf("客厅", "厨房", "卧室", "书房", "儿童房", "浴室", "餐厅", "走廊", "车库", "地下室", "阳台", "花园")

    override val coverageNoRooms = "尚未检查房间"
    override fun coverageWeakInRoom(room: String) = "${room}的 Wi-Fi 信号较弱"

    override fun coverageWeakInRooms(count: Int) = "${count} 个房间的 Wi-Fi 信号较弱"

    override fun coverageRoomsCheckedNoneWeak(count: Int) = "已检查 ${count} 个房间，均未发现弱信号"

    override fun tipHighBandWeak(room: String, band: String) =
        "在${room}中，您使用的设备是${band}，它穿过墙壁的距离较短。如果您的路由器可以分割频段，2.4 GHz 可能会更好。"
    override fun tipSignalDrop(dropDb: Int, bestRoom: String, worstRoom: String) =
        "信号从${bestRoom}下降到${worstRoom}${dropDb}dB。尝试使用更中心、更高的铣刀位置，然后重新检查相同的位置，看看是否有帮助。"
    override fun tipMultipleAccessPoints(count: Int) =
        "您的设备在这些房间中使用了${count} 个接入点，这可能反映了不同的频段或接入点。仅此一点并不能证实可靠的漫游。"
    override val tipRouterPlacement = "将路由器放置在空旷的地方，远离地板，并远离微波炉、镜子和鱼缸。"

    override val spotFirstReading = "初读·修改后重新检查"
    override val spotDifferentNetwork = "不同网络或现货 · 开始新的比较"

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
    override val monitorChannelDescription = "当WiFi Weakspot正在监控WiFi网络时显示"
    override val alertsChannelName = "网络警报"
    override val alertsChannelDescription = "新网络和信号变化警报"
    override val monitorStartFailed = "监控无法启动。允许 Wi-Fi 扫描并启用位置服务，然后重试。"
    override val monitorWaitingForScanLimited = "等待新的Wi-Fi扫描。 Android 可能会限制扫描请求。"
    override val monitorWaitingForScan = "等待新的Wi-Fi扫描"
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
            MonitorSignalLevel.Excellent -> "优秀"
            MonitorSignalLevel.Good -> "好"
            MonitorSignalLevel.Fair -> "公平"
            MonitorSignalLevel.Weak -> "弱"
        }
        return "“${networkName}”信号现在为${quality}（${rssi}dBm）"
    }

    override val exportFileChooserTitle = "导出文件"
    override fun chartDescription(name: String) = "图表：${name}"

    override val lanVpnUnavailable = "无法通过此 VPN 连接进行设备发现。检查您的 VPN 是否允许本地网络访问，或断开连接后重试。"
    override val lanConnectFirst = "在扫描本地设备之前连接到Wi-Fi或以太网。"
    override val lanNotConnected = "未连接"

}
