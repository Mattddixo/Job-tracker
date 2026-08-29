package com.homejobs.android.ui.navigation

import android.net.Uri

/**
 * What an incoming `hometracker://...` Uri (from Job Jar's "Send to Job Tracker"/"Link to
 * existing Job Tracker job" buttons, or any other app) is asking this app to do. Parsed
 * explicitly here — rather than relying on Navigation-Compose's declarative `navDeepLink`
 * auto-matching — so the whole handoff is plain, readable code: [MainActivity] parses the
 * incoming Uri once and calls `navController.navigate` itself, for both cold start and an
 * already-running instance ([android.app.Activity.onNewIntent]).
 */
sealed interface IncomingDeepLink {
    /** `hometracker://newjob?title=...&category=...&sourceId=...` */
    data class CreateJob(val title: String?, val category: String?, val sourceJobJarId: Long?) : IncomingDeepLink

    /** `hometracker://job/{jobId}` */
    data class ViewJob(val jobId: Long) : IncomingDeepLink

    /** `hometracker://pickjob?returnJobId=...` — Job Jar wants the user to pick an existing
     * Tracker job to link its own job [returnJobId] to. */
    data class PickJob(val returnJobId: Long) : IncomingDeepLink

    /** `hometracker://linked?jobId=...&otherId=...` — the return callback once a link is
     * established from the other side: this app's own job [jobId] should remember [otherId] as
     * the Job Jar job it's now linked to. */
    data class Linked(val jobId: Long, val otherId: Long) : IncomingDeepLink
}

fun parseIncomingDeepLink(uri: Uri): IncomingDeepLink? = when (uri.host) {
    "newjob" -> IncomingDeepLink.CreateJob(
        title = uri.getQueryParameter("title"),
        category = uri.getQueryParameter("category"),
        sourceJobJarId = uri.getQueryParameter("sourceId")?.toLongOrNull(),
    )
    "job" -> uri.pathSegments.firstOrNull()?.toLongOrNull()?.let { IncomingDeepLink.ViewJob(it) }
    "pickjob" -> uri.getQueryParameter("returnJobId")?.toLongOrNull()?.let { IncomingDeepLink.PickJob(it) }
    "linked" -> {
        val jobId = uri.getQueryParameter("jobId")?.toLongOrNull()
        val otherId = uri.getQueryParameter("otherId")?.toLongOrNull()
        if (jobId != null && otherId != null) IncomingDeepLink.Linked(jobId, otherId) else null
    }
    else -> null
}
