package com.bernaferrari.wifiweakspot.core.util

/**
 * Short, friendly age such as "just now", "5 min ago", or "3 d ago". Non-breaking spaces keep
 * it on one line when it ends a wrapped sentence.
 */
fun relativeTime(timestampMillis: Long, nowMillis: Long = com.bernaferrari.wifiweakspot.core.util.nowMillis()): String {
    val seconds = ((nowMillis - timestampMillis) / 1000).coerceAtLeast(0)
    return when {
        seconds < 45 -> "just\u00A0now"
        seconds < 3_600 -> "${(seconds / 60).coerceAtLeast(1)}\u00A0min\u00A0ago"
        seconds < 86_400 -> "${seconds / 3_600}\u00A0h\u00A0ago"
        else -> "${seconds / 86_400}\u00A0d\u00A0ago"
    }
}
