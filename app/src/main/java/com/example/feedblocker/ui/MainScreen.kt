package com.example.feedblocker.ui

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.feedblocker.ui.theme.FeedBlockerTheme
import com.example.feedblocker.ui.theme.Mint

/**
 * Главный экран. Ручного переключателя защиты больше нет: блокировка
 * работает всегда, как только включён сервис доступности — единственное
 * временное исключение — кнопка паузы на 5 минут.
 */
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    isServiceEnabled: Boolean,
    pauseRemainingMs: Long,
    onEnableServiceClick: () -> Unit,
    onOpenBatterySettingsClick: () -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    val isPaused = pauseRemainingMs > 0L
    val colors = MaterialTheme.colorScheme
    var showSetupHelp by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
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

        Spacer(modifier = Modifier.height(28.dp))

        StatusCard(
            isServiceEnabled = isServiceEnabled,
            isPaused = isPaused,
            pauseRemainingMs = pauseRemainingMs
        )

        if (isServiceEnabled) {
            Spacer(modifier = Modifier.height(20.dp))
            if (isPaused) {
                Button(
                    onClick = onResumeClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = colors.onPrimary)
                ) {
                    Text("Снять паузу")
                }
            } else {
                OutlinedButton(
                    onClick = onPauseClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Выключить блокировку на 5 минут")
                }
            }
        }
    }

    if (showSetupHelp) {
        SetupHelpDialog(
            onDismiss = { showSetupHelp = false },
            onOpenAccessibility = onEnableServiceClick,
            onOpenAppSettings = onOpenBatterySettingsClick
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
    onOpenAppSettings: () -> Unit
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
                    body = "Если система спросит разрешение на уведомления — разрешите. Через них можно поставить паузу на 5 минут и видеть, что защита включена."
                )
                HelpStep(
                    number = "4",
                    title = "Батарея и автозапуск",
                    body = "В настройках приложения (кнопка «Настройки приложения» ниже) откройте Батарея → «Без ограничений» и, если есть, включите автозапуск. Иначе Android может остановить сервис в фоне или ночью."
                )
                HelpStep(
                    number = "5",
                    title = "Xiaomi, Huawei, Oppo и похожие",
                    body = "На некоторых оболочках пункты автозапуска называются иначе (например, «Автозапуск» или «Запуск приложений») и находятся в отдельном системном приложении, а не в настройках FeedBlocker."
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onOpenAppSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Настройки приложения (батарея, автозапуск)")
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

@Composable
private fun StatusCard(
    isServiceEnabled: Boolean,
    isPaused: Boolean,
    pauseRemainingMs: Long
) {
    val colors = MaterialTheme.colorScheme
    val (title, subtitle, dotColor) = when {
        isPaused -> Triple(
            "На паузе",
            "Снова включится через ${formatRemaining(pauseRemainingMs)}",
            colors.error
        )
        isServiceEnabled -> Triple(
            "Включена",
            "Защита работает",
            Mint
        )
        else -> Triple(
            "Выключена",
            "Защита сейчас не работает",
            colors.onSurfaceVariant
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.size(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, color = colors.onSurface)
                Text(subtitle, fontSize = 13.sp, color = colors.onSurfaceVariant)
            }
        }
    }
}

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
            isServiceEnabled = true,
            pauseRemainingMs = 0,
            onEnableServiceClick = {},
            onOpenBatterySettingsClick = {},
            onPauseClick = {},
            onResumeClick = {}
        )
    }
}
