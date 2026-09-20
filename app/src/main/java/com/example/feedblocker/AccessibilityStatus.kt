package com.example.feedblocker

import android.content.Context
import android.provider.Settings

/**
 * Проверка, включена ли наша AccessibilityService в системных настройках.
 * Раньше эта же логика была продублирована в MainActivity и требовалась
 * ещё и в BootReceiver — вынесена сюда, чтобы не разъезжаться в двух местах.
 */
object AccessibilityStatus {
    fun isEnabled(context: Context): Boolean {
        val service = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return service?.contains("${context.packageName}/.FeedBlockerAccessibilityService") == true
    }
}
