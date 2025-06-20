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
class LimitedAppDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var limitedAppDao: LimitedAppDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        limitedAppDao = database.limitedAppDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetLimitedApp() = runBlocking {
        val limitedApp = LimitedApp("com.example.app", 600000L) // 10 minutes
        limitedAppDao.insertLimitedApp(limitedApp)

        val retrievedAppFlow = limitedAppDao.getLimitedApp("com.example.app").first()
        assertNotNull(retrievedAppFlow)
        assertEquals(limitedApp, retrievedAppFlow)

        val retrievedAppOnce = limitedAppDao.getLimitedAppOnce("com.example.app")
        assertNotNull(retrievedAppOnce)
        assertEquals(limitedApp, retrievedAppOnce)
    }

    @Test
    fun insertAndDeleteLimitedApp() = runBlocking {
        val limitedApp = LimitedApp("com.example.app", 600000L)
        limitedAppDao.insertLimitedApp(limitedApp)

        var retrievedApp = limitedAppDao.getLimitedAppOnce("com.example.app")
        assertNotNull(retrievedApp)

        limitedAppDao.deleteLimitedApp(limitedApp)
        retrievedApp = limitedAppDao.getLimitedAppOnce("com.example.app")
        assertNull(retrievedApp)
    }

    @Test
    fun getAllLimitedApps() = runBlocking {
        val app1 = LimitedApp("com.app1", 300000L)
        val app2 = LimitedApp("com.app2", 900000L)
        limitedAppDao.insertLimitedApp(app1)
        limitedAppDao.insertLimitedApp(app2)

        val allAppsFlow = limitedAppDao.getAllLimitedApps().first()
        assertEquals(2, allAppsFlow.size)
        assertTrue(allAppsFlow.contains(app1))
        assertTrue(allAppsFlow.contains(app2))

        val allAppsOnce = limitedAppDao.getAllLimitedAppsOnce()
        assertEquals(2, allAppsOnce.size)
        assertTrue(allAppsOnce.contains(app1))
        assertTrue(allAppsOnce.contains(app2))
    }

    @Test
    fun insertReplaceExisting() = runBlocking {
        val initialLimit = LimitedApp("com.example.app", 600000L) // 10 minutes
        limitedAppDao.insertLimitedApp(initialLimit)

        val updatedLimit = LimitedApp("com.example.app", 1200000L) // 20 minutes
        limitedAppDao.insertLimitedApp(updatedLimit) // Should replace due to OnConflictStrategy.REPLACE

        val retrievedApp = limitedAppDao.getLimitedAppOnce("com.example.app")
        assertNotNull(retrievedApp)
        assertEquals(updatedLimit.timeLimitMillis, retrievedApp!!.timeLimitMillis)
        assertEquals(1, limitedAppDao.getAllLimitedAppsOnce().size)
    }
}
