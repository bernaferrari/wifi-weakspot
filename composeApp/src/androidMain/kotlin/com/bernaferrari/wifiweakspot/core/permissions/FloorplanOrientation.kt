package com.bernaferrari.wifiweakspot.core.permissions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import java.io.File

/**
 * How far to turn a photo clockwise so it is upright, from the orientation note cameras write
 * into the file (EXIF): 3 is upside down, 6 and 8 are on their side. Mirrored variants, which
 * only some front cameras write, are left alone.
 */
internal fun exifRotationDegrees(orientation: Int): Int = when (orientation) {
    3 -> 180
    6 -> 90
    8 -> 270
    else -> 0
}

/**
 * Phone cameras store a photo as the sensor saw it and note how to turn it; the map draws the
 * pixels as stored, so a photo taken upright would lie on its side. This rewrites a freshly picked
 * JPEG upright. It runs once, when the photo is picked, never when it is shown: readings on older
 * maps were placed on the photo as it was drawn then, and turning it would move them.
 */
internal fun storeUpright(file: File, mimeType: String?) {
    // Other formats are either never rotated this way or already turned by Android's decoder.
    if (mimeType != "image/jpeg") return
    val orientation = runCatching {
        ExifInterface(file.absolutePath).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)
    }.getOrDefault(1)
    val degrees = exifRotationDegrees(orientation)
    if (degrees == 0) return

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(file.absolutePath, bounds)
    // A full camera photo and its turned copy do not both fit in memory on small phones;
    // about 2000 pixels on the long side is plenty for a floor plan.
    var sample = 1
    while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= 1280) sample *= 2
    val stored = BitmapFactory.decodeFile(file.absolutePath, BitmapFactory.Options().apply { inSampleSize = sample })
        ?: return
    val upright = Bitmap.createBitmap(
        stored, 0, 0, stored.width, stored.height, Matrix().apply { postRotate(degrees.toFloat()) }, true
    )
    if (upright !== stored) stored.recycle()
    try {
        file.outputStream().use { output ->
            check(upright.compress(Bitmap.CompressFormat.JPEG, 92, output)) { "Image could not be saved" }
        }
    } finally {
        upright.recycle()
    }
}
