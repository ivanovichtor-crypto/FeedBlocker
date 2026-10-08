package com.example.feedblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Ловит нажатие кнопки «Пауза на 5 минут» в уведомлении.
 * Логика единая с кнопкой на главном экране — PauseController.start:
 * пауза на 5 минут + 5 минут в счётчик экранного времени одним действием.
 */
class PauseNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        PauseController.start(context)
    }
}
