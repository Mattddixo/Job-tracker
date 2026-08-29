package com.homejobs.android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.homejobs.android.data.local.prefs.ThemePreferences
import com.homejobs.android.ui.navigation.HomeJobsNavGraph
import com.homejobs.android.ui.navigation.IncomingDeepLink
import com.homejobs.android.ui.navigation.Routes
import com.homejobs.android.ui.navigation.parseIncomingDeepLink
import com.homejobs.android.ui.theme.HomeJobsTrackerTheme
import com.homejobs.android.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    // Set once, on the first composition, so onNewIntent (which runs outside any composable) can
    // still reach the same NavController the graph is using.
    private lateinit var navController: NavHostController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navController = rememberNavController()
            this.navController = navController

            val themeMode by themePreferences.themeMode.collectAsState()
            val customColors by themePreferences.customColors.collectAsState()
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.CUSTOM -> isSystemInDarkTheme()
            }

            // Cold start via a hometracker:// deep link (e.g. Job Jar's "Send to Job Tracker").
            // A warm start (app already running) is handled by onNewIntent below instead.
            LaunchedEffect(Unit) {
                intent?.data?.let { uri -> parseIncomingDeepLink(uri)?.let { navigateTo(navController, it) } }
            }

            HomeJobsTrackerTheme(
                darkTheme = darkTheme,
                customColors = if (themeMode == ThemeMode.CUSTOM) customColors else null,
            ) {
                HomeJobsNavGraph(
                    navController = navController,
                    themeMode = themeMode,
                    onThemeModeChange = themePreferences::setThemeMode,
                    customColors = customColors,
                    onCustomColorsChange = themePreferences::setCustomColors,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri -> parseIncomingDeepLink(uri)?.let { navigateTo(navController, it) } }
    }

    private fun navigateTo(navController: NavHostController, link: IncomingDeepLink) {
        when (link) {
            is IncomingDeepLink.ViewJob -> navController.navigate(Routes.jobDetail(link.jobId))
            is IncomingDeepLink.CreateJob -> navController.navigate(
                Routes.jobFormFromDeepLink(link.title, link.category, link.sourceJobJarId),
            )
        }
    }
}
