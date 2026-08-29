package com.homejobs.android.ui.navigation

import android.net.Uri

object Routes {
    const val JOB_LIST = "jobList"
    const val JOB_DETAIL = "jobDetail/{jobId}"
    const val JOB_FORM = "jobForm?jobId={jobId}&title={title}&category={category}&sourceJobJarId={sourceJobJarId}"
    const val JOB_PHOTOS = "jobPhotos/{jobId}?photoId={photoId}"
    const val APPEARANCE = "appearance"
    const val STATS = "stats"
    const val PAYMENT_METHODS = "paymentMethods"

    fun jobDetail(jobId: Long) = "jobDetail/$jobId"
    fun jobFormCreate() = "jobForm"
    fun jobFormEdit(jobId: Long) = "jobForm?jobId=$jobId"

    /** Lands on a blank create form pre-filled from a `hometracker://newjob` deep link. */
    fun jobFormFromDeepLink(title: String?, category: String?, sourceJobJarId: Long?): String {
        val params = buildList {
            title?.let { add("title=${Uri.encode(it)}") }
            category?.let { add("category=${Uri.encode(it)}") }
            sourceJobJarId?.let { add("sourceJobJarId=$it") }
        }
        return if (params.isEmpty()) "jobForm" else "jobForm?" + params.joinToString("&")
    }

    fun jobPhotos(jobId: Long, focusPhotoId: Long? = null) =
        if (focusPhotoId != null) "jobPhotos/$jobId?photoId=$focusPhotoId" else "jobPhotos/$jobId"
}
