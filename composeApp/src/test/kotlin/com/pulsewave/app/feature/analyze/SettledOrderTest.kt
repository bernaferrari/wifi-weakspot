package com.pulsewave.app.feature.analyze

import org.junit.Test
import org.junit.Assert.assertEquals

class SettledOrderTest {
    private data class N(val id: String, val rssi: Int, val mine: Boolean = false)
    private fun order(previous: List<String>, vararg items: N) =
        settledOrder(previous, items.toList(), { it.id }, { it.rssi }, { it.mine }).map { it.id }

    @Test fun firstShowingIsStrongestFirstWithYoursOnTop() =
        assertEquals(listOf("home", "b", "a"), order(emptyList(), N("a", -70), N("b", -50), N("home", -80, mine = true)))

    @Test fun smallWobblesKeepRowsInPlace() =
        assertEquals(listOf("a", "b"), order(listOf("a", "b"), N("a", -62), N("b", -59)))

    @Test fun clearlyStrongerRowsMoveUp() =
        assertEquals(listOf("b", "a"), order(listOf("a", "b"), N("a", -70), N("b", -55)))

    @Test fun newcomersSlotInBySignal() =
        assertEquals(listOf("c", "a", "b"), order(listOf("a", "b"), N("a", -60), N("b", -61), N("c", -40)))
}
