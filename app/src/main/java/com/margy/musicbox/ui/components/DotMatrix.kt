package com.margy.musicbox.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingWhite
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Волна из точек — красно-белая, без градиентов.
 * Если пришёл реальный спектр (levels) — столбики рисуются по нему:
 * слева бас, справа верха. Нет спектра — тихая синтетическая полка.
 */
@Composable
fun DotMatrixWave(
    levels: FloatArray,
    playing: Boolean,
    modifier: Modifier = Modifier,
    columns: Int = 28,
    rows: Int = 12
) {
    // фаза для «дыхания», когда спектра нет
    val t = System.currentTimeMillis() / 1000f

    val live = levels.isNotEmpty()
    val cols = if (live) levels.size else columns

    Canvas(modifier = modifier.fillMaxWidth()) {
        val stepX = size.width / cols
        val stepY = size.height / rows
        val dot = minOf(stepX, stepY) * 0.33f

        for (col in 0 until cols) {
            val value = if (live) {
                levels[col].coerceIn(0f, 1f)
            } else {
                val wobble = abs(sin(col * 0.42f + t * 2.1f)) * 0.6f +
                    abs(cos(col * 0.23f - t * 1.3f)) * 0.4f
                if (playing) (0.10f + wobble * 0.5f) else 0.05f
            }

            val filled = (rows * value).toInt().coerceIn(1, rows)
            val cx = stepX * (col + 0.5f)

            // чередование по столбцам: чётный — белый, нечётный — красный
            val base = if (col % 2 == 0) NothingWhite else NothingRed

            for (row in 0 until rows) {
                val active = row < filled
                val y = size.height - stepY * (row + 0.5f)

                val color = when {
                    !active -> if (col % 2 == 0) NothingWhite.copy(alpha = 0.10f) else NothingRed.copy(alpha = 0.10f)
                    row >= filled - 1 -> base.copy(alpha = 1f)
                    row >= filled - 3 -> base.copy(alpha = 0.85f)
                    else -> base.copy(alpha = 0.55f)
                }

                drawCircle(
                    color = color,
                    radius = if (active) dot else dot * 0.55f,
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
            drawCircle(
                color.copy(alpha = 0.18f),
                radius = 1f * density,
                center = Offset(x, size.height / 2f)
            )
            x += step
        }
    }
}
