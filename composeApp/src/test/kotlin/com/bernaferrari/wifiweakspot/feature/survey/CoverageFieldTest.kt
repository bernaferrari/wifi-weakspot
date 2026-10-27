package com.bernaferrari.wifiweakspot.feature.survey

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CoverageFieldTest {
    @Test fun configuredPhysicalDistancesDetermineSignalWeights() {
        // A 20 × 10 m map: .1 across and .2 down are both two metres.
        val grid = coverageGridForMap(listOf(
            PlanReading(0.6f, 0.5f, -50), PlanReading(0.5f, 0.7f, -80)
        ), widthMeters = 20f, heightMeters = 10f)
        assertEquals(28, grid.rows)
        assertEquals(-65f, grid.at(0.5f, 0.5f).first, 0.01f)
    }

    @Test fun rotatingViewportDoesNotChangeThePhysicalCoverageField() {
        fun field(viewWidth: Float, viewHeight: Float): CoverageGrid {
            val readings = listOf(Triple(0.2f, 0.4f, -50), Triple(0.7f, 0.8f, -80)).map { (x, y, rssi) ->
                val fitted = fittedMapSize(viewWidth, viewHeight, 20f, 10f)
                assertEquals(2f, fitted.width / fitted.height, 0.001f)
                val mapPosition = normalizedSurveyPosition(x * fitted.width, y * fitted.height, fitted.width, fitted.height)!!
                PlanReading(mapPosition.x, mapPosition.y, rssi)
            }
            return coverageGridForMap(readings, widthMeters = 20f, heightMeters = 10f)
        }
        val portrait = field(300f, 600f)
        val landscape = field(1200f, 400f)
        org.junit.Assert.assertArrayEquals(portrait.rssi, landscape.rssi, 0.001f)
        org.junit.Assert.assertArrayEquals(portrait.reach, landscape.reach, 0.001f)
        assertEquals(portrait.columns, landscape.columns)
        assertEquals(portrait.rows, landscape.rows)
    }

    private fun CoverageGrid.at(x: Float, y: Float): Pair<Float, Float> {
        val i = (x * columns).toInt().coerceIn(0, columns - 1)
        val j = (y * rows).toInt().coerceIn(0, rows - 1)
        return rssi[j * columns + i] to reach[j * columns + i]
    }

    @Test fun cellsNextToAReadingTakeItsValueAndFullReach() {
        val grid = coverageGrid(listOf(PlanReading(0.5f, 0.5f, -60)), aspect = 1f)
        val (rssi, reach) = grid.at(0.5f, 0.5f)
        assertEquals(-60f, rssi, 0.01f)
        assertEquals(1f, reach, 0.001f)
    }

    @Test fun betweenTwoReadingsTheValueBlends() {
        val grid = coverageGrid(listOf(PlanReading(0.2f, 0.5f, -50), PlanReading(0.8f, 0.5f, -80)), aspect = 1f)
        val (rssi, _) = grid.at(0.5f, 0.5f)
        assertTrue(rssi in -70f..-60f)
    }

    @Test fun farFromEveryReadingNothingIsClaimed() {
        val grid = coverageGrid(listOf(PlanReading(0.05f, 0.05f, -50)), aspect = 1f)
        assertEquals(0f, grid.at(0.95f, 0.95f).second, 0.001f)
    }

    @Test fun widePlansKeepReachRound() {
        // On a 2:1 plan, 0.15 across is the same distance as 0.3 down.
        val grid = coverageGrid(listOf(PlanReading(0.5f, 0.5f, -50)), aspect = 2f)
        assertEquals(grid.at(0.65f, 0.5f).second, grid.at(0.5f, 0.8f).second, 0.15f)
    }

    @Test fun mapFitsWithoutStretchingInPortraitLandscapeAndExportSizes() {
        for ((width, height) in listOf(300f to 600f, 1200f to 400f, 2048f to 2048f)) {
            val fitted = fittedMapSize(width, height, 20f, 10f)
            assertEquals(2f, fitted.width / fitted.height, 0.001f)
            assertTrue(fitted.width <= width && fitted.height <= height)
        }
    }

    @Test fun tapSelectsNearestSavedPointAndLeavesEmptyAreasForNewReadings() {
        val point = com.bernaferrari.wifiweakspot.core.database.entities.SurveyPointEntity(
            4, 1, 0.25f, 0.75f, -60, "Home", "radio", timestamp = 1)
        assertEquals(point, nearestSurveyPoint(listOf(point), 52f, 77f, 200f, 100f, 24f))
        assertEquals(null, nearestSurveyPoint(listOf(point), 190f, 10f, 200f, 100f, 24f))
    }
}
