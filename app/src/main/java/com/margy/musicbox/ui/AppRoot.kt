package com.margy.musicbox.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.margy.musicbox.core.model.Demo
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.data.provider.ProviderRegistry
import com.margy.musicbox.playback.AudioReactive
import com.margy.musicbox.playback.PlayerEngine
import com.margy.musicbox.ui.components.MiniPlayer
import com.margy.musicbox.ui.screens.HomeScreen
import com.margy.musicbox.ui.screens.LibraryScreen
import com.margy.musicbox.ui.screens.PlayerScreen
import com.margy.musicbox.ui.screens.SearchScreen
import com.margy.musicbox.ui.screens.SettingsScreen
import com.margy.musicbox.ui.screens.WaveEditorScreen
import kotlinx.coroutines.launch

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("Главная", Icons.Rounded.Home),
    Search("Поиск", Icons.Rounded.Search),
    Library("Библиотека", Icons.Rounded.LibraryMusic)
}

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) { PlayerEngine.init(context) }

    val playback by PlayerEngine.state.collectAsState()
    val levels by AudioReactive.levels.collectAsState()

    var tab by remember { mutableStateOf(Tab.Home) }
    var queue by remember { mutableStateOf<List<Track>>(emptyList()) }
    var playerOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var waveEditorOpen by remember { mutableStateOf(false) }

    var wavePlaying by remember { mutableStateOf(false) }
    var waveStatus by remember { mutableStateOf<String?>(null) }
    val waveMoods = remember { mutableStateListOf<String>() }
    var waveEnergy by remember { mutableFloatStateOf(0.6f) }

    val waveLabel = if (waveMoods.isEmpty()) {
        "подстроена под тебя"
    } else {
        "подстроена: " + waveMoods.joinToString(", ")
    }

    fun playTrack(track: Track) {
        wavePlaying = false
        waveStatus = null
        scope.launch { PlayerEngine.play(track) }
    }

    fun startWave() {
        wavePlaying = true
        waveStatus = "сканирую сервисы…"
        scope.launch {
            val mood = waveMoods.toList().shuffled().take(2)
            val query = (mood.ifEmpty { listOf("популярная музыка") }).joinToString(" ")
            try {
                val found = ProviderRegistry.search(Source.YOUTUBE, query)
                if (found.isEmpty()) {
                    wavePlaying = false
                    waveStatus = "по «$query» нихуя не нашлось"
                } else {
                    queue = found
                    waveStatus = null
                    PlayerEngine.play(found.random())
                }
            } catch (e: Exception) {
                wavePlaying = false
                waveStatus = "волна сломалась: ${e.message ?: "ошибка сети"}"
            }
        }
    }

    fun toggleWave() {
        if (wavePlaying && playback.playing) {
            wavePlaying = false
            PlayerEngine.pause()
        } else if (wavePlaying && playback.track != null) {
            PlayerEngine.toggle()
        } else {
            startWave()
        }
    }

    fun stopWave() {
        wavePlaying = false
        PlayerEngine.pause()
    }

    fun next() {
        val list = queue.ifEmpty { Demo.tracks }
        val idx = list.indexOfFirst { it.key == playback.track?.key }
        if (idx >= 0) playTrack(list[(idx + 1) % list.size])
    }

    fun prev() {
        val list = queue.ifEmpty { Demo.tracks }
        val idx = list.indexOfFirst { it.key == playback.track?.key }
        if (idx >= 0) playTrack(list[if (idx - 1 < 0) list.size - 1 else idx - 1])
    }

    if (playerOpen && playback.track != null) {
        BackHandler { playerOpen = false }
        PlayerScreen(
            playback = playback,
            onClose = { playerOpen = false },
            onToggle = { PlayerEngine.toggle() },
            onSeek = { PlayerEngine.seekTo(it) },
            onNext = { next() },
            onPrev = { prev() }
        )
        return
    }

    if (waveEditorOpen) {
        BackHandler { waveEditorOpen = false }
        WaveEditorScreen(
            onBack = { waveEditorOpen = false },
            onStart = { startWave() },
            selectedMoods = waveMoods,
            onMoodsChange = { list ->
                waveMoods.clear()
                waveMoods.addAll(list)
            },
            energy = waveEnergy,
            onEnergyChange = { waveEnergy = it }
        )
        return
    }

    if (settingsOpen) {
        BackHandler { settingsOpen = false }
        SettingsScreen(onBack = { settingsOpen = false })
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Column {
                playback.track?.let { t ->
                    MiniPlayer(
                        track = t,
                        playing = playback.playing,
                        onOpen = { playerOpen = true },
                        onToggle = { PlayerEngine.toggle() },
                        onNext = { next() }
                    )
                }
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    Tab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    ) { inner ->
        Column(Modifier.fillMaxSize().padding(inner)) {
            when (tab) {
                Tab.Home -> HomeScreen(
                    onTrackClick = { playTrack(it) },
                    onOpenSettings = { settingsOpen = true },
                    wavePlaying = wavePlaying && playback.playing,
                    waveLevels = levels,
                    waveLabel = waveLabel,
                    waveStatus = waveStatus,
                    onWaveToggle = { toggleWave() },
                    onWaveStop = { stopWave() },
                    onOpenWaveEditor = { waveEditorOpen = true }
                )
                Tab.Search -> SearchScreen(
                    onTrackClick = { playTrack(it) },
                    onResults = { queue = it }
                )
                Tab.Library -> LibraryScreen(
                    onTrackClick = { playTrack(it) },
                    onOpenSettings = { settingsOpen = true }
                )
            }
        }
    }
}
