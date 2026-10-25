package com.bernaferrari.wifiweakspot.core.export

import com.bernaferrari.wifiweakspot.core.i18n.appStrings
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class AndroidExportManager(
    private val context: Context
) : ExportManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault())

    override suspend fun exportWifiToCsv(networks: List<WifiExportData>, fileName: String) {
        val csv = buildString {
            appendLine("SSID,BSSID,Signal (dBm),Frequency (MHz),Channel,Security,Timestamp")
            networks.forEach { n ->
                appendLine(
                    listOf(
                        csvField(n.ssid),
                        csvField(n.bssid),
                        n.rssi.toString(),
                        n.frequency.toString(),
                        n.channel.toString(),
                        csvField(n.security),
                        csvField(formatTimestamp(n.timestamp))
                    ).joinToString(",")
                )
            }
        }
        val file = writeFile("$fileName.csv", csv)
        shareFile(file, "text/csv")
    }

    override suspend fun exportWifiToJson(networks: List<WifiExportData>, fileName: String) {
        val payload = WifiExportFile(
            exportedAt = System.currentTimeMillis(),
            networkCount = networks.size,
            networks = networks
        )
        val file = writeFile("$fileName.json", json.encodeToString(payload))
        shareFile(file, "application/json")
    }

    override suspend fun exportLanToCsv(devices: List<LanExportData>, fileName: String) {
        val csv = buildString {
            appendLine("IP Address,MAC Address,Hostname,Vendor,Online,Last Seen")
            devices.forEach { d ->
                appendLine(
                    listOf(
                        csvField(d.ipAddress),
                        csvField(d.macAddress ?: ""),
                        csvField(d.hostname ?: ""),
                        csvField(d.vendor ?: ""),
                        if (d.isOnline) "Yes" else "No",
                        csvField(formatTimestamp(d.lastSeen))
                    ).joinToString(",")
                )
            }
        }
        val file = writeFile("$fileName.csv", csv)
        shareFile(file, "text/csv")
    }

    override suspend fun exportLanToJson(devices: List<LanExportData>, fileName: String) {
        val payload = LanExportFile(
            exportedAt = System.currentTimeMillis(),
            deviceCount = devices.size,
            devices = devices
        )
        val file = writeFile("$fileName.json", json.encodeToString(payload))
        shareFile(file, "application/json")
    }

    override suspend fun exportSpeedTestToCsv(results: List<SpeedTestExportData>, fileName: String) {
        val csv = buildString {
            appendLine("Timestamp,Download (Mbps),Upload (Mbps),HTTP latency (ms),Server")
            results.forEach { r ->
                appendLine(
                    listOf(
                        csvField(formatTimestamp(r.timestamp)),
                        r.downloadMbps.toString(),
                        if (r.uploadMbps < 0f) "" else r.uploadMbps.toString(),
                        r.pingMs.toString(),
                        csvField(r.server)
                    ).joinToString(",")
                )
            }
        }
        val file = writeFile("$fileName.csv", csv)
        shareFile(file, "text/csv")
    }

    override suspend fun exportToCsv(data: List<Any>, fileName: String) {
        val csv = data.joinToString(separator = "\n") { it.toString() }
        val safeName = fileName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val file = writeFile("$safeName.csv", csv)
        shareFile(file, "text/csv")
    }

    override suspend fun shareReport(text: String, title: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, appStrings.common.share).apply {
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
            description = appStrings.domain.chartDescription(name)
        )
    }

    private fun writeFile(name: String, content: String): File {
        val file = File(context.cacheDir, name)
        file.writeText(content)
        return file
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
        context.startActivity(Intent.createChooser(intent, appStrings.domain.exportFileChooserTitle).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    private fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(timestamp))
    }
}

@Serializable
private data class WifiExportFile(
    val exportedAt: Long,
    val networkCount: Int,
    val networks: List<WifiExportData>
)

@Serializable
private data class LanExportFile(
    val exportedAt: Long,
    val deviceCount: Int,
    val devices: List<LanExportData>
)
