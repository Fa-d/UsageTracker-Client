package dev.sadakat.screentimetracker.core.presentation.temp

import dev.sadakat.screentimetracker.core.domain.categorization.AppCategorizer
import dev.sadakat.screentimetracker.core.domain.categorization.DomainAppCategories
import dev.sadakat.screentimetracker.core.domain.service.AppInfo
import dev.sadakat.screentimetracker.core.domain.service.CategorySource
import dev.sadakat.screentimetracker.core.domain.error.DomainResult

/**
 * Temporary implementation of AppCategorizer for Phase 1.
 * This will be replaced with proper infrastructure implementation in Phase 5.
 */
class TempAppCategorizer : AppCategorizer {

    // Known apps mapping (simplified for temporary use)
    private val knownApps = mapOf(
        // Social
        "com.instagram.android" to DomainAppCategories.SOCIAL,
        "com.facebook.katana" to DomainAppCategories.SOCIAL,
        "com.twitter.android" to DomainAppCategories.SOCIAL,
        "com.snapchat.android" to DomainAppCategories.SOCIAL,
        "com.linkedin.android" to DomainAppCategories.SOCIAL,
        "com.reddit.frontpage" to DomainAppCategories.SOCIAL,

        // Entertainment
        "com.netflix.mediaclient" to DomainAppCategories.ENTERTAINMENT,
        "com.youtube.android" to DomainAppCategories.ENTERTAINMENT,
        "com.spotify.music" to DomainAppCategories.ENTERTAINMENT,
        "com.disney.disneyplus" to DomainAppCategories.ENTERTAINMENT,

        // Productivity
        "com.microsoft.office.word" to DomainAppCategories.PRODUCTIVITY,
        "com.google.android.apps.docs.editors.docs" to DomainAppCategories.PRODUCTIVITY,
        "com.slack" to DomainAppCategories.PRODUCTIVITY,
        "com.microsoft.teams" to DomainAppCategories.PRODUCTIVITY,

        // Communication
        "com.whatsapp" to DomainAppCategories.COMMUNICATION,
        "com.android.mms" to DomainAppCategories.COMMUNICATION,
        "com.google.android.apps.messaging" to DomainAppCategories.COMMUNICATION,
        "com.discord" to DomainAppCategories.COMMUNICATION,

        // Games
        "com.android.games" to DomainAppCategories.GAMES,
        "com.supercell.clashofclans" to DomainAppCategories.GAMES,
        "com.king.candycrushsaga" to DomainAppCategories.GAMES,

        // Finance
        "com.paypal.android.p2pmobile" to DomainAppCategories.FINANCE,
        "com.venmo" to DomainAppCategories.FINANCE,
        "com.chase.sig.android" to DomainAppCategories.FINANCE,

        // Health
        "com.myfitnesspal.android" to DomainAppCategories.HEALTH,
        "com.fitbit.FitbitMobile" to DomainAppCategories.HEALTH,
        "com.google.android.apps.fitness" to DomainAppCategories.HEALTH
    )

    override suspend fun categorizeApp(packageName: String): DomainResult<String> {
        return try {
            // 1. Use known mappings
            knownApps[packageName]?.let { category ->
                return DomainResult.success(category)
            }

            // 2. Pattern matching on package name
            val category = categorizeByPattern(packageName) ?: DomainAppCategories.OTHER
            DomainResult.success(category)
        } catch (e: Exception) {
            DomainResult.failure(
                dev.sadakat.screentimetracker.core.domain.error.DomainError.SystemError(
                    "App categorization failed",
                    e.message ?: "Unknown error"
                )
            )
        }
    }

    override suspend fun updateCategoryManually(packageName: String, category: String): DomainResult<Unit> {
        // For temp implementation, just return success
        return DomainResult.success(Unit)
    }

    override suspend fun getCategoryStats(): DomainResult<Map<String, Int>> {
        // Return some mock stats for now
        val stats = mapOf(
            DomainAppCategories.SOCIAL to 5,
            DomainAppCategories.ENTERTAINMENT to 3,
            DomainAppCategories.PRODUCTIVITY to 7,
            DomainAppCategories.COMMUNICATION to 4,
            DomainAppCategories.GAMES to 2,
            DomainAppCategories.OTHER to 10
        )
        return DomainResult.success(stats)
    }

    override suspend fun cleanStaleCache(): DomainResult<Unit> {
        // For temp implementation, just return success
        return DomainResult.success(Unit)
    }

    override suspend fun getAppInfo(packageName: String): DomainResult<AppInfo> {
        val category = when (val result = categorizeApp(packageName)) {
            is DomainResult.Success -> result.data
            is DomainResult.Failure -> DomainAppCategories.OTHER
        }

        val appInfo = AppInfo(
            packageName = packageName,
            appName = packageName.split(".").lastOrNull() ?: packageName,
            category = category,
            confidence = if (knownApps.containsKey(packageName)) 1.0f else 0.6f,
            source = if (knownApps.containsKey(packageName)) CategorySource.KNOWN else CategorySource.PATTERN
        )

        return DomainResult.success(appInfo)
    }

    override suspend fun categorizeApps(packageNames: List<String>): DomainResult<Map<String, String>> {
        val results = mutableMapOf<String, String>()
        packageNames.forEach { packageName ->
            when (val result = categorizeApp(packageName)) {
                is DomainResult.Success -> results[packageName] = result.data
                is DomainResult.Failure -> results[packageName] = DomainAppCategories.OTHER
            }
        }
        return DomainResult.success(results)
    }

    private fun categorizeByPattern(packageName: String): String? {
        val lowercasePackage = packageName.lowercase()

        return when {
            // Games patterns
            lowercasePackage.contains("game") ||
            lowercasePackage.contains("play") ||
            lowercasePackage.contains("puzzle") -> DomainAppCategories.GAMES

            // Social patterns
            lowercasePackage.contains("social") ||
            lowercasePackage.contains("chat") ||
            lowercasePackage.contains("dating") -> DomainAppCategories.SOCIAL

            // Entertainment patterns
            lowercasePackage.contains("music") ||
            lowercasePackage.contains("video") ||
            lowercasePackage.contains("stream") ||
            lowercasePackage.contains("media") -> DomainAppCategories.ENTERTAINMENT

            // Finance patterns
            lowercasePackage.contains("bank") ||
            lowercasePackage.contains("pay") ||
            lowercasePackage.contains("wallet") ||
            lowercasePackage.contains("finance") -> DomainAppCategories.FINANCE

            // Health patterns
            lowercasePackage.contains("health") ||
            lowercasePackage.contains("fitness") ||
            lowercasePackage.contains("medical") -> DomainAppCategories.HEALTH

            // Communication patterns
            lowercasePackage.contains("message") ||
            lowercasePackage.contains("mail") ||
            lowercasePackage.contains("sms") -> DomainAppCategories.COMMUNICATION

            // Shopping patterns
            lowercasePackage.contains("shop") ||
            lowercasePackage.contains("store") ||
            lowercasePackage.contains("buy") -> DomainAppCategories.SHOPPING

            else -> null
        }
    }
}