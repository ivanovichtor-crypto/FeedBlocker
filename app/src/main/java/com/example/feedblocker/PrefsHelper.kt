package com.example.feedblocker

import android.content.Context

/**
 * Хранилище состояния "паузы" в SharedPreferences.
 * Отдельного флага "блокировка включена/выключена" больше нет:
 * защита активна всегда, пока включён сервис доступности,
 * — единственное временное исключение — эта пауза.
 */
object PrefsHelper {

    private const val PREFS_NAME = "feed_blocker_prefs"
    private const val KEY_PAUSE_UNTIL = "pause_until"

    /** Включить паузу на заданное количество миллисекунд от текущего момента. */
    fun setPause(context: Context, durationMillis: Long) {
        val pauseUntil = System.currentTimeMillis() + durationMillis
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().putLong(KEY_PAUSE_UNTIL, pauseUntil).apply()
    }

    fun getPauseUntil(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_PAUSE_UNTIL, 0L)
    }

    fun remainingPauseMillis(context: Context): Long {
        return (getPauseUntil(context) - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun isPaused(context: Context): Boolean {
        return System.currentTimeMillis() < getPauseUntil(context)
    }

    /** Досрочно снять паузу. */
    fun clearPause(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().remove(KEY_PAUSE_UNTIL).apply()
    }
}
