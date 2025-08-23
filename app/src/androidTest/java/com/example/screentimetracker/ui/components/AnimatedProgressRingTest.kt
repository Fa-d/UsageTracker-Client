package dev.sadakat.screentimetracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.sadakat.screentimetracker.ui.base.BaseComposeInstrumentedTest
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AnimatedProgressRingTest : BaseComposeInstrumentedTest() {

    @Test
    fun animatedProgressRing_displaysWithDefaultValues() {
        // Given
        val progress = 0.5f

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(progress = progress)
            }
        }

        // Then - Component should render without crashing
        composeTestRule.waitForIdle()
        // No assertion needed - test passes if no exception is thrown
    }

    @Test
    fun animatedProgressRing_displaysProgressWithCustomValues() {
        // Given
        val progress = 0.75f
        val centerText = "75%"
        val centerSubtext = "Complete"

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(
                    progress = progress,
                    centerText = centerText,
                    centerSubtext = centerSubtext
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(centerText)
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(centerSubtext)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun animatedProgressRing_handlesZeroProgress() {
        // Given
        val progress = 0f
        val centerText = "0%"

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(
                    progress = progress,
                    centerText = centerText
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(centerText)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun animatedProgressRing_handlesFullProgress() {
        // Given
        val progress = 1f
        val centerText = "100%"

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(
                    progress = progress,
                    centerText = centerText
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(centerText)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun animatedProgressRing_displaysWithCustomSize() {
        // Given
        val progress = 0.6f
        val customSize = 200.dp
        val centerText = "Custom Size"

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(
                    progress = progress,
                    size = customSize,
                    centerText = centerText
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(centerText)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun multiProgressRing_displaysMultipleRings() {
        // Given
        val progressData = listOf(
            ProgressData(0.6f, Color.Red, "App 1"),
            ProgressData(0.8f, Color.Blue, "App 2"),
            ProgressData(0.4f, Color.Green, "App 3")
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                MultiProgressRing(
                    progressData = progressData,
                    centerContent = {
                        androidx.compose.material3.Text("Multi Ring")
                    }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Multi Ring")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun progressData_createsCorrectly() {
        // Given
        val progress = 0.65f
        val color = Color.Yellow
        val label = "Test App"

        // When
        val progressData = ProgressData(progress, color, label)

        // Then
        assert(progressData.progress == progress)
        assert(progressData.color == color)
        assert(progressData.label == label)
    }
}