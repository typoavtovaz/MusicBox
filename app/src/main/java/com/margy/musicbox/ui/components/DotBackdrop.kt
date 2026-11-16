package com.margy.musicbox.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sin

/**
 * Подложка из точек на весь экран: чёрный фон, сетка красно-белых точек.
 * Под музыку точки гладко вспыхивают снизу вверх по спектру и бегут волной,
 * без музыки — тихая ровная решётка.
 *
 * Рисуем через нативный Canvas.drawPoints: тысячи точек за один вызов,
 * поэтому даже 60 к/с не жрёт батарею.
 */

private const val GROUPS = 6
private const val CAPACITY = 12000

private val GROUP_COLORS = intArrayOf(
    0x26F5F5F5.toInt(), // белый тусклый
    0x7AF5F5F5.toInt(), // белый средний
    0xFFF5F5F5.toInt(), // белый яркий
    0x26D71921.toInt(), // красный тусклый
    0x7AD71921.toInt(), // красный средний
    0xFFD71921.toInt()  // красный яркий
)

private val GROUP_WIDTH = floatArrayOf(1.4f, 1.7f, 2.0f, 1.4f, 1.7f, 2.0f)

@Composable
fun DotBackdrop(
    levels: FloatArray,
    playing: Boolean,
    modifier: Modifier = Modifier,
    spacing: Dp = 11.dp,
    dotRadius: Dp = 1.6.dp
) {
    val spectrum = remember { FloatArray(28) }
    val phase = remember { mutableFloatStateOf(0f) }
    val tick = remember { mutableIntStateOf(0) }
    val levelsNow = rememberUpdatedState(levels)
    val playingNow = rememberUpdatedState(playing)

    val xs = remember { Array(GROUPS) { FloatArray(CAPACITY) } }
    val ys = remember { Array(GROUPS) { FloatArray(CAPACITY) } }
    val buf = remember { Array(GROUPS) { FloatArray(CAPACITY * 2) } }
    val counts = remember { IntArray(GROUPS) }
    val paint = remember {
        Paint().apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
        }
    }

    LaunchedEffect(Unit) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                val dt = if (last == 0L) {
                    0.016f
                } else {
                    ((now - last) / 1_000_000_000f).coerceIn(0.008f, 0.05f)
                }
                last = now

                val src = levelsNow.value
                if (src.isEmpty()) {
                    for (i in spectrum.indices) {
                        spectrum[i] += (0f - spectrum[i]) * 0.10f
                    }
                } else {
                    for (i in spectrum.indices) {
                        val idx = (i * src.size / spectrum.size).coerceIn(0, src.size - 1)
                        spectrum[i] += (src[idx] - spectrum[i]) * 0.22f
                    }
                }

                phase.value += dt * if (playingNow.value) 1.15f else 0.25f
                tick.intValue++
            }
        }
    }

    Canvas(modifier.fillMaxSize()) {
        tick.intValue
        val ph = phase.value
        val step = spacing.toPx()
        if (step < 1f) return@Canvas
        val dot = dotRadius.toPx()

        val cols = (size.width / step).toInt().coerceAtLeast(1)
        val rows = (size.height / step).toInt().coerceAtLeast(1)
        val offX = (size.width - (cols - 1) * step) / 2f
        val offY = (size.height - (rows - 1) * step) / 2f

        val live = playingNow.value
        val bands = spectrum.size
        java.util.Arrays.fill(counts, 0)

        for (c in 0 until cols) {
            val cf = c.toFloat() / cols
            val band = spectrum[(cf * bands).toInt().coerceIn(0, bands - 1)]
            val travel = sin(cf * 9f - ph * 2.1f) * 0.5f + 0.5f
            val white = c % 2 == 0
            val cx = offX + c * step

            for (r in 0 until rows) {
                val fromBottom = 1f - r.toFloat() / rows
                val v = if (!live || band < 0.02f) {
                    0f
                } else {
                    (band * (0.30f + 0.70f * fromBottom) * (0.45f + 0.55f * travel))
                        .coerceIn(0f, 1f)
                }

                val alpha = 0.085f + v * 0.82f
                val level = when {
                    alpha < 0.20f -> 0
                    alpha < 0.50f -> 1
                    else -> 2
                }
                val g = level + if (white) 0 else 3

                val n = counts[g]
                if (n >= CAPACITY) continue
                xs[g][n] = cx
                ys[g][n] = offY + r * step - v * step * 0.14f
                counts[g] = n + 1
            }
        }

        drawIntoCanvas { canvas ->
            val native = canvas.nativeCanvas
            for (g in 0 until GROUPS) {
                val n = counts[g]
                if (n == 0) continue
                paint.color = GROUP_COLORS[g]
                paint.strokeWidth = GROUP_WIDTH[g] * dot
                val arr = buf[g]
                val xa = xs[g]
                val ya = ys[g]
                for (i in 0 until n) {
                    arr[2 * i] = xa[i]
                    arr[2 * i + 1] = ya[i]
                }
                native.drawPoints(arr, 0, n * 2, paint)
            }
        }
    }
}

/** Чёрный экран + дышащая подложка. Всё содержимое рисуется поверх. */
@Composable
fun Backdrop(
    levels: FloatArray,
    playing: Boolean,
    content: @Composable () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        DotBackdrop(levels, playing, Modifier.fillMaxSize())
        content()
    }
}
