package com.bernaferrari.wifiweakspot.core.i18n

import com.bernaferrari.wifiweakspot.core.monitoring.MonitorSignalLevel
import org.junit.Assert.*
import org.junit.Test

/** Regressions from ambiguous English words: reading, fair, open, and in the open. */
class TranslationMeaningTest {
    @Test fun storedUnknownPortMarkersUseEveryLanguagesLabel() {
        for (language in AppLanguage.entries) {
            val label = language.strings.common.unknown
            for (stored in listOf("unknown", "Unknown", "UNKNOWN", "")) {
                assertEquals(language.tag, label,
                    com.bernaferrari.wifiweakspot.feature.tools.serviceLabel(stored, label))
            }
            assertEquals("HTTPS", com.bernaferrari.wifiweakspot.feature.tools.serviceLabel("HTTPS", label))
        }
    }

    @Test fun signalAlertsUseTheSameScaleAsTheRestOfTheApp() {
        for (language in AppLanguage.entries) {
            val domain = language.strings.domain
            for ((level, label) in listOf(
                MonitorSignalLevel.Excellent to domain.qualityExcellentLabel,
                MonitorSignalLevel.Good to domain.qualityGoodLabel,
                MonitorSignalLevel.Fair to domain.qualityFairLabel,
                MonitorSignalLevel.Weak to domain.qualityWeakLabel
            )) {
                val alert = domain.signalChangeText("Café 東京", level, -72)
                assertTrue("${language.tag}: $alert", alert.contains(label))
                assertTrue(alert.contains("Café 東京"))
                assertTrue(alert.contains("-72"))
            }
        }
    }

    @Test fun routerPlacementExplicitlyMeansIndoors() {
        val indoorTerms = mapOf(
            "en" to "indoors", "es" to "dentro de casa", "pt-BR" to "dentro de casa",
            "fr" to "à l’intérieur", "de" to "im Innenraum", "it" to "in casa",
            "ru" to "в помещении", "ja" to "屋内", "ko" to "실내", "zh-Hans" to "室内"
        )
        for (language in AppLanguage.entries) {
            assertTrue(language.tag, language.strings.domain.tipRouterPlacement.contains(indoorTerms.getValue(language.tag)))
        }
    }

    @Test fun absentAndLatestReadingsReferToMeasurements() {
        val measurementTerms = mapOf(
            "en" to "measurement", "es" to "medición", "pt-BR" to "medição",
            "fr" to "mesure", "de" to "Messwert", "it" to "misurazione",
            "ru" to "измерени", "ja" to "測定値", "ko" to "측정값", "zh-Hans" to "读数"
        )
        for (language in AppLanguage.entries) {
            val s = language.strings.analyze
            val term = measurementTerms.getValue(language.tag)
            assertTrue(language.tag, s.historyPaused("Home", null, 2).contains(term, ignoreCase = true))
            assertTrue(language.tag, s.inspectedReading("-52 dBm", "Good", 0).contains(term, ignoreCase = true))
        }
    }

    @Test fun openPortUnitsAreStatusesWithNumberAgreement() {
        val es = AppLanguage.Spanish.strings.tools
        val pt = AppLanguage.PortugueseBrazil.strings.tools
        val fr = AppLanguage.French.strings.tools
        val it = AppLanguage.Italian.strings.tools
        val ru = AppLanguage.Russian.strings.tools
        assertEquals("abierto", es.portsOpenUnit(1))
        assertEquals("abiertos", es.portsOpenUnit(3))
        assertEquals("aberta", pt.portsOpenUnit(1))
        assertEquals("abertas", pt.portsOpenUnit(3))
        assertEquals("ouvert", fr.portsOpenUnit(1))
        assertEquals("ouverts", fr.portsOpenUnit(3))
        assertEquals("aperta", it.portsOpenUnit(1))
        assertEquals("aperte", it.portsOpenUnit(3))
        assertEquals("открытый порт", ru.portsOpenUnit(21))
        assertEquals("открытых порта", ru.portsOpenUnit(22))
        assertEquals("открытых портов", ru.portsOpenUnit(11))
    }
    @Test fun colorChoicesHaveDistinctAccessibleNames() {
        for (language in AppLanguage.entries) {
            val s = language.strings.settings
            val names = listOf(s.colorIndigo, s.colorOrange, s.colorAmber, s.colorGreen,
                s.colorTeal, s.colorCyan, s.colorBlue, s.colorPurple, s.colorPink)
            assertEquals(language.tag, names.size, names.toSet().size)
        }
    }

    @Test fun channelAbbreviationsAreReusedInFormattedReadings() {
        for (language in AppLanguage.entries) {
            val s = language.strings.analyze
            val channel = s.channelShort(36)
            assertTrue(language.tag, s.shareNetworkLine(-52, 36, "5 GHz").contains(channel))
            assertTrue(language.tag, s.selectedNetworkSummary("-52 dBm", "Good", 36).contains(channel))
            assertTrue(language.tag, s.scanReportLine("Café", -52, 36, "WPA3").contains(channel))
        }
        assertEquals("Can. 36", AppLanguage.PortugueseBrazil.strings.analyze.channelShort(36))
        assertEquals("Can. 36", AppLanguage.Italian.strings.analyze.channelShort(36))
    }

    @Test fun coverageAdviceMeansPhysicalMeasurements() {
        val measurementTerms = mapOf(
            AppLanguage.English to "measurements",
            AppLanguage.Spanish to "mediciones",
            AppLanguage.PortugueseBrazil to "medições",
            AppLanguage.French to "mesures",
            AppLanguage.German to "Messungen",
            AppLanguage.Italian to "misurazioni",
            AppLanguage.Russian to "измерения"
        )
        for ((language, term) in measurementTerms) {
            assertTrue(language.tag, language.strings.analyze.adviceWeakSignalBody.contains(term))
        }
    }

}
