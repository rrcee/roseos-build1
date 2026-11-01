package os.rose.launcher.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import os.rose.launcher.core.ThemeId

/**
 * The four Rose OS identities. Same bones, different soul.
 */
object Themes {

    // ---------------------------------------------------------------- LIQUID
    val Liquid = RoseTheme(
        id = ThemeId.LIQUID,
        palette = Palette(
            bgStops = stops(
                0.00f to Color(0xFF05081C),
                0.32f to Color(0xFF101B57),
                0.66f to Color(0xFF3A2378),
                1.00f to Color(0xFF7A47B5),
            ),
            accent = Color(0xFFB9A0FF),
            onBg = Color.White,
            onBgDim = Color(0xB3FFFFFF),
            tile = Color(0x1AFFFFFF),
            tileFocused = Color(0x38FFFFFF),
            tileBorder = Color(0x33FFFFFF),
            tileBorderFocused = Color(0xA6FFFFFF),
            label = Color(0xF2FFFFFF),
            labelFocused = Color.White,
            glow = Color(0x668B5CF6),
            dock = Color(0x14FFFFFF),
            dockBorder = Color(0x2EFFFFFF),
            scrim = Color(0x66000000),
        ),
        type = TypeSpec(
            display = RoseFonts.Inter,
            ui = RoseFonts.Inter,
            label = RoseFonts.Inter,
            clockSize = 46.sp,
            dateSize = 14.sp,
            labelSize = 14.sp,
            labelTracking = 0.2.sp,
            uppercaseLabels = false,
            clockWeight = FontWeight.Light,
        ),
        tile = tileSpec(height = 104.dp, icon = 42.dp, corner = 22.dp),
        motion = MotionSpec(
            focusScale = 1.08f,
            focusMs = 170,
            enterMs = 460,
            enterStaggerMs = 24,
            damping = 0.62f,
            stiffness = 480f,
        ),
        decor = DecorSpec(
            orbs = true,
            dots = false,
            glassHighlight = true,
            focusRing = false,
            vignette = 0.35f,
            parallaxDp = 10f,
        ),
    )

    // --------------------------------------------------------------- NOTHING
    val Nothing = RoseTheme(
        id = ThemeId.NOTHING,
        palette = Palette(
            bgStops = stops(
                0.0f to Color(0xFF000000),
                1.0f to Color(0xFF0A0A0A),
            ),
            accent = Color(0xFFD71921),
            onBg = Color.White,
            onBgDim = Color(0x80FFFFFF),
            tile = Color(0x0AFFFFFF),
            tileFocused = Color(0xFFFFFFFF),
            tileBorder = Color(0x26FFFFFF),
            tileBorderFocused = Color(0xFFFFFFFF),
            label = Color(0xE6FFFFFF),
            labelFocused = Color(0xFF000000),
            glow = Color.Transparent,
            dock = Color(0x00FFFFFF),
            dockBorder = Color(0x26FFFFFF),
            scrim = Color(0x99000000),
        ),
        type = TypeSpec(
            display = RoseFonts.Doto,
            ui = RoseFonts.Doto,
            label = RoseFonts.Doto,
            clockSize = 52.sp,
            dateSize = 13.sp,
            labelSize = 13.sp,
            labelTracking = 1.4.sp,
            uppercaseLabels = true,
            clockWeight = FontWeight.Normal,
        ),
        tile = tileSpec(height = 100.dp, icon = 38.dp, corner = 6.dp, gap = 10.dp),
        motion = MotionSpec(
            focusScale = 1.05f,
            focusMs = 120,
            enterMs = 260,
            enterStaggerMs = 14,
            damping = 0.9f,
            stiffness = 900f,
        ),
        decor = DecorSpec(
            orbs = false,
            dots = true,
            glassHighlight = false,
            focusRing = false,
            vignette = 0f,
            parallaxDp = 0f,
        ),
    )

    // --------------------------------------------------------------- APPLE TV
    val AppleTv = RoseTheme(
        id = ThemeId.APPLETV,
        palette = Palette(
            bgStops = stops(
                0.00f to Color(0xFF01030C),
                0.45f to Color(0xFF0A1136),
                1.00f to Color(0xFF22307A),
            ),
            accent = Color.White,
            onBg = Color.White,
            onBgDim = Color(0x99FFFFFF),
            tile = Color(0x0FFFFFFF),
            tileFocused = Color(0x22FFFFFF),
            tileBorder = Color(0x17FFFFFF),
            tileBorderFocused = Color(0xD9FFFFFF),
            label = Color(0xF2FFFFFF),
            labelFocused = Color.White,
            glow = Color(0xA6000000),
            dock = Color(0x0DFFFFFF),
            dockBorder = Color(0x1AFFFFFF),
            scrim = Color(0x80000000),
        ),
        type = TypeSpec(
            display = RoseFonts.Inter,
            ui = RoseFonts.Inter,
            label = RoseFonts.Inter,
            clockSize = 40.sp,
            dateSize = 13.sp,
            labelSize = 13.sp,
            labelTracking = 0.4.sp,
            uppercaseLabels = false,
            clockWeight = FontWeight.Light,
        ),
        tile = tileSpec(height = 124.dp, icon = 48.dp, corner = 16.dp, gap = 18.dp),
        motion = MotionSpec(
            focusScale = 1.12f,
            focusMs = 220,
            enterMs = 520,
            enterStaggerMs = 30,
            damping = 0.68f,
            stiffness = 420f,
        ),
        decor = DecorSpec(
            orbs = true,
            dots = false,
            glassHighlight = false,
            focusRing = true,
            vignette = 0.55f,
            parallaxDp = 14f,
        ),
    )

    // ----------------------------------------------------------------- STOCK
    val Stock = RoseTheme(
        id = ThemeId.STOCK,
        palette = Palette(
            bgStops = stops(
                0.0f to Color(0xFF0E0F12),
                1.0f to Color(0xFF17191F),
            ),
            accent = Color(0xFF8AB4F8),
            onBg = Color(0xFFE8EAED),
            onBgDim = Color(0xFF9AA0A6),
            tile = Color(0xFF1E2027),
            tileFocused = Color(0xFF272B33),
            tileBorder = Color(0x0AFFFFFF),
            tileBorderFocused = Color(0xFF8AB4F8),
            label = Color(0xFFE8EAED),
            labelFocused = Color.White,
            glow = Color.Transparent,
            dock = Color(0xFF1A1C22),
            dockBorder = Color(0x0FFFFFFF),
            scrim = Color.Transparent,
        ),
        type = TypeSpec(
            display = RoseFonts.Inter,
            ui = RoseFonts.Inter,
            label = RoseFonts.Inter,
            clockSize = 34.sp,
            dateSize = 13.sp,
            labelSize = 14.sp,
            labelTracking = 0.sp,
            uppercaseLabels = false,
            clockWeight = FontWeight.Normal,
        ),
        tile = tileSpec(height = 96.dp, icon = 38.dp, corner = 10.dp, gap = 12.dp),
        motion = MotionSpec(
            focusScale = 1.03f,
            focusMs = 90,
            enterMs = 160,
            enterStaggerMs = 8,
            damping = 1f,
            stiffness = 1200f,
        ),
        decor = DecorSpec(
            orbs = false,
            dots = false,
            glassHighlight = false,
            focusRing = false,
            vignette = 0f,
            parallaxDp = 0f,
        ),
    )

    val all = listOf(Liquid, Nothing, AppleTv, Stock)

    fun of(id: ThemeId): RoseTheme = when (id) {
        ThemeId.LIQUID -> Liquid
        ThemeId.NOTHING -> Nothing
        ThemeId.APPLETV -> AppleTv
        ThemeId.STOCK -> Stock
    }
}
