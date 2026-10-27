package com.bernaferrari.wifiweakspot.core.backup

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.bernaferrari.wifiweakspot.core.database.SURVEY_POINT_LABEL_MIGRATION
import com.bernaferrari.wifiweakspot.core.database.WifiWeakspotDatabase
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyPointEntity
import com.bernaferrari.wifiweakspot.core.database.entities.SurveyProjectEntity
import com.bernaferrari.wifiweakspot.core.rooms.RoomCheckRepository
import com.bernaferrari.wifiweakspot.core.rooms.RoomReading
import java.io.File
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the real driver, ContentResolver, image decoder and Room/DataStore handoff on Android. */
@RunWith(AndroidJUnit4::class)
class AndroidBackupServiceTest {
    private lateinit var root: File
    private lateinit var context: Context
    private val databases = mutableListOf<WifiWeakspotDatabase>()

    @Before fun setUp() {
        val base = InstrumentationRegistry.getInstrumentation().targetContext
        root = File(base.cacheDir, "backup-instrumentation-${UUID.randomUUID()}").apply { check(mkdirs()) }
        context = isolatedContext(base, root)
    }

    @After fun tearDown() {
        databases.forEach { it.close() }
        root.deleteRecursively()
    }

    @Test(timeout = 60_000) fun exportRestoreRoundTripPreservesExistingDataLabelsBasementImagesAndRooms() = runBlocking {
        val source = database()
        val sourceRooms = RoomCheckRepository(MemoryPreferences())
        val image = createFloorplan()
        val original = project("Imported map", image.absolutePath)
        val projectId = source.dao().insertSurveyProject(original)
        source.dao().insertSurveyPoint(point(projectId, label = "Basement desk"))
        sourceRooms.save(room("source-reading", "Office"))
        sourceRooms.save(room("shared-id", "Archive version"))
        val archive = File(root, "roundtrip.zip")
        AndroidBackupService(context, source, sourceRooms).exportTo(Uri.fromFile(archive).toString())
        assertTrue(archive.length() > 0)

        // Import into a separate real Room instance, with independently owned files and existing data.
        val destinationContext = isolatedContext(context, File(root, "destination"))
        val destination = database(destinationContext)
        val existingProject = destination.dao().insertSurveyProject(project("Keep this map"))
        destination.dao().insertSurveyPoint(point(existingProject, "Keep this point"))
        val destinationRooms = RoomCheckRepository(MemoryPreferences())
        val localReading = room("shared-id", "Local version")
        destinationRooms.save(localReading)
        destinationRooms.save(room("local-only", "Kitchen"))
        val service = AndroidBackupService(destinationContext, destination, destinationRooms)

        assertEquals(RestoreResult.Restored, service.restoreFrom(Uri.fromFile(archive).toString()))
        val projects = destination.dao().snapshotSurveyProjects()
        assertEquals(2, projects.size)
        assertTrue(projects.any { it.id == existingProject && it.name == "Keep this map" })
        val restored = projects.single { it.name == original.name }
        assertNotEquals(existingProject, restored.id)
        assertEquals(original.widthMeters, restored.widthMeters, 0f)
        assertEquals(original.heightMeters, restored.heightMeters, 0f)
        assertEquals(original.targetBssid, restored.targetBssid)
        val restoredImage = File(requireNotNull(restored.imagePath))
        assertEquals(File(destinationContext.filesDir, "survey_floorplans").canonicalFile, restoredImage.parentFile.canonicalFile)
        assertArrayEquals(image.readBytes(), restoredImage.readBytes())
        val bitmap = requireNotNull(BitmapFactory.decodeFile(restoredImage.absolutePath))
        assertNotNull(bitmap)
        assertEquals(8, bitmap.width)
        assertEquals(4, bitmap.height)
        bitmap.recycle()
        val points = destination.dao().snapshotSurveyPoints()
        assertEquals(2, points.size)
        val restoredPoint = points.single { it.projectId == restored.id }
        assertEquals(0, restoredPoint.floorId)
        assertEquals("Basement desk", restoredPoint.label)
        assertEquals(0.25f, restoredPoint.x, 0f)
        assertEquals(0.75f, restoredPoint.y, 0f)
        assertEquals(-67, restoredPoint.rssi)
        val readings = destinationRooms.readings.first()
        assertEquals(3, readings.size)
        assertEquals(localReading, readings.single { it.id == "shared-id" })
        assertTrue(readings.any { it.id == "source-reading" })

        assertEquals(RestoreResult.AlreadyRestored, service.restoreFrom(Uri.fromFile(archive).toString()))
        assertEquals(projects, destination.dao().snapshotSurveyProjects())
        assertEquals(points, destination.dao().snapshotSurveyPoints())
        assertEquals(readings, destinationRooms.readings.first())
        assertTrue(destination.dao().getPendingBackupImports().isEmpty())
        assertEquals(1, File(destinationContext.filesDir, "survey_floorplans").listFiles()!!.size)
    }

    @Test(timeout = 60_000) fun malformedArchiveCannotMutateExistingDatabaseOrRoomData() = runBlocking {
        val db = database()
        val rooms = RoomCheckRepository(MemoryPreferences())
        val id = db.dao().insertSurveyProject(project("Keep"))
        db.dao().insertSurveyPoint(point(id, "Keep point"))
        rooms.save(room("keep-room", "Keep room"))
        val originalProjects = db.dao().snapshotSurveyProjects()
        val originalPoints = db.dao().snapshotSurveyPoints()
        val originalRooms = rooms.readings.first()
        val malformed = File(root, "bad.zip").apply { writeText("This is not a backup archive") }
        expectFailure { AndroidBackupService(context, db, rooms).restoreFrom(Uri.fromFile(malformed).toString()) }
        assertEquals(originalProjects, db.dao().snapshotSurveyProjects())
        assertEquals(originalPoints, db.dao().snapshotSurveyPoints())
        assertEquals(originalRooms, rooms.readings.first())
        assertTrue(db.dao().getPendingBackupImports().isEmpty())
        assertTrue(context.cacheDir.listFiles().orEmpty().none { it.name.startsWith("restore-") })
    }

    @Test(timeout = 60_000) fun committedRestoreRecoversAfterDataStoreFailureWithoutDuplicatingMaps() = runBlocking {
        val source = database()
        val sourceRooms = RoomCheckRepository(MemoryPreferences())
        source.dao().insertSurveyProject(project("Recover me"))
        sourceRooms.save(room("recover-room", "Bedroom"))
        val archive = File(root, "recover.zip")
        AndroidBackupService(context, source, sourceRooms).exportTo(Uri.fromFile(archive).toString())
        val destination = database()
        val preferences = MemoryPreferences().apply { failNextWrite = true }
        val destinationRooms = RoomCheckRepository(preferences)
        val service = AndroidBackupService(context, destination, destinationRooms)
        expectFailure { service.restoreFrom(Uri.fromFile(archive).toString()) }
        assertEquals(1, destination.dao().snapshotSurveyProjects().size)
        assertEquals(1, destination.dao().getPendingBackupImports().size)
        assertTrue(destinationRooms.readings.first().isEmpty())

        val restarted = AndroidBackupService(context, destination, destinationRooms)
        restarted.recoverPendingImports()
        restarted.recoverPendingImports()
        assertTrue(destination.dao().getPendingBackupImports().isEmpty())
        assertEquals("recover-room", destinationRooms.readings.first().single().id)
        assertEquals(RestoreResult.AlreadyRestored, restarted.restoreFrom(Uri.fromFile(archive).toString()))
        assertEquals(1, destination.dao().snapshotSurveyProjects().size)
    }

    @Test(timeout = 60_000) fun realRoomMigratesVersionSixAndValidatesVersionSevenSchema() = runBlocking {
        val databaseFile = File(root, "migration.db")
        val assets = InstrumentationRegistry.getInstrumentation().context.assets
        val schema = assets.open("com.bernaferrari.wifiweakspot.core.database.WifiWeakspotDatabase/6.json")
            .bufferedReader().use { JSONObject(it.readText()).getJSONObject("database") }
        BundledSQLiteDriver().open(databaseFile.absolutePath).use { connection ->
            val entities = schema.getJSONArray("entities")
            for (i in 0 until entities.length()) {
                val entity = entities.getJSONObject(i)
                val table = entity.getString("tableName")
                connection.execSQL(entity.getString("createSql").replace("\${TABLE_NAME}", table))
                entity.optJSONArray("indices")?.let { indices ->
                    for (j in 0 until indices.length()) connection.execSQL(
                        indices.getJSONObject(j).getString("createSql").replace("\${TABLE_NAME}", table))
                }
            }
            val setup = schema.getJSONArray("setupQueries")
            for (i in 0 until setup.length()) connection.execSQL(setup.getString(i))
            connection.execSQL("PRAGMA user_version = 6")
            connection.execSQL("INSERT INTO survey_projects (id, name, createdAt, updatedAt) VALUES (7, 'Existing map', 100, 200)")
            connection.execSQL("INSERT INTO survey_points (id, projectId, x, y, rssi, floorId, timestamp) VALUES (9, 7, 0.25, 0.75, -67, 0, 300)")
        }
        val migrated = Room.databaseBuilder(context, WifiWeakspotDatabase::class.java, databaseFile.absolutePath)
            .setDriver(BundledSQLiteDriver())
            .addMigrations(SURVEY_POINT_LABEL_MIGRATION)
            .build().also { databases += it }
        val existing = migrated.dao().snapshotSurveyPoints().single()
        assertEquals(9L, existing.id)
        assertEquals(0, existing.floorId)
        assertNull(existing.label)
        assertEquals("Existing map", migrated.dao().snapshotSurveyProjects().single().name)
        assertTrue(migrated.dao().getPendingBackupImports().isEmpty())
        migrated.dao().updateSurveyPoint(existing.copy(label = "Migrated label"))
        assertEquals("Migrated label", migrated.dao().snapshotSurveyPoints().single().label)
    }

    private fun database(ctx: Context = context): WifiWeakspotDatabase =
        Room.inMemoryDatabaseBuilder(ctx, WifiWeakspotDatabase::class.java)
            .setDriver(BundledSQLiteDriver()).build().also { databases += it }

    private fun createFloorplan(): File {
        val folder = File(context.filesDir, "survey_floorplans").apply { check(mkdirs()) }
        val file = File(folder, "plan.png")
        val bitmap = Bitmap.createBitmap(8, 4, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLUE) }
        try { file.outputStream().use { check(bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)) } }
        finally { bitmap.recycle() }
        return file
    }

    private fun project(name: String, imagePath: String? = null) = SurveyProjectEntity(
        name = name, description = "A measured home", imagePath = imagePath, createdAt = 100, updatedAt = 200,
        targetSsid = "Home", targetBssid = "aa:bb:cc:dd:ee:ff", widthMeters = 20f, heightMeters = 10f)

    private fun point(projectId: Long, label: String) = SurveyPointEntity(
        projectId = projectId, x = 0.25f, y = 0.75f, rssi = -67, ssid = "Home", bssid = "aa:bb:cc:dd:ee:ff",
        floorId = 0, timestamp = 300, label = label)

    private fun room(id: String, name: String) = RoomReading(id, name, -60, -65, 300, 5180,
        "Home", "aa:bb:cc:dd:ee:ff", 3, 400, routerMs = 4f)

    private suspend fun expectFailure(action: suspend () -> Unit) {
        try { action() } catch (_: Exception) { return }
        fail("Expected operation to fail")
    }

    private fun isolatedContext(base: Context, directory: File): Context {
        val files = File(directory, "files").apply { check(isDirectory || mkdirs()) }
        val cache = File(directory, "cache").apply { check(isDirectory || mkdirs()) }
        return object : ContextWrapper(base) {
            override fun getApplicationContext(): Context = this
            override fun getFilesDir(): File = files
            override fun getCacheDir(): File = cache
        }
    }

    private class MemoryPreferences : DataStore<Preferences> {
        private val state = MutableStateFlow<Preferences>(emptyPreferences())
        private val mutex = Mutex()
        var failNextWrite = false
        override val data: Flow<Preferences> = state
        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences = mutex.withLock {
            if (failNextWrite) { failNextWrite = false; throw IOException("Simulated storage failure") }
            transform(state.value).also { state.value = it }
        }
    }
}
