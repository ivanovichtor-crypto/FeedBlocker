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
    fun isPaused(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val pauseUntil = prefs.getLong(KEY_PAUSE_UNTIL, 0L)
        return System.currentTimeMillis() < pauseUntil
    }

    /**
     * Досрочно снять паузу (если понадобится).
     */
    fun clearPause(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_PAUSE_UNTIL).apply()
    }
}