package com.pulsewave.app.core.util

/** Short, friendly age such as "just now", "5 min ago", or "3 d ago". */
fun relativeTime(timestampMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
    val seconds = ((nowMillis - timestampMillis) / 1000).coerceAtLeast(0)
    return when {
        seconds < 45 -> "just now"
        seconds < 3_600 -> "${(seconds / 60).coerceAtLeast(1)} min ago"
        seconds < 86_400 -> "${seconds / 3_600} h ago"
        else -> "${seconds / 86_400} d ago"
    }
}
