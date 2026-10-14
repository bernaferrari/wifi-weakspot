package com.pulsewave.app.feature.overview

import com.pulsewave.app.core.model.NetworkStatus

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
        "Allow Wi-Fi scans in Analyze to see signal measurements. Compare signal in the room where you notice problems."
    )
    status.rssi < -70 -> ConnectionAdvice(
        "Compare coverage near your router",
        "The current Wi-Fi signal is weak. Run a speed test here, then repeat near the router. Use a survey of your own network to find weak spots."
    )
    else -> ConnectionAdvice(
        "Check speed and responsiveness",
        "Signal strength looks usable and Android reports internet access. If the connection feels slow, compare a speed test and a ping test; strong signal alone does not guarantee performance."
    )
}
