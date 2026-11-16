package com.margy.musicbox.data.auth

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

/**
 * OAuth Device Flow Яндекса: показываем пользователю короткий код,
 * он подтверждает вход на ya.ru/device, мы забираем токен.
 * Пароль нигде через нас не проходит.
 */
object YandexAuth {

    private const val OAUTH = "https://oauth.yandex.ru"

    // Публичные креды официального андроид-клиента Яндекс Музыки
    private const val CLIENT_ID = "23cabbbdc6cd418abb4b39c32c41195d"
    private const val CLIENT_SECRET = "53bc75238f0c4d08a118e51fe9203300"

    data class DeviceCode(
        val deviceCode: String,
        val userCode: String,
        val verificationUrl: String,
        val expiresIn: Int,
        val interval: Int
    )

    private fun post(path: String, params: Map<String, String>): String {
        val conn = (URL(OAUTH + path).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            connectTimeout = 15_000
            readTimeout = 15_000
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            setRequestProperty("User-Agent", "MusicBox/0.3")
        }
        val body = params.entries.joinToString("&") {
            "${URLEncoder.encode(it.key, "UTF-8")}=${URLEncoder.encode(it.value, "UTF-8")}"
        }
        conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
        if (code !in 200..299) throw IllegalStateException("HTTP $code ${text.take(160)}")
        return text
    }

    fun requestCode(): DeviceCode {
        val json = JSONObject(
            post(
                "/device/code",
                mapOf(
                    "client_id" to CLIENT_ID,
                    "device_id" to (1..10).map { (('a'..'z') + ('0'..'9')).random() }.joinToString(""),
                    "device_name" to "MusicBox"
                )
            )
        )
        return DeviceCode(
            deviceCode = json.getString("device_code"),
            userCode = json.getString("user_code"),
            verificationUrl = json.optString("verification_url", "https://ya.ru/device"),
            expiresIn = json.optInt("expires_in", 300),
            interval = json.optInt("interval", 5)
        )
    }

    /** null — пользователь ещё не подтвердил. */
    fun poll(deviceCode: String): String? {
        return try {
            val json = JSONObject(
                post(
                    "/token",
                    mapOf(
                        "grant_type" to "device_code",
                        "code" to deviceCode,
                        "client_id" to CLIENT_ID,
                        "client_secret" to CLIENT_SECRET
                    )
                )
            )
            json.optString("access_token").ifBlank { null }
        } catch (e: IllegalStateException) {
            if (e.message?.contains("authorization_pending") == true) null else throw e
        }
    }
}
