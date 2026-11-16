package com.margy.musicbox.data.provider

import com.margy.musicbox.core.model.Source
import com.margy.musicbox.core.model.Track
import com.margy.musicbox.data.auth.TokenStore
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * ВК Музыка. С user-токеном со scope audio.
 * В свежих версиях API трек отдаёт готовый url на mp3 — его и играем.
 */
object VkProvider {

    private const val API = "https://api.vk.com/method"
    private const val VERSION = "5.199"

    private fun token(): String? = TokenStore.get(TokenStore.VK)

    fun isLoggedIn(): Boolean = !token().isNullOrBlank()

    private fun call(method: String, params: Map<String, String>): JSONObject {
        val t = token() ?: throw ProviderError("ВК: сначала войди")
        val query = params.entries.joinToString("&") { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
        val url = "$API/$method?$query&access_token=$t&v=$VERSION"

        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 15_000
            setRequestProperty("User-Agent", "VKAndroidApp/7.14")
        }
        val body = conn.inputStream.bufferedReader().use { it.readText() }
        val json = JSONObject(body)

        if (json.has("error")) {
            val err = json.getJSONObject("error")
            throw ProviderError("ВК: ${err.optString("error_msg")}")
        }
        return json
    }

    fun search(query: String, limit: Int = 30): List<Track> {
        val json = call(
            "audio.search",
            mapOf("q" to query, "count" to limit.toString(), "auto_complete" to "1")
        )
        val items = json.optJSONObject("response")?.optJSONArray("items") ?: return emptyList()
        return parseItems(items)
    }

    fun myTracks(limit: Int = 50): List<Track> {
        val json = call("audio.get", mapOf("count" to limit.toString()))
        val items = json.optJSONObject("response")?.optJSONArray("items") ?: return emptyList()
        return parseItems(items)
    }

    private fun parseItems(items: org.json.JSONArray): List<Track> {
        val out = ArrayList<Track>()
        for (i in 0 until items.length()) {
            val a = items.optJSONObject(i) ?: continue
            if (a.optString("url").isBlank()) continue
            out.add(
                Track(
                    id = "${a.optString("owner_id")}_${a.optString("id")}",
                    source = Source.VK,
                    title = a.optString("title"),
                    artist = a.optString("artist"),
                    durationMs = a.optLong("duration") * 1000L,
                    artworkUrl = a.optString("thumb").takeIf { it.startsWith("http") },
                    streamUrl = a.optString("url")
                )
            )
        }
        return out
    }

    /** В ВК ссылка уже пришла вместе с треком. */
    fun audioUrl(track: Track): String? = track.streamUrl
}
