package com.bernaferrari.wifiweakspot.core.i18n.ja

import com.bernaferrari.wifiweakspot.core.i18n.SurveyStrings

object JaSurveyStrings : SurveyStrings {
    override val measuringHoldStill = "測定中…静止してください"
    override val tapWhereYouAreStanding = "あなたが立っている場所の地図をタップします"
    override val chooseWifi = "Wi-Fi を選択してください"
    override val undoLastReading = "最後の読み取りを元に戻す"
    override val allReadings = "すべての測定値"
    override val floorPlanTitle = "間取り図"
    override fun readingCount(count: Int) = "${count} 件の測定値"

    override val mapOptions = "地図オプション"
    override val usePlanPhoto = "計画の写真を使用する"
    override val changePhoto = "写真を変更します"
    override val removePhoto = "写真を削除してください"
    override val exportReadingsCsv = "測定値のエクスポート (CSV)"
    override fun floorName(floor: Int) = if (floor == 0) "地下室" else "フロア ${floor}"
    override fun floorShortLabel(floor: Int) = if (floor == 0) "B" else "${floor}"
    override fun floorButtonLabel(floor: Int) = "フロア ${floorShortLabel(floor)}"
    override fun addFloor(floor: Int) = if (floor == 0) "地下室を追加" else "フロアを追加 ${floor}"
    override val deleteFloorReadingsAction = "このフロアの測定値を削除します"
    override fun deleteFloorBody(count: Int) =
        if (count == 1) "1 件の読み取り値が削除されます。" else "${count} の読み取り値は削除されます。"

    override val oneRouterAtATime = "マップは一度に 1 台のルーターに従います。メッシュ システムでは各ユニットが個別に表示されます。"
    override val noNetworksFound = "ネットワークがまだ見つかりません。 Wi-Fi と位置情報がオンになっていることを確認します。"
    override val scanning = "スキャン中…"
    override val refresh = "リフレッシュ"
    override val switchWifiTitle = "別の Wi-Fi に切り替えますか?"
    override val switchWifiBody = "これにより、このマップの各フロアの測定値がクリアされます。両方を保持するには、新しいマップを作成します。"
    override val clearAndSwitch = "クリアして切り替え"

    override val deleteMapBody = "各フロアのすべての測定値が削除されます。"

    override val noReadingsYet = "ここにはまだ読み取り値がありません。あなたが立っている場所の地図をタップします。"
    override val weakestFirst = "最初に最も弱いので、問題のある箇所が上になります。"
    override val mapYourHome = "自宅の地図を作成する"
    override val mapYourHomeBody = "自宅の地図であなたの Wi-Fi を確認してください。計画の写真または空白のグリッドを使用します。"
    override val createMap = "地図を作成する"

    override val namePlaceholder = "上の階はオフィス…"

    override val floorplanDescription = "プロジェクトのフロアプラン"
    override fun coverageMapDescription(count: Int) =
        "${readingCount(count)} のカバレッジ マップ。立っている場所をタップして測定します。"

    override val networkNotSeenHere = "そのWi-Fiはここには現れませんでした。何も救われませんでした。"
    override val projectChangedWhileScanning = "スキャン中にプロジェクトが変更されました。ポイントは貯まらなかった。"
    override val couldNotSaveReading = "測定値を保存できませんでした。他の測定値は安全です。"
    override fun defaultMapName(index: Int) = if (index == 1) "ホーム" else "ホーム ${index}"
    override val stillSavingMap = "マップを保存中です。"
    override val noFreshScan = "Android は新しいスキャンを提供しませんでした。しばらくしてからもう一度お試しください。"
    override val couldNotScan = "スキャンできませんでした。 Wi-Fi と位置情報の設定を確認してください。"
    override val chooseAccessPointFirst = "カバレッジをマッピングする前に、アクセス ポイントを選択してください。"
    override val saveMeasurementBeforeExport = "エクスポートする前に、少なくとも 1 つの測定値を保存してください。"
    override val couldNotExportProject = "このプロジェクトをエクスポートできませんでした。もう一度やり直してください。"

    override val connectToYourWifi = "Wi-Fi に接続してください"
    override fun checkingRoom(room: String) = "${room} を確認中…じっとしててください"
    override val connectedWifi = "接続中のWi-Fi"
    override val connectedNoReading = "デバイスは接続されていますが、信号の読み取りはまだ利用できません。"
    override val connectToCheck = "確認したいWi-Fiに接続します。"
    override fun removedRoom(room: String) = "${room} を削除しました"
    override val undo = "元に戻す"
    override val roomsEmptyHint =
        "部屋に立って測定値を保存します。別の部屋をチェックして、信号が弱くなる場所を見つけてください。"
    override val noWifiSignal = "Wi-Fi 信号がありません"
    override val saveRoom = "部屋を保存する"
    override fun dbStronger(delta: Int) = "${delta} dB 強くなります"
    override fun dbWeaker(delta: Int) = "${delta} dB 弱くなります"
    override val savedRooms = "保存された部屋"
    override fun deleteRoom(room: String) = "${room} を削除"
    override fun roomDescription(room: String, quality: String, reading: String) =
        "${room}、${quality} 信号、${reading}。読書を見直して、この場所を再確認してください。"

    override val noSignalToSave = "保存する Wi-Fi 信号がありません。まず、Wi-Fi に接続してください。"
    override val allowScansToLink = "ネットワークでの Wi-Fi スキャンを許可して、この測定値をネットワークにリンクできるようにします。"
    override val connectToSpotNetwork = "再確認する前に、このスポットのネットワークに接続してください。"
    override fun savedRoom(room: String) = "保存しました ${room}"
    override val couldNotSaveStableReading = "安定した測定値を保存できませんでした。同じ Wi-Fi をそのままにして、もう一度試してください。"
    override val whereAreYou = "どこにいますか？"
    override val somewhereElse = "別の場所"

    override fun savedAt(time: String) = "保存しました ${time}"
    override val moveRouterTip = "ルーターをより高い位置、より中央、またはこの部屋の近くに移動してみてください。"
    override val betterThanLastTime = "前回よりも良くなりました"
    override val worseThanLastTime = "前回よりも悪い"
    override val sameAsLastTime = "前回と同じ"
    override val earlierChecks = "以前のチェック"
    override val remove = "削除"
    override fun connectToRecheck(ssid: String?) =
        "${ssid?.ifBlank { null } ?: "同じ Wi-Fi"} に接続して再度確認してください。"

    override val stepDeveloperOptionsTitle = "開発者向けオプションをオンにする"
    override val stepDeveloperOptionsBody =
        "[設定] で、**端末情報** を開き、**ビルド番号** を 7 回タップします。画面ロックを入力する必要がある場合があります。"
    override val openAboutPhone = "電話についてを開く"
    override val stepThrottlingTitle = "Wi-Fi スキャンスロットリングをオフにする"
    override val stepThrottlingBody =
        "**開発者向けオプション**で、**ネットワーク**まで下にスクロールし、**Wi-Fiスキャン調整**をオフにします。"
    override val openDeveloperOptions = "開発者向けオプションを開く"
    override val stepComeBackTitle = "ここに戻ってください"
    override val stepComeBackBody =
        "WiFi Weakspot は、戻るときに自動的にチェックされます。いつでもスロットルをオンに戻すことができます。"
    override val hideComputerMethod = "コンピューターを隠す方法"
    override val useAdbInstead = "コンピューターの方が好きですか? ADBを使用してください"
    override val adbCommandClipLabel = "ADB コマンド"
    override val hideRootOption = "root オプションを非表示"
    override val useRootInstead = "root化されたデバイスですか?代わりに root を使用してください"
    override val scanLimitFootnote = "Android バッテリーを節約するためにスキャンを制限します。いつでも制限をオンに戻すことができます。"
    override val unlimitedWithRootTitle = "root で無制限にスキャン"
    override val unlimitedOnTitle = "無制限のスキャンがオンになっています"
    override val unlimitedOnBody = "近くのネットワークとチャネルグラフは数秒ごとに更新されます。"
    override val scansLimitedTitle = "スキャンは制限されています"
    override val scansLimitedAndroid9Body =
        "Android 9 では 2 分ごとに 4 回のスキャンが可能ですが、これをオフにすることはできません。 WiFi Weakspot はその間の最近の結果を再利用します。"
    override val usingRoot = "rootの使用"
    override val deviceRooted = "デバイスはルート化されています"
    override val rootScanningBody = "スキャンはシステム シェルを経由するため、開発者向けオプションで変更するものは何もありません。"
    override val rootDeniedBody =
        "root アクセスが拒否されました。 root マネージャーで WiFi Weakspot を許可して再試行するか、以下の開発者向けオプションを使用してください。"
    override val rootNotNeededBody = "スキャンはすでに無制限です。ルートは必要ありません。"
    override val rootOfferBody =
        "開発者向けオプションをスキップし、WiFi Weakspot で root をスキャンします。 root マネージャーが一度尋ねます。"
    override val stopUsingRoot = "root の使用をやめる"
    override val waitingForRootManager = "root マネージャーを待っています…"
    override val tryRootAgain = "root をもう一度お試しください"
    override val useRoot = "rootを使用してください"
    override val copied = "コピーしました"
    override val copyCommand = "コピーコマンド"
}
