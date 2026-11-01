package os.rose.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.StateFlow
import os.rose.launcher.apps.AppEntry
import os.rose.launcher.apps.AppRepository
import os.rose.launcher.core.DeviceProfile
import os.rose.launcher.core.RosePrefs
import os.rose.launcher.core.Settings

/**
 * Thin coordinator. Holds no UI state and no heavy objects — just references
 * to the process-wide singletons so composition can stay dumb.
 */
class LauncherViewModel(application: Application) : AndroidViewModel(application) {

    private val rose = application as RoseApp

    val prefs: RosePrefs get() = rose.prefs
    val repository: AppRepository get() = rose.repository
    val device: DeviceProfile get() = rose.device

    val settings: StateFlow<Settings> get() = rose.prefs.settings
    val apps: StateFlow<List<AppEntry>> get() = rose.repository.apps
    val memoryPressure: StateFlow<Boolean> get() = rose.memoryPressure

    fun update(block: (Settings) -> Settings) = rose.prefs.update(block)

    fun launch(entry: AppEntry) {
        rose.sound.confirm()
        rose.prefs.noteLaunched(entry.packageName)
        rose.repository.launch(entry.packageName)
    }

    fun moveFocus() = rose.sound.move()

    fun syncSound(enabled: Boolean) = rose.sound.setEnabled(enabled)

    override fun onCleared() {
        super.onCleared()
        rose.sound.release()
    }
}
