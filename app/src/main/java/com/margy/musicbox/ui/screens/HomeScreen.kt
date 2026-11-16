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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.margy.musicbox.core.model.Demo
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.data.provider.ProviderRegistry
import com.margy.musicbox.ui.components.DotText
import com.margy.musicbox.ui.components.IconButtonBare
import com.margy.musicbox.ui.components.NothingChip
import com.margy.musicbox.ui.components.SectionTitle
import com.margy.musicbox.ui.components.TileLarge
import com.margy.musicbox.ui.components.TrackRow
import com.margy.musicbox.ui.components.WaveCard
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingWhite

@Composable
fun HomeScreen(
    onTrackClick: (Track) -> Unit,
    onOpenSettings: () -> Unit,
    wavePlaying: Boolean,
    waveLevels: FloatArray,
    waveLabel: String,
    waveStatus: String?,
    onWaveToggle: () -> Unit,
    onWaveStop: () -> Unit,
    onOpenWaveEditor: () -> Unit
) {
    var filter by remember { mutableStateOf<Source?>(null) }
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            tracks = ProviderRegistry.search(Source.YOUTUBE, "популярная музыка 2026")
        } catch (e: Exception) {
            error = e.message ?: "сеть легла"
        }
        loading = false
    }

    val visible = tracks.filter { filter == null || it.source == filter }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    DotText(
                        text = "MUSICBOX",
                        modifier = Modifier.fillMaxWidth(),
                        height = 34.dp,
                        color = NothingWhite
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
            Spacer(Modifier.height(16.dp))
            WaveCard(
                playing = wavePlaying,
                levels = waveLevels,
                moodLabel = waveLabel,
                statusText = waveStatus,
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

        item { SectionTitle("Плейлисты", "всё") }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(Demo.playlists) { pl ->
                    TileLarge(pl.title, "${pl.trackCount} треков", pl.id) {}
                }
            }
        }

        item { SectionTitle("Треки для тебя", "обновить") }

        if (loading) {
            item {
                Text(
                    "тяну треки из YouTube…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            }
        }

        if (error != null) {
            item {
                Text(
                    "не вышло: $error",
                    style = MaterialTheme.typography.bodySmall,
                    color = NothingRed,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        items(visible) { track ->
            TrackRow(track = track, onClick = { onTrackClick(track) })
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
