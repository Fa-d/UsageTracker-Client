package dev.sadakat.screentimetracker.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Default Light Color Scheme
private val DefaultLightColorScheme = lightColorScheme(
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
    onSurface = ColorfulOnSurface
)

// Default Dark Color Scheme
private val DefaultDarkColorScheme = darkColorScheme(
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

@Composable
fun ScreenTimeTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DefaultDarkColorScheme
        else -> DefaultLightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}