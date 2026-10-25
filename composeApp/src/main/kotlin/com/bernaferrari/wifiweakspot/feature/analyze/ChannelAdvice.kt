package com.bernaferrari.wifiweakspot.feature.analyze

import com.bernaferrari.wifiweakspot.core.i18n.appStrings

internal data class ChannelHint(val channel: Int, val label: String)

/** The explanation keeps the same scan comparison that the person tapped. */
internal data class ChannelAdviceSnapshot(val hint: ChannelHint, val band: FrequencyBand, val scores: List<ChannelScore>)

/** Equal scores never justify telling someone to change their router's channel. */
internal fun channelHint(scores: List<ChannelScore>, currentChannel: Int?): ChannelHint? {
    val s = appStrings.analyze
    val best = scores.firstOrNull { it.recommendation } ?: return null
    val current = scores.firstOrNull { it.channelNumber == currentChannel }
    return if (current != null && current.score <= best.score) {
        ChannelHint(current.channelNumber, if (current.rating == TrafficRating.LOW) {
            s.channelLooksClear
        } else s.noChannelImprovement)
    } else if (current != null) {
        ChannelHint(best.channelNumber, s.lessOverlapThanChannel(current.channelNumber))
    } else {
        ChannelHint(best.channelNumber, s.suggestedChannel)
    }
}
