package com.bernaferrari.wifiweakspot.core.backup

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.room.useReaderConnection
import androidx.room.useWriterConnection
import androidx.room.Transactor.SQLiteTransactionType
import com.bernaferrari.wifiweakspot.core.database.WifiWeakspotDatabase
import com.bernaferrari.wifiweakspot.core.database.entities.BackupImportEntity
import com.bernaferrari.wifiweakspot.core.rooms.RoomCheckRepository
import com.bernaferrari.wifiweakspot.core.rooms.RoomReading
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.encodeToString

class AndroidBackupService(
    private val context: Context,
    private val database: WifiWeakspotDatabase,
    private val rooms: RoomCheckRepository
) : BackupService {
    private val mutex = Mutex()
    private val dao get() = database.dao()

    override suspend fun exportTo(destination: String): Unit = withContext(Dispatchers.IO) {
        mutex.withLock {
            recoverPendingImportsLocked()
            val archiveId = UUID.randomUUID().toString()
            val images = mutableMapOf<String, File>()
            val projects = database.useReaderConnection { connection ->
                connection.withTransaction(SQLiteTransactionType.DEFERRED) {
                val points = dao.snapshotSurveyPoints().groupBy { it.projectId }
                dao.snapshotSurveyProjects().mapIndexed { index, project ->
                    val entry = project.imagePath?.let { path ->
                        val owned = File(context.filesDir, "survey_floorplans").canonicalFile
                        val file = File(path).canonicalFile
                        require(file.parentFile == owned && file.isFile) { "Floor plan unavailable" }
                        "images/$index.bin".also { images[it] = file }
                    }
                    project.backup(points[project.id].orEmpty(), entry)
                }
                }
            }
            val archive = BackupArchive(archiveId = archiveId, createdAt = System.currentTimeMillis(),
                projects = projects, roomReadings = rooms.readings.first())
            // Finish/validate the archive before opening the user's destination for writing.
            val temporary = File.createTempFile("backup-", ".zip", context.cacheDir)
            try {
                temporary.outputStream().use { BackupZip.write(it, archive, images) }
                context.contentResolver.openOutputStream(Uri.parse(destination), "wt")?.use { output ->
                    temporary.inputStream().use { it.copyTo(output) }
                } ?: error("Backup destination unavailable")
            } finally { temporary.delete() }
            Unit
        }
    }

    override suspend fun restoreFrom(source: String): RestoreResult = withContext(Dispatchers.IO) {
        mutex.withLock {
            recoverPendingImportsLocked()
            val staging = File(context.cacheDir, "restore-${UUID.randomUUID()}").apply { check(mkdirs()) }
            val ownedImages = mutableListOf<File>()
            var committed = false
            try {
                val decoded = context.contentResolver.openInputStream(Uri.parse(source))?.use {
                    BackupZip.read(it, staging)
                } ?: error("Backup source unavailable")
                val archive = decoded.archive
                if (dao.getBackupImport(archive.archiveId) != null) return@withLock RestoreResult.AlreadyRestored
                decoded.images.values.forEach { file ->
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(file.absolutePath, bounds)
                    require(bounds.outWidth > 0 && bounds.outHeight > 0) { "Invalid floor plan" }
                    require(bounds.outWidth.toLong() * bounds.outHeight <= 100_000_000) { "Floor plan too large" }
                }
                val folder = File(context.filesDir, "survey_floorplans").apply { check(isDirectory || mkdirs()) }
                val paths = decoded.images.mapValues { (_, staged) ->
                    val file = File(folder, "restored-${UUID.randomUUID()}.bin")
                    ownedImages += file
                    staged.copyTo(file)
                    file.absolutePath
                }
                // Once committed, the durable handoff is replayed on startup if the process dies.
                // NonCancellable keeps cancellation from deleting files after a successful commit.
                currentCoroutineContext().ensureActive()
                withContext(NonCancellable) {
                    database.useWriterConnection { connection ->
                        connection.withTransaction(SQLiteTransactionType.IMMEDIATE) {
                        archive.projects.forEach { project ->
                            val id = dao.insertSurveyProject(project.toEntity(project.imageEntry?.let(paths::getValue)))
                            project.points.forEach { dao.insertSurveyPoint(it.toEntity(id)) }
                        }
                        dao.insertBackupImport(BackupImportEntity(archive.archiveId, backupJson.encodeToString(archive.roomReadings)))
                        }
                    }
                    committed = true
                    recoverPendingImportsLocked()
                }
                RestoreResult.Restored
            } finally {
                if (!committed) ownedImages.forEach { it.delete() }
                staging.deleteRecursively()
            }
        }
    }

    suspend fun recoverPendingImports() = withContext(Dispatchers.IO) {
        mutex.withLock { recoverPendingImportsLocked() }
    }

    private suspend fun recoverPendingImportsLocked() {
        replayPendingBackupImports(dao, rooms)
    }
}
