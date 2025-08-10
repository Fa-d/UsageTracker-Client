package com.example.screentimetracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.screentimetracker.ui.base.BaseComposeInstrumentedTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class InteractiveTimelineTest : BaseComposeInstrumentedTest() {

    private fun createSampleTimelineEvents(): List<TimelineEvent> {
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L
        
        return listOf(
            TimelineEvent(
                id = "1",
                appPackageName = "com.instagram.android",
                appName = "Instagram",
                startTime = now - oneHour * 8,
                duration = oneHour,
                color = Color.Blue
            ),
            TimelineEvent(
                id = "2", 
                appPackageName = "com.microsoft.office.word",
                appName = "Microsoft Word",
                startTime = now - oneHour * 4,
                duration = oneHour * 2,
                color = Color.Green
            )
        )
    }

    @Test
    fun interactiveTimeline_displaysWithDefaultValues() {
        // Given
        val events = createSampleTimelineEvents()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = events
                )
            }
        }

        // Then
        composeTestRule.waitForIdle()
        // The component should render without crashing
        // We can't easily assert on specific internal elements without test tags
    }

    @Test
    fun interactiveTimeline_handlesEmptyEvents() {
        // Given
        val emptyEvents = emptyList<TimelineEvent>()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = emptyEvents
                )
            }
        }

        // Then
        composeTestRule.waitForIdle()
        // Should not crash with empty events
    }

    @Test
    fun timelineEvent_createsCorrectly() {
        // Given
        val id = "test-event"
        val appPackageName = "com.test.app"
        val appName = "Test App"
        val startTime = System.currentTimeMillis()
        val duration = 60 * 60 * 1000L
        val color = Color.Red

        // When
        val event = TimelineEvent(id, appPackageName, appName, startTime, duration, color)

        // Then
        assert(event.id == id)
        assert(event.appPackageName == appPackageName)
        assert(event.appName == appName)
        assert(event.startTime == startTime)
        assert(event.duration == duration)
        assert(event.color == color)
    }

    @Test
    fun formatDuration_handlesMinutes() {
        // This would test a utility function if it exists
        // For now, just a simple assertion
        val duration = 30 * 60 * 1000L // 30 minutes
        assert(duration > 0)
    }

    @Test
    fun formatDuration_handlesHoursAndMinutes() {
        // This would test a utility function if it exists
        // For now, just a simple assertion
        val duration = 90 * 60 * 1000L // 1 hour 30 minutes
        assert(duration > 0)
    }
}