package dev.sadakat.screentimetracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.ColorScheme
import dev.sadakat.screentimetracker.core.data.local.entities.ColorScheme as AppColorScheme
import dev.sadakat.screentimetracker.core.data.local.ThemeMode
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Default Dark Color Scheme
private val DefaultDarkColorScheme = darkColorScheme(
    primary = PlayfulPrimary,
    secondary = PlayfulSecondary,
    tertiary = LavenderPurple,
    background = Purple80,
    surface = PurpleGrey80,
    error = ErrorColor,
    onPrimary = Purple80,
    onSecondary = PurpleGrey80,
    onTertiary = Pink80,
    onBackground = Purple40,
    onSurface = Purple40,
    onError = Purple80
)

// Colorful Dark Color Scheme
private val ColorfulDarkColorScheme = darkColorScheme(
    primary = ColorfulDarkPrimary,
    secondary = ColorfulDarkSecondary,
    tertiary = ColorfulDarkTertiary,
    background = ColorfulDarkBackground,
    surface = ColorfulDarkSurface,
    error = ColorfulError,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onTertiary = androidx.compose.ui.graphics.Color.Black,
    onBackground = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.87f),
    onSurface = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.87f),
    onError = androidx.compose.ui.graphics.Color.White
)

// Minimal Dark Color Scheme
private val MinimalDarkColorScheme = darkColorScheme(
    primary = MinimalDarkPrimary,
    secondary = MinimalDarkSecondary,
    tertiary = MinimalDarkTertiary,
    background = MinimalDarkBackground,
    surface = MinimalDarkSurface,
    error = MinimalError,
    onPrimary = androidx.compose.ui.graphics.Color.Black,
    onSecondary = androidx.compose.ui.graphics.Color.Black,
    onTertiary = androidx.compose.ui.graphics.Color.Black,
    onBackground = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.87f),
    onSurface = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.87f),
    onError = androidx.compose.ui.graphics.Color.White
)

// Default Light Color Scheme
private val DefaultLightColorScheme = lightColorScheme(
    primary = PlayfulPrimary,
    secondary = PlayfulSecondary,
    tertiary = VibrantOrange,
    background = Purple80.copy(alpha = 0.05f),
    surface = androidx.compose.ui.graphics.Color.White,
    error = ErrorColor,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White,
    onTertiary = androidx.compose.ui.graphics.Color.White,
    onBackground = AccessibleTextOnLight,
    onSurface = AccessibleTextOnLight,
    onError = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
    onSurfaceVariant = AccessibleTextSecondary,
    outline = AccessibleTextTertiary,
    outlineVariant = AccessibleTextTertiary.copy(alpha = 0.5f)
)

// Colorful Light Color Scheme
private val ColorfulLightColorScheme = lightColorScheme(
    primary = ColorfulPrimary,
    secondary = ColorfulSecondary,
    tertiary = ColorfulTertiary,
    background = ColorfulBackground,
    surface = ColorfulSurface,
    error = ColorfulError,
    onPrimary = ColorfulOnPrimary,
    onSecondary = ColorfulOnSecondary,
    onTertiary = ColorfulOnTertiary,
    onBackground = ColorfulOnBackground,
    onSurface = ColorfulOnSurface,
    onError = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = ColorfulSurface.copy(alpha = 0.8f),
    onSurfaceVariant = AccessibleTextSecondary,
    outline = AccessibleTextTertiary,
    outlineVariant = AccessibleTextTertiary.copy(alpha = 0.5f)
)

// Minimal Light Color Scheme
private val MinimalLightColorScheme = lightColorScheme(
    primary = MinimalPrimary,
    secondary = MinimalSecondary,
    tertiary = MinimalTertiary,
    background = MinimalBackground,
    surface = MinimalSurface,
    error = MinimalError,
    onPrimary = MinimalOnPrimary,
    onSecondary = MinimalOnSecondary,
    onTertiary = MinimalOnTertiary,
    onBackground = MinimalOnBackground,
    onSurface = MinimalOnSurface,
    onError = androidx.compose.ui.graphics.Color.White,
    surfaceVariant = MinimalSurface.copy(alpha = 0.8f),
    onSurfaceVariant = AccessibleTextSecondary,
    outline = AccessibleTextTertiary,
    outlineVariant = AccessibleTextTertiary.copy(alpha = 0.5f)
)

@Composable
fun ScreenTimeTrackerTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    colorScheme: AppColorScheme = AppColorScheme.DEFAULT,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    val materialColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> {
            when (colorScheme) {
                AppColorScheme.COLORFUL -> ColorfulDarkColorScheme
                AppColorScheme.MINIMAL -> MinimalDarkColorScheme
                AppColorScheme.DEFAULT -> DefaultDarkColorScheme
            }
        }
        else -> {
            when (colorScheme) {
                AppColorScheme.COLORFUL -> ColorfulLightColorScheme
                AppColorScheme.MINIMAL -> MinimalLightColorScheme
                AppColorScheme.DEFAULT -> DefaultLightColorScheme
            }
        }
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = materialColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility wrapper
@Composable
fun ScreenTimeTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    ScreenTimeTrackerTheme(
        themeMode = if (darkTheme) ThemeMode.DARK else ThemeMode.LIGHT,
        colorScheme = AppColorScheme.DEFAULT,
        dynamicColor = dynamicColor,
        content = content
    )
}
