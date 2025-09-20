package dev.sadakat.screentimetracker.core.domain.usecases

import android.content.Context
import com.google.gson.GsonBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.sadakat.screentimetracker.core.data.local.dao.AchievementDao
import dev.sadakat.screentimetracker.core.data.local.dao.AppSessionDao
import dev.sadakat.screentimetracker.core.data.local.dao.AppUsageDao
import dev.sadakat.screentimetracker.core.data.local.dao.ChallengeDao
import dev.sadakat.screentimetracker.core.data.local.dao.DailyAppSummaryDao
import dev.sadakat.screentimetracker.core.data.local.dao.DailyScreenUnlockSummaryDao
import dev.sadakat.screentimetracker.core.data.local.dao.FocusSessionDao
import dev.sadakat.screentimetracker.core.data.local.dao.HabitTrackerDao
import dev.sadakat.screentimetracker.core.data.local.dao.LimitedAppDao
import dev.sadakat.screentimetracker.core.data.local.dao.ProgressiveLimitDao
import dev.sadakat.screentimetracker.core.data.local.dao.ProgressiveMilestoneDao
import dev.sadakat.screentimetracker.core.data.local.dao.ScreenUnlockDao
import dev.sadakat.screentimetracker.core.data.local.dao.TimeRestrictionDao
import dev.sadakat.screentimetracker.core.data.local.dao.UserGoalDao
import dev.sadakat.screentimetracker.core.data.local.dao.WellnessScoreDao
import dev.sadakat.screentimetracker.core.data.local.entities.Achievement
import dev.sadakat.screentimetracker.core.data.local.entities.AppSessionEvent
import dev.sadakat.screentimetracker.core.data.local.entities.AppUsageEvent
import dev.sadakat.screentimetracker.core.data.local.entities.Challenge
import dev.sadakat.screentimetracker.core.data.local.entities.DailyAppSummary
import dev.sadakat.screentimetracker.core.data.local.entities.DailyScreenUnlockSummary
import dev.sadakat.screentimetracker.core.data.local.entities.FocusSession
import dev.sadakat.screentimetracker.core.data.local.entities.HabitTracker
import dev.sadakat.screentimetracker.core.data.local.entities.LimitedApp
import dev.sadakat.screentimetracker.core.data.local.entities.ProgressiveLimit
import dev.sadakat.screentimetracker.core.data.local.entities.ProgressiveMilestone
import dev.sadakat.screentimetracker.core.data.local.entities.ScreenUnlockEvent
import dev.sadakat.screentimetracker.core.data.local.entities.TimeRestriction
import dev.sadakat.screentimetracker.core.data.local.entities.UserGoal
import dev.sadakat.screentimetracker.core.data.local.entities.WellnessScore
import dev.sadakat.screentimetracker.core.domain.repository.TrackerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.time.LocalDate

data class ExportData(
    val exportDate: String,
    val appUsageEvents: List<AppUsageEvent>,
    val appSessionEvents: List<AppSessionEvent>,
    val screenUnlockEvents: List<ScreenUnlockEvent>,
    val dailyAppSummaries: List<DailyAppSummary>,
    val dailyScreenUnlockSummaries: List<DailyScreenUnlockSummary>,
    val achievements: List<Achievement>,
    val wellnessScores: List<WellnessScore>,
    val userGoals: List<UserGoal>,
    val challenges: List<Challenge>,
    val focusSessions: List<FocusSession>,
    val habitTrackers: List<HabitTracker>,
    val timeRestrictions: List<TimeRestriction>,
    val progressiveLimits: List<ProgressiveLimit>,
    val progressiveMilestones: List<ProgressiveMilestone>,
    val limitedApps: List<LimitedApp>
)

class DataExportUseCase(
    @ApplicationContext private val context: Context,
    private val trackerRepository: TrackerRepository,
    private val appUsageDao: AppUsageDao,
    private val appSessionDao: AppSessionDao,
    private val screenUnlockDao: ScreenUnlockDao,
    private val dailyAppSummaryDao: DailyAppSummaryDao,
    private val dailyScreenUnlockSummaryDao: DailyScreenUnlockSummaryDao,
    private val achievementDao: AchievementDao,
    private val wellnessScoreDao: WellnessScoreDao,
    private val userGoalDao: UserGoalDao,
    private val challengeDao: ChallengeDao,
    private val focusSessionDao: FocusSessionDao,
    private val habitTrackerDao: HabitTrackerDao,
    private val timeRestrictionDao: TimeRestrictionDao,
    private val progressiveLimitDao: ProgressiveLimitDao,
    private val progressiveMilestoneDao: ProgressiveMilestoneDao,
    private val limitedAppDao: LimitedAppDao,
    private val privacyManagerUseCase: PrivacyManagerUseCase
) {
    
    suspend fun exportDataAsJson(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val exportData = gatherAllData()
            val gson = GsonBuilder().setPrettyPrinting().create()
            val jsonString = gson.toJson(exportData)
            
            val fileName = "screen_time_data_${LocalDate.now()}.json"
            val file = File(context.getExternalFilesDir(null), fileName)
            
            FileWriter(file).use { writer ->
                writer.write(jsonString)
            }
            
            privacyManagerUseCase.updateLastExportTime()
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun exportDataAsCsv(): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            val files = mutableListOf<File>()
            val dateString = LocalDate.now().toString()
            
            // Export App Usage Events
            val appUsageEvents = appUsageDao.getAllAppUsageEventsForExport()
            files.add(createCsvFile("app_usage_events_$dateString.csv", appUsageEvents) { event ->
                "${event.packageName},${event.eventName},${event.timestamp}"
            })
            
            // Export App Session Events
            val appSessionEvents = appSessionDao.getAllAppSessionEventsForExport()
            files.add(createCsvFile("app_session_events_$dateString.csv", appSessionEvents) { event ->
                "${event.packageName},${event.startTimeMillis},${event.endTimeMillis},${event.durationMillis}"
            })
            
            // Export Screen Unlock Events
            val screenUnlockEvents = screenUnlockDao.getAllScreenUnlockEventsForExport()
            files.add(createCsvFile("screen_unlock_events_$dateString.csv", screenUnlockEvents) { event ->
                "${event.timestamp}"
            })
            
            // Export Daily Summaries
            val dailySummaries = dailyAppSummaryDao.getAllDailyAppSummariesForExport()
            files.add(createCsvFile("daily_app_summaries_$dateString.csv", dailySummaries) { summary ->
                "${summary.dateMillis},${summary.packageName},${summary.totalDurationMillis},${summary.openCount}"
            })
            
            // Export Achievements
            val achievements = achievementDao.getAllAchievementsForExport()
            files.add(createCsvFile("achievements_$dateString.csv", achievements) { achievement ->
                "${achievement.achievementId},${achievement.name},${achievement.isUnlocked},${achievement.unlockedDate},${achievement.currentProgress}"
            })
            
            // Export Wellness Scores
            val wellnessScores = wellnessScoreDao.getAllWellnessScoresForExport()
            files.add(createCsvFile("wellness_scores_$dateString.csv", wellnessScores) { score ->
                "${score.date},${score.totalScore},${score.timeLimitScore},${score.focusSessionScore},${score.breaksScore},${score.sleepHygieneScore},${score.level}"
            })
            
            privacyManagerUseCase.updateLastExportTime()
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    private suspend fun gatherAllData(): ExportData {
        return ExportData(
            exportDate = LocalDate.now().toString(),
            appUsageEvents = appUsageDao.getAllAppUsageEventsForExport(),
            appSessionEvents = appSessionDao.getAllAppSessionEventsForExport(),
            screenUnlockEvents = screenUnlockDao.getAllScreenUnlockEventsForExport(),
            dailyAppSummaries = dailyAppSummaryDao.getAllDailyAppSummariesForExport(),
            dailyScreenUnlockSummaries = dailyScreenUnlockSummaryDao.getAllDailyScreenUnlockSummariesForExport(),
            achievements = achievementDao.getAllAchievementsForExport(),
            wellnessScores = wellnessScoreDao.getAllWellnessScoresForExport(),
            userGoals = userGoalDao.getAllUserGoalsForExport(),
            challenges = challengeDao.getAllChallengesForExport(),
            focusSessions = focusSessionDao.getAllFocusSessionsForExport(),
            habitTrackers = habitTrackerDao.getAllHabitTrackersForExport(),
            timeRestrictions = timeRestrictionDao.getAllTimeRestrictionsForExport(),
            progressiveLimits = progressiveLimitDao.getAllProgressiveLimitsForExport(),
            progressiveMilestones = progressiveMilestoneDao.getAllProgressiveMilestonesForExport(),
            limitedApps = limitedAppDao.getAllLimitedAppsForExport()
        )
    }
    
    private fun <T> createCsvFile(fileName: String, data: List<T>, mapper: (T) -> String): File {
        val file = File(context.getExternalFilesDir(null), fileName)
        FileWriter(file).use { writer ->
            data.forEach { item ->
                writer.write(mapper(item))
                writer.write("\n")
            }
        }
        return file
    }
}