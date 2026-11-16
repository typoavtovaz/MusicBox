package com.margy.musicbox.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.data.auth.TokenStore
import com.margy.musicbox.ui.components.DotText
import com.margy.musicbox.ui.theme.NothingLine
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingWhite

private const val YANDEX_CLIENT = "23cabbbdc6cd418abb4b39c32c41195e"
private const val VK_CLIENT = "2685278"

private fun authUrl(source: Source): String = when (source) {
    Source.YANDEX ->
        "https://oauth.yandex.ru/authorize?response_type=token&client_id=$YANDEX_CLIENT"
    Source.VK ->
        "https://oauth.vk.com/authorize?client_id=$VK_CLIENT&scope=audio,offline" +
            "&redirect_uri=https://oauth.vk.com/blank.html&display=mobile&response_type=token&v=5.199"
    Source.YOUTUBE -> ""
}

/**
 * Вход в сервис. Открываем их родную страницу логина, токен ловим из адреса.
 * Никаких паролей через нас — они вводятся на сайте сервиса.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginScreen(
    source: Source,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    var status by remember { mutableStateOf<String?>(null) }
    var manual by remember { mutableStateOf("") }

    fun accept(raw: String) {
        val token = raw.substringAfter("access_token=", "")
            .substringBefore("&")
            .substringBefore("#")
            .trim()
            .ifBlank { raw.trim() }
        if (token.length < 8) {
            status = "токен не поймал, попробуй вставить руками"
            return
        }
        TokenStore.save(
            if (source == Source.YANDEX) TokenStore.YANDEX else TokenStore.VK,
            token
        )
        onDone()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = "назад", tint = Color.White)
            }
            DotText(
                text = "ВХОД",
                modifier = Modifier.weight(1f),
                height = 26.dp,
                color = NothingWhite
            )
        }

        Text(
            "Логинишься в ${source.title}. Пароль вводится на их сайте, к нам попадает только токен и хранится на телефоне.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(Modifier.height(8.dp))

        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
                .border(1.dp, NothingLine, RoundedCornerShape(12.dp)),
            factory = { ctx ->
                WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.userAgentString =
                        "Mozilla/5.0 (Linux; Android 14; Pixel 7) AppleWebKit/537.36 " +
                            "(KHTML, like Gecko) Chrome/124.0.0.0 Mobile Safari/537.36"
                    webViewClient = object : WebViewClient() {
                        override fun shouldOverrideUrlLoading(
                            view: WebView?,
                            request: WebResourceRequest?
                        ): Boolean {
                            val u = request?.url?.toString().orEmpty()
                            if (u.contains("access_token=")) {
                                accept(u)
                                return true
                            }
                            return false
                        }

                        override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                            if (url != null && url.contains("access_token=")) accept(url)
                        }
                    }
                    loadUrl(authUrl(source))
                }
            }
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "если окно не открылось — вставь токен вручную",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(6.dp))

        OutlinedTextField(
            value = manual,
            onValueChange = { manual = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            placeholder = { Text("токен") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NothingRed,
                unfocusedBorderColor = NothingLine,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                cursorColor = NothingRed
            )
        )

        Box(
            Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(48.dp)
                .background(if (manual.isBlank()) NothingLine else NothingRed, RoundedCornerShape(12.dp))
                .clickable(enabled = manual.isNotBlank()) { accept(manual) },
            contentAlignment = Alignment.Center
        ) {
            Text("СОХРАНИТЬ ТОКЕН", color = NothingWhite, style = MaterialTheme.typography.labelLarge)
        }

        status?.let {
            Text(
                it,
                color = NothingRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.height(12.dp))
    }
}
