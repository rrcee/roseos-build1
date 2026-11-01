package os.rose.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.rose.launcher.ui.theme.RoseTheme

/**
 * One focusable + clickable pattern for the whole app. `clickable` gives us
 * correct D-pad CENTER/ENTER handling and accessibility semantics; the ripple
 * is disabled because focus feedback is the interaction language here.
 */
@Composable
fun focusClickable(
    onClick: () -> Unit,
    onFocusChanged: (Boolean) -> Unit = {},
): Modifier = Modifier
    .onFocusChanged { onFocusChanged(it.isFocused) }
    .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick,
    )

@Composable
fun SectionLabel(text: String, theme: RoseTheme, modifier: Modifier = Modifier) {
    BasicText(
        text = if (theme.type.uppercaseLabels) text.uppercase() else text,
        modifier = modifier,
        style = TextStyle(
            color = theme.palette.onBgDim,
            fontSize = if (theme.type.uppercaseLabels) 12.sp else 12.sp,
            letterSpacing = if (theme.type.uppercaseLabels) 2.sp else 1.2.sp,
            fontFamily = theme.type.ui,
            fontWeight = FontWeight.Medium,
        ),
    )
}

/**
 * Dock / settings pill.
 */
@Composable
fun RoseButton(
    label: String,
    theme: RoseTheme,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    reduceMotion: Boolean = false,
    accentDot: Boolean = false,
) {
    var focused by remember { mutableStateOf(false) }
    val ms = if (reduceMotion) 0 else 140
    val bg by animateColorAsState(
        if (focused) theme.palette.tileFocused else theme.palette.dock,
        tween(ms),
        label = "btnBg",
    )
    val fg by animateColorAsState(
        if (focused) theme.palette.labelFocused else theme.palette.onBgDim,
        tween(ms),
        label = "btnFg",
    )
    val ring by animateColorAsState(
        if (focused) theme.palette.tileBorderFocused else theme.palette.dockBorder,
        tween(ms),
        label = "btnRing",
    )
    val scale by animateFloatAsState(
        if (focused && !reduceMotion) 1.06f else 1f,
        tween(ms),
        label = "btnScale",
    )
    val shape = RoundedCornerShape(percent = 50)

    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(bg)
            .border(1.dp, ring, shape)
            .then(focusClickable(onClick = onClick) { focused = it })
            .height(44.dp)
            .padding(horizontal = 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (accentDot) {
            Box(Modifier.size(6.dp).background(theme.palette.accent, CircleShape))
            Spacer(Modifier.width(10.dp))
        }
        BasicText(
            text = if (theme.type.uppercaseLabels) label.uppercase() else label,
            style = TextStyle(
                color = fg,
                fontSize = 14.sp,
                fontFamily = theme.type.ui,
                letterSpacing = if (theme.type.uppercaseLabels) 1.4.sp else 0.2.sp,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The Rose bloom mark. Drawn, not a bitmap: two arcs and a dot.
 */
@Composable
fun RoseMark(theme: RoseTheme, modifier: Modifier = Modifier, label: Boolean = true) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(18.dp)
                .border(1.6.dp, theme.palette.onBg.copy(alpha = 0.55f), CircleShape)
        ) {
            Box(
                Modifier
                    .align(Alignment.Center)
                    .size(6.dp)
                    .background(theme.palette.accent, CircleShape)
            )
        }
        if (label) {
            Spacer(Modifier.width(10.dp))
            BasicText(
                text = "ROSE OS",
                style = TextStyle(
                    color = theme.palette.onBg.copy(alpha = 0.55f),
                    fontSize = 11.sp,
                    letterSpacing = 2.4.sp,
                    fontFamily = theme.type.ui,
                    fontWeight = FontWeight.Medium,
                ),
            )
        }
    }
}

/** Wide "recently used" card: icon left, label right. */
@Composable
fun RecentTile(
    entry: os.rose.launcher.apps.AppEntry,
    theme: RoseTheme,
    reduceMotion: Boolean,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var focused by remember { mutableStateOf(false) }
    val ms = if (reduceMotion) 0 else 150
    val bg by animateColorAsState(
        if (focused) theme.palette.tileFocused else theme.palette.tile,
        tween(ms),
        label = "recentBg",
    )
    val ring by animateColorAsState(
        if (focused) theme.palette.tileBorderFocused else theme.palette.tileBorder,
        tween(ms),
        label = "recentRing",
    )
    val fg by animateColorAsState(
        if (focused) theme.palette.labelFocused else theme.palette.label,
        tween(ms),
        label = "recentFg",
    )
    val scale by animateFloatAsState(
        if (focused && !reduceMotion) 1.04f else 1f,
        tween(ms),
        label = "recentScale",
    )
    val shape = RoundedCornerShape(theme.tile.corner * 0.8f)

    Row(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(bg)
            .border(1.dp, ring, shape)
            .then(focusClickable(onClick = onActivate) { focused = it })
            .fillMaxHeight()
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppIcon(packageName = entry.packageName, size = 30.dp, theme = theme, label = entry.label)
        Spacer(Modifier.width(12.dp))
        BasicText(
            text = if (theme.type.uppercaseLabels) entry.label.uppercase() else entry.label,
            style = TextStyle(
                color = fg,
                fontSize = 13.sp,
                fontFamily = theme.type.label,
                letterSpacing = theme.type.labelTracking,
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** Small icon loader shared by recents and settings rows. */
@Composable
fun AppIcon(
    packageName: String,
    size: androidx.compose.ui.unit.Dp,
    theme: RoseTheme,
    label: String,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val density = androidx.compose.ui.platform.LocalDensity.current
    val px = with(density) { size.toPx() }.toInt().coerceIn(24, 160)
    var bmp by remember(packageName, px) {
        mutableStateOf(os.rose.launcher.core.IconLoader.peek(packageName, px))
    }
    androidx.compose.runtime.LaunchedEffect(packageName, px) {
        if (bmp == null) bmp = os.rose.launcher.core.IconLoader.load(context, packageName, px)
    }
    val image = bmp
    if (image != null) {
        androidx.compose.foundation.Image(
            bitmap = image,
            contentDescription = null,
            modifier = Modifier.size(size),
        )
    } else {
        Box(
            Modifier.size(size).clip(RoundedCornerShape(6.dp))
                .background(theme.palette.onBg.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            BasicText(
                text = label.take(1).uppercase(),
                style = TextStyle(color = theme.palette.onBg.copy(alpha = 0.5f), fontSize = 12.sp),
            )
        }
    }
}

/** A single hairline divider that respects the theme. */
@Composable
fun RoseDivider(theme: RoseTheme, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(1.dp)
            .background(theme.palette.onBg.copy(alpha = 0.08f))
    )
}
