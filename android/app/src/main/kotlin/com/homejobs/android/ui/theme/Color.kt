package com.homejobs.android.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * A hand-picked "workshop" palette — deep slate-teal, warm clay, and quiet sage on warm neutral
 * paper — instead of stock Material swatches or Android 12+ wallpaper-extracted dynamic color
 * (deliberately not used here; see Theme.kt). Clay and sage double as the cost/time variance
 * colors: clay reads as "worth a look" rather than alarm-red for over-budget/over-time, sage as
 * a quiet, non-triumphant "on track" for under-budget/under-time.
 */

// Light scheme
val SlateTeal = Color(0xFF2E5C5E)
val OnSlateTeal = Color(0xFFFFFFFF)
val SlateTealContainer = Color(0xFFC9E4E1)
val OnSlateTealContainer = Color(0xFF0A2B2C)

val Sage = Color(0xFF6B7D5E)
val OnSage = Color(0xFFFFFFFF)
val SageContainer = Color(0xFFDCE6D1)
val OnSageContainer = Color(0xFF232B1B)

val Clay = Color(0xFFA65B3F)
val OnClay = Color(0xFFFFFFFF)
val ClayContainer = Color(0xFFF5D9CB)
val OnClayContainer = Color(0xFF3A1B0F)

val ErrorRed = Color(0xFFB3261E)
val OnErrorRed = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFF9DEDC)
val OnErrorContainer = Color(0xFF410E0B)

val PaperBackground = Color(0xFFFAF7F2)
val OnPaperBackground = Color(0xFF221F1B)
val PaperSurfaceVariant = Color(0xFFEAE3D9)
val OnPaperSurfaceVariant = Color(0xFF4D473E)
val PaperOutline = Color(0xFF7C7568)

// Dark scheme — a warm charcoal, not a cold near-black
val SlateTealDark = Color(0xFF8FCFCB)
val OnSlateTealDark = Color(0xFF053335)
val SlateTealContainerDark = Color(0xFF16494B)
val OnSlateTealContainerDark = Color(0xFFC9E4E1)

val SageDark = Color(0xFFB7C9A8)
val OnSageDark = Color(0xFF2C3320)
val SageContainerDark = Color(0xFF4F5C3F)
val OnSageContainerDark = Color(0xFFDCE6D1)

val ClayDark = Color(0xFFE8B39C)
val OnClayDark = Color(0xFF4A2415)
val ClayContainerDark = Color(0xFF66341F)
val OnClayContainerDark = Color(0xFFF5D9CB)

val ErrorRedDark = Color(0xFFF2B8B5)
val OnErrorRedDark = Color(0xFF601410)
val ErrorContainerDark = Color(0xFF8C1D18)
val OnErrorContainerDark = Color(0xFFF9DEDC)

val CharcoalBackground = Color(0xFF1B1D1B)
val OnCharcoalBackground = Color(0xFFE6E2DA)
val CharcoalSurfaceVariant = Color(0xFF3C3D37)
val OnCharcoalSurfaceVariant = Color(0xFFC6C2B6)
val CharcoalOutline = Color(0xFF8F8A7C)

// --- Alternate palettes (ColorPalette.kt) — same neutral paper/charcoal backgrounds and error
// colors as Workshop above; only the primary/secondary/tertiary accent families change. ---

// Terracotta — warm burnt-orange primary, khaki secondary, deep teal tertiary.
val TerracottaPrimary = Color(0xFFB15A34)
val OnTerracottaPrimary = Color(0xFFFFFFFF)
val TerracottaPrimaryContainer = Color(0xFFF6D9C8)
val OnTerracottaPrimaryContainer = Color(0xFF3D1B0C)
val TerracottaSecondary = Color(0xFF7A7048)
val OnTerracottaSecondary = Color(0xFFFFFFFF)
val TerracottaSecondaryContainer = Color(0xFFE8E2C8)
val OnTerracottaSecondaryContainer = Color(0xFF28230F)
val TerracottaTertiary = Color(0xFF3D6E68)
val OnTerracottaTertiary = Color(0xFFFFFFFF)
val TerracottaTertiaryContainer = Color(0xFFCFE6E1)
val OnTerracottaTertiaryContainer = Color(0xFF12312D)

val TerracottaPrimaryDark = Color(0xFFE7B398)
val OnTerracottaPrimaryDark = Color(0xFF4A2313)
val TerracottaPrimaryContainerDark = Color(0xFF7A3A20)
val OnTerracottaPrimaryContainerDark = Color(0xFFF6D9C8)
val TerracottaSecondaryDark = Color(0xFFD0C9A0)
val OnTerracottaSecondaryDark = Color(0xFF332E17)
val TerracottaSecondaryContainerDark = Color(0xFF5C5530)
val OnTerracottaSecondaryContainerDark = Color(0xFFE8E2C8)
val TerracottaTertiaryDark = Color(0xFFA9D2CC)
val OnTerracottaTertiaryDark = Color(0xFF0D2B27)
val TerracottaTertiaryContainerDark = Color(0xFF274E49)
val OnTerracottaTertiaryContainerDark = Color(0xFFCFE6E1)

// Forest — deep forest-green primary, mossy khaki secondary, warm amber tertiary.
val ForestPrimary = Color(0xFF2E6B4F)
val OnForestPrimary = Color(0xFFFFFFFF)
val ForestPrimaryContainer = Color(0xFFCCE8DA)
val OnForestPrimaryContainer = Color(0xFF0B2B1D)
val ForestSecondary = Color(0xFF6B6142)
val OnForestSecondary = Color(0xFFFFFFFF)
val ForestSecondaryContainer = Color(0xFFE6DFC4)
val OnForestSecondaryContainer = Color(0xFF241F0D)
val ForestTertiary = Color(0xFF8A5A2A)
val OnForestTertiary = Color(0xFFFFFFFF)
val ForestTertiaryContainer = Color(0xFFF1DCC0)
val OnForestTertiaryContainer = Color(0xFF331F09)

val ForestPrimaryDark = Color(0xFF9ED4B9)
val OnForestPrimaryDark = Color(0xFF0A3323)
val ForestPrimaryContainerDark = Color(0xFF1F5A40)
val OnForestPrimaryContainerDark = Color(0xFFCCE8DA)
val ForestSecondaryDark = Color(0xFFCBC29B)
val OnForestSecondaryDark = Color(0xFF332C14)
val ForestSecondaryContainerDark = Color(0xFF524A2A)
val OnForestSecondaryContainerDark = Color(0xFFE6DFC4)
val ForestTertiaryDark = Color(0xFFE3BE8C)
val OnForestTertiaryDark = Color(0xFF40270B)
val ForestTertiaryContainerDark = Color(0xFF6B461F)
val OnForestTertiaryContainerDark = Color(0xFFF1DCC0)

// Lavender — dusty plum primary, sage secondary, dusty rose tertiary.
val LavenderPrimary = Color(0xFF6B5A8E)
val OnLavenderPrimary = Color(0xFFFFFFFF)
val LavenderPrimaryContainer = Color(0xFFE5DBF5)
val OnLavenderPrimaryContainer = Color(0xFF241830)
val LavenderSecondary = Color(0xFF63775A)
val OnLavenderSecondary = Color(0xFFFFFFFF)
val LavenderSecondaryContainer = Color(0xFFDCE8D4)
val OnLavenderSecondaryContainer = Color(0xFF1E2B19)
val LavenderTertiary = Color(0xFF96586A)
val OnLavenderTertiary = Color(0xFFFFFFFF)
val LavenderTertiaryContainer = Color(0xFFF5DCE2)
val OnLavenderTertiaryContainer = Color(0xFF33121A)

val LavenderPrimaryDark = Color(0xFFCBB8EA)
val OnLavenderPrimaryDark = Color(0xFF372A4C)
val LavenderPrimaryContainerDark = Color(0xFF4E4069)
val OnLavenderPrimaryContainerDark = Color(0xFFE5DBF5)
val LavenderSecondaryDark = Color(0xFFB7C9AE)
val OnLavenderSecondaryDark = Color(0xFF2C3826)
val LavenderSecondaryContainerDark = Color(0xFF455038)
val OnLavenderSecondaryContainerDark = Color(0xFFDCE8D4)
val LavenderTertiaryDark = Color(0xFFE5B4C2)
val OnLavenderTertiaryDark = Color(0xFF4A2530)
val LavenderTertiaryContainerDark = Color(0xFF653C48)
val OnLavenderTertiaryContainerDark = Color(0xFFF5DCE2)
