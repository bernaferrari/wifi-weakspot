package com.pulsewave.app.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/** App-level haptic vocabulary, respecting the haptics setting. */
class PulseWaveHapticFeedback(private val haptics: HapticFeedback, private val enabled: Boolean = true) {
    private fun play(type: HapticFeedbackType) {
        if (enabled) haptics.performHapticFeedback(type)
    }

    fun click() = play(HapticFeedbackType.ContextClick)
    fun light() = play(HapticFeedbackType.SegmentTick)
    fun heavy() = play(HapticFeedbackType.LongPress)
    fun success() = play(HapticFeedbackType.Confirm)
    fun error() = play(HapticFeedbackType.Reject)
}

@Composable
fun rememberPulseWaveHapticFeedback(): PulseWaveHapticFeedback {
    val haptics = LocalHapticFeedback.current
    val enabled = LocalHapticsEnabled.current
    return remember(haptics, enabled) { PulseWaveHapticFeedback(haptics, enabled) }
}
