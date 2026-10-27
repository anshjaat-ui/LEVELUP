package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LevelUpLightColorScheme = lightColorScheme(
    primary = SageGreen,
    onPrimary = PureWhite,
    primaryContainer = PaleSage,
    onPrimaryContainer = DeepCharcoal,
    secondary = MutedBlue,
    onSecondary = PureWhite,
    secondaryContainer = SoftSand,
    onSecondaryContainer = DeepCharcoal,
    tertiary = MutedBlue,
    background = WarmIvory,
    onBackground = DeepCharcoal,
    surface = PureWhite,
    onSurface = DeepCharcoal,
    surfaceVariant = PaleSage,
    onSurfaceVariant = MutedGrey,
    outline = WarmBorder,
    outlineVariant = WarmBorder.copy(alpha = 0.6f)
)

private val LevelUpDarkColorScheme = darkColorScheme(
    primary = SageGreen,
    onPrimary = PureWhite,
    primaryContainer = Color(0xFF2C3930),
    onPrimaryContainer = PaleSage,
    secondary = MutedBlue,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF293540),
    onSecondaryContainer = Color(0xFFD6E4F0),
    tertiary = MutedBlue,
    background = DarkBackground,
    onBackground = Color(0xFFE3E5E3),
    surface = DarkSurface,
    onSurface = Color(0xFFE3E5E3),
    surfaceVariant = Color(0xFF282E2A),
    onSurfaceVariant = Color(0xFFA6ACA7),
    outline = DarkBorder,
    outlineVariant = DarkBorder
)

@Composable
fun LevelUpTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) LevelUpDarkColorScheme else LevelUpLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
