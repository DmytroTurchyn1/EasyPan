package com.cook.easypan.core.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "ImageCache"
private const val SHARED_IMAGES_DIR = "shared_images"

/**
 * Writes [bitmap] as a PNG into the app cache and returns a `content://` URI for it.
 *
 * The directory is the one declared in `res/xml/file_paths.xml`, so the URI can be handed to another
 * app via `FLAG_GRANT_READ_URI_PERMISSION`. Reuses [fileName] rather than generating a unique one,
 * so repeated shares overwrite instead of filling the cache.
 *
 * Returns null when the file cannot be written.
 */
suspend fun saveBitmapToCache(
    context: Context,
    bitmap: Bitmap,
    fileName: String,
): Uri? = withContext(Dispatchers.IO) {
    runCatching {
        val directory = File(context.cacheDir, SHARED_IMAGES_DIR).apply { mkdirs() }
        val file = File(directory, fileName)
        file.outputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }.onFailure { error ->
        Log.e(TAG, "Failed to cache image for sharing", error)
    }.getOrNull()
}
