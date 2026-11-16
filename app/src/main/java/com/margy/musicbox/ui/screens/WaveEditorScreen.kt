package com.margy.musicbox.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.margy.musicbox.ui.components.DotText
import com.margy.musicbox.ui.components.NothingChip
import com.margy.musicbox.ui.theme.NothingLine
import com.margy.musicbox.ui.theme.NothingRed
import com.margy.musicbox.ui.theme.NothingWhite

private val Moods = listOf(
    "весёлое", "грустное", "спокойное", "дерзкое",
    "романтика", "меланхолия", "злость", "ночной драйв"
)
private val Activities = listOf(
    "учёба", "спорт", "дорога", "работа",
    "сон", "вечеринка", "готовка", "думать"
)
private val Languages = listOf("любой", "русский", "английский")

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WaveEditorScreen(
    onBack: () -> Unit,
    onStart: () -> Unit,
    selectedMoods: List<String>,
    onMoodsChange: (List<String>) -> Unit,
    energy: Float,
    onEnergyChange: (Float) -> Unit
) {
    var activity by remember { mutableIntStateOf(0) }
    var language by remember { mutableIntStateOf(0) }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 16.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.ArrowBack,
                    contentDescription = "назад",
                    tint = NothingWhite
                )
            }
            DotText(
                text = "РЕДАКТОР ВОЛНЫ",
                modifier = Modifier.weight(1f),
                height = 22.dp,
                color = NothingWhite
            )        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            item {                Text(
                    "Волна собирает трек из всех трёх сервисов по этим настройкам. Чем больше крутишь — тем точнее попадание.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                SectionLabel("НАСТРОЕНИЕ")
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Moods.forEach { mood ->
                        NothingChip(
                            text = mood,
                            selected = selectedMoods.contains(mood),
                            onClick = {
                                onMoodsChange(
                                    if (selectedMoods.contains(mood)) selectedMoods - mood
                                    else selectedMoods + mood
                                )
                            }
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                SectionLabel("ЧЕМ ЗАНИМАЕШЬСЯ")
            }

            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Activities.forEachIndexed { index, itemText ->
                        NothingChip(
                            text = itemText,
                            selected = activity == index,
                            onClick = { activity = index }
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                SectionLabel("ЯЗЫК")
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Languages.forEachIndexed { index, lang ->
                        NothingChip(
                            text = lang,
                            selected = language == index,
                            onClick = { language = index }
                        )
                    }
                }
                Spacer(Modifier.height(22.dp))
                SectionLabel("ЭНЕРГИЯ")
            }

            item {
                Slider(
                    value = energy,
                    onValueChange = onEnergyChange,
                    colors = SliderDefaults.colors(
                        thumbColor = NothingRed,
                        activeTrackColor = NothingRed,
                        inactiveTrackColor = NothingLine
                    )
                )
                Row(Modifier.fillMaxWidth()) {
                    Text(
                        "СПОКОЙНО",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        "ЖЁСТКО",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(28.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NothingRed)
                        .clickable {
                            onStart()
                            onBack()
                        },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = NothingWhite,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "СЛУШАТЬ ВОЛНУ",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = NothingWhite
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NothingLine, RoundedCornerShape(14.dp))
                        .clickable { }
                        .padding(vertical = 14.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "СКИНУТЬ НАСТРОЙКИ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

/** Костыльное, но рабочее состояние: просто индекс в обычной переменной. */
@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = NothingRed,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 10.dp)
    )
}
