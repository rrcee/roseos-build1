package os.rose.launcher.core

/**
 * The four Rose OS visual identities. Only appearance changes between them;
 * discovery, navigation and behaviour are shared.
 */
enum class ThemeId(val key: String, val label: String, val tagline: String) {
    LIQUID("liquid", "Liquid Glass", "Translucent, luminous, alive"),
    NOTHING("nothing", "Nothing OS", "Mono. Dot-matrix. Exact"),
    APPLETV("appletv", "Apple TV", "Cinematic and calm"),
    STOCK("stock", "Stock", "Fast, plain, dependable");

    companion object {
        fun from(key: String?): ThemeId = entries.firstOrNull { it.key == key } ?: LIQUID
    }
}
