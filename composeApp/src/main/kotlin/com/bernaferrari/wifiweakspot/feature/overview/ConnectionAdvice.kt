package com.bernaferrari.wifiweakspot.feature.overview

import com.bernaferrari.wifiweakspot.core.model.NetworkStatus

data class ConnectionAdvice(val title: String, val explanation: String)

fun connectionAdvice(status: NetworkStatus): ConnectionAdvice = when {
    !status.isConnected -> ConnectionAdvice(
        "Start with a Wi-Fi connection",
        "Connect to the network you want to check. You can still run internet tests and network tools using your current connection."
    )
    !status.internetValidated -> ConnectionAdvice(
        "Check internet access first",
        "Android has not confirmed internet access on this Wi-Fi connection. Check for a sign-in page, then test connectivity to your router and an internet host."
    )
    !status.hasSignalMeasurement -> ConnectionAdvice(
        "Read your Wi-Fi signal",
        "Allow Wi-Fi scans in Networks to see signal measurements. Compare signal in the room where you notice problems."
    )
    com.bernaferrari.wifiweakspot.core.wifi.SignalQuality.of(status.rssi).isProblem -> ConnectionAdvice(
        "Compare coverage near your router",
        "The current Wi-Fi signal is weak. Run a router check here, then repeat near the router. Use a survey of your own network to find weak spots."
    )
    else -> ConnectionAdvice(
        "Compare router responsiveness",
        "Signal strength looks usable and Android reports internet access. If the connection feels slow, compare router response here and near the router; strong signal alone does not guarantee performance."
    )
}
