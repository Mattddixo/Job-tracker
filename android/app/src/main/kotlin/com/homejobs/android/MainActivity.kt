package com.homejobs.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.homejobs.android.data.local.prefs.ThemePreferences
import com.homejobs.android.ui.navigation.HomeJobsNavGraph
import com.homejobs.android.ui.theme.HomeJobsTrackerTheme
import com.homejobs.android.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by themePreferences.themeMode.collectAsState()
            val customColors by themePreferences.customColors.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.CUSTOM -> isSystemInDarkTheme()
            }
            HomeJobsTrackerTheme(
                darkTheme = darkTheme,
                customColors = if (themeMode == ThemeMode.CUSTOM) customColors else null,
            ) {
                HomeJobsNavGraph(
                    themeMode = themeMode,
                    onThemeModeChange = themePreferences::setThemeMode,
                    customColors = customColors,
                    onCustomColorsChange = themePreferences::setCustomColors,
                )
            }
        }
    }
}
