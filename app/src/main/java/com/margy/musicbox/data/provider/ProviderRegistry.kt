package com.margy.musicbox.data.provider

import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Точка входа для UI: поиск и стрим по любому источнику.
 * Пока живой только YouTube, остальные честно говорят, что нужен токен.
 */
object ProviderRegistry {

    suspend fun search(source: Source, query: String): List<Track> = withContext(Dispatchers.IO) {
        when (source) {
            Source.YOUTUBE -> YouTubeProvider.search(query)
            Source.YANDEX -> throw ProviderError("Яндекс: нужен OAuth-токен, прикрутим следом")
            Source.VK -> throw ProviderError("ВК: нужен токен со scope audio, прикрутим следом")
        }
    }

    suspend fun searchAll(query: String): List<Track> = withContext(Dispatchers.IO) {
        val yt = try {
            YouTubeProvider.search(query)
        } catch (e: Exception) {
            emptyList()
        }
        yt
    }

    suspend fun streamUrl(track: Track): String? = withContext(Dispatchers.IO) {
        when (track.source) {
            Source.YOUTUBE -> YouTubeProvider.audioUrl(track.id)
            Source.YANDEX, Source.VK -> null
        }
    }
}

class ProviderError(message: String) : Exception(message)
