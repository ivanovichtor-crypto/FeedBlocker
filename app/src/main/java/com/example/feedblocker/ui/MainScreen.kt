package com.example.feedblocker.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.feedblocker.ui.theme.FeedBlockerTheme
import com.example.feedblocker.ui.theme.Mint
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    isServiceEnabled: Boolean,
    isBlockingEnabled: Boolean,
    pauseRemainingMs: Long,
    onEnableServiceClick: () -> Unit,
    onOpenBatterySettingsClick: () -> Unit,
    onToggleService: (Boolean) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit
) {
    val isPaused = pauseRemainingMs > 0L
    val protectionOn = isServiceEnabled && isBlockingEnabled && !isPaused
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
            isBlockingEnabled = isBlockingEnabled,
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
                ProtectionSwitch(
                    checked = protectionOn,
                    onCheckedChange = { checked ->
                        if (checked && !isServiceEnabled) {
                            onEnableServiceClick()
                        } else if (checked && isPaused) {
                            onResumeClick()
                        } else {
                            onToggleService(checked)
                        }
                    }
                )
            }
        }

        if (isServiceEnabled && isBlockingEnabled) {
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
private fun ProtectionSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 28.dp else 4.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "thumbOffset"
    )
    val trackColor by animateColorAsState(
        targetValue = if (checked) Mint else colors.outline,
        animationSpec = tween(durationMillis = 220),
        label = "trackColor"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) Color.White else colors.onSurface,
        animationSpec = tween(durationMillis = 220),
        label = "thumbColor"
    )
    val scale = remember { Animatable(1f) }
    LaunchedEffect(checked) {
        scale.snapTo(0.86f)
        scale.animateTo(
            1f,
            spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Box(
        modifier = Modifier
            .width(56.dp)
            .height(32.dp)
            .scale(scale.value)
            .clip(RoundedCornerShape(16.dp))
            .background(trackColor)
            .semantics {
                role = Role.Switch
                toggleableState = if (checked) ToggleableState.On else ToggleableState.Off
            }
            .clickable { onCheckedChange(!checked) }
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset, y = 4.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(thumbColor)
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
                    text = "Как настроить",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colors.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Без системных разрешений Android остановит блокировку лент. Сделайте это один раз.",
                    fontSize = 13.sp,
                    color = colors.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                HelpStep(
                    number = "1",
                    title = "Сервис доступности",
                    body = "Настройки → Специальные возможности → Скачанные приложения → FeedBlocker. Включите верхний тумблер. Без этого приложение не видит ленту TikTok и YouTube Shorts."
                )
                HelpStep(
                    number = "2",
                    title = "Уведомления",
                    body = "Если система спросит разрешение на уведомления — разрешите. Через них можно поставить паузу на 5 минут и видеть, что защита включена."
                )
                HelpStep(
                    number = "3",
                    title = "Батарея",
                    body = "Откройте сведения о приложении → Батарея (или расход энергии) и поставьте «Без ограничений» / отключите оптимизацию. Иначе Android убьёт сервис ночью или в фоне."
                )
                HelpStep(
                    number = "4",
                    title = "Автозапуск (Xiaomi, Huawei, Oppo)",
                    body = "В настройках приложения включите автозапуск и снимите ограничения фона. На части оболочек пункт называется «Автозапуск» или «Запуск приложений»."
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOpenAccessibility,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Mint, contentColor = colors.onPrimary)
                ) {
                    Text("Открыть доступность")
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onOpenAppSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Открыть настройки приложения")
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
    isBlockingEnabled: Boolean,
    isPaused: Boolean,
    pauseRemainingMs: Long
) {
    val colors = MaterialTheme.colorScheme
    val (title, subtitle, dotColor) = when {
        isPaused && isBlockingEnabled -> Triple(
            "На паузе",
            "Снова включится через ${formatRemaining(pauseRemainingMs)}",
            colors.error
        )
        isServiceEnabled && isBlockingEnabled -> Triple(
            "Активна",
            "Рекомендации TikTok и YouTube Shorts закрываются",
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
            isBlockingEnabled = true,
            pauseRemainingMs = 0,
            onEnableServiceClick = {},
            onOpenBatterySettingsClick = {},
            onToggleService = {},
            onPauseClick = {},
            onResumeClick = {}
        )
    }
}
