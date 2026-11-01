package os.rose.launcher.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.rose.launcher.BuildConfig
import os.rose.launcher.apps.AppEntry
import os.rose.launcher.apps.applyLayout
import os.rose.launcher.core.DeviceProfile
import os.rose.launcher.core.IconLoader
import os.rose.launcher.core.Settings
import os.rose.launcher.ui.components.AppIcon
import os.rose.launcher.ui.components.RoseBackground
import os.rose.launcher.ui.components.RoseButton
import os.rose.launcher.ui.components.SectionLabel
import os.rose.launcher.ui.components.focusClickable
import os.rose.launcher.ui.theme.RoseTheme
import os.rose.launcher.ui.theme.Themes

private val Inset = 56.dp
private val TABS = listOf("Appearance", "Home", "Personal", "Performance", "About")
private val INTENSITY_LABELS = listOf("Subtle", "Balanced", "Lively")
private val INTENSITY_VALUES = listOf(0.55f, 1f, 1.35f)

@Composable
fun SettingsScreen(
    settings: Settings,
    theme: RoseTheme,
    device: DeviceProfile,
    apps: List<AppEntry>,
    onUpdate: ((Settings) -> Settings) -> Unit,
    onClose: () -> Unit,
    onReplayHello: () -> Unit,
    onPickWallpaper: () -> Unit,
    onClearWallpaper: () -> Unit,
    onClearCache: () -> Unit,
    onRestoreLauncher: () -> Unit,
) {
    var tab by remember { mutableStateOf(0) }
    val reduceMotion = settings.reduceMotion

    Box(Modifier.fillMaxSize()) {
        RoseBackground(theme, null, 0f, economy = true)
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.46f)))

        Column(
            Modifier
                .fillMaxSize()
                .padding(start = Inset, end = Inset, top = 30.dp, bottom = 26.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                RoseButton("Back", theme, onClick = onClose, reduceMotion = reduceMotion)
                Spacer(Modifier.width(20.dp))
                BasicText(
                    "Settings",
                    style = TextStyle(
                        color = theme.palette.onBg,
                        fontSize = 22.sp,
                        fontFamily = theme.type.display,
                        fontWeight = FontWeight.Light,
                        letterSpacing = 0.sp,
                    ),
                )
                Spacer(Modifier.weight(1f))
                BasicText(
                    "ROSE OS",
                    style = TextStyle(
                        color = theme.palette.onBg.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        letterSpacing = 2.4.sp,
                        fontFamily = theme.type.ui,
                    ),
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TABS.forEachIndexed { index, label ->
                    TabChip(label, tab == index, theme, reduceMotion) { tab = index }
                }
            }

            Spacer(Modifier.height(18.dp))

            val ordered = remember(apps, settings.order) { apps.applyLayout(settings.order, emptySet()) }

            fun moveApp(pkg: String, delta: Int) {
                onUpdate { s ->
                    val base = apps.applyLayout(s.order, emptySet()).map { it.packageName }
                    val from = base.indexOf(pkg)
                    val to = from + delta
                    if (from < 0 || to < 0 || to >= base.size) s else {
                        val m = base.toMutableList()
                        m.removeAt(from)
                        m.add(to, pkg)
                        s.copy(order = m)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentPadding = PaddingValues(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                when (tab) {
                    0 -> {
                        item(key = "a-theme") { SectionLabel("Theme", theme) }
                        items(Themes.all, key = { "theme-${it.id.key}" }) { candidate ->
                            SettingCard(theme, reduceMotion, onClick = { onUpdate { it.copy(theme = candidate.id) } }) {
                                TitleBlock(candidate.name, null, theme)
                                if (settings.theme == candidate.id) Dot(theme.palette.accent)
                            }
                        }
                        item(key = "a-wall") { Spacer(Modifier.height(6.dp)) }
                        item(key = "a-wall-label") { SectionLabel("Wallpaper", theme) }
                        item(key = "a-wall-pick") {
                            SettingCard(theme, reduceMotion, onPickWallpaper) {
                                TitleBlock("Choose an image", "Any picture on this device", theme)
                                Chevron(theme)
                            }
                        }
                        if (settings.wallpaperPath != null) {
                            item(key = "a-wall-clear") {
                                SettingCard(theme, reduceMotion, onClearWallpaper) {
                                    TitleBlock("Use the theme background", "Removes the custom image", theme)
                                    Chevron(theme)
                                }
                            }
                        }
                        item(key = "a-motion") { Spacer(Modifier.height(6.dp)) }
                        item(key = "a-motion-label") { SectionLabel("Motion", theme) }
                        item(key = "a-intensity") {
                            val index = INTENSITY_VALUES.indexOfFirst { it == settings.animationIntensity }.let { if (it < 0) 1 else it }
                            SettingCard(
                                theme, reduceMotion,
                                onClick = { onUpdate { it.copy(animationIntensity = INTENSITY_VALUES[(index + 1) % INTENSITY_VALUES.size]) } },
                                onLeft = { onUpdate { it.copy(animationIntensity = INTENSITY_VALUES[(index - 1 + INTENSITY_VALUES.size) % INTENSITY_VALUES.size]) } },
                                onRight = { onUpdate { it.copy(animationIntensity = INTENSITY_VALUES[(index + 1) % INTENSITY_VALUES.size]) } },
                            ) {
                                TitleBlock("Animation intensity", "How far focus and entrances travel", theme)
                                Value("‹ ${INTENSITY_LABELS[index]} ›", theme, true)
                            }
                        }
                        item(key = "a-reduce") {
                            ToggleRow("Reduce motion", "No scaling, instant transitions", settings.reduceMotion, theme, reduceMotion) {
                                onUpdate { s -> s.copy(reduceMotion = it) }
                            }
                        }
                    }
                    1 -> {
                        item(key = "h-label") { SectionLabel("Home screen", theme) }
                        item(key = "h-recents") {
                            ToggleRow("Recently used row", "Show the last apps you opened", settings.showRecents, theme, reduceMotion) {
                                onUpdate { s -> s.copy(showRecents = it) }
                            }
                        }
                        item(key = "h-hint") {
                            BasicText(
                                "◀ ▶ to reorder · OK to hide or show",
                                modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 2.dp),
                                style = TextStyle(color = theme.palette.onBgDim, fontSize = 11.sp, fontFamily = theme.type.ui, letterSpacing = 0.6.sp),
                            )
                        }
                        items(ordered, key = { "app-${it.packageName}" }) { entry ->
                            val hidden = entry.packageName in settings.hidden
                            SettingCard(
                                theme, reduceMotion,
                                onClick = { onUpdate { s -> s.copy(hidden = if (hidden) s.hidden - entry.packageName else s.hidden + entry.packageName) } },
                                onLeft = { moveApp(entry.packageName, -1) },
                                onRight = { moveApp(entry.packageName, 1) },
                            ) {
                                AppIcon(packageName = entry.packageName, size = 26.dp, theme = theme, label = entry.label)
                                Spacer(Modifier.width(14.dp))
                                TitleBlock(entry.label, entry.packageName, theme)
                                Value(if (hidden) "Hidden" else "Shown", theme, !hidden)
                            }
                        }
                        item(key = "h-reset") {
                            SettingCard(theme, reduceMotion, onClick = { onUpdate { it.copy(order = emptyList(), hidden = emptySet()) } }) {
                                TitleBlock("Restore default layout", "Alphabetical, everything visible", theme)
                                Chevron(theme)
                            }
                        }
                    }
                    2 -> {
                        item(key = "p-label") { SectionLabel("Greeting", theme) }
                        item(key = "p-replay") { SettingCard(theme, reduceMotion, onReplayHello) {
                            TitleBlock("Replay the Hello animation", "Play the welcome again", theme)
                            Chevron(theme)
                        }}
                        item(key = "p-greeting") {
                            ToggleRow("Greeting on first run", "Show the welcome on a fresh install", settings.greetingEnabled, theme, reduceMotion) {
                                onUpdate { s -> s.copy(greetingEnabled = it) }
                            }
                        }
                        item(key = "p-clock-label") { Spacer(Modifier.height(6.dp)) }
                        item(key = "p-clock-l") { SectionLabel("Clock", theme) }
                        item(key = "p-clock") {
                            ToggleRow("Show the clock", "Current time in the top corner", settings.showClock, theme, reduceMotion) {
                                onUpdate { s -> s.copy(showClock = it) }
                            }
                        }
                        item(key = "p-date") {
                            ToggleRow("Show the date", "Today below the clock", settings.showDate, theme, reduceMotion) {
                                onUpdate { s -> s.copy(showDate = it) }
                            }
                        }
                        item(key = "p-sound-label") { Spacer(Modifier.height(6.dp)) }
                        item(key = "p-sound-l") { SectionLabel("Sound", theme) }
                        item(key = "p-sound") {
                            ToggleRow("Interface sounds", "Quiet tones on focus and open", settings.soundEnabled, theme, reduceMotion) {
                                onUpdate { s -> s.copy(soundEnabled = it) }
                            }
                        }
                    }
                    3 -> {
                        item(key = "f-label") { SectionLabel("Performance", theme) }
                        item(key = "f-perf") {
                            ToggleRow("Performance mode", "Turns off glows, orbs and depth", settings.performanceMode, theme, reduceMotion) {
                                onUpdate { s -> s.copy(performanceMode = it) }
                            }
                        }
                        item(key = "f-reduced") {
                            ToggleRow("Reduced effects", "Static backgrounds, no parallax", settings.reducedEffects, theme, reduceMotion) {
                                onUpdate { s -> s.copy(reducedEffects = it) }
                            }
                        }
                        item(key = "f-cache") {
                            SettingCard(theme, reduceMotion, onClearCache) {
                                TitleBlock("Clear image cache", "Frees icon memory", theme)
                                Chevron(theme)
                            }
                        }
                        item(key = "f-info") { Spacer(Modifier.height(6.dp)) }
                        item(key = "f-info-l") { SectionLabel("This device", theme) }
                        item(key = "f-mem") { InfoRow("Icon cache in use", "${IconLoader.usedKb()} KB", theme) }
                        item(key = "f-ram") { InfoRow("Total memory", "${device.totalRamMb} MB", theme) }
                        item(key = "f-class") {
                            InfoRow("Memory class", "${device.memoryClassMb} MB${if (device.constrained) " · low" else ""}", theme)
                        }
                        item(key = "f-free") {
                            InfoRow("Free storage", if (device.freeStorageMb >= 0) "${device.freeStorageMb} MB" else "unknown", theme)
                        }
                        item(key = "f-version") { InfoRow("Rose OS version", BuildConfig.VERSION_NAME, theme) }
                    }
                    else -> {
                        item(key = "b-label") { SectionLabel("About", theme) }
                        item(key = "b-name") { InfoRow("Rose OS", "Version ${BuildConfig.VERSION_NAME}", theme) }
                        item(key = "b-device") { InfoRow("Device", "${device.manufacturer} ${device.model}", theme) }
                        item(key = "b-android") { InfoRow("Android", "${device.androidRelease} (SDK ${device.sdk})", theme) }
                        item(key = "b-res") { InfoRow("Display", "${device.resolution} · ${device.densityDpi} dpi", theme) }
                        item(key = "b-gpu") { InfoRow("GPU", device.gpu, theme) }
                        item(key = "b-launcher") {
                            SettingCard(theme, reduceMotion, onRestoreLauncher) {
                                TitleBlock("Switch back to the original launcher", "Clears Rose OS as the default home app", theme)
                                Chevron(theme)
                            }
                        }
                        item(key = "b-lic-label") { Spacer(Modifier.height(8.dp)) }
                        item(key = "b-lic-title") { SectionLabel("Open source", theme) }
                        item(key = "b-lic") {
                            BasicText(
                                LICENCES,
                                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(theme.palette.tile).padding(20.dp),
                                style = TextStyle(color = theme.palette.onBgDim, fontSize = 11.sp, lineHeight = 18.sp,
                                    fontFamily = theme.type.ui, textAlign = androidx.compose.ui.text.style.TextAlign.Start),
                            )
                        }
                        item(key = "b-privacy") {
                            BasicText(
                                "Rose OS makes no network requests, collects no data and contains no advertising or analytics.",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
                                style = TextStyle(color = theme.palette.onBg.copy(alpha = 0.45f), fontSize = 11.sp, fontFamily = theme.type.ui),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabChip(label: String, selected: Boolean, theme: RoseTheme, reduceMotion: Boolean, onClick: () -> Unit) {
    var focused by remember { mutableStateOf(false) }
    val ms = if (reduceMotion) 0 else 140
    val bg by animateColorAsState(
        when { focused -> theme.palette.tileFocused; selected -> theme.palette.tile; else -> Color.Transparent },
        tween(ms), label = "tabBg",
    )
    val fg by animateColorAsState(
        when { focused -> theme.palette.labelFocused; selected -> theme.palette.onBg; else -> theme.palette.onBgDim },
        tween(ms), label = "tabFg",
    )
    val shape = RoundedCornerShape(percent = 50)

    Box(
        Modifier.clip(shape).background(bg).border(1.dp, if (selected) theme.palette.tileBorderFocused else theme.palette.tileBorder, shape)
            .then(focusClickable(onClick) { focused = it }).height(38.dp).padding(horizontal = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(text = label, style = TextStyle(color = fg, fontSize = 13.sp, fontFamily = theme.type.ui, letterSpacing = 0.2.sp))
    }
}

@Composable
private fun SettingCard(
    theme: RoseTheme, reduceMotion: Boolean,
    onClick: () -> Unit, onLeft: (() -> Unit)? = null, onRight: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    val ms = if (reduceMotion) 0 else 140
    val bg by animateColorAsState(if (focused) theme.palette.tileFocused else theme.palette.tile, tween(ms), label = "cardBg")
    val ring by animateColorAsState(if (focused) theme.palette.tileBorderFocused else theme.palette.tileBorder, tween(ms), label = "cardRing")
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier.fillMaxWidth().clip(shape).background(bg).border(1.dp, ring, shape)
            .onKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onKeyEvent false
                when (event.key) {
                    Key.DirectionLeft -> { onLeft?.invoke(); onLeft != null }
                    Key.DirectionRight -> { onRight?.invoke(); onRight != null }
                    else -> false
                }
            }
            .then(focusClickable(onClick) { focused = it })
            .padding(horizontal = 20.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

@Composable
private fun RowScope.TitleBlock(title: String, subtitle: String?, theme: RoseTheme) {
    Column(Modifier.weight(1f)) {
        BasicText(text = title, style = TextStyle(color = theme.palette.onBg, fontSize = 15.sp, fontFamily = theme.type.ui, letterSpacing = 0.sp))
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(2.dp))
            BasicText(text = subtitle, style = TextStyle(color = theme.palette.onBgDim, fontSize = 11.sp, fontFamily = theme.type.ui))
        }
    }
}

@Composable
private fun ToggleRow(
    title: String, subtitle: String?, value: Boolean, theme: RoseTheme, reduceMotion: Boolean, onChange: (Boolean) -> Unit,
) {
    SettingCard(theme, reduceMotion, { onChange(!value) }) {
        TitleBlock(title, subtitle, theme)
        SwitchPill(value, theme)
    }
}

@Composable
private fun SwitchPill(on: Boolean, theme: RoseTheme) {
    val track by animateColorAsState(if (on) theme.palette.accent else theme.palette.onBg.copy(alpha = 0.16f), tween(160), label = "switchTrack")
    Box(Modifier.width(46.dp).height(26.dp).clip(CircleShape).background(track)) {
        Box(
            Modifier.align(if (on) Alignment.CenterEnd else Alignment.CenterStart).padding(3.dp).size(20.dp)
                .background(if (on) Color(0xFF111111) else Color.White, CircleShape),
        )
    }
}

@Composable
private fun Value(text: String, theme: RoseTheme, active: Boolean) {
    BasicText(text, style = TextStyle(color = if (active) theme.palette.onBg else theme.palette.onBgDim,
        fontSize = 12.sp, fontFamily = theme.type.ui, letterSpacing = 0.3.sp))
}

@Composable
private fun Chevron(theme: RoseTheme) {
    BasicText(text = "›", style = TextStyle(color = theme.palette.onBgDim, fontSize = 18.sp))
}

@Composable
private fun Dot(color: Color) {
    Box(Modifier.size(9.dp).background(color, CircleShape))
}

@Composable
private fun InfoRow(title: String, value: String, theme: RoseTheme) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(theme.palette.tile.copy(alpha = 0.5f)).padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BasicText(text = title, modifier = Modifier.weight(1f), style = TextStyle(color = theme.palette.onBgDim, fontSize = 12.sp, fontFamily = theme.type.ui))
        BasicText(text = value, style = TextStyle(color = theme.palette.onBg, fontSize = 12.sp, fontFamily = theme.type.ui))
    }
}

private const val LICENCES = """AndroidX / Jetpack Compose
Copyright (C) The Android Open Source Project
Licensed under the Apache License, Version 2.0.

Kotlin standard library
Copyright (C) JetBrains s.r.o.
Licensed under the Apache License, Version 2.0.

Inter / Space Grotesk / Instrument Serif / Doto
All typefaces are licensed under the SIL Open Font License, 1.1.

Rose OS bundles no advertising SDKs, no analytics libraries and no
third-party network services."""