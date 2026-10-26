package com.example.catering.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    // Основные акценты
    primary = SiestaAmber,
    onPrimary = SiestaBackgroundDark,

    secondary = SiestaTerracotta,
    onSecondary = SiestaBackgroundDark,

    tertiary = SiestaGold,
    onTertiary = SiestaBackgroundDark,

// Фон приложения
    background = SiestaBgMiddle,
    onBackground = SiestaTextPrimary,

// Карточки и поверхности
    surface = SiestaCardMiddle,
    onSurface = SiestaTextPrimary,

    surfaceVariant = SiestaCardTop,
    onSurfaceVariant = SiestaNavUnselected,

// Границы элементов
    outline = SiestaNavOutline,
    outlineVariant = SiestaNavOutline.copy(alpha = 0.5f),
    primaryContainer = SiestaGoldSurface,
    onPrimaryContainer = SiestaCtaGoldLight,
    secondaryContainer = SiestaCardTop,
    onSecondaryContainer = SiestaTextPrimary,
    tertiaryContainer = SiestaGoldSurface,
    onTertiaryContainer = SiestaGold,
    error = SiestaError,
    onError = SiestaBackgroundDark,
    errorContainer = SiestaErrorSurface,
    onErrorContainer = SiestaError,
    surfaceTint = SiestaAmber,
    inverseSurface = SiestaTextPrimary,
    inverseOnSurface = SiestaBgMiddle,
    inversePrimary = SiestaTerracotta,
    surfaceDim = SiestaBgMiddle,
    surfaceBright = SiestaSearchTop,
    surfaceContainerLowest = SiestaBgMiddle,
    surfaceContainerLow = SiestaCardBottom,
    surfaceContainer = SiestaCardMiddle,
    surfaceContainerHigh = SiestaCardTop,
    surfaceContainerHighest = SiestaSearchTop,
    scrim = SiestaBackgroundDark
)
private val LightColorScheme = lightColorScheme(
    primary = SiestaOlive,
    onPrimary = SiestaCream,

    secondary = SiestaTerracotta,
    onSecondary = SiestaCream,

    tertiary = SiestaGold,
    onTertiary = SiestaBackgroundDark,

    background = SiestaBackgroundLight,
    onBackground = SiestaTextPrimaryLight,

    surface = SiestaSurfaceLight,
    onSurface = SiestaTextPrimaryLight,

    surfaceVariant = SiestaSurfaceElevatedLight,
    onSurfaceVariant = SiestaTextSecondaryLight,

    outline = SiestaOutlineLight
)

@Composable
fun CateringTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}