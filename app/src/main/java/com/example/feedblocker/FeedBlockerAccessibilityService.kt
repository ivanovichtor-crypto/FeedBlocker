package com.example.feedblocker

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class FeedBlockerAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "FeedBlocker"
        private const val TIKTOK_PACKAGE = "com.zhiliaoapp.musically"
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val MIN_INTERVAL_MS = 800L
        private const val PAUSE_CACHE_MS = 1000L
        private const val SLOW_EVENT_MS = 16L
    }

    private var isBlocking = false
    private var lastBlockTime = 0L
    private var pauseUntilCached = 0L
    private var lastPauseCheckElapsed = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || isBlocking) return

        val type = event.eventType
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            type != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            type != AccessibilityEvent.TYPE_VIEW_SCROLLED
        ) {
            return
        }

        if (isPausedCached()) return

        val now = SystemClock.elapsedRealtime()
        if (now - lastBlockTime < MIN_INTERVAL_MS) return

        val packageName = event.packageName?.toString() ?: return
        if (packageName != TIKTOK_PACKAGE && packageName != YOUTUBE_PACKAGE) return

        val started = if (BuildConfig.DEBUG) SystemClock.elapsedRealtime() else 0L
        val root = rootInActiveWindow ?: return
        try {
            when (packageName) {
                TIKTOK_PACKAGE -> handleTikTok(root)
                YOUTUBE_PACKAGE -> handleYouTube(root)
            }
        } finally {
            if (BuildConfig.DEBUG) {
                val dt = SystemClock.elapsedRealtime() - started
                if (dt > SLOW_EVENT_MS) {
                    Log.w(TAG, "slow a11y ${dt}ms type=$type pkg=$packageName")
                }
            }
        }
    }

    private fun isPausedCached(): Boolean {
        val elapsed = SystemClock.elapsedRealtime()
        if (elapsed - lastPauseCheckElapsed >= PAUSE_CACHE_MS) {
            lastPauseCheckElapsed = elapsed
            pauseUntilCached = PrefsHelper.getPauseUntil(this)
        }
        return System.currentTimeMillis() < pauseUntilCached
    }

    private fun handleTikTok(root: AccessibilityNodeInfo) {
        val recommendationsNodes = root.findAccessibilityNodeInfosByText("Рекомендации")
        val found = recommendationsNodes.isNotEmpty()
        recycleNodes(recommendationsNodes)
        if (!found) return

        Log.d(TAG, "Найдено 'Рекомендации' — переходим в Друзья")
        isBlocking = true
        lastBlockTime = SystemClock.elapsedRealtime()

        val success = switchToFriendsTab(root)
        if (!success) {
            Log.d(TAG, "Не удалось переключиться на Друзья")
            performGlobalAction(GLOBAL_ACTION_BACK)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            isBlocking = false
        }, MIN_INTERVAL_MS)
    }

    private fun switchToFriendsTab(root: AccessibilityNodeInfo): Boolean {
        val friendsTextNodes = root.findAccessibilityNodeInfosByText("Друзья")
        try {
            for (node in friendsTextNodes) {
                if (clickNodeOrClickableParent(node)) return true
            }
        } finally {
            recycleNodes(friendsTextNodes)
        }

        val navNodes = root.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/bottom_navigation")
        try {
            if (navNodes.isNotEmpty()) {
                val navContainer = navNodes[0]
                val friendsIndex = 1
                if (navContainer.childCount > friendsIndex) {
                    val friendsTab = navContainer.getChild(friendsIndex)
                    if (friendsTab != null && clickNodeOrClickableParent(friendsTab)) {
                        return true
                    }
                }
            }
        } finally {
            recycleNodes(navNodes)
        }

        val friendsIdNodes = root.findAccessibilityNodeInfosByViewId("com.zhiliaoapp.musically:id/tab_friends")
        try {
            for (node in friendsIdNodes) {
                if (clickNodeOrClickableParent(node)) return true
            }
        } finally {
            recycleNodes(friendsIdNodes)
        }

        return false
    }

    private fun clickNodeOrClickableParent(node: AccessibilityNodeInfo): Boolean {
        if (node.isClickable) {
            node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
            return true
        }
        var parent = node.parent
        while (parent != null) {
            if (parent.isClickable) {
                parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                return true
            }
            parent = parent.parent
        }
        return false
    }

    private fun handleYouTube(root: AccessibilityNodeInfo) {
        val shortsNodes = root.findAccessibilityNodeInfosByViewId(
            "com.google.android.youtube:id/reel_player_page_container"
        )
        val found = shortsNodes.isNotEmpty()
        recycleNodes(shortsNodes)
        if (!found) return

        Log.d(TAG, "Обнаружены YouTube Shorts — назад")
        performGlobalAction(GLOBAL_ACTION_BACK)
        lastBlockTime = SystemClock.elapsedRealtime()
    }

    @Suppress("DEPRECATION")
    private fun recycleNodes(nodes: List<AccessibilityNodeInfo>) {
        for (node in nodes) {
            try {
                node.recycle()
            } catch (_: Exception) {
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Сервис прерван системой")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        lastPauseCheckElapsed = 0L
        Log.d(TAG, "Сервис запущен и подключён")
    }
}
