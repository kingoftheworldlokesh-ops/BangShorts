package com.bangshorts

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.os.SystemClock

/**
 * Allows the first Shorts screen and backs out on the next Shorts swipe.
 * YouTube's accessibility labels are not a public API, therefore detection
 * intentionally accepts several labels and may need maintenance over time.
 */
class ShortsAccessibilityService : AccessibilityService() {
    private var firstShortAllowed = false
    private var inShorts = false
    private var lastOutsideShortsAt = 0L
    private var lastBackAt = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = serviceInfo.apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED or
                AccessibilityEvent.TYPE_VIEW_SCROLLED
            packageNames = arrayOf(YOUTUBE_PACKAGE)
            notificationTimeout = 100L
            flags = flags or AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        if (event.packageName?.toString() != YOUTUBE_PACKAGE) return

        val root = rootInActiveWindow ?: return
        val shortsNow = containsShortsMarker(root)
        val now = SystemClock.uptimeMillis()

        if (!shortsNow) {
            if (inShorts) lastOutsideShortsAt = now
            inShorts = false
            // Leaving Shorts starts a new allowance. The delay avoids resetting
            // during the brief tree transition caused by a swipe.
            if (now - lastOutsideShortsAt > RESET_DELAY_MS) firstShortAllowed = false
            return
        }

        if (!inShorts) {
            inShorts = true
            if (!firstShortAllowed) {
                firstShortAllowed = true
                return
            }
        }

        // A scroll while already in Shorts represents moving to another Short.
        if (event.eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED &&
            now - lastBackAt > BACK_COOLDOWN_MS) {
            lastBackAt = now
            performGlobalAction(GLOBAL_ACTION_BACK)
        }
    }

    private fun containsShortsMarker(node: AccessibilityNodeInfo): Boolean {
        val text = node.text?.toString()?.trim()?.lowercase()
        val description = node.contentDescription?.toString()?.trim()?.lowercase()
        if (text == "shorts" || description == "shorts") return true
        for (index in 0 until node.childCount) {
            node.getChild(index)?.let { child ->
                val found = containsShortsMarker(child)
                child.recycle()
                if (found) return true
            }
        }
        return false
    }

    override fun onInterrupt() = Unit

    companion object {
        private const val YOUTUBE_PACKAGE = "com.google.android.youtube"
        private const val RESET_DELAY_MS = 1_500L
        private const val BACK_COOLDOWN_MS = 1_000L
    }
}
