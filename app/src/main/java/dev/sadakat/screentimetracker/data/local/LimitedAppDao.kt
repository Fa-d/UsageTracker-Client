package dev.sadakat.screentimetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LimitedAppDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLimitedApp(limitedApp: LimitedApp)

    @Delete
    suspend fun deleteLimitedApp(limitedApp: LimitedApp)

    @Query("SELECT * FROM limited_apps WHERE packageName = :packageName")
    fun getLimitedApp(packageName: String): Flow<LimitedApp?> // Flow for observing changes

    @Query("SELECT * FROM limited_apps WHERE packageName = :packageName")
    suspend fun getLimitedAppOnce(packageName: String): LimitedApp? // Suspend fun for one-time fetch

    @Query("SELECT * FROM limited_apps")
    fun getAllLimitedApps(): Flow<List<LimitedApp>> // Flow for observing list changes

    @Query("SELECT * FROM limited_apps")
    suspend fun getAllLimitedAppsOnce(): List<LimitedApp> // Suspend fun for one-time fetch of the list

    // Export methods
    @Query("SELECT * FROM limited_apps ORDER BY packageName ASC")
    suspend fun getAllLimitedAppsForExport(): List<LimitedApp>
}
