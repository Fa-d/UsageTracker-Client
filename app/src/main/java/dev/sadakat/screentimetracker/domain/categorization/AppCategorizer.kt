package dev.sadakat.screentimetracker.domain.categorization

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppCategorizer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun categorizeApp(packageName: String): String {
        // Simplified categorization logic
        return when {
            packageName.contains("facebook") || packageName.contains("twitter") || packageName.contains("instagram") -> "Social"
            packageName.contains("youtube") || packageName.contains("netflix") || packageName.contains("spotify") -> "Entertainment"
            packageName.contains("email") || packageName.contains("message") || packageName.contains("whatsapp") -> "Communication"
            packageName.contains("game") -> "Games"
            else -> "Other"
        }
    }
}