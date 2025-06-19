package com.example.screentimetracker.data.local

import android.content.Context // Will be mocked by Robolectric or not used if possible
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider // Needs Robolectric for local JVM test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

// --- IMPORTANT ---
// To run this test in a pure JVM environment (without a real Android device/emulator),
// Robolectric is often used to provide mock Android framework classes.
// The @RunWith(RobolectricTestRunner::class) and @Config annotations are for Robolectric.
// If running in an Android instrumented test environment, these would be different.
// For this subtask, we'll assume a Robolectric-like environment for Context.
// If Robolectric isn't actually available in the execution sandbox, this test
// might not be directly runnable by the subtask worker, but the code serves as a template.
// A simpler version would avoid Android Context if DAO tests don't strictly need it.

@RunWith(RobolectricTestRunner::class) // For JVM tests needing Android context
@Config(sdk = [Config.OLDEST_SDK]) // Configure Robolectric SDK if needed
class AppUsageDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var appUsageDao: AppUsageDao

    @Before
    fun setup() {
        // Robolectric provides a Context for ApplicationProvider.getApplicationContext()
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // Only for testing
            .build()
        appUsageDao = database.appUsageDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetAppUsageEvent() = runBlocking {
        val event = AppUsageEvent(packageName = "com.test.app", eventName = "opened", timestamp = 1000L)
        appUsageDao.insertAppUsageEvent(event)

        val retrievedEvents = appUsageDao.getUsageEventsForApp("com.test.app").first()
        assertEquals(1, retrievedEvents.size)
        assertEquals(event.copy(id=retrievedEvents[0].id), retrievedEvents[0]) // Compare, allowing for auto-generated ID
    }

    @Test
    fun getAppOpenCountsSince_returnsCorrectData() = runBlocking {
        val now = System.currentTimeMillis()
        val yesterday = now - (24 * 60 * 60 * 1000)

        appUsageDao.insertAppUsageEvent(AppUsageEvent(packageName = "app1", eventName = "opened", timestamp = now - 1000))
        appUsageDao.insertAppUsageEvent(AppUsageEvent(packageName = "app1", eventName = "opened", timestamp = now - 2000))
        appUsageDao.insertAppUsageEvent(AppUsageEvent(packageName = "app2", eventName = "opened", timestamp = now - 3000))
        appUsageDao.insertAppUsageEvent(AppUsageEvent(packageName = "app1", eventName = "closed", timestamp = now - 500)) // Should not be counted
        appUsageDao.insertAppUsageEvent(AppUsageEvent(packageName = "app3", eventName = "opened", timestamp = yesterday - 1000)) // Too old

        val counts = appUsageDao.getAppOpenCountsSince(yesterday).first() // Query for events since yesterday

        assertEquals(2, counts.size) // app1 and app2, app3 is too old

        val app1Data = counts.find { it.packageName == "app1" }
        assertEquals(2, app1Data?.openCount)
        assertTrue(app1Data?.lastOpenedTimestamp == now - 1000L)

        val app2Data = counts.find { it.packageName == "app2" }
        assertEquals(1, app2Data?.openCount)
        assertTrue(app2Data?.lastOpenedTimestamp == now - 3000L)
    }
}
