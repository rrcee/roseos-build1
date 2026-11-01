package os.rose.launcher.ui.screens

import android.content.Context
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.Typeface
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import kotlinx.coroutines.delay
import os.rose.launcher.R
import os.rose.launcher.ui.theme.Themes

private const val WORD = "Hello,"
private const val FULL = "Hello, Master Rohith."

@Composable
fun HelloScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val palette = Themes.Liquid.palette

    val bgFade = remember { Animatable(0f) }
    val stroke = remember { Animatable(0f) }
    val fill = remember { Animatable(0f) }
    val exit = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        bgFade.animateTo(1f, tween(360, easing = FastOutSlowInEasing))
        delay(110)
        stroke.animateTo(1f, tween(1750, easing = LinearEasing))
        fill.animateTo(1f, tween(560, easing = FastOutSlowInEasing))
        delay(950)
        exit.animateTo(1f, tween(560, easing = FastOutSlowInEasing))
        onFinished()
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Brush.linearGradient(*palette.bgStops.toTypedArray()))
    ) {
        val w = constraints.maxWidth.toFloat()
        val h = constraints.maxHeight.toFloat()
        val art = remember(w, h) { buildArt(context, w, h) }

        Box(
            Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = bgFade.value }
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val e = exit.value
                val fade = 1f - e

                val glowRadius = size.width * 0.44f
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to Color.White.copy(alpha = 0.10f * fade),
                        1f to Color.Transparent,
                        center = Offset(size.width / 2f, size.height / 2f),
                        radius = glowRadius,
                    ),
                    radius = glowRadius,
                    center = Offset(size.width / 2f, size.height / 2f),
                )

                scale(
                    scaleX = 1f - 0.025f * e,
                    scaleY = 1f - 0.025f * e,
                    pivot = Offset(size.width / 2f, size.height / 2f),
                ) {
                    translate(art.leftX, art.baselineY) {
                        drawIntoCanvas { canvas ->
                            val native = canvas.nativeCanvas

                            if (fill.value < 1f && stroke.value > 0f) {
                                art.strokePaint.alpha =
                                    ((1f - fill.value) * fade * 255f).toInt().coerceIn(0, 255)
                                art.glyphs.forEachIndexed { i, measure ->
                                    val local = (stroke.value * art.glyphs.size - i).coerceIn(0f, 1f)
                                    if (local > 0.002f) {
                                        art.tmp.reset()
                                        measure.getSegment(
                                            0f,
                                            art.lengths[i] * local,
                                            art.tmp,
                                            true,
                                        )
                                        native.drawPath(art.tmp, art.strokePaint)
                                    }
                                }
                            }

                            if (fill.value > 0f) {
                                art.fillPaint.alpha =
                                    (fill.value * fade * 255f).toInt().coerceIn(0, 255)
                                native.drawText(FULL, 0f, 0f, art.fillPaint)
                            }
                        }
                    }
                }
            }
        }
    }
}

private class Art(
    val strokePaint: Paint,
    val fillPaint: Paint,
    val glyphs: List<PathMeasure>,
    val lengths: FloatArray,
    val tmp: Path,
    val leftX: Float,
    val baselineY: Float,
)

private fun buildArt(context: Context, widthPx: Float, heightPx: Float): Art {
    val typeface = ResourcesCompat.getFont(context, R.font.instrument_serif_italic)
        ?: Typeface.SERIF

    val probe = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        textSize = 100f
        letterSpacing = 0.02f
    }
    val widthAt100 = probe.measureText(FULL).coerceAtLeast(1f)
    val textSize = 100f * ((widthPx * 0.70f) / widthAt100)

    val measurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.typeface = typeface
        this.textSize = textSize
        letterSpacing = 0.02f
    }

    val glyphs = ArrayList<PathMeasure>(WORD.length)
    val lengths = FloatArray(WORD.length)
    for (i in WORD.indices) {
        val path = Path()
        val x = measurePaint.measureText(WORD, 0, i)
        measurePaint.getTextPath(WORD, i, i + 1, x, 0f, path)
        val pm = PathMeasure(path, false)
        glyphs.add(pm)
        lengths[i] = pm.length
    }

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = (textSize * 0.020f).coerceAtLeast(2.4f)
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = 0xFFFFFFFF.toInt()
    }

    val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0xFFFFFFFF.toInt()
        this.typeface = typeface
        this.textSize = textSize
        letterSpacing = 0.02f
    }

    val total = measurePaint.measureText(FULL)
    return Art(
        strokePaint = strokePaint,
        fillPaint = fillPaint,
        glyphs = glyphs,
        lengths = lengths,
        tmp = Path(),
        leftX = (widthPx - total) / 2f,
        baselineY = heightPx / 2f + textSize * 0.34f,
    )
}

