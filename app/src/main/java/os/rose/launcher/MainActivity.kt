package os.rose.launcher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : ComponentActivity() {

    private val vm: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            RoseRoot(vm)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        (application as RoseApp).onTrim(level)
    }

    /**
     * The HOME key. Android sends us back to ourselves; make sure the app list
     * is fresh without rebuilding the UI.
     */
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        (application as RoseApp).repository.refreshAsync()
    }
}
