package com.example.feedblocker

import android.content.Context

/**
 * Простое хранилище состояния "паузы".
 * SharedPreferences — это встроенный в Android механизм
 * хранения маленьких настроек (ключ-значение) на диске телефона.
 */
object PrefsHelper {

    private const val PREFS_NAME = "feed_blocker_prefs"
    private const val KEY_PAUSE_UNTIL = "pause_until"

    /**
     * Включить паузу на заданное количество миллисекунд от текущего момента.
     */
    fun setPause(context: Context, durationMillis: Long) {
        val pauseUntil = System.currentTimeMillis() + durationMillis
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putLong(KEY_PAUSE_UNTIL, pauseUntil).apply()
    }

    /**
     * Проверить, активна ли пауза прямо сейчас.
     */
    fun getPauseUntil(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getLong(KEY_PAUSE_UNTIL, 0L)
    }

    fun remainingPauseMillis(context: Context): Long {
        return (getPauseUntil(context) - System.currentTimeMillis()).coerceAtLeast(0L)
    }

    fun isPaused(context: Context): Boolean {
        return System.currentTimeMillis() < getPauseUntil(context)
    }

    /**
     * Досрочно снять паузу (если понадобится).
     */
    fun clearPause(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PAUSE_UNTIL).apply()
    }
}