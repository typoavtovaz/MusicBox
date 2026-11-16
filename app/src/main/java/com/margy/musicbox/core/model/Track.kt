package com.margy.musicbox.core.model

/** Откуда трек. Пока три источника, спотифай в жопе. */
enum class Source(val title: String) {
    YOUTUBE("YouTube Music"),
    YANDEX("Яндекс Музыка"),
    VK("ВК Музыка")
}

/**
 * Единая модель трека. Всё, что нужно экрану и плееру.
 * streamUrl заполняется лениво провайдером, потому что ссылки протухают.
 */
data class Track(
    val id: String,
    val source: Source,
    val title: String,
    val artist: String,
    val album: String? = null,
    val durationMs: Long = 0L,
    val artworkUrl: String? = null,
    val streamUrl: String? = null
) {
    val key: String get() = "${source.name}:$id"
}
