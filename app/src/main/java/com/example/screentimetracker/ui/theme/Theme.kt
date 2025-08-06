package com.example.screentimetracker.ui.theme

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

private val DarkColorScheme = darkColorScheme(
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

private val LightColorScheme = lightColorScheme(
    primary = PlayfulPrimary,
    secondary = PlayfulSecondary,
    tertiary = VibrantOrange,
    background = Purple80.copy(alpha = 0.05f),
    surface = Purple40.copy(alpha = 0.02f),
    error = ErrorColor,
    onPrimary = Purple80,
    onSecondary = Purple80,
    onTertiary = Purple80,
    onBackground = Purple40,
    onSurface = Purple40,
    onError = Purple80
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
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
