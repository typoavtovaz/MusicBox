package com.margy.musicbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.margy.musicbox.core.model.Demo
import com.margy.musicbox.playback.PlaybackUi
import com.margy.musicbox.ui.components.Artwork
import com.margy.musicbox.ui.components.DotText
import com.margy.musicbox.ui.components.IconButtonBare
import com.margy.musicbox.ui.components.sourceShort

@Composable
fun PlayerScreen(
    playback: PlaybackUi,
    onClose: () -> Unit,
    onToggle: () -> Unit,
    onSeek: (Long) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val track = playback.track ?: return
    val duration = playback.durationMs.coerceAtLeast(1L)

    var dragging by remember { mutableStateOf(false) }
    var dragValue by remember { mutableFloatStateOf(0f) }
    val fraction = if (dragging) dragValue else (playback.positionMs.toFloat() / duration).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 8.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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

            Spacer(Modifier.height(24.dp))

            Artwork(
                seed = track.key,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                corner = 28.dp,
                cells = 13
            )

            Spacer(Modifier.height(26.dp))

            DotText(
                text = track.title.uppercase(),
                modifier = Modifier.fillMaxWidth(),
                height = 26.dp,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (playback.error != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = playback.error,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE8454C),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            Slider(
                value = fraction,
                onValueChange = {
                    dragging = true
                    dragValue = it
                },
                onValueChangeFinished = {
                    onSeek((dragValue * duration).toLong())
                    dragging = false
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color(0xFFD71921),
                    inactiveTrackColor = Color.White.copy(alpha = 0.20f)
                )
            )
            Row(Modifier.fillMaxWidth()) {
                Text(
                    Demo.formatted((duration * fraction).toLong()),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(Modifier.weight(1f))
                Text(
                    Demo.formatted(duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(Modifier.height(18.dp))

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
                        .background(Color(0xFFD71921)),
                    contentAlignment = Alignment.Center
                ) {
                    if (playback.loading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(30.dp)
                        )
                    } else {
                        IconButtonBare(
                            icon = if (playback.playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                            onClick = onToggle,
                            size = 76.dp,
                            iconSize = 40.dp,
                            tint = Color.White
                        )
                    }
                }

                IconButtonBare(Icons.Rounded.SkipNext, onNext, iconSize = 36.dp, tint = Color.White)
                IconButtonBare(Icons.Rounded.Repeat, {}, tint = Color.White.copy(alpha = 0.8f))
            }

            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButtonBare(Icons.Rounded.FavoriteBorder, {}, tint = Color.White.copy(alpha = 0.8f))
                IconButtonBare(Icons.Rounded.Mic, {}, tint = Color.White.copy(alpha = 0.8f))
                IconButtonBare(Icons.Rounded.QueueMusic, {}, tint = Color.White.copy(alpha = 0.8f))
                IconButtonBare(Icons.Rounded.Share, {}, tint = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}
