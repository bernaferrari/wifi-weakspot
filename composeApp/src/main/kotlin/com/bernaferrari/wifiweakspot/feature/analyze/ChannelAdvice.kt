package com.bernaferrari.wifiweakspot.feature.analyze

internal data class ChannelHint(val channel: Int, val label: String)

/** Equal scores never justify telling someone to change their router's channel. */
internal fun channelHint(scores: List<ChannelScore>, currentChannel: Int?): ChannelHint? {
    val best = scores.firstOrNull { it.recommendation } ?: return null
    val current = scores.firstOrNull { it.channelNumber == currentChannel }
    return if (current != null && current.score <= best.score) {
        ChannelHint(current.channelNumber, if (current.rating == TrafficRating.LOW) {
            "Your channel looks clear"
        } else "No improvement found in this scan")
    } else if (current != null) {
        ChannelHint(best.channelNumber, "Less overlap than channel ${current.channelNumber}")
    } else {
        ChannelHint(best.channelNumber, "Least overlap in this scan")
    }
}
