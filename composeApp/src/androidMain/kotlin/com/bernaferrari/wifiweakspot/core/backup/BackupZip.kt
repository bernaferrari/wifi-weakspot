package com.bernaferrari.wifiweakspot.core.backup

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.serialization.encodeToString

/** Stream assets into a private staging directory, never into paths supplied by an archive. */
internal object BackupZip {
    const val MAX_METADATA_BYTES = 8 * 1024 * 1024
    const val MAX_IMAGE_BYTES = 20 * 1024 * 1024
    const val MAX_TOTAL_BYTES = 64 * 1024 * 1024
    private const val MANIFEST = "manifest.json"
    private val imageName = Regex("images/[0-9]+\\.bin")

    data class Decoded(val archive: BackupArchive, val images: Map<String, File>)

    fun write(output: OutputStream, archive: BackupArchive, images: Map<String, File>) {
        archive.validate()
        require(images.keys == archive.projects.mapNotNull { it.imageEntry }.toSet())
        val manifest = backupJson.encodeToString(archive).encodeToByteArray()
        require(manifest.size <= MAX_METADATA_BYTES)
        var total = manifest.size.toLong()
        ZipOutputStream(output).use { zip ->
            zip.putNextEntry(ZipEntry(MANIFEST))
            zip.write(manifest)
            zip.closeEntry()
            images.forEach { (entry, file) ->
                require(entry.matches(imageName))
                zip.putNextEntry(ZipEntry(entry))
                file.inputStream().use { input ->
                    copyLimited(input, zip, MAX_IMAGE_BYTES) { bytes ->
                        total += bytes
                        require(total <= MAX_TOTAL_BYTES) { "Backup too large" }
                    }
                }
                zip.closeEntry()
            }
        }
    }

    fun read(input: InputStream, stagingDirectory: File): Decoded {
        require(stagingDirectory.isDirectory)
        val seen = mutableSetOf<String>()
        val images = mutableMapOf<String, File>()
        var manifest: ByteArray? = null
        var total = 0L
        ZipInputStream(input).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                require(!entry.isDirectory && seen.add(entry.name) && seen.size <= 1_001) { "Invalid archive entry" }
                require(entry.name == MANIFEST || entry.name.matches(imageName)) { "Unexpected archive path" }
                val limit = if (entry.name == MANIFEST) MAX_METADATA_BYTES else MAX_IMAGE_BYTES
                require(entry.size <= limit) { "Entry too large" }
                val count: (Int) -> Unit = { bytes ->
                    total += bytes
                    require(total <= MAX_TOTAL_BYTES) { "Backup too large" }
                }
                if (entry.name == MANIFEST) {
                    manifest = ByteArrayOutputStream().use { buffer ->
                        copyLimited(zip, buffer, limit, count)
                        buffer.toByteArray()
                    }
                } else {
                    // The generated filename is unrelated to untrusted ZIP paths.
                    val file = File.createTempFile("image-", ".bin", stagingDirectory)
                    file.outputStream().use { copyLimited(zip, it, limit, count) }
                    images[entry.name] = file
                }
                zip.closeEntry()
            }
        }
        val archive = backupJson.decodeFromString<BackupArchive>(requireNotNull(manifest).decodeToString())
        archive.validate()
        require(images.keys == archive.projects.mapNotNull { it.imageEntry }.toSet()) { "Missing or unused images" }
        return Decoded(archive, images)
    }

    private fun copyLimited(input: InputStream, output: OutputStream, limit: Int, count: (Int) -> Unit) {
        val buffer = ByteArray(8192)
        var size = 0L
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            size += read
            require(size <= limit) { "Entry too large" }
            count(read)
            output.write(buffer, 0, read)
        }
    }
}
