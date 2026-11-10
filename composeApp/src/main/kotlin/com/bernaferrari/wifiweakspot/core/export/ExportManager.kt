package com.bernaferrari.wifiweakspot.core.export

fun interface ExportManager {
    suspend fun exportCsv(lines: List<String>, fileName: String)
}

/** Quote a text field and keep untrusted names from being interpreted as spreadsheet formulas.
 * The CSV apostrophe is intentional text protection.
 */
fun csvField(value: String): String {
    val firstMeaningful = value.firstOrNull { !it.isWhitespace() && !it.isISOControl() && it != '\uFEFF' }
    val unsafe = firstMeaningful in listOf('=', '+', '-', '@') ||
        value.firstOrNull() in listOf('\t', '\r', '\n')
    val text = if (unsafe) "'$value" else value
    return "\"${text.replace("\"", "\"\"")}\""
}
