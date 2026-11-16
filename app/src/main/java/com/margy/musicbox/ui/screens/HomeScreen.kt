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
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.data.provider.ProviderRegistry
import com.margy.musicbox.ui.components.DotText
import com.margy.musicbox.ui.components.IconButtonBare
import com.margy.musicbox.ui.components.NothingChip
import com.margy.musicbox.ui.components.SectionTitle
import com.margy.musicbox.ui.components.TrackRow
import com.margy.musicbox.ui.components.WaveCard
import com.margy.musicbox.ui.theme.NothingLine
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingWhite

@Composable
fun HomeScreen(
    onTrackClick: (Track) -> Unit,
    onResults: (List<Track>) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLogin: (Source) -> Unit,
    accountsVersion: Int,
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
    val loggedIn = ProviderRegistry.isAnyLoggedIn()

    LaunchedEffect(accountsVersion, loggedIn) {
        if (!loggedIn) {
            loading = false
            tracks = emptyList()
            return@LaunchedEffect
        }
        loading = true
        error = null
        try {
            val list = ProviderRegistry.feed(40)
            tracks = list
            onResults(list)
            if (list.isEmpty()) error = "сервис ответил пусто"
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
                        "яндекс · вк",
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

        if (!loggedIn) {
            item {
                Spacer(Modifier.height(16.dp))
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(NothingWhite.copy(alpha = 0.04f))
                        .border(1.dp, NothingLine, RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Text(
                        "ПОДКЛЮЧИ СЕРВИС",
                        style = MaterialTheme.typography.labelMedium,
                        color = NothingWhite
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Без входа играть нечего. Яндекс — полные треки, ВК — своя музыка. Вход один раз.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LoginButton("ЯНДЕКС", NothingRed) { onOpenLogin(Source.YANDEX) }
                        LoginButton("ВК", NothingWhite, textColor = Color.Black) {
                            onOpenLogin(Source.VK)
                        }
                    }
                }
            }
        } else {
            item {
                Spacer(Modifier.height(14.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { NothingChip("ВСЕ", filter == null, onClick = { filter = null }) }
                    item {
                        NothingChip(
                            "ЯНДЕКС",
                            filter == Source.YANDEX,
                            onClick = { filter = Source.YANDEX }
                        )
                    }
                    item {
                        NothingChip("ВК", filter == Source.VK, onClick = { filter = Source.VK })
                    }
                }
            }

            item { SectionTitle(if (filter == Source.VK) "Моя музыка" else "Чарт") }

            if (loading) {
                item {
                    Text(
                        "тяну треки…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
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

            items(visible, key = { it.key }) { track ->
                TrackRow(track = track, onClick = { onTrackClick(track) })
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    "YouTube появится, когда поднимем прокси — без него Google с этой сети недоступен.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { Spacer(Modifier.height(24.dp)) }
    }
}

@Composable
private fun LoginButton(
    text: String,
    color: Color,
    textColor: Color = NothingWhite,
    onClick: () -> Unit
) {
    Box(
        Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 11.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
