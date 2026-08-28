package com.homejobs.android.ui.navigation

object Routes {
    const val JOB_LIST = "jobList"
    const val JOB_DETAIL = "jobDetail/{jobId}"
    const val JOB_FORM = "jobForm?jobId={jobId}"
    const val JOB_PHOTOS = "jobPhotos/{jobId}?photoId={photoId}"
    const val APPEARANCE = "appearance"

    fun jobDetail(jobId: Long) = "jobDetail/$jobId"
    fun jobFormCreate() = "jobForm"
    fun jobFormEdit(jobId: Long) = "jobForm?jobId=$jobId"
    fun jobPhotos(jobId: Long, focusPhotoId: Long? = null) =
        if (focusPhotoId != null) "jobPhotos/$jobId?photoId=$focusPhotoId" else "jobPhotos/$jobId"
}
