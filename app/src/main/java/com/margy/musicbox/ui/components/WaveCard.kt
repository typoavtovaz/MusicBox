package com.margy.musicbox.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.margy.musicbox.ui.theme.NothingLine
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingSurface
import com.margy.musicbox.ui.theme.NothingWhite

@Composable
fun WaveCard(
    playing: Boolean,
    onToggle: () -> Unit,
    onStop: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    moodLabel: String = "подстроена под тебя"
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(NothingSurface)
            .border(1.dp, NothingLine, RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (playing) NothingRed else NothingLine)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "МОЯ ВОЛНА",
                style = MaterialTheme.typography.labelMedium,
                color = NothingWhite
            )
            Spacer(Modifier.weight(1f))
            Text(
                if (playing) "В ЭФИРЕ" else "ПАУЗА",
                style = MaterialTheme.typography.labelSmall,
                color = if (playing) NothingRed else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = moodLabel,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(14.dp))

        DotMatrixWave(
            playing = playing,
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
            energy = if (playing) 0.85f else 0.4f
        )

        Spacer(Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (playing) NothingWhite else NothingRed)
                    .clickable { onToggle() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = "волна",
                    tint = if (playing) Color.Black else NothingWhite,
                    modifier = Modifier.size(30.dp)
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, NothingLine, RoundedCornerShape(14.dp))
                    .clickable { onEdit() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "РЕДАКТОР ВОЛНЫ",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = NothingWhite
                )
            }

            if (playing) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, NothingLine, RoundedCornerShape(14.dp))
                        .clickable { onStop() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Stop,
                        contentDescription = "стоп",
                        tint = NothingRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun NothingChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) NothingRed else Color.Transparent)
            .border(1.dp, if (selected) NothingRed else NothingLine, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 13.dp, vertical = 8.dp)
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontSize = 11.sp,
            color = if (selected) NothingWhite else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
