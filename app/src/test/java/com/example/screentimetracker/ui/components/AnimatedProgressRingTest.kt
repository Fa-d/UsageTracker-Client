package com.example.screentimetracker.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AnimatedProgressRingTest {

    @get:Rule
    val composeTestRule = createComposeRule()

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

        // Then
        composeTestRule
            .onNodeWithTag("AnimatedProgressRing", useUnmergedTree = true)
            .assertExists()
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
    fun animatedProgressRing_handlesProgressGreaterThanOne() {
        // Given
        val progress = 1.5f
        val centerText = "150%"

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
    fun animatedProgressRing_handlesNegativeProgress() {
        // Given
        val progress = -0.5f
        val centerText = "Error"

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
    fun animatedProgressRing_displaysWithCustomColors() {
        // Given
        val progress = 0.8f
        val progressColor = Color.Red
        val backgroundColor = Color.Gray
        val centerText = "Custom Colors"

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(
                    progress = progress,
                    progressColor = progressColor,
                    backgroundColor = backgroundColor,
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
    fun animatedProgressRing_handlesEmptyText() {
        // Given
        val progress = 0.4f
        val centerText = ""
        val centerSubtext = ""

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
        // Should not crash and component should still exist
        // Since text is empty, we can't find specific text nodes
        // but the component should still be rendered
        composeTestRule.waitForIdle()
    }

    @Test
    fun animatedProgressRing_handlesLongText() {
        // Given
        val progress = 0.3f
        val longCenterText = "This is a very long text that might not fit"
        val longSubtext = "This is also a very long subtext"

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(
                    progress = progress,
                    centerText = longCenterText,
                    centerSubtext = longSubtext
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(longCenterText)
            .assertExists()
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithText(longSubtext)
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun animatedProgressRing_progressUpdateTriggersRecomposition() {
        // Given
        var progress by mutableStateOf(0.2f)
        val centerText = "Dynamic"

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(
                    progress = progress,
                    centerText = centerText,
                    animationDuration = 100 // Short duration for testing
                )
            }
        }

        // Verify initial state
        composeTestRule
            .onNodeWithText(centerText)
            .assertExists()

        // Change progress
        progress = 0.8f

        // Wait for recomposition and animation
        composeTestRule.waitForIdle()

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
    fun multiProgressRing_handlesEmptyProgressData() {
        // Given
        val progressData = emptyList<ProgressData>()

        // When
        composeTestRule.setContent {
            MaterialTheme {
                MultiProgressRing(
                    progressData = progressData,
                    centerContent = {
                        androidx.compose.material3.Text("Empty Data")
                    }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Empty Data")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun multiProgressRing_handlesSingleProgressData() {
        // Given
        val progressData = listOf(
            ProgressData(0.7f, Color.Magenta, "Single App")
        )

        // When
        composeTestRule.setContent {
            MaterialTheme {
                MultiProgressRing(
                    progressData = progressData,
                    centerContent = {
                        androidx.compose.material3.Text("Single Ring")
                    }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Single Ring")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun multiProgressRing_handlesCustomSize() {
        // Given
        val progressData = listOf(
            ProgressData(0.5f, Color.Cyan, "App")
        )
        val customSize = 180.dp

        // When
        composeTestRule.setContent {
            MaterialTheme {
                MultiProgressRing(
                    progressData = progressData,
                    size = customSize,
                    centerContent = {
                        androidx.compose.material3.Text("Custom Size")
                    }
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText("Custom Size")
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

    @Test
    fun animatedProgressRing_handlesMultipleStateChanges() {
        // Given
        var progress by mutableStateOf(0.1f)
        var centerText by mutableStateOf("Start")

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(
                    progress = progress,
                    centerText = centerText,
                    animationDuration = 50 // Very short for testing
                )
            }
        }

        // Initial state
        composeTestRule
            .onNodeWithText("Start")
            .assertExists()

        // Multiple updates
        progress = 0.3f
        centerText = "Middle"
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("Middle")
            .assertExists()

        progress = 0.9f
        centerText = "End"
        composeTestRule.waitForIdle()

        // Then
        composeTestRule
            .onNodeWithText("End")
            .assertExists()
            .assertIsDisplayed()
    }

    @Test
    fun animatedProgressRing_withGradientAndGlow() {
        // Given
        val progress = 0.7f
        val centerText = "Gradient Test"

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(
                    progress = progress,
                    centerText = centerText,
                    showGradient = true,
                    showGlow = true
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
    fun animatedProgressRing_withoutGradientAndGlow() {
        // Given
        val progress = 0.7f
        val centerText = "No Effects"

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(
                    progress = progress,
                    centerText = centerText,
                    showGradient = false,
                    showGlow = false
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
    fun animatedProgressRing_customStrokeWidth() {
        // Given
        val progress = 0.5f
        val centerText = "Custom Stroke"
        val strokeWidth = 20.dp

        // When
        composeTestRule.setContent {
            MaterialTheme {
                AnimatedProgressRing(
                    progress = progress,
                    centerText = centerText,
                    strokeWidth = strokeWidth
                )
            }
        }

        // Then
        composeTestRule
            .onNodeWithText(centerText)
            .assertExists()
            .assertIsDisplayed()
    }
}