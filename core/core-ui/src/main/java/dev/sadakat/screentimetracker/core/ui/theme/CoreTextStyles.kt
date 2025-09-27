package dev.sadakat.screentimetracker.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

object CoreTextStyles {
    val screenTitle: TextStyle
        @Composable get() = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.Bold
        )

    val sectionHeader: TextStyle
        @Composable get() = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold
        )

    val cardTitle: TextStyle
        @Composable get() = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.SemiBold
        )

    val metricValue: TextStyle
        @Composable get() = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold
        )

    val metricLabel: TextStyle
        @Composable get() = MaterialTheme.typography.bodySmall

    val listItemTitle: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge.copy(
            fontWeight = FontWeight.Medium
        )

    val listItemSubtitle: TextStyle
        @Composable get() = MaterialTheme.typography.bodySmall

    val emptyStateTitle: TextStyle
        @Composable get() = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Medium
        )

    val emptyStateDescription: TextStyle
        @Composable get() = MaterialTheme.typography.bodyMedium
}