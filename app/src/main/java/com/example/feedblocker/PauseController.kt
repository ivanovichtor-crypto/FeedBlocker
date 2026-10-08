package com.example.feedblocker

import android.content.Context

/**
 * Единая точка «включить ленту на 5 минут».
 *
 * Вызывается и с главного экрана (кнопка «Включить ленту на 5 минут»),
 * и из уведомления (действие «Пауза на 5 минут»). Одним действием:
 *  1) ставит паузу блокировки на 5 минут;
 *  2) добавляет ровно 5 минут к счётчику экранного времени
 *     (ScreenTimeCounter) — так счётчик «оптимизируется»: одно нажатие
 *     = +5 минут во все периоды;
 *  3) планирует автоматический возврат уведомления в «FeedBlocker
 *     активен», когда пауза закончится (кнопки «Снять паузу» больше нет).
 */
object PauseController {

    /** Длительность паузы — ровно 5 минут. */
    const val DURATION_MS: Long = 5 * 60 * 1000L

    fun start(context: Context) {
        PrefsHelper.setPause(context, DURATION_MS)
        ScreenTimeCounter.add(context, DURATION_MS)
        PauseEndScheduler.schedule(context)
        NotificationHelper.showPausedNotification(context)
    }

    /**
     * Перепланировать авто-возврат уведомления (например, после
     * перезагрузки устройства, когда AlarmManager-будильники сбрасываются).
     */
    fun reschedule(context: Context) {
        PauseEndScheduler.schedule(context)
    }
}
