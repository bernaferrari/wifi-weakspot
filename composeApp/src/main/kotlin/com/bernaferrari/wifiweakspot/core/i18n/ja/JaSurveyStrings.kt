package com.bernaferrari.wifiweakspot.core.i18n.ja

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object JaSurveyStrings : SurveyStrings {
    override val horizontalPosition = "左からの位置"
    override val verticalPosition = "上からの位置"
    override val measureHere = "ここで測定"
    override val recheckReading = "ここで再測定"
    override val moveLeft = "左に移動"
    override val moveRight = "右に移動"
    override val moveUp = "上に移動"
    override val moveDown = "下に移動"
    override fun mapPosition(x: Int, y: Int) = "位置：左から$x%、上から$y%"
    override fun pointName(id: Long) = "測定値 $id"

    override val measuringHoldStill = "測定中です。そのまま動かずにお待ちください"

    override val tapWhereYouAreStanding = "間取り図で今いる場所をタップしてください"

    override val chooseWifi = "Wi-Fi を選択してください"
    override val undoLastReading = "直前の測定を取り消す"

    override val allReadings = "すべての測定値"
    override val floorPlanTitle = "間取り図"
    override fun readingCount(count: Int) = "測定値${count}件"

    override val mapOptions = "地図オプション"
    override val usePlanPhoto = "間取り図の写真を使う"

    override val changePhoto = "写真を変更"

    override val removePhoto = "写真を削除"

    override val exportReadingsCsv = "測定値をCSVでエクスポート"

    override fun floorName(floor: Int) = if (floor == 0) "地下" else "${floor}階"

    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "フロア ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "地下階を追加" else "${floor}階を追加"

    override val deleteFloorReadingsAction = "この階の測定値を削除"

    override fun deleteFloorBody(count: Int) = "この階の測定値${count}件を削除します。"

    override val oneRouterAtATime = "1つのマップでは1つのアクセスポイントを測定します。メッシュWi-Fiでは、各機器が個別に表示されます。"

    override val noNetworksFound = "ネットワークが見つかりません。Wi-Fiと位置情報がオンになっているか確認してください。"

    override val scanning = "スキャン中…"
    override val refresh = "更新"

    override val switchWifiTitle = "別の Wi-Fi に切り替えますか?"
    override val switchWifiBody = "このマップのすべての階の測定値が消去されます。両方を残す場合は、新しいマップを作成してください。"

    override val clearAndSwitch = "クリアして切り替え"

    override val deleteMapBody = "各フロアのすべての測定値が削除されます。"

    override val noReadingsYet = "まだ測定値がありません。間取り図で今いる場所をタップしてください。"

    override val weakestFirst = "信号が弱い順に表示しています。問題のある場所を先に確認できます。"

    override val mapYourHome = "自宅の地図を作成する"
    override val mapYourHomeBody = "間取り図にWi-Fiの電波状況を表示できます。間取り図の写真、または空のグリッドを使えます。"

    override val createMap = "地図を作成する"

    override val namePlaceholder = "2階、書斎など"

    override val floorplanDescription = "測定用の間取り図"

    override fun coverageMapDescription(count: Int) = "測定値${count}件のWi-Fiカバレッジマップです。今いる場所をタップして測定してください。"

    override val networkNotSeenHere = "ここでは対象のWi-Fiが見つかりませんでした。測定値は保存していません。"

    override val projectChangedWhileScanning = "スキャン中にマップが切り替わったため、測定値は保存していません。"

    override val couldNotSaveReading = "測定値を保存できませんでした。他の測定値は保持されています。"

    override fun defaultMapName(index: Int) = if (index == 1) "自宅" else "自宅 ${index}"

    override val stillSavingMap = "マップを保存しています。少しお待ちください。"

    override val noFreshScan = "Androidから新しいスキャン結果を取得できませんでした。少し待ってから再試行してください。"

    override val couldNotScan = "スキャンできませんでした。Wi-Fiと位置情報の設定を確認してください。"

    override val chooseAccessPointFirst = "カバレッジを測定するアクセスポイントを選択してください。"

    override val saveMeasurementBeforeExport = "エクスポートする前に、少なくとも 1 つの測定値を保存してください。"
    override val couldNotExportProject = "マップをエクスポートできませんでした。もう一度お試しください。"

    override val connectToYourWifi = "Wi-Fi に接続してください"
    override fun checkingRoom(room: String) = "${room}を測定中です。そのまま動かずにお待ちください"

    override val connectedWifi = "接続中のWi-Fi"
    override val connectedNoReading = "Wi-Fiには接続していますが、信号の測定値をまだ取得できません。"

    override val connectToCheck = "測定したいWi-Fiに接続してください。"

    override fun removedRoom(room: String) = "${room} を削除しました"
    override val undo = "元に戻す"
    override val roomsEmptyHint = "部屋で静止して測定値を保存し、別の部屋でも測定して、信号が弱くなる場所を比べてください。"

    override val noWifiSignal = "Wi-Fi 信号がありません"
    override val saveRoom = "部屋の測定を保存"

    override fun dbStronger(delta: Int) = "${delta} dB強くなりました"

    override fun dbWeaker(delta: Int) = "${delta} dB弱くなりました"

    override val savedRooms = "測定済みの部屋"

    override fun deleteRoom(room: String) = "${room} を削除"
    override fun roomDescription(room: String, quality: String, reading: String) = "${room}、信号：${quality}、${reading}。測定値を確認し、この場所を再測定できます。"

    override val noSignalToSave = "Wi-Fiの信号を取得できません。先にWi-Fiに接続してください。"

    override val allowScansToLink = "「ネットワーク」でWi-Fiスキャンを許可すると、測定値をネットワークに関連付けられます。"

    override val connectToSpotNetwork = "再測定する前に、この場所を測定したときのネットワークに接続してください。"

    override fun savedRoom(room: String) = "${room}を保存しました"

    override val couldNotSaveStableReading = "安定した測定値を保存できませんでした。同じWi-Fiに接続したまま、動かずに再試行してください。"

    override val whereAreYou = "どこにいますか？"
    override val somewhereElse = "別の場所"

    override fun savedAt(time: String) = "${time}に保存"

    override val moveRouterTip = "ルーターをより高い位置、より中央、またはこの部屋の近くに移動してみてください。"
    override val betterThanLastTime = "前回より改善"

    override val worseThanLastTime = "前回より悪化"

    override val sameAsLastTime = "前回と同じ"
    override val earlierChecks = "以前の測定"

    override val remove = "削除"
    override fun connectToRecheck(ssid: String?) =
        "${ssid?.ifBlank { null } ?: "同じ Wi-Fi"} に接続して再度確認してください。"

    override val stepDeveloperOptionsTitle = "開発者向けオプションをオンにする"
    override val stepDeveloperOptionsBody = "「設定」の**デバイス情報**で、**ビルド番号**を7回タップします。画面ロックの認証を求められる場合があります。"

    override val openAboutPhone = "「デバイス情報」を開く"

    override val stepThrottlingTitle = "Wi-Fiスキャンの回数制限を解除"

    override val stepThrottlingBody = "**開発者向けオプション**の**ネットワーク**で、**Wi‑Fi スキャン スロットリング**をオフにします。項目名は機種によって異なる場合があります。"

    override val openDeveloperOptions = "開発者向けオプションを開く"
    override val stepComeBackTitle = "アプリに戻る"

    override val stepComeBackBody = "アプリに戻ると、WiFi Weakspotが設定を自動的に確認します。回数制限はいつでも元に戻せます。"

    override val hideComputerMethod = "パソコンでの手順を閉じる"

    override val useAdbInstead = "パソコンからADBで設定する"

    override val adbCommandClipLabel = "ADB コマンド"
    override val hideRootOption = "rootを使う方法を閉じる"

    override val useRootInstead = "root権限を使って設定する"

    override val scanLimitFootnote = "Androidは電池消費を抑えるためにスキャン回数を制限します。制限はいつでも元に戻せます。"

    override val unlimitedWithRootTitle = "rootでスキャン回数を無制限に"

    override val unlimitedOnTitle = "スキャン回数は無制限です"

    override val unlimitedOnBody = "近くのネットワークやチャンネルグラフが数秒ごとに更新されます。"

    override val scansLimitedTitle = "スキャン回数に制限があります"

    override val scansLimitedAndroid9Body = "Android 9では、スキャンは2分間に4回までです。この制限は解除できません。WiFi Weakspotは待機中に前回の結果を表示します。"

    override val usingRoot = "rootを使用中"

    override val deviceRooted = "root権限を利用できます"

    override val rootScanningBody = "システムシェルからスキャンするため、開発者向けオプションを変更する必要はありません。"

    override val rootDeniedBody = "root権限が許可されませんでした。root管理アプリでWiFi Weakspotを許可して再試行するか、下の開発者向けオプションの手順を使ってください。"

    override val rootNotNeededBody = "スキャン回数はすでに無制限です。root権限は必要ありません。"

    override val rootOfferBody = "root権限を使うと、開発者向けオプションを変更せずにスキャンできます。root管理アプリが一度だけ許可を求めます。"

    override val stopUsingRoot = "rootの使用を停止"

    override val waitingForRootManager = "root管理アプリの応答を待っています…"

    override val tryRootAgain = "rootで再試行"

    override val useRoot = "rootを使う"

    override val copied = "コピーしました"
    override val copyCommand = "コマンドをコピー"

}
