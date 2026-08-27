package com.homejobs.android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.homejobs.android.ui.jobs.detail.JobDetailScreen
import com.homejobs.android.ui.jobs.form.JobFormScreen
import com.homejobs.android.ui.jobs.list.JobListScreen

@Composable
fun HomeJobsNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.JOB_LIST) {
        composable(Routes.JOB_LIST) {
            JobListScreen(
                onJobClick = { id -> navController.navigate(Routes.jobDetail(id)) },
                onAddJobClick = { navController.navigate(Routes.jobFormCreate()) },
            )
        }
        composable(
            route = Routes.JOB_DETAIL,
            arguments = listOf(navArgument("jobId") { type = NavType.LongType }),
        ) {
            JobDetailScreen(
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate(Routes.jobFormEdit(id)) },
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
    }
}
