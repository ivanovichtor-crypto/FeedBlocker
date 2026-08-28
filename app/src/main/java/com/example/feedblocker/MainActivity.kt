package com.example.feedblocker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.sp
import com.example.feedblocker.ui.theme.FeedBlockerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        NotificationHelper.createNotificationChannel(this)

        setContent {
            FeedBlockerTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.White
                ) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        onEnableServiceClick = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        onOpenBatterySettingsClick = {
                            // Открывает экран "Сведения о приложении",
                            // откуда можно перейти в раздел батареи/автозапуска
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", packageName, null)
                            )
                            startActivity(intent)
                        },
                        onToggleService = { isEnabled ->
                            if (isEnabled) {
                                if (!isAccessibilityServiceEnabled()) {
                                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                                    return@MainScreen
                                }
                                NotificationHelper.showActiveNotification(this)
                            } else {
                                NotificationHelper.dismissNotification(this)
                            }
                        },
                        isServiceEnabled = isAccessibilityServiceEnabled()
                    )
                }
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(this, KeepAliveService::class.java))
        } else {
            startService(Intent(this, KeepAliveService::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        if (isAccessibilityServiceEnabled() && !PrefsHelper.isPaused(this)) {
            NotificationHelper.showActiveNotification(this)
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return service?.contains("com.example.feedblocker/.FeedBlockerAccessibilityService") == true
    }
}

class KeepAliveService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()
        val notification = NotificationCompat.Builder(this, "feed_blocker_channel")
            .setContentTitle("Блокировщик лент работает")
            .setContentText("Сервис активен и защищает вас от лент")
            .setSmallIcon(android.R.drawable.ic_lock_lock) // Замените на свою иконку
            .setOngoing(true) // Уведомление нельзя смахинуть
            .build()

        startForeground(1, notification)
        return START_STICKY // Если систему убьет сервис, Android перезапустит его
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "feed_blocker_channel",
                "Blocking Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

// ============================================================
// ГЛАВНЫЙ ЭКРАН — МИНИМАЛИЗМ (БЕЗ ЛОГОТИПА)
// ============================================================

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    onEnableServiceClick: () -> Unit,
    onOpenBatterySettingsClick: () -> Unit,
    onToggleService: (Boolean) -> Unit,
    isServiceEnabled: Boolean
) {
    var isEnabled by remember { mutableStateOf(isServiceEnabled) }

    LaunchedEffect(isServiceEnabled) {
        isEnabled = isServiceEnabled
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "FeedBlocker",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1C1C1E)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Блокировка лент соцсетей",
                fontSize = 16.sp,
                color = Color(0xFF8E8E93)
            )

            Spacer(modifier = Modifier.height(56.dp))

            Switch(
                checked = isEnabled,
                onCheckedChange = { checked ->
                    isEnabled = checked
                    onToggleService(checked)
                },
                modifier = Modifier
                    .size(80.dp, 48.dp)
                    .clip(RoundedCornerShape(24.dp)),
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF34C759),
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFE5E5EA),
                    checkedBorderColor = Color.Transparent,
                    uncheckedBorderColor = Color.Transparent
                ),
                thumbContent = null
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isEnabled) "Активна" else "Выключено",
                fontSize = 18.sp,
                color = if (isEnabled) Color(0xFF34C759) else Color(0xFF8E8E93),
                fontWeight = if (isEnabled) FontWeight.Medium else FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(56.dp))

            Text(
                text = "Для работы нажмите сюда → скачанные приложения → FeedBlocker → переключите верхний тумблер",
                fontSize = 14.sp,
                color = Color(0xFF007AFF),
                modifier = Modifier.clickable { onEnableServiceClick() }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Новая ссылка на настройки батареи
            Text(
                text = "Отключите ограничения батареи для работы приложения",
                fontSize = 14.sp,
                color = Color(0xFF007AFF),
                modifier = Modifier.clickable { onOpenBatterySettingsClick() }
            )
        }
    }
}