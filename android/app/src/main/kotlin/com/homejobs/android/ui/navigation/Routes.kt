package com.homejobs.android.ui.navigation

object Routes {
    const val JOB_LIST = "jobList"
    const val JOB_DETAIL = "jobDetail/{jobId}"
    const val JOB_FORM = "jobForm?jobId={jobId}"
    const val SETTINGS = "settings"

    fun jobDetail(jobId: Long) = "jobDetail/$jobId"
    fun jobFormCreate() = "jobForm"
    fun jobFormEdit(jobId: Long) = "jobForm?jobId=$jobId"
}
