package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PolishBluePrimary,
    onPrimary = Color.White,
    primaryContainer = PolishNavyDark,
    onPrimaryContainer = PolishBlueContainer,
    secondary = PolishBlueContainer,
    onSecondary = PolishNavyDark,
    secondaryContainer = PolishBlueLight.copy(alpha = 0.2f),
    onSecondaryContainer = PolishBlueContainer,
    background = DarkCanvas,
    onBackground = DarkTextPrimaryDark,
    surface = DarkSurface,
    onSurface = DarkTextPrimaryDark,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondaryDark,
    outline = BorderSubtle.copy(alpha = 0.2f),
    error = DebitRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = PolishBluePrimary,
    onPrimary = Color.White,
    primaryContainer = PolishBlueContainer,
    onPrimaryContainer = PolishNavyDark,
    secondary = PolishBluePrimary,
    onSecondary = Color.White,
    secondaryContainer = PolishBlueLight,
    onSecondaryContainer = PolishNavyDark,
    background = PolishCanvas,
    onBackground = DarkTextPrimary,
    surface = PolishCard,
    onSurface = DarkTextPrimary,
    surfaceVariant = SettledGrayLight,
    onSurfaceVariant = DarkTextSecondary,
    outline = BorderSubtle,
    error = DebitRed,
    onError = Color.White
)

@Composable
fun HisaabTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

