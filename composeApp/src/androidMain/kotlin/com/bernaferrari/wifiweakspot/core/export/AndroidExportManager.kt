package com.bernaferrari.wifiweakspot.core.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.bernaferrari.wifiweakspot.core.i18n.appStrings
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AndroidExportManager(private val context: Context) : ExportManager {
    override suspend fun exportCsv(lines: List<String>, fileName: String) {
        val file = withContext(Dispatchers.IO) {
            val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
            val folder = File(context.cacheDir, "shared").apply { check(isDirectory || mkdirs()) }
            val output = File(folder, "$safeName.csv")
            try {
                output.bufferedWriter().use { writer ->
                    lines.forEachIndexed { index, line ->
                        if (index > 0) writer.newLine()
                        writer.write(line)
                    }
                }
                output
            } catch (failure: Exception) {
                output.delete()
                throw failure
            }
        }
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND)
            .setType("text/csv")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(
            Intent.createChooser(send, appStrings.domain.exportFileChooserTitle)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        )
    }
}
