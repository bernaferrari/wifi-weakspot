package com.bernaferrari.wifiweakspot.core.util

import com.bernaferrari.wifiweakspot.core.i18n.appStrings

/**
 * Short, friendly age such as "just now", "5 min ago", or "3 d ago". Non-breaking spaces keep
 * it on one line when it ends a wrapped sentence.
 */
fun relativeTime(timestampMillis: Long, nowMillis: Long = com.bernaferrari.wifiweakspot.core.util.nowMillis()): String {
    val seconds = ((nowMillis - timestampMillis) / 1000).coerceAtLeast(0)
    return when {
        seconds < 45 -> appStrings.common.justNow
        seconds < 3_600 -> appStrings.common.minutesAgo((seconds / 60).coerceAtLeast(1))
        seconds < 86_400 -> appStrings.common.hoursAgo(seconds / 3_600)
        else -> appStrings.common.daysAgo(seconds / 86_400)
    }
}
