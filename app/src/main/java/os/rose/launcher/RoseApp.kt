package os.rose.launcher

import android.app.Application
import android.content.ComponentCallbacks2
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import os.rose.launcher.apps.AppRepository
import os.rose.launcher.audio.SoundFx
import os.rose.launcher.core.DeviceProfile
import os.rose.launcher.core.IconLoader
import os.rose.launcher.core.RosePrefs

class RoseApp : Application() {

    lateinit var prefs: RosePrefs
        private set
    lateinit var repository: AppRepository
        private set
    lateinit var device: DeviceProfile
        private set

    val sound = SoundFx()

    private val _memoryPressure = MutableStateFlow(false)
    val memoryPressure: StateFlow<Boolean> = _memoryPressure.asStateFlow()

    override fun onCreate() {
        super.onCreate()
        prefs = RosePrefs(this)
        device = DeviceProfile.detect(this)
        repository = AppRepository(this)
        repository.start()
        sound.setEnabled(prefs.settings.value.soundEnabled)
    }

    /**
     * Low-memory fallback. When the system says we are under pressure we drop
     * half the icon cache and switch the UI to its cheap path.
     */
    fun onTrim(level: Int) {
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW) {
            _memoryPressure.value = true
            IconLoader.trim(0.5f)
        }
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL) {
            IconLoader.clear()
        }
    }
}
