package com.bernaferrari.wifiweakspot.core.designsystem

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

internal fun contrastRatio(first: Color, second: Color): Float {
    val a = first.luminance()
    val b = second.luminance()
    return (maxOf(a, b) + 0.05f) / (minOf(a, b) + 0.05f)
}
