package com.bernaferrari.wifiweakspot.core.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import com.bernaferrari.wifiweakspot.core.i18n.en.EnStrings

/**
 * Every user-facing string, one group per area of the app. Each language implements the whole
 * interface, so a missing translation is a compile error rather than an English leak.
 *
 * Plain text is a `val`; anything with numbers or names in it is a `fun`, so each language owns
 * its own word order and plural rules. Keep units, SSIDs, MAC addresses, and protocol names
 * (Wi-Fi, dBm, Mbps, DNS) out of the translations unless a language spells them differently.
 */
interface Strings {
    val common: CommonStrings
    val domain: DomainStrings
    val analyze: AnalyzeStrings
    val survey: SurveyStrings
    val history: HistoryStrings
    val tools: ToolsStrings
    val settings: SettingsStrings
}

/** A language the app ships, in the order the language picker would list it. */
enum class AppLanguage(val tag: String, val strings: Strings) {
    English("en", EnStrings),
    Spanish("es", com.bernaferrari.wifiweakspot.core.i18n.es.EsStrings),
    PortugueseBrazil("pt-BR", com.bernaferrari.wifiweakspot.core.i18n.ptbr.PtBrStrings),
    French("fr", com.bernaferrari.wifiweakspot.core.i18n.fr.FrStrings),
    German("de", com.bernaferrari.wifiweakspot.core.i18n.de.DeStrings),
    Italian("it", com.bernaferrari.wifiweakspot.core.i18n.it.ItStrings),
    Russian("ru", com.bernaferrari.wifiweakspot.core.i18n.ru.RuStrings),
    Japanese("ja", com.bernaferrari.wifiweakspot.core.i18n.ja.JaStrings),
    Korean("ko", com.bernaferrari.wifiweakspot.core.i18n.ko.KoStrings),
    ChineseSimplified("zh-Hans", com.bernaferrari.wifiweakspot.core.i18n.zhhans.ZhHansStrings),

}

/** The catalog for [locale], falling back to English for languages the app doesn't ship. */
fun stringsFor(locale: Locale = Locale.current): Strings =
    resolveCatalog(locale.toLanguageTag(), AppLanguage.entries.associate { it.tag to it.strings })

/** Match full BCP 47 tags before language fallbacks; never substitute a different script. */
internal fun resolveCatalog(tag: String, catalogs: Map<String, Strings>): Strings {
    fun normalized(value: String) = value.replace('_', '-').lowercase()
    fun script(parts: List<String>): String? = parts.drop(1).firstOrNull { it.length == 4 }
        ?: if (parts.firstOrNull() == "zh") {
            when {
                parts.any { it in setOf("tw", "hk", "mo") } -> "hant"
                parts.any { it in setOf("cn", "sg") } -> "hans"
                else -> null
            }
        } else null
    val requested = normalized(tag)
    val available = catalogs.entries.associate { normalized(it.key) to it.value }
    available[requested]?.let { return it }
    val parts = requested.split('-')
    val language = parts.first()
    val requestedScript = script(parts)
    if (requestedScript != null) available["$language-$requestedScript"]?.let { return it }
    available[language]?.let { return it }
    return available.entries.firstOrNull { (candidate, _) ->
        val candidateParts = candidate.split('-')
        candidateParts.first() == language &&
            (requestedScript == null || script(candidateParts) == null || script(candidateParts) == requestedScript)
    }?.value ?: EnStrings
}

/**
 * Strings for code outside composition (view models, services, exports). Reads the current
 * locale on every call, so a language change applies the next time the text is built.
 */
val appStrings: Strings get() = stringsFor()

val LocalStrings = staticCompositionLocalOf { stringsFor() }

/** Strings inside composition: `strings.analyze.title`. */
val strings: Strings
    @Composable @ReadOnlyComposable get() = LocalStrings.current
