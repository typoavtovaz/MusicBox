package com.margy.musicbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.data.provider.ProviderRegistry
import com.margy.musicbox.ui.components.DotText
import com.margy.musicbox.ui.components.IconButtonBare
import com.margy.musicbox.ui.components.SectionTitle
import com.margy.musicbox.ui.components.TrackRow
import com.margy.musicbox.ui.theme.NothingLine
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingWhite

@Composable
fun LibraryScreen(
    onTrackClick: (Track) -> Unit,
    onResults: (List<Track>) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLogin: (Source) -> Unit,
    accountsVersion: Int
) {
    var tracks by remember { mutableStateOf<List<Track>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(accountsVersion) {
        if (!ProviderRegistry.isAnyLoggedIn()) {
            tracks = emptyList()
            return@LaunchedEffect
        }
        loading = true
        error = null
        try {
            val list = ProviderRegistry.feed(50)
            tracks = list
            onResults(list)
        } catch (e: Exception) {
            error = e.message
        }
        loading = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DotText(
                    text = "БИБЛИОТЕКА",
                    modifier = Modifier.weight(1f),
                    height = 30.dp,
                    color = NothingWhite
                )
                IconButtonBare(
                    icon = Icons.Rounded.Settings,
                    onClick = onOpenSettings,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { SectionTitle("Аккаунты") }

        items(listOf(Source.YANDEX, Source.VK)) { src ->
            val on = ProviderRegistry.isLoggedIn(src)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(NothingWhite.copy(alpha = 0.04f))
                    .border(1.dp, NothingLine, RoundedCornerShape(14.dp))
                    .clickable(enabled = !on) { onOpenLogin(src) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(src.title, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        if (on) "подключено" else "не подключено",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (on) NothingWhite else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    if (on) "готово" else "войти",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (on) MaterialTheme.colorScheme.onSurfaceVariant else NothingRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item { SectionTitle("Моя музыка") }

        if (loading) {
            item {
                Text(
                    "загружаю…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        error?.let { msg ->
            item {
                Text(
                    "не вышло: $msg",
                    style = MaterialTheme.typography.bodySmall,
                    color = NothingRed,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        if (!ProviderRegistry.isAnyLoggedIn()) {
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NothingWhite.copy(alpha = 0.04f))
                        .padding(16.dp)
                ) {
                    Text(
                        "Войди в Яндекс или ВК, и здесь появится твоя музыка.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(tracks, key = { it.key }) { track ->
            TrackRow(track = track, onClick = { onTrackClick(track) })
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}
