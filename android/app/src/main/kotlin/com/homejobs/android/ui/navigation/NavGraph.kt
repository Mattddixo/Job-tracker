package com.homejobs.android.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.homejobs.android.ui.appearance.AppearanceScreen
import com.homejobs.android.ui.jobs.detail.JobDetailScreen
import com.homejobs.android.ui.jobs.form.JobFormScreen
import com.homejobs.android.ui.jobs.list.JobListScreen
import com.homejobs.android.ui.jobs.photos.JobPhotosScreen
import com.homejobs.android.ui.stats.PaymentMethodsScreen
import com.homejobs.android.ui.stats.StatsScreen
import com.homejobs.android.ui.theme.CustomColors
import com.homejobs.android.ui.theme.ThemeMode

private const val TRANSITION_DURATION_MS = 200

@Composable
fun HomeJobsNavGraph(
    navController: NavHostController = rememberNavController(),
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    customColors: CustomColors,
    onCustomColorsChange: (CustomColors) -> Unit,
) {
    // A plain slide (no fade/scale) instead of the library's default fade-through — the fade
    // briefly reveals the surface behind both screens, which reads as the page "flashing
    // brighter," and its longer duration makes navigation feel sluggish. A short slide is fast
    // and has no crossfade to flash.
    NavHost(
        navController = navController,
        startDestination = Routes.JOB_LIST,
        enterTransition = { slideInHorizontally(tween(TRANSITION_DURATION_MS)) { fullWidth -> fullWidth } },
        exitTransition = { slideOutHorizontally(tween(TRANSITION_DURATION_MS)) { fullWidth -> -fullWidth / 4 } },
        popEnterTransition = { slideInHorizontally(tween(TRANSITION_DURATION_MS)) { fullWidth -> -fullWidth / 4 } },
        popExitTransition = { slideOutHorizontally(tween(TRANSITION_DURATION_MS)) { fullWidth -> fullWidth } },
    ) {
        composable(Routes.JOB_LIST) {
            JobListScreen(
                onJobClick = { id -> navController.navigate(Routes.jobDetail(id)) },
                onAddJobClick = { navController.navigate(Routes.jobFormCreate()) },
                themeMode = themeMode,
                onOpenAppearance = { navController.navigate(Routes.APPEARANCE) },
                onOpenStats = { navController.navigate(Routes.STATS) },
                onOpenPaymentMethods = { navController.navigate(Routes.PAYMENT_METHODS) },
            )
        }
        composable(
            route = Routes.JOB_DETAIL,
            arguments = listOf(navArgument("jobId") { type = NavType.LongType }),
        ) { backStackEntry ->
            // A pending "scroll to this note" request left by JobPhotosScreen's "go to note"
            // button — consumed once so returning to this same entry later doesn't re-trigger it.
            val scrollToNoteId = remember(backStackEntry) {
                backStackEntry.savedStateHandle.remove<Long>("scrollToNoteId")
            }
            JobDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.jobFormEdit(id)) },
                onViewPhotos = { id, photoId -> navController.navigate(Routes.jobPhotos(id, photoId)) },
                scrollToNoteId = scrollToNoteId,
            )
        }
        composable(
            route = Routes.JOB_FORM,
            arguments = listOf(navArgument("jobId") { type = NavType.StringType; nullable = true }),
        ) {
            JobFormScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
        composable(
            route = Routes.JOB_PHOTOS,
            arguments = listOf(
                navArgument("jobId") { type = NavType.LongType },
                navArgument("photoId") { type = NavType.StringType; nullable = true },
            ),
        ) {
            JobPhotosScreen(
                onBack = { navController.popBackStack() },
                onGoToNote = { noteId ->
                    navController.previousBackStackEntry?.savedStateHandle?.set("scrollToNoteId", noteId)
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.APPEARANCE) {
            AppearanceScreen(
                onBack = { navController.popBackStack() },
                themeMode = themeMode,
                onThemeModeChange = onThemeModeChange,
                customColors = customColors,
                onCustomColorsChange = onCustomColorsChange,
            )
        }
        composable(Routes.STATS) {
            StatsScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.PAYMENT_METHODS) {
            PaymentMethodsScreen(onBack = { navController.popBackStack() })
        }
    }
}
