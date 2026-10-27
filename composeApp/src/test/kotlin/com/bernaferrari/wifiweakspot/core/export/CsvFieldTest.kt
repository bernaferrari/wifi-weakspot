package com.bernaferrari.wifiweakspot.core.export

import org.junit.Assert.assertEquals
import org.junit.Test

class CsvFieldTest {
    @Test fun quotesOrdinaryTextWithoutChangingItsValue() {
        assertEquals("\"Living room, \"\"TV\"\"\"", csvField("Living room, \"TV\""))
        assertEquals("\"\"", csvField(""))
        assertEquals("\"Office\nWi-Fi\"", csvField("Office\nWi-Fi"))
    }

    @Test fun marksFormulaLikeNamesAsTextIncludingHiddenPrefixes() {
        for (value in listOf("=1+1", "+1", "-1", "@name", "  =1", "\t=1", "\r@name", "\n+1", "\u0000=1", "\uFEFF=1", "\tordinary")) {
            assertEquals("\"'$value\"", csvField(value))
        }
    }
}
