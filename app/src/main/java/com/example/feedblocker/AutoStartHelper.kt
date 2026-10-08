package com.example.feedblocker

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build

/**
 * У чистого Android нет единого системного экрана "Автозапуск" —
 * у каждого производителя он свой, в отдельном системном приложении.
 * Здесь — известные экраны для основных вендоров; если ни один не
 * нашёлся (или это "чистый" Android/Pixel), просто открываем страницу
 * приложения в настройках как разумный запасной вариант.
 */
object AutoStartHelper {

    /** Пытается открыть экран управления автозапуском. true — если удалось. */
    fun open(context: Context): Boolean {
        for ((pkg, activity) in KNOWN_SCREENS) {
            try {
                val intent = Intent().apply {
                    component = android.content.ComponentName(pkg, activity)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
                return true
            } catch (_: ActivityNotFoundException) {
                // У этого производителя другой пакет/версия — пробуем следующий.
            } catch (_: Exception) {
                // Экран есть, но открыть не вышло (например, требует другие extra) — пробуем дальше.
            }
        }
        return false
    }

    /** Короткая подсказка, актуальная для текущего производителя устройства. */
    fun hintForCurrentDevice(): String {
        val manufacturer = Build.MANUFACTURER.lowercase()
        return when {
            manufacturer.contains("xiaomi") ->
                "На MIUI/HyperOS: Настройки → Приложения → Управление приложениями → FeedBlocker → Автозапуск — включите."
            manufacturer.contains("huawei") || manufacturer.contains("honor") ->
                "На EMUI/MagicOS: Диспетчер телефона → Автозапуск приложений → FeedBlocker — включите вручную и разрешите «Автозапуск», «Дополнительный запуск» и «Работу в фоне»."
            manufacturer.contains("oppo") || manufacturer.contains("realme") || manufacturer.contains("oneplus") ->
                "На ColorOS/OxygenOS: Настройки → Батарея → Автозапуск приложений → FeedBlocker — включите."
            manufacturer.contains("vivo") ->
                "На Funtouch/OriginOS: i Управление → Автозапуск → FeedBlocker — включите."
            manufacturer.contains("meizu") ->
                "На Flyme: Настройки → Приложения → Автозапуск → FeedBlocker — включите."
            manufacturer.contains("samsung") ->
                "На One UI отдельного экрана автозапуска нет: откройте настройки приложения → Батарея → выберите «Без ограничений», чтобы систему не выгружала FeedBlocker из фона."
            else ->
                "Если после перезагрузки телефона блокировка перестаёт работать — поищите в настройках телефона пункт «Автозапуск» или «Диспетчер приложений» и разрешите FeedBlocker запускаться в фоне."
        }
    }

    private val KNOWN_SCREENS = listOf(
        // Xiaomi / MIUI / HyperOS
        "com.miui.securitycenter" to "com.miui.permcenter.autostart.AutoStartManagementActivity",
        // Huawei
        "com.huawei.systemmanager" to "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
        "com.huawei.systemmanager" to "com.huawei.systemmanager.optimize.process.ProtectActivity",
        // Honor (после разделения с Huawei)
        "com.hihonor.systemmanager" to "com.hihonor.systemmanager.startupmgr.ui.StartupNormalAppListActivity",
        // Oppo / Realme / OnePlus (ColorOS)
        "com.coloros.safecenter" to "com.coloros.safecenter.permission.startup.StartupAppListActivity",
        "com.coloros.safecenter" to "com.coloros.safecenter.startupapp.StartupAppListActivity",
        "com.oppo.safe" to "com.oppo.safe.permission.startup.StartupAppListActivity",
        // Vivo / iQOO
        "com.vivo.permissionmanager" to "com.vivo.permissionmanager.activity.BgStartUpManagerActivity",
        "com.iqoo.secure" to "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity",
        // Meizu
        "com.meizu.safe" to "com.meizu.safe.permission.SmartBGActivity",
        // Asus
        "com.asus.mobilemanager" to "com.asus.mobilemanager.autostart.AutoStartActivity",
        // Letv
        "com.letv.android.letvsafe" to "com.letv.android.letvsafe.AutobootManageActivity"
    )
}
