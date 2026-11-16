package com.margy.musicbox.core.model

import com.margy.musicbox.core.provider.Playlist

object Demo {

    val tracks: List<Track> = listOf(
        Track("1", Source.YANDEX, "Ночной эфир", "Молчат Дома", durationMs = 214_000),
        Track("2", Source.YOUTUBE, "Sweater Weather", "The Neighbourhood", durationMs = 240_000),
        Track("3", Source.VK, "Лондон", "Три дня дождя", durationMs = 198_000),
        Track("4", Source.YANDEX, "Плачу на техно", "Кровосток", durationMs = 205_000),
        Track("5", Source.YOUTUBE, "Midnight City", "M83", durationMs = 244_000),
        Track("6", Source.VK, "Не с тобой", "Zivert", durationMs = 187_000),
        Track("7", Source.YANDEX, "Всё идёт по плану", "Гражданская Оборона", durationMs = 233_000),
        Track("8", Source.YOUTUBE, "Instant Crush", "Daft Punk", durationMs = 337_000)
    )

    val playlists: List<Playlist> = listOf(
        Playlist("p1", "Дорога домой", trackCount = 42),
        Playlist("p2", "Ночной драйв", trackCount = 28),
        Playlist("p3", "Русский рок", trackCount = 76),
        Playlist("p4", "Workout", trackCount = 31)
    )

    fun formatted(ms: Long): String {
        val total = ms / 1000
        val m = total / 60
        val s = total % 60
        return "$m:${s.toString().padStart(2, '0')}"
    }
}
