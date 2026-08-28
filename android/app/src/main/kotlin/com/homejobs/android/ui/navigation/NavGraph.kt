package com.homejobs.android.ui.navigation

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
import com.homejobs.android.ui.theme.CustomColors
import com.homejobs.android.ui.theme.ThemeMode

@Composable
fun HomeJobsNavGraph(
    navController: NavHostController = rememberNavController(),
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    customColors: CustomColors,
    onCustomColorsChange: (CustomColors) -> Unit,
) {
    NavHost(navController = navController, startDestination = Routes.JOB_LIST) {
        composable(Routes.JOB_LIST) {
            JobListScreen(
                onJobClick = { id -> navController.navigate(Routes.jobDetail(id)) },
                onAddJobClick = { navController.navigate(Routes.jobFormCreate()) },
                themeMode = themeMode,
                onOpenAppearance = { navController.navigate(Routes.APPEARANCE) },
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
    }
}
