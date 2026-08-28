package com.homejobs.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private fun lightScheme(palette: ColorPalette): ColorScheme {
    val accents = palette.lightAccents()
    return lightColorScheme(
        primary = accents.primary,
        onPrimary = accents.onPrimary,
        primaryContainer = accents.primaryContainer,
        onPrimaryContainer = accents.onPrimaryContainer,
        secondary = accents.secondary,
        onSecondary = accents.onSecondary,
        secondaryContainer = accents.secondaryContainer,
        onSecondaryContainer = accents.onSecondaryContainer,
        tertiary = accents.tertiary,
        onTertiary = accents.onTertiary,
        tertiaryContainer = accents.tertiaryContainer,
        onTertiaryContainer = accents.onTertiaryContainer,
        error = ErrorRed,
        onError = OnErrorRed,
        errorContainer = ErrorContainer,
        onErrorContainer = OnErrorContainer,
        background = PaperBackground,
        onBackground = OnPaperBackground,
        surface = PaperBackground,
        onSurface = OnPaperBackground,
        surfaceVariant = PaperSurfaceVariant,
        onSurfaceVariant = OnPaperSurfaceVariant,
        outline = PaperOutline,
    )
}

private fun darkScheme(palette: ColorPalette): ColorScheme {
    val accents = palette.darkAccents()
    return darkColorScheme(
        primary = accents.primary,
        onPrimary = accents.onPrimary,
        primaryContainer = accents.primaryContainer,
        onPrimaryContainer = accents.onPrimaryContainer,
        secondary = accents.secondary,
        onSecondary = accents.onSecondary,
        secondaryContainer = accents.secondaryContainer,
        onSecondaryContainer = accents.onSecondaryContainer,
        tertiary = accents.tertiary,
        onTertiary = accents.onTertiary,
        tertiaryContainer = accents.tertiaryContainer,
        onTertiaryContainer = accents.onTertiaryContainer,
        error = ErrorRedDark,
        onError = OnErrorRedDark,
        errorContainer = ErrorContainerDark,
        onErrorContainer = OnErrorContainerDark,
        background = CharcoalBackground,
        onBackground = OnCharcoalBackground,
        surface = CharcoalBackground,
        onSurface = OnCharcoalBackground,
        surfaceVariant = CharcoalSurfaceVariant,
        onSurfaceVariant = OnCharcoalSurfaceVariant,
        outline = CharcoalOutline,
    )
}

/**
 * Applies one of the hand-picked palettes from Color.kt/ColorPalette.kt — Material You dynamic
 * color (Android 12+ wallpaper-extracted theming) is deliberately never used, since it's what
 * makes most Compose apps look interchangeable.
 */
@Composable
fun HomeJobsTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    palette: ColorPalette = ColorPalette.WORKSHOP,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) darkScheme(palette) else lightScheme(palette),
        typography = Typography,
        content = content,
    )
}
