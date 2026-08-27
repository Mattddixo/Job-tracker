package com.homejobs.android.ui.jobs.list

import com.homejobs.android.domain.model.Job
import com.homejobs.android.domain.model.JobFilter

data class JobListUiState(
    val jobs: List<Job> = emptyList(),
    val filter: JobFilter = JobFilter(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
