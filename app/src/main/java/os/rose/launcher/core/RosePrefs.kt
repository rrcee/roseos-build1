package os.rose.launcher.core

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SharedPreferences-backed settings store exposed as a [StateFlow].
 * Writes are synchronous but tiny (a dozen primitives), so no background
 * thread or async persistence layer is needed.
 */
class RosePrefs(context: Context) {

    private val sp = context.applicationContext
        .getSharedPreferences("rose_os", Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(read())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    private fun read(): Settings = Settings(
        theme = ThemeId.from(sp.getString(K_THEME, null)),
        setupDone = sp.getBoolean(K_SETUP, false),
        greetingEnabled = sp.getBoolean(K_GREETING, true),
        showClock = sp.getBoolean(K_CLOCK, true),
        showDate = sp.getBoolean(K_DATE, true),
        showRecents = sp.getBoolean(K_RECENTS_ROW, true),
        animationIntensity = sp.getFloat(K_INTENSITY, 1f),
        reduceMotion = sp.getBoolean(K_REDUCE_MOTION, false),
        performanceMode = sp.getBoolean(K_PERF, false),
        reducedEffects = sp.getBoolean(K_REDUCED, false),
        soundEnabled = sp.getBoolean(K_SOUND, false),
        hidden = sp.getStringSet(K_HIDDEN, emptySet())?.toSet() ?: emptySet(),
        order = decodeList(sp.getString(K_ORDER, "") ?: ""),
        recents = decodeList(sp.getString(K_RECENT_LIST, "") ?: ""),
        wallpaperPath = sp.getString(K_WALLPAPER, ""),
    )

    private fun write(s: Settings) {
        sp.edit()
            .putString(K_THEME, s.theme.key)
            .putBoolean(K_SETUP, s.setupDone)
            .putBoolean(K_GREETING, s.greetingEnabled)
            .putBoolean(K_CLOCK, s.showClock)
            .putBoolean(K_DATE, s.showDate)
            .putBoolean(K_RECENTS_ROW, s.showRecents)
            .putFloat(K_INTENSITY, s.animationIntensity)
            .putBoolean(K_REDUCE_MOTION, s.reduceMotion)
            .putBoolean(K_PERF, s.performanceMode)
            .putBoolean(K_REDUCED, s.reducedEffects)
            .putBoolean(K_SOUND, s.soundEnabled)
            .putStringSet(K_HIDDEN, s.hidden)
            .putString(K_ORDER, encodeList(s.order))
            .putString(K_RECENT_LIST, encodeList(s.recents))
            .putString(K_WALLPAPER, s.wallpaperPath)
            .apply()
    }

    fun update(block: (Settings) -> Settings) {
        val next = block(_settings.value)
        if (next == _settings.value) return
        _settings.value = next
        write(next)
    }

    fun noteLaunched(pkg: String) = update { s ->
        val next = ArrayList<String>(s.recents.size + 1)
        next.add(pkg)
        for (p in s.recents) if (p != pkg && next.size < MAX_RECENTS) next.add(p)
        s.copy(recents = next)
    }

    private fun encodeList(list: List<String>): String = list.joinToString("\u0001")

    private fun decodeList(raw: String?): List<String> = if (raw?.isNotBlank() == true) raw.split("\u0001").filter { it.isNotBlank() } else emptyList()

    private companion object {
        const val MAX_RECENTS = 6
        const val K_THEME = "theme"
        const val K_SETUP = "setup_done"
        const val K_GREETING = "greeting"
        const val K_CLOCK = "clock"
        const val K_DATE = "date"
        const val K_RECENTS_ROW = "recents_row"
        const val K_INTENSITY = "intensity"
        const val K_REDUCE_MOTION = "reduce_motion"
        const val K_PERF = "performance"
        const val K_REDUCED = "reduced_effects"
        const val K_SOUND = "sound"
        const val K_HIDDEN = "hidden"
        const val K_ORDER = "order"
        const val K_RECENT_LIST = "recents_list"
        const val K_WALLPAPER = "wallpaper"
    }
}
