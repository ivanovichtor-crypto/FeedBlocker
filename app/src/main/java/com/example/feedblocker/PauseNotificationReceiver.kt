package com.example.feedblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * BroadcastReceiver — это "слушатель" системных или наших
 * собственных событий. Здесь он ловит нажатие кнопки "Пауза"
 * в уведомлении.
 */
class PauseNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // Включаем паузу на 5 минут (5 * 60 * 1000 миллисекунд)
        PrefsHelper.setPause(context, 5 * 60 * 1000L)

        // Обновляем уведомление, чтобы показать, что пауза активна
        NotificationHelper.showPausedNotification(context)
    }
}