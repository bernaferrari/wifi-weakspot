package com.pulsewave.app.core.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

class AndroidExportManager(
    private val context: Context
) : ExportManager {

    override suspend fun exportToCsv(data: List<Any>, fileName: String) {
        val file = File(context.cacheDir, "$fileName.csv")
        file.writeText(data.joinToString("\n") { it.toString() })
        shareFile(file, "text/csv")
    }

    override suspend fun exportToJson(data: Any, fileName: String) {
        val file = File(context.cacheDir, "$fileName.json")
        file.writeText(data.toString())
        shareFile(file, "application/json")
    }

    override suspend fun shareReport(text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Share PulseWave Report").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    override suspend fun exportChart(chartData: ByteArray, name: String): ExportArtifact {
        val file = File(context.cacheDir, "$name.png")
        file.writeBytes(chartData)
        
        return ExportArtifact(
            id = System.currentTimeMillis(),
            format = ExportFormat.PNG,
            path = file.absolutePath,
            createdAt = System.currentTimeMillis(),
            sizeBytes = file.length(),
            description = "Chart: $name"
        )
    }

    private fun shareFile(file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Export File").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}
