package com.bernaferrari.wifiweakspot.core.i18n.zhhans

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object ZhHansToolsStrings : ToolsStrings {
    override val tryAgain = "再试一次"

    override val targetPlaceholder = "网站或IP地址"
    override val clearTarget = "清除"
    override val checkSectionTitle = "检查"
    override val recentSectionTitle = "最近"

    override val toolPortsDescription = "开放时间"

    override val pingQuestion = "它有回应吗？"
    override val dnsQuestion = "地址是什么？"
    override val dnsDetail = "DNS查找"
    override val portsQuestion = "它提供什么？"

    override val runningPortScanTitle = "正在检查端口"

    override val diagnosticFailed = "诊断失败"
    override val invalidTarget = "输入主机名或 IP 地址，不带 URL 路径或空格。"
    override val diagnosticCouldNotComplete = "诊断无法完成。"
    override val connectionChangedRunAgain = "连接已更改。在新网络上再次运行此检查。"

    override val openItsWebPage = "打开其网页"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ${total} 次中收到 ${answered} 次响应"

    override val slowest = "最慢"
    override fun repliesContentDescription(replies: String) = "回复：${replies}"

    override val pingDidntAnswer = "未接听"
    override val pingResponsive = "响应式"
    override val pingResponsiveButUneven = "反应灵敏，但不均匀"
    override val pingDroppingReplies = "删除回复"
    override val pingSlowToAnswer = "回答慢"

    override val pingNoAnswerLocal = "它可能已关闭、处于睡眠状态或设置为忽略 ping。"
    override fun pingRepliesLost(lost: Int, total: Int) = "${total} 次请求中有 ${lost} 次未收到响应。请对比路由器检查和互联网主机测试的结果，帮助定位问题；仅凭此测试无法确定原因。"

    override val pingUneven = "回复时间各不相同。比较此处和路由器附近的路由器检查，然后测试互联网主机。"
    override val pingLocalExplanation = "到达该设备并返回的时间到了。设备本身会影响结果。"
    override val pingInternetExplanation = "是时候到达该主机并返回了。您的连接、路由和主机都会影响结果。"

    override fun dnsAddressUnit(count: Int) = "个地址"

    override fun dnsForQuery(query: String) = "对于${query}"
    override val dnsNotFound = "找不到它"
    override val dnsFound = "找到了"
    override val dnsNotFoundExplanation = "检查拼写，或重试；该名称可能不存在。"
    override fun dnsFoundExplanation(query: String) = "您的设备可以找到${query}。"
    override val dnsAddressesTitle = "地址"

    override val portsOpenUnit = "打开"
    override fun portsCommonServicesOn(host: String) = "${host}上的常用服务"
    override val portsNothingOpen = "没有开放"
    override fun portsOffersServices(count: Int) = if (count == 1) "提供 1 项服务" else "提供${count}服务"
    override val portsNoneAnswered = "没有公共服务应答。"
    override val portsOffersExplanation = "设置页面或文件共享之类的东西。"

    override val portWebPage = "网页"
    override val portSecureWebPage = "安全网页"
    override val portRemoteLoginSsh = "远程登录(SSH)"
    override val portOldRemoteLoginTelnet = "旧远程登录 (Telnet)"
    override val portFileTransferFtp = "文件传输 (FTP)"
    override val portAddressLookupsDns = "地址查找 (DNS)"
    override val portFileSharing = "文件共享"
    override val portMacFileSharing = "Mac 文件共享"
    override val portPrinting = "打印"
    override val portRemoteDesktop = "远程桌面"
    override val portScreenSharing = "屏幕共享"
    override val portDeviceDiscoveryUpnp = "设备发现 (UPnP)"
    override val portStorageOrAdminPage = "存储或管理页面"
    override val portDatabase = "数据库"
    override val portService = "服务"
    override val devicesLooking = "正在寻找设备..."
    override val devicesFindOnWifi = "在您的 Wi-Fi 上查找设备"
    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${count}于${ssid}"
    override fun devicesOnYourNetwork(count: Int) = "${count} 在您的网络上"
    override val couldntLookForDevices = "无法查找设备"
    override val scanFailed = "扫描失败"
    override val connectionChangedLookAgain = "连接已更改。再到这个网络上看一下。"
    override val stoppedLooking = "停止寻找"
    override val noDevicesFound = "未找到设备"
    override val noDevicesFoundSubtitle =
        "有些设备在被询问时会保持安静。请稍后重试，或检查此设备是否在您的家中Wi-Fi。"
    override fun otherDevicesGroup(count: Int) = "其他设备 · ${count}"
    override fun quietDevices(count: Int) = if (count == 1) "1 个安静设备" else "${count} 安静的设备"
    override val quietDevicesSubtitle = "他们没有说他们是什么"
    override val hide = "隐藏"
    override val show = "显示"
    override fun checkingLocalAddresses(percent: Int) = "检查本地地址 · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · 未接听"

    override val deviceNotScannedYet = "扫描中尚未找到"
    override val deviceAnsweredLastScan = "已回答上次扫描"
    override val deviceSeenButNoAnswer = "在网络上看到，但没有回复"
    override val actionCheckPorts = "检查端口"
    override val openRouterSettings = "打开路由器设置"
    override val detailsTitle = "细节"
    override val factNetworkName = "网络名称"
    override val factHardwareAddress = "硬件地址"
    override val macHiddenByAndroid = "被Android隐藏"
    override val factYourName = "你的名字"
    override val portsNotCheckedYet = "尚未检查。"
    override fun portsNoneOpenWhenChecked(moment: String) =
        "检查时${moment.replaceFirstChar { it.lowercase() }}，所有公共端口均未打开。"
    override fun portsCheckedAt(moment: String) = "检查${moment.replaceFirstChar { it.lowercase() }}。"
    override val renameDeviceTitle = "重命名设备"
    override val renameSupporting = "留空以使用找到的名称WiFi Weakspot。"
    override val connectionChangedOpenAgain = "连接已更改。在新网络上再次打开该设备。"
    override val couldntLoadDetails = "无法加载保存的详细信息。尝试再次打开该设备。"
    override val scanBeforeNaming = "保存名称之前扫描此设备。"

}
