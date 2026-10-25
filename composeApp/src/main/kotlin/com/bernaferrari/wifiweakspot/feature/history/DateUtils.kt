@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.bernaferrari.wifiweakspot.feature.history

import com.bernaferrari.wifiweakspot.core.i18n.appStrings
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    val hour = local.hour.toString().padStart(2, '0')
    val minute = local.minute.toString().padStart(2, '0')
    return "${local.date} $hour:$minute"
}

/** Compact stamp for file names, such as 20261013_0942. */
fun fileTimestamp(timestamp: Long = com.bernaferrari.wifiweakspot.core.util.nowMillis()): String {
    val local = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    fun two(v: Int) = v.toString().padStart(2, '0')
    return "${local.year}${two(local.month.ordinal + 1)}${two(local.day)}_${two(local.hour)}${two(local.minute)}"
}

/** "14:32" in local time. */
fun timeOfDay(timestamp: Long): String {
    val local = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
}

/** "Today", "Yesterday", "Monday" within a week, then "Mon, 12 Oct" (with the year once it differs). */
fun dayLabel(timestamp: Long, now: Long = com.bernaferrari.wifiweakspot.core.util.nowMillis()): String {
    val zone = TimeZone.currentSystemDefault()
    val day = Instant.fromEpochMilliseconds(timestamp).toLocalDateTime(zone).date
    val today = Instant.fromEpochMilliseconds(now).toLocalDateTime(zone).date
    val daysAgo = (today.toEpochDays() - day.toEpochDays()).toLong()
    val s = appStrings.history
    // DayOfWeek runs Monday..Sunday and Month January..December, matching the catalog lists.
    val weekdayShort = s.weekdayShortNames[day.dayOfWeek.ordinal]
    val month = s.monthShortNames[day.month.ordinal]
    return when {
        daysAgo == 0L -> s.today
        daysAgo == 1L -> s.yesterday
        daysAgo in 2L..6L -> s.weekdayNames[day.dayOfWeek.ordinal]
        day.year == today.year -> s.dateThisYear(weekdayShort, day.day, month)
        else -> s.dateOtherYear(weekdayShort, day.day, month, day.year)
    }
}

/** "Today at 14:32". */
fun momentLabel(timestamp: Long): String = appStrings.history.momentAt(dayLabel(timestamp), timeOfDay(timestamp))
