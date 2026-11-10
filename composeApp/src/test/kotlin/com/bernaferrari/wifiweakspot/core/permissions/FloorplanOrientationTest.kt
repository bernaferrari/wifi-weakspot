package com.bernaferrari.wifiweakspot.core.permissions

import org.junit.Assert.assertEquals
import org.junit.Test

class FloorplanOrientationTest {
    @Test fun sidewaysAndUpsideDownPhotosAreTurnedUpright() {
        assertEquals(90, exifRotationDegrees(6))
        assertEquals(180, exifRotationDegrees(3))
        assertEquals(270, exifRotationDegrees(8))
    }

    @Test fun uprightUnknownAndMirroredPhotosAreLeftAlone() {
        for (orientation in listOf(0, 1, 2, 4, 5, 7, 9, -1)) {
            assertEquals(0, exifRotationDegrees(orientation))
        }
    }
}
