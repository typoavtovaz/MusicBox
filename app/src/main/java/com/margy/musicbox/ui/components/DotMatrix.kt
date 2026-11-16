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
import androidx.compose.ui.unit.dp
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingWhite
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Волна из точек в стиле Nothing: столбики из красно-белых пиксельных точек.
 * Играет — бежит, пауза — замирает в низкой полке.
 */
@Composable
fun DotMatrixWave(
    playing: Boolean,
    modifier: Modifier = Modifier,
    energy: Float = 0.65f,
    columns: Int = 28,
    rows: Int = 12,
    dimWhenStopped: Boolean = true
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

    val p = if (playing) phase else 0f
    val energyFactor = energy.coerceIn(0.15f, 1f)

    Canvas(
        modifier = modifier.fillMaxWidth()
    ) {
        val stepX = size.width / columns
        val stepY = size.height / rows
        val dot = minOf(stepX, stepY) * 0.30f

        for (col in 0 until columns) {
            // Две синусоиды + косинус = живая, но всё же ритмичная волна
            val wobble = abs(sin(col * 0.42f + p * 0.9f)) * 0.65f +
                abs(cos(col * 0.23f - p * 0.55f)) * 0.35f

            val idle = 0.16f
            val height = if (playing) {
                (idle + wobble * energyFactor * 0.84f).coerceIn(0.05f, 1f)
            } else {
                idle
            }

            val filled = (rows * height).toInt().coerceIn(1, rows)
            val cx = stepX * (col + 0.5f)

            for (row in 0 until rows) {
                val fromBottom = row < filled
                val y = size.height - stepY * (row + 0.5f)

                val color: Color = when {
                    fromBottom && (col + row) % 3 == 0 -> NothingRed
                    fromBottom -> NothingWhite
                    !dimWhenStopped && !playing -> NothingWhite.copy(alpha = 0.06f)
                    else -> NothingWhite.copy(alpha = 0.10f)
                }

                // верхние точки чуть меньше — сглаживает «пилу»
                val r = if (fromBottom) dot else dot * 0.7f
                drawCircle(color = color, radius = r, center = Offset(cx, y))
            }
        }
    }
}

/** Тонкая строка из точек — разделитель в стиле Nothing. */
@Composable
fun DotDivider(modifier: Modifier = Modifier, color: Color = NothingWhite) {
    Canvas(modifier = modifier.fillMaxWidth().then(Modifier)) {
        val step = 8.dp.toPx()
        var x = 0f
        while (x < size.width) {
            drawCircle(color.copy(alpha = 0.25f), radius = 1f, center = Offset(x, size.height / 2f))
            x += step
        }
    }
}
