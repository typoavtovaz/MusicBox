package com.margy.musicbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.margy.musicbox.core.model.Demo
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.ui.components.Artwork
import com.margy.musicbox.ui.components.IconButtonBare
import com.margy.musicbox.ui.components.gradientFor
import com.margy.musicbox.ui.components.sourceShort

@Composable
fun PlayerScreen(
    track: Track,
    playing: Boolean,
    onClose: () -> Unit,
    onToggle: () -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onLike: () -> Unit
) {
    var position by remember(track.key) { mutableFloatStateOf(0.3f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientFor(track.key))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.9f),
                            Color(0xFF0B0D11)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 44.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Шапка
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButtonBare(
                    icon = Icons.Rounded.KeyboardArrowDown,
                    onClick = onClose,
                    tint = Color.White
                )
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "ИГРАЕТ СЕЙЧАС",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        "из ${sourceShort(track.source)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                IconButtonBare(
                    icon = Icons.Rounded.MoreVert,
                    onClick = {},
                    tint = Color.White
                )
            }

            Spacer(Modifier.height(28.dp))

            Artwork(
                seed = track.key,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                corner = 28.dp,
                iconSize = 64.dp
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text = track.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(24.dp))

            Slider(
                value = position,
                onValueChange = { position = it },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                )
            )
            Row(Modifier.fillMaxWidth()) {
                Text(
                    Demo.formatted((track.durationMs * position).toLong()),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    Demo.formatted(track.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButtonBare(Icons.Rounded.Shuffle, {}, tint = Color.White.copy(alpha = 0.8f))
                IconButtonBare(Icons.Rounded.SkipPrevious, onPrev, iconSize = 36.dp, tint = Color.White)
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    IconButtonBare(
                        icon = if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                        onClick = onToggle,
                        size = 76.dp,
                        iconSize = 40.dp,
                        tint = Color(0xFF0B0D11)
                    )
                }
                IconButtonBare(Icons.Rounded.SkipNext, onNext, iconSize = 36.dp, tint = Color.White)
                IconButtonBare(Icons.Rounded.Repeat, {}, tint = Color.White.copy(alpha = 0.8f))
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButtonBare(Icons.Rounded.FavoriteBorder, onLike, tint = Color.White.copy(alpha = 0.8f))
                IconButtonBare(Icons.Rounded.Mic, {}, tint = Color.White.copy(alpha = 0.8f))
                IconButtonBare(Icons.Rounded.QueueMusic, {}, tint = Color.White.copy(alpha = 0.8f))
                IconButtonBare(Icons.Rounded.Share, {}, tint = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
