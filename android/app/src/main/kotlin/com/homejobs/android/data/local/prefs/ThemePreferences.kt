package com.homejobs.android.data.local.prefs

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.edit
import com.homejobs.android.ui.theme.CustomColors
import com.homejobs.android.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "theme_prefs"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_CUSTOM_PRIMARY = "custom_primary"
private const val KEY_CUSTOM_SECONDARY = "custom_secondary"
private const val KEY_CUSTOM_TERTIARY = "custom_tertiary"

@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(readThemeMode())
    val themeMode = _themeMode.asStateFlow()

    private val _customColors = MutableStateFlow(readCustomColors())
    val customColors = _customColors.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
        _themeMode.value = mode
    }

    fun setCustomColors(colors: CustomColors) {
        prefs.edit {
            putInt(KEY_CUSTOM_PRIMARY, colors.primary.toArgb())
            putInt(KEY_CUSTOM_SECONDARY, colors.secondary.toArgb())
            putInt(KEY_CUSTOM_TERTIARY, colors.tertiary.toArgb())
        }
        _customColors.value = colors
    }

    private fun readThemeMode(): ThemeMode {
        val stored = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.LIGHT
        return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.LIGHT)
    }

    private fun readCustomColors(): CustomColors {
        val defaults = CustomColors()
        return CustomColors(
            primary = readColorOrDefault(KEY_CUSTOM_PRIMARY, defaults.primary),
            secondary = readColorOrDefault(KEY_CUSTOM_SECONDARY, defaults.secondary),
            tertiary = readColorOrDefault(KEY_CUSTOM_TERTIARY, defaults.tertiary),
        )
    }

    private fun readColorOrDefault(key: String, default: Color): Color =
        if (prefs.contains(key)) Color(prefs.getInt(key, 0)) else default
}
