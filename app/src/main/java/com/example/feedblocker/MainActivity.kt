package com.example.feedblocker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.app.NotificationCompat
import com.example.feedblocker.ui.MainScreen
import com.example.feedblocker.ui.theme.FeedBlockerTheme
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private var serviceEnabled by mutableStateOf(false)
    private var blockingEnabled by mutableStateOf(true)
    private var pauseRemainingMs by mutableLongStateOf(0L)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)
        refreshStatus()

        setContent {
            FeedBlockerTheme {
                LaunchedEffect(Unit) {
                    while (true) {
                        pauseRemainingMs = PrefsHelper.remainingPauseMillis(this@MainActivity)
                        delay(1000)
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        isServiceEnabled = serviceEnabled,
                        isBlockingEnabled = blockingEnabled,
                        pauseRemainingMs = pauseRemainingMs,
                        onEnableServiceClick = {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                        },
                        onOpenBatterySettingsClick = {
                            startActivity(
                                Intent(
                                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", packageName, null)
                                )
                            )
                        },
                        onToggleService = { enabled ->
                            // Никакого автоматического перехода в системные настройки.
                            // Тумблер только сохраняет желаемое состояние блокировки.
                            // Если сервис доступности ещё не включён, статус-строка
                            // и кнопка "i" на главном экране подскажут это пользователю,
                            // а перейти в настройки можно только через отдельные кнопки.
                            if (enabled) {
                                PrefsHelper.setBlockingEnabled(this, true)
                                PrefsHelper.clearPause(this)
                                blockingEnabled = true
                                pauseRemainingMs = 0L
                                if (isAccessibilityServiceEnabled()) {
                                    NotificationHelper.showActiveNotification(this)
                                }
                            } else {
                                PrefsHelper.setBlockingEnabled(this, false)
                                PrefsHelper.clearPause(this)
                                blockingEnabled = false
                                pauseRemainingMs = 0L
                                NotificationHelper.dismissNotification(this)
                            }
                        },
                        onPauseClick = {
                            PrefsHelper.setPause(this, 5 * 60 * 1000L)
                            pauseRemainingMs = PrefsHelper.remainingPauseMillis(this)
                            NotificationHelper.showPausedNotification(this)
                        },
                        onResumeClick = {
                            PrefsHelper.setBlockingEnabled(this, true)
                            PrefsHelper.clearPause(this)
                            blockingEnabled = true
                            pauseRemainingMs = 0L
                            if (isAccessibilityServiceEnabled()) {
                                NotificationHelper.showActiveNotification(this)
                            }
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
        if (serviceEnabled && blockingEnabled && !PrefsHelper.isPaused(this)) {
            NotificationHelper.showActiveNotification(this)
        } else if (!blockingEnabled) {
            NotificationHelper.dismissNotification(this)
        }
    }

    private fun refreshStatus() {
        serviceEnabled = isAccessibilityServiceEnabled()
        blockingEnabled = PrefsHelper.isBlockingEnabled(this)
        pauseRemainingMs = PrefsHelper.remainingPauseMillis(this)
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return service?.contains("$packageName/.FeedBlockerAccessibilityService") == true
    }
}

class KeepAliveService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            val channel = NotificationChannel(
            "feed_blocker_channel",
            "Blocking Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        val notification = NotificationCompat.Builder(this, "feed_blocker_channel")
            .setContentTitle("Блокировщик лент работает")
            .setContentText("Сервис активен и защищает вас от лент")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                1,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(1, notification)
        }
        return START_STICKY
    }
}
