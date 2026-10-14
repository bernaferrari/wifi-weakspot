package com.pulsewave.app.core.tools

/** Common-port scan storage also supports the original comma-separated format. */
internal fun encodePortScan(ports: List<Pair<Int, String>>): String =
    ports.joinToString(",") { "${it.first}:${it.second}" }

internal fun decodePortScan(value: String): List<Pair<Int, String>> = value.split(',').mapNotNull { entry ->
    val parts = entry.split(':', limit = 2)
    val port = parts.firstOrNull()?.toIntOrNull()?.takeIf { it in 1..65535 } ?: return@mapNotNull null
    port to parts.getOrElse(1) { "Unknown" }
}.distinctBy { it.first }
