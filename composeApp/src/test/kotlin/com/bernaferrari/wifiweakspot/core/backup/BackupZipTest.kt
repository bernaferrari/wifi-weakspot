package com.bernaferrari.wifiweakspot.core.backup

import com.bernaferrari.wifiweakspot.core.rooms.RoomReading
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class BackupZipTest {
    @get:Rule val temporary = TemporaryFolder()
    private val project = BackupProject(
        name = "Home", description = "A map", imageEntry = "images/0.bin", createdAt = 1, updatedAt = 2,
        targetSsid = "Home Wi-Fi", targetBssid = "00:11:22:33:44:55", widthMeters = 12f, heightMeters = 8f,
        stepLengthMeters = .7f, mapTopBearingDegrees = 30f,
        points = listOf(BackupPoint(.2f, .7f, -66, "Home Wi-Fi", "00:11:22:33:44:55", 0, 3, "Basement desk"))
    )
    private val archive = BackupArchive(archiveId = "01234567-89ab-cdef-0123-456789abcdef", createdAt = 4,
        projects = listOf(project), roomReadings = listOf(
            RoomReading("room-1", "Kitchen", -60, -65, 144, 5180, "Home Wi-Fi", "ap", 4, 3, routerMs = 4f)
        ))

    @Test fun archiveRoundTripsGeometryLabelsBasementRoomsAndImageBytes() {
        val asset = temporary.newFile().apply { writeBytes(byteArrayOf(1, 2, 3, 4)) }
        val output = ByteArrayOutputStream()
        BackupZip.write(output, archive, mapOf("images/0.bin" to asset))
        val decoded = BackupZip.read(ByteArrayInputStream(output.toByteArray()), temporary.newFolder())
        assertEquals(archive, decoded.archive)
        assertArrayEquals(asset.readBytes(), decoded.images.getValue("images/0.bin").readBytes())
        val restored = decoded.archive.projects.single().toEntity("/owned/new-image")
        assertEquals(0L, restored.id)
        assertEquals(12f, restored.widthMeters)
        val point = decoded.archive.projects.single().points.single().toEntity(100)
        assertEquals(100L, point.projectId)
        assertEquals(0L, point.id)
        assertEquals("Basement desk", point.label)
        assertEquals(0, point.floorId)
    }

    @Test fun rejectsFutureVersionsAndInvalidCoordinatesBeforeRestoring() {
        assertThrows(IllegalArgumentException::class.java) { archive.copy(version = 2).validate() }
        assertThrows(IllegalArgumentException::class.java) {
            archive.copy(projects = listOf(project.copy(points = listOf(project.points.first().copy(x = 1.1f))))).validate()
        }
        assertThrows(IllegalArgumentException::class.java) { archive.copy(projects = listOf(project.copy(widthMeters = Float.NaN))).validate() }
    }

    @Test fun pathTraversalAndAbsoluteEntriesAreNeverExtracted() {
        for (name in listOf("../escape", "/tmp/escape", "images/../escape", "images\\escape")) {
            val folder = temporary.newFolder()
            assertThrows(IllegalArgumentException::class.java) { BackupZip.read(zip(name to byteArrayOf(1)), folder) }
            assertTrue(folder.listFiles()!!.isEmpty())
        }
    }

    @Test fun missingAndUnreferencedImagesAreRejected() {
        val manifest = backupJson.encodeToString(archive).encodeToByteArray()
        assertThrows(IllegalArgumentException::class.java) { BackupZip.read(zip("manifest.json" to manifest), temporary.newFolder()) }
        assertThrows(IllegalArgumentException::class.java) {
            BackupZip.read(zip("manifest.json" to manifest, "images/0.bin" to byteArrayOf(1), "images/1.bin" to byteArrayOf(2)), temporary.newFolder())
        }
    }

    @Test fun sizeLimitCountsExpandedBytesOfHighlyCompressedData() {
        val bytes = ByteArray(BackupZip.MAX_METADATA_BYTES + 1) { ' '.code.toByte() }
        assertThrows(IllegalArgumentException::class.java) {
            BackupZip.read(zip("manifest.json" to bytes), temporary.newFolder())
        }
    }

    @Test fun duplicateRoomIdsAndUnsafeImageReferencesAreRejected() {
        assertThrows(IllegalArgumentException::class.java) { archive.copy(projects = listOf(project, project.copy(name = "Other map"))).validate() }
        assertThrows(IllegalArgumentException::class.java) { archive.copy(roomReadings = archive.roomReadings + archive.roomReadings).validate() }
        assertThrows(IllegalArgumentException::class.java) { archive.copy(projects = listOf(project.copy(imageEntry = "../../file"))).validate() }
    }

    private fun zip(vararg entries: Pair<String, ByteArray>): ByteArrayInputStream {
        val output = ByteArrayOutputStream()
        ZipOutputStream(output).use { zip ->
            entries.forEach { (name, bytes) ->
                zip.putNextEntry(ZipEntry(name)); zip.write(bytes); zip.closeEntry()
            }
        }
        return ByteArrayInputStream(output.toByteArray())
    }
}
