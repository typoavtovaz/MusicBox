package com.margy.musicbox.core.provider

import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track

data class Playlist(
    val id: String,
    val title: String,
    val artworkUrl: String? = null,
    val trackCount: Int = 0
)

/**
 * Один контракт — три реализации. Никакой логики UI здесь быть не должно.
 * Все методы кидают ProviderException наружу, ловим на уровне репозитория.
 */
interface MusicProvider {

    val source: Source

    /** Готов ли провайдер работать: залогинен, токен живой. */
    suspend fun isReady(): Boolean

    suspend fun search(query: String, limit: Int = 30): List<Track>

    /** Прямая ссылка на аудиопоток. Живёт недолго, кэшировать нельзя. */
    suspend fun streamUrl(track: Track): String?

    suspend fun playlists(): List<Playlist>

    suspend fun playlistTracks(playlistId: String): List<Track>
}

class ProviderException(
    val source: Source,
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
