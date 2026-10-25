package com.bernaferrari.wifiweakspot.core.i18n.zhhans

import com.bernaferrari.wifiweakspot.core.i18n.ToolsStrings

object ZhHansToolsStrings : ToolsStrings {
    override val tryAgain = "重试"

    override val targetPlaceholder = "网站或IP地址"
    override val clearTarget = "清除"
    override val checkSectionTitle = "检查"

    override val recentSectionTitle = "最近检查"

    override val toolPortsDescription = "检查开放端口"

    override val pingQuestion = "是否有响应？"

    override val dnsQuestion = "IP 地址是什么？"

    override val dnsDetail = "DNS查找"
    override val portsQuestion = "提供哪些服务？"

    override val runningPortScanTitle = "正在检查端口"

    override val diagnosticFailed = "诊断失败"
    override val invalidTarget = "请输入主机名或 IP 地址，不要包含 URL 路径或空格。"

    override val diagnosticCouldNotComplete = "诊断无法完成。"
    override val connectionChangedRunAgain = "网络连接已更改，请在新网络上重新检查。"

    override val openItsWebPage = "打开其网页"

    override fun pingRepliedLabel(target: String, answered: Int, total: Int) = "${target} · ${total} 次中收到 ${answered} 次响应"

    override val slowest = "最慢"
    override fun repliesContentDescription(replies: String) = "回复：${replies}"

    override val pingDidntAnswer = "无响应"

    override val pingResponsive = "响应良好"

    override val pingResponsiveButUneven = "响应时间不稳定"

    override val pingDroppingReplies = "部分请求无响应"

    override val pingSlowToAnswer = "响应较慢"

    override val pingNoAnswerLocal = "设备可能已关机、进入休眠，或被设置为不响应 Ping。"

    override fun pingRepliesLost(lost: Int, total: Int) = "${total} 次请求中有 ${lost} 次未收到响应。请对比路由器检查和互联网主机测试的结果，帮助定位问题；仅凭此测试无法确定原因。"

    override val pingUneven = "响应时间不稳定。请对比当前位置和路由器附近的检查结果，再测试互联网主机。"

    override val pingLocalExplanation = "这是数据往返此设备所需的时间，目标设备本身也会影响结果。"

    override val pingInternetExplanation = "这是数据往返此主机所需的时间，网络连接、传输路径和主机状态都会影响结果。"

    override fun dnsAddressUnit(count: Int) = "个地址"

    override fun dnsForQuery(query: String) = "查询：${query}"

    override val dnsNotFound = "未找到地址"

    override val dnsFound = "已找到地址"

    override val dnsNotFoundExplanation = "请检查名称拼写并重试，该名称也可能尚未注册。"

    override fun dnsFoundExplanation(query: String) = "此设备能够查询到 ${query} 的地址。"

    override val dnsAddressesTitle = "IP 地址"

    override val portsOpenUnit = "个开放"

    override fun portsCommonServicesOn(host: String) = "${host}上的常用服务"
    override val portsNothingOpen = "没有开放端口"

    override fun portsOffersServices(count: Int) = "提供 ${count} 项服务"

    override val portsNoneAnswered = "常用服务均未响应。"

    override val portsOffersExplanation = "例如管理页面或文件共享服务。"

    override val portWebPage = "网页"
    override val portSecureWebPage = "安全网页"
    override val portRemoteLoginSsh = "远程登录(SSH)"
    override val portOldRemoteLoginTelnet = "旧远程登录 (Telnet)"
    override val portFileTransferFtp = "文件传输 (FTP)"
    override val portAddressLookupsDns = "域名解析（DNS）"

    override val portFileSharing = "文件共享"
    override val portMacFileSharing = "Mac 文件共享"
    override val portPrinting = "打印"
    override val portRemoteDesktop = "远程桌面"
    override val portScreenSharing = "屏幕共享"
    override val portDeviceDiscoveryUpnp = "设备发现 (UPnP)"
    override val portStorageOrAdminPage = "存储或管理页面"
    override val portDatabase = "数据库"
    override val portService = "服务"
    override val devicesLooking = "正在搜索设备…"

    override val devicesFindOnWifi = "查找 Wi-Fi 中的设备"

    override fun devicesOnNetworkNamed(count: Int, ssid: String) = "${ssid} 中发现 ${count} 台设备"

    override fun devicesOnYourNetwork(count: Int) = "你的网络中发现 ${count} 台设备"

    override val couldntLookForDevices = "无法搜索设备"

    override val scanFailed = "扫描失败"
    override val connectionChangedLookAgain = "网络连接已更改，请在此网络上重新搜索。"

    override val stoppedLooking = "搜索已停止"

    override val noDevicesFound = "未找到设备"
    override val noDevicesFoundSubtitle = "部分设备不会响应扫描。请稍后重试，或确认此设备已连接家中的 Wi-Fi。"

    override fun otherDevicesGroup(count: Int) = "其他设备 · ${count}"
    override fun quietDevices(count: Int) = "${count} 台类型未知的设备"

    override val quietDevicesSubtitle = "未能识别设备类型"

    override val hide = "收起"

    override val show = "展开"

    override fun checkingLocalAddresses(percent: Int) = "检查本地地址 · ${percent}%"
    override fun deviceDidntAnswer(detail: String) = "${detail} · 未接听"

    override val deviceNotScannedYet = "尚未在扫描中发现"

    override val deviceAnsweredLastScan = "上次扫描时有响应"

    override val deviceSeenButNoAnswer = "已在网络上发现，但未响应"

    override val actionCheckPorts = "检查端口"

    override val openRouterSettings = "打开路由器设置"
    override val detailsTitle = "细节"
    override val factNetworkName = "网络名称"
    override val factHardwareAddress = "硬件地址"
    override val macHiddenByAndroid = "Android 未提供此信息"

    override val factYourName = "你设置的名称"

    override val portsNotCheckedYet = "尚未检查"

    override fun portsNoneOpenWhenChecked(moment: String) = "在${moment}检查时，常用端口均未开放。"

    override fun portsCheckedAt(moment: String) = "检查时间：${moment}"

    override val renameDeviceTitle = "重命名设备"
    override val renameSupporting = "留空即可使用 WiFi Weakspot 发现的名称。"

    override val connectionChangedOpenAgain = "网络连接已更改，请在新网络上重新打开此设备的详情。"

    override val couldntLoadDetails = "无法加载保存的详细信息。尝试再次打开该设备。"
    override val scanBeforeNaming = "保存名称之前扫描此设备。"

}
