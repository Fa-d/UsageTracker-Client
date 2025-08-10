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
import java.time.LocalDate
import java.time.YearMonth

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class HeatMapCalendarTest : BaseComposeInstrumentedTest() {

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
                HeatMapCalendar(
                    data = data,
                    title = "Test Calendar"
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Test Calendar")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapCalendar_handlesEmptyData() {
        // Given
        val emptyData = emptyList<HeatMapData>()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                HeatMapCalendar(
                    data = emptyData,
                    title = "Empty Calendar"
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Empty Calendar")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun heatMapData_createsCorrectly() {
        // Given
        val date = LocalDate.now()
        val value = 0.75f
        val displayValue = "5h 30m"
        val details = "Test details"

        // When
        val heatMapData = HeatMapData(date, value, displayValue, details)

        // Then
        assert(heatMapData.date == date)
        assert(heatMapData.value == value)
        assert(heatMapData.displayValue == displayValue)
        assert(heatMapData.details == details)
    }
}