package dev.sadakat.screentimetracker.core.presentation.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Utilities for consistent theme usage and accessibility compliance
 */
object ThemeUtils {
    
    /**
     * Ensures proper contrast ratio between foreground and background colors.
     * Returns a color that meets WCAG AA standards (4.5:1 contrast ratio).
     */
    @Composable
    @ReadOnlyComposable
    fun accessibleTextColor(
        backgroundColor: Color,
        preferredColor: Color = MaterialTheme.colorScheme.onSurface
    ): Color {
        val backgroundLuminance = backgroundColor.luminance()
        val preferredLuminance = preferredColor.luminance()
        
        val contrastRatio = if (backgroundLuminance > preferredLuminance) {
            (backgroundLuminance + 0.05f) / (preferredLuminance + 0.05f)
        } else {
            (preferredLuminance + 0.05f) / (backgroundLuminance + 0.05f)
        }
        
        return if (contrastRatio >= 4.5f) {
            preferredColor
        } else {
            // Fall back to high contrast color
            if (backgroundColor.luminance() > 0.5f) {
                MaterialTheme.colorScheme.onSurface
            } else {
                Color.White
            }
        }
    }
    
    /**
     * Get appropriate surface color with proper elevation tinting
     */
    @Composable
    @ReadOnlyComposable
    fun surfaceColorAtElevation(elevation: Int): Color {
        return when (elevation) {
            0 -> MaterialTheme.colorScheme.surface
            1 -> MaterialTheme.colorScheme.surfaceVariant
            2 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
            3 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f)
        }
    }
    
    /**
     * Category colors that work well with all theme variants
     */
    @Composable
    @ReadOnlyComposable
    fun getCategoryColors(): List<Color> {
        return listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
    }
    
    /**
     * Status colors for different states (success, warning, error, info)
     */
    @Composable
    @ReadOnlyComposable
    fun statusColors(): StatusColors {
        return StatusColors(
            success = Color(0xFF4CAF50), // Material Green 500
            warning = Color(0xFFFF9800), // Material Orange 500
            error = MaterialTheme.colorScheme.error,
            info = MaterialTheme.colorScheme.primary
        )
    }
}

data class StatusColors(
    val success: Color,
    val warning: Color,
    val error: Color,
    val info: Color
)

/**
 * Extension functions for ColorScheme to provide additional semantic colors
 */
val ColorScheme.success: Color
    @Composable
    @ReadOnlyComposable
    get() = ThemeUtils.statusColors().success

val ColorScheme.warning: Color
    @Composable
    @ReadOnlyComposable
    get() = ThemeUtils.statusColors().warning

val ColorScheme.info: Color
    @Composable
    @ReadOnlyComposable
    get() = ThemeUtils.statusColors().info

/**
 * Get shadow color with proper opacity for the current theme
 */
val ColorScheme.shadow: Color
    @Composable
    @ReadOnlyComposable
    get() = onSurface.copy(alpha = 0.2f)