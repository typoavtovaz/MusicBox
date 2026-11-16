package com.margy.musicbox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.margy.musicbox.core.model.Demo
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.ui.components.IconButtonBare
import com.margy.musicbox.ui.components.NothingChip
import com.margy.musicbox.ui.components.SectionTitle
import com.margy.musicbox.ui.components.TileLarge
import com.margy.musicbox.ui.components.TrackRow
import com.margy.musicbox.ui.components.WaveCard
import com.margy.musicbox.ui.theme.NothingRed

@Composable
fun HomeScreen(
    onTrackClick: (Track) -> Unit,
    onOpenSettings: () -> Unit,
    wavePlaying: Boolean,
    waveLabel: String,
    onWaveToggle: () -> Unit,
    onWaveStop: () -> Unit,
    onOpenWaveEditor: () -> Unit
) {
    var filter by remember { mutableStateOf<Source?>(null) }
    val visible = Demo.tracks.filter { filter == null || it.source == filter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "MUSICBOX",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "ютуб · яндекс · вк",
                        style = MaterialTheme.typography.labelSmall,
                        color = NothingRed
                    )
                }
                IconButtonBare(
                    icon = Icons.Rounded.Settings,
                    onClick = onOpenSettings,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Spacer(Modifier.height(18.dp))
            WaveCard(
                playing = wavePlaying,
                moodLabel = waveLabel,
                onToggle = onWaveToggle,
                onStop = onWaveStop,
                onEdit = onOpenWaveEditor
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { NothingChip("ВСЕ", filter == null, onClick = { filter = null }) }
                items(Source.entries) { src ->
                    NothingChip(src.title, filter == src, onClick = { filter = src })
                }
            }
        }

        item { SectionTitle("Слушали недавно", "всё") }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(Demo.playlists) { pl ->
                    TileLarge(pl.title, "${pl.trackCount} треков", pl.id) {}
                }
            }
        }

        item { SectionTitle("Новые релизы", "всё") }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(Demo.playlists.reversed()) { pl ->
                    TileLarge(pl.title, "плейлист", pl.id + "x") {}
                }
            }
        }

        item { SectionTitle("Треки для тебя") }

        items(visible) { track ->
            TrackRow(track = track, onClick = { onTrackClick(track) })
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
