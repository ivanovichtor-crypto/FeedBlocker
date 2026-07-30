package com.example.feedblocker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationHelper {

    private const val CHANNEL_ID = "feed_blocker_channel"
    private const val NOTIFICATION_ID = 1

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "FeedBlocker",
                NotificationManager.IMPORTANCE_LOW
            )
            channel.description = "Уведомление о статусе блокировки ленты"

            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun showActiveNotification(context: Context) {
        val pauseIntent = Intent(context, PauseNotificationReceiver::class.java)
        val pausePendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("FeedBlocker активен")
            .setContentText("Лента рекомендаций блокируется")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .addAction(0, "Пауза на 5 минут", pausePendingIntent)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    fun showPausedNotification(context: Context) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("FeedBlocker на паузе")
            .setContentText("Блокировка отключена на 5 минут")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, notification)
    }

    // ============================================================
    // НОВЫЙ МЕТОД — скрывает уведомление
    // ============================================================

    fun dismissNotification(context: Context) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(NOTIFICATION_ID)
    }
}