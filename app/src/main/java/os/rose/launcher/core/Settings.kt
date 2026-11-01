package os.rose.launcher.core

/**
 * Everything Rose OS persists. Small, flat and cheap to read: a single
 * SharedPreferences file, no database, no serialization framework.
 */
data class Settings(
    val theme: ThemeId = ThemeId.LIQUID,
    val setupDone: Boolean = false,
    val greetingEnabled: Boolean = true,

    val showClock: Boolean = true,
    val showDate: Boolean = true,
    val showRecents: Boolean = true,

    /** 0f..1f multiplier applied to every focus/entrance animation. */
    val animationIntensity: Float = 1f,
    val reduceMotion: Boolean = false,

    val performanceMode: Boolean = false,
    val reducedEffects: Boolean = false,
    val soundEnabled: Boolean = false,

    val hidden: Set<String> = emptySet(),
    val order: List<String> = emptyList(),
    val recents: List<String> = emptyList(),

    /** null = use the theme's own background. */
    val wallpaperPath: String? = null,
) {
    /** True when the device (or the user) asked for the cheapest possible path. */
    val economy: Boolean get() = performanceMode || reducedEffects || reduceMotion
}
