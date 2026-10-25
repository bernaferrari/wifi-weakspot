package com.bernaferrari.wifiweakspot.core.i18n

import com.bernaferrari.wifiweakspot.core.i18n.en.EnStrings
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.*
import org.junit.Test

class StringsTest {
    private fun catalog() = object : Strings by EnStrings {}

    @Test fun regionTagsResolveBeforeLanguageFallbacks() {
        val portuguese = catalog()
        val british = catalog()
        val catalogs = linkedMapOf("en-GB" to british, "en" to EnStrings, "pt-BR" to portuguese)
        assertSame(portuguese, resolveCatalog("pt-BR", catalogs))
        assertSame(portuguese, resolveCatalog("PT_br", catalogs))
        assertSame(portuguese, resolveCatalog("pt-PT", catalogs))
        assertSame(british, resolveCatalog("en-GB", catalogs))
        assertSame(EnStrings, resolveCatalog("en-US", catalogs))
        assertSame(EnStrings, resolveCatalog("ar-SA", catalogs))
    }

    @Test fun chineseScriptsAreNotInterchangeable() {
        val simplified = catalog()
        val traditional = catalog()
        val catalogs = mapOf("zh-Hans" to simplified, "zh-Hant" to traditional)
        assertSame(simplified, resolveCatalog("zh-Hans-CN", catalogs))
        assertSame(simplified, resolveCatalog("zh-CN", catalogs))
        assertSame(traditional, resolveCatalog("zh-TW", catalogs))
        assertSame(traditional, resolveCatalog("zh-Hant-HK", catalogs))
        assertSame(EnStrings, resolveCatalog("zh-Hant", mapOf("zh-Hans" to simplified)))
    }

    @Test fun androidOnlyAdvertisesCompleteCatalogs() {
        val file = File("../androidApp/src/main/res/xml/locales_config.xml")
        val factory = DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
        val nodes = factory.newDocumentBuilder().parse(file).getElementsByTagName("locale")
        val tags = (0 until nodes.length).map {
            nodes.item(it).attributes.getNamedItemNS("http://schemas.android.com/apk/res/android", "name").nodeValue
        }
        assertEquals(AppLanguage.entries.map { it.tag }.toSet(), tags.toSet())
        assertEquals(tags.size, tags.toSet().size)
    }

    @Test fun everyCatalogProvidesTextAndStableCalendarAndRoomLists() {
        for (language in AppLanguage.entries) {
            val s = language.strings
            assertEquals(language.tag, 7, s.history.weekdayNames.size)
            assertEquals(language.tag, 7, s.history.weekdayShortNames.size)
            assertEquals(language.tag, 12, s.history.monthShortNames.size)
            assertEquals(language.tag, 12, s.domain.roomSuggestions.size)
            // Public interface getters cover every plain string without duplicating the catalog keys.
            for ((type, group) in listOf(
                CommonStrings::class.java to s.common, DomainStrings::class.java to s.domain,
                AnalyzeStrings::class.java to s.analyze, SurveyStrings::class.java to s.survey,
                HistoryStrings::class.java to s.history, ToolsStrings::class.java to s.tools,
                SettingsStrings::class.java to s.settings
            )) {
                for (getter in type.methods.filter { it.parameterCount == 0 }) {
                    when (val value = getter.invoke(group)) {
                        is String -> assertTrue("${language.tag}.${getter.name}", value.isNotBlank())
                        is List<*> -> assertTrue("${language.tag}.${getter.name}", value.all { it is String && it.isNotBlank() })
                    }
                }
            }
            for (count in listOf(0, 1, 2, 5, 11, 21, 101)) {
                assertTrue(s.common.scansCount(count).isNotBlank())
                assertTrue(s.analyze.networksOnBand(count, "5 GHz").contains("5 GHz"))
                assertTrue(s.history.devicesFound(count).isNotBlank())
            }
            val network = "Café 東京 <&>"
            assertTrue(s.analyze.detailsForNetwork(network).contains(network))
            assertTrue(s.tools.dnsForQuery(network).contains(network))
        }
    }
    @Test fun allTenRequestedLanguagesAreRegistered() {
        assertEquals(setOf("en", "es", "pt-BR", "fr", "de", "it", "ru", "ja", "ko", "zh-Hans"),
            AppLanguage.entries.map { it.tag }.toSet())
        for (language in AppLanguage.entries) {
            assertSame(language.strings, stringsFor(androidx.compose.ui.text.intl.Locale(language.tag)))
        }
    }

    @Test fun russianCountsUseOneFewAndManyForms() {
        val s = AppLanguage.Russian.strings
        for ((count, noun) in mapOf(0 to "сканирований", 1 to "сканирование", 2 to "сканирования",
            5 to "сканирований", 11 to "сканирований", 14 to "сканирований",
            21 to "сканирование", 22 to "сканирования", 101 to "сканирование")) {
            assertEquals("$count $noun", s.common.scansCount(count))
        }
        assertEquals("адрес", s.tools.dnsAddressUnit(21))
        assertEquals("адреса", s.tools.dnsAddressUnit(22))
        assertEquals("адресов", s.tools.dnsAddressUnit(11))
    }

    @Test fun eastAsianCountsAndDatesUseNaturalOrder() {
        val ja = AppLanguage.Japanese.strings
        val ko = AppLanguage.Korean.strings
        val zh = AppLanguage.ChineseSimplified.strings
        assertEquals("10月24日（土）", ja.history.dateThisYear("土", 24, "10月"))
        assertEquals("2026년 10월 24일 (토)", ko.history.dateOtherYear("토", 24, "10월", 2026))
        assertEquals("10月24日（周六）", zh.history.dateThisYear("周六", 24, "10月"))
        assertEquals("example.com · 4 回中 3 回応答", ja.tools.pingRepliedLabel("example.com", 3, 4))
        assertEquals("example.com · 4회 중 3회 응답", ko.tools.pingRepliedLabel("example.com", 3, 4))
        assertEquals("example.com · 4 次中收到 3 次响应", zh.tools.pingRepliedLabel("example.com", 3, 4))
        assertEquals("2 次扫描", zh.common.scansCount(2))
    }

    @Test fun formattedMessagesPreserveInputsAndContainNoDraftMarkers() {
        for (language in AppLanguage.entries) {
            val s = language.strings
            for ((type, group) in listOf(
                CommonStrings::class.java to s.common, DomainStrings::class.java to s.domain,
                AnalyzeStrings::class.java to s.analyze, SurveyStrings::class.java to s.survey,
                HistoryStrings::class.java to s.history, ToolsStrings::class.java to s.tools,
                SettingsStrings::class.java to s.settings
            )) {
                for (method in type.methods.filter { it.parameterCount > 0 }) {
                    for (count in listOf(0, 1, 2, 5, 11, 21, 101)) {
                        val inputs = method.parameterTypes.mapIndexed { index, parameter ->
                            when {
                                parameter == String::class.java -> "P${index}Café東京"
                                parameter == Int::class.javaPrimitiveType || parameter == Int::class.javaObjectType -> count
                                parameter == Long::class.javaPrimitiveType -> count.toLong()
                                parameter == Boolean::class.javaPrimitiveType -> count % 2 == 0
                                parameter == List::class.java -> listOf("Café", "東京", "Sala")
                                parameter.isEnum -> parameter.enumConstants.first()
                                else -> error("Add fixture for $parameter")
                            }
                        }
                        val message = method.invoke(group, *inputs.toTypedArray()) as String
                        val label = "${language.tag}.${method.name}($count): $message"
                        assertTrue(label, message.isNotBlank())
                        assertFalse(label, message.contains('⟪') || message.contains('⟦') || message.contains("PORT_SCAN"))
                        for (input in inputs.filterIsInstance<String>()) {
                            assertTrue(label, message.contains(input, ignoreCase = true))
                        }
                    }
                }
            }
        }
    }

}
