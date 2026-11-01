package os.rose.launcher.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.Canvas as ComposeCanvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import os.rose.launcher.ui.theme.RoseTheme

/**
 * The backdrop for every theme. Everything here is static: gradients and one
 * tiled shader, no continuously running animation, so idle CPU stays at zero.
 */
@Composable
fun RoseBackground(
    theme: RoseTheme,
    wallpaper: ImageBitmap?,
    parallax: Float,
    economy: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxSize()) {

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = parallax }
        ) {
            if (wallpaper != null) {
                Image(
                    bitmap = wallpaper,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
                Box(
                    Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.42f))
                )
            } else {
                Box(
                    Modifier
                        .fillMaxSize()
                        .drawBehind {
                            drawRect(
                                brush = Brush.linearGradient(
                                    *theme.palette.bgStops.toTypedArray(),
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, size.height),
                                )
                            )
                            if (theme.decor.orbs && !economy) drawOrbs(theme)
                        }
                )
            }
        }

        if (theme.decor.dots && !economy) {
            DotGrid(
                color = Color.White.copy(alpha = 0.13f),
                step = 26.dp,
                radius = 1.1.dp,
            )
        }

        if (theme.decor.vignette > 0f && !economy) {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawRect(
                            brush = Brush.radialGradient(
                                0f to Color.Transparent,
                                0.62f to Color.Transparent,
                                1f to Color.Black.copy(alpha = theme.decor.vignette),
                                center = Offset(size.width * 0.5f, size.height * 0.46f),
                                radius = size.maxDimension * 0.78f,
                            )
                        )
                    }
            )
        }
    }
}

private fun DrawScope.drawOrbs(theme: RoseTheme) {
    val a = theme.palette.accent.copy(alpha = 0.30f)
    val b = theme.palette.accent.copy(alpha = 0.16f)
    val big = size.minDimension * 0.9f

    drawCircle(
        brush = Brush.radialGradient(
            0f to a,
            1f to Color.Transparent,
            center = Offset(size.width * 0.16f, size.height * 0.10f),
            radius = big,
        ),
        radius = big,
        center = Offset(size.width * 0.16f, size.height * 0.10f),
    )
    drawCircle(
        brush = Brush.radialGradient(
            0f to b,
            1f to Color.Transparent,
            center = Offset(size.width * 0.92f, size.height * 0.88f),
            radius = big,
        ),
        radius = big,
        center = Offset(size.width * 0.92f, size.height * 0.88f),
    )
}

/**
 * A single tiled 1-bit shader instead of thousands of drawCircle calls.
 */
@Composable
fun DotGrid(color: Color, step: Dp, radius: Dp) {
    val density = LocalDensity.current
    val brush = remember(color, step, radius, density) {
        val cell = with(density) { step.toPx() }.toInt().coerceAtLeast(6)
        val r = with(density) { radius.toPx() }
        val bmp = Bitmap.createBitmap(cell, cell, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color.toArgb() }
        canvas.drawCircle(cell / 2f, cell / 2f, r, paint)
        ShaderBrush(ImageShader(bmp.asImageBitmap(), TileMode.Repeated, TileMode.Repeated))
    }
    ComposeCanvas(Modifier.fillMaxSize()) {
        drawRect(brush = brush)
    }
}
