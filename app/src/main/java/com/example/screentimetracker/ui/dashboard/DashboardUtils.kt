package com.example.screentimetracker.ui.dashboard

import androidx.compose.ui.graphics.Color
import com.example.screentimetracker.utils.millisToReadableTime

// Helper to group app usages by category and sum durations, opens, etc.
fun getCategoryDataFromAppUsages(appUsages: List<AppUsageUIModel>): List<CategoryData> {
    // TODO: Replace with real category mapping logic
    val colorPalette = listOf(
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1), Color(0xFF96CEB4), Color(0xFFFFEAA7)
    )
    val grouped = appUsages.groupBy { it.appName } // Replace with real category if available
    return grouped.entries.mapIndexed { idx, entry ->
        val totalDuration = entry.value.sumOf { it.totalDurationMillisToday }
        val totalOpens = entry.value.sumOf { it.openCount }
        CategoryData(
            name = entry.key,
            value = totalOpens,
            color = colorPalette[idx % colorPalette.size],
            time = millisToReadableTime(totalDuration)
        )
    }
}
