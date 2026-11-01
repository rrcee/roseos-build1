package os.rose.launcher.core

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.StatFs
import java.io.File

/**
 * A snapshot of what we are actually running on. Rose OS never assumes a
 * device class: the low-memory path is chosen from these numbers, not from
 * the model name.
 */
data class DeviceProfile(
    val manufacturer: String,
    val model: String,
    val androidRelease: String,
    val sdk: Int,
    val totalRamMb: Long,
    val memoryClassMb: Int,
    val isLowRamDevice: Boolean,
    val widthPx: Int,
    val heightPx: Int,
    val densityDpi: Int,
    val gpu: String,
    val freeStorageMb: Long,
) {
    /** 1 GB-class sticks land here. */
    val constrained: Boolean
        get() = isLowRamDevice || memoryClassMb <= 128 || totalRamMb <= 1400

    val resolution: String get() = "${widthPx}×${heightPx}"

    companion object {
        fun detect(context: Context): DeviceProfile {
            val am = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
            val mi = ActivityManager.MemoryInfo()
            runCatching { am?.getMemoryInfo(mi) }
            val dm = context.resources.displayMetrics
            val stat = runCatching { StatFs(context.filesDir.absolutePath) }.getOrNull()

            return DeviceProfile(
                manufacturer = Build.MANUFACTURER ?: "unknown",
                model = Build.MODEL ?: "unknown",
                androidRelease = Build.VERSION.RELEASE ?: "?",
                sdk = Build.VERSION.SDK_INT,
                totalRamMb = (mi.totalMem / (1024L * 1024L)),
                memoryClassMb = am?.memoryClass ?: 128,
                isLowRamDevice = am?.isLowRamDevice ?: false,
                widthPx = dm.widthPixels,
                heightPx = dm.heightPixels,
                densityDpi = dm.densityDpi,
                gpu = readGpu(),
                freeStorageMb = stat?.let { (it.availableBytes / (1024L * 1024L)) } ?: -1L,
            )
        }

        /** Mali GPUs expose this on Amlogic/Broadcom TV sticks; fall back to BOARD. */
        private fun readGpu(): String {
            val candidates = listOf(
                "/sys/class/misc/mali0/device/gpuinfo",
                "/sys/kernel/gpu/gpu_model",
                "/proc/gpuinfo",
            )
            for (path in candidates) {
                runCatching {
                    val f = File(path)
                    if (f.exists() && f.canRead()) {
                        val text = f.readText().trim().lines().firstOrNull { it.isNotBlank() }
                        if (!text.isNullOrBlank()) return text.take(48)
                    }
                }
            }
            return Build.HARDWARE ?: "unknown"
        }
    }
}
