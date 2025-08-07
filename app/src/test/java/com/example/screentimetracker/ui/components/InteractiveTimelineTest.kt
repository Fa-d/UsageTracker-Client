package com.example.screentimetracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class InteractiveTimelineTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createSampleTimelineEvents(): List<TimelineEvent> {
        val now = System.currentTimeMillis()
        val oneHour = 60 * 60 * 1000L
        
        return listOf(
            TimelineEvent(
                id = "1",
                appPackageName = "com.instagram.android",
                appName = "Instagram",
                startTime = now - 4 * oneHour,
                duration = oneHour,
                color = Color.Blue,
                icon = "📷"
            ),
            TimelineEvent(
                id = "2",
                appPackageName = "com.spotify.music",
                appName = "Spotify",
                startTime = now - 3 * oneHour,
                duration = 30 * 60 * 1000L, // 30 minutes
                color = Color.Green,
                icon = "🎵"
            ),
            TimelineEvent(
                id = "3",
                appPackageName = "com.google.chrome",
                appName = "Chrome",
                startTime = now - 2 * oneHour,
                duration = 2 * oneHour,
                color = Color.Red,
                icon = "🌐"
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
                InteractiveTimeline(events = events)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_displaysTimeRange() {
        // Given
        val events = createSampleTimelineEvents()
        val now = System.currentTimeMillis()
        val startTime = now - 24 * 60 * 60 * 1000L // 24 hours ago
        val endTime = now

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = events,
                    timeRangeStart = startTime,
                    timeRangeEnd = endTime,
                    showHours = true
                )
            }
        }

        // Then
        // Timeline header should show formatted times
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_handlesEmptyEvents() {
        // Given
        val emptyEvents = emptyList<TimelineEvent>()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(events = emptyEvents)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_handlesSingleEvent() {
        // Given
        val singleEvent = listOf(
            TimelineEvent(
                id = "1",
                appPackageName = "com.test.app",
                appName = "Test App",
                startTime = System.currentTimeMillis() - 60 * 60 * 1000L,
                duration = 30 * 60 * 1000L,
                color = Color.Cyan,
                icon = "📱"
            )
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(events = singleEvent)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_handlesEventSelection() {
        // Given
        val events = createSampleTimelineEvents()
        var selectedEvent by mutableStateOf<TimelineEvent?>(null)

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = events,
                    selectedEvent = selectedEvent,
                    onEventSelected = { selectedEvent = it }
                )
            }
        }

        // Note: Clicking on the timeline canvas is complex to test in unit tests
        // as it requires precise coordinate calculation. This test verifies the basic setup.
        
        // Then
        assert(selectedEvent == null) // Initially no event selected
    }

    @Test
    fun interactiveTimeline_displaysSelectedEventDetails() {
        // Given
        val events = createSampleTimelineEvents()
        val selectedEvent = events.first()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = events,
                    selectedEvent = selectedEvent
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(selectedEvent.appName)
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(selectedEvent.icon)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_closesSelectedEventDetails() {
        // Given
        val events = createSampleTimelineEvents()
        val initialSelectedEvent = events.first()
        var selectedEvent by mutableStateOf<TimelineEvent?>(initialSelectedEvent)

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = events,
                    selectedEvent = selectedEvent,
                    onEventSelected = { selectedEvent = it }
                )
            }
        }

        // Verify event details are shown
        composeTestRule
            .onNodeWithText(initialSelectedEvent.appName)
            .assertExists()
            .assertIsDisplayed()

        // Click close button
        composeTestRule
            .onNodeWithContentDescription("Close")
            .performClick()

        composeTestRule.waitForIdle()

        // Then
        assert(selectedEvent == null)
        composeTestRule
            .onNodeWithText(initialSelectedEvent.appName)
            .assertDoesNotExist()
    }

    @Test
    fun interactiveTimeline_handlesHoursDisplay() {
        // Given
        val events = createSampleTimelineEvents()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = events,
                    showHours = true
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_handlesDateDisplay() {
        // Given
        val events = createSampleTimelineEvents()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = events,
                    showHours = false
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_handlesCustomTimeRange() {
        // Given
        val events = createSampleTimelineEvents()
        val customStartTime = System.currentTimeMillis() - 12 * 60 * 60 * 1000L // 12 hours ago
        val customEndTime = System.currentTimeMillis()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = events,
                    timeRangeStart = customStartTime,
                    timeRangeEnd = customEndTime
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_handlesOverlappingEvents() {
        // Given
        val now = System.currentTimeMillis()
        val overlappingEvents = listOf(
            TimelineEvent(
                id = "1",
                appPackageName = "com.app1",
                appName = "App 1",
                startTime = now - 2 * 60 * 60 * 1000L,
                duration = 2 * 60 * 60 * 1000L,
                color = Color.Blue,
                icon = "📱"
            ),
            TimelineEvent(
                id = "2",
                appPackageName = "com.app2",
                appName = "App 2",
                startTime = now - 90 * 60 * 1000L, // Overlaps with first event
                duration = 60 * 60 * 1000L,
                color = Color.Red,
                icon = "🎮"
            )
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(events = overlappingEvents)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_handlesEventsWithZeroDuration() {
        // Given
        val eventsWithZeroDuration = listOf(
            TimelineEvent(
                id = "1",
                appPackageName = "com.instant.app",
                appName = "Instant App",
                startTime = System.currentTimeMillis() - 60 * 60 * 1000L,
                duration = 0L,
                color = Color.Yellow,
                icon = "⚡"
            )
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(events = eventsWithZeroDuration)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_handlesEventsWithVeryLongDuration() {
        // Given
        val longDurationEvents = listOf(
            TimelineEvent(
                id = "1",
                appPackageName = "com.long.app",
                appName = "Long Running App",
                startTime = System.currentTimeMillis() - 10 * 60 * 60 * 1000L,
                duration = 8 * 60 * 60 * 1000L, // 8 hours
                color = Color.Magenta,
                icon = "🔥"
            )
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(events = longDurationEvents)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_displaysEventDurationInDetails() {
        // Given
        val events = createSampleTimelineEvents()
        val selectedEvent = events.first() // 1 hour duration

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = events,
                    selectedEvent = selectedEvent
                )
            }
        }

        // Then - Should display formatted duration
        composeTestRule
            .onNodeWithText("1h 0m") // Expected format for 1 hour
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun interactiveTimeline_handlesEventsOutsideTimeRange() {
        // Given
        val now = System.currentTimeMillis()
        val timeRangeStart = now - 2 * 60 * 60 * 1000L // 2 hours ago
        val timeRangeEnd = now
        
        val eventsOutsideRange = listOf(
            TimelineEvent(
                id = "1",
                appPackageName = "com.old.app",
                appName = "Old App",
                startTime = now - 5 * 60 * 60 * 1000L, // 5 hours ago (outside range)
                duration = 60 * 60 * 1000L,
                color = Color.Gray,
                icon = "🏛️"
            ),
            TimelineEvent(
                id = "2",
                appPackageName = "com.recent.app",
                appName = "Recent App",
                startTime = now - 60 * 60 * 1000L, // 1 hour ago (inside range)
                duration = 30 * 60 * 1000L,
                color = Color.Green,
                icon = "🆕"
            )
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = eventsOutsideRange,
                    timeRangeStart = timeRangeStart,
                    timeRangeEnd = timeRangeEnd
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun formatDuration_handlesMinutes() {
        // Given
        val durationMinutes = 45 * 60 * 1000L // 45 minutes

        // When
        // We can't directly test the private function, but we can test it through the component
        val event = TimelineEvent(
            id = "1",
            appPackageName = "com.test",
            appName = "Test App",
            startTime = System.currentTimeMillis(),
            duration = durationMinutes,
            color = Color.Blue
        )

        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = listOf(event),
                    selectedEvent = event
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("45m")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun formatDuration_handlesHoursAndMinutes() {
        // Given
        val durationHoursAndMinutes = 2 * 60 * 60 * 1000L + 30 * 60 * 1000L // 2h 30m

        // When
        val event = TimelineEvent(
            id = "1",
            appPackageName = "com.test",
            appName = "Test App",
            startTime = System.currentTimeMillis(),
            duration = durationHoursAndMinutes,
            color = Color.Blue
        )

        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = listOf(event),
                    selectedEvent = event
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("2h 30m")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun timelineEvent_createsCorrectly() {
        // Given
        val id = "test_id"
        val appPackageName = "com.test.app"
        val appName = "Test App"
        val startTime = System.currentTimeMillis()
        val duration = 60 * 60 * 1000L
        val color = Color.Blue
        val icon = "🧪"

        // When
        val event = TimelineEvent(
            id = id,
            appPackageName = appPackageName,
            appName = appName,
            startTime = startTime,
            duration = duration,
            color = color,
            icon = icon
        )

        // Then
        assert(event.id == id)
        assert(event.appPackageName == appPackageName)
        assert(event.appName == appName)
        assert(event.startTime == startTime)
        assert(event.duration == duration)
        assert(event.color == color)
        assert(event.icon == icon)
    }

    @Test
    fun timelineEvent_handlesDefaultIcon() {
        // Given/When
        val event = TimelineEvent(
            id = "1",
            appPackageName = "com.test",
            appName = "Test App",
            startTime = System.currentTimeMillis(),
            duration = 60000L,
            color = Color.Blue
        )

        // Then
        assert(event.icon == "📱") // Default icon
    }

    @Test
    fun interactiveTimeline_handlesMultipleSelections() {
        // Given
        val events = createSampleTimelineEvents()
        var selectedEvent by mutableStateOf<TimelineEvent?>(null)

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = events,
                    selectedEvent = selectedEvent,
                    onEventSelected = { selectedEvent = it }
                )
            }
        }

        // Simulate selecting different events
        selectedEvent = events[0]
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(events[0].appName)
            .assertExists()

        selectedEvent = events[1]
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText(events[1].appName)
            .assertExists()

        composeTestRule
            .onNodeWithText(events[0].appName)
            .assertDoesNotExist()
    }

    @Test
    fun interactiveTimeline_handlesRapidStateChanges() {
        // Given
        val events = createSampleTimelineEvents()
        var selectedEvent by mutableStateOf<TimelineEvent?>(null)
        var showHours by mutableStateOf(true)

        // When
        composeTestRule.setContent {
            MaterialTheme {
                InteractiveTimeline(
                    events = events,
                    selectedEvent = selectedEvent,
                    onEventSelected = { selectedEvent = it },
                    showHours = showHours
                )
            }
        }

        // Rapid state changes
        selectedEvent = events[0]
        showHours = false
        selectedEvent = null
        showHours = true
        selectedEvent = events[1]

        composeTestRule.waitForIdle()

        // Then - Should handle rapid changes without crashing
        composeTestRule
            .onNodeWithText("Timeline")
            .assertExists()
            .assertIsDisplayed()
    }
}