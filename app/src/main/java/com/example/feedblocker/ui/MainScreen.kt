package com.example.feedblocker.ui

import androidx.compose.foundation.background
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feedblocker.ui.theme.FeedBlockerTheme
import com.example.feedblocker.ui.theme.Mint

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    isServiceEnabled: Boolean,
    pauseRemainingMs: Long,
    onEnableServiceClick: () -> Unit,
    onOpenBatterySettingsClick: () -> Unit,
    onToggleService: (Boolean) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    val isPaused = pauseRemainingMs > 0L
    val protectionOn = isServiceEnabled && !isPaused
    val colors = MaterialTheme.colorScheme

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
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

        Spacer(modifier = Modifier.height(28.dp))

        StatusCard(
            isServiceEnabled = isServiceEnabled,
            isPaused = isPaused,
            pauseRemainingMs = pauseRemainingMs
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Защита",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = colors.onSurface
                    )
                    Text(
                        text = if (protectionOn) "TikTok и Shorts блокируются" else "Ленты сейчас не фильтруются",
                        fontSize = 13.sp,
                        color = colors.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isServiceEnabled && !isPaused,
                    onCheckedChange = { checked ->
                        if (checked && !isServiceEnabled) {
                            onEnableServiceClick()
                        } else if (checked && isPaused) {
                            onResumeClick()
                        } else {
                            onToggleService(checked)
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colors.onPrimary,
                        checkedTrackColor = Mint,
                        uncheckedThumbColor = colors.onSurface,
                        uncheckedTrackColor = colors.outline
                    )
                )
            }
        }

        if (isServiceEnabled) {
            Spacer(modifier = Modifier.height(12.dp))
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
                    Text("Пауза на 5 минут")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Настройка",
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = colors.onBackground
        )
        Spacer(modifier = Modifier.height(12.dp))

        SetupRow(
            step = "1",
            title = "Сервис доступности",
            body = "Скачанные приложения → FeedBlocker → включите верхний тумблер",
            actionLabel = if (isServiceEnabled) "Открыть ещё раз" else "Включить",
            onClick = onEnableServiceClick
        )
        Spacer(modifier = Modifier.height(10.dp))
        SetupRow(
            step = "2",
            title = "Батарея",
            body = "Уберите ограничения, иначе Android остановит блокировку",
            actionLabel = "Настройки приложения",
            onClick = onOpenBatterySettingsClick
        )
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
            "Активна",
            "Рекомендации TikTok и YouTube Shorts закрываются",
            Mint
        )
        else -> Triple(
            "Нужно разрешение",
            "Без сервиса доступности блокировка не работает",
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

@Composable
private fun SetupRow(
    step: String,
    title: String,
    body: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Mint.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(step, fontWeight = FontWeight.Bold, color = Mint, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.size(10.dp))
                Text(title, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(body, fontSize = 13.sp, color = colors.onSurfaceVariant, lineHeight = 18.sp)
            TextButton(onClick = onClick) {
                Text(actionLabel)
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
            onToggleService = {},
            onPauseClick = {},
            onResumeClick = {}
        )
    }
}
