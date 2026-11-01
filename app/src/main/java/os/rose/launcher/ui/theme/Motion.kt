package os.rose.launcher.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

/**
 * One motion language for every theme: only the numbers change.
 * Intensity is a single global multiplier, so "Reduce motion" and
 * "Animation intensity" affect everything at once.
 */
object RoseMotion {

    fun focusScale(theme: RoseTheme, intensity: Float, reduceMotion: Boolean): Float {
        if (reduceMotion) return 1f
        val extra = (theme.motion.focusScale - 1f) * intensity.coerceIn(0f, 1.5f)
        return 1f + extra
    }

    fun springFor(theme: RoseTheme, reduceMotion: Boolean): SpringSpec<Float> =
        if (reduceMotion) {
            spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = 10000f)
        } else {
            spring(dampingRatio = theme.motion.damping, stiffness = theme.motion.stiffness)
        }

    fun enterSpec(theme: RoseTheme, intensity: Float, reduceMotion: Boolean) =
        if (reduceMotion) {
            tween<Float>(durationMillis = 0)
        } else {
            tween<Float>(
                durationMillis = (theme.motion.enterMs * intensity.coerceIn(0.35f, 1.5f)).toInt(),
                easing = FastOutSlowInEasing,
            )
        }
}

private const val THEME_FADE_MS = 320

/**
 * Interpolates every visual token when the theme changes, so switching
 * identity is a crossfade rather than a hard swap — and the home screen
 * keeps its focus, scroll position and composition.
 */
@Composable
fun rememberAnimatedTheme(target: RoseTheme, enabled: Boolean): RoseTheme {
    if (!enabled) return target

    val p = target.palette
    val t = target.tile
    val c = tween<Color>(THEME_FADE_MS)

    val bg0 = animateColorAsState(p.bgStops.getOrElse(0) { 0f to p.accent }.second, c, label = "bg0").value
    val bg1 = animateColorAsState(p.bgStops.getOrElse(1) { 0f to p.accent }.second, c, label = "bg1").value
    val bg2 = animateColorAsState(p.bgStops.getOrElse(2) { 0f to p.accent }.second, c, label = "bg2").value
    val bg3 = animateColorAsState(p.bgStops.getOrElse(3) { 0f to p.accent }.second, c, label = "bg3").value

    val accent = animateColorAsState(p.accent, c, label = "accent").value
    val onBg = animateColorAsState(p.onBg, c, label = "onBg").value
    val onBgDim = animateColorAsState(p.onBgDim, c, label = "onBgDim").value
    val tile = animateColorAsState(p.tile, c, label = "tile").value
    val tileFocused = animateColorAsState(p.tileFocused, c, label = "tileFocused").value
    val tileBorder = animateColorAsState(p.tileBorder, c, label = "tileBorder").value
    val tileBorderFocused = animateColorAsState(p.tileBorderFocused, c, label = "tileBorderFocused").value
    val label = animateColorAsState(p.label, c, label = "label").value
    val labelFocused = animateColorAsState(p.labelFocused, c, label = "labelFocused").value
    val glow = animateColorAsState(p.glow, c, label = "glow").value
    val dock = animateColorAsState(p.dock, c, label = "dock").value
    val dockBorder = animateColorAsState(p.dockBorder, c, label = "dockBorder").value
    val scrim = animateColorAsState(p.scrim, c, label = "scrim").value

    val height: Dp = animateDpAsState(t.height, tween(THEME_FADE_MS), label = "height").value
    val icon: Dp = animateDpAsState(t.iconSize, tween(THEME_FADE_MS), label = "icon").value
    val corner: Dp = animateDpAsState(t.corner, tween(THEME_FADE_MS), label = "corner").value

    val stops = p.bgStops.mapIndexed { i, (f, _) ->
        f to when (i) {
            0 -> bg0
            1 -> bg1
            2 -> bg2
            else -> bg3
        }
    }

    return target.copy(
        palette = p.copy(
            bgStops = stops,
            accent = accent,
            onBg = onBg,
            onBgDim = onBgDim,
            tile = tile,
            tileFocused = tileFocused,
            tileBorder = tileBorder,
            tileBorderFocused = tileBorderFocused,
            label = label,
            labelFocused = labelFocused,
            glow = glow,
            dock = dock,
            dockBorder = dockBorder,
            scrim = scrim,
        ),
        tile = t.copy(height = height, iconSize = icon, corner = corner),
    )
}
