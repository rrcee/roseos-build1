package os.rose.launcher.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.rose.launcher.apps.AppEntry
import os.rose.launcher.core.IconLoader
import os.rose.launcher.ui.theme.RoseMotion
import os.rose.launcher.ui.theme.RoseTheme
import kotlin.math.roundToInt

/**
 * A floating application tile.
 *
 * Everything is drawn with plain Compose primitives: a translucent fill, a
 * hairline border, an optional top highlight and an optional soft glow. No
 * backdrop blur is used, which is what keeps focus transitions smooth on a
 * Mali-450.
 */
@Composable
fun AppTile(
    entry: AppEntry,
    theme: RoseTheme,
    index: Int,
    entered: Boolean,
    intensity: Float,
    reduceMotion: Boolean,
    economy: Boolean,
    onFocusGained: (Int) -> Unit,
    onActivate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var focused by remember { mutableStateOf(false) }

    val iconPx = with(density) { theme.tile.iconSize.toPx() }.roundToInt().coerceIn(32, 192)
    var icon by remember(entry.packageName, iconPx) {
        mutableStateOf<ImageBitmap?>(IconLoader.peek(entry.packageName, iconPx))
    }
    LaunchedEffect(entry.packageName, iconPx) {
        if (icon == null) icon = IconLoader.load(context, entry.packageName, iconPx)
    }

    // ---- entrance -----------------------------------------------------------
    val enterAnim = remember { Animatable(if (entered) 1f else 0f) }
    LaunchedEffect(entered, theme.id) {
        if (entered) {
            val stagger = (index * theme.motion.enterStaggerMs * intensity).toInt().coerceAtMost(760)
            val duration = (theme.motion.enterMs * intensity).toInt().coerceIn(1, 1200)
            enterAnim.snapTo(0f)
            enterAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = duration,
                    delayMillis = if (reduceMotion) 0 else stagger,
                    easing = FastOutSlowInEasing,
                ),
            )
        } else {
            enterAnim.snapTo(0f)
        }
    }

    // ---- focus --------------------------------------------------------------
    val targetScale = if (focused) RoseMotion.focusScale(theme, intensity, reduceMotion) else 1f
    val scale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = RoseMotion.springFor(theme, reduceMotion),
        label = "tileScale",
    )
    val fadeMs = if (reduceMotion) 0 else theme.motion.focusMs
    val fill by animateColorAsState(
        if (focused) theme.palette.tileFocused else theme.palette.tile,
        tween(fadeMs),
        label = "tileFill",
    )
    val stroke by animateColorAsState(
        if (focused) theme.palette.tileBorderFocused else theme.palette.tileBorder,
        tween(fadeMs),
        label = "tileStroke",
    )
    val labelColor by animateColorAsState(
        if (focused) theme.palette.labelFocused else theme.palette.label,
        tween(fadeMs),
        label = "tileLabel",
    )
    val glowAlpha by animateFloatAsState(
        if (focused && !economy && theme.palette.glow != Color.Transparent) 1f else 0f,
        tween(if (reduceMotion) 0 else 260),
        label = "glow",
    )
    val borderWidth by animateDpAsState(
        if (focused && theme.decor.focusRing) 2.dp else 1.dp,
        tween(fadeMs),
        label = "borderWidth",
    )

    val shape = RoundedCornerShape(theme.tile.corner)

    Box(
        modifier
            .graphicsLayer {
                val p = enterAnim.value
                val e = 0.94f + 0.06f * p
                scaleX = scale * e
                scaleY = scale * e
                alpha = p
                translationY = (1f - p) * 22.dp.toPx()
            }
            .onFocusChanged { state ->
                if (state.isFocused) {
                    focused = true
                    onFocusGained(index)
                } else {
                    focused = false
                }
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onActivate,
            )
    ) {
        if (glowAlpha > 0.01f) {
            Box(
                Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = 1.18f
                        scaleY = 1.18f
                        alpha = glowAlpha
                    }
                    .background(
                        brush = Brush.radialGradient(
                            0f to theme.palette.glow,
                            1f to Color.Transparent,
                            center = Offset.Unspecified,
                        )
                    )
            )
        }

        Box(
            Modifier
                .fillMaxSize()
                .clip(shape)
                .background(fill)
                .border(borderWidth, stroke, shape)
        ) {
            if (theme.decor.glassHighlight) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(theme.tile.height * 0.55f)
                        .background(
                            Brush.verticalGradient(
                                0f to Color.White.copy(alpha = if (focused) 0.14f else 0.07f),
                                1f to Color.Transparent,
                            )
                        )
                )
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                if (icon != null) {
                    Image(
                        bitmap = icon!!,
                        contentDescription = null,
                        modifier = Modifier.size(theme.tile.iconSize),
                        filterQuality = FilterQuality.Medium,
                    )
                } else {
                    Box(
                        Modifier
                            .size(theme.tile.iconSize)
                            .clip(RoundedCornerShape(8.dp))
                            .background(theme.palette.onBg.copy(alpha = 0.08f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        BasicText(
                            text = entry.label.take(1).uppercase(),
                            style = TextStyle(
                                color = theme.palette.onBg.copy(alpha = 0.55f),
                                fontSize = (theme.tile.iconSize.value * 0.42f).sp,
                            ),
                        )
                    }
                }

                Spacer(Modifier.height(9.dp))

                BasicText(
                    text = if (theme.type.uppercaseLabels) entry.label.uppercase() else entry.label,
                    style = TextStyle(
                        color = labelColor,
                        fontSize = theme.type.labelSize,
                        fontFamily = theme.type.label,
                        letterSpacing = theme.type.labelTracking,
                        textAlign = TextAlign.Center,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (theme.decor.dots && focused) {
                Box(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(9.dp)
                        .size(5.dp)
                        .background(theme.palette.accent, CircleShape)
                )
            }
        }
    }
}
