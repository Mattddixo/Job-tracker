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
