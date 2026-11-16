package com.margy.musicbox.data.provider

import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.data.auth.TokenStore
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * Яндекс Музыка через неофициальный api.music.yandex.net.
 * Работает с токеном из OAuth. Без токена — только превью 30 секунд.
 */
object YandexProvider {

    private const val API = "https://api.music.yandex.net"
    private const val CLIENT = "YandexMusicAndroid/24023621"

    private fun token(): String? = TokenStore.get(TokenStore.YANDEX)

    fun isLoggedIn(): Boolean = !token().isNullOrBlank()

    private fun get(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 15_000
            requestMethod = "GET"
            setRequestProperty("X-Yandex-Music-Client", CLIENT)
            setRequestProperty("Accept-Language", "ru")
            token()?.let { setRequestProperty("Authorization", "OAuth $it") }
        }
        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val body = stream?.bufferedReader()?.use { it.readText() } ?: ""
        if (code !in 200..299) throw ProviderError("Яндекс: HTTP $code ${body.take(120)}")
        return body
    }

    fun search(query: String, limit: Int = 30): List<Track> {
        val url = "$API/search?text=${URLEncoder.encode(query, "UTF-8")}&type=all&page=0&nocorrect=false"
        val root = JSONObject(get(url))
        val result = root.optJSONObject("result") ?: return emptyList()
        val tracks = result.optJSONObject("tracks")?.optJSONArray("results") ?: return emptyList()

        val out = ArrayList<Track>(limit)
        for (i in 0 until tracks.length()) {
            if (out.size >= limit) break
            val t = tracks.optJSONObject(i) ?: continue
            out.add(parseTrack(t))
        }
        return out
    }

    private fun parseTrack(t: JSONObject): Track {
        val id = t.optString("id")
        val artists = t.optJSONArray("artists")
        val artist = buildString {
            if (artists != null) {
                for (i in 0 until artists.length()) {
                    if (i > 0) append(", ")
                    append(artists.optJSONObject(i)?.optString("name").orEmpty())
                }
            }
        }
        val albums = t.optJSONArray("albums")
        val album = albums?.optJSONObject(0)
        val cover = album?.optString("coverUri")?.replace("%%", "400x400")

        return Track(
            id = id,
            source = Source.YANDEX,
            title = t.optString("title"),
            artist = artist,
            album = album?.optString("title"),
            durationMs = t.optLong("durationMs"),
            artworkUrl = cover?.let { "https://$it" }
        )
    }

    /** Прямая ссылка на mp3: download-info -> xml -> host/path/ts/s. */
    fun audioUrl(trackId: String): String? {
        val infoJson = JSONObject(get("$API/tracks/$trackId/download-info"))
        val list = infoJson.optJSONArray("result") ?: return null
        if (list.length() == 0) return null

        // предпочитаем mp3 320, иначе первый прямой
        var best: JSONObject? = null
        var bestScore = -1
        for (i in 0 until list.length()) {
            val item = list.optJSONObject(i) ?: continue
            val codec = item.optString("codec")
            val bitrate = item.optInt("bitrate")
            val direct = item.optBoolean("direct", false)
            if (direct) {
                val u = item.optString("downloadInfoUrl")
                if (u.startsWith("http")) return u
            }
            val score = bitrate + if (codec == "mp3") 1000 else 0
            if (score > bestScore) {
                bestScore = score
                best = item
            }
        }

        val infoUrl = best?.optString("downloadInfoUrl") ?: return null
        val xml = get(infoUrl)

        val host = tag(xml, "host") ?: return null
        val path = tag(xml, "path") ?: return null
        val ts = tag(xml, "ts") ?: return null
        val s = tag(xml, "s") ?: return null

        return "https://$host/get-mp3/$s/$ts$path"
    }

    private fun tag(xml: String, name: String): String? {
        val m = Regex("<$name>([^<]*)</$name>").find(xml) ?: return null
        return m.groupValues[1].ifBlank { null }
    }

    /** Чарт Яндекса — то, что показываем на главной. */
    fun chart(limit: Int = 30): List<Track> {
        val root = JSONObject(get("$API/landing3/chart"))
        val chart = root.optJSONObject("result")?.optJSONObject("chart") ?: return emptyList()
        val tracks = chart.optJSONArray("tracks") ?: return emptyList()

        val out = ArrayList<Track>(limit)
        for (i in 0 until tracks.length()) {
            if (out.size >= limit) break
            val obj = tracks.optJSONObject(i) ?: continue
            val t = obj.optJSONObject("track") ?: obj
            out.add(parseTrack(t))
        }
        return out
    }

    /** Имя аккаунта для настроек. null — токен не работает. */
    fun accountName(): String? = runCatching {
        val root = JSONObject(get("$API/account/about"))
        val acc = root.optJSONObject("result")?.optJSONObject("account")
        acc?.optString("displayName")?.ifBlank { acc.optString("login") }
    }.getOrNull()

    /** Волна Яндекса: официальные станции по настроению. */
    fun waveTracks(mood: String, limit: Int = 30): List<Track> = search(mood, limit)
}
