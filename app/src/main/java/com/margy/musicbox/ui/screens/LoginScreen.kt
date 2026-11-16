package com.margy.musicbox.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.data.auth.TokenStore
import com.margy.musicbox.data.auth.YandexAuth
import com.margy.musicbox.ui.components.DotText
import com.margy.musicbox.ui.theme.NothingLine
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingWhite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun LoginScreen(
    source: Source,
    onBack: () -> Unit,
    onDone: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
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

        if (source == Source.YANDEX) {
            YandexDeviceLogin(onDone = onDone)
        } else {
            ManualTokenLogin(
                source = source,
                hint = "ВК пока принимает только токен. Вставь его ниже — сохраню на телефоне.",
                onDone = onDone
            )
        }
    }
}

/**
 * Яндекс: Device Flow. Показываем короткий код, пользователь подтверждает
 * на ya.ru/device, мы сами забираем токен. Пароль никуда не пересылается.
 */
@Composable
private fun YandexDeviceLogin(onDone: () -> Unit) {
    val context = LocalContext.current
    var attempt by remember { mutableIntStateOf(0) }
    var code by remember { mutableStateOf<String?>(null) }
    var status by remember { mutableStateOf("получаю код…") }
    var error by remember { mutableStateOf<String?>(null) }
    var secondsLeft by remember { mutableIntStateOf(0) }

    LaunchedEffect(attempt) {
        if (attempt == 0) return@LaunchedEffect
        error = null
        code = null
        status = "получаю код…"

        val device = try {
            withContext(Dispatchers.IO) { YandexAuth.requestCode() }
        } catch (e: Exception) {
            error = "яндекс не ответил: ${e.message}"
            return@LaunchedEffect
        }

        code = device.userCode
        secondsLeft = device.expiresIn
        status = "открой ya.ru/device и введи код"

        val step = device.interval.coerceAtLeast(2)
        while (secondsLeft > 0) {
            delay(step * 1000L)
            secondsLeft -= step
            val token = try {
                withContext(Dispatchers.IO) { YandexAuth.poll(device.deviceCode) }
            } catch (e: Exception) {
                error = "ошибка входа: ${e.message}"
                return@LaunchedEffect
            }
            if (token != null) {
                TokenStore.save(TokenStore.YANDEX, token)
                status = "готово"
                onDone()
                return@LaunchedEffect
            }
        }
        error = "код истёк, запроси новый"
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        Text(
            "Яндекс Музыка",
            style = MaterialTheme.typography.titleMedium,
            color = NothingWhite
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Жми «получить код», открой ya.ru/device и введи код. Пароль останется на сайте Яндекса.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        when {
            error != null -> Text(
                error ?: "",
                color = NothingRed,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )

            code == null -> CircularProgressIndicator(color = NothingRed)
        }

        code?.let { c ->
            Text(
                c.uppercase(),
                color = NothingWhite,
                fontSize = 44.sp,
                letterSpacing = 8.sp,
                style = MaterialTheme.typography.displaySmall
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "осталось ${secondsLeft}с",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(NothingRed)
                    .clickable {
                        context.startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://ya.ru/device")
                            )
                        )
                    }
                    .padding(horizontal = 22.dp, vertical = 12.dp)
            ) {
                Text("ОТКРЫТЬ YA.RU/DEVICE", color = NothingWhite, style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            status,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(22.dp))

        Box(
            Modifier
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, NothingLine, RoundedCornerShape(12.dp))
                .clickable { attempt++ }
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            Text(
                if (code == null) "ПОЛУЧИТЬ КОД" else "НОВЫЙ КОД",
                color = NothingWhite,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ManualTokenLogin(
    source: Source,
    hint: String,
    onDone: () -> Unit
) {
    var manual by remember { mutableStateOf("") }

    fun accept(raw: String) {
        val token = raw
            .substringAfter("access_token=", raw)
            .substringBefore("&")
            .substringBefore("#")
            .trim()
        if (token.length < 8) return
        TokenStore.save(
            if (source == Source.YANDEX) TokenStore.YANDEX else TokenStore.VK,
            token
        )
        onDone()
    }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(12.dp))
        Text(source.title, style = MaterialTheme.typography.titleMedium, color = NothingWhite)
        Spacer(Modifier.height(6.dp))
        Text(
            hint,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = manual,
            onValueChange = { manual = it },
            modifier = Modifier.fillMaxWidth(),
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

        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (manual.isBlank()) NothingLine else NothingRed)
                .clickable(enabled = manual.isNotBlank()) { accept(manual) },
            contentAlignment = Alignment.Center
        ) {
            Text("СОХРАНИТЬ", color = NothingWhite, style = MaterialTheme.typography.labelLarge)
        }
    }
}
