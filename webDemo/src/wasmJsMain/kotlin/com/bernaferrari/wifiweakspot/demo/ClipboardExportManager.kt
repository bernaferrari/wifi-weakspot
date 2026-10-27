package com.bernaferrari.wifiweakspot.demo

import com.bernaferrari.wifiweakspot.core.export.ExportArtifact
import com.bernaferrari.wifiweakspot.core.export.ExportFormat
import com.bernaferrari.wifiweakspot.core.export.ExportManager
import com.bernaferrari.wifiweakspot.core.export.LanExportData
import com.bernaferrari.wifiweakspot.core.export.SpeedTestExportData
import com.bernaferrari.wifiweakspot.core.export.WifiExportData
import com.bernaferrari.wifiweakspot.core.util.nowMillis

/** Files can't be written from the browser; reports go to the clipboard instead. */
class ClipboardExportManager : ExportManager {
    override suspend fun exportWifiToCsv(networks: List<WifiExportData>, fileName: String) = Unit
    override suspend fun exportWifiToJson(networks: List<WifiExportData>, fileName: String) = Unit
    override suspend fun exportLanToCsv(devices: List<LanExportData>, fileName: String) = Unit
    override suspend fun exportLanToJson(devices: List<LanExportData>, fileName: String) = Unit
    override suspend fun exportSpeedTestToCsv(results: List<SpeedTestExportData>, fileName: String) = Unit
    override suspend fun shareReport(text: String, title: String) = copyText(text)
    override suspend fun exportChart(chartData: ByteArray, name: String) =
        ExportArtifact(0, ExportFormat.PNG, name, nowMillis(), chartData.size.toLong(), name)
}

