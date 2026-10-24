package com.bernaferrari.wifiweakspot.core.platform

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.core.content.ContextCompat
import java.io.File

class AndroidPlatformActions(private val context: Context) : PlatformActions {
    private fun open(action: String, data: Uri? = null) {
        val intent = Intent(action).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).apply { if (data != null) setData(data) }
        // Some phones hide a settings page; fall back to the main Settings screen.
        runCatching { context.startActivity(intent) }
            .onFailure { context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
    }

    override fun openWifiSettings() = open(Settings.ACTION_WIFI_SETTINGS)
    override fun openLocationSettings() = open(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    override fun openAppSettings() = open(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
    override fun openDeveloperOptions() = open(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
    override fun openDeviceInfo() = open(Settings.ACTION_DEVICE_INFO_SETTINGS)

    override fun share(text: String, title: String) {
        val send = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, text)
        context.startActivity(Intent.createChooser(send, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun shareImage(image: ImageBitmap, title: String) {
        // Cache is already exposed through the FileProvider; one file, replaced on each share.
        val file = File(context.cacheDir, "shared-map.png")
        file.outputStream().use { image.asAndroidBitmap().compress(android.graphics.Bitmap.CompressFormat.PNG, 100, it) }
        val uri = androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val send = Intent(Intent.ACTION_SEND).setType("image/png").putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(send, title).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    override fun showMessage(text: String) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show()
    }

    override fun copyToClipboard(label: String, text: String) {
        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(ClipData.newPlainText(label, text))
    }

    override fun needsNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

    override fun loadImage(path: String): ImageBitmap? = runCatching {
        // Floor plans are often phone photos; decode near screen size, not at full resolution.
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        android.graphics.BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 2048) sample *= 2
        val options = android.graphics.BitmapFactory.Options().apply { inSampleSize = sample }
        android.graphics.BitmapFactory.decodeFile(path, options)?.asImageBitmap()
    }.getOrNull()

    override fun openUrl(url: String) = open(Intent.ACTION_VIEW, Uri.parse(url))

    override fun deleteOwnedFile(path: String) {
        val owned = File(context.filesDir, "survey_floorplans").canonicalFile
        val file = File(path).canonicalFile
        if (file.parentFile == owned) file.delete()
    }
}
