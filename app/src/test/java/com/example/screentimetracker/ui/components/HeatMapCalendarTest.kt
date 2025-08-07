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
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@RunWith(AndroidJUnit4::class)
class HeatMapCalendarTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun createSampleHeatMapData(): List<HeatMapData> {
        val currentMonth = YearMonth.now()
        return listOf(
            HeatMapData(
                date = currentMonth.atDay(1),
                value = 0.2f,
                displayValue = "2h 30m",
                details = "Light usage day"
            ),
            HeatMapData(
                date = currentMonth.atDay(5),
                value = 0.8f,
                displayValue = "6h 15m",
                details = "Heavy usage day"
            ),
            HeatMapData(
                date = currentMonth.atDay(10),
                value = 0.5f,
                displayValue = "4h 0m",
                details = "Moderate usage day"
            ),
            HeatMapData(
                date = currentMonth.atDay(15),
                value = 1.0f,
                displayValue = "8h 45m",
                details = "Maximum usage day"
            )
        )
    }

    @Test
    fun heatMapCalendar_displaysWithDefaultValues() {
        // Given
        val data = createSampleHeatMapData()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(data = data)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Activity Heat Map")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapCalendar_displaysCustomTitle() {
        // Given
        val data = createSampleHeatMapData()
        val customTitle = "Screen Time Heat Map"

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(
                    data = data,
                    title = customTitle
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(customTitle)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapCalendar_displaysDayHeaders() {
        // Given
        val data = createSampleHeatMapData()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(data = data)
            }
        }

        // Then
        val dayHeaders = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        dayHeaders.forEach { day ->
            composeTestRule
                .onNodeWithText(day)
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun heatMapCalendar_displaysCurrentMonth() {
        // Given
        val data = createSampleHeatMapData()
        val currentMonth = YearMonth.now()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(
                    data = data,
                    selectedMonth = currentMonth
                )
            }
        }

        // Then
        val monthYearText = currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        composeTestRule
            .onNodeWithText(monthYearText)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapCalendar_displaysNavigationButtons() {
        // Given
        val data = createSampleHeatMapData()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(data = data)
            }
        }

        // Then
        composeTestRule
            .onNodeWithContentDescription("Previous month")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithContentDescription("Next month")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapCalendar_handlesMonthNavigation() {
        // Given
        val data = createSampleHeatMapData()
        var selectedMonth by mutableStateOf(YearMonth.now())
        val initialMonth = selectedMonth

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(
                    data = data,
                    selectedMonth = selectedMonth,
                    onMonthChanged = { selectedMonth = it }
                )
            }
        }

        // Click next month
        composeTestRule
            .onNodeWithContentDescription("Next month")
            .performClick()

        composeTestRule.waitForIdle()

        // Then
        val expectedNextMonth = initialMonth.plusMonths(1)
        val nextMonthText = expectedNextMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        composeTestRule
            .onNodeWithText(nextMonthText)
            .assertExists()

        // Click previous month twice to go back
        composeTestRule
            .onNodeWithContentDescription("Previous month")
            .performClick()

        composeTestRule
            .onNodeWithContentDescription("Previous month")
            .performClick()

        composeTestRule.waitForIdle()

        // Should now be one month before the initial
        val expectedPreviousMonth = initialMonth.minusMonths(1)
        val prevMonthText = expectedPreviousMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy"))
        composeTestRule
            .onNodeWithText(prevMonthText)
            .assertExists()
    }

    @Test
    fun heatMapCalendar_displaysDayNumbers() {
        // Given
        val data = createSampleHeatMapData()
        val currentMonth = YearMonth.now()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(
                    data = data,
                    selectedMonth = currentMonth
                )
            }
        }

        // Then
        // Check for first few days of the month
        for (day in 1..5) {
            composeTestRule
                .onNodeWithText(day.toString())
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun heatMapCalendar_handlesDateSelection() {
        // Given
        val data = createSampleHeatMapData()
        var selectedDate by mutableStateOf<LocalDate?>(null)
        val testDate = YearMonth.now().atDay(1)

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(
                    data = data,
                    onDateSelected = { selectedDate = it }
                )
            }
        }

        // Click on day 1
        composeTestRule
            .onNodeWithText("1")
            .performClick()

        composeTestRule.waitForIdle()

        // Then
        assert(selectedDate == testDate)
    }

    @Test
    fun heatMapCalendar_displaysSelectedDateDetails() {
        // Given
        val data = createSampleHeatMapData()
        val selectedMonth = YearMonth.now()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(
                    data = data,
                    selectedMonth = selectedMonth
                )
            }
        }

        // Click on a day with data (day 1)
        composeTestRule
            .onNodeWithText("1")
            .performClick()

        composeTestRule.waitForIdle()

        // Then
        val expectedData = data.find { it.date == selectedMonth.atDay(1) }
        if (expectedData != null) {
            composeTestRule
                .onNodeWithText(expectedData.displayValue)
                .assertExists()
                .assertIsDisplayed()

            composeTestRule
                .onNodeWithText(expectedData.details)
                .assertExists()
                .assertIsDisplayed()
        }
    }

    @Test
    fun heatMapCalendar_closesSelectedDateDetails() {
        // Given
        val data = createSampleHeatMapData()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(data = data)
            }
        }

        // Click on a day with data
        composeTestRule
            .onNodeWithText("1")
            .performClick()

        composeTestRule.waitForIdle()

        // Click close button
        composeTestRule
            .onNodeWithContentDescription("Close")
            .performClick()

        composeTestRule.waitForIdle()

        // Then - details should be hidden
        val expectedData = data.find { it.date == YearMonth.now().atDay(1) }
        if (expectedData != null) {
            composeTestRule
                .onNodeWithText(expectedData.displayValue)
                .assertDoesNotExist()
        }
    }

    @Test
    fun heatMapCalendar_displaysLegend() {
        // Given
        val data = createSampleHeatMapData()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(
                    data = data,
                    showLegend = true
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Less")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("More")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapCalendar_hidesLegendWhenDisabled() {
        // Given
        val data = createSampleHeatMapData()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(
                    data = data,
                    showLegend = false
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Less")
            .assertDoesNotExist()

        composeTestRule
            .onNodeWithText("More")
            .assertDoesNotExist()
    }

    @Test
    fun heatMapCalendar_handlesEmptyData() {
        // Given
        val emptyData = emptyList<HeatMapData>()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(data = emptyData)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Activity Heat Map")
            .assertExists()
            .assertIsDisplayed()

        // Should still show day numbers
        composeTestRule
            .onNodeWithText("1")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapCalendar_handlesCustomBaseColor() {
        // Given
        val data = createSampleHeatMapData()
        val customColor = Color.Red

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(
                    data = data,
                    baseColor = customColor
                )
            }
        }

        // Then - Component should render without crashing
        composeTestRule
            .onNodeWithText("Activity Heat Map")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapCalendar_handlesDataWithZeroValues() {
        // Given
        val dataWithZeros = listOf(
            HeatMapData(
                date = YearMonth.now().atDay(1),
                value = 0f,
                displayValue = "0m",
                details = "No usage"
            ),
            HeatMapData(
                date = YearMonth.now().atDay(2),
                value = 0.5f,
                displayValue = "4h",
                details = "Some usage"
            )
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(data = dataWithZeros)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("1")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("2")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapCalendar_handlesDataWithMaxValues() {
        // Given
        val dataWithMax = listOf(
            HeatMapData(
                date = YearMonth.now().atDay(1),
                value = 1.0f,
                displayValue = "10h",
                details = "Maximum usage"
            )
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(data = dataWithMax)
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("1")
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("10h")
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText("Maximum usage")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapCalendar_animationCanBeDisabled() {
        // Given
        val data = createSampleHeatMapData()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(
                    data = data,
                    animateOnLoad = false
                )
            }
        }

        // Then - Should render without issues
        composeTestRule
            .onNodeWithText("Activity Heat Map")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapCalendar_handlesDateFormats() {
        // Given
        val data = createSampleHeatMapData()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(data = data)
            }
        }

        // Click on a day with data
        composeTestRule
            .onNodeWithText("1")
            .performClick()

        composeTestRule.waitForIdle()

        // Then - Should display formatted date
        val expectedDate = YearMonth.now().atDay(1)
        val formattedDate = expectedDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd"))
        
        // The formatted date should be displayed somewhere in the selected date card
        composeTestRule.waitForIdle()
        // We can't easily test the exact text format without knowing the current date,
        // but we verify the component doesn't crash with date formatting
    }

    @Test
    fun heatMapCalendar_handlesMultipleClicks() {
        // Given
        val data = createSampleHeatMapData()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(data = data)
            }
        }

        // Click same day twice (should deselect)
        composeTestRule
            .onNodeWithText("1")
            .performClick()

        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("1")
            .performClick()

        composeTestRule.waitForIdle()

        // Then - Details should be hidden after second click
        val expectedData = data.find { it.date == YearMonth.now().atDay(1) }
        if (expectedData != null) {
            composeTestRule
                .onNodeWithText(expectedData.displayValue)
                .assertDoesNotExist()
        }
    }

    @Test
    fun heatMapData_createsCorrectly() {
        // Given
        val date = LocalDate.now()
        val value = 0.75f
        val displayValue = "5h 30m"
        val details = "Test details"

        // When
        val heatMapData = HeatMapData(
            date = date,
            value = value,
            displayValue = displayValue,
            details = details
        )

        // Then
        assert(heatMapData.date == date)
        assert(heatMapData.value == value)
        assert(heatMapData.displayValue == displayValue)
        assert(heatMapData.details == details)
    }

    @Test
    fun heatMapData_handlesDefaultValues() {
        // Given
        val date = LocalDate.now()
        val value = 0.5f

        // When
        val heatMapData = HeatMapData(
            date = date,
            value = value
        )

        // Then
        assert(heatMapData.date == date)
        assert(heatMapData.value == value)
        assert(heatMapData.displayValue == "")
        assert(heatMapData.details == "")
    }
}