package com.bernaferrari.wifiweakspot.core.designsystem

import com.bernaferrari.wifiweakspot.core.i18n.SettingsStrings
import com.materialkolor.PaletteStyle

/** Stable preference names, independent of the color engine's serialization. */
enum class ColorStyle(val palette: PaletteStyle) {
    Expressive(PaletteStyle.Expressive),
    Vibrant(PaletteStyle.Vibrant),
    Tonal(PaletteStyle.TonalSpot),
    Cmf(PaletteStyle.Cmf());

    fun label(s: SettingsStrings): String = when (this) {
        Expressive -> s.styleExpressive
        Vibrant -> s.styleVibrant
        Tonal -> s.styleTonal
        Cmf -> s.styleCmf
    }

    companion object {
        fun fromPreference(value: String?): ColorStyle = entries.firstOrNull { it.name == value } ?: Expressive
    }
}
