package com.margy.musicbox.ui.components

import android.graphics.Bitmap
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.margy.musicbox.ui.theme.NothingRed
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

/**
 * Дот-матричный текст. Рисуем строку в битмап системным шрифтом и пересобираем
 * её точками — поэтому работает и с кириллицей, и с любым другим языком.
 */
data class DotPattern(
    val cols: Int,
    val rows: Int,
    val dots: BooleanArray,
    val aspect: Float
)

private fun rasterize(text: String, bold: Boolean = true): DotPattern {
    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 96f
        typeface = Typeface.create(
            if (bold) "sans-serif" else "sans-serif-light",
            if (bold) Typeface.BOLD else Typeface.NORMAL
        )
        letterSpacing = 0.06f
    }

    val width = ceil(paint.measureText(text)).toInt().coerceAtLeast(2)
    val fm = paint.fontMetrics
    val height = ceil(fm.descent - fm.ascent).toInt().coerceAtLeast(2)

    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    android.graphics.Canvas(bmp).drawText(text, 1f, -fm.ascent, paint)

    // шаг сетки: чем крупнее буква, тем ровнее точки
    val cell = max(4, (height / 14f).toInt())
    val cols = max(1, width / cell)
    val rows = max(1, height / cell)
    val dots = BooleanArray(cols * rows)

    val pixels = IntArray(width * height)
    bmp.getPixels(pixels, 0, width, 0, 0, width, height)

    for (r in 0 until rows) {
        for (c in 0 until cols) {
            var sum = 0
            var count = 0
            val y0 = r * cell
            val x0 = c * cell
            for (y in y0 until min(height, y0 + cell)) {
                for (x in x0 until min(width, x0 + cell)) {
                    sum += pixels[y * width + x] ushr 24
                    count++
                }
            }
            dots[r * cols + c] = count > 0 && (sum.toFloat() / count) > 100f
        }
    }

    bmp.recycle()
    return DotPattern(cols, rows, dots, width.toFloat() / height)
}

@Composable
fun DotText(
    text: String,
    modifier: Modifier = Modifier,
    height: Dp = 34.dp,
    color: Color = NothingRed,
    bold: Boolean = true
) {
    val pattern = remember(text, bold) { rasterize(text, bold) }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val rows = pattern.rows
        val cols = pattern.cols
        val cell = min(size.height / rows, size.width / (cols * 0.62f))
        val dotR = cell * 0.30f
        val totalW = cols * cell * 0.62f
        val startX = (size.width - totalW) / 2f + cell * 0.31f

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                if (!pattern.dots[r * cols + c]) continue
                drawCircle(
                    color = color,
                    radius = dotR,
                    center = Offset(
                        startX + c * cell * 0.62f,
                        size.height * 0.12f + r * cell
                    )
                )
            }
        }
    }
}
