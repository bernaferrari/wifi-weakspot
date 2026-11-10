package com.bernaferrari.wifiweakspot.core.backup

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.DocumentsContract
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

    override suspend fun prepareExport(): PreparedBackup {
        var temporary: File? = null
        try {
            return withContext(Dispatchers.IO) {
                mutex.withLock {
                    recoverPendingImportsLocked()
                    val images = mutableMapOf<String, File>()
                    val projects = database.useReaderConnection { connection ->
                        connection.withTransaction(SQLiteTransactionType.DEFERRED) {
                            val points = dao.snapshotSurveyPoints().groupBy { it.projectId }
                            val owned = File(context.filesDir, "survey_floorplans").canonicalFile
                            dao.snapshotSurveyProjects().mapIndexed { index, project ->
                                val entry = project.imagePath?.let { path ->
                                    val file = File(path).canonicalFile
                                    require(file.parentFile == owned && file.isFile) { "Floor plan unavailable" }
                                    "images/$index.bin".also { images[it] = file }
                                }
                                project.backup(points[project.id].orEmpty(), entry)
                            }
                        }
                    }
                    val archive = BackupArchive(
                        archiveId = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis(),
                        projects = projects, roomReadings = rooms.readings.first()
                    )
                    val file = File.createTempFile("backup-", ".zip", context.cacheDir)
                    temporary = file
                    file.outputStream().use { BackupZip.write(it, archive, images) }
                    Export(file)
                }
            }
        } catch (failure: Exception) {
            temporary?.delete()
            throw failure
        }
    }

    override suspend fun discardExportDestination(destination: String) {
        val uri = Uri.parse(destination)
        // The system create-document picker never overwrites an existing file.
        withContext(NonCancellable + Dispatchers.IO) {
            if (DocumentsContract.isDocumentUri(context, uri)) {
                runCatching { DocumentsContract.deleteDocument(context.contentResolver, uri) }
            }
        }
    }

    private inner class Export(private val file: File) : PreparedBackup {
        override suspend fun writeTo(destination: String) {
            var written = false
            try {
                withContext(Dispatchers.IO) {
                    val uri = Uri.parse(destination)
                    context.contentResolver.openOutputStream(uri, "wt")?.use { output ->
                        file.inputStream().use { it.copyTo(output) }
                    } ?: error("Backup destination unavailable")
                    written = true
                }
            } catch (failure: Exception) {
                if (!written) discardExportDestination(destination)
                throw failure
            }
        }

        override fun discard() { file.delete() }
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
