package com.bernaferrari.wifiweakspot.feature.analyze

import org.junit.Test
import org.junit.Assert.assertEquals

class SettledOrderTest {
    private data class N(val id: String, val rssi: Int, val mine: Boolean = false, val favorite: Boolean = false)
    private fun order(previous: List<String>, vararg items: N) =
        settledOrder(previous, items.toList(), { it.id }, { it.rssi }, rank = { item ->
            when {
                item.mine -> 2
                item.favorite -> 1
                else -> 0
            }
        }).map { it.id }

    @Test fun firstShowingIsStrongestFirstWithYoursOnTop() =
        assertEquals(listOf("home", "b", "a"), order(emptyList(), N("a", -70), N("b", -50), N("home", -80, mine = true)))

    @Test fun smallWobblesKeepRowsInPlace() =
        assertEquals(listOf("a", "b"), order(listOf("a", "b"), N("a", -62), N("b", -59)))

    @Test fun clearlyStrongerRowsMoveUp() =
        assertEquals(listOf("b", "a"), order(listOf("a", "b"), N("a", -70), N("b", -55)))

    @Test fun newcomersSlotInBySignal() =
        assertEquals(listOf("c", "a", "b"), order(listOf("a", "b"), N("a", -60), N("b", -61), N("c", -40)))

    @Test fun aRepeatedNewcomerMakesOneRow() =
        assertEquals(listOf("a", "b"), order(emptyList(), N("a", -70), N("b", -60), N("a", -50)))

    @Test fun aRepeatedPreviousIdMakesOneRow() =
        assertEquals(listOf("a", "b"), order(listOf("a", "a", "b"), N("a", -60), N("b", -61)))

    @Test fun aFavoriteLeadsStrongerStrangers() =
        assertEquals(listOf("fav", "a"), order(emptyList(), N("a", -40), N("fav", -80, favorite = true)))

    @Test fun yourConnectionStaysAboveAStrongerFavorite() =
        assertEquals(listOf("home", "fav", "a"), order(emptyList(), N("fav", -40, favorite = true), N("home", -80, mine = true), N("a", -50)))

    @Test fun aNewFavoriteMovesUpFromWhereItWas() =
        assertEquals(listOf("c", "a", "b"), order(listOf("a", "b", "c"), N("a", -40), N("b", -50), N("c", -80, favorite = true)))

    @Test fun removingAFavoriteLetsItSinkBySignal() =
        assertEquals(listOf("a", "b", "c"), order(listOf("c", "a", "b"), N("a", -40), N("b", -50), N("c", -80)))

    @Test fun favoritesHoldTheirPlacesThroughSmallWobbles() =
        assertEquals(listOf("f1", "f2", "a"), order(listOf("f1", "f2", "a"), N("f1", -62, favorite = true), N("f2", -59, favorite = true), N("a", -30)))
}
