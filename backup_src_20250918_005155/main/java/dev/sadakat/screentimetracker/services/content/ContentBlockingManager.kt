package dev.sadakat.screentimetracker.services.content

import android.content.Context
import android.content.Intent
import android.view.accessibility.AccessibilityNodeInfo
import dev.sadakat.screentimetracker.domain.repository.TrackerRepository
import dev.sadakat.screentimetracker.ui.blocking.ContentBlockingActivity
import dev.sadakat.screentimetracker.utils.logger.AppLogger
import dev.sadakat.screentimetracker.utils.ui.AppNotificationManager
import dev.sadakat.screentimetracker.utils.ui.AppToastManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentBlockingManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: TrackerRepository,
    private val appLogger: AppLogger,
    private val appNotificationManager: AppNotificationManager,
    private val appToastManager: AppToastManager
) {
    companion object {
        private const val TAG = "ContentBlockingManager"

        // Content blocking patterns for different apps
        private val BLOCKED_CONTENT_PATTERNS = mapOf(
            "com.instagram.android" to BlockedContentConfig(
                featureName = "Instagram Reels",
                patterns = listOf(
                    "reels", "reel", "/reels/", "Watch Reels",
                    "com.instagram.reels", "reels_viewer"
                ),
                textPatterns = listOf("reels", "reel"),
                contentDescPatterns = listOf("reels", "reel", "Watch Reels")
            ),
            "com.google.android.youtube" to BlockedContentConfig(
                featureName = "YouTube Shorts",
                patterns = listOf(
                    "shorts", "/shorts/", "Short", "Shorts",
                    "com.google.android.apps.youtube.app.shorts"
                ),
                textPatterns = listOf("shorts", "short"),
                contentDescPatterns = listOf("Shorts", "Short video")
            ),
            "com.tiktok.android" to BlockedContentConfig(
                featureName = "TikTok",
                patterns = listOf(""), // Block entire app
                textPatterns = emptyList(),
                contentDescPatterns = emptyList(),
                blockEntireApp = true
            ),
            "com.facebook.katana" to BlockedContentConfig(
                featureName = "Facebook Reels",
                patterns = listOf(
                    "reels", "reel", "Watch", "facebook.reels",
                    "ReelsFeedFragment", "video_home"
                ),
                textPatterns = listOf("reels", "reel", "watch"),
                contentDescPatterns = listOf("Reels", "Watch")
            )
        )
    }

    data class BlockedContentConfig(
        val featureName: String,
        val patterns: List<String>,
        val textPatterns: List<String>,
        val contentDescPatterns: List<String>,
        val blockEntireApp: Boolean = false
    )

    private var blockedApps: Set<String> = emptySet()

    suspend fun loadBlockedApps() {
        try {
            // Load from repository - you might want to add a table for blocked content features
            // For now, we'll use the apps that have content restrictions
            blockedApps = BLOCKED_CONTENT_PATTERNS.keys.toSet()
            appLogger.d(TAG, "Loaded blocked content apps: ${blockedApps.size}")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to load blocked apps", e)
        }
    }

    fun shouldBlockContent(packageName: String, rootNode: AccessibilityNodeInfo): Boolean {
        val config = BLOCKED_CONTENT_PATTERNS[packageName] ?: return false

        // If entire app is blocked
        if (config.blockEntireApp) {
            return true
        }

        return detectBlockedContent(rootNode, config)
    }

    private fun detectBlockedContent(
        node: AccessibilityNodeInfo,
        config: BlockedContentConfig
    ): Boolean {
        try {
            return checkNodeForBlockedContent(node, config)
        } catch (e: Exception) {
            appLogger.e(TAG, "Error detecting blocked content", e)
            return false
        }
    }

    private fun checkNodeForBlockedContent(
        node: AccessibilityNodeInfo,
        config: BlockedContentConfig
    ): Boolean {
        // Check current node
        if (isNodeBlocked(node, config)) {
            return true
        }

        // Recursively check children
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            if (child != null) {
                if (checkNodeForBlockedContent(child, config)) {
                    child.recycle()
                    return true
                }
                child.recycle()
            }
        }
        return false
    }

    private fun isNodeBlocked(node: AccessibilityNodeInfo, config: BlockedContentConfig): Boolean {
        // Check text content
        node.text?.toString()?.let { text ->
            if (config.textPatterns.any { pattern ->
                text.contains(pattern, ignoreCase = true)
            }) {
                appLogger.d(TAG, "Blocked content found in text: $text")
                return true
            }
        }

        // Check content description
        node.contentDescription?.toString()?.let { desc ->
            if (config.contentDescPatterns.any { pattern ->
                desc.contains(pattern, ignoreCase = true)
            }) {
                appLogger.d(TAG, "Blocked content found in description: $desc")
                return true
            }
        }

        // Check view ID resource name
        node.viewIdResourceName?.let { resourceName ->
            if (config.patterns.any { pattern ->
                resourceName.contains(pattern, ignoreCase = true)
            }) {
                appLogger.d(TAG, "Blocked content found in resource: $resourceName")
                return true
            }
        }

        // Check class name
        node.className?.toString()?.let { className ->
            if (config.patterns.any { pattern ->
                className.contains(pattern, ignoreCase = true)
            }) {
                appLogger.d(TAG, "Blocked content found in class: $className")
                return true
            }
        }

        return false
    }

    fun getBlockedFeatureName(packageName: String, rootNode: AccessibilityNodeInfo): String {
        return BLOCKED_CONTENT_PATTERNS[packageName]?.featureName ?: "Blocked Content"
    }

    fun blockContent(packageName: String, featureName: String) {
        appLogger.i(TAG, "Blocking content: $featureName in $packageName")

        // Show blocking notification
        appNotificationManager.showContentBlockedNotification(featureName)

        // Show blocking toast
        appToastManager.showContentBlockedToast(featureName)

        // Navigate away from the blocked content
        navigateAwayFromBlockedContent(packageName)
    }

    private fun navigateAwayFromBlockedContent(packageName: String) {
        try {
            // Show blocking screen first
            val featureName = BLOCKED_CONTENT_PATTERNS[packageName]?.featureName ?: "Blocked Content"
            val blockingIntent = Intent(context, ContentBlockingActivity::class.java).apply {
                putExtra(ContentBlockingActivity.EXTRA_FEATURE_NAME, featureName)
                putExtra(ContentBlockingActivity.EXTRA_PACKAGE_NAME, packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(blockingIntent)

            appLogger.d(TAG, "Launched blocking screen for $packageName")

        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to launch blocking screen, falling back to home", e)

            // Fallback: Go to home screen
            try {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(homeIntent)
                appLogger.d(TAG, "Navigated to home screen as fallback")
            } catch (fallbackException: Exception) {
                appLogger.e(TAG, "Fallback navigation also failed", fallbackException)
            }
        }
    }

    fun isAppContentBlocked(packageName: String): Boolean {
        return BLOCKED_CONTENT_PATTERNS.containsKey(packageName)
    }

    fun getBlockedContentConfig(packageName: String): BlockedContentConfig? {
        return BLOCKED_CONTENT_PATTERNS[packageName]
    }

    // Method to add custom blocked content patterns (for future extensibility)
    suspend fun addCustomBlockedContent(
        packageName: String,
        featureName: String,
        patterns: List<String>
    ) {
        try {
            // This could be stored in database for persistence
            appLogger.i(TAG, "Added custom blocked content: $featureName for $packageName")
        } catch (e: Exception) {
            appLogger.e(TAG, "Failed to add custom blocked content", e)
        }
    }
}