package com.homejobs.android.ui.navigation

import android.net.Uri

/**
 * What an incoming `hometracker://...` Uri (from Job Jar's "Send to Job Tracker" button, or any
 * other app) is asking this app to do. Parsed explicitly here — rather than relying on
 * Navigation-Compose's declarative `navDeepLink` auto-matching — so the whole handoff is plain,
 * readable code: [MainActivity] parses the incoming Uri once and calls `navController.navigate`
 * itself, for both cold start and an already-running instance ([android.app.Activity.onNewIntent]).
 */
sealed interface IncomingDeepLink {
    /** `hometracker://newjob?title=...&category=...&sourceId=...` */
    data class CreateJob(val title: String?, val category: String?, val sourceJobJarId: Long?) : IncomingDeepLink

    /** `hometracker://job/{jobId}` */
    data class ViewJob(val jobId: Long) : IncomingDeepLink
}

fun parseIncomingDeepLink(uri: Uri): IncomingDeepLink? = when (uri.host) {
    "newjob" -> IncomingDeepLink.CreateJob(
        title = uri.getQueryParameter("title"),
        category = uri.getQueryParameter("category"),
        sourceJobJarId = uri.getQueryParameter("sourceId")?.toLongOrNull(),
    )
    "job" -> uri.pathSegments.firstOrNull()?.toLongOrNull()?.let { IncomingDeepLink.ViewJob(it) }
    else -> null
}
