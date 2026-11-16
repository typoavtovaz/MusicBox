package com.margy.musicbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.margy.musicbox.core.model.Demo
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.ui.components.Artwork
import com.margy.musicbox.ui.components.IconButtonBare
import com.margy.musicbox.ui.components.SectionTitle
import com.margy.musicbox.ui.components.TrackRow

@Composable
fun LibraryScreen(
    onTrackClick: (Track) -> Unit,
    onOpenSettings: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Библиотека",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.weight(1f)
                )
                IconButtonBare(
                    icon = Icons.Rounded.Settings,
                    onClick = onOpenSettings,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { QuickTile("Лайки", Icons.Rounded.Favorite, Color(0xFF3DDC97)) {} }
                item { QuickTile("Скачанные", Icons.Rounded.Download, Color(0xFF32ADE6)) {} }
                item { QuickTile("История", Icons.Rounded.History, Color(0xFFFFB44C)) {} }
            }
        }

        item { SectionTitle("Плейлисты") }

        items(Demo.playlists) { pl ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { }
                    .padding(vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Artwork(seed = pl.id, modifier = Modifier.size(54.dp), corner = 12.dp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(pl.title, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        "${pl.trackCount} треков",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { SectionTitle("Аккаунты") }

        items(Source.entries) { src ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(src.title, color = MaterialTheme.colorScheme.onSurface)
                        Text(
                            "не подключено",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        "войти",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            SectionTitle("Недавно играло")
        }

        items(Demo.tracks.take(4)) { track ->
            TrackRow(track = track, onClick = { onTrackClick(track) })
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun QuickTile(title: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(108.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
