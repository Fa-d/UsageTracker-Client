package dev.sadakat.screentimetracker.core.presentation.ui.dashboard.utils

import androidx.compose.ui.graphics.Color
import dev.sadakat.screentimetracker.core.presentation.ui.dashboard.state.AppUsageUIModel
import dev.sadakat.screentimetracker.core.presentation.ui.dashboard.state.CategoryData
import dev.sadakat.screentimetracker.utils.millisToReadableTime

// Helper to group app usages by category and sum durations, opens, etc.
fun getCategoryDataFromAppUsages(appUsages: List<AppUsageUIModel>): List<CategoryData> {
    val colorPalette = listOf(
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1), Color(0xFF96CEB4), Color(0xFFFFEAA7),
        Color(0xFFF06292), Color(0xFFBA68C8), Color(0xFF9575CD), Color(0xFF7986CB), Color(0xFF64B5F6),
        Color(0xFF4FC3F7), Color(0xFF4DD0E1), Color(0xFF4DB6AC), Color(0xFF81C784), Color(0xFFAED581),
        Color(0xFFFF8A65)
    )
    
    // Group by packageName to avoid duplicates and get proper app data
    val grouped = appUsages.groupBy { it.packageName }
    return grouped.entries.mapIndexed { idx, entry ->
        val apps = entry.value
        val totalDuration = apps.sumOf { it.totalDurationMillisToday }
        val totalOpens = apps.sumOf { it.openCount }
        // Use the first app's name as the display name (they should all be the same for same package)
        val displayName = apps.first().appName.takeIf { it.isNotBlank() } ?: entry.key
        
        CategoryData(
            name = displayName,
            packageName = entry.key, // Store package name for filtering
            value = totalDuration, // Use duration instead of opens for pie chart value
            color = colorPalette[idx % colorPalette.size],
            time = millisToReadableTime(totalDuration),
            openCount = totalOpens
        )
    }.sortedByDescending { it.value } // Sort by duration descending
}
