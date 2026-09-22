package com.example.feedblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != Intent.ACTION_LOCKED_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) {
            return
        }

        Log.d("FeedBlocker", "Загрузка устройства завершена")

        // Важно: тут НЕ нужно запускать MainActivity.
        // 1) Если AccessibilityService уже была включена пользователем
        //    в системных настройках раньше, система сама подключит её
        //    заново после перезагрузки — приложению для этого ничего
        //    делать не нужно.
        // 2) Пауза (pause_until) хранится в SharedPreferences и переживает
        //    перезагрузку сама по себе.
        // Открывать интерфейс приложения поверх экрана пользователя сразу
        // после загрузки телефона — плохой UX, а на Android 10+ система
        // и так почти наверняка блокирует такой запуск Activity в фоне.
        //
        // Единственное, что реально "не переживает" перезагрузку — это
        // постоянное уведомление о работе блокировки (оно обычное, а не
        // от foreground-сервиса). Тихо восстанавливаем его здесь, если
        // защита должна быть активна (сервис включён и нет паузы).
        if (!PrefsHelper.isPaused(context) && AccessibilityStatus.isEnabled(context)) {
            NotificationHelper.createNotificationChannel(context)
            NotificationHelper.showActiveNotification(context)
        }
    }
}
