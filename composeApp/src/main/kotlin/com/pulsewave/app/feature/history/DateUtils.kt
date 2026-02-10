@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.pulsewave.app.feature.history

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

fun formatDate(timestamp: Long): String {
    val instant = Instant.fromEpochMilliseconds(timestamp)
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "${local.date} ${local.hour}:${local.minute}"
}
