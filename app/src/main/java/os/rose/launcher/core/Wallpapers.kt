package os.rose.launcher.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream

/**
 * Custom wallpapers are copied into internal storage, downsampled and
 * re-encoded. Rose OS never keeps a multi-megabyte original in memory and
 * needs no storage permission to read the user's pick.
 */
object Wallpapers {

    private const val TARGET_WIDTH = 1920

    fun save(context: Context, uri: Uri): String? = runCatching {
        val dir = File(context.filesDir, "wallpapers").apply { mkdirs() }
        dir.listFiles()?.forEach { it.delete() }

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, bounds)
        }
        if (bounds.outWidth <= 0) return@runCatching null

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= TARGET_WIDTH) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        } ?: return@runCatching null

        val file = File(dir, "wall.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 88, out)
        }
        bitmap.recycle()
        file.absolutePath
    }.getOrNull()

    fun load(path: String): ImageBitmap? = runCatching {
        val file = File(path)
        if (!file.exists()) return@runCatching null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= TARGET_WIDTH) sample *= 2

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        BitmapFactory.decodeFile(path, options)?.asImageBitmap()
    }.getOrNull()

    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        runCatching { File(path).delete() }
    }
}
