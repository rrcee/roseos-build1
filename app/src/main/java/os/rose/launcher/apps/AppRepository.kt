package os.rose.launcher.apps

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Discovers launchable apps. Queries both LAUNCHER and LEANBACK_LAUNCHER so
 * TV-only apps (and sideloaded ones that only register a TV entry point) show
 * up. Stays in sync through the package broadcasts.
 */
class AppRepository(private val context: Context) {

    private val pm: PackageManager = context.packageManager
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())
    val apps: StateFlow<List<AppEntry>> = _apps.asStateFlow()

    private var receiver: BroadcastReceiver? = null

    fun start() {
        refreshAsync()
        if (receiver != null) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_CHANGED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        val r = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                // A package we hide/remove may still be launching; just rescan.
                refreshAsync()
            }
        }
        receiver = r
        runCatching {
            ContextCompat.registerReceiver(context, r, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
        }
    }

    fun refreshAsync() {
        scope.launch { _apps.value = query() }
    }

    private fun query(): List<AppEntry> {
        val found = LinkedHashMap<String, AppEntry>()
        val categories = listOf(
            Intent.CATEGORY_LAUNCHER,
            "android.intent.category.LEANBACK_LAUNCHER",
        )
        for (category in categories) {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
            val resolved = runCatching { pm.queryIntentActivities(intent, 0) }.getOrDefault(emptyList())
            for (ri in resolved) {
                val ai = ri.activityInfo?.applicationInfo ?: continue
                val pkg = ai.packageName ?: continue
                if (pkg == context.packageName) continue
                if (found.containsKey(pkg)) continue
                val label = runCatching { ri.loadLabel(pm)?.toString() }
                    .getOrNull()?.trim().orEmpty()
                    .ifEmpty { pkg }
                found[pkg] = AppEntry(
                    packageName = pkg,
                    label = label,
                    isSystem = (ai.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                )
            }
        }
        return found.values.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
    }

    fun launch(pkg: String): Boolean {
        val intent = pm.getLaunchIntentForPackage(pkg) ?: return false
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        return runCatching {
            context.startActivity(intent)
            true
        }.getOrDefault(false)
    }

    fun stop() {
        receiver?.let { runCatching { context.unregisterReceiver(it) } }
        receiver = null
    }
}
