package com.margy.musicbox.data.provider

import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

/**
 * Единая точка входа. YouTube выключен: с российских сетей Google недоступен,
 * для него нужен прокси-сервер.
 */
object ProviderRegistry {

    fun isLoggedIn(source: Source): Boolean = when (source) {
        Source.YANDEX -> YandexProvider.isLoggedIn()
        Source.VK -> VkProvider.isLoggedIn()
        Source.YOUTUBE -> false
    }

    fun isAnyLoggedIn(): Boolean =
        YandexProvider.isLoggedIn() || VkProvider.isLoggedIn()

    suspend fun search(source: Source, query: String): List<Track> = withContext(Dispatchers.IO) {
        when (source) {
            Source.YANDEX -> YandexProvider.search(query)
            Source.VK -> VkProvider.search(query)
            Source.YOUTUBE -> throw ProviderError("YouTube недоступен без прокси-сервера")
        }
    }

    /** Все подключённые сервисы разом: кто ответил — того треки и показываем. */
    suspend fun searchAll(query: String): Map<Source, List<Track>> = withContext(Dispatchers.IO) {
        coroutineScope {
            val ya = async {
                if (YandexProvider.isLoggedIn()) runCatching { YandexProvider.search(query) }.getOrNull() else null
            }
            val vk = async {
                if (VkProvider.isLoggedIn()) runCatching { VkProvider.search(query) }.getOrNull() else null
            }

            buildMap {
                ya.await()?.takeIf { it.isNotEmpty() }?.let { put(Source.YANDEX, it) }
                vk.await()?.takeIf { it.isNotEmpty() }?.let { put(Source.VK, it) }
            }
        }
    }

    /** Главная: чарт Яндекса, иначе мои треки из ВК. */
    suspend fun feed(limit: Int = 30): List<Track> = withContext(Dispatchers.IO) {
        if (YandexProvider.isLoggedIn()) {
            runCatching { YandexProvider.chart(limit) }.getOrNull()
                ?.takeIf { it.isNotEmpty() }
                ?: runCatching { YandexProvider.search("хиты", limit) }.getOrDefault(emptyList())
        } else if (VkProvider.isLoggedIn()) {
            runCatching { VkProvider.myTracks(limit) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
    }

    /** «Моя волна»: берём поток треков под настроение. */
    suspend fun wave(mood: String, limit: Int = 30): List<Track> = withContext(Dispatchers.IO) {
        if (YandexProvider.isLoggedIn()) {
            runCatching { YandexProvider.waveTracks(mood, limit) }.getOrDefault(emptyList())
        } else if (VkProvider.isLoggedIn()) {
            runCatching { VkProvider.myTracks(limit) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
    }

    suspend fun streamUrl(track: Track): String? = withContext(Dispatchers.IO) {
        when (track.source) {
            Source.YANDEX -> YandexProvider.audioUrl(track.id)
            Source.VK -> VkProvider.audioUrl(track)
            Source.YOUTUBE -> null
        }
    }
}

class ProviderError(message: String) : Exception(message)
