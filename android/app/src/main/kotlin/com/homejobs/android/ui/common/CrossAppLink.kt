package com.homejobs.android.ui.common

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

/**
 * Launches an implicit `ACTION_VIEW` intent at Job Jar, showing a toast instead of crashing if
 * Job Jar isn't installed. Shared by every screen that hands a job off to (or bounces into)
 * Job Jar — see the `jobjar://` URI contract documented in this app's README.
 */
fun openInJobJar(context: Context, uri: Uri) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "Job Jar isn't installed", Toast.LENGTH_SHORT).show()
    }
}

/**
 * Fires the `jobjar://linked?jobId=&otherId=` return callback once this app has linked its own
 * [trackerJobId] to Job Jar's [jobJarId] — the other half of Job Jar's own `Linked` deep-link
 * case, telling it which Tracker job to remember.
 */
fun fireLinkedCallback(context: Context, jobJarId: Long, trackerJobId: Long) {
    val uri = Uri.parse("jobjar://linked").buildUpon()
        .appendQueryParameter("jobId", jobJarId.toString())
        .appendQueryParameter("otherId", trackerJobId.toString())
        .build()
    openInJobJar(context, uri)
}
