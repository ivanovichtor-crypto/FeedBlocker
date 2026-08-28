package com.example.feedblocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON") {

            Log.d("FeedBlocker", "📱 Телефон загружен, запускаем приложение")

            // Запускаем MainActivity, чтобы приложение и его настройки "поднялись"
            val i = Intent(context, MainActivity::class.java)
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(i)
        }
    }
}