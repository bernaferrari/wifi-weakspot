@file:OptIn(kotlin.time.ExperimentalTime::class, kotlin.uuid.ExperimentalUuidApi::class)

package com.pulsewave.app.core.util

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToLong

/** Wall-clock time in epoch milliseconds, on every platform. */
fun nowMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

fun randomId(): String = kotlin.uuid.Uuid.random().toString()

/** Fixed-point formatting without JVM-only String.format, e.g. 3.14159 to "3.1". */
fun Number.fixed(decimals: Int): String {
    val value = toDouble()
    if (!value.isFinite()) return value.toString()
    if (decimals <= 0) return value.roundToLong().toString()
    val factor = 10.0.pow(decimals)
    val scaled = (abs(value) * factor).roundToLong()
    val whole = scaled / factor.toLong()
    val fraction = (scaled % factor.toLong()).toString().padStart(decimals, '0')
    return (if (value < 0 && scaled != 0L) "-" else "") + "$whole.$fraction"
}

fun Number.twoDigits(): String = toLong().toString().padStart(2, '0')

/** Percent-encodes a value for a navigation query string. */
fun encodeQuery(value: String): String = buildString {
    value.encodeToByteArray().forEach { byte ->
        val c = byte.toInt().toChar()
        if (byte >= 0 && (c.isLetterOrDigit() || c in "-._~")) append(c)
        else append('%').append((byte.toInt() and 0xFF).toString(16).uppercase().padStart(2, '0'))
    }
}
