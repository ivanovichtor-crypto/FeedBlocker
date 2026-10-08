package com.example.feedblocker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.feedblocker.ui.MainScreen
import com.example.feedblocker.ui.theme.FeedBlockerTheme
import kotlinx.coroutines.delay

/**
 * Ручного вкл/выкл больше нет: защита работает всегда, пока включён
 * сервис доступности. Единственное временное исключение — пауза на
 * 5 минут, которая запускается одним действием (PauseController.start):
 * ставит паузу и добавляет ровно 5 минут к счётчику экранного времени.
 * Снять паузу вручную нельзя — она заканчивается сама.
 */
class MainActivity : ComponentActivity() {

    private var serviceEnabled by mutableStateOf(false)
    private var pauseRemainingMs by mutableLongStateOf(0L)
    private var screenTime by mutableStateOf(
        ScreenTimeCounter.Snapshot(dayMillis = 0L, weekMillis = 0L, monthMillis = 0L, yearMillis = 0L)
    )

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
                        // Чтение снимка заодно применяет строгие сбросы
                        // периодов: ровно в 0:00 ночью — день, в ночь
                        // вс→пн — неделя, 1-го числа — месяц, 1 января — год.
                        screenTime = ScreenTimeCounter.snapshot(this@MainActivity)
                        delay(1000)
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        modifier = Modifier.padding(innerPadding),
                        pauseRemainingMs = pauseRemainingMs,
                        screenTime = screenTime,
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
                        onOpenAutoStartClick = {
                            if (!AutoStartHelper.open(this)) {
                                startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", packageName, null)
                                    )
                                )
                            }
                        },
                        onPauseClick = {
                            // Одно действие: пауза на 5 минут + 5 минут
                            // в счётчик экранного времени + авто-возврат
                            // уведомления по окончании паузы.
                            PauseController.start(this)
                            pauseRemainingMs = PrefsHelper.remainingPauseMillis(this)
                            screenTime = ScreenTimeCounter.snapshot(this)
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
        if (serviceEnabled && !PrefsHelper.isPaused(this)) {
            NotificationHelper.showActiveNotification(this)
        } else if (!serviceEnabled) {
            NotificationHelper.dismissNotification(this)
        }
    }

    private fun refreshStatus() {
        serviceEnabled = AccessibilityStatus.isEnabled(this)
        pauseRemainingMs = PrefsHelper.remainingPauseMillis(this)
        screenTime = ScreenTimeCounter.snapshot(this)
    }
}
