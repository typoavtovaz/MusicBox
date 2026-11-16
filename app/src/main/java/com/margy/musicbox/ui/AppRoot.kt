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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.margy.musicbox.core.model.Demo
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.ui.components.MiniPlayer
import com.margy.musicbox.ui.screens.HomeScreen
import com.margy.musicbox.ui.screens.LibraryScreen
import com.margy.musicbox.ui.screens.PlayerScreen
import com.margy.musicbox.ui.screens.SearchScreen
import com.margy.musicbox.ui.screens.SettingsScreen
import com.margy.musicbox.ui.screens.WaveEditorScreen
import kotlin.random.Random

private enum class Tab(val label: String, val icon: ImageVector) {
    Home("Главная", Icons.Rounded.Home),
    Search("Поиск", Icons.Rounded.Search),
    Library("Библиотека", Icons.Rounded.LibraryMusic)
}

@Composable
fun AppRoot() {
    var tab by remember { mutableStateOf(Tab.Home) }
    var queue by remember { mutableStateOf(Demo.tracks) }
    var current by remember { mutableStateOf<Track?>(null) }
    var playing by remember { mutableStateOf(false) }
    var playerOpen by remember { mutableStateOf(false) }
    var settingsOpen by remember { mutableStateOf(false) }
    var waveEditorOpen by remember { mutableStateOf(false) }

    var wavePlaying by remember { mutableStateOf(false) }
    val waveMoods = remember { mutableStateListOf<String>() }
    var waveEnergy by remember { mutableFloatStateOf(0.6f) }

    val waveLabel = if (waveMoods.isEmpty()) {
        "подстроена под тебя"
    } else {
        "подстроена: " + waveMoods.joinToString(", ")
    }

    fun play(track: Track) {
        current = track
        playing = true
    }

    fun next() {
        val list = queue
        val idx = list.indexOfFirst { it.key == current?.key }
        if (idx >= 0 && list.isNotEmpty()) {
            current = list[(idx + 1) % list.size]
            playing = true
        }
    }

    fun prev() {
        val list = queue
        val idx = list.indexOfFirst { it.key == current?.key }
        if (idx >= 0 && list.isNotEmpty()) {
            current = list[if (idx - 1 < 0) list.size - 1 else idx - 1]
            playing = true
        }
    }

    fun startWave() {
        wavePlaying = true
        play(Demo.tracks[Random.nextInt(Demo.tracks.size)])
    }

    if (playerOpen && current != null) {
        BackHandler { playerOpen = false }
        PlayerScreen(
            track = current!!,
            playing = playing,
            onClose = { playerOpen = false },
            onToggle = { playing = !playing },
            onNext = { next() },
            onPrev = { prev() },
            onLike = {}
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
                current?.let { t ->
                    MiniPlayer(
                        track = t,
                        playing = playing,
                        onOpen = { playerOpen = true },
                        onToggle = { playing = !playing },
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
                    onTrackClick = { play(it) },
                    onOpenSettings = { settingsOpen = true },
                    wavePlaying = wavePlaying,
                    waveLabel = waveLabel,
                    onWaveToggle = {
                        if (wavePlaying) {
                            wavePlaying = false
                            playing = false
                        } else {
                            startWave()
                        }
                    },
                    onWaveStop = {
                        wavePlaying = false
                        playing = false
                    },
                    onOpenWaveEditor = { waveEditorOpen = true }
                )
                Tab.Search -> SearchScreen(
                    onTrackClick = { play(it); queue = Demo.tracks },
                    onOpenSettings = { settingsOpen = true }
                )
                Tab.Library -> LibraryScreen(
                    onTrackClick = { play(it) },
                    onOpenSettings = { settingsOpen = true }
                )
            }
        }
    }
}
