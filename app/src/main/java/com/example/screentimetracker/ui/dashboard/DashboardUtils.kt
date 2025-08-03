package com.example.screentimetracker.ui.dashboard

import androidx.compose.ui.graphics.Color
import com.example.screentimetracker.utils.millisToReadableTime

// Helper to group app usages by category and sum durations, opens, etc.
fun getCategoryDataFromAppUsages(appUsages: List<AppUsageUIModel>): List<CategoryData> {
    // TODO: Replace with real category mapping logic
    val colorPalette = listOf(
        Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFF45B7D1), Color(0xFF96CEB4), Color(0xFFFFEAA7),
        Color(0xFFF06292), Color(0xFFBA68C8), Color(0xFF9575CD), Color(0xFF7986CB), Color(0xFF64B5F6),
        Color(0xFF4FC3F7), Color(0xFF4DD0E1), Color(0xFF4DB6AC), Color(0xFF81C784), Color(0xFFAED581),
        Color(0xFFFF8A65)
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
