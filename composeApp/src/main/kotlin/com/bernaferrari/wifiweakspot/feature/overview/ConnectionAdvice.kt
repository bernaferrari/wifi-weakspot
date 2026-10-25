package com.bernaferrari.wifiweakspot.feature.overview

import com.bernaferrari.wifiweakspot.core.i18n.appStrings
import com.bernaferrari.wifiweakspot.core.model.NetworkStatus

data class ConnectionAdvice(val title: String, val explanation: String)

fun connectionAdvice(status: NetworkStatus): ConnectionAdvice = with(appStrings.analyze) { when {
    !status.isConnected -> ConnectionAdvice(
        appStrings.survey.connectToYourWifi,
        adviceNoWifiBody
    )
    !status.internetValidated -> ConnectionAdvice(
        adviceNoInternetTitle,
        adviceNoInternetBody
    )
    !status.hasSignalMeasurement -> ConnectionAdvice(
        adviceNoSignalTitle,
        adviceNoSignalBody
    )
    com.bernaferrari.wifiweakspot.core.wifi.SignalQuality.of(status.rssi).isProblem -> ConnectionAdvice(
        adviceWeakSignalTitle,
        adviceWeakSignalBody
    )
    else -> ConnectionAdvice(
        adviceGoodSignalTitle,
        adviceGoodSignalBody
    )
} }
