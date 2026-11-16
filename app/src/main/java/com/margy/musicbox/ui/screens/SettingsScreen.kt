package com.margy.musicbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.margy.musicbox.core.model.Source
import com.margy.musicbox.data.provider.ProviderRegistry
import com.margy.musicbox.ui.components.DotText
import com.margy.musicbox.ui.theme.NothingLine
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingWhite

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogin: (Source) -> Unit,
    accountsVersion: Int
) {
    var wifiOnly by remember { mutableStateOf(true) }
    var hiQuality by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "назад",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            DotText(
                text = "НАСТРОЙКИ",
                modifier = Modifier.weight(1f),
                height = 26.dp,
                color = NothingWhite
            )
        }

        Spacer(Modifier.height(16.dp))
        Text(
            "АККАУНТЫ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        listOf(Source.YANDEX, Source.VK).forEach { src ->
            val on = ProviderRegistry.isLoggedIn(src)
            RowCard(
                modifier = Modifier.clickable(enabled = !on) { onLogin(src) }
            ) {
                Column(Modifier.weight(1f)) {
                    Text(src.title, color = MaterialTheme.colorScheme.onSurface)
                    Text(
                        if (on) "подключено" else "не подключено",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    if (on) "сменить" else "войти",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (on) MaterialTheme.colorScheme.onSurfaceVariant else NothingRed
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        RowCard {
            Column(Modifier.weight(1f)) {
                Text("YouTube", color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "нужен прокси-сервер: Google с этой сети недоступен",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                "выкл",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "ВОСПРОИЗВЕДЕНИЕ",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        RowCard {
            Column(Modifier.weight(1f)) {
                Text("Скачивать только по Wi-Fi", color = MaterialTheme.colorScheme.onSurface)
            }
            SwitchToggle(wifiOnly) { wifiOnly = it }
        }

        RowCard {
            Column(Modifier.weight(1f)) {
                Text("Высокое качество", color = MaterialTheme.colorScheme.onSurface)
                Text(
                    "256 kbps, больше трафика",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            SwitchToggle(hiQuality) { hiQuality = it }
        }

        Spacer(Modifier.height(24.dp))
        Text(
            "MusicBox 0.3 · яндекс + вк",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun RowCard(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(NothingWhite.copy(alpha = 0.04f))
            .border(1.dp, NothingLine, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

@Composable
private fun SwitchToggle(checked: Boolean, onChange: (Boolean) -> Unit) {
    Switch(
        checked = checked,
        onCheckedChange = onChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = NothingRed
        )
    )
}
