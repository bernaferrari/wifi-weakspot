package com.bernaferrari.wifiweakspot.core.i18n.zhhans

import com.bernaferrari.wifiweakspot.core.i18n.CommonStrings

object ZhHansCommonStrings : CommonStrings {
    override fun deleteItemTitle(name: String) = "删除${name}？"

    override val couldNotSaveChanges = "无法保存更改，请重试。"
    override val couldNotDeleteItem = "无法删除此项目，请重试。"

    override val hiddenNetworkShort = "隐藏"
    override val network = "网络"
    override val tools = "工具"
    override val maker = "制作者"
    override val noReply = "没有回复"
    override val fastest = "最快"
    override val address = "地址"
    override val responseTime = "响应时间"
    override val checkAgain = "复查"
    override val rename = "重命名"
    override val stop = "停止"
    override val unknown = "未知"

    override val hiddenNetwork = "隐藏网络"
    override val fasterScans = "加快扫描"
    override val openWifiSettings = "打开 Wi-Fi 设置"
    override val signalUnavailable = "无法获取信号"
    override val devices = "设备"
    override val openPorts = "开放端口"
    override val nameLabel = "名称"
    override val historyTitle = "历史记录"
    override val routerCheck = "检查路由器"
    override val testWebsite = "测试网站"
    override val yourRouter = "你的路由器"
    override val yourNetwork = "你的网络"

    override val openInNewTab = "在新选项卡中打开"
    override val mapSharingAndroidOnly = "可以在Android 应用程序中共享地图。"
    override val back = "返回"
    override val close = "关闭"
    override val cancel = "取消"
    override val save = "保存"
    override val done = "完成"
    override val share = "分享"
    override val delete = "删除"

    override val tabHome = "首页"
    override val tabNetworks = "网络"
    override val tabRooms = "房间"
    override val tabSettings = "设置"

    override val permissionScreenTitle = "允许访问附近Wi-Fi"
    override val permissionAllowAccess = "允许访问"
    override val openAppSettings = "打开应用程序设置"

    override fun signalBadgeDescription(quality: String, reading: String) = "${quality}信号，${reading}"

    override val scanReady = "准备好了"
    override fun scansCount(count: Int) = "${count} 次扫描"

    override val scansLeftCaption = "剩余"
    override val scanWaitingCaption = "等待"
    override val scanDevMode = "开发模式"
    override val readyToScan = "准备扫描"
    override fun scansRemaining(count: Int) = "剩余 ${count} 次扫描"

    override fun nextScanIn(time: String) = "下一次扫描在 ${time}"
    override val checking = "检查..."
    override val justNow = "刚刚"
    override fun minutesAgo(minutes: Long) = "${minutes} 分钟前"

    override fun hoursAgo(hours: Long) = "${hours} 小时前"

    override fun daysAgo(days: Long) = "${days} 天前"

}
