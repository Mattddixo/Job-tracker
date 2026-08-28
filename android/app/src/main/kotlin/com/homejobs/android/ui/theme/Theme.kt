package com.homejobs.android.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = SlateTeal,
    onPrimary = OnSlateTeal,
    primaryContainer = SlateTealContainer,
    onPrimaryContainer = OnSlateTealContainer,
    secondary = Sage,
    onSecondary = OnSage,
    secondaryContainer = SageContainer,
    onSecondaryContainer = OnSageContainer,
    tertiary = Clay,
    onTertiary = OnClay,
    tertiaryContainer = ClayContainer,
    onTertiaryContainer = OnClayContainer,
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

private val DarkColors = darkColorScheme(
    primary = SlateTealDark,
    onPrimary = OnSlateTealDark,
    primaryContainer = SlateTealContainerDark,
    onPrimaryContainer = OnSlateTealContainerDark,
    secondary = SageDark,
    onSecondary = OnSageDark,
    secondaryContainer = SageContainerDark,
    onSecondaryContainer = OnSageContainerDark,
    tertiary = ClayDark,
    onTertiary = OnClayDark,
    tertiaryContainer = ClayContainerDark,
    onTertiaryContainer = OnClayContainerDark,
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

/**
 * Always applies the hand-picked palette from Color.kt — Material You dynamic color
 * (Android 12+ wallpaper-extracted theming) is deliberately never used, since it's what makes
 * most Compose apps look interchangeable.
 */
@Composable
fun HomeJobsTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = Typography,
        content = content,
    )
}
