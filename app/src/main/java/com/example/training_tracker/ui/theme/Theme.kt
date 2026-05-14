package com.example.training_tracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = CyanDark,
    secondary = CyanAccent,
    background = LightBackground,
    onSecondaryContainer = ExerciseCardBackgroundDark,
    surface = LightSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = LightText,
    onSurface = LightText,
    tertiary = GrayDarkText,
    outline = DarkOutlineText,
    surfaceTint = FadeCardBackgroundLight
)

private val DarkColorScheme = darkColorScheme(
    primary = CyanAccent,
    secondary = CyanDark,
    background = DarkBackground,
    onSecondaryContainer = ExerciseCardBackgroundLight,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = DarkText,
    onSurface = DarkText,
    tertiary = GrayWhiteText,
    outline = LightOutlineText
)

@Composable
fun Training_trackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

    val brushes = if (darkTheme) DarkBrushes else LightBrushes

    CompositionLocalProvider(
        LocalCustomBrushes provides brushes,
        LocalAccentTheme provides AccentThemes.Cyan
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}

data class CustomBrushes(
    val primaryGradient: Brush,
    val backgroundGradient: Brush
)

val LocalCustomBrushes = staticCompositionLocalOf<CustomBrushes> {
    error("No CustomBrushes provided")
}

val LocalAccentTheme = staticCompositionLocalOf<AccentTheme> {
    AccentThemes.Cyan
}

object AppTheme {
    val brushes: CustomBrushes
        @Composable
        get() = LocalCustomBrushes.current

    val accent: AccentTheme
        @Composable
        get() = LocalAccentTheme.current
}
