package com.homejobs.android.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils

/** The three accent colors a user picks for the Custom theme, via the color wheel. */
data class CustomColors(
    val primary: Color = SlateTeal,
    val secondary: Color = Sage,
    val tertiary: Color = Clay,
)

private fun Color.withHslLightness(lightness: Float): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(toArgb(), hsl)
    hsl[2] = lightness
    return Color(ColorUtils.HSLToColor(hsl))
}

private fun Color.contrastingOnColor(): Color =
    if (ColorUtils.calculateLuminance(toArgb()) < 0.5) Color.White else Color.Black

private data class Role(val color: Color, val onColor: Color, val container: Color, val onContainer: Color)

/** The picked color as-is for light mode, with a pastel container tint and a dark on-container. */
private fun Color.asLightRole(): Role = Role(
    color = this,
    onColor = contrastingOnColor(),
    container = withHslLightness(0.88f),
    onContainer = withHslLightness(0.16f),
)

/** A brightened tint of the picked color for dark mode, matching the built-in dark palette's
 * convention of light accents on a dark surface, with a muted container and pastel on-container. */
private fun Color.asDarkRole(): Role {
    val bright = withHslLightness(0.78f)
    return Role(
        color = bright,
        onColor = bright.contrastingOnColor(),
        container = withHslLightness(0.30f),
        onContainer = withHslLightness(0.90f),
    )
}

internal fun CustomColors.toLightScheme(): ColorScheme {
    val p = primary.asLightRole()
    val s = secondary.asLightRole()
    val t = tertiary.asLightRole()
    return lightColorScheme(
        primary = p.color, onPrimary = p.onColor, primaryContainer = p.container, onPrimaryContainer = p.onContainer,
        secondary = s.color, onSecondary = s.onColor, secondaryContainer = s.container, onSecondaryContainer = s.onContainer,
        tertiary = t.color, onTertiary = t.onColor, tertiaryContainer = t.container, onTertiaryContainer = t.onContainer,
        error = ErrorRed, onError = OnErrorRed, errorContainer = ErrorContainer, onErrorContainer = OnErrorContainer,
        background = PaperBackground, onBackground = OnPaperBackground,
        surface = PaperBackground, onSurface = OnPaperBackground,
        surfaceVariant = PaperSurfaceVariant, onSurfaceVariant = OnPaperSurfaceVariant,
        outline = PaperOutline,
    )
}

internal fun CustomColors.toDarkScheme(): ColorScheme {
    val p = primary.asDarkRole()
    val s = secondary.asDarkRole()
    val t = tertiary.asDarkRole()
    return darkColorScheme(
        primary = p.color, onPrimary = p.onColor, primaryContainer = p.container, onPrimaryContainer = p.onContainer,
        secondary = s.color, onSecondary = s.onColor, secondaryContainer = s.container, onSecondaryContainer = s.onContainer,
        tertiary = t.color, onTertiary = t.onColor, tertiaryContainer = t.container, onTertiaryContainer = t.onContainer,
        error = ErrorRedDark, onError = OnErrorRedDark, errorContainer = ErrorContainerDark, onErrorContainer = OnErrorContainerDark,
        background = CharcoalBackground, onBackground = OnCharcoalBackground,
        surface = CharcoalBackground, onSurface = OnCharcoalBackground,
        surfaceVariant = CharcoalSurfaceVariant, onSurfaceVariant = OnCharcoalSurfaceVariant,
        outline = CharcoalOutline,
    )
}
