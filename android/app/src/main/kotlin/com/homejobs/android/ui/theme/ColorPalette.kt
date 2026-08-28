package com.homejobs.android.ui.theme

import androidx.compose.ui.graphics.Color

/** A hand-picked accent palette a user can choose from the Appearance screen. */
enum class ColorPalette(val label: String) {
    WORKSHOP("Workshop"),
    TERRACOTTA("Terracotta"),
    FOREST("Forest"),
    LAVENDER("Lavender"),
}

/** The primary/secondary/tertiary triplet (+ "on" and container colors) a palette contributes. */
internal data class AccentColors(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val tertiary: Color,
    val onTertiary: Color,
    val tertiaryContainer: Color,
    val onTertiaryContainer: Color,
)

internal fun ColorPalette.lightAccents(): AccentColors = when (this) {
    ColorPalette.WORKSHOP -> AccentColors(
        primary = SlateTeal, onPrimary = OnSlateTeal,
        primaryContainer = SlateTealContainer, onPrimaryContainer = OnSlateTealContainer,
        secondary = Sage, onSecondary = OnSage,
        secondaryContainer = SageContainer, onSecondaryContainer = OnSageContainer,
        tertiary = Clay, onTertiary = OnClay,
        tertiaryContainer = ClayContainer, onTertiaryContainer = OnClayContainer,
    )
    ColorPalette.TERRACOTTA -> AccentColors(
        primary = TerracottaPrimary, onPrimary = OnTerracottaPrimary,
        primaryContainer = TerracottaPrimaryContainer, onPrimaryContainer = OnTerracottaPrimaryContainer,
        secondary = TerracottaSecondary, onSecondary = OnTerracottaSecondary,
        secondaryContainer = TerracottaSecondaryContainer, onSecondaryContainer = OnTerracottaSecondaryContainer,
        tertiary = TerracottaTertiary, onTertiary = OnTerracottaTertiary,
        tertiaryContainer = TerracottaTertiaryContainer, onTertiaryContainer = OnTerracottaTertiaryContainer,
    )
    ColorPalette.FOREST -> AccentColors(
        primary = ForestPrimary, onPrimary = OnForestPrimary,
        primaryContainer = ForestPrimaryContainer, onPrimaryContainer = OnForestPrimaryContainer,
        secondary = ForestSecondary, onSecondary = OnForestSecondary,
        secondaryContainer = ForestSecondaryContainer, onSecondaryContainer = OnForestSecondaryContainer,
        tertiary = ForestTertiary, onTertiary = OnForestTertiary,
        tertiaryContainer = ForestTertiaryContainer, onTertiaryContainer = OnForestTertiaryContainer,
    )
    ColorPalette.LAVENDER -> AccentColors(
        primary = LavenderPrimary, onPrimary = OnLavenderPrimary,
        primaryContainer = LavenderPrimaryContainer, onPrimaryContainer = OnLavenderPrimaryContainer,
        secondary = LavenderSecondary, onSecondary = OnLavenderSecondary,
        secondaryContainer = LavenderSecondaryContainer, onSecondaryContainer = OnLavenderSecondaryContainer,
        tertiary = LavenderTertiary, onTertiary = OnLavenderTertiary,
        tertiaryContainer = LavenderTertiaryContainer, onTertiaryContainer = OnLavenderTertiaryContainer,
    )
}

internal fun ColorPalette.darkAccents(): AccentColors = when (this) {
    ColorPalette.WORKSHOP -> AccentColors(
        primary = SlateTealDark, onPrimary = OnSlateTealDark,
        primaryContainer = SlateTealContainerDark, onPrimaryContainer = OnSlateTealContainerDark,
        secondary = SageDark, onSecondary = OnSageDark,
        secondaryContainer = SageContainerDark, onSecondaryContainer = OnSageContainerDark,
        tertiary = ClayDark, onTertiary = OnClayDark,
        tertiaryContainer = ClayContainerDark, onTertiaryContainer = OnClayContainerDark,
    )
    ColorPalette.TERRACOTTA -> AccentColors(
        primary = TerracottaPrimaryDark, onPrimary = OnTerracottaPrimaryDark,
        primaryContainer = TerracottaPrimaryContainerDark, onPrimaryContainer = OnTerracottaPrimaryContainerDark,
        secondary = TerracottaSecondaryDark, onSecondary = OnTerracottaSecondaryDark,
        secondaryContainer = TerracottaSecondaryContainerDark, onSecondaryContainer = OnTerracottaSecondaryContainerDark,
        tertiary = TerracottaTertiaryDark, onTertiary = OnTerracottaTertiaryDark,
        tertiaryContainer = TerracottaTertiaryContainerDark, onTertiaryContainer = OnTerracottaTertiaryContainerDark,
    )
    ColorPalette.FOREST -> AccentColors(
        primary = ForestPrimaryDark, onPrimary = OnForestPrimaryDark,
        primaryContainer = ForestPrimaryContainerDark, onPrimaryContainer = OnForestPrimaryContainerDark,
        secondary = ForestSecondaryDark, onSecondary = OnForestSecondaryDark,
        secondaryContainer = ForestSecondaryContainerDark, onSecondaryContainer = OnForestSecondaryContainerDark,
        tertiary = ForestTertiaryDark, onTertiary = OnForestTertiaryDark,
        tertiaryContainer = ForestTertiaryContainerDark, onTertiaryContainer = OnForestTertiaryContainerDark,
    )
    ColorPalette.LAVENDER -> AccentColors(
        primary = LavenderPrimaryDark, onPrimary = OnLavenderPrimaryDark,
        primaryContainer = LavenderPrimaryContainerDark, onPrimaryContainer = OnLavenderPrimaryContainerDark,
        secondary = LavenderSecondaryDark, onSecondary = OnLavenderSecondaryDark,
        secondaryContainer = LavenderSecondaryContainerDark, onSecondaryContainer = OnLavenderSecondaryContainerDark,
        tertiary = LavenderTertiaryDark, onTertiary = OnLavenderTertiaryDark,
        tertiaryContainer = LavenderTertiaryContainerDark, onTertiaryContainer = OnLavenderTertiaryContainerDark,
    )
}
