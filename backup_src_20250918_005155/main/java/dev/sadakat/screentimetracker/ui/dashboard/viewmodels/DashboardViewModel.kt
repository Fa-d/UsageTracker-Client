package dev.sadakat.screentimetracker.ui.dashboard.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.sadakat.screentimetracker.domain.usecases.GetDashboardDataUseCase
import dev.sadakat.screentimetracker.domain.usecases.GetHistoricalDataUseCase
import dev.sadakat.screentimetracker.domain.usecases.HistoricalData
import dev.sadakat.screentimetracker.domain.usecases.GetAppSessionEventsUseCase
import dev.sadakat.screentimetracker.domain.usecases.GetAchievementsUseCase
import dev.sadakat.screentimetracker.domain.usecases.CalculateWellnessScoreUseCase
import dev.sadakat.screentimetracker.domain.usecases.InitializeAchievementsUseCase
import dev.sadakat.screentimetracker.domain.usecases.ChallengeManagerUseCase
import dev.sadakat.screentimetracker.domain.usecases.FocusSessionManagerUseCase
import dev.sadakat.screentimetracker.domain.usecases.WeeklyInsightsUseCase
import dev.sadakat.screentimetracker.domain.model.Achievement
import dev.sadakat.screentimetracker.domain.model.WellnessScore
import dev.sadakat.screentimetracker.ui.dashboard.state.DashboardState
import dev.sadakat.screentimetracker.ui.dashboard.state.AppUsageUIModel
import dev.sadakat.screentimetracker.core.data.repository.DigitalPetRepository
import dev.sadakat.screentimetracker.core.data.local.DigitalPet
import dev.sadakat.screentimetracker.core.data.local.PetStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getDashboardDataUseCase: GetDashboardDataUseCase,
    private val getHistoricalDataUseCase: GetHistoricalDataUseCase,
    private val getAppSessionEventsUseCase: GetAppSessionEventsUseCase,
    private val getAchievementsUseCase: GetAchievementsUseCase,
    private val calculateWellnessScoreUseCase: CalculateWellnessScoreUseCase,
    private val initializeAchievementsUseCase: InitializeAchievementsUseCase,
    val challengeManagerUseCase: ChallengeManagerUseCase,
    val focusSessionManagerUseCase: FocusSessionManagerUseCase,
    val weeklyInsightsUseCase: WeeklyInsightsUseCase,
    private val digitalPetRepository: DigitalPetRepository,
    private val application: Application
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardState(isLoading = true))
    val uiState: StateFlow<DashboardState> = _uiState.asStateFlow()

    private val _timelineEvents = MutableStateFlow<List<dev.sadakat.screentimetracker.core.data.local.AppSessionEvent>>(emptyList())
    val timelineEvents: StateFlow<List<dev.sadakat.screentimetracker.core.data.local.AppSessionEvent>> = _timelineEvents.asStateFlow()

    private val _achievements = MutableStateFlow<List<Achievement>>(emptyList())
    val achievements: StateFlow<List<Achievement>> = _achievements.asStateFlow()

    private val _wellnessScore = MutableStateFlow<WellnessScore?>(null)
    val wellnessScore: StateFlow<WellnessScore?> = _wellnessScore.asStateFlow()

    private val _digitalPet = MutableStateFlow<DigitalPet?>(null)
    val digitalPet: StateFlow<DigitalPet?> = _digitalPet.asStateFlow()

    private val _petStats = MutableStateFlow<PetStats?>(null)
    val petStats: StateFlow<PetStats?> = _petStats.asStateFlow()

    init {
        // Initialize achievements first
        viewModelScope.launch {
            try {
                initializeAchievementsUseCase()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error initializing achievements", e)
            }
        }

        // Initialize digital pet if needed
        viewModelScope.launch {
            try {
                createDefaultPetIfNeeded()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error initializing digital pet", e)
            }
        }

        // Load dashboard data, achievements, wellness score, and digital pet
        viewModelScope.launch {
            combine(
                getDashboardDataUseCase(),
                getHistoricalDataUseCase(),
                getAchievementsUseCase(),
                digitalPetRepository.getPet()
            ) { todayData, historicalData, achievements, digitalPet ->
                val appUsageUIModels = todayData.appDetailsToday.map { detail ->
                    AppUsageUIModel(
                        packageName = detail.packageName,
                        appName = getAppName(detail.packageName),
                        openCount = detail.sessionCount,
                        lastOpenedTimestamp = detail.lastOpenedTimestamp,
                        totalDurationMillisToday = detail.totalDurationMillis
                    )
                }

                val avgScreenTime = if (historicalData.appSummaries.isNotEmpty()) {
                    historicalData.appSummaries
                        .groupBy { it.dateMillis } // Group by day
                        .mapValues { entry -> entry.value.sumOf { it.totalDurationMillis } } // Sum durations for each day
                        .values.average().toLong() // Average of daily sums
                } else 0L

                val avgUnlocks = if (historicalData.unlockSummaries.isNotEmpty()) {
                    historicalData.unlockSummaries.map { it.unlockCount }.average().toInt()
                } else 0

                // Update achievements state
                _achievements.value = achievements

                // Calculate wellness score for today (dynamic calculation)
                try {
                    val todayWellnessScore = calculateWellnessScoreUseCase(System.currentTimeMillis(), forceRecalculate = false)
                    _wellnessScore.value = todayWellnessScore
                } catch (e: Exception) {
                    Log.e("DashboardViewModel", "Error calculating wellness score", e)
                }

                // Update digital pet state and wellness
                _digitalPet.value = digitalPet
                if (digitalPet != null) {
                    try {
                        val stats = digitalPetRepository.getPetStats(digitalPet)
                        _petStats.value = stats
                        
                        // Update pet wellness with dynamic wellness score if enough time has passed
                        if (digitalPetRepository.shouldUpdatePetWellness()) {
                            val currentState = _uiState.value.copy(
                                isLoading = false,
                                totalScreenUnlocksToday = todayData.totalScreenUnlocksToday,
                                appUsagesToday = appUsageUIModels,
                                totalScreenTimeTodayMillis = todayData.totalScreenTimeFromSessionsToday,
                                historicalAppSummaries = historicalData.appSummaries,
                                historicalUnlockSummaries = historicalData.unlockSummaries,
                                averageDailyScreenTimeMillisLastWeek = avgScreenTime,
                                averageDailyUnlocksLastWeek = avgUnlocks,
                                error = null
                            )
                            
                            // Update pet with current wellness score for dynamic response
                            val updatedPet = digitalPetRepository.updatePetWithWellness(
                                currentState, 
                                wellnessScore = _wellnessScore.value
                            )
                            _digitalPet.value = updatedPet
                            val updatedStats = digitalPetRepository.getPetStats(updatedPet)
                            _petStats.value = updatedStats
                        }
                    } catch (e: Exception) {
                        Log.e("DashboardViewModel", "Error calculating pet stats", e)
                    }
                }

                _uiState.value.copy(
                    isLoading = false,
                    totalScreenUnlocksToday = todayData.totalScreenUnlocksToday,
                    appUsagesToday = appUsageUIModels,
                    totalScreenTimeTodayMillis = todayData.totalScreenTimeFromSessionsToday,
                    historicalAppSummaries = historicalData.appSummaries,
                    historicalUnlockSummaries = historicalData.unlockSummaries,
                    averageDailyScreenTimeMillisLastWeek = avgScreenTime,
                    averageDailyUnlocksLastWeek = avgUnlocks,
                    error = null
                )
            }.catch { exception ->
                Log.e("DashboardViewModel", "Error loading dashboard data", exception)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to load dashboard data: ${exception.localizedMessage ?: "Unknown error"}"
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    internal fun getAppName(packageName: String): String {
        return try {
            val pm = application.packageManager
            val applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            pm.getApplicationLabel(applicationInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            Log.w("DashboardViewModel", "App name not found for package: $packageName")
            packageName // Fallback to package name
        }
    }

    fun loadTimelineEvents(startTime: Long, endTime: Long) {
        Log.d("DashboardViewModel", "Loading timeline events from $startTime to $endTime")
        viewModelScope.launch {
            getAppSessionEventsUseCase(startTime, endTime)
                .catch { exception ->
                    Log.e("DashboardViewModel", "Error loading timeline events", exception)
                    // Handle error, maybe update a separate error state for timeline
                }
                .collect { events ->
                    Log.d("DashboardViewModel", "Received ${events.size} timeline events.")
                    _timelineEvents.value = events
                }
        }
    }

    fun updatePetWithCurrentWellness() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val updatedPet = digitalPetRepository.updatePetWithWellness(
                    currentState,
                    wellnessScore = _wellnessScore.value
                )
                _digitalPet.value = updatedPet
                val stats = digitalPetRepository.getPetStats(updatedPet)
                _petStats.value = stats
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error updating pet wellness", e)
            }
        }
    }

    fun interactWithPet() {
        viewModelScope.launch {
            try {
                val updatedPet = digitalPetRepository.petInteraction()
                if (updatedPet != null) {
                    _digitalPet.value = updatedPet
                    val stats = digitalPetRepository.getPetStats(updatedPet)
                    _petStats.value = stats
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error interacting with pet", e)
            }
        }
    }

    fun feedPet() {
        viewModelScope.launch {
            try {
                val updatedPet = digitalPetRepository.feedPet()
                if (updatedPet != null) {
                    _digitalPet.value = updatedPet
                    val stats = digitalPetRepository.getPetStats(updatedPet)
                    _petStats.value = stats
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error feeding pet", e)
            }
        }
    }

    fun createDefaultPetIfNeeded() {
        viewModelScope.launch {
            try {
                if (digitalPetRepository.isFirstTimeUser()) {
                    val newPet = digitalPetRepository.createDefaultPet()
                    _digitalPet.value = newPet
                    val stats = digitalPetRepository.getPetStats(newPet)
                    _petStats.value = stats
                }
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error creating default pet", e)
            }
        }
    }

    fun refreshPetWellness() {
        viewModelScope.launch {
            try {
                updatePetWithCurrentWellness()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error refreshing pet wellness", e)
            }
        }
    }
    
    fun refreshWellnessScore() {
        viewModelScope.launch {
            try {
                val todayWellnessScore = calculateWellnessScoreUseCase(System.currentTimeMillis(), forceRecalculate = true)
                _wellnessScore.value = todayWellnessScore
                
                // Update pet with the new wellness score for immediate response
                updatePetWithCurrentWellness()
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error refreshing wellness score", e)
            }
        }
    }

    fun exportUsageData() {
        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                val currentTime = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
                val timestamp = dateFormat.format(Date(currentTime))
                
                // Create CSV content
                val csvContent = StringBuilder()
                csvContent.append("Export Date,${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(currentTime))}\n")
                csvContent.append("Total Screen Time Today (minutes),${currentState.totalScreenTimeTodayMillis / (1000 * 60)}\n")
                csvContent.append("Total Unlocks Today,${currentState.totalScreenUnlocksToday}\n")
                csvContent.append("Average Daily Screen Time Last Week (minutes),${currentState.averageDailyScreenTimeMillisLastWeek / (1000 * 60)}\n")
                csvContent.append("Average Daily Unlocks Last Week,${currentState.averageDailyUnlocksLastWeek}\n")
                csvContent.append("\nApp Usage Today:\n")
                csvContent.append("App Name,Package Name,Total Time (minutes),Open Count,Last Opened\n")
                
                currentState.appUsagesToday.forEach { app ->
                    val lastOpenedDate = if (app.lastOpenedTimestamp > 0) {
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(app.lastOpenedTimestamp))
                    } else {
                        "Never"
                    }
                    csvContent.append("${app.appName},${app.packageName},${app.totalDurationMillisToday / (1000 * 60)},${app.openCount},$lastOpenedDate\n")
                }
                
                // Create file in app's external files directory
                val downloadsDir = File(application.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "ScreenTimeTracker")
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs()
                }
                
                val fileName = "screen_time_export_$timestamp.csv"
                val file = File(downloadsDir, fileName)
                
                FileWriter(file).use { writer ->
                    writer.write(csvContent.toString())
                }
                
                Log.d("DashboardViewModel", "Data exported to: ${file.absolutePath}")
                
                // Show success message and share intent
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(application, "Data exported to: $fileName", Toast.LENGTH_LONG).show()
                    
                    // Create share intent
                    val uri = FileProvider.getUriForFile(
                        application,
                        "${application.packageName}.fileprovider",
                        file
                    )
                    
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Screen Time Data Export")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    
                    val chooserIntent = Intent.createChooser(shareIntent, "Share Screen Time Data")
                    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    application.startActivity(chooserIntent)
                }
                
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error exporting data", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(application, "Failed to export data: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun clearCache() {
        viewModelScope.launch {
            try {
                // Clear app's internal cache directory
                val cacheDir = application.cacheDir
                deleteDirectoryContents(cacheDir)
                
                // Clear external cache directory if it exists
                application.externalCacheDir?.let { externalCacheDir ->
                    deleteDirectoryContents(externalCacheDir)
                }
                
                // Calculate freed space (approximate)
                val cacheSize = calculateDirectorySize(cacheDir)
                val externalCacheSize = application.externalCacheDir?.let { calculateDirectorySize(it) } ?: 0L
                val totalFreed = cacheSize + externalCacheSize
                
                Log.d("DashboardViewModel", "Cache cleared successfully. Freed approximately ${totalFreed / 1024} KB")
                
                // Show success message
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    val freedMB = totalFreed / (1024 * 1024)
                    val message = if (freedMB > 0) {
                        "Cache cleared successfully! Freed ${freedMB} MB of storage"
                    } else {
                        "Cache cleared successfully!"
                    }
                    Toast.makeText(application, message, Toast.LENGTH_LONG).show()
                }
                
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error clearing cache", e)
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(application, "Failed to clear cache: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun deleteDirectoryContents(directory: File) {
        if (directory.exists() && directory.isDirectory) {
            directory.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    deleteDirectoryContents(file)
                    file.delete()
                } else {
                    file.delete()
                }
            }
        }
    }
    
    private fun calculateDirectorySize(directory: File): Long {
        if (!directory.exists() || !directory.isDirectory) return 0L
        
        var size = 0L
        directory.listFiles()?.forEach { file ->
            size += if (file.isDirectory) {
                calculateDirectorySize(file)
            } else {
                file.length()
            }
        }
        return size
    }
}
