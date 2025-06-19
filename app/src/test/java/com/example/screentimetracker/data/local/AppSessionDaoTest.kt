package com.example.screentimetracker.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.OLDEST_SDK])
class AppSessionDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var appSessionDao: AppSessionDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        appSessionDao = database.appSessionDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAggregatedSessionDataForDay() = runBlocking {
        val day1Start = 0L // Example start of day
        val day1End = day1Start + TimeUnit.DAYS.toMillis(1)

        appSessionDao.insertAppSession(AppSessionEvent(packageName = "app1", startTimeMillis = day1Start + 1000, endTimeMillis = day1Start + 2000, durationMillis = 1000))
        appSessionDao.insertAppSession(AppSessionEvent(packageName = "app1", startTimeMillis = day1Start + 3000, endTimeMillis = day1Start + 5000, durationMillis = 2000))
        appSessionDao.insertAppSession(AppSessionEvent(packageName = "app2", startTimeMillis = day1Start + 6000, endTimeMillis = day1Start + 7000, durationMillis = 1000))

        // Session outside the day range
        appSessionDao.insertAppSession(AppSessionEvent(packageName = "app3", startTimeMillis = day1End + 1000, endTimeMillis = day1End + 2000, durationMillis = 1000))


        val aggregates = appSessionDao.getAggregatedSessionDataForDay(day1Start, day1End).first()

        assertEquals(2, aggregates.size) // app1 and app2

        val app1Data = aggregates.find { it.packageName == "app1" }
        assertNotNull(app1Data)
        assertEquals(3000, app1Data!!.totalDuration) // 1000 + 2000
        assertEquals(2, app1Data.sessionCount)

        val app2Data = aggregates.find { it.packageName == "app2" }
        assertNotNull(app2Data)
        assertEquals(1000, app2Data!!.totalDuration)
        assertEquals(1, app2Data.sessionCount)
    }
}

// Helper (already defined in AppUsageDaoTest, can be extracted to a common test util if many DAO tests)
object TimeUnit { // Simple object for test time units
    fun DAYS_toMillis(days: Long): Long = days * 24 * 60 * 60 * 1000
}
