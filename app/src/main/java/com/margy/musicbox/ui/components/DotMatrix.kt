package com.margy.musicbox.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.margy.musicbox.ui.theme.NothingRed
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Волна из точек. Если пришёл реальный спектр (levels) — столбики рисуются по нему:
 * слева бас, справа верха, всё честно дёргается под трек.
 * Если спектра нет (пауза/нет разрешения) — тихая синтетическая полка.
 */
@Composable
fun DotMatrixWave(
    levels: FloatArray,
    playing: Boolean,
    modifier: Modifier = Modifier,
    columns: Int = 28,
    rows: Int = 12
) {
    val transition = rememberInfiniteTransition(label = "wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2f * Math.PI.toFloat() * 40f),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val live = levels.isNotEmpty()
    val cols = if (live) levels.size else columns

    Canvas(modifier = modifier.fillMaxWidth()) {
        val stepX = size.width / cols
        val stepY = size.height / rows
        val dot = minOf(stepX, stepY) * 0.30f

        for (col in 0 until cols) {
            val value = if (live) {
                levels[col].coerceIn(0f, 1f)
            } else {
                val wobble = abs(sin(col * 0.42f + phase * 0.9f)) * 0.65f +
                    abs(cos(col * 0.23f - phase * 0.55f)) * 0.35f
                if (playing) (0.12f + wobble * 0.55f) else 0.06f
            }

            val filled = (rows * value).toInt().coerceIn(1, rows)
            val cx = stepX * (col + 0.5f)

            for (row in 0 until rows) {
                val active = row < filled
                val y = size.height - stepY * (row + 0.5f)

                val color = when {
                    !active -> NothingRed.copy(alpha = 0.07f)
                    // макушка столбика — самая яркая, низ мягче
                    row >= filled - 2 -> NothingRed.copy(alpha = 1f)
                    else -> NothingRed.copy(alpha = 0.55f)
                }

                drawCircle(
                    color = color,
                    radius = if (active) dot else dot * 0.68f,
                    center = Offset(cx, y)
                )
            }
        }
    }
}

@Composable
fun DotDivider(modifier: Modifier = Modifier, color: Color = NothingRed) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val step = 8f * density
        var x = 0f
        while (x < size.width) {
            drawCircle(color.copy(alpha = 0.18f), radius = 1f * density, center = Offset(x, size.height / 2f))
            x += step
        }
    }
}
