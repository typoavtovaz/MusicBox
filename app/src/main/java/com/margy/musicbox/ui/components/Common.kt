package com.margy.musicbox.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingWhite
import kotlin.math.abs
import kotlin.math.min

/**
 * Обложка из точек: никаких градиентов, только чёрный фон и красно-белая мозаика,
 * детерминированная от ключа трека. Один и тот же трек — один и тот же рисунок.
 */
private fun mosaic(seed: String, cells: Int): BooleanArray {
    val rnd = java.util.Random(seed.hashCode().toLong() * 31 + cells)
    val out = BooleanArray(cells * cells)
    // зеркалим по вертикали — узор выглядит осмысленно, а не как шум
    val half = (cells + 1) / 2
    for (r in 0 until cells) {
        for (c in 0 until half) {
            val on = rnd.nextFloat() < 0.42f
            out[r * cells + c] = on
            out[r * cells + (cells - 1 - c)] = on
        }
    }
    return out
}

@Composable
fun Artwork(
    seed: String,
    modifier: Modifier = Modifier,
    corner: Dp = 12.dp,
    cells: Int = 7
) {
    val pattern = remember(seed, cells) { mosaic(seed, cells) }
    val tint = remember(seed) { if (abs(seed.hashCode()) % 2 == 0) NothingRed else NothingWhite }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(corner))
            .background(NothingWhite.copy(alpha = 0.03f))
    ) {
        Canvas(Modifier.fillMaxSize().padding(5.dp)) {
            val step = min(size.width, size.height) / cells
            val r = step * 0.24f
            val offX = (size.width - step * cells) / 2f
            val offY = (size.height - step * cells) / 2f
            for (row in 0 until cells) {
                for (col in 0 until cells) {
                    val on = pattern[row * cells + col]
                    val color = when {
                        !on -> NothingWhite.copy(alpha = 0.06f)
                        (row + col) % 3 == 0 -> NothingWhite.copy(alpha = 0.85f)
                        else -> tint.copy(alpha = 0.9f)
                    }
                    drawCircle(
                        color = color,
                        radius = if (on) r else r * 0.5f,
                        center = Offset(
                            offX + step * (col + 0.5f),
                            offY + step * (row + 0.5f)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.weight(1f)
        )
        if (action != null) {
            Text(
                text = action,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onAction() }
            )
        }
    }
}

@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showSource: Boolean = true
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Artwork(seed = track.key, modifier = Modifier.size(52.dp), corner = 12.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = if (showSource) "${track.artist} · ${sourceShort(track.source)}" else track.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            text = com.margy.musicbox.core.model.Demo.formatted(track.durationMs),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

fun sourceShort(source: Source): String = when (source) {
    Source.YOUTUBE -> "YouTube"
    Source.YANDEX -> "Яндекс"
    Source.VK -> "ВК"
}

@Composable
fun MiniPlayer(
    track: Track,
    playing: Boolean,
    onOpen: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpen() }
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Artwork(seed = track.key, modifier = Modifier.size(44.dp), corner = 10.dp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButtonBare(
                icon = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                onClick = onToggle
            )
            IconButtonBare(icon = Icons.Rounded.SkipNext, onClick = onNext)
        }
    }
}

@Composable
fun IconButtonBare(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    size: Dp = 40.dp,
    iconSize: Dp = 24.dp,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(50))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(iconSize))
    }
}

@Composable
fun ChipSmall(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun TileLarge(title: String, subtitle: String, seed: String, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(148.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp)
    ) {
        Artwork(seed = seed, modifier = Modifier.size(128.dp), corner = 14.dp, cells = 9)
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
