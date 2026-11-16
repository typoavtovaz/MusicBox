package com.margy.musicbox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.margy.musicbox.core.model.Demo
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.ui.components.ChipSmall
import com.margy.musicbox.ui.components.TrackRow

@Composable
fun SearchScreen(
    onTrackClick: (Track) -> Unit,
    onOpenSettings: () -> Unit
) {
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf<Source?>(null) }

    val results = Demo.tracks.filter { t ->
        val bySource = filter == null || t.source == filter
        val byText = query.isBlank() ||
            t.title.contains(query, true) || t.artist.contains(query, true)
        bySource && byText
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Поиск",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 12.dp)
        )

        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp)),
            placeholder = { Text("Трек, артист, альбом") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(12.dp))

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { ChipSmall("Все", filter == null) { filter = null } }
            items(Source.entries) { src ->
                ChipSmall(src.title, filter == src) { filter = src }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (results.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text(
                    "нихуя не нашлось",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(results) { track ->
                    TrackRow(track = track, onClick = { onTrackClick(track) })
                }
            }
        }
    }
}
