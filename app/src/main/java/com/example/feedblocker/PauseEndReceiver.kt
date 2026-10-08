package com.example.feedblocker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * Планирует и обрабатывает автоматическое окончание паузы.
 *
 * Раньше паузу можно было снять вручную кнопкой «Снять паузу» — теперь её
 * нет (см. PauseController), поэтому, когда 5 минут истекли, система сама
 * должна вернуть уведомление в «FeedBlocker активен». Делаем это через
 * AlarmManager — он переживает и закрытие приложения, и выгрузку сервиса
 * из памяти.
 *
 * Используем нестрогий будильник (setAndAllowWhileIdle) — точность в пару
 * минут для этой задачи не критична, а специальное разрешение
 * SCHEDULE_EXACT_ALARM не требуется.
 */
object PauseEndScheduler {

    private const val REQUEST_CODE = 1001
    private const val ACTION = "com.example.feedblocker.PAUSE_END"

    fun schedule(context: Context) {
        val triggerAt = PrefsHelper.getPauseUntil(context)
        val pendingIntent = buildPendingIntent(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }

    private fun buildPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, PauseEndReceiver::class.java).setAction(ACTION)
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

/** Срабатывает, когда истекли 5 минут паузы. */
class PauseEndReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (PrefsHelper.isPaused(context)) return // пауза всё ещё активна — ничего не делаем

        if (AccessibilityStatus.isEnabled(context)) {
            NotificationHelper.createNotificationChannel(context)
            NotificationHelper.showActiveNotification(context)
        } else {
            NotificationHelper.dismissNotification(context)
        }
    }
}
