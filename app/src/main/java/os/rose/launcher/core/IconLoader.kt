package os.rose.launcher.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.collection.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Application icon pipeline.
 *
 * - Drawables are rasterised straight to their final display size (no full
 *   resolution intermediate bitmap is ever allocated).
 * - Results live in a byte-bounded LRU so a 1 GB device can never accumulate
 *   hundreds of icons.
 * - Loading is off the main thread and de-duplicated.
 */
object IconLoader {

    private const val CACHE_KB = 5 * 1024

    private val cache = object : LruCache<String, ImageBitmap>(CACHE_KB) {
        override fun sizeOf(key: String, value: ImageBitmap): Int =
            (value.width * value.height * 4) / 1024
    }

    private val inFlight = java.util.Collections.synchronizedSet(HashSet<String>())

    fun peek(pkg: String, sizePx: Int): ImageBitmap? = cache.get(key(pkg, sizePx))

    suspend fun load(context: Context, pkg: String, sizePx: Int): ImageBitmap? {
        val k = key(pkg, sizePx)
        cache.get(k)?.let { return it }
        if (!inFlight.add(k)) return null
        try {
            return withContext(Dispatchers.IO) {
                val drawable: Drawable? = try {
                    context.packageManager.getApplicationIcon(pkg)
                } catch (t: Throwable) {
                    null
                }

                val bmp = rasterise(drawable, sizePx)
                val image = bmp.asImageBitmap()
                cache.put(k, image)
                image
            }
        } finally {
            inFlight.remove(k)
        }
    }

    private fun key(pkg: String, sizePx: Int) = "$pkg@$sizePx"

    private fun rasterise(drawable: Drawable?, sizePx: Int): Bitmap {
        val bmp = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        if (drawable != null) {
            drawable.setBounds(0, 0, sizePx, sizePx)
            drawable.draw(canvas)
        }
        return bmp
    }

    fun clear() = cache.evictAll()

    /** Drop down to a fraction of the cache when the system reports pressure. */
    fun trim(keepFraction: Float) {
        cache.trimToSize((CACHE_KB * keepFraction).toInt().coerceAtLeast(0))
    }

    fun usedKb(): Int = cache.size()
}
