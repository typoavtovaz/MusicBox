package com.margy.musicbox.data.net

import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Минимальный Downloader для NewPipeExtractor на голом HttpURLConnection.
 * Никаких лишних зависимостей.
 */
class SimpleDownloader : Downloader() {

    @Throws(IOException::class)
    override fun execute(request: Request): Response {
        val conn = (URL(request.url()).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 20_000
            instanceFollowRedirects = true
            requestMethod = request.httpMethod()
            setRequestProperty("User-Agent", USER_AGENT)
            setRequestProperty("Accept-Language", "ru-RU,ru;q=0.9,en;q=0.8")
        }

        request.headers().forEach { (name, values) ->
            if (name != null && values.isNotEmpty()) {
                conn.setRequestProperty(name, values.joinToString(","))
            }
        }

        val body = request.dataToSend()
        if (body != null && request.httpMethod() != "GET" && request.httpMethod() != "HEAD") {
            conn.doOutput = true
            conn.outputStream.use { it.write(body) }
        }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = try {
            stream?.bufferedReader()?.use { it.readText() } ?: ""
        } catch (e: Exception) {
            ""
        }

        val headers = HashMap<String, List<String>>()
        conn.headerFields.forEach { (key, values) ->
            if (key != null) headers[key] = values
        }

        return Response(
            code,
            conn.responseMessage ?: "",
            headers,
            text,
            conn.url?.toString() ?: request.url()
        )
    }

    companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) " +
                "Chrome/124.0.0.0 Mobile Safari/537.36"
    }
}
