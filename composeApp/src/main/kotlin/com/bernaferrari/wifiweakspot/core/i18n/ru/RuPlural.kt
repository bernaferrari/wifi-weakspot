package com.bernaferrari.wifiweakspot.core.i18n.ru

/** Russian cardinal forms for non-negative UI counts. */
internal fun plural(count: Int, one: String, few: String, many: String): String = when {
    count % 100 in 11..14 -> many
    count % 10 == 1 -> one
    count % 10 in 2..4 -> few
    else -> many
}
