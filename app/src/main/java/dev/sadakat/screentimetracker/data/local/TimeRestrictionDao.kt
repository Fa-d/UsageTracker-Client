package dev.sadakat.screentimetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeRestrictionDao {
    @Query("SELECT * FROM time_restrictions ORDER BY startTimeMinutes ASC")
    fun getAllTimeRestrictions(): Flow<List<TimeRestriction>>

    @Query("SELECT * FROM time_restrictions WHERE isEnabled = 1 ORDER BY startTimeMinutes ASC")
    fun getActiveTimeRestrictions(): Flow<List<TimeRestriction>>

    @Query("SELECT * FROM time_restrictions WHERE restrictionType = :type AND isEnabled = 1")
    fun getRestrictionsByType(type: String): Flow<List<TimeRestriction>>

    @Query("SELECT * FROM time_restrictions WHERE id = :id")
    suspend fun getRestrictionById(id: Long): TimeRestriction?

    @Query("""
        SELECT * FROM time_restrictions 
        WHERE isEnabled = 1 
        AND (:currentTimeMinutes BETWEEN startTimeMinutes AND endTimeMinutes 
             OR (startTimeMinutes > endTimeMinutes AND (:currentTimeMinutes >= startTimeMinutes OR :currentTimeMinutes <= endTimeMinutes)))
        AND (daysOfWeek LIKE '%' || :dayOfWeek || '%' OR daysOfWeek = '[]')
    """)
    suspend fun getActiveRestrictionsForTime(currentTimeMinutes: Int, dayOfWeek: Int): List<TimeRestriction>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeRestriction(restriction: TimeRestriction): Long

    @Update
    suspend fun updateTimeRestriction(restriction: TimeRestriction)

    @Query("UPDATE time_restrictions SET isEnabled = :isEnabled, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateRestrictionEnabled(id: Long, isEnabled: Boolean, updatedAt: Long)

    @Delete
    suspend fun deleteTimeRestriction(restriction: TimeRestriction)

    @Query("DELETE FROM time_restrictions WHERE restrictionType = :type")
    suspend fun deleteRestrictionsByType(type: String)

    // Export methods
    @Query("SELECT * FROM time_restrictions ORDER BY createdAt ASC")
    suspend fun getAllTimeRestrictionsForExport(): List<TimeRestriction>
}