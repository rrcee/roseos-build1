package os.rose.launcher.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.rose.launcher.core.ThemeId

/**
 * Shared theme tokens. Every theme fills the same structure, so components
 * never branch on "which theme is this" — they read tokens.
 */
data class Palette(
    val bgStops: List<Pair<Float, Color>>,
    val accent: Color,
    val onBg: Color,
    val onBgDim: Color,
    val tile: Color,
    val tileFocused: Color,
    val tileBorder: Color,
    val tileBorderFocused: Color,
    val label: Color,
    val labelFocused: Color,
    val glow: Color,
    val dock: Color,
    val dockBorder: Color,
    val scrim: Color,
)

data class TypeSpec(
    val display: FontFamily,
    val ui: FontFamily,
    val label: FontFamily,
    val clockSize: TextUnit,
    val dateSize: TextUnit,
    val labelSize: TextUnit,
    val labelTracking: TextUnit,
    val uppercaseLabels: Boolean,
    val clockWeight: FontWeight,
)

data class TileSpec(
    val columns: Int,
    val height: Dp,
    val iconSize: Dp,
    val corner: Dp,
    val gap: Dp,
)

data class MotionSpec(
    /** Scale at full focus, e.g. 1.08. */
    val focusScale: Float,
    val focusMs: Int,
    val enterMs: Int,
    val enterStaggerMs: Int,
    val damping: Float,
    val stiffness: Float,
)

data class DecorSpec(
    val orbs: Boolean,
    val dots: Boolean,
    val glassHighlight: Boolean,
    val focusRing: Boolean,
    val vignette: Float,
    val parallaxDp: Float,
)

data class RoseTheme(
    val id: ThemeId,
    val palette: Palette,
    val type: TypeSpec,
    val tile: TileSpec,
    val motion: MotionSpec,
    val decor: DecorSpec,
) {
    val name: String get() = id.label
}

internal val defaultColumns = 6

internal fun stops(vararg pairs: Pair<Float, Color>): List<Pair<Float, Color>> = pairs.toList()

internal fun tileSpec(height: Dp, icon: Dp, corner: Dp, gap: Dp = 14.dp) =
    TileSpec(columns = defaultColumns, height = height, iconSize = icon, corner = corner, gap = gap)

internal val noGlow = Color.Transparent

internal val defaultMotion = MotionSpec(
    focusScale = 1.06f,
    focusMs = 180,
    enterMs = 420,
    enterStaggerMs = 26,
    damping = 0.72f,
    stiffness = 520f,
)

@Suppress("unused")
internal val baseType = TypeSpec(
    display = RoseFonts.Inter,
    ui = RoseFonts.Inter,
    label = RoseFonts.Inter,
    clockSize = 46.sp,
    dateSize = 14.sp,
    labelSize = 14.sp,
    labelTracking = 0.sp,
    uppercaseLabels = false,
    clockWeight = FontWeight.Light,
)
