package com.example.screentimetracker.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.screentimetracker.data.local.DailyAppSummary
import com.example.screentimetracker.data.local.DailyScreenUnlockSummary
import com.example.screentimetracker.ui.theme.ScreenTimeTrackerTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.max


// Helper function to format duration
@Composable
fun rememberFormattedDuration(durationMillis: Long): String {
    return remember(durationMillis) {
        if (durationMillis < 0) return@remember "N/A"
        val hours = TimeUnit.MILLISECONDS.toHours(durationMillis)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60
        val seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60

        when {
            hours > 0 -> String.format("%dh %02dm", hours, minutes)
            minutes > 0 -> String.format("%dm %02ds", minutes, seconds)
            seconds > 0 -> String.format("%ds", seconds)
            durationMillis == 0L -> "0s"
            else -> "<1s"
        }
    }
}


@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    DashboardContent(
        state = uiState,
        onRefresh = { viewModel.processIntent(DashboardIntent.LoadDashboardData) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardContent(
    state: DashboardState,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Screen Time Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (state.error != null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: ${state.error}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    ScreenUnlockSummary(unlockCount = state.totalScreenUnlocksToday, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(16.dp))
                    TotalScreenTimeTodaySummary(totalScreenTimeMillis = state.totalScreenTimeTodayMillis, modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (state.appUsagesToday.any { it.totalDurationMillisToday > 0 }) {
                    AppUsageVisuals(appUsages = state.appUsagesToday)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                AppUsageList(appUsages = state.appUsagesToday)
                Spacer(modifier = Modifier.height(24.dp))

                HistoricalDataSection(
                    historicalAppSummaries = state.historicalAppSummaries,
                    historicalUnlockSummaries = state.historicalUnlockSummaries,
                    averageDailyScreenTimeMillis = state.averageDailyScreenTimeMillisLastWeek,
                    averageDailyUnlocks = state.averageDailyUnlocksLastWeek
                )
            }
        }
    }
}

@Composable
fun ScreenUnlockSummary(unlockCount: Int, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Screen Unlocks Today", style = MaterialTheme.typography.titleMedium)
            Text("$unlockCount", style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp), modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun TotalScreenTimeTodaySummary(totalScreenTimeMillis: Long, modifier: Modifier = Modifier) {
    val formattedTotalDuration = rememberFormattedDuration(totalScreenTimeMillis)
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Screen Time", style = MaterialTheme.typography.titleMedium)
            Text(formattedTotalDuration, style = MaterialTheme.typography.headlineLarge.copy(fontSize = 48.sp), modifier = Modifier.padding(top = 8.dp))
        }
    }
}

@Composable
fun AppUsageVisuals(appUsages: List<AppUsageUIModel>) {
    val topN = 5
    val sortedApps = appUsages.filter { it.totalDurationMillisToday > 0 }
                                     .sortedByDescending { it.totalDurationMillisToday }
                                     .take(topN)
    if (sortedApps.isEmpty()) return
    val maxDuration = max(1L, sortedApps.firstOrNull()?.totalDurationMillisToday ?: 1L)

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Top App Durations Today:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 12.dp))
            sortedApps.forEach { app ->
                val formattedDuration = rememberFormattedDuration(app.totalDurationMillisToday)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(app.appName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(0.4f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Box(modifier = Modifier.weight(0.6f).height(20.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.surfaceVariant)) {
                        Box(modifier = Modifier.fillMaxWidth(fraction = (app.totalDurationMillisToday.toFloat() / maxDuration).coerceIn(0.05f, 1f)).height(20.dp).clip(MaterialTheme.shapes.small).background(MaterialTheme.colorScheme.primary))
                    }
                    Text(formattedDuration, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 8.dp), textAlign = TextAlign.End)
                }
            }
        }
    }
}

@Composable
fun AppUsageList(appUsages: List<AppUsageUIModel>) {
    if (appUsages.isEmpty()) {
        Text("No app usage data available for today.", modifier = Modifier.padding(top = 16.dp), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        return
    }
    Text("App Activity Today Details:", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp, top = 8.dp))
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(appUsages) { appUsage -> AppUsageRow(appUsage = appUsage) }
    }
}

@Composable
fun AppUsageRow(appUsage: AppUsageUIModel) {
    val sdf = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    val formattedDuration = rememberFormattedDuration(appUsage.totalDurationMillisToday)
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(appUsage.appName, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Duration: $formattedDuration", style = MaterialTheme.typography.bodyMedium)
                Text("Opened: ${appUsage.openCount} times | Last: ${if (appUsage.lastOpenedTimestamp > 0) sdf.format(Date(appUsage.lastOpenedTimestamp)) else "N/A"}", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun HistoricalDataSection(
    historicalAppSummaries: List<DailyAppSummary>,
    historicalUnlockSummaries: List<DailyScreenUnlockSummary>,
    averageDailyScreenTimeMillis: Long,
    averageDailyUnlocks: Int,
    modifier: Modifier = Modifier
) {
    val formattedAvgScreenTime = rememberFormattedDuration(averageDailyScreenTimeMillis)
    val sdfDate = remember { SimpleDateFormat("MMM dd (EEE)", Locale.getDefault()) }

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Last 7 Days Insights", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 12.dp))
            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Avg. Screen Time/Day", style = MaterialTheme.typography.labelMedium)
                    Text(formattedAvgScreenTime, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Avg. Unlocks/Day", style = MaterialTheme.typography.labelMedium)
                    Text("$averageDailyUnlocks", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }
            }

            val dailyDataMap = remember(historicalAppSummaries, historicalUnlockSummaries) { // Remember consolidated data
                val map = mutableMapOf<Long, Pair<Long, Int>>()
                historicalAppSummaries.groupBy { it.dateMillis }.forEach { (date, summaries) ->
                    val totalDuration = summaries.sumOf { it.totalDurationMillis }
                    map[date] = Pair(totalDuration, map[date]?.second ?: 0)
                }
                historicalUnlockSummaries.forEach { summary ->
                    map[summary.dateMillis] = Pair(map[summary.dateMillis]?.first ?: 0L, summary.unlockCount)
                }
                map.entries.sortedByDescending { it.key } // Sort once
            }

            if (dailyDataMap.isNotEmpty()) {
                Text("Daily Breakdown (Last 7 Days):", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 150.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 300.dp).fillMaxWidth() // Ensure it fills width for grid
                ) {
                    items(dailyDataMap.size) { index ->
                        val (dateMillis, dataPair) = dailyDataMap[index]
                        val (totalDuration, unlockCount) = dataPair
                        DailySummaryCard(
                            dateFormatted = sdfDate.format(Date(dateMillis)),
                            totalDurationMillis = totalDuration,
                            unlockCount = unlockCount
                        )
                    }
                }
            } else {
                Text("No historical data available for the last 7 days.", style = MaterialTheme.typography.bodyMedium, fontStyle = FontStyle.Italic, modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
fun DailySummaryCard(dateFormatted: String, totalDurationMillis: Long, unlockCount: Int) {
    val formattedDuration = rememberFormattedDuration(totalDurationMillis)
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(dateFormatted, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Screen Time: $formattedDuration", style = MaterialTheme.typography.bodyMedium)
            Text("Unlocks: $unlockCount", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


// --- PREVIEWS ---
@Preview(showBackground = true, name = "Dashboard Content - Loading")
@Composable
fun DashboardContentPreviewLoading() {
    ScreenTimeTrackerTheme { DashboardContent(state = DashboardState(isLoading = true), onRefresh = {}) }
}

@Preview(showBackground = true, name = "Dashboard Content - Error")
@Composable
fun DashboardContentPreviewError() {
    ScreenTimeTrackerTheme { DashboardContent(state = DashboardState(isLoading = false, error = "Failed to load data."), onRefresh = {}) }
}

@Preview(showBackground = true, name = "App Usage Visuals Preview")
@Composable
fun AppUsageVisualsPreview() {
    val sampleAppUsages = listOf(
        AppUsageUIModel("com.app.sample1", "Productivity App", 15, System.currentTimeMillis() - 100000, 150 * 60 * 1000L),
        AppUsageUIModel("com.app.sample2", "Social Media", 12, System.currentTimeMillis() - 3000000, 120 * 60 * 1000L)
    )
    ScreenTimeTrackerTheme { AppUsageVisuals(appUsages = sampleAppUsages) }
}

@Preview(showBackground = true, name = "Historical Data Section Preview")
@Composable
fun HistoricalDataSectionPreview() {
    val calendar = Calendar.getInstance()
    val todayMillis = calendar.timeInMillis
    val sampleAppSummaries = (0..6).map { i ->
        calendar.timeInMillis = todayMillis
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        DailyAppSummary(calendar.timeInMillis, "com.sample.app${i % 2 + 1}", (1000L * 60 * (30 + i * 10)), 5 + i)
    }
    val sampleUnlockSummaries = (0..6).map { i ->
        calendar.timeInMillis = todayMillis
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        DailyScreenUnlockSummary(calendar.timeInMillis, 20 + i * 5)
    }
    ScreenTimeTrackerTheme {
        HistoricalDataSection(
            historicalAppSummaries = sampleAppSummaries,
            historicalUnlockSummaries = sampleUnlockSummaries,
            averageDailyScreenTimeMillis = 3 * 60 * 60 * 1000L, // 3 hours
            averageDailyUnlocks = 35
        )
    }
}

@Preview(showBackground = true, name = "Dashboard Content - Full Data")
@Composable
fun DashboardContentPreviewData() {
    val calendar = Calendar.getInstance()
    val todayMillis = calendar.timeInMillis

    val sampleAppUsagesToday = listOf(
        AppUsageUIModel("com.app.today1", "Today Productive", 5, System.currentTimeMillis() - 100000, 2 * 60 * 60 * 1000L + 15 * 60 * 1000L),
        AppUsageUIModel("com.app.today2", "Today Social", 10, System.currentTimeMillis() - 200000, 1 * 60 * 60 * 1000L + 30 * 60 * 1000L)
    )
    val sampleHistoricalAppSummaries = (0..6).map { i ->
        calendar.timeInMillis = todayMillis
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        DailyAppSummary(calendar.timeInMillis, "com.historic.app${i % 2 + 1}", (1000L * 60 * (60 + i * 15)), 3 + i)
    }.reversed() // Ensure chronological for processing if needed, though map sorts by key later

    val sampleHistoricalUnlockSummaries = (0..6).map { i ->
        calendar.timeInMillis = todayMillis
        calendar.add(Calendar.DAY_OF_YEAR, -i)
        DailyScreenUnlockSummary(calendar.timeInMillis, 20 + i * 2)
    }.reversed()

    ScreenTimeTrackerTheme {
        DashboardContent(
            state = DashboardState(
                isLoading = false,
                totalScreenUnlocksToday = 25,
                appUsagesToday = sampleAppUsagesToday,
                totalScreenTimeTodayMillis = sampleAppUsagesToday.sumOf { it.totalDurationMillisToday },
                historicalAppSummaries = sampleHistoricalAppSummaries,
                historicalUnlockSummaries = sampleHistoricalUnlockSummaries,
                averageDailyScreenTimeMillisLastWeek = (sampleHistoricalAppSummaries.sumOf { it.totalDurationMillis } / (if (sampleHistoricalAppSummaries.isNotEmpty()) sampleHistoricalAppSummaries.distinctBy { it.dateMillis }.size else 1)).toLong(),
                averageDailyUnlocksLastWeek = if (sampleHistoricalUnlockSummaries.isNotEmpty()) (sampleHistoricalUnlockSummaries.sumOf { it.unlockCount } / sampleHistoricalUnlockSummaries.size) else 0,
                error = null
            ),
            onRefresh = {}
        )
    }
}

@Preview(showBackground = true, name = "Dashboard Content - Empty Historical")
@Composable
fun DashboardContentPreviewEmptyHistorical() {
     val sampleAppUsagesToday = listOf(
        AppUsageUIModel("com.app.today1", "Today Productive", 5, System.currentTimeMillis() - 100000, 2 * 60 * 60 * 1000L + 15 * 60 * 1000L)
    )
    ScreenTimeTrackerTheme {
        DashboardContent(
            state = DashboardState(
                isLoading = false,
                totalScreenUnlocksToday = 10,
                appUsagesToday = sampleAppUsagesToday,
                totalScreenTimeTodayMillis = sampleAppUsagesToday.sumOf { it.totalDurationMillisToday },
                historicalAppSummaries = emptyList(),
                historicalUnlockSummaries = emptyList(),
                averageDailyScreenTimeMillisLastWeek = 0,
                averageDailyUnlocksLastWeek = 0
            ),
            onRefresh = {}
        )
    }
}
