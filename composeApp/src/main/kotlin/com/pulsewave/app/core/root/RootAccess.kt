package com.pulsewave.app.core.root

import kotlinx.coroutines.flow.StateFlow



enum class RootState {
    /** No `su` on this phone: the normal, unrooted path. */
    NotRooted,
    /** `su` exists; the person has not opted in. */
    Available,
    /** Opted in and granted: scans run through the system shell. */
    Enabled,
    /** The root manager refused or timed out. */
    Denied
}

/**
 * Optional root support. Scans requested by the system shell are not throttled and their
 * results reach every app, so rooted phones get live scanning without Developer options.
 * Root is only requested after the person taps "Use root".
 */
interface RootAccess {
    val state: StateFlow<RootState>
    val isEnabled: Boolean get() = state.value == RootState.Enabled

    /** Shows the root manager's prompt. Returns true when root was granted. */
    suspend fun enable(): Boolean
    fun disable()

    /** Fire-and-forget scan through the root shell. Returns false when root is not enabled. */
    fun requestScan(): Boolean
}
