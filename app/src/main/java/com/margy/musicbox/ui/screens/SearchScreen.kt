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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.data.provider.ProviderRegistry
import com.margy.musicbox.ui.components.DotText
import com.margy.musicbox.ui.components.NothingChip
import com.margy.musicbox.ui.components.TrackRow
import com.margy.musicbox.ui.theme.NothingLine
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingWhite
import kotlinx.coroutines.launch

private val QuickQueries = listOf("Husky", "Zivert", "лоу-фай", "рок 90х", "phonk")

@Composable
fun SearchScreen(
    onTrackClick: (Track) -> Unit,
    onResults: (List<Track>) -> Unit,
    onOpenLogin: (Source) -> Unit,
    accountsVersion: Int
) {
    var query by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<Track>>(emptyList()) }
    var bySource by remember { mutableStateOf<Map<Source, Int>>(emptyMap()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val keyboard = LocalSoftwareKeyboardController.current
    val loggedIn = ProviderRegistry.isAnyLoggedIn()

    fun runSearch(text: String) {
        val q = text.trim()
        if (q.isEmpty()) return
        keyboard?.hide()
        loading = true
        error = null
        scope.launch {
            try {
                val found = ProviderRegistry.searchAll(q)
                val flat = found.values.flatten()
                results = flat
                bySource = found.mapValues { it.value.size }
                onResults(flat)
                if (flat.isEmpty()) error = "по «$q» ничего"
            } catch (e: Exception) {
                error = e.message ?: "поиск сдох"
            } finally {
                loading = false
            }
        }
    }

    Column(Modifier.fillMaxSize()) {
        Box(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            DotText(
                text = "ПОИСК",
                modifier = Modifier.fillMaxWidth(),
                height = 34.dp,
                color = NothingWhite
            )
        }

        Spacer(Modifier.height(10.dp))

        TextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, NothingLine, RoundedCornerShape(16.dp)),
            placeholder = { Text("трек, артист, альбом") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { runSearch(query) }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = NothingRed
            )
        )

        Spacer(Modifier.height(12.dp))

        if (!loggedIn) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(NothingWhite.copy(alpha = 0.04f))
                    .border(1.dp, NothingLine, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text("СНАЧАЛА ВОЙДИ", style = MaterialTheme.typography.labelMedium, color = NothingWhite)
                Spacer(Modifier.height(6.dp))
                Text(
                    "Поиск идёт по Яндексу и ВК, для них нужен вход.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NothingRed)
                            .clickable { onOpenLogin(Source.YANDEX) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) { Text("ЯНДЕКС", color = NothingWhite, style = MaterialTheme.typography.labelMedium) }
                    Box(
                        Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(NothingWhite)
                            .clickable { onOpenLogin(Source.VK) }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) { Text("ВК", color = Color.Black, style = MaterialTheme.typography.labelMedium) }
                }
            }
            return@Column
        }

        if (results.isEmpty() && !loading) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(QuickQueries) { q ->
                    NothingChip(q, false, onClick = { query = q; runSearch(q) })
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        when {
            loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NothingRed)
            }

            error != null && results.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(error ?: "", color = NothingRed)
            }

            results.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "напиши что-нибудь, и я найду",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                item {
                    val parts = bySource.entries.joinToString(" · ") { "${it.key.title}: ${it.value}" }
                    Text(
                        "${results.size} результатов · $parts",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                    )
                }
                items(results, key = { it.key }) { track ->
                    TrackRow(track = track, onClick = { onTrackClick(track) })
                }
            }
        }
    }
}
