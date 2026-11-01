package os.rose.launcher.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.rose.launcher.apps.AppEntry
import os.rose.launcher.core.Settings
import os.rose.launcher.ui.components.AppTile
import os.rose.launcher.ui.components.RecentTile
import os.rose.launcher.ui.components.RoseBackground
import os.rose.launcher.ui.components.RoseButton
import os.rose.launcher.ui.components.RoseMark
import os.rose.launcher.ui.components.SectionLabel
import os.rose.launcher.ui.components.formatClock
import os.rose.launcher.ui.components.formatDate
import os.rose.launcher.ui.components.rememberNow
import os.rose.launcher.ui.theme.RoseTheme
import kotlinx.coroutines.delay

private val SideInset = 56.dp

@Composable
fun HomeScreen(
    apps: List<AppEntry>,
    recents: List<AppEntry>,
    theme: RoseTheme,
    settings: Settings,
    wallpaper: ImageBitmap?,
    entered: Boolean,
    onLaunch: (AppEntry) -> Unit,
    onOpenSettings: () -> Unit,
    onCycleTheme: () -> Unit,
    onFocusSound: () -> Unit,
) {
    val reduceMotion = settings.reduceMotion
    val intensity = settings.animationIntensity
    val economy = settings.economy
    val columns = theme.tile.columns

    // Subtle depth: the backdrop drifts a few pixels with the focus column.
    var focusColumn by remember { mutableStateOf(0.5f) }
    val parallaxTarget = (focusColumn - 0.5f) * 2f * theme.decor.parallaxDp
    val parallaxDp by animateFloatAsState(
        targetValue = parallaxTarget,
        animationSpec = androidx.compose.animation.core.spring(0.9f, 300f),
        label = "parallax",
    )
    val parallaxPx = with(LocalDensity.current) { parallaxDp.dp.toPx() }

    val now = rememberNow()
    val clock = formatClock(now.value.time)
    val date = formatDate(now.value.time)

    val firstTile = remember { FocusRequester() }

    LaunchedEffect(entered, apps.size) {
        if (entered && apps.isNotEmpty()) {
            delay(140)
            runCatching { firstTile.requestFocus() }
        }
    }

    Box(Modifier.fillMaxSize()) {
        RoseBackground(theme, wallpaper, parallaxPx, economy)

        Column(Modifier.fillMaxSize()) {

            // ---------------------------------------------------------- top
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = SideInset, end = SideInset, top = 32.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (settings.showClock) {
                    Column {
                        BasicText(
                            text = clock,
                            style = TextStyle(
                                color = theme.palette.onBg,
                                fontSize = theme.type.clockSize,
                                fontFamily = theme.type.display,
                                fontWeight = theme.type.clockWeight,
                                letterSpacing = if (theme.type.uppercaseLabels) 2.sp else (-0.5).sp,
                            ),
                        )
                        if (settings.showDate) {
                            Spacer(Modifier.height(2.dp))
                            BasicText(
                                text = if (theme.type.uppercaseLabels) date.uppercase() else date,
                                style = TextStyle(
                                    color = theme.palette.onBgDim,
                                    fontSize = theme.type.dateSize,
                                    fontFamily = theme.type.ui,
                                    letterSpacing = if (theme.type.uppercaseLabels) 1.6.sp else 0.4.sp,
                                ),
                            )
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                RoseMark(theme)
            }

            // --------------------------------------------------------- apps
            if (apps.isEmpty()) {
                Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                    BasicText(
                        text = "Looking for apps…",
                        style = TextStyle(
                            color = theme.palette.onBgDim,
                            fontSize = 16.sp,
                            fontFamily = theme.type.ui,
                            textAlign = TextAlign.Center,
                        ),
                    )
                }
            } else {
                val rows = remember(apps, columns) { apps.chunked(columns) }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(
                        start = SideInset,
                        end = SideInset,
                        top = 18.dp,
                        bottom = 10.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(theme.tile.gap),
                ) {
                    if (recents.isNotEmpty() && settings.showRecents) {
                        item(key = "recents-label") {
                            SectionLabel("Recently used", theme)
                        }
                        item(key = "recents-row") {
                            Row(
                                Modifier.fillMaxWidth().height(72.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                val shown = recents.take(5)
                                shown.forEach { entry ->
                                    RecentTile(
                                        entry = entry,
                                        theme = theme,
                                        reduceMotion = reduceMotion,
                                        onActivate = { onLaunch(entry) },
                                        modifier = Modifier.weight(1f),
                                    )
                                }
                                repeat(5 - shown.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }

                    item(key = "apps-label") {
                        SectionLabel("Apps", theme)
                    }

                    itemsIndexed(rows, key = { _, row -> row.first().packageName }) { rowIndex, row ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(theme.tile.gap),
                        ) {
                            row.forEachIndexed { col, entry ->
                                val globalIndex = rowIndex * columns + col
                                var tileModifier = Modifier.weight(1f).height(theme.tile.height)
                                if (globalIndex == 0) tileModifier = tileModifier.focusRequester(firstTile)

                                AppTile(
                                    entry = entry,
                                    theme = theme,
                                    index = globalIndex,
                                    entered = entered,
                                    intensity = intensity,
                                    reduceMotion = reduceMotion,
                                    economy = economy,
                                    onFocusGained = {
                                        focusColumn = if (columns <= 1) 0.5f
                                        else col.toFloat() / (columns - 1)
                                        onFocusSound()
                                    },
                                    onActivate = { onLaunch(entry) },
                                    modifier = tileModifier,
                                )
                            }
                            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                        }
                    }
                }
            }

            // --------------------------------------------------------- dock
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = SideInset, end = SideInset, top = 4.dp, bottom = 26.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RoseButton(
                    label = "Settings",
                    theme = theme,
                    onClick = onOpenSettings,
                    reduceMotion = reduceMotion,
                )
                RoseButton(
                    label = theme.name,
                    theme = theme,
                    onClick = onCycleTheme,
                    reduceMotion = reduceMotion,
                    accentDot = true,
                )
                Spacer(Modifier.weight(1f))
                BasicText(
                    text = if (theme.type.uppercaseLabels) "OK TO OPEN" else "Press OK to open",
                    style = TextStyle(
                        color = theme.palette.onBg.copy(alpha = 0.32f),
                        fontSize = 11.sp,
                        fontFamily = theme.type.ui,
                        letterSpacing = 1.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            }
        }
    }
}
