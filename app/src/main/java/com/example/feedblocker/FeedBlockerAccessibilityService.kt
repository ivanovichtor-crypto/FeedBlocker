package com.example.feedblocker

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class FeedBlockerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "FeedBlocker"
        private const val TIKTOK_PACKAGE = "com.zhiliaoapp.musically"
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val MIN_INTERVAL_MS = 1000L
    }

    private var isBlocking = false
    private var lastBlockTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (PrefsHelper.isPaused(this)) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        if (packageName != TIKTOK_PACKAGE && packageName != YOUTUBE_PACKAGE) {
            return
        }

        val root = rootInActiveWindow ?: return

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBlockTime < MIN_INTERVAL_MS) {
            return
        }

        when (packageName) {
            TIKTOK_PACKAGE -> handleTikTok(root)
            YOUTUBE_PACKAGE -> handleYouTube(root)
        }
    }

    // ============================================================
    // БЛОКИРОВКА TIKTOK — ТОЛЬКО ПО СЛОВУ "Рекомендации"
    // ============================================================

    private fun handleTikTok(root: AccessibilityNodeInfo) {
        val recommendationsNodes = root.findAccessibilityNodeInfosByText("Рекомендации")

        if (recommendationsNodes.isNotEmpty() && !isBlocking) {
            Log.d(TAG, "🔴 Найдено 'Рекомендации' — переходим в Друзья")
            isBlocking = true
            lastBlockTime = System.currentTimeMillis()

            val success = switchToFriendsTab(root)

            if (!success) {
                Log.d(TAG, "⚠️ Не удалось переключиться на Друзья")
                performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            }

            Handler(Looper.getMainLooper()).postDelayed({
                isBlocking = false
            }, MIN_INTERVAL_MS)
        }
    }

    // ============================================================
    // ПЕРЕКЛЮЧЕНИЕ НА ВКЛАДКУ "ДРУЗЬЯ" (по поиску)
    // ============================================================

    private fun switchToFriendsTab(root: AccessibilityNodeInfo): Boolean {
        // Способ 1: Ищем по тексту "Друзья" и кликаем по кликабельному родителю
        val friendsTextNodes = root.findAccessibilityNodeInfosByText("Друзья")
        if (friendsTextNodes.isNotEmpty()) {
            Log.d(TAG, "✅ Найдено 'Друзья' по тексту")
            for (node in friendsTextNodes) {
                // Пытаемся найти кликабельного родителя
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        Log.d(TAG, "✅ Кликаем по родителю 'Друзья'")
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        return true
                    }
                    parent = parent.parent
                }

                // Если родитель не кликабельный — пробуем сам узел
                if (node.isClickable) {
                    Log.d(TAG, "✅ Кликаем по 'Друзья'")
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    return true
                }
            }
        }

        // Способ 2: Ищем по индексу 1 в нижней навигации
        val navNodes = root.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/bottom_navigation")
        if (navNodes.isNotEmpty()) {
            Log.d(TAG, "✅ Найдена нижняя навигация")
            val navContainer = navNodes[0]

            // Друзья на позиции 1 (0-Главная, 1-Друзья, 2-Создать, 3-Входящие, 4-Профиль)
            val friendsIndex = 1
            if (navContainer.childCount > friendsIndex) {
                val friendsTab = navContainer.getChild(friendsIndex)
                if (friendsTab != null) {
                    Log.d(TAG, "✅ Найдена вкладка Друзья по индексу $friendsIndex")

                    if (friendsTab.isClickable) {
                        friendsTab.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Log.d(TAG, "✅ Кликнули по вкладке Друзья")
                        return true
                    }

                    var parent = friendsTab.parent
                    while (parent != null) {
                        if (parent.isClickable) {
                            parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                            Log.d(TAG, "✅ Кликнули по родителю вкладки Друзья")
                            return true
                        }
                        parent = parent.parent
                    }
                }
            }
        }

        // Способ 3: Ищем по ID tab_friends (если есть)
        val friendsIdNodes = root.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/tab_friends")
        if (friendsIdNodes.isNotEmpty()) {
            Log.d(TAG, "✅ Найден tab_friends")
            for (node in friendsIdNodes) {
                if (node.isClickable) {
                    node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Log.d(TAG, "✅ Кликнули по tab_friends")
                    return true
                }
                var parent = node.parent
                while (parent != null) {
                    if (parent.isClickable) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                        Log.d(TAG, "✅ Кликнули по родителю tab_friends")
                        return true
                    }
                    parent = parent.parent
                }
            }
        }

        return false
    }

    // ============================================================
    // БЛОКИРОВКА YOUTUBE SHORTS
    // ============================================================

    private fun handleYouTube(root: AccessibilityNodeInfo) {
        val shortsNodes = root.findAccessibilityNodeInfosByViewId("com.google.android.youtube:id/reel_player_page_container")

        if (shortsNodes.isNotEmpty()) {
            Log.d(TAG, "🔴 Обнаружены YouTube Shorts — возвращаем на главную")
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK)
            lastBlockTime = System.currentTimeMillis()
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Сервис прерван системой")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "✅ Сервис запущен и подключён")
    }
}