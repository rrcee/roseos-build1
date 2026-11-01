package os.rose.launcher.apps

/** A launchable application as Rose OS sees it. */
data class AppEntry(
    val packageName: String,
    val label: String,
    val isSystem: Boolean,
)

/**
 * Merges the installed list with the user's saved order and hidden set.
 * Saved order wins; anything new is appended alphabetically.
 */
fun List<AppEntry>.applyLayout(order: List<String>, hidden: Set<String>): List<AppEntry> {
    val visible = filter { it.packageName !in hidden }
    if (order.isEmpty()) return visible
    val byPkg = visible.associateBy { it.packageName }
    val out = ArrayList<AppEntry>(visible.size)
    for (pkg in order) byPkg[pkg]?.let { out.add(it) }
    for (entry in visible) if (entry.packageName !in order) out.add(entry)
    return out
}
