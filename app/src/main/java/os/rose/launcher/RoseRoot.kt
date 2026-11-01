package os.rose.launcher

import android.content.Intent
import android.provider.Settings as AndroidSettings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import os.rose.launcher.apps.applyLayout
import os.rose.launcher.core.IconLoader
import os.rose.launcher.core.ThemeId
import os.rose.launcher.core.Wallpapers
import os.rose.launcher.ui.screens.HelloScreen
import os.rose.launcher.ui.screens.HomeScreen
import os.rose.launcher.ui.screens.SettingsScreen
import os.rose.launcher.ui.theme.Themes
import os.rose.launcher.ui.theme.rememberAnimatedTheme

@Composable
fun RoseRoot(vm: LauncherViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val settings by vm.settings.collectAsState()
    val apps by vm.apps.collectAsState()
    val pressure by vm.memoryPressure.collectAsState()

    val theme = rememberAnimatedTheme(
        target = Themes.of(settings.theme),
        enabled = !settings.reduceMotion && !settings.performanceMode,
    )

    // One-time welcome. Decided once, at first composition, from persisted state.
    val startWithHello = remember { !settings.setupDone && settings.greetingEnabled }
    var helloVisible by remember { mutableStateOf(startWithHello) }
    var entered by remember { mutableStateOf(!startWithHello) }
    var showSettings by remember { mutableStateOf(false) }

    LaunchedEffect(settings.soundEnabled) {
        vm.syncSound(settings.soundEnabled)
    }

    LaunchedEffect(Unit) {
        if (!settings.setupDone && !settings.greetingEnabled) {
            vm.update { it.copy(setupDone = true) }
        }
    }

    val visibleApps = remember(apps, settings.order, settings.hidden) {
        apps.applyLayout(settings.order, settings.hidden)
    }
    val recents = remember(apps, settings.recents) {
        settings.recents.mapNotNull { pkg -> apps.firstOrNull { it.packageName == pkg } }
    }

    val wallpaper by produceState<ImageBitmap?>(initialValue = null, settings.wallpaperPath) {
        val path = settings.wallpaperPath
        value = if (path == null) null else withContext(Dispatchers.IO) { Wallpapers.load(path) }
    }

    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val saved = withContext(Dispatchers.IO) { Wallpapers.save(context, uri) }
                if (saved != null) vm.update { it.copy(wallpaperPath = saved) }
            }
        }
    }

    BackHandler(enabled = showSettings) { showSettings = false }

    // Memory pressure folds into the same economy flag the UI already understands.
    val effectiveSettings = if (pressure) settings.copy(reducedEffects = true) else settings

    Box(Modifier.fillMaxSize().background(Color.Black)) {

        HomeScreen(
            apps = visibleApps,
            recents = recents,
            theme = theme,
            settings = effectiveSettings,
            wallpaper = wallpaper,
            entered = entered,
            onLaunch = { entry -> vm.launch(entry) },
            onOpenSettings = { showSettings = true },
            onCycleTheme = {
                val all = ThemeId.entries
                val next = all[(all.indexOf(settings.theme) + 1) % all.size]
                vm.update { it.copy(theme = next) }
            },
            onFocusSound = { vm.moveFocus() },
        )

        AnimatedVisibility(
            visible = showSettings,
            enter = fadeIn(tween(200)) + slideInVertically(tween(240)) { it / 14 },
            exit = fadeOut(tween(160)) + slideOutVertically(tween(200)) { it / 14 },
        ) {
            SettingsScreen(
                settings = settings,
                theme = theme,
                device = vm.device,
                apps = apps,
                onUpdate = { block -> vm.update(block) },
                onClose = { showSettings = false },
                onReplayHello = {
                    showSettings = false
                    entered = false
                    helloVisible = true
                },
                onPickWallpaper = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                onClearWallpaper = {
                    val old = settings.wallpaperPath
                    vm.update { it.copy(wallpaperPath = null) }
                    Wallpapers.delete(old)
                },
                onClearCache = { IconLoader.clear() },
                onRestoreLauncher = {
                    runCatching {
                        @Suppress("DEPRECATION")
                        context.packageManager.clearPackagePreferredActivities(context.packageName)
                    }
                    val home = Intent(AndroidSettings.ACTION_HOME_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    runCatching { context.startActivity(home) }.onFailure {
                        runCatching {
                            context.startActivity(
                                Intent(AndroidSettings.ACTION_SETTINGS)
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        }
                    }
                },
            )
        }

        if (helloVisible) {
            HelloScreen(
                onFinished = {
                    helloVisible = false
                    entered = true
                    vm.update { it.copy(setupDone = true) }
                }
            )
        }
    }
}
