package com.example.feedblocker.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.feedblocker.ScreenTimeCounter
import com.example.feedblocker.ui.theme.FeedBlockerTheme
import com.example.feedblocker.ui.theme.Mint

/**
 * Главный экран в минималистичном стиле Apple:
 *  - сверху — заголовок и кнопка помощи "i";
 *  - по центру — карточка «Экранное время» (день / неделя / месяц / год);
 *  - внизу — единственное действие: «Включить ленту на 5 минут».
 *
 * Кнопки «Снять паузу» больше нет: пауза всегда заканчивается сама через
 * 5 минут (и тем самым корректно учитывается счётчиком — одно включение
 * ленты = ровно +5 минут экранного времени).
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    pauseRemainingMs: Long,
    screenTime: ScreenTimeCounter.Snapshot,
    onEnableServiceClick: () -> Unit,
    onOpenBatterySettingsClick: () -> Unit,
    onOpenAutoStartClick: () -> Unit,
    onPauseClick: () -> Unit
) {
    val isPaused = pauseRemainingMs > 0L
    val colors = MaterialTheme.colorScheme
    var showSetupHelp by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
                Text(
                    text = "FeedBlocker",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Меньше ленты, больше контроля",
                    fontSize = 15.sp,
                    color = colors.onSurfaceVariant
                )
            }
            HelpButton(onClick = { showSetupHelp = true })
        }

        // Счётчик экранного времени — по центру экрана.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            ScreenTimeCard(screenTime = screenTime)
        }

        // Единственное действие на экране. Пока идёт пауза — только
        // обратный отсчёт, кнопки нет (пауза закончится сама).
        if (isPaused) {
            Text(
                text = "Лента включится через ${formatRemaining(pauseRemainingMs)}",
                fontSize = 14.sp,
                color = colors.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        } else {
            Button(
                onClick = onPauseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Mint,
                    contentColor = colors.onPrimary
                )
            ) {
                Text(
                    text = "Включить ленту на 5 минут",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showSetupHelp) {
        SetupHelpDialog(
            onDismiss = { showSetupHelp = false },
            onOpenAccessibility = onEnableServiceClick,
            onOpenAppSettings = onOpenBatterySettingsClick,
            onOpenAutoStart = onOpenAutoStartClick
        )
    }
}

/**
 * Карточка «Экранное время» в духе Apple: спокойная поверхность,
 * скругление 28 dp, крупная типографика, тонкий разделитель и три
 * второстепенных периода (неделя / месяц / год) под главным значением.
 */
@Composable
private fun ScreenTimeCard(screenTime: ScreenTimeCounter.Snapshot) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "ЭКРАННОЕ ВРЕМЯ · СЕГОДНЯ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.2.sp,
                color = colors.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = ScreenTimeCounter.format(screenTime.dayMillis),
                fontSize = 44.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 20.dp),
                thickness = 0.5.dp,
                color = colors.outline.copy(alpha = 0.6f)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PeriodMetric("НЕДЕЛЯ", ScreenTimeCounter.format(screenTime.weekMillis))
                PeriodMetric("МЕСЯЦ", ScreenTimeCounter.format(screenTime.monthMillis))
                PeriodMetric("ГОД", ScreenTimeCounter.format(screenTime.yearMillis))
            }
        }
    }
}

@Composable
private fun PeriodMetric(label: String, value: String) {
    val colors = MaterialTheme.colorScheme
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp,
            color = colors.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = colors.onSurface
        )
    }
}

@Composable
private fun HelpButton(onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "i",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Mint
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Как настроить",
            fontSize = 11.sp,
            color = colors.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SetupHelpDialog(
    onDismiss: () -> Unit,
    onOpenAccessibility: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenAutoStart: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Настройка FeedBlocker",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Защита работает автоматически, как только сервис доступности включён в системных настройках — отдельного переключателя в приложении нет. Разрешения нужно выдать вручную, один раз — используйте кнопки внизу.",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                HelpStep(
                    number = "1",
                    title = "Разрешить ограниченные настройки",
                    body = "Приложение установлено не из Google Play, поэтому Android (начиная с 13-й версии) по умолчанию скрывает переключатель доступности для него. Откройте настройки приложения (кнопка ниже) → нажмите на три точки в правом верхнем углу → «Разрешить ограниченные настройки» → подтвердите. Без этого шага включить сервис доступности не получится."
                )
                HelpStep(
                    number = "2",
                    title = "Специальные возможности",
                    body = "Настройки → Специальные возможности → Скачанные приложения → FeedBlocker. Включите верхний тумблер. Без этого приложение не видит ленту TikTok и YouTube Shorts. Открыть этот экран можно кнопкой «Специальные возможности» ниже."
                )
                HelpStep(
                    number = "3",
                    title = "Уведомления",
                    body = "Если система спросит разрешение на уведомления — разрешите. Через них можно поставить паузу на 5 минут; она закончится автоматически, а уведомление вернётся в состояние «FeedBlocker активен»."
                )
                HelpStep(
                    number = "4",
                    title = "Батарея",
                    body = "В настройках приложения (кнопка «Настройки приложения» ниже) откройте Батарея → «Без ограничений». Иначе Android может остановить сервис в фоне или ночью."
                )
                HelpStep(
                    number = "5",
                    title = "Автозапуск",
                    body = "Отдельный экран для этого есть почти на всех телефонах не от Google — Xiaomi, Huawei/Honor, Oppo/Realme/OnePlus, Vivo и др. Без включённого автозапуска система может выгружать FeedBlocker из фона, и блокировка перестанет работать. Кнопка «Автозапуск» ниже попробует открыть нужный экран автоматически; " + autoStartHint() + " Если кнопка ничего не открыла — откроется страница приложения, ищите пункт вручную."
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onOpenAppSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Настройки приложения")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onOpenAutoStart,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Автозапуск")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOpenAccessibility,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = colors.onPrimary)
                ) {
                    Text("Специальные возможности")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Закрыть")
                }
            }
        }
    }
}

@Composable
private fun HelpStep(number: String, title: String, body: String) {
    val colors = MaterialTheme.colorScheme
    Row(modifier = Modifier.padding(bottom = 14.dp)) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Mint.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, fontWeight = FontWeight.Bold, color = Mint, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.size(10.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(body, fontSize = 13.sp, color = colors.onSurfaceVariant, lineHeight = 18.sp)
        }
    }
}

/** Короткая подсказка под конкретный производитель телефона (см. AutoStartHelper). */
private fun autoStartHint(): String =
    com.example.feedblocker.AutoStartHelper.hintForCurrentDevice()

private fun formatRemaining(ms: Long): String {
    val totalSec = (ms / 1000).coerceAtLeast(0)
    val min = totalSec / 60
    val sec = totalSec % 60
    return "%d:%02d".format(min, sec)
}

@Preview(showBackground = true)
@Composable
private fun MainScreenPreview() {
    FeedBlockerTheme {
        MainScreen(
            pauseRemainingMs = 0,
            screenTime = ScreenTimeCounter.Snapshot(
                dayMillis = 85 * 60_000L,
                weekMillis = 320 * 60_000L,
                monthMillis = 1100 * 60_000L,
                yearMillis = 9600 * 60_000L
            ),
            onEnableServiceClick = {},
            onOpenBatterySettingsClick = {},
            onOpenAutoStartClick = {},
            onPauseClick = {}
        )
    }
}
