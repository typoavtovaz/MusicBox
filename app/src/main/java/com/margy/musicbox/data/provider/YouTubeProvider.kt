package com.margy.musicbox.data.provider

import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.data.net.SimpleDownloader
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.ServiceList
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import java.net.URLDecoder

/**
 * YouTube / YouTube Music через NewPipeExtractor.
 * Отдаём аудио-поток (m4a/webm) — ExoPlayer играет его как обычный media item.
 */
object YouTubeProvider {

    @Volatile
    private var initialized = false

    private fun ensureInit() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            NewPipe.init(
                SimpleDownloader(),
                Localization("ru", "RU"),
                ContentCountry("RU")
            )
            initialized = true
        }
    }

    fun search(query: String, limit: Int = 25): List<Track> {
        ensureInit()
        val service = NewPipe.getService(ServiceList.YouTube.serviceId)
        val extractor = service.getSearchExtractor(query)
        extractor.fetchPage()

        return extractor.initialPage.items
            .filterIsInstance<StreamInfoItem>()
            .take(limit)
            .map { item ->
                Track(
                    id = item.url ?: item.name,
                    source = Source.YOUTUBE,
                    title = item.name ?: "без названия",
                    artist = item.uploaderName ?: "",
                    durationMs = (item.duration ?: 0L) * 1000L,
                    artworkUrl = item.thumbnails.firstOrNull()?.url
                )
            }
    }

    fun searchMusic(query: String, limit: Int = 20): List<Track> =
        search("$query audio", limit)

    /** Прямая ссылка на аудиопоток. Живёт пару часов, поэтому не кэшируем. */
    fun audioUrl(trackId: String): String? {
        ensureInit()
        val service = NewPipe.getService(ServiceList.YouTube.serviceId)
        val url = normalizeUrl(trackId)

        val extractor = service.getStreamExtractor(url)
        extractor.fetchPage()

        val audio = extractor.audioStreams
            .filter { it.isUrl && !it.content.isNullOrBlank() }
            .maxByOrNull { it.averageBitrate }

        return audio?.content
    }

    private fun normalizeUrl(raw: String): String = when {
        raw.startsWith("http") -> raw
        raw.length == 11 -> "https://www.youtube.com/watch?v=$raw"
        else -> "https://www.youtube.com/watch?v=" + URLDecoder.decode(raw, "UTF-8")
    }
}
