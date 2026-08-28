package com.homejobs.android.data.local.prefs

import android.content.Context
import androidx.core.content.edit
import com.homejobs.android.ui.theme.ColorPalette
import com.homejobs.android.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val PREFS_NAME = "theme_prefs"
private const val KEY_THEME_MODE = "theme_mode"
private const val KEY_COLOR_PALETTE = "color_palette"

@Singleton
class ThemePreferences @Inject constructor(@ApplicationContext context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(readThemeMode())
    val themeMode = _themeMode.asStateFlow()

    private val _colorPalette = MutableStateFlow(readColorPalette())
    val colorPalette = _colorPalette.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit { putString(KEY_THEME_MODE, mode.name) }
        _themeMode.value = mode
    }

    fun setColorPalette(palette: ColorPalette) {
        prefs.edit { putString(KEY_COLOR_PALETTE, palette.name) }
        _colorPalette.value = palette
    }

    private fun readThemeMode(): ThemeMode {
        val stored = prefs.getString(KEY_THEME_MODE, null) ?: return ThemeMode.SYSTEM
        return runCatching { ThemeMode.valueOf(stored) }.getOrDefault(ThemeMode.SYSTEM)
    }

    private fun readColorPalette(): ColorPalette {
        val stored = prefs.getString(KEY_COLOR_PALETTE, null) ?: return ColorPalette.WORKSHOP
        return runCatching { ColorPalette.valueOf(stored) }.getOrDefault(ColorPalette.WORKSHOP)
    }
}
